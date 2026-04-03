package com.springboot.todoapi.group.controller;

import com.springboot.todoapi.auth.security.CustomUserPrincipal;
import com.springboot.todoapi.group.dto.request.GroupInvitationBlockCreateRequest;
import com.springboot.todoapi.group.dto.response.GroupInvitationBlockResponse;
import com.springboot.todoapi.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/group-invitation-blocks")
public class GroupInvitationBlockController {

    private final GroupService groupService;

    @Operation(
            summary = "특정 사용자 그룹 초대 차단",
            description = "해당 사용자가 나를 어떤 그룹에도 초대하지 못하도록 차단합니다."
    )
    @PostMapping
    public ResponseEntity<Void> blockInviter(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody GroupInvitationBlockCreateRequest request
    ) {
        groupService.blockInviter(principal.getId(), principal.getEmail(), request.getEmail());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내 차단 목록 조회",
            description = "내가 차단한 그룹 초대 차단 목록을 조회합니다."
    )
    @GetMapping("/me")
    public ResponseEntity<List<GroupInvitationBlockResponse>> getMyBlocks(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(groupService.getMyInvitationBlocks(principal.getId()));
    }

    @Operation(
            summary = "그룹 초대 차단 해제",
            description = "지정한 차단 건을 해제합니다."
    )
    @DeleteMapping("/{blockId}")
    public ResponseEntity<Void> unblockInviter(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "차단 ID", example = "1")
            @PathVariable Long blockId
    ) {
        groupService.unblockInviter(principal.getId(), blockId);
        return ResponseEntity.ok().build();
    }
}
