package com.springboot.todoapi.todo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TodoEditLogResponse {

    private Long logId;
    private List<FieldChange> changes;
    private String actorEmail;
    private LocalDateTime editedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldChange {
        private String label;
        private String before;
        private String after;
    }
}
