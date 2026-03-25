package com.springboot.todoapi.todo.controller;

import tools.jackson.databind.JsonNode;
import com.springboot.todoapi.todo.dto.request.TodoCreateRequest;
import com.springboot.todoapi.todo.dto.request.TodoPatchRequest;
import com.springboot.todoapi.todo.dto.response.TodoResponse;
import com.springboot.todoapi.todo.service.TodoService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;


//@SecurityRequirement(name = "bearerAuth")     //테스트 단계서 제외
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    // 🔐 Todolist 생성
    /*
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @Valid @RequestBody TodoCreateRequest request
    ) {
        TodoResponse created = todoService.create(userId, request);
        return ResponseEntity.ok(created);
    }
*/
    // CREATE
    @PostMapping
    public ResponseEntity<TodoResponse> create(@Valid @RequestBody TodoCreateRequest request) {
        Long userId = 1L;   //테스트용
        return ResponseEntity.ok(todoService.create(userId, request));
    }

    // 기간을 던져서 저장한 DATA를 가져온다
    @GetMapping("/range")
    public ResponseEntity<List<TodoResponse>> getByRange(
            //@AuthenticationPrincipal Long userId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to

    ) {
        Long userId = 1L; //임시
        return ResponseEntity.ok(todoService.getTodosByRange(userId, from, to));
    }

    // 입력한 DATA 수정
    @Operation(summary = "Todo 부분 수정")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TodoPatchRequest.class)
            )
    )
    @PatchMapping("/{todoId}")
    public ResponseEntity<TodoResponse> patchTodo(
            @PathVariable Long todoId,
            @RequestBody JsonNode request
    ) {
        Long userId = 1L;
        return ResponseEntity.ok(todoService.patchTodo(userId, todoId, request));
    }


    /*
    // 🔐 기간으로 조회 (구글 캘린더처럼 하루, 3일, 일주일, 한달)
    // 프론트에서 기간을 던지면 위의 기간들의 일정들을 보여줌
    @GetMapping
    public List<TodoResponse> getSchedulesInRange(
            @AuthenticationPrincipal Long userId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end

    ) {
        return todoService.getSchedulesInRange(userId, start, end);
    }
*/
/*
    // 🔐 하루 일정 조회
    @GetMapping("/day/{date}")
    public List<TodoResponse> getDaySchedule(
            @PathVariable LocalDate date,
            @AuthenticationPrincipal Long userId
    ) {
        return todoService.getDaySchedule(userId, date);
    }
*/
    // 🔐 단건 조회
    /*
    @GetMapping("/{scheduleId}")
    public TodoResponse getTimeScheduleById(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long scheduleId
    ) {
        return todoService.getScheduleById(userId, scheduleId);
    }
*/


    // 🔐 일정 삭제
    /*
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        todoService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }
*/
    // 🔐 일정 수정
    /*
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> updateSchedule(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TodoCreateRequest request
    ) {
        TodoResponse updated = todoService.update(userId, id, request);
        return ResponseEntity.ok(updated);
    }

     */
}