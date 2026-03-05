package com.springboot.scheduleapi.schedule.repository;

import com.springboot.scheduleapi.schedule.dto.response.ScheduleResponse;
import com.springboot.scheduleapi.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository  extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUser_IdAndStartTimeBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );



    Optional<Schedule> findByUser_IdAndId(Long userId, Long scheduleId);




}
