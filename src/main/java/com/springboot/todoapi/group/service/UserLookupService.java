package com.springboot.todoapi.group.service;

import java.util.Optional;

public interface UserLookupService {

    Optional<UserInfo> findByEmail(String email);

    record UserInfo(Long userId, String email) {
    }
}
