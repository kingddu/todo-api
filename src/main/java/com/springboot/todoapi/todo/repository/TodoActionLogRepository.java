package com.springboot.todoapi.todo.repository;

import com.springboot.todoapi.todo.entity.TodoActionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoActionLogRepository extends JpaRepository<TodoActionLog, Long> {

    List<TodoActionLog> findByTodo_IdOrderByCreatedAtDesc(Long todoId);

    void deleteAllByTodo_Id(Long todoId);
}