package com.tasknest.repository;

import com.tasknest.entity.TaskList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskListRepository extends JpaRepository<TaskList, Long> {

    List<TaskList> findByUserId(Long userId);

    long countByUserId(Long userId);

    Optional<TaskList> findByIdAndUserId(Long id, Long userId);
}
