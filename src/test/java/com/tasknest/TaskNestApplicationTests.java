package com.tasknest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasknest.dto.LoginRequestDto;
import com.tasknest.dto.TaskListRequestDto;
import com.tasknest.dto.TaskRequestDto;
import com.tasknest.dto.UserRequestDto;
import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import com.tasknest.entity.TaskList;
import com.tasknest.entity.User;
import com.tasknest.repository.TaskListRepository;
import com.tasknest.repository.TaskRepository;
import com.tasknest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class TaskNestApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskListRepository taskListRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskListRepository.deleteAll();
        userRepository.deleteAll();
    }

    // 1. Create User Test
    @Test
    @DisplayName("1. Create User - Success")
    void test1_CreateUser() throws Exception {
        UserRequestDto request = UserRequestDto.builder()
                .name("John Doe")
                .email("john.doe@example.com")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    // 2. Create Task List Test
    @Test
    @DisplayName("2. Create Task List - Success")
    void test2_CreateTaskList() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Jane Doe")
                .email("jane.doe@example.com")
                .password("password123")
                .build());

        TaskListRequestDto request = TaskListRequestDto.builder()
                .name("Computer Science Projects")
                .description("All final year university capstone tasks")
                .build();

        mockMvc.perform(post("/api/users/" + user.getId() + "/lists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Computer Science Projects"))
                .andExpect(jsonPath("$.userId").value(user.getId()));
    }

    // 3. Create Task Test
    @Test
    @DisplayName("3. Create Task - Success")
    void test3_CreateTask() throws Exception {
        User user = userRepository.save(User.builder()
                .name("Test User")
                .email("test.user@example.com")
                .password("password123")
                .build());

        TaskList list = taskListRepository.save(TaskList.builder()
                .name("Work List")
                .user(user)
                .build());

        TaskRequestDto request = TaskRequestDto.builder()
                .title("Complete Assignment 1")
                .description("Write 5 pages on distributed systems")
                .dueDate(LocalDate.now().plusDays(2))
                .priority(Priority.HIGH)
                .completed(false)
                .build();

        mockMvc.perform(post("/api/lists/" + list.getId() + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Complete Assignment 1"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.taskListId").value(list.getId()));
    }

    // 4. Mark Task Completed Test
    @Test
    @DisplayName("4. Mark Task Completed - Success")
    void test4_MarkTaskCompleted() throws Exception {
        User user = userRepository.save(User.builder().name("U1").email("u1@a.com").password("pwd123").build());
        TaskList list = taskListRepository.save(TaskList.builder().name("L1").user(user).build());
        Task task = taskRepository.save(Task.builder()
                .title("T1")
                .dueDate(LocalDate.now())
                .priority(Priority.MEDIUM)
                .completed(false)
                .taskList(list)
                .build());

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.completed").value(true))
                .andExpect(jsonPath("$.completedAt").isNotEmpty());
    }

    // 5. Mark Task Incomplete Test
    @Test
    @DisplayName("5. Mark Task Incomplete - Success")
    void test5_MarkTaskIncomplete() throws Exception {
        User user = userRepository.save(User.builder().name("U1").email("u1@a.com").password("pwd123").build());
        TaskList list = taskListRepository.save(TaskList.builder().name("L1").user(user).build());
        Task task = taskRepository.save(Task.builder()
                .title("T1")
                .dueDate(LocalDate.now())
                .priority(Priority.MEDIUM)
                .completed(true)
                .taskList(list)
                .build());

        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/incomplete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(task.getId()))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.completedAt").isEmpty());
    }

    // 6. Get Today's Tasks Test
    @Test
    @DisplayName("6. Get Today's Tasks - Success")
    void test6_GetTodayTasks() throws Exception {
        User user = userRepository.save(User.builder().name("U1").email("u1@a.com").password("pwd123").build());
        TaskList list = taskListRepository.save(TaskList.builder().name("L1").user(user).build());

        LocalDate today = LocalDate.now();
        taskRepository.save(Task.builder().title("Due Today").dueDate(today).priority(Priority.HIGH).taskList(list).build());
        taskRepository.save(Task.builder().title("Due Tomorrow").dueDate(today.plusDays(1)).priority(Priority.LOW).taskList(list).build());

        mockMvc.perform(get("/api/users/" + user.getId() + "/tasks/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Due Today"));
    }

    // 7. Get Overdue Tasks Test
    @Test
    @DisplayName("7. Get Overdue Tasks - Returns incomplete tasks past due date")
    void test7_GetOverdueTasks() throws Exception {
        User user = userRepository.save(User.builder().name("U1").email("u1@a.com").password("pwd123").build());
        TaskList list = taskListRepository.save(TaskList.builder().name("L1").user(user).build());

        LocalDate yesterday = LocalDate.now().minusDays(1);
        Task overdueTask = taskRepository.save(Task.builder()
                .title("Overdue Task")
                .dueDate(yesterday)
                .priority(Priority.HIGH)
                .completed(false)
                .taskList(list)
                .build());

        // Completed task past due should not appear as overdue
        taskRepository.save(Task.builder()
                .title("Past but completed")
                .dueDate(yesterday)
                .priority(Priority.LOW)
                .completed(true)
                .taskList(list)
                .build());

        mockMvc.perform(get("/api/users/" + user.getId() + "/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Overdue Task"))
                .andExpect(jsonPath("$[0].overdue").value(true));

        // When task is marked completed, it should disappear from overdue
        mockMvc.perform(patch("/api/tasks/" + overdueTask.getId() + "/complete"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/users/" + user.getId() + "/tasks/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // 8. Move Task Between Lists Test
    @Test
    @DisplayName("8. Move Task Between Lists - Same user vs different user")
    void test8_MoveTaskBetweenLists() throws Exception {
        User userA = userRepository.save(User.builder().name("User A").email("userA@a.com").password("pwd123").build());
        User userB = userRepository.save(User.builder().name("User B").email("userB@b.com").password("pwd123").build());

        TaskList listA1 = taskListRepository.save(TaskList.builder().name("List A1").user(userA).build());
        TaskList listA2 = taskListRepository.save(TaskList.builder().name("List A2").user(userA).build());
        TaskList listB = taskListRepository.save(TaskList.builder().name("List B").user(userB).build());

        Task task = taskRepository.save(Task.builder()
                .title("Moveable Task")
                .dueDate(LocalDate.now())
                .priority(Priority.MEDIUM)
                .taskList(listA1)
                .build());

        // Successful move within user's own lists
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/move/" + listA2.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taskListId").value(listA2.getId()))
                .andExpect(jsonPath("$.taskListName").value("List A2"));

        // Unauthorized move across different users must return 400 Bad Request
        mockMvc.perform(patch("/api/tasks/" + task.getId() + "/move/" + listB.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("A task can only be moved between lists belonging to the same user"));
    }

    // 9. Invalid Priority Test
    @Test
    @DisplayName("9. Invalid Priority - Returns 400 with clear error message")
    void test9_InvalidPriority() throws Exception {
        User user = userRepository.save(User.builder().name("U1").email("u1@a.com").password("pwd123").build());
        TaskList list = taskListRepository.save(TaskList.builder().name("L1").user(user).build());

        String invalidBody = """
                {
                    "title": "Invalid Priority Task",
                    "dueDate": "2026-09-30",
                    "priority": "SUPER_HIGH"
                }
                """;

        mockMvc.perform(post("/api/lists/" + list.getId() + "/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid priority. Allowed values: LOW, MEDIUM, HIGH"));
    }

    // 10. Invalid Task / List / User ID Test
    @Test
    @DisplayName("10. Invalid IDs - Return 404 Not Found")
    void test10_InvalidIds() throws Exception {
        mockMvc.perform(get("/api/users/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("User not found with id: 99999")));

        mockMvc.perform(get("/api/lists/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Task list not found with id: 99999")));

        mockMvc.perform(get("/api/tasks/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(containsString("Task not found with id: 99999")));
    }

    // 11. Validation Errors Test
    @Test
    @DisplayName("11. Validation Errors & Duplicate Email")
    void test11_ValidationErrors() throws Exception {
        // Missing name, invalid email, password too short
        String invalidUserJson = """
                {
                    "name": "",
                    "email": "not-an-email",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());

        // Duplicate Email test -> 409 Conflict
        userRepository.save(User.builder().name("Original").email("duplicate@test.com").password("pass1234").build());

        UserRequestDto duplicateUser = UserRequestDto.builder()
                .name("Another Person")
                .email("duplicate@test.com")
                .password("pass1234")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(containsString("already registered")));
    }

    // 12. Authentication Endpoints Test (Login & Register)
    @Test
    @DisplayName("12. Authentication Endpoints - Login & Register")
    void test12_AuthEndpoints() throws Exception {
        // Register new user via auth endpoint
        UserRequestDto registerRequest = UserRequestDto.builder()
                .name("Alice Wonderland")
                .email("alice@wonderland.com")
                .password("secretAlice99")
                .build();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.email").value("alice@wonderland.com"))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"));

        // Login with valid credentials
        LoginRequestDto validLogin = LoginRequestDto.builder()
                .email("alice@wonderland.com")
                .password("secretAlice99")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLogin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alice@wonderland.com"))
                .andExpect(jsonPath("$.name").value("Alice Wonderland"));

        // Login with wrong password -> 401 Unauthorized
        LoginRequestDto wrongPass = LoginRequestDto.builder()
                .email("alice@wonderland.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPass)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(containsString("Invalid email or password")));

        // Login with unknown email -> 401 Unauthorized
        LoginRequestDto unknownUser = LoginRequestDto.builder()
                .email("unknown@tasknest.com")
                .password("somePassword123")
                .build();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(unknownUser)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }
}
