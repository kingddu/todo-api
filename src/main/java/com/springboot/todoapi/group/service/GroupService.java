package com.springboot.todoapi.group.service;

import com.springboot.todoapi.group.dto.request.GroupCreateRequest;
import com.springboot.todoapi.group.dto.request.GroupInviteRequest;
import com.springboot.todoapi.group.dto.response.GroupDetailResponse;
import com.springboot.todoapi.group.dto.response.GroupInvitationBlockResponse;
import com.springboot.todoapi.group.dto.response.GroupInvitationSummaryResponse;
import com.springboot.todoapi.group.dto.response.GroupResponse;
import com.springboot.todoapi.group.dto.response.MyGroupSummaryResponse;
import com.springboot.todoapi.group.entity.GroupInvitation;
import com.springboot.todoapi.group.entity.GroupInvitationBlock;
import com.springboot.todoapi.group.entity.GroupInvitationStatus;
import com.springboot.todoapi.group.entity.GroupMember;
import com.springboot.todoapi.group.entity.GroupMemberRole;
import com.springboot.todoapi.group.entity.GroupMemberStatus;
import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.group.entity.TodoGroupStatus;
import com.springboot.todoapi.group.repository.GroupInvitationBlockRepository;
import com.springboot.todoapi.group.repository.GroupInvitationRepository;
import com.springboot.todoapi.group.repository.GroupMemberRepository;
import com.springboot.todoapi.group.repository.TodoGroupRepository;
import com.springboot.todoapi.user.entity.User;
import com.springboot.todoapi.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final TodoGroupRepository groupRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationBlockRepository groupInvitationBlockRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MyGroupSummaryResponse> getMyGroups(Long userId) {
        List<GroupMember> memberships = groupMemberRepository.findAllByUserIdAndStatus(userId, GroupMemberStatus.ACTIVE);
        return memberships.stream()
                .map(member -> {
                    TodoGroup group = groupRepository.findById(member.getGroupId())
                            .orElseThrow();
                    long activeMemberCount = groupMemberRepository.countByGroupIdAndStatus(
                            group.getId(), GroupMemberStatus.ACTIVE);
                    return MyGroupSummaryResponse.of(group, member, activeMemberCount);
                })
                .toList();
    }

    public GroupResponse createGroup(Long userId, String userEmail, GroupCreateRequest request) {
        String normalizedGroupName = normalizeGroupName(request.getGroupName());

        validateDuplicateEmails(request.getInviteEmails());
        validateSelfInvite(userEmail, request.getInviteEmails());

        String description = request.getDescription() != null ? request.getDescription().trim() : null;
        if (description != null && description.isBlank()) description = null;

        TodoGroup group = TodoGroup.create(normalizedGroupName, userId, description);
        groupRepository.save(group);

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

        // 수신자가 발신자를 차단한 경우 즉시 BLOCKED 처리 (발신자에겐 대기중으로 보임)
        blockInvitationsFromBlockedInvitees(userId, userEmail, invitations);

        groupInvitationRepository.saveAll(invitations);

        syncGroupStatus(group.getId());

        return GroupResponse.builder()
                .id(group.getId())
                .groupName(group.getGroupName())
                .creatorUserId(group.getCreatorUserId())
                .invitedEmails(
                        invitations.stream()
                                .map(GroupInvitation::getEmail)
                                .toList()
                )
                .build();
    }

    public List<GroupInvitationSummaryResponse> getMyPendingInvitations(Long userId, String userEmail) {
        String normalizedEmail = normalizeEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        List<GroupInvitation> invitations = groupInvitationRepository
                .findAllByEmailAndStatusOrderByCreatedAtDesc(normalizedEmail, GroupInvitationStatus.PENDING);

        for (GroupInvitation invitation : invitations) {
            if (invitation.isExpired(now)) {
                invitation.expire(now);
                syncGroupStatus(invitation.getGroupId());
            }
        }

        List<GroupInvitation> activeInvitations = invitations.stream()
                .filter(invitation -> invitation.getStatus() == GroupInvitationStatus.PENDING)
                .toList();

        Map<Long, TodoGroup> groupMap = groupRepository.findAllById(
                        activeInvitations.stream()
                                .map(GroupInvitation::getGroupId)
                                .distinct()
                                .toList()
                ).stream()
                .collect(Collectors.toMap(TodoGroup::getId, Function.identity()));

        // 초대자 이름 + 이메일 조회
        Set<Long> inviterIds = activeInvitations.stream()
                .map(GroupInvitation::getInvitedByUserId)
                .collect(Collectors.toSet());
        List<User> inviterUsers = userRepository.findAllById(inviterIds);
        Map<Long, String> inviterNameMap = inviterUsers.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        Map<Long, String> inviterEmailMap = inviterUsers.stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));

        return activeInvitations.stream()
                .map(invitation -> {
                    TodoGroup group = groupMap.get(invitation.getGroupId());
                    String groupName = group != null ? group.getGroupName() : "(삭제된 그룹)";
                    String inviterName = inviterNameMap.getOrDefault(invitation.getInvitedByUserId(), "알 수 없음");
                    String inviterEmail = inviterEmailMap.getOrDefault(invitation.getInvitedByUserId(), "");
                    String groupDescription = group != null ? group.getDescription() : null;
                    return GroupInvitationSummaryResponse.of(invitation, groupName, groupDescription, inviterName, inviterEmail);
                })
                .toList();
    }

    public void acceptInvitation(Long userId, String userEmail, Long invitationId) {
        String normalizedEmail = normalizeEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        GroupInvitation invitation = groupInvitationRepository.findByIdAndEmail(invitationId, normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 초대를 찾을 수 없습니다."));

        if (!invitation.isPending()) {
            throw new IllegalStateException("이미 처리된 초대입니다.");
        }

        if (invitation.isExpired(now)) {
            invitation.expire(now);
            syncGroupStatus(invitation.getGroupId());
            throw new IllegalStateException("만료된 초대입니다.");
        }

        TodoGroup group = groupRepository.findById(invitation.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getStatus() == TodoGroupStatus.DISBANDED) {
            throw new IllegalStateException("해산된 그룹의 초대는 수락할 수 없습니다.");
        }

        boolean alreadyActiveMember = groupMemberRepository.existsByGroupIdAndUserIdAndStatus(
                invitation.getGroupId(),
                userId,
                GroupMemberStatus.ACTIVE
        );

        if (alreadyActiveMember) {
            throw new IllegalStateException("이미 그룹 멤버입니다.");
        }

        GroupMember member = GroupMember.createMember(invitation.getGroupId(), userId);
        groupMemberRepository.save(member);

        invitation.accept(now);

        syncGroupStatus(invitation.getGroupId());
    }

    public void rejectInvitation(String userEmail, Long invitationId) {
        String normalizedEmail = normalizeEmail(userEmail);
        LocalDateTime now = LocalDateTime.now();

        GroupInvitation invitation = groupInvitationRepository.findByIdAndEmail(invitationId, normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("해당 초대를 찾을 수 없습니다."));

        if (!invitation.isPending()) {
            throw new IllegalStateException("이미 처리된 초대입니다.");
        }

        if (invitation.isExpired(now)) {
            invitation.expire(now);
            syncGroupStatus(invitation.getGroupId());
            throw new IllegalStateException("만료된 초대입니다.");
        }

        invitation.reject(now);

        syncGroupStatus(invitation.getGroupId());
    }

    private void syncGroupStatus(Long groupId) {
        TodoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getStatus() == TodoGroupStatus.DISBANDED) {
            return;
        }

        expireOverdueInvitations(groupId);

        long activeMemberCount = groupMemberRepository.countByGroupIdAndStatus(
                groupId,
                GroupMemberStatus.ACTIVE
        );

        // BLOCKED 상태도 대기중으로 간주: 발신자 입장에선 아직 응답 대기 중
        long pendingInvitationCount = groupInvitationRepository.countByGroupIdAndStatusIn(
                groupId,
                List.of(GroupInvitationStatus.PENDING, GroupInvitationStatus.BLOCKED)
        );

        if (activeMemberCount >= 2) {
            group.activate();
            return;
        }

        if (activeMemberCount >= 1 && pendingInvitationCount > 0) {
            group.activate();
            return;
        }

        group.inactivate();
    }

    private void expireOverdueInvitations(Long groupId) {
        LocalDateTime now = LocalDateTime.now();

        List<GroupInvitation> pendingInvitations = groupInvitationRepository.findAllByGroupIdAndStatus(
                groupId,
                GroupInvitationStatus.PENDING
        );

        for (GroupInvitation invitation : pendingInvitations) {
            if (invitation.isExpired(now)) {
                invitation.expire(now);
            }
        }
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
        if (groupName == null || groupName.trim().isBlank()) {
            throw new IllegalArgumentException("그룹명은 비어 있을 수 없습니다.");
        }

        return groupName.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    public void changeMyGroupAlias(Long userId, Long groupId, String aliasName) {
        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        String normalizedAlias = normalizeAlias(aliasName);

        if (normalizedAlias != null &&
                groupMemberRepository.existsByUserIdAndAliasNameAndStatusAndGroupIdNot(
                        userId, normalizedAlias, GroupMemberStatus.ACTIVE, groupId)) {
            throw new IllegalArgumentException("이미 사용 중인 개인 그룹닉네임입니다.");
        }

        member.changeAliasName(normalizedAlias);
    }

    private String normalizeAlias(String aliasName) {
        if (aliasName == null) {
            return null;
        }

        String value = aliasName.trim();
        return value.isBlank() ? null : value;
    }

    public void transferLeader(Long requesterUserId, Long groupId, Long targetUserId) {
        if (requesterUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("본인에게 다시 그룹장을 위임할 수 없습니다.");
        }

        GroupMember currentLeader = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, requesterUserId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!currentLeader.isLeader()) {
            throw new IllegalStateException("그룹장만 위임할 수 있습니다.");
        }

        GroupMember targetMember = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, targetUserId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("위임 대상은 해당 그룹의 활성 멤버여야 합니다."));

        if (targetMember.isLeader()) {
            throw new IllegalStateException("이미 그룹장인 멤버입니다.");
        }

        currentLeader.demoteToMember();
        targetMember.promoteToLeader();
    }

    public void inviteMembers(Long userId, String userEmail, Long groupId, GroupInviteRequest request) {
        TodoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getStatus() == TodoGroupStatus.DISBANDED) {
            throw new IllegalStateException("해산된 그룹에는 초대할 수 없습니다.");
        }

        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!requester.isLeader()) {
            throw new IllegalStateException("그룹장만 멤버를 초대할 수 있습니다.");
        }

        validateDuplicateEmails(request.getInviteEmails());
        validateSelfInvite(userEmail, request.getInviteEmails());
        validatePendingInvitations(groupId, request.getInviteEmails());

        List<GroupInvitation> invitations = request.getInviteEmails().stream()
                .map(this::normalizeEmail)
                .map(email -> GroupInvitation.create(
                        groupId,
                        email,
                        userId,
                        LocalDateTime.now().plusDays(7)
                ))
                .toList();

        // 수신자가 발신자를 차단한 경우 즉시 BLOCKED 처리 (발신자에겐 대기중으로 보임)
        blockInvitationsFromBlockedInvitees(userId, userEmail, invitations);

        groupInvitationRepository.saveAll(invitations);

        syncGroupStatus(groupId);
    }

    private void validatePendingInvitations(Long groupId, List<String> inviteEmails) {
        List<String> normalizedEmails = inviteEmails.stream()
                .map(this::normalizeEmail)
                .toList();

        // BLOCKED도 발신자 입장에서 대기중이므로 중복 초대 방지
        List<GroupInvitation> pendingInvitations = groupInvitationRepository.findByGroupIdAndEmailInAndStatusIn(
                groupId,
                normalizedEmails,
                List.of(GroupInvitationStatus.PENDING, GroupInvitationStatus.BLOCKED)
        );

        if (!pendingInvitations.isEmpty()) {
            String pendingEmails = pendingInvitations.stream()
                    .map(GroupInvitation::getEmail)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(", "));

            throw new IllegalArgumentException("이미 초대 대기 중인 이메일이 있습니다: " + pendingEmails);
        }
    }

    public void leaveGroup(Long userId, Long groupId) {
        GroupMember member = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (member.isLeader()) {
            throw new IllegalStateException("그룹장은 그룹장 위임 후 나갈 수 있습니다.");
        }

        member.leave();

        syncGroupStatus(groupId);
    }

    public GroupDetailResponse getGroupDetail(Long userId, Long groupId) {
        TodoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        groupMemberRepository.findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        syncGroupStatus(groupId);

        GroupMember leader = groupMemberRepository.findByGroupIdAndRoleAndStatus(
                groupId,
                GroupMemberRole.LEADER,
                GroupMemberStatus.ACTIVE
        ).orElse(null);

        long activeCount = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.ACTIVE);
        long leftCount = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.LEFT);
        long kickedCount = groupMemberRepository.countByGroupIdAndStatus(groupId, GroupMemberStatus.KICKED);

        long pendingCount = groupInvitationRepository.countByGroupIdAndStatus(groupId, GroupInvitationStatus.PENDING);
        long acceptedCount = groupInvitationRepository.countByGroupIdAndStatus(groupId, GroupInvitationStatus.ACCEPTED);
        long rejectedCount = groupInvitationRepository.countByGroupIdAndStatus(groupId, GroupInvitationStatus.REJECTED);
        long expiredCount = groupInvitationRepository.countByGroupIdAndStatus(groupId, GroupInvitationStatus.EXPIRED);

        List<GroupMember> activeMembers = groupMemberRepository
                .findAllByGroupIdAndStatusOrderByJoinedAtAsc(groupId, GroupMemberStatus.ACTIVE);

        Set<Long> memberUserIds = activeMembers.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toSet());
        List<User> memberUsers = userRepository.findAllById(memberUserIds);
        Map<Long, String> memberUserNameMap = memberUsers.stream()
                .collect(Collectors.toMap(User::getId, User::getName));
        Map<Long, String> memberUserEmailMap = memberUsers.stream()
                .collect(Collectors.toMap(User::getId, User::getEmail));
        Map<Long, String> memberProfileImageMap = memberUsers.stream()
                .filter(u -> u.getProfileImageUrl() != null)
                .collect(Collectors.toMap(User::getId, User::getProfileImageUrl));

        List<GroupDetailResponse.MemberInfo> members = activeMembers.stream()
                .map(member -> GroupDetailResponse.MemberInfo.builder()
                        .userId(member.getUserId())
                        .userName(memberUserNameMap.getOrDefault(member.getUserId(), "알 수 없음"))
                        .userEmail(memberUserEmailMap.getOrDefault(member.getUserId(), ""))
                        .role(member.getRole().name())
                        .aliasName(member.getAliasName())
                        .profileImageUrl(memberProfileImageMap.get(member.getUserId()))
                        .joinedAt(member.getJoinedAt())
                        .build())
                .toList();

        // BLOCKED 상태도 포함: 수신자가 차단했더라도 발신자(A)에겐 여전히 대기중으로 보여야 함
        List<GroupDetailResponse.PendingInvitationInfo> pendingInvitations = groupInvitationRepository
                .findAllByGroupIdAndStatusInOrderByCreatedAtAsc(groupId,
                        List.of(GroupInvitationStatus.PENDING, GroupInvitationStatus.BLOCKED))
                .stream()
                .map(invitation -> {
                    // 가입된 계정이 있는 경우에만 목록에 포함
                    var invitedUser = userRepository.findByEmail(invitation.getEmail()).orElse(null);
                    if (invitedUser == null) return null;
                    return GroupDetailResponse.PendingInvitationInfo.builder()
                            .invitationId(invitation.getId())
                            .email(invitation.getEmail())
                            .userId(invitedUser.getId())
                            .userName(invitedUser.getName())
                            .profileImageUrl(invitedUser.getProfileImageUrl())
                            .invitedByUserId(invitation.getInvitedByUserId())
                            .expiresAt(invitation.getExpiresAt())
                            .createdAt(invitation.getCreatedAt())
                            .build();
                })
                .filter(info -> info != null)
                .toList();

        return GroupDetailResponse.builder()
                .groupId(group.getId())
                .groupName(group.getGroupName())
                .description(group.getDescription())
                .groupStatus(group.getStatus().name())
                .creatorUserId(group.getCreatorUserId())
                .createdAt(group.getCreatedAt())
                .leaderUserId(leader != null ? leader.getUserId() : null)
                .memberSummary(GroupDetailResponse.MemberSummary.builder()
                        .activeCount(activeCount)
                        .leftCount(leftCount)
                        .kickedCount(kickedCount)
                        .build())
                .invitationSummary(GroupDetailResponse.InvitationSummary.builder()
                        .pendingCount(pendingCount)
                        .acceptedCount(acceptedCount)
                        .rejectedCount(rejectedCount)
                        .expiredCount(expiredCount)
                        .build())
                .members(members)
                .pendingInvitations(pendingInvitations)
                .build();
    }

    public void cancelInvitation(Long userId, Long groupId, Long invitationId) {
        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!requester.isLeader()) {
            throw new IllegalStateException("그룹장만 초대를 취소할 수 있습니다.");
        }

        GroupInvitation invitation = groupInvitationRepository.findByIdAndGroupId(invitationId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("해당 초대를 찾을 수 없습니다."));

        if (!invitation.isCancellable()) {
            throw new IllegalStateException("대기 중인 초대만 취소할 수 있습니다.");
        }

        groupInvitationRepository.delete(invitation);
        syncGroupStatus(groupId);
    }

    public void kickMember(Long userId, Long groupId, Long targetUserId) {
        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!requester.isLeader()) {
            throw new IllegalStateException("그룹장만 그룹원을 강퇴할 수 있습니다.");
        }

        if (userId.equals(targetUserId)) {
            throw new IllegalArgumentException("그룹장은 본인을 강퇴할 수 없습니다.");
        }

        GroupMember targetMember = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, targetUserId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("강퇴 대상은 해당 그룹의 활성 멤버여야 합니다."));

        if (targetMember.isLeader()) {
            throw new IllegalStateException("그룹장은 강퇴할 수 없습니다.");
        }

        targetMember.kick();

        syncGroupStatus(groupId);
    }

    public void changeGroupName(Long userId, Long groupId, String groupName) {
        TodoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getStatus() == TodoGroupStatus.DISBANDED) {
            throw new IllegalStateException("해산된 그룹의 이름은 변경할 수 없습니다.");
        }

        if (group.getStatus() == TodoGroupStatus.INACTIVE) {
            throw new IllegalStateException("비활성화된 그룹의 이름은 변경할 수 없습니다.");
        }

        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!requester.isLeader()) {
            throw new IllegalStateException("그룹장만 그룹명을 변경할 수 있습니다.");
        }

        String normalizedGroupName = normalizeGroupName(groupName);
        group.changeGroupName(normalizedGroupName);
    }

    public void changeGroupDescription(Long userId, Long groupId, String description) {
        TodoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        if (group.getStatus() == TodoGroupStatus.DISBANDED) {
            throw new IllegalStateException("해산된 그룹의 소개는 변경할 수 없습니다.");
        }

        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!requester.isLeader()) {
            throw new IllegalStateException("그룹장만 그룹 소개를 변경할 수 있습니다.");
        }

        String trimmed = (description == null) ? null : description.trim();
        group.changeDescription((trimmed == null || trimmed.isEmpty()) ? null : trimmed);
    }

    public void disbandGroup(Long userId, Long groupId) {
        TodoGroup group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        GroupMember requester = groupMemberRepository
                .findByGroupIdAndUserIdAndStatus(groupId, userId, GroupMemberStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("해당 그룹의 활성 멤버가 아닙니다."));

        if (!requester.isLeader()) {
            throw new IllegalStateException("그룹장만 그룹을 해산할 수 있습니다.");
        }

        if (group.getStatus() != TodoGroupStatus.ACTIVE) {
            throw new IllegalStateException("활성화된 그룹만 해산할 수 있습니다.");
        }

        group.disband();
        expirePendingInvitationsOnDisband(groupId);
    }

    private void expirePendingInvitationsOnDisband(Long groupId) {
        LocalDateTime now = LocalDateTime.now();

        List<GroupInvitation> pendingInvitations = groupInvitationRepository
                .findAllByGroupIdAndStatus(groupId, GroupInvitationStatus.PENDING);

        for (GroupInvitation invitation : pendingInvitations) {
            invitation.expire(now);
        }
    }

    public void blockInviter(Long blockerUserId, String blockerEmail, String blockedEmail) {
        String normalizedBlockerEmail = normalizeEmail(blockerEmail);
        String normalizedBlockedEmail = normalizeEmail(blockedEmail);

        if (normalizedBlockedEmail.equals(normalizedBlockerEmail)) {
            throw new IllegalArgumentException("본인은 차단할 수 없습니다.");
        }


        var blockedUser = userRepository.findByEmail(normalizedBlockedEmail)
                .orElseThrow(() -> new IllegalArgumentException("가입된 사용자만 차단할 수 있습니다."));

        boolean alreadyBlockedByUserId = groupInvitationBlockRepository
                .existsByBlockerUserIdAndBlockedUserId(blockerUserId, blockedUser.getId());

        boolean alreadyBlockedByEmail = groupInvitationBlockRepository
                .existsByBlockerUserIdAndBlockedEmail(blockerUserId, normalizedBlockedEmail);

        if (alreadyBlockedByUserId || alreadyBlockedByEmail) {
            throw new IllegalStateException("이미 차단한 사용자입니다.");
        }

        groupInvitationBlockRepository.save(
                GroupInvitationBlock.create(
                        blockerUserId,
                        blockedUser.getId(),
                        normalizedBlockedEmail
                )
        );

        blockPendingInvitationsFromBlockedUser(normalizedBlockerEmail, blockedUser.getId());
    }

    @Transactional(readOnly = true)
    public List<GroupInvitationBlockResponse> getMyInvitationBlocks(Long userId) {
        return groupInvitationBlockRepository.findAllByBlockerUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(GroupInvitationBlockResponse::from)
                .toList();
    }

    public void unblockInviter(Long blockerUserId, Long blockId) {
        GroupInvitationBlock block = groupInvitationBlockRepository.findByIdAndBlockerUserId(blockId, blockerUserId)
                .orElseThrow(() -> new IllegalArgumentException("차단 내역을 찾을 수 없습니다."));

        User blocker = userRepository.findById(blockerUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        String blockerEmail = normalizeEmail(blocker.getEmail());
        Long blockedUserId = block.getBlockedUserId();

        groupInvitationBlockRepository.delete(block);

        // 차단으로 숨겨진 초대를 대기중으로 복귀
        LocalDateTime now = LocalDateTime.now();
        List<GroupInvitation> blockedInvitations = groupInvitationRepository
                .findAllByEmailAndInvitedByUserIdAndStatus(blockerEmail, blockedUserId, GroupInvitationStatus.BLOCKED);

        Set<Long> affectedGroupIds = new LinkedHashSet<>();
        for (GroupInvitation invitation : blockedInvitations) {
            if (!invitation.isExpired(now)) {
                invitation.restore();
                affectedGroupIds.add(invitation.getGroupId());
            } else {
                invitation.expire(now);
            }
        }

        for (Long groupId : affectedGroupIds) {
            syncGroupStatus(groupId);
        }
    }

    private void blockPendingInvitationsFromBlockedUser(String blockerEmail, Long blockedUserId) {
        LocalDateTime now = LocalDateTime.now();

        List<GroupInvitation> invitations = groupInvitationRepository
                .findAllByEmailAndStatusOrderByCreatedAtDesc(blockerEmail, GroupInvitationStatus.PENDING);

        Set<Long> affectedGroupIds = new LinkedHashSet<>();

        for (GroupInvitation invitation : invitations) {
            if (invitation.getInvitedByUserId().equals(blockedUserId)) {
                invitation.block(now);
                affectedGroupIds.add(invitation.getGroupId());
            }
        }

        for (Long groupId : affectedGroupIds) {
            syncGroupStatus(groupId);
        }
    }

    /**
     * 초대 대상이 발신자를 차단한 경우 해당 초대를 즉시 BLOCKED 처리한다.
     * 발신자에겐 아무 오류 없이 대기중으로 보이고, 수신자는 초대 목록에 노출되지 않는다.
     */
    private void blockInvitationsFromBlockedInvitees(
            Long inviterUserId, String inviterEmail, List<GroupInvitation> invitations) {
        String normalizedInviterEmail = normalizeEmail(inviterEmail);
        LocalDateTime now = LocalDateTime.now();

        for (GroupInvitation invitation : invitations) {
            var invitee = userRepository.findByEmail(invitation.getEmail()).orElse(null);
            if (invitee == null) continue;

            boolean blockedByUserId = groupInvitationBlockRepository
                    .existsByBlockerUserIdAndBlockedUserId(invitee.getId(), inviterUserId);
            boolean blockedByEmail = groupInvitationBlockRepository
                    .existsByBlockerUserIdAndBlockedEmail(invitee.getId(), normalizedInviterEmail);

            if (blockedByUserId || blockedByEmail) {
                invitation.block(now);
            }
        }
    }
}