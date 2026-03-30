package com.springboot.todoapi.group.repository;

import com.springboot.todoapi.group.entity.TodoGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TodoGroupRepository extends JpaRepository<TodoGroup, Long> {
}
