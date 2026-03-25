package com.springboot.todoapi.todo.entity;

public enum TodoType {
    DATE_ONLY,   // 특정 날짜 (그날만)
    RANGE,       // 기간 (startDate ~ endDate)
    DEADLINE     // 마감일 + 이월 가능
}
