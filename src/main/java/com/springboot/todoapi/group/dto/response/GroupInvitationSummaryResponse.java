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
    private Long invitedByUserId;
    private String invitedByUserName;
    private List<String> memberEmails;

    public static GroupInvitationSummaryResponse of(
            GroupInvitation invitation,
            String groupName,
            String invitedByUserName,
            List<String> memberEmails
    ) {
        return GroupInvitationSummaryResponse.builder()
                .invitationId(invitation.getId())
                .groupId(invitation.getGroupId())
                .groupName(groupName)
                .status(invitation.getStatus().name())
                .expiresAt(invitation.getExpiresAt())
                .invitedByUserId(invitation.getInvitedByUserId())
                .invitedByUserName(invitedByUserName)
                .memberEmails(memberEmails)
                .build();
    }
}
