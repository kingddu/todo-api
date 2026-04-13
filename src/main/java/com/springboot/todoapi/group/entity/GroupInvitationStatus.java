package com.springboot.todoapi.group.entity;

public enum GroupInvitationStatus {
    PENDING,        // 진행중인
    ACCEPTED,
    REJECTED,
    EXPIRED,
    BLOCKED         // 수신자가 발신자를 차단하여 숨겨진 상태
}
