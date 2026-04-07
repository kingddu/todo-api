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

import com.springboot.todoapi.group.entity.GroupMember;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


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
        Todo todo = findTodoWithAccess(userId, todoId);

        todo.complete(userId);

        todoActionLogRepository.save(
                TodoActionLog.completed(todo, userId)
        );

        return TodoResponse.from(todo);
    }

    @Transactional
    public TodoResponse uncomplete(Long userId, Long todoId) {
        Todo todo = findTodoWithAccess(userId, todoId);

        todo.uncomplete();

        todoActionLogRepository.save(
                TodoActionLog.uncompleted(todo, userId)
        );

        return TodoResponse.from(todo);
    }

    // 본인이 만든 todo이거나, 해당 그룹의 활성 멤버이면 접근 허용
    private Todo findTodoWithAccess(Long userId, Long todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo를 찾을 수 없습니다."));

        if (todo.getUser().getId().equals(userId)) {
            return todo;
        }

        if (todo.getGroup() != null) {
            boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(
                    todo.getGroup().getId(), userId, GroupMemberStatus.ACTIVE);
            if (isMember) return todo;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }

    @Transactional(readOnly = true)
    public List<TodoResponse> getTodosByRange(Long userId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("조회 시작일은 종료일보다 이후일 수 없습니다.");
        }

        LocalDate today = LocalDate.now();

        // 1. 내가 만든 todo (개인 + 내가 만든 그룹 todo)
        List<Todo> myTodos = todoRepository
                .findByUser_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(userId, to, from);

        // 2. 내가 활성 멤버로 속한 그룹 ID 목록
        List<Long> myGroupIds = groupMemberRepository
                .findAllByUserIdAndStatus(userId, GroupMemberStatus.ACTIVE)
                .stream()
                .map(GroupMember::getGroupId)
                .toList();

        // 3. 해당 그룹들의 todo (다른 멤버가 만든 그룹 todo 포함)
        List<Todo> groupTodos = myGroupIds.isEmpty() ? List.of() :
                todoRepository.findByGroup_IdInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        myGroupIds, to, from);

        // 4. 합치고 중복 제거 (내가 만든 그룹 todo는 1·3 양쪽에 포함되므로)
        Map<Long, Todo> merged = new LinkedHashMap<>();
        for (Todo t : myTodos) merged.put(t.getId(), t);
        for (Todo t : groupTodos) merged.put(t.getId(), t);

        // 5. carryOver 이월 todo 추가 (오늘 이전/오늘 날짜 조회 시에만)
        // 조건: carryOver=true, completed=false, startDate <= min(to, today)
        if (!from.isAfter(today)) {
            LocalDate carryOverBound = to.isBefore(today) ? to : today;

            List<Todo> myCarryOvers = todoRepository
                    .findByUser_IdAndCarryOverTrueAndCompletedFalseAndStartDateLessThanEqual(userId, carryOverBound);
            for (Todo t : myCarryOvers) merged.put(t.getId(), t);

            if (!myGroupIds.isEmpty()) {
                List<Todo> groupCarryOvers = todoRepository
                        .findByGroup_IdInAndCarryOverTrueAndCompletedFalseAndStartDateLessThanEqual(myGroupIds, carryOverBound);
                for (Todo t : groupCarryOvers) merged.put(t.getId(), t);
            }
        }

        return merged.values().stream().map(TodoResponse::from).toList();
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