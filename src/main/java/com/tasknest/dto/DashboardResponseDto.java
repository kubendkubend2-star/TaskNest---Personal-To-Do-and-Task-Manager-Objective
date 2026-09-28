package com.tasknest.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDto {

    private long totalTaskLists;
    private long totalTasks;
    private long completedTasks;
    private long incompleteTasks;
    private long overdueTasks;
    private long tasksDueToday;
}
