package com.tasknest.controller;

import com.tasknest.dto.TaskListRequestDto;
import com.tasknest.dto.TaskListResponseDto;
import com.tasknest.service.TaskListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task List Management", description = "Endpoints for managing task lists")
public class TaskListController {

    private final TaskListService taskListService;

    @PostMapping("/api/users/{userId}/lists")
    @Operation(summary = "Create a task list for a user")
    public ResponseEntity<TaskListResponseDto> createTaskList(
            @PathVariable Long userId,
            @Valid @RequestBody TaskListRequestDto request) {
        TaskListResponseDto createdList = taskListService.createTaskList(userId, request);
        return new ResponseEntity<>(createdList, HttpStatus.CREATED);
    }

    @GetMapping("/api/users/{userId}/lists")
    @Operation(summary = "Get all task lists of a user")
    public ResponseEntity<List<TaskListResponseDto>> getTaskListsByUserId(@PathVariable Long userId) {
        List<TaskListResponseDto> lists = taskListService.getTaskListsByUserId(userId);
        return ResponseEntity.ok(lists);
    }

    @GetMapping("/api/lists/{listId}")
    @Operation(summary = "Get a specific task list by ID")
    public ResponseEntity<TaskListResponseDto> getTaskListById(@PathVariable Long listId) {
        TaskListResponseDto list = taskListService.getTaskListById(listId);
        return ResponseEntity.ok(list);
    }

    @PutMapping("/api/lists/{listId}")
    @Operation(summary = "Update a task list")
    public ResponseEntity<TaskListResponseDto> updateTaskList(
            @PathVariable Long listId,
            @Valid @RequestBody TaskListRequestDto request) {
        TaskListResponseDto updatedList = taskListService.updateTaskList(listId, request);
        return ResponseEntity.ok(updatedList);
    }

    @DeleteMapping("/api/lists/{listId}")
    @Operation(summary = "Delete a task list")
    public ResponseEntity<Void> deleteTaskList(@PathVariable Long listId) {
        taskListService.deleteTaskList(listId);
        return ResponseEntity.noContent().build();
    }
}
