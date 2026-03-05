package com.springboot.scheduleapi.schedule.controller;

import com.springboot.scheduleapi.schedule.dto.request.ScheduleCreateRequest;
import com.springboot.scheduleapi.schedule.dto.response.ScheduleResponse;
import com.springboot.scheduleapi.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 🔐 기간으로 조회 (구글 캘린더처럼 하루, 3일, 일주일, 한달)
    // 프론트에서 기간을 던지면 위의 기간들의 일정들을 보여줌
    @GetMapping
    public List<ScheduleResponse> getSchedulesInRange(
            @AuthenticationPrincipal Long userId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end

    ) {
        return scheduleService.getSchedulesInRange(userId, start, end);
    }


    // 🔐 하루 일정 조회
    @GetMapping("/day/{date}")
    public List<ScheduleResponse> getDaySchedule(
            @PathVariable LocalDate date,
            @AuthenticationPrincipal Long userId
    ) {
        return scheduleService.getDaySchedule(userId, date);
    }

    // 🔐 단건 조회
    @GetMapping("/{scheduleId}")
    public ScheduleResponse getTimeScheduleById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        return scheduleService.getScheduleById(userId, scheduleId);
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