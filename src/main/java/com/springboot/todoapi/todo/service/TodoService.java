package com.springboot.todoapi.todo.service;

import com.springboot.todoapi.group.entity.GroupMemberStatus;
import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.group.entity.TodoGroupStatus;
import com.springboot.todoapi.group.repository.GroupMemberRepository;
import com.springboot.todoapi.group.repository.TodoGroupRepository;
import com.springboot.todoapi.todo.entity.TodoActionLog;
import com.springboot.todoapi.todo.repository.TodoActionLogRepository;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoActionLogRepository todoActionLogRepository;
    private final UserRepository userRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final GroupMemberRepository groupMemberRepository;


    private String buildUpdateDescription(JsonNode request) {
        List<String> changedFields = new ArrayList<>();

        if (request.has("title")) changedFields.add("title");
        if (request.has("content")) changedFields.add("content");
        if (request.has("category")) changedFields.add("category");
        if (request.has("type")) changedFields.add("type");
        if (request.has("startDate")) changedFields.add("startDate");
        if (request.has("endDate")) changedFields.add("endDate");
        if (request.has("carryOver")) changedFields.add("carryOver");

        if (changedFields.isEmpty()) {
            return "Todo 수정";
        }

        return "수정 필드: " + String.join(", ", changedFields);
    }




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

        TodoGroup group = resolveGroup(request.getGroupId(), userId);

        Todo todo = Todo.builder()
                .title(request.getTitle())
                .content(normalizeNullableText(request.getContent()))
                .category(normalizeNullableText(request.getCategory()))
                .type(request.getType())
                .startDate(startDate)
                .endDate(endDate)
                .carryOver(Boolean.TRUE.equals(request.getCarryOver()))
                .user(user)
                .group(group)
                .build();

        Todo saved = todoRepository.save(todo);

        todoActionLogRepository.save(
                TodoActionLog.created(saved, userId)
        );

        return TodoResponse.from(saved);
    }

    private TodoGroup resolveGroup(Long groupId, Long userId) {
        if (groupId == null) {
            return null;
        }

        TodoGroup group = todoGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "그룹을 찾을 수 없습니다."));

        if (group.getStatus() == TodoGroupStatus.DISBANDED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해산된 그룹에는 Todo를 추가할 수 없습니다.");
        }

        boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(
                groupId, userId, GroupMemberStatus.ACTIVE
        );
        if (!isMember) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 그룹의 멤버가 아닙니다.");
        }

        return group;
    }

    @Transactional
    public void delete(Long userId, Long todoId) {
        Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 Todo가 없습니다."));

        todoActionLogRepository.deleteAllByTodo_Id(todoId);
        todoRepository.delete(todo);
    }

    @Transactional
    public TodoResponse complete(Long userId, Long todoId) {
        Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo를 찾을 수 없습니다."));

        todo.complete(userId);

        todoActionLogRepository.save(
                TodoActionLog.completed(todo, userId)
        );

        return TodoResponse.from(todo);
    }

    @Transactional
    public TodoResponse uncomplete(Long userId, Long todoId) {
        Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo를 찾을 수 없습니다."));

        todo.uncomplete();

        todoActionLogRepository.save(
                TodoActionLog.uncompleted(todo, userId)
        );

        return TodoResponse.from(todo);
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

        //수정한 이력도 로그로 save
        todoActionLogRepository.save(
                TodoActionLog.updated(todo, userId, buildUpdateDescription(request))
        );

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

}