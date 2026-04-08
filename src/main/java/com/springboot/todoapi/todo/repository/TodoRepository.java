package com.springboot.todoapi.todo.repository;

import com.springboot.todoapi.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // 키워드 검색 - 내 todo (title/content/category, 날짜 최신순)
    @Query("SELECT t FROM Todo t WHERE t.user.id = :userId AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.content) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.category) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY t.startDate DESC, t.id DESC")
    List<Todo> searchByUserId(@Param("userId") Long userId, @Param("q") String q);

    // 키워드 검색 - 그룹 todo (날짜 최신순)
    @Query("SELECT t FROM Todo t WHERE t.group.id IN :groupIds AND (" +
           "LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.content) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(t.category) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY t.startDate DESC, t.id DESC")
    List<Todo> searchByGroupIds(@Param("groupIds") Collection<Long> groupIds, @Param("q") String q);

    Optional<Todo> findByIdAndUser_Id(Long id, Long userId);
}
