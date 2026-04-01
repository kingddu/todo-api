package com.springboot.todoapi.group.dto.response;


import com.springboot.todoapi.group.entity.GroupInvitation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupInvitationSummaryResponse {

    private Long invitationId;
    private Long groupId;
    private String groupName;
    private String status;
    private LocalDateTime expiresAt;
    private Long invitedByUserId;

    public static GroupInvitationSummaryResponse of(GroupInvitation invitation, String groupName) {
        return GroupInvitationSummaryResponse.builder()
                .invitationId(invitation.getId())
                .groupId(invitation.getGroupId())
                .groupName(groupName)
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .invitedByUserId(invitation.getInvitedByUserId())
                .build();
    }
}
