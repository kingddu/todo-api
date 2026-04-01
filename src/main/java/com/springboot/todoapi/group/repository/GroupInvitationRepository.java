package com.springboot.todoapi.group.repository;

import com.springboot.todoapi.group.entity.GroupInvitation;
import com.springboot.todoapi.group.entity.GroupInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    List<GroupInvitation> findByGroupIdAndEmailInAndStatus(
            Long groupId,
            Collection<String> emails,
            GroupInvitationStatus status
    );

    Optional<GroupInvitation> findByIdAndEmail(Long id, String email);

    List<GroupInvitation> findAllByEmailAndStatusOrderByCreatedAtDesc(
            String email,
            GroupInvitationStatus status
    );

    List<GroupInvitation> findAllByGroupIdAndStatus(Long groupId, GroupInvitationStatus status);

    List<GroupInvitation> findAllByGroupIdAndStatusOrderByCreatedAtAsc(
            Long groupId,
            GroupInvitationStatus status
    );

    long countByGroupIdAndStatus(Long groupId, GroupInvitationStatus status);
}