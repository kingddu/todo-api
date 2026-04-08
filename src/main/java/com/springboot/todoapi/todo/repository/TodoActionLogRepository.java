package com.springboot.todoapi.todo.repository;

import com.springboot.todoapi.todo.entity.TodoActionLog;
import com.springboot.todoapi.todo.entity.TodoActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TodoActionLogRepository extends JpaRepository<TodoActionLog, Long> {

    List<TodoActionLog> findByTodo_IdOrderByCreatedAtDesc(Long todoId);

    List<TodoActionLog> findByTodo_IdAndActionTypeInOrderByCreatedAtDesc(Long todoId, Collection<TodoActionType> actionTypes);

    int countByTodo_IdAndActionTypeIn(Long todoId, Collection<TodoActionType> actionTypes);

    @Query("SELECT l.todo.id, COUNT(l) FROM TodoActionLog l " +
           "WHERE l.todo.id IN :todoIds AND l.actionType IN ('UPDATED', 'COMPLETED', 'UNCOMPLETED') " +
           "GROUP BY l.todo.id")
    List<Object[]> countUpdatesByTodoIds(@Param("todoIds") Collection<Long> todoIds);

    void deleteAllByTodo_Id(Long todoId);
}