package com.springboot.todoapi.group.repository;

import com.springboot.todoapi.group.entity.GroupMember;
import com.springboot.todoapi.group.entity.GroupMemberRole;
import com.springboot.todoapi.group.entity.GroupMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupIdAndUserIdAndStatus(
            Long groupId,
            Long userId,
            GroupMemberStatus status
    );

    long countByGroupIdAndStatus(Long groupId, GroupMemberStatus status);

    Optional<GroupMember> findByGroupIdAndUserIdAndStatus(
            Long groupId,
            Long userId,
            GroupMemberStatus status
    );

    boolean existsByUserIdAndAliasNameAndStatus(
            Long userId,
            String aliasName,
            GroupMemberStatus status
    );

    boolean existsByUserIdAndAliasNameAndStatusAndGroupIdNot(
            Long userId,
            String aliasName,
            GroupMemberStatus status,
            Long groupId
    );

    Optional<GroupMember> findByGroupIdAndRoleAndStatus(
            Long groupId,
            GroupMemberRole role,
            GroupMemberStatus status
    );

    List<GroupMember> findAllByGroupIdAndStatusOrderByJoinedAtAsc(
            Long groupId,
            GroupMemberStatus status
    );
}