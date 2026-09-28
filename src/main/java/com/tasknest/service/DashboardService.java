package com.tasknest.service;

import com.tasknest.dto.DashboardResponseDto;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final UserService userService;
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    public DashboardResponseDto getDashboard(Long userId) {
        // Ensure user exists
        userService.getUserEntity(userId);

        LocalDate today = LocalDate.now();

        long totalLists = taskListRepository.countByUserId(userId);
        long totalTasks = taskRepository.countByTaskListUserId(userId);
        long completedTasks = taskRepository.countByTaskListUserIdAndCompletedTrue(userId);
        long incompleteTasks = taskRepository.countByTaskListUserIdAndCompletedFalse(userId);
        long overdueTasks = taskRepository.countOverdueTasks(userId, today);
        long tasksDueToday = taskRepository.countTodayTasks(userId, today);

        return DashboardResponseDto.builder()
                .totalTaskLists(totalLists)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .incompleteTasks(incompleteTasks)
                .overdueTasks(overdueTasks)
                .tasksDueToday(tasksDueToday)
                .build();
    }
}
