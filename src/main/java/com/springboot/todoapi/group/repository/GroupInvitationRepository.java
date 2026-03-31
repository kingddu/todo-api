package com.springboot.todoapi.group.repository;

import com.springboot.todoapi.group.entity.GroupInvitation;
import com.springboot.todoapi.group.entity.GroupInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {

    List<GroupInvitation> findByGroupIdAndEmailInAndStatus(
            Long groupId,
            Collection<String> emails,
            GroupInvitationStatus status
    );
}