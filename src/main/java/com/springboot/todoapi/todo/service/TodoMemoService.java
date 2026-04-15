package com.springboot.todoapi.todo.service;

import com.springboot.todoapi.group.entity.GroupMemberStatus;
import com.springboot.todoapi.group.repository.GroupMemberRepository;
import com.springboot.todoapi.todo.dto.response.TodoMemoResponse;
import com.springboot.todoapi.todo.entity.Todo;
import com.springboot.todoapi.todo.entity.TodoMemo;
import com.springboot.todoapi.todo.repository.TodoMemoRepository;
import com.springboot.todoapi.todo.repository.TodoRepository;
import com.springboot.todoapi.user.entity.User;
import com.springboot.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TodoMemoService {

    private final TodoRepository todoRepository;
    private final TodoMemoRepository todoMemoRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    @Transactional(readOnly = true)
    public List<TodoMemoResponse> getMemos(Long todoId, Long requestUserId) {
        validateAccess(todoId, requestUserId);

        List<TodoMemo> memos = todoMemoRepository.findAllByTodoIdOrderByCreatedAtAsc(todoId);

        Set<Long> userIds = memos.stream().map(TodoMemo::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return memos.stream()
                .map(memo -> {
                    User user = userMap.get(memo.getUserId());
                    if (user == null) return null;
                    return TodoMemoResponse.of(memo, user);
                })
                .filter(r -> r != null)
                .toList();
    }

    @Transactional
    public TodoMemoResponse saveMemo(Long todoId, Long userId, String content) {
        validateAccess(todoId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."));

        TodoMemo memo = todoMemoRepository.findByTodoIdAndUserId(todoId, userId)
                .orElse(null);

        if (memo == null) {
            memo = TodoMemo.builder()
                    .todoId(todoId)
                    .userId(userId)
                    .content(content)
                    .build();
        } else {
            memo.updateContent(content);
        }

        todoMemoRepository.save(memo);
        return TodoMemoResponse.of(memo, user);
    }

    @Transactional
    public void deleteMemo(Long todoId, Long userId) {
        TodoMemo memo = todoMemoRepository.findByTodoIdAndUserId(todoId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "메모를 찾을 수 없습니다."));

        todoMemoRepository.delete(memo);
    }

    private void validateAccess(Long todoId, Long userId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Todo를 찾을 수 없습니다."));

        if (todo.getUser().getId().equals(userId)) return;

        if (todo.getGroup() != null) {
            boolean isMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(
                    todo.getGroup().getId(), userId, GroupMemberStatus.ACTIVE);
            if (isMember) return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
    }
}
