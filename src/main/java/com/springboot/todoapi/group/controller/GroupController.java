package com.springboot.todoapi.group.controller;

import com.springboot.todoapi.group.dto.request.GroupCreateRequest;
import com.springboot.todoapi.group.dto.response.GroupResponse;
import com.springboot.todoapi.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody GroupCreateRequest request
    ) {
        Long userId = 1L; // 테스트용
        String userEmail = "jmj@clean.com"; // TODO: 로그인 구현 후 교체

        return ResponseEntity.ok(
                groupService.createGroup(userId, userEmail, request)
        );
    }
}