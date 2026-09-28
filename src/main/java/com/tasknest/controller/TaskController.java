package com.tasknest.controller;

import com.tasknest.dto.TaskRequestDto;
import com.tasknest.dto.TaskResponseDto;
import com.tasknest.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Task Management", description = "Endpoints for creating, managing, filtering, and moving tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/lists/{listId}/tasks")
    @Operation(summary = "Create a task inside a task list")
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable Long listId,
            @Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto createdTask = taskService.createTask(listId, request);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }

    @GetMapping("/api/lists/{listId}/tasks")
    @Operation(summary = "Get all tasks in a task list")
    public ResponseEntity<List<TaskResponseDto>> getTasksByListId(@PathVariable Long listId) {
        List<TaskResponseDto> tasks = taskService.getTasksByListId(listId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/api/tasks/{taskId}")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponseDto> getTaskById(@PathVariable Long taskId) {
        TaskResponseDto task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }

    @PutMapping("/api/tasks/{taskId}")
    @Operation(summary = "Update an existing task")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskRequestDto request) {
        TaskResponseDto updatedTask = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/api/tasks/{taskId}")
    @Operation(summary = "Delete a task by ID")
    public ResponseEntity<Void> deleteTask(@PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/tasks/{taskId}/complete")
    @Operation(summary = "Mark task as completed")
    public ResponseEntity<TaskResponseDto> markTaskCompleted(@PathVariable Long taskId) {
        TaskResponseDto task = taskService.markTaskCompleted(taskId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/api/tasks/{taskId}/incomplete")
    @Operation(summary = "Mark task as incomplete")
    public ResponseEntity<TaskResponseDto> markTaskIncomplete(@PathVariable Long taskId) {
        TaskResponseDto task = taskService.markTaskIncomplete(taskId);
        return ResponseEntity.ok(task);
    }

    @PatchMapping("/api/tasks/{taskId}/move/{targetListId}")
    @Operation(summary = "Move a task to another list belonging to the same user")
    public ResponseEntity<TaskResponseDto> moveTask(
            @PathVariable Long taskId,
            @PathVariable Long targetListId) {
        TaskResponseDto movedTask = taskService.moveTask(taskId, targetListId);
        return ResponseEntity.ok(movedTask);
    }

    @GetMapping("/api/users/{userId}/tasks/today")
    @Operation(summary = "Get today's tasks for a user")
    public ResponseEntity<List<TaskResponseDto>> getTodayTasks(@PathVariable Long userId) {
        List<TaskResponseDto> tasks = taskService.getTodayTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/api/users/{userId}/tasks/overdue")
    @Operation(summary = "Get overdue tasks for a user")
    public ResponseEntity<List<TaskResponseDto>> getOverdueTasks(@PathVariable Long userId) {
        List<TaskResponseDto> tasks = taskService.getOverdueTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/api/users/{userId}/tasks")
    @Operation(summary = "Filter tasks by priority, completed status, or due date")
    public ResponseEntity<List<TaskResponseDto>> filterTasks(
            @PathVariable Long userId,
            @Parameter(description = "Priority filter (LOW, MEDIUM, HIGH)")
            @RequestParam(required = false) String priority,
            @Parameter(description = "Completed status filter (true or false)")
            @RequestParam(required = false) Boolean completed,
            @Parameter(description = "Due date filter (YYYY-MM-DD)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate) {
        List<TaskResponseDto> tasks = taskService.filterTasks(userId, priority, completed, dueDate);
        return ResponseEntity.ok(tasks);
    }
}
