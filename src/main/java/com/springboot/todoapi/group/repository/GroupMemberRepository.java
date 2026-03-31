package com.springboot.todoapi.group.repository;

import com.springboot.todoapi.group.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
}
