package com.firstech.repository;

import com.firstech.model.Project;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByUserOrderByOrderIndexAsc(User user);

    @Modifying
    @Query("DELETE FROM Project p WHERE p.user = :user")
    void deleteByUser(@Param("user") User user);
}
