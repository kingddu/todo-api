package com.springboot.todoapi.todo.repository;

import com.springboot.todoapi.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    // 내 todo (날짜 범위)
    List<Todo> findByUser_IdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long userId,
            LocalDate endDate,
            LocalDate startDate
    );

    // 그룹 todo (날짜 범위) - 그룹원 공유 조회용
    List<Todo> findByGroup_IdInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Collection<Long> groupIds,
            LocalDate endDate,
            LocalDate startDate
    );

    // 이월(carryOver) 미완료 todo - 개인
    List<Todo> findByUser_IdAndCarryOverTrueAndCompletedFalseAndStartDateLessThanEqual(
            Long userId,
            LocalDate startDateBound
    );

    // 이월(carryOver) 미완료 todo - 그룹
    List<Todo> findByGroup_IdInAndCarryOverTrueAndCompletedFalseAndStartDateLessThanEqual(
            Collection<Long> groupIds,
            LocalDate startDateBound
    );

    Optional<Todo> findByIdAndUser_Id(Long id, Long userId);
}
