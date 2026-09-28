package com.tasknest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskListRequestDto {

    @NotBlank(message = "Task list name is required")
    @Size(min = 1, max = 150, message = "Name must be between 1 and 150 characters")
    private String name;

    private String description;
}
