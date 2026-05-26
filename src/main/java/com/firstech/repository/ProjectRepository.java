package com.firstech.repository;

import com.firstech.model.Project;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserOrderByOrderIndexAsc(User user);
}
