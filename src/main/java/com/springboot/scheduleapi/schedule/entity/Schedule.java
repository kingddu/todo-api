package com.springboot.scheduleapi.schedule.entity;

import com.springboot.scheduleapi.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
                    String location,
                    User user) {

        this.title = title;
        this.content = content;
        this.color = color;
        this.startTime = startTime;
        this.endTime = endTime;
        this.location = location;
        this.user = user;
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
