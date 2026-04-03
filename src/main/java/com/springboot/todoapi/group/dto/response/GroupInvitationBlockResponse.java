package com.springboot.todoapi.group.dto.response;

import com.springboot.todoapi.group.entity.GroupInvitationBlock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "그룹 초대 차단 응답")
public class GroupInvitationBlockResponse {

    @Schema(description = "차단 ID", example = "1")
    private Long blockId;

    @Schema(description = "차단한 상대 사용자 ID", example = "2")
    private Long blockedUserId;

    @Schema(description = "차단 당시 이메일", example = "test@example.com")
    private String blockedEmail;

    @Schema(description = "차단 일시")
    private LocalDateTime createdAt;

    public static GroupInvitationBlockResponse from(GroupInvitationBlock block) {
        return GroupInvitationBlockResponse.builder()
                .blockId(block.getId())
                .blockedUserId(block.getBlockedUserId())
                .blockedEmail(block.getBlockedEmail())
                .createdAt(block.getCreatedAt())
                .build();
    }
}
