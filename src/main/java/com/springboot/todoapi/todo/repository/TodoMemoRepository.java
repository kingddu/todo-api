package com.springboot.todoapi.todo.repository;

import com.springboot.todoapi.todo.entity.TodoMemo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TodoMemoRepository extends JpaRepository<TodoMemo, Long> {

    List<TodoMemo> findAllByTodoIdOrderByCreatedAtAsc(Long todoId);

    Optional<TodoMemo> findByTodoIdAndUserId(Long todoId, Long userId);

    void deleteAllByTodoId(Long todoId);
}
