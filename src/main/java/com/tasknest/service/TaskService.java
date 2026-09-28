package com.tasknest.service;

import com.tasknest.dto.TaskRequestDto;
import com.tasknest.dto.TaskResponseDto;
import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.exception.InvalidPriorityException;
import com.tasknest.exception.InvalidTaskMoveException;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskListService taskListService;
    private final UserService userService;

    public TaskResponseDto createTask(Long listId, TaskRequestDto request) {
        TaskList taskList = taskListService.getTaskListEntity(listId);

        boolean isCompleted = request.getCompleted() != null && request.getCompleted();
        LocalDateTime completedAt = isCompleted ? LocalDateTime.now() : null;

        Task task = Task.builder()
                .title(request.getTitle().trim())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .completed(isCompleted)
                .completedAt(completedAt)
                .taskList(taskList)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponseDto(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByListId(Long listId) {
        // Ensure task list exists
        taskListService.getTaskListEntity(listId);

        return taskRepository.findByTaskListId(listId).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long taskId) {
        Task task = getTaskEntity(taskId);
        return mapToResponseDto(task);
    }

    public TaskResponseDto updateTask(Long taskId, TaskRequestDto request) {
        Task task = getTaskEntity(taskId);

        task.setTitle(request.getTitle().trim());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());

        if (request.getCompleted() != null) {
            if (request.getCompleted()) {
                task.setCompleted(true);
                if (task.getCompletedAt() == null) {
                    task.setCompletedAt(LocalDateTime.now());
                }
            } else {
                task.setCompleted(false);
                task.setCompletedAt(null);
            }
        }

        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    public void deleteTask(Long taskId) {
        Task task = getTaskEntity(taskId);
        taskRepository.delete(task);
    }

    public TaskResponseDto markTaskCompleted(Long taskId) {
        Task task = getTaskEntity(taskId);
        task.setCompleted(true);
        task.setCompletedAt(LocalDateTime.now());

        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    public TaskResponseDto markTaskIncomplete(Long taskId) {
        Task task = getTaskEntity(taskId);
        task.setCompleted(false);
        task.setCompletedAt(null);

        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    public TaskResponseDto moveTask(Long taskId, Long targetListId) {
        Task task = getTaskEntity(taskId);
        TaskList targetList = taskListService.getTaskListEntity(targetListId);

        Long currentUserId = task.getTaskList().getUser().getId();
        Long targetUserId = targetList.getUser().getId();

        if (!currentUserId.equals(targetUserId)) {
            throw new InvalidTaskMoveException("A task can only be moved between lists belonging to the same user");
        }

        task.setTaskList(targetList);
        Task updatedTask = taskRepository.save(task);
        return mapToResponseDto(updatedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTodayTasks(Long userId) {
        userService.getUserEntity(userId);
        LocalDate today = LocalDate.now();
        return taskRepository.findTodayTasksByUser(userId, today).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getOverdueTasks(Long userId) {
        userService.getUserEntity(userId);
        LocalDate today = LocalDate.now();
        return taskRepository.findOverdueTasksByUser(userId, today).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> filterTasks(Long userId, String priorityStr, Boolean completed, LocalDate dueDate) {
        userService.getUserEntity(userId);

        Priority priority = null;
        if (priorityStr != null && !priorityStr.trim().isEmpty()) {
            priority = Priority.fromString(priorityStr);
        }

        return taskRepository.filterTasks(userId, priority, completed, dueDate).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Task getTaskEntity(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));
    }

    public TaskResponseDto mapToResponseDto(Task task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .completed(task.isCompleted())
                .createdAt(task.getCreatedAt())
                .completedAt(task.getCompletedAt())
                .taskListId(task.getTaskList() != null ? task.getTaskList().getId() : null)
                .taskListName(task.getTaskList() != null ? task.getTaskList().getName() : null)
                .overdue(task.isOverdue())
                .build();
    }
}
