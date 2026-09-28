package com.tasknest;

import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.entity.User;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import com.tasknest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@ConditionalOnProperty(name = "app.load-sample-data", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data, skipping initialization.");
            return;
        }

        log.info("Initializing sample data for TaskNest...");

        // 1. Create Sample Users
        User alex = User.builder()
                .name("Alex Mercer")
                .email("alex.mercer@tasknest.edu")
                .password("secretPass123")
                .createdAt(LocalDateTime.now().minusDays(10))
                .build();

        User maya = User.builder()
                .name("Maya Lin")
                .email("maya.lin@tasknest.edu")
                .password("secureMaya!456")
                .createdAt(LocalDateTime.now().minusDays(5))
                .build();

        userRepository.saveAll(List.of(alex, maya));

        // 2. Create Task Lists for Alex
        TaskList academicList = TaskList.builder()
                .name("Academic Assignments")
                .description("Coursework, midterms, lab reports and project submissions")
                .createdAt(LocalDateTime.now().minusDays(9))
                .user(alex)
                .build();

        TaskList clubList = TaskList.builder()
                .name("Robotics Club")
                .description("Team meetings, hardware sourcing, and competition preparations")
                .createdAt(LocalDateTime.now().minusDays(8))
                .user(alex)
                .build();

        TaskList errandsList = TaskList.builder()
                .name("Personal Errands")
                .description("Grocery shopping, dorm chores, and weekend activities")
                .createdAt(LocalDateTime.now().minusDays(7))
                .user(alex)
                .build();

        // Task List for Maya (to test cross-user move validation)
        TaskList mayaList = TaskList.builder()
                .name("Maya's Research")
                .description("Literature reviews and thesis notes")
                .createdAt(LocalDateTime.now().minusDays(4))
                .user(maya)
                .build();

        taskListRepository.saveAll(List.of(academicList, clubList, errandsList, mayaList));

        // 3. Create Tasks for Alex
        LocalDate today = LocalDate.now();

        // Overdue tasks (due yesterday and 3 days ago, incomplete)
        Task overdueTask1 = Task.builder()
                .title("Submit CS501 Operating Systems Lab 2")
                .description("Complete semaphore synchronization experiments and PDF write-up")
                .dueDate(today.minusDays(2))
                .priority(Priority.HIGH)
                .completed(false)
                .createdAt(LocalDateTime.now().minusDays(5))
                .taskList(academicList)
                .build();

        Task overdueTask2 = Task.builder()
                .title("Order Arduino stepper motor drivers")
                .description("Order 4x A4988 motor driver chips for the robot chassis")
                .dueDate(today.minusDays(1))
                .priority(Priority.MEDIUM)
                .completed(false)
                .createdAt(LocalDateTime.now().minusDays(4))
                .taskList(clubList)
                .build();

        // Tasks due Today
        Task todayTask1 = Task.builder()
                .title("Attend Database Systems Quiz")
                .description("Online Moodle quiz covering SQL joins and normalization")
                .dueDate(today)
                .priority(Priority.HIGH)
                .completed(false)
                .createdAt(LocalDateTime.now().minusDays(2))
                .taskList(academicList)
                .build();

        Task todayTask2 = Task.builder()
                .title("Buy groceries for dorm kitchen")
                .description("Milk, oats, fruits, and bread")
                .dueDate(today)
                .priority(Priority.LOW)
                .completed(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .taskList(errandsList)
                .build();

        // Upcoming tasks
        Task upcomingTask1 = Task.builder()
                .title("Robotics Club General Body Meeting")
                .description("Present subsystem progress in student center room 302")
                .dueDate(today.plusDays(3))
                .priority(Priority.MEDIUM)
                .completed(false)
                .createdAt(LocalDateTime.now().minusDays(1))
                .taskList(clubList)
                .build();

        // Completed task
        Task completedTask1 = Task.builder()
                .title("Pay semester activity fee")
                .description("Paid via university student portal")
                .dueDate(today.minusDays(3))
                .priority(Priority.LOW)
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(3))
                .createdAt(LocalDateTime.now().minusDays(6))
                .taskList(errandsList)
                .build();

        taskRepository.saveAll(List.of(overdueTask1, overdueTask2, todayTask1, todayTask2, upcomingTask1, completedTask1));

        log.info("Sample data initialization completed successfully.");
    }
}
