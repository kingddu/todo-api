package com.springboot.scheduleapi.schedule.repository;

import com.springboot.scheduleapi.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface ScheduleRepository  extends JpaRepository<Schedule, Long> {

    List<Schedule> findByStartTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );




}
