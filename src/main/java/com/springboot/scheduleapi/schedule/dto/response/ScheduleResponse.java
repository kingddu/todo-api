package com.springboot.scheduleapi.schedule.dto.response;

import com.springboot.scheduleapi.schedule.entity.Schedule;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ScheduleResponse {
    private final Long id;
    private final String title;
    private final String content;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final String color;
    private final String location;

    public ScheduleResponse(Schedule schedule) {
        this.id = schedule.getId();
        this.title = schedule.getTitle();
        this.content = schedule.getContent();
        this.startTime = schedule.getStartTime();
        this.endTime = schedule.getEndTime();
        this.color = schedule.getColor();
        this.location = schedule.getLocation();
    }

    public static ScheduleResponse from(Schedule schedule) {
        return new ScheduleResponse(schedule);
    }

}
