package com.tasknest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tasknest.entity.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDto {

    @NotBlank(message = "Task title is required")
    @Size(min = 1, max = 200, message = "Task title must be between 1 and 200 characters")
    private String title;

    private String description;

    @NotNull(message = "Due date cannot be null")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dueDate;

    @NotNull(message = "Priority is required. Allowed values: LOW, MEDIUM, HIGH")
    private Priority priority;

    private Boolean completed;
}
