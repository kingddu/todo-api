package com.springboot.scheduleapi.schedule.controller;

import com.springboot.scheduleapi.schedule.dto.request.ScheduleCreateRequest;
import com.springboot.scheduleapi.schedule.dto.response.ScheduleResponse;
import com.springboot.scheduleapi.schedule.entity.Schedule;
import com.springboot.scheduleapi.schedule.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    @GetMapping("/{date}")
    public List<ScheduleResponse> getDaySchedule(@PathVariable LocalDate date) {
        return scheduleService.getDaySchedule(date);
    }


    @PostMapping
    @Operation(summary = "포스트 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "사용자 없음")
    })
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody ScheduleCreateRequest request
    ) {
        ScheduleResponse created = scheduleService.create(request);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }




}
