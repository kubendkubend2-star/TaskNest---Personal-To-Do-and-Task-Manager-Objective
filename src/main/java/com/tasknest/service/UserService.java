package com.tasknest.service;

import com.tasknest.dto.LoginRequestDto;
import com.tasknest.dto.UserRequestDto;
import com.tasknest.dto.UserResponseDto;
import com.tasknest.entity.User;
import com.tasknest.exception.DuplicateEmailException;
import com.tasknest.exception.ResourceNotFoundException;
import com.tasknest.exception.UnauthorizedException;
import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import com.tasknest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final TaskListRepository taskListRepository;
    private final TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public UserResponseDto authenticate(LoginRequestDto request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password. Please verify your credentials."));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new UnauthorizedException("Invalid email or password. Please verify your credentials.");
        }

        return mapToResponseDto(user);
    }

    public UserResponseDto createUser(UserRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email '" + request.getEmail() + "' is already registered");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .password(request.getPassword())
                .build();

        User savedUser = userRepository.save(user);
        return mapToResponseDto(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        User user = getUserEntity(id);
        return mapToResponseDto(user);
    }

    public UserResponseDto updateUser(Long id, UserRequestDto request) {
        User user = getUserEntity(id);

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailAndIdNot(normalizedEmail, id)) {
            throw new DuplicateEmailException("Email '" + request.getEmail() + "' is already registered by another user");
        }

        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setPassword(request.getPassword());

        User updatedUser = userRepository.save(user);
        return mapToResponseDto(updatedUser);
    }

    public void deleteUser(Long id) {
        User user = getUserEntity(id);
        userRepository.delete(user);
    }

    public void clearUserData(Long id) {
        User user = getUserEntity(id);
        List<TaskList> lists = taskListRepository.findByUserId(id);
        taskListRepository.deleteAll(lists);
    }

    public void loadSampleDataForUser(Long id) {
        User user = getUserEntity(id);
        clearUserData(id);

        LocalDate today = LocalDate.now();

        TaskList academic = TaskList.builder()
                .name("Academic Assignments")
                .description("Coursework, midterms, lab reports and project submissions")
                .user(user)
                .build();

        TaskList club = TaskList.builder()
                .name("Robotics Club")
                .description("Team meetings, hardware sourcing, and competition preparations")
                .user(user)
                .build();

        TaskList errands = TaskList.builder()
                .name("Personal Errands")
                .description("Grocery shopping, dorm chores, and weekend activities")
                .user(user)
                .build();

        taskListRepository.saveAll(List.of(academic, club, errands));

        Task t1 = Task.builder()
                .title("Submit CS501 Operating Systems Lab 2")
                .description("Complete semaphore synchronization experiments and PDF write-up")
                .dueDate(today.minusDays(2))
                .priority(Priority.HIGH)
                .completed(false)
                .taskList(academic)
                .build();

        Task t2 = Task.builder()
                .title("Order Arduino stepper motor drivers")
                .description("Order 4x A4988 motor driver chips for the robot chassis")
                .dueDate(today.minusDays(1))
                .priority(Priority.MEDIUM)
                .completed(false)
                .taskList(club)
                .build();

        Task t3 = Task.builder()
                .title("Attend Database Systems Quiz")
                .description("Online Moodle quiz covering SQL joins and normalization")
                .dueDate(today)
                .priority(Priority.HIGH)
                .completed(false)
                .taskList(academic)
                .build();

        Task t4 = Task.builder()
                .title("Buy groceries for dorm kitchen")
                .description("Milk, oats, fruits, and bread")
                .dueDate(today)
                .priority(Priority.LOW)
                .completed(false)
                .taskList(errands)
                .build();

        Task t5 = Task.builder()
                .title("Robotics Club General Body Meeting")
                .description("Present subsystem progress in student center room 302")
                .dueDate(today.plusDays(3))
                .priority(Priority.MEDIUM)
                .completed(false)
                .taskList(club)
                .build();

        Task t6 = Task.builder()
                .title("Pay semester activity fee")
                .description("Paid via university student portal")
                .dueDate(today.minusDays(3))
                .priority(Priority.LOW)
                .completed(true)
                .completedAt(LocalDateTime.now().minusDays(3))
                .taskList(errands)
                .build();

        taskRepository.saveAll(List.of(t1, t2, t3, t4, t5, t6));
    }

    @Transactional(readOnly = true)
    public User getUserEntity(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private UserResponseDto mapToResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .taskListsCount(user.getTaskLists() != null ? user.getTaskLists().size() : 0)
                .build();
    }
}
