package com.springboot.scheduleapi.schedule.service;

import com.springboot.scheduleapi.schedule.dto.request.ScheduleCreateRequest;
import com.springboot.scheduleapi.schedule.dto.response.ScheduleResponse;
import com.springboot.scheduleapi.schedule.entity.Schedule;
import com.springboot.scheduleapi.schedule.repository.ScheduleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ScheduleService {


    private final ScheduleRepository scheduleRepository;

    public List<ScheduleResponse> getDaySchedule(LocalDate date){

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return scheduleRepository
                .findByStartTimeBetween(start, end)
                .stream()
                .map(ScheduleResponse::new)
                .toList();
    }

    public ScheduleResponse create(@Valid ScheduleCreateRequest request) {

        // 프론트에서도 체크하고 백엔드에서도 체크해야 함
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("startTime은 endTime보다 늦을 수 없습니다.");
        }

        // 2) DTO -> Entity
        Schedule schedule = Schedule.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .color(request.getColor())
                .location(request.getLocation())
                .build();

        // 3) 저장
        Schedule saved = scheduleRepository.save(schedule);

        // 4) Entity -> Response
        return ScheduleResponse.from(saved); // 또는 new ScheduleResponse(saved)

    }
}
