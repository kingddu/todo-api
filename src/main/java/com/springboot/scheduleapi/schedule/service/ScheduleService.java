package com.springboot.scheduleapi.schedule.service;

import com.springboot.scheduleapi.schedule.dto.request.ScheduleCreateRequest;
import com.springboot.scheduleapi.schedule.dto.response.ScheduleResponse;
import com.springboot.scheduleapi.schedule.entity.Schedule;
import com.springboot.scheduleapi.schedule.repository.ScheduleRepository;
import com.springboot.scheduleapi.user.entity.User;
import com.springboot.scheduleapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 🔐 하루 일정 조회 (로그인 사용자 기준)
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getDaySchedule(Long userId, LocalDate date) {

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        return scheduleRepository
                .findByUserIdAndStartTimeBetween(userId, start, end)
                .stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    // 🔐 일정 생성 (로그인 사용자 기준)
    @Transactional
    public ScheduleResponse create(Long userId, ScheduleCreateRequest request) {

        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new IllegalArgumentException("startTime은 endTime보다 늦을 수 없습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Schedule schedule = new Schedule(
                request.getTitle(),
                request.getContent(),
                request.getColor(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation(),
                user
        );

        Schedule saved = scheduleRepository.save(schedule);

        return ScheduleResponse.from(saved);
    }

    // 🔐 일정 삭제 (본인 것만)
    @Transactional
    public void delete(Long userId, Long scheduleId) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 삭제할 수 있습니다.");
        }

        scheduleRepository.delete(schedule);
    }

    // 🔐 일정 수정 (본인 것만)
    @Transactional
    public ScheduleResponse update(Long userId, Long scheduleId, ScheduleCreateRequest request) {

        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("해당 일정이 없습니다."));

        if (!schedule.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 일정만 수정할 수 있습니다.");
        }

        schedule.update(
                request.getTitle(),
                request.getContent(),
                request.getColor(),
                request.getStartTime(),
                request.getEndTime(),
                request.getLocation()
        );

        return ScheduleResponse.from(schedule);
    }
}