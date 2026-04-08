package com.springboot.todoapi.todo.service;

import com.springboot.todoapi.group.entity.GroupMemberStatus;
import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.group.entity.TodoGroupStatus;
import com.springboot.todoapi.group.repository.GroupMemberRepository;
import com.springboot.todoapi.group.repository.TodoGroupRepository;
import com.springboot.todoapi.todo.entity.TodoActionLog;
import com.springboot.todoapi.todo.entity.TodoActionType;
import com.springboot.todoapi.todo.repository.TodoActionLogRepository;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.springboot.todoapi.todo.dto.request.TodoCreateRequest;
import com.springboot.todoapi.todo.dto.request.TodoPatchRequest;
import com.springboot.todoapi.todo.dto.response.TodoEditLogResponse;
import com.springboot.todoapi.todo.dto.response.TodoEditLogResponse.FieldChange;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoActionLogRepository todoActionLogRepository;
    private final UserRepository userRepository;
    private final TodoGroupRepository todoGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ObjectMapper objectMapper;

    private static final String[] KO_DAY = {"일", "월", "화", "수", "목", "금", "토"};

    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.getMonthValue() + "." + date.getDayOfMonth() + ".(" + KO_DAY[date.getDayOfWeek().getValue() % 7] + ")";
    }

    private String formatPeriod(LocalDate start, LocalDate end) {
        if (start.equals(end)) return formatDate(start);
        return formatDate(start) + " ~ " + formatDate(end);
    }

    private String nvl(String value) {
        return value == null ? "(없음)" : value;
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

        int editCount = todoActionLogRepository.countByTodo_IdAndActionTypeIn(
                todoId, List.of(TodoActionType.UPDATED, TodoActionType.COMPLETED, TodoActionType.UNCOMPLETED));
        return TodoResponse.from(todo, editCount);
    }

    @Transactional
    public TodoResponse uncomplete(Long userId, Long todoId) {
        Todo todo = findTodoWithAccess(userId, todoId);

        todo.uncomplete();

        todoActionLogRepository.save(
                TodoActionLog.uncompleted(todo, userId)
        );

        int editCount = todoActionLogRepository.countByTodo_IdAndActionTypeIn(
                todoId, List.of(TodoActionType.UPDATED, TodoActionType.COMPLETED, TodoActionType.UNCOMPLETED));
        return TodoResponse.from(todo, editCount);
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

        List<Long> todoIds = new ArrayList<>(merged.keySet());
        Map<Long, Integer> editCounts = batchEditCounts(todoIds);

        return merged.values().stream()
                .map(t -> TodoResponse.from(t, editCounts.getOrDefault(t.getId(), 0)))
                .toList();
    }

    // 키워드 검색 (내 todo + 내가 속한 그룹 todo, 최신순)
    @Transactional(readOnly = true)
    public List<TodoResponse> searchTodos(Long userId, String q) {
        if (q == null || q.isBlank()) {
            return List.of();
        }
        String keyword = q.trim();

        List<Long> myGroupIds = groupMemberRepository
                .findAllByUserIdAndStatus(userId, GroupMemberStatus.ACTIVE)
                .stream()
                .map(GroupMember::getGroupId)
                .toList();

        Map<Long, Todo> merged = new LinkedHashMap<>();

        // 최신순(id desc)으로 내 todo 먼저
        for (Todo t : todoRepository.searchByUserId(userId, keyword)) {
            merged.put(t.getId(), t);
        }
        if (!myGroupIds.isEmpty()) {
            for (Todo t : todoRepository.searchByGroupIds(myGroupIds, keyword)) {
                merged.put(t.getId(), t);
            }
        }

        List<Long> todoIds = new ArrayList<>(merged.keySet());
        Map<Long, Integer> editCounts = batchEditCounts(todoIds);

        // 중복 제거 후 날짜 최신순(startDate desc) 재정렬
        return merged.values().stream()
                .sorted((a, b) -> {
                    int dateCompare = b.getStartDate().compareTo(a.getStartDate());
                    return dateCompare != 0 ? dateCompare : Long.compare(b.getId(), a.getId());
                })
                .map(t -> TodoResponse.from(t, editCounts.getOrDefault(t.getId(), 0)))
                .toList();
    }

    // 수정 이력 조회 (todo 소유자 또는 그룹 활성 멤버만)
    @Transactional(readOnly = true)
    public List<TodoEditLogResponse> getEditLogs(Long userId, Long todoId) {
        findTodoWithAccess(userId, todoId); // 접근 권한 확인

        List<TodoActionLog> logs = todoActionLogRepository
                .findByTodo_IdAndActionTypeInOrderByCreatedAtDesc(
                        todoId, List.of(TodoActionType.UPDATED, TodoActionType.COMPLETED, TodoActionType.UNCOMPLETED));

        if (logs.isEmpty()) return List.of();

        // 액터 userId → email 배치 조회
        List<Long> actorIds = logs.stream().map(TodoActionLog::getActorUserId).distinct().toList();
        Map<Long, String> emailMap = userRepository.findAllById(actorIds).stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));

        return logs.stream()
                .map(log -> {
                    List<FieldChange> changes = resolveChanges(log);
                    return TodoEditLogResponse.builder()
                            .logId(log.getId())
                            .changes(changes)
                            .actorEmail(emailMap.getOrDefault(log.getActorUserId(), "알 수 없음"))
                            .editedAt(log.getCreatedAt())
                            .build();
                })
                .toList();
    }

    private List<FieldChange> resolveChanges(TodoActionLog log) {
        if (log.getActionType() == TodoActionType.COMPLETED) {
            return List.of(new FieldChange("완료 상태", "미완료", "완료"));
        }
        if (log.getActionType() == TodoActionType.UNCOMPLETED) {
            return List.of(new FieldChange("완료 상태", "완료", "미완료"));
        }
        // UPDATED: description에 저장된 JSON 파싱
        try {
            return objectMapper.readValue(log.getDescription(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, FieldChange.class));
        } catch (Exception e) {
            return List.of();
        }
    }

    // todo ID 목록의 수정 횟수 배치 조회
    private Map<Long, Integer> batchEditCounts(List<Long> todoIds) {
        if (todoIds.isEmpty()) return Map.of();
        Map<Long, Integer> result = new HashMap<>();
        for (Object[] row : todoActionLogRepository.countUpdatesByTodoIds(todoIds)) {
            result.put((Long) row[0], ((Long) row[1]).intValue());
        }
        return result;
    }

    //등록한 todo 수정
    @Transactional
    public TodoResponse patchTodo(Long userId, Long todoId, JsonNode request) {
        Todo todo = todoRepository.findByIdAndUser_Id(todoId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 todo가 없습니다."));

        // 수정 전 값 캡처 (추적 대상: 제목, 내용, 기간, 미완료 시 이월)
        String beforeTitle    = todo.getTitle();
        String beforeContent  = todo.getContent();
        LocalDate beforeStart = todo.getStartDate();
        LocalDate beforeEnd   = todo.getEndDate();
        boolean beforeCarryOver = todo.isCarryOver();

        applyPatch(todo, request);
        validatePatchedTodo(todo);
        todoRepository.flush();

        // 변경된 필드만 FieldChange 목록으로 구성
        List<FieldChange> changes = new ArrayList<>();

        if (!Objects.equals(beforeTitle, todo.getTitle())) {
            changes.add(new FieldChange("제목", nvl(beforeTitle), nvl(todo.getTitle())));
        }
        if (!Objects.equals(beforeContent, todo.getContent())) {
            changes.add(new FieldChange("내용", nvl(beforeContent), nvl(todo.getContent())));
        }
        if (!beforeStart.equals(todo.getStartDate()) || !beforeEnd.equals(todo.getEndDate())) {
            changes.add(new FieldChange("기간",
                    formatPeriod(beforeStart, beforeEnd),
                    formatPeriod(todo.getStartDate(), todo.getEndDate())));
        }
        if (beforeCarryOver != todo.isCarryOver()) {
            changes.add(new FieldChange("미완료 시 이월",
                    beforeCarryOver ? "이월" : "없음",
                    todo.isCarryOver() ? "이월" : "없음"));
        }

        // 추적 대상 필드 변경이 있을 때만 로그 저장
        if (!changes.isEmpty()) {
            try {
                String description = objectMapper.writeValueAsString(changes);
                todoActionLogRepository.save(TodoActionLog.updated(todo, userId, description));
            } catch (Exception ignored) {}
        }

        int editCount = todoActionLogRepository.countByTodo_IdAndActionTypeIn(
                todoId, List.of(TodoActionType.UPDATED, TodoActionType.COMPLETED, TodoActionType.UNCOMPLETED));
        return TodoResponse.from(todo, editCount);
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