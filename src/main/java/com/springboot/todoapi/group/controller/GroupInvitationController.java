package com.springboot.todoapi.group.controller;

import com.springboot.todoapi.auth.security.CustomUserPrincipal;
import com.springboot.todoapi.group.dto.response.GroupInvitationSummaryResponse;
import com.springboot.todoapi.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-invitations")
public class GroupInvitationController {

    private final GroupService groupService;

    @Operation(
            summary = "내 그룹 초대 목록 조회",
            description = "현재 로그인한 사용자가 받은 대기 중(PENDING) 그룹 초대 목록을 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<List<GroupInvitationSummaryResponse>> getMyInvitations(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(groupService.getMyPendingInvitations(principal.getId(), principal.getEmail()));
    }

    @Operation(
            summary = "그룹 초대 수락",
            description = "지정한 그룹 초대를 수락하고 해당 그룹의 멤버로 가입합니다."
    )
    @PostMapping("/{invitationId}/accept")
    public ResponseEntity<Void> acceptInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "수락할 그룹 초대 ID", example = "1")
            @PathVariable Long invitationId
    ) {
        groupService.acceptInvitation(principal.getId(), principal.getEmail(), invitationId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "그룹 초대 거절",
            description = "지정한 그룹 초대를 거절합니다."
    )
    @PostMapping("/{invitationId}/reject")
    public ResponseEntity<Void> rejectInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "거절할 그룹 초대 ID", example = "1")
            @PathVariable Long invitationId
    ) {
        groupService.rejectInvitation(principal.getEmail(), invitationId);
        return ResponseEntity.ok().build();
    }
}
