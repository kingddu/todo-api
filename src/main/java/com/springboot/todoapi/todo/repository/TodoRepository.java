package com.springboot.todoapi.todo.repository;

import com.springboot.todoapi.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 특정 날짜 포함된 todo 조회
    List<Todo> findByUser_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId,
            LocalDate endDate,
            LocalDate startDate
    );

    // 기간 조회
    List<Todo> findByUser_IdAndStartDateBetween(
            Long userId,
            LocalDate start,
            LocalDate end
    );



    Optional<Todo> findByIdAndUser_Id(Long id, Long userId);
}
