package com.tasknest.service;

import com.tasknest.dto.TaskListRequestDto;
import com.tasknest.dto.TaskListResponseDto;
import com.tasknest.entity.TaskList;
import com.tasknest.entity.User;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.repository.TaskListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskListService {

    private final TaskListRepository taskListRepository;
    private final UserService userService;

    public TaskListResponseDto createTaskList(Long userId, TaskListRequestDto request) {
        User user = userService.getUserEntity(userId);

        TaskList taskList = TaskList.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .user(user)
                .build();

        TaskList savedList = taskListRepository.save(taskList);
        return mapToResponseDto(savedList);
    }

    @Transactional(readOnly = true)
    public List<TaskListResponseDto> getTaskListsByUserId(Long userId) {
        // Ensure user exists
        userService.getUserEntity(userId);

        return taskListRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskListResponseDto getTaskListById(Long listId) {
        TaskList taskList = getTaskListEntity(listId);
        return mapToResponseDto(taskList);
    }

    public TaskListResponseDto updateTaskList(Long listId, TaskListRequestDto request) {
        TaskList taskList = getTaskListEntity(listId);

        taskList.setName(request.getName().trim());
        taskList.setDescription(request.getDescription());

        TaskList updatedList = taskListRepository.save(taskList);
        return mapToResponseDto(updatedList);
    }

    public void deleteTaskList(Long listId) {
        TaskList taskList = getTaskListEntity(listId);
        taskListRepository.delete(taskList);
    }

    @Transactional(readOnly = true)
    public TaskList getTaskListEntity(Long listId) {
        return taskListRepository.findById(listId)
                .orElseThrow(() -> new ResourceNotFoundException("Task list not found with id: " + listId));
    }

    public TaskListResponseDto mapToResponseDto(TaskList taskList) {
        return TaskListResponseDto.builder()
                .id(taskList.getId())
                .name(taskList.getName())
                .description(taskList.getDescription())
                .createdAt(taskList.getCreatedAt())
                .userId(taskList.getUser().getId())
                .userName(taskList.getUser().getName())
                .taskCount(taskList.getTasks() != null ? taskList.getTasks().size() : 0)
                .build();
    }
}
