package com.springboot.todoapi.todo.service;

import tools.jackson.databind.JsonNode;
import com.springboot.todoapi.todo.dto.request.TodoCreateRequest;
import com.springboot.todoapi.todo.dto.request.TodoPatchRequest;
import com.springboot.todoapi.todo.dto.response.TodoResponse;
import com.springboot.todoapi.todo.entity.Todo;
import com.springboot.todoapi.todo.entity.TodoType;
import com.springboot.todoapi.todo.repository.TodoRepository;
import com.springboot.todoapi.user.entity.User;
import com.springboot.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import javax.sql.DataSource;


@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    private final DataSource dataSource;



    // 🔐 하루 일정 조회 (로그인 사용자 기준)
    /*
    @Transactional(readOnly = true)
    public List<TodoResponse> getDaySchedule(Long userId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return scheduleRepository
                .findByUser_IdAndStartTimeBetween(userId, start, end)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

    // 🔐 단건 일정 조회 (로그인 사용자 기준)
    @Transactional(readOnly = true)
    public TodoResponse getScheduleById(Long userId, Long scheduleId) {

        System.out.println("userId=" + userId + ", scheduleId=" + scheduleId);

        Todo schedule = scheduleRepository.findByUser_IdAndId(userId, scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("일정을 찾을 수 없습니다."));
        return TodoResponse.from(schedule);
    }
*/

    // todo 생성
    @Transactional
    public TodoResponse create(Long userId, TodoCreateRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();


        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작일은 종료일보다 이후일 수 없습니다.");
        }

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .content(normalizeNullableText(request.getContent()))
                .category(normalizeNullableText(request.getCategory()))
                .type(request.getType())
                .startDate(startDate)
                .endDate(endDate)
                .carryOver(Boolean.TRUE.equals(request.getCarryOver()))
                .user(user)
                .build();

        Todo saved = todoRepository.save(todo);

        /*
        try {
            Connection conn = dataSource.getConnection();
            System.out.println(">>> REAL DB URL = " + conn.getMetaData().getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(">>> SAVED ID = " + saved.getId());
*/

        return TodoResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByRange(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일은 종료일보다 이후일 수 없습니다.");
        }

        return todoRepository
                .findByUser_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, to, from)
                .stream()
                .map(TodoResponse::from)
                .toList();


    }

    //등록한 todo 수정
    @Transactional
    public TodoResponse patchTodo(Long userId, Long todoId, JsonNode request) {
        Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 todo가 없습니다."));

        applyPatch(todo, request);
        validatePatchedTodo(todo);

        todoRepository.flush();
        return TodoResponse.from(todo);
    }

    private void applyPatch(Todo todo, JsonNode request) {
        if (request.has("title")) {
            String title = normalizeNullableText(request.get("title"));
            if (title == null) {
                throw new IllegalArgumentException("title은 null 또는 공백일 수 없습니다.");
            }
            todo.changeTitle(title);
        }

        if (request.has("content")) {
            todo.changeContent(normalizeNullableText(request.get("content")));
        }

        if (request.has("category")) {
            todo.changeCategory(normalizeNullableText(request.get("category")));
        }

        if (request.has("type")) {
            JsonNode node = request.get("type");
            if (node == null || node.isNull()) {
                throw new IllegalArgumentException("type은 null일 수 없습니다.");
            }
            todo.changeType(TodoType.valueOf(node.asText()));
        }

        if (request.has("startDate")) {
            JsonNode node = request.get("startDate");
            if (node == null || node.isNull()) {
                throw new IllegalArgumentException("startDate는 null일 수 없습니다.");
            }
            todo.changeStartDate(LocalDate.parse(node.asText()));
        }

        if (request.has("endDate")) {
            JsonNode node = request.get("endDate");
            if (node == null || node.isNull()) {
                throw new IllegalArgumentException("endDate는 null일 수 없습니다.");
            }
            todo.changeEndDate(LocalDate.parse(node.asText()));
        }

        if (request.has("carryOver")) {
            JsonNode node = request.get("carryOver");
            if (node == null || node.isNull()) {
                throw new IllegalArgumentException("carryOver는 true/false여야 합니다.");
            }
            todo.changeCarryOver(node.asBoolean());
        }
    }

    private void validatePatchedTodo(Todo todo) {
        if (todo.getTitle() == null || todo.getTitle().isBlank()) {
            throw new IllegalArgumentException("title은 비어 있을 수 없습니다.");
        }

        if (todo.getType() == null) {
            throw new IllegalArgumentException("type은 null일 수 없습니다.");
        }

        if (todo.getStartDate() == null) {
            throw new IllegalArgumentException("startDate는 null일 수 없습니다.");
        }

        if (todo.getEndDate() == null) {
            throw new IllegalArgumentException("endDate는 null일 수 없습니다.");
        }

        if (todo.getStartDate().isAfter(todo.getEndDate())) {
            throw new IllegalArgumentException("startDate는 endDate보다 늦을 수 없습니다.");
        }
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.isEmpty() ? null : value;
    }

    private String normalizeNullableText(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        return normalizeNullableText(node.asText());
    }

    // 🔐 일정 삭제 (본인 것만)
    /*
    @Transactional
    public void delete(Long userId, Long scheduleId) {

        Todo schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 삭제할 수 있습니다.");
        }

        scheduleRepository.delete(schedule);
    }
*/
    // 🔐 일정 수정 (본인 것만)
    /*
    @Transactional
    public TodoResponse update(Long userId, Long scheduleId, TodoCreateRequest request) {

        Todo schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 수정할 수 있습니다.");
        }

        schedule.update(
                request.getTitle(),
                request.getContent(),
                request.getColor(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation()
        );

        return TodoResponse.from(schedule);
    }

    public List<TodoResponse> getSchedulesInRange(Long userId, LocalDateTime start, LocalDateTime end) {


        return scheduleRepository
                .findByUser_IdAndStartTimeBetween(userId, start, end)
                .stream()
                .map(TodoResponse::from)
                .toList();
    }

     */
}