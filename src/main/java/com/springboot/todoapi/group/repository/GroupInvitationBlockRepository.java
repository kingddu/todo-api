package com.springboot.todoapi.group.repository;

import com.springboot.todoapi.group.entity.GroupInvitationBlock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GroupInvitationBlockRepository extends JpaRepository<GroupInvitationBlock, Long> {

    boolean existsByBlockerUserIdAndBlockedUserId(Long blockerUserId, Long blockedUserId);

    boolean existsByBlockerUserIdAndBlockedEmail(Long blockerUserId, String blockedEmail);

    Optional<GroupInvitationBlock> findByIdAndBlockerUserId(Long id, Long blockerUserId);

    List<GroupInvitationBlock> findAllByBlockerUserIdOrderByCreatedAtDesc(Long blockerUserId);
}