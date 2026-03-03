package com.springboot.scheduleapi.schedule.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(length = 1000)
    private String content;

    private String color;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String location;

    @Builder
    public Schedule(String title,
                    String content,
                    String color,
                    LocalDateTime startTime,
                    LocalDateTime endTime,
                    String location) {

        this.title = title;
        this.content = content;
        this.color = color;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }

    public void update(String title,
                       String content,
                       String color,
                       LocalDateTime startTime,
                       LocalDateTime endTime,
                       String location) {

        this.title = title;
        this.content = content;
        this.color = color;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
    }
}
