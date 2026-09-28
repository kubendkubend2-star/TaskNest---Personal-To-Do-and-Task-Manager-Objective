# TaskNest – Personal To-Do and Task Manager

TaskNest is a robust, full-featured backend REST API service built with **Spring Boot 3**, **Java 21**, and **MySQL**. It is specifically designed for university students, developers, and professionals to streamline task management across academics, club projects, and personal errands.

---

## Table of Contents
1. [Project Overview](#1-project-overview)
2. [Features](#2-features)
3. [Technologies Used](#3-technologies-used)
4. [Database Setup](#4-database-setup)
5. [How to Configure MySQL](#5-how-to-configure-mysql)
6. [How to Run the Project](#6-how-to-run-the-project)
7. [API Endpoint List](#7-api-endpoint-list)
8. [Sample Requests and Responses](#8-sample-requests-and-responses)
9. [Business Rules](#9-business-rules)
10. [Swagger / OpenAPI Documentation](#10-swagger--openapi-documentation)
11. [Postman Testing Instructions](#11-postman-testing-instructions)

---

## 1. Project Overview

Students often juggle high-stakes course deliverables, club responsibilities, and everyday life errands. TaskNest provides a structured, hierarchical task management solution where:
- Users organize tasks into dedicated **Task Lists** (e.g., "Academic Assignments", "Robotics Club", "Personal Errands").
- Each task includes strict due dates, priority tiers (`LOW`, `MEDIUM`, `HIGH`), and status states.
- The system automatically identifies **Overdue Tasks** (tasks past due date that remain incomplete) and keeps them flagged until finished.
- Tasks due today can be queried in a single call.
- Tasks can be dynamically transferred between lists belonging to the same user.
- A **Dashboard** provides real-time counts and key productivity metrics.

---

## 2. Features

- **Modern Web Frontend UI**: A rich single-page application (SPA) built with semantic HTML5, modern vanilla CSS (glassmorphism, dark mode, responsive layout), and vanilla JavaScript. Served directly from `src/main/resources/static/` at `http://localhost:8081/`.
- **Clean Layered Architecture**: Strict separation of concerns (`Controller` → `Service` → `Repository` → `MySQL Database`).
- **User Management**: Create, view, update, and delete users with email uniqueness enforcement.
- **Task List Organization**: Create customized lists per user with automatic cascade deletion.
- **Task Lifecycle & Tracking**:
  - Add tasks with mandatory titles, due dates, and priority levels.
  - Complete/Incomplete status toggle with automatic `completedAt` timestamp tracking.
- **Overdue Monitoring Engine**: Tasks past due date automatically surface in the overdue view and remain there until completed.
- **Today's Focus View**: Quick retrieval of all tasks scheduled for the current day.
- **Cross-List Movement**: Move tasks between lists with ownership validation (prevents moving to another user's list).
- **Multi-Parameter Search & Filtering**: Filter tasks by priority (`LOW`, `MEDIUM`, `HIGH`), completion status (`true`/`false`), and specific due dates.
- **Executive Dashboard API**: Aggregated metrics on total lists, total tasks, completed, pending, overdue, and due-today tasks.
- **Global Exception Handling**: Standardized JSON responses for validation errors, 404s, 409 conflict, 400 bad requests, and invalid priorities.
- **Interactive Swagger / OpenAPI UI**: Built-in interactive API documentation for zero-friction testing.
- **Automated Sample Seed Data**: Seeds realistic test data on initial boot for immediate out-of-the-box exploration.

---

## 3. Technologies Used

| Technology | Version / Specification | Purpose |
| :--- | :--- | :--- |
| **Java** | 21 (LTS) | Core programming language |
| **Spring Boot** | 3.2.5 | Application framework & dependency injection |
| **Maven** | 3.9+ | Build and dependency management |
| **Spring Data JPA** | 3.x | Object-Relational Mapping (ORM) repository abstraction |
| **Hibernate** | 6.x | JPA implementation & schema management |
| **MySQL** | 8.0+ | Relational database storage |
| **Jakarta Validation** | 3.x (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`) | Request payload validation |
| **Lombok** | Latest | Eliminates boilerplate getters/setters/builders |
| **Springdoc OpenAPI** | 2.5.0 (Swagger 3) | Interactive REST API documentation |
| **H2 Database** | 2.x | High-speed in-memory database for automated tests |
| **JUnit 5 & MockMvc** | Latest | Unit and integration test suite |

---

## 4. Database Setup

TaskNest requires a MySQL database called `tasknest_db`.

### Create Database Schema:
Log into your MySQL client and run:
```sql
CREATE DATABASE IF NOT EXISTS tasknest_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Entity Schema & Relationships:

```
+--------------------+        +--------------------+        +--------------------+
|       User         | 1    * |      TaskList      | 1    * |        Task        |
+--------------------+--------+--------------------+--------+--------------------+
| id (PK)            |        | id (PK)            |        | id (PK)            |
| name               |        | name               |        | title              |
| email (UNIQUE)     |        | description        |        | description        |
| password           |        | created_at         |        | due_date           |
| created_at         |        | user_id (FK)       |        | priority (ENUM)    |
+--------------------+        +--------------------+        | completed (BOOL)   |
                                                            | created_at         |
                                                            | completed_at       |
                                                            | task_list_id (FK)  |
                                                            +--------------------+
```

- Hibernate `spring.jpa.hibernate.ddl-auto=update` automatically generates and updates tables and foreign keys upon startup.

---

## 5. How to Configure MySQL

Edit `src/main/resources/application.properties` to match your local MySQL configuration:

```properties
# Server Port (8081 is used to prevent collisions with other local tools)
server.port=8081

# MySQL Database Configuration
# Default connects to MySQL on port 3307 or fallback to environment variables
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3307}/${DB_NAME:tasknest_db}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD:}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=false

# Swagger / OpenAPI Path
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

> **Note on Ports**:
> - If your local MySQL runs on the standard port **3306**, simply set `DB_PORT=3306` or update the URL to `localhost:3306`.
> - If your MySQL root account has a password, specify it in `spring.datasource.password=your_password`.

---

## 6. How to Run the Project

### Option A: Using the Windows Batch Script (`run.bat`)
Double-click `run.bat` or execute in PowerShell / Command Prompt:
```cmd
.\run.bat
```
The script automatically configures Java 21, frees port 8081 if occupied, and launches the server.

### Option B: Using Maven CLI
Ensure `JAVA_HOME` points to Java 21:
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.12.1"
mvn spring-boot:run
```

### Option C: Running Automated Tests
Run the comprehensive 11-scenario test suite:
```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.12.1"
mvn clean test
```

Once started, the backend and frontend are accessible at:
- **Interactive Web App**: [http://localhost:8081/](http://localhost:8081/) or [http://localhost:8081/index.html](http://localhost:8081/index.html)
- **Swagger UI**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

---

## 7. API Endpoint List

### User Management
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/users` | Register a new user | `201 Created` |
| `GET` | `/api/users` | Retrieve all users | `200 OK` |
| `GET` | `/api/users/{id}` | Retrieve user by ID | `200 OK` / `404 Not Found` |
| `PUT` | `/api/users/{id}` | Update user details | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/users/{id}` | Delete user (cascades to lists and tasks) | `204 No Content` / `404` |

### Task List Management
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/users/{userId}/lists` | Create a task list for a user | `201 Created` |
| `GET` | `/api/users/{userId}/lists` | Get all lists for a user | `200 OK` |
| `GET` | `/api/lists/{listId}` | Get specific task list by ID | `200 OK` / `404 Not Found` |
| `PUT` | `/api/lists/{listId}` | Update task list name or description | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/lists/{listId}` | Delete task list | `204 No Content` / `404` |

### Task Management
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/lists/{listId}/tasks` | Create task inside a list | `201 Created` |
| `GET` | `/api/lists/{listId}/tasks` | Get all tasks in a list | `200 OK` |
| `GET` | `/api/tasks/{taskId}` | Get task by ID | `200 OK` / `404 Not Found` |
| `PUT` | `/api/tasks/{taskId}` | Update an existing task | `200 OK` / `404 Not Found` |
| `DELETE` | `/api/tasks/{taskId}` | Delete a task | `204 No Content` / `404` |
| `PATCH` | `/api/tasks/{taskId}/complete` | Mark task as completed | `200 OK` |
| `PATCH` | `/api/tasks/{taskId}/incomplete` | Mark task as incomplete | `200 OK` |
| `PATCH` | `/api/tasks/{taskId}/move/{targetListId}` | Move task between lists (same user) | `200 OK` / `400 Bad Request` |

### Filter & Specialized Views
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/{userId}/tasks/today` | Tasks scheduled for today (`dueDate = today`) | `200 OK` |
| `GET` | `/api/users/{userId}/tasks/overdue` | Overdue tasks (`dueDate < today AND completed = false`) | `200 OK` |
| `GET` | `/api/users/{userId}/tasks` | Filter tasks by `priority`, `completed`, `dueDate` | `200 OK` |

### Dashboard Analytics
| Method | Endpoint | Description | Status Code |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/users/{userId}/dashboard` | Aggregated user metrics | `200 OK` |

---

## 8. Sample Requests and Responses

### 1. Create a User
**Request:**
`POST http://localhost:8081/api/users`
```json
{
  "name": "Alex Mercer",
  "email": "alex.mercer@tasknest.edu",
  "password": "secretPassword123"
}
```

**Response (`201 Created`):**
```json
{
  "id": 1,
  "name": "Alex Mercer",
  "email": "alex.mercer@tasknest.edu",
  "createdAt": "2026-09-28T21:50:12",
  "taskListsCount": 0
}
```

---

### 2. Create a Task List
**Request:**
`POST http://localhost:8081/api/users/1/lists`
```json
{
  "name": "Academic Assignments",
  "description": "Coursework, midterms, lab reports and project submissions"
}
```

**Response (`201 Created`):**
```json
{
  "id": 1,
  "name": "Academic Assignments",
  "description": "Coursework, midterms, lab reports and project submissions",
  "createdAt": "2026-09-28T21:50:12",
  "userId": 1,
  "userName": "Alex Mercer",
  "taskCount": 0
}
```

---

### 3. Create a Task
**Request:**
`POST http://localhost:8081/api/lists/1/tasks`
```json
{
  "title": "Submit CS501 Operating Systems Lab 2",
  "description": "Complete semaphore synchronization experiments and PDF write-up",
  "dueDate": "2026-09-30",
  "priority": "HIGH",
  "completed": false
}
```

**Response (`201 Created`):**
```json
{
  "id": 1,
  "title": "Submit CS501 Operating Systems Lab 2",
  "description": "Complete semaphore synchronization experiments and PDF write-up",
  "dueDate": "2026-09-30",
  "priority": "HIGH",
  "completed": false,
  "createdAt": "2026-09-28T21:50:12",
  "completedAt": null,
  "taskListId": 1,
  "taskListName": "Academic Assignments",
  "overdue": false
}
```

---

### 4. Mark Task Completed
**Request:**
`PATCH http://localhost:8081/api/tasks/1/complete`

**Response (`200 OK`):**
```json
{
  "id": 1,
  "title": "Submit CS501 Operating Systems Lab 2",
  "description": "Complete semaphore synchronization experiments and PDF write-up",
  "dueDate": "2026-09-30",
  "priority": "HIGH",
  "completed": true,
  "createdAt": "2026-09-28T21:50:12",
  "completedAt": "2026-09-28T21:51:27",
  "taskListId": 1,
  "taskListName": "Academic Assignments",
  "overdue": false
}
```

---

### 5. Move Task to Another List
**Request:**
`PATCH http://localhost:8081/api/tasks/1/move/2`

**Response (`200 OK`):**
```json
{
  "id": 1,
  "title": "Submit CS501 Operating Systems Lab 2",
  "description": "Complete semaphore synchronization experiments and PDF write-up",
  "dueDate": "2026-09-30",
  "priority": "HIGH",
  "completed": false,
  "createdAt": "2026-09-28T21:50:12",
  "completedAt": null,
  "taskListId": 2,
  "taskListName": "Robotics Club",
  "overdue": false
}
```

---

### 6. User Dashboard
**Request:**
`GET http://localhost:8081/api/users/1/dashboard`

**Response (`200 OK`):**
```json
{
  "totalTaskLists": 3,
  "totalTasks": 6,
  "completedTasks": 1,
  "incompleteTasks": 5,
  "overdueTasks": 2,
  "tasksDueToday": 2
}
```

---

### 7. Invalid Priority Error Response
**Request:**
`POST http://localhost:8081/api/lists/1/tasks` with `"priority": "SUPER_HIGH"`

**Response (`400 Bad Request`):**
```json
{
  "timestamp": "2026-09-28T21:50:15",
  "status": 400,
  "message": "Invalid priority. Allowed values: LOW, MEDIUM, HIGH"
}
```

---

## 9. Business Rules

1. **Overdue Task Definition**: A task is overdue when `dueDate < current date AND completed = false`.
2. **Persistence of Overdue View**: Overdue tasks continue to appear in `GET /api/users/{userId}/tasks/overdue` until marked complete.
3. **Priority Validation**: Only `LOW`, `MEDIUM`, and `HIGH` values are accepted. Any other value immediately produces a clean HTTP 400 with message `"Invalid priority. Allowed values: LOW, MEDIUM, HIGH"`.
4. **Mandatory Fields**: Task `title`, task `dueDate`, user `name`, and user `email` are strictly validated using Jakarta Bean Validation. Empty values return HTTP 400.
5. **Completion Timestamping**:
   - Marking a task completed sets `completed = true` and `completedAt = current timestamp`.
   - Marking a task incomplete sets `completed = false` and clears `completedAt = null`.
6. **Task Movement Security**: A task can only be moved to a list belonging to the exact same user. Attempting to move across users returns an HTTP 400 error.
7. **Email Uniqueness**: User emails must be unique. Duplicate emails return an HTTP 409 Conflict error.

---

## 10. Swagger / OpenAPI Documentation

TaskNest includes built-in Swagger/OpenAPI documentation.

- **Swagger UI Interactive Console**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **OpenAPI JSON Specification**: [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

You can explore all schemas, try out API calls interactively with pre-populated sample payloads, and verify status codes directly in your browser.

---

## 11. Postman Testing Instructions

A complete, pre-configured Postman Collection is provided in the root directory: [`TaskNest_Postman_Collection.json`](file:///d:/TaskNest/TaskNest_Postman_Collection.json).

### Steps to Import and Test:
1. Open **Postman**.
2. Click the **Import** button in the upper left header.
3. Select or drag-and-drop `TaskNest_Postman_Collection.json`.
4. The imported collection contains organized folders:
   - `1. User Management`
   - `2. Task Lists`
   - `3. Tasks CRUD`
   - `4. Status & Movement`
   - `5. Views & Filters`
   - `6. Dashboard Analytics`
   - `7. Error Validation Scenarios`
5. The collection includes environment variables with defaults:
   - `baseUrl`: `http://localhost:8081`
   - `userId`: `1`
   - `listId`: `1`
   - `taskId`: `1`
   - `targetListId`: `2`
6. Click **Run Collection** or execute individual requests sequentially to test every endpoint!
