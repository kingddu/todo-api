package com.springboot.todoapi.todo.dto.response;

import com.springboot.todoapi.todo.entity.TodoMemo;
import com.springboot.todoapi.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class TodoMemoResponse {

    private Long memoId;
    private Long userId;
    private String userName;
    private String profileImageUrl;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TodoMemoResponse of(TodoMemo memo, User user) {
        return TodoMemoResponse.builder()
                .memoId(memo.getId())
                .userId(memo.getUserId())
                .userName(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .content(memo.getContent())
                .createdAt(memo.getCreatedAt())
                .updatedAt(memo.getUpdatedAt())
                .build();
    }
}
