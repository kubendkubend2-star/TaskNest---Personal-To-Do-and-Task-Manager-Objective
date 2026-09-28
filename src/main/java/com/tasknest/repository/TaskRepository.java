package com.tasknest.repository;

import com.tasknest.entity.Priority;
import com.tasknest.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByTaskListId(Long taskListId);

    List<Task> findByTaskListUserId(Long userId);

    @Query("SELECT t FROM Task t WHERE t.taskList.user.id = :userId AND t.dueDate = :today ORDER BY t.priority DESC, t.id ASC")
    List<Task> findTodayTasksByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    @Query("SELECT t FROM Task t WHERE t.taskList.user.id = :userId AND t.dueDate < :currentDate AND t.completed = false ORDER BY t.dueDate ASC, t.priority DESC")
    List<Task> findOverdueTasksByUser(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT t FROM Task t WHERE t.taskList.user.id = :userId " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:completed IS NULL OR t.completed = :completed) " +
           "AND (:dueDate IS NULL OR t.dueDate = :dueDate) " +
           "ORDER BY t.dueDate ASC, t.id ASC")
    List<Task> filterTasks(@Param("userId") Long userId,
                           @Param("priority") Priority priority,
                           @Param("completed") Boolean completed,
                           @Param("dueDate") LocalDate dueDate);

    long countByTaskListUserId(Long userId);

    long countByTaskListUserIdAndCompletedTrue(Long userId);

    long countByTaskListUserIdAndCompletedFalse(Long userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskList.user.id = :userId AND t.dueDate < :currentDate AND t.completed = false")
    long countOverdueTasks(@Param("userId") Long userId, @Param("currentDate") LocalDate currentDate);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.taskList.user.id = :userId AND t.dueDate = :today")
    long countTodayTasks(@Param("userId") Long userId, @Param("today") LocalDate today);
}
