package com.springboot.todoapi.group.service;

import com.springboot.todoapi.group.dto.request.GroupCreateRequest;
import com.springboot.todoapi.group.dto.response.GroupResponse;
import com.springboot.todoapi.group.entity.GroupInvitation;
import com.springboot.todoapi.group.entity.GroupMember;
import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.group.repository.GroupInvitationRepository;
import com.springboot.todoapi.group.repository.GroupMemberRepository;
import com.springboot.todoapi.group.repository.TodoGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final TodoGroupRepository groupRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupResponse createGroup(Long userId, String userEmail, GroupCreateRequest request) {

        String normalizedGroupName = normalizeGroupName(request.getGroupName());

        if (groupRepository.existsByOwnerUserIdAndGroupName(userId, normalizedGroupName)) {
            throw new IllegalArgumentException("이미 같은 이름의 그룹이 존재합니다.");
        }

        validateDuplicateEmails(request.getInviteEmails());
        validateSelfInvite(userEmail, request.getInviteEmails());



        //그룹 마스터 저장
        TodoGroup group = TodoGroup.create(normalizedGroupName, userId);
        groupRepository.save(group);

        //그룹원 저장(처음엔 그룹리더만 저장)
        GroupMember leader = GroupMember.createLeader(group.getId(), userId);
        groupMemberRepository.save(leader);

        List<GroupInvitation> invitations = request.getInviteEmails().stream()
                .map(this::normalizeEmail)
                .map(email -> GroupInvitation.create(
                        group.getId(),
                        email,
                        userId,
                        LocalDateTime.now().plusDays(7)
                ))
                .toList();

        //그룹 초대 저장
        groupInvitationRepository.saveAll(invitations);

        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .ownerUserId(group.getOwnerUserId())
                .invitedEmails(
                        invitations.stream()
                                .map(GroupInvitation::getEmail)
                                .toList()
                )
                .build();
    }

    private void validateDuplicateEmails(List<String> inviteEmails) {
        Set<String> normalized = new LinkedHashSet<>();

        for (String email : inviteEmails) {
            String normalizedEmail = normalizeEmail(email);
            if (!normalized.add(normalizedEmail)) {
                throw new IllegalArgumentException("중복된 이메일이 있습니다: " + normalizedEmail);
            }
        }
    }

    private void validateSelfInvite(String userEmail, List<String> inviteEmails) {
        String normalizedUserEmail = normalizeEmail(userEmail);

        for (String email : inviteEmails) {
            if (normalizeEmail(email).equals(normalizedUserEmail)) {
                throw new IllegalArgumentException("본인 이메일은 초대할 수 없습니다.");
            }
        }
    }

    private String normalizeGroupName(String groupName) {
        return groupName.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}