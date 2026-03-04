package com.springboot.scheduleapi.schedule.controller;

import com.springboot.scheduleapi.schedule.dto.request.ScheduleCreateRequest;
import com.springboot.scheduleapi.schedule.dto.response.ScheduleResponse;
import com.springboot.scheduleapi.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 🔐 하루 일정 조회
    @GetMapping("/{date}")
    public List<ScheduleResponse> getDaySchedule(
            @PathVariable LocalDate date,
            @AuthenticationPrincipal Long userId
    ) {
        return scheduleService.getDaySchedule(userId, date);
    }

    // 🔐 일정 생성
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        ScheduleResponse created = scheduleService.create(userId, request);
        return ResponseEntity.ok(created);
    }

    // 🔐 일정 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        scheduleService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    // 🔐 일정 수정
    @PutMapping("/{id}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        ScheduleResponse updated = scheduleService.update(userId, id, request);
        return ResponseEntity.ok(updated);
    }
}