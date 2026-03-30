package com.springboot.todoapi.group.service;

import com.springboot.todoapi.group.dto.request.GroupCreateRequest;
import com.springboot.todoapi.group.dto.response.GroupResponse;
import com.springboot.todoapi.group.entity.TodoGroup;
import com.springboot.todoapi.group.repository.TodoGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final TodoGroupRepository groupRepository;

    public GroupResponse createGroup(Long userId, GroupCreateRequest request) {

        TodoGroup group = TodoGroup.create(request.getName(), userId);
        groupRepository.save(group);

        return GroupResponse.from(group);
    }
}