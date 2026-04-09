package com.springboot.todoapi.group.dto.response;


import com.springboot.todoapi.group.entity.GroupInvitation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupInvitationSummaryResponse {

    private Long invitationId;
    private Long groupId;
    private String groupName;
    private String status;
    private LocalDateTime expiresAt;
    private String description;
    private Long invitedByUserId;
    private String invitedByUserName;
    private String invitedByUserEmail;

    public static GroupInvitationSummaryResponse of(
            GroupInvitation invitation,
            String groupName,
            String description,
            String invitedByUserName,
            String invitedByUserEmail
    ) {
        return GroupInvitationSummaryResponse.builder()
                .invitationId(invitation.getId())
                .groupId(invitation.getGroupId())
                .groupName(groupName)
                .description(description)
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .invitedByUserId(invitation.getInvitedByUserId())
                .invitedByUserName(invitedByUserName)
                .invitedByUserEmail(invitedByUserEmail)
                .build();
    }
}
