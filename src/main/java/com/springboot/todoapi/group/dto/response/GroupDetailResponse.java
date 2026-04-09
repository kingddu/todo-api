package com.springboot.todoapi.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "그룹 상세 조회 응답")
public class GroupDetailResponse {

    @Schema(description = "그룹 ID", example = "1")
    private Long groupId;

    @Schema(description = "정식 그룹명", example = "family")
    private String groupName;

    @Schema(description = "그룹 소개", example = "정가네 todo")
    private String description;

    @Schema(description = "그룹 상태", example = "ACTIVE")
    private String groupStatus;

    @Schema(description = "그룹 최초 생성자 ID", example = "1")
    private Long creatorUserId;

    @Schema(description = "그룹 생성 일시")
    private LocalDateTime createdAt;

    @Schema(description = "현재 그룹장 사용자 ID", example = "2")
    private Long leaderUserId;

    @Schema(description = "그룹 멤버 상태 요약")
    private MemberSummary memberSummary;

    @Schema(description = "그룹 초대 상태 요약")
    private InvitationSummary invitationSummary;

    @Schema(description = "현재 활성 멤버 목록")
    private List<MemberInfo> members;

    @Schema(description = "대기 중인 초대 목록")
    private List<PendingInvitationInfo> pendingInvitations;

    @Getter
    @Builder
    public static class MemberSummary {
        @Schema(description = "활성 멤버 수", example = "3")
        private long activeCount;

        @Schema(description = "탈퇴 멤버 수", example = "1")
        private long leftCount;

        @Schema(description = "강퇴 멤버 수", example = "0")
        private long kickedCount;
    }

    @Getter
    @Builder
    public static class InvitationSummary {
        @Schema(description = "대기 중인 초대 수", example = "2")
        private long pendingCount;

        @Schema(description = "수락된 초대 수", example = "1")
        private long acceptedCount;

        @Schema(description = "거절된 초대 수", example = "0")
        private long rejectedCount;

        @Schema(description = "만료된 초대 수", example = "1")
        private long expiredCount;
    }

    @Getter
    @Builder
    public static class MemberInfo {
        @Schema(description = "사용자 ID", example = "2")
        private Long userId;

        @Schema(description = "사용자 이름", example = "홍길동")
        private String userName;

        @Schema(description = "사용자 이메일", example = "user@example.com")
        private String userEmail;

        @Schema(description = "그룹 내 역할", example = "LEADER")
        private String role;

        @Schema(description = "개인 alias", example = "가족방")
        private String aliasName;

        @Schema(description = "프로필 이미지 URL")
        private String profileImageUrl;

        @Schema(description = "그룹 가입 일시")
        private LocalDateTime joinedAt;
    }

    @Getter
    @Builder
    public static class PendingInvitationInfo {
        @Schema(description = "초대 ID", example = "10")
        private Long invitationId;

        @Schema(description = "초대한 이메일", example = "test@example.com")
        private String email;

        @Schema(description = "초대받은 사용자 ID (가입된 경우)")
        private Long userId;

        @Schema(description = "초대받은 사용자 이름 (가입된 경우)")
        private String userName;

        @Schema(description = "초대받은 사용자 프로필 이미지 URL")
        private String profileImageUrl;

        @Schema(description = "초대한 사용자 ID", example = "2")
        private Long invitedByUserId;

        @Schema(description = "초대 만료 일시")
        private LocalDateTime expiresAt;

        @Schema(description = "초대 생성 일시")
        private LocalDateTime createdAt;
    }
}