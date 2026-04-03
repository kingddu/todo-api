package com.springboot.todoapi.todo.controller;

import tools.jackson.databind.JsonNode;
import com.springboot.todoapi.auth.security.CustomUserPrincipal;
import com.springboot.todoapi.todo.dto.request.TodoCreateRequest;
import com.springboot.todoapi.todo.dto.request.TodoPatchRequest;
import com.springboot.todoapi.todo.dto.response.TodoResponse;
import com.springboot.todoapi.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<TodoResponse> create(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TodoCreateRequest request
    ) {
        return ResponseEntity.ok(todoService.create(principal.getId(), request));
    }

    @GetMapping("/range")
    public ResponseEntity<List<TodoResponse>> getByRange(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to
    ) {
        return ResponseEntity.ok(todoService.getTodosByRange(principal.getId(), from, to));
    }

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
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long todoId,
            @RequestBody JsonNode request
    ) {
        return ResponseEntity.ok(todoService.patchTodo(principal.getId(), todoId, request));
    }

    @Operation(summary = "Todo 삭제")
    @DeleteMapping("/{todoId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long todoId
    ) {
        todoService.delete(principal.getId(), todoId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Todo 완료 처리")
    @PatchMapping("/{todoId}/complete")
    public ResponseEntity<TodoResponse> complete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long todoId
    ) {
        return ResponseEntity.ok(todoService.complete(principal.getId(), todoId));
    }

    @Operation(summary = "Todo 완료 취소")
    @PatchMapping("/{todoId}/uncomplete")
    public ResponseEntity<TodoResponse> uncomplete(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @PathVariable Long todoId
    ) {
        return ResponseEntity.ok(todoService.uncomplete(principal.getId(), todoId));
    }
}
