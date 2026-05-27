package com.firstech.repository;

import com.firstech.model.Experience;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByUserOrderByOrderIndexAsc(User user);

    @Modifying
    @Query("DELETE FROM Experience e WHERE e.user = :user")
    void deleteByUser(@Param("user") User user);
}
