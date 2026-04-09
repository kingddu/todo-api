package com.springboot.todoapi.group.controller;

import com.springboot.todoapi.auth.security.CustomUserPrincipal;
import com.springboot.todoapi.group.dto.request.GroupAliasUpdateRequest;
import com.springboot.todoapi.group.dto.request.GroupCreateRequest;
import com.springboot.todoapi.group.dto.request.GroupDescriptionUpdateRequest;
import com.springboot.todoapi.group.dto.request.GroupInviteRequest;
import com.springboot.todoapi.group.dto.request.GroupLeaderTransferRequest;
import com.springboot.todoapi.group.dto.request.GroupNameUpdateRequest;
import com.springboot.todoapi.group.dto.response.GroupDetailResponse;
import com.springboot.todoapi.group.dto.response.GroupResponse;
import com.springboot.todoapi.group.dto.response.MyGroupSummaryResponse;

import java.util.List;
import com.springboot.todoapi.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    @Operation(
            summary = "그룹 생성",
            description = "새 그룹을 생성하고, 입력한 이메일 목록으로 그룹 초대를 생성합니다."
    )
    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody GroupCreateRequest request
    ) {
        return ResponseEntity.ok(
                groupService.createGroup(principal.getId(), principal.getEmail(), request)
        );
    }

    @Operation(
            summary = "기존 그룹에 멤버 초대",
            description = "이미 생성된 그룹에 새로운 멤버를 이메일로 초대합니다."
    )
    @PostMapping("/{groupId}/invitations")
    public ResponseEntity<Void> inviteMembers(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "초대할 대상 그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Valid @RequestBody GroupInviteRequest request
    ) {
        groupService.inviteMembers(principal.getId(), principal.getEmail(), groupId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내 그룹 별칭 변경",
            description = "해당 그룹에 대해 내가 사용할 개인 별칭(alias)을 변경합니다."
    )
    @PatchMapping("/{groupId}/alias")
    public ResponseEntity<Void> changeMyAlias(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "별칭을 변경할 그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Valid @RequestBody GroupAliasUpdateRequest request
    ) {
        groupService.changeMyGroupAlias(principal.getId(), groupId, request.getAliasName());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "그룹장 위임",
            description = "현재 그룹장이 같은 그룹의 다른 활성 멤버에게 그룹장 권한을 위임합니다."
    )
    @PatchMapping("/{groupId}/leader")
    public ResponseEntity<Void> transferLeader(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "그룹장 위임이 발생할 그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Valid @RequestBody GroupLeaderTransferRequest request
    ) {
        groupService.transferLeader(principal.getId(), groupId, request.getTargetUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "그룹 나가기",
            description = "현재 로그인한 사용자가 그룹에서 나갑니다. 그룹장은 먼저 그룹장 위임 후 나갈 수 있습니다."
    )
    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<Void> leaveGroup(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "나갈 그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        groupService.leaveGroup(principal.getId(), groupId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "그룹 상세 조회",
            description = "현재 로그인한 사용자가 속한 그룹의 상세 정보, 현재 멤버 목록, 초대 상태 요약을 조회합니다."
    )
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "조회할 그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(groupService.getGroupDetail(principal.getId(), groupId));
    }

    @Operation(
            summary = "그룹원 강퇴",
            description = "현재 그룹장이 같은 그룹의 활성 멤버를 강퇴합니다. 그룹장은 본인을 강퇴할 수 없습니다."
    )
    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseEntity<Void> kickMember(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "강퇴할 대상 사용자 ID", example = "2")
            @PathVariable Long targetUserId
    ) {
        groupService.kickMember(principal.getId(), groupId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "공식 그룹명 변경",
            description = "현재 그룹장이 그룹의 공식 이름을 변경합니다."
    )
    @PatchMapping("/{groupId}/name")
    public ResponseEntity<Void> changeGroupName(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Valid @RequestBody GroupNameUpdateRequest request
    ) {
        groupService.changeGroupName(principal.getId(), groupId, request.getGroupName());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내 그룹 목록 조회",
            description = "현재 로그인한 사용자가 활성 상태로 속한 그룹 목록을 반환합니다."
    )
    @GetMapping
    public ResponseEntity<List<MyGroupSummaryResponse>> getMyGroups(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(groupService.getMyGroups(principal.getId()));
    }

    @Operation(
            summary = "초대 취소",
            description = "그룹장이 대기 중인 초대를 취소합니다."
    )
    @DeleteMapping("/{groupId}/invitations/{invitationId}")
    public ResponseEntity<Void> cancelInvitation(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long groupId,
            @PathVariable Long invitationId
    ) {
        groupService.cancelInvitation(principal.getId(), groupId, invitationId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "그룹 소개 변경",
            description = "현재 그룹장이 그룹 소개를 변경합니다. 빈 값이면 소개가 삭제됩니다."
    )
    @PatchMapping("/{groupId}/description")
    public ResponseEntity<Void> changeGroupDescription(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "그룹 ID", example = "1")
            @PathVariable Long groupId,
            @Valid @RequestBody GroupDescriptionUpdateRequest request
    ) {
        groupService.changeGroupDescription(principal.getId(), groupId, request.getDescription());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "그룹 해산",
            description = "현재 그룹장이 활성 그룹을 해산합니다."
    )
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> disbandGroup(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "해산할 그룹 ID", example = "1")
            @PathVariable Long groupId
    ) {
        groupService.disbandGroup(principal.getId(), groupId);
        return ResponseEntity.ok().build();
    }
}
