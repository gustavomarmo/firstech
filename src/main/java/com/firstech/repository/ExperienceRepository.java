package com.firstech.repository;

import com.firstech.model.Experience;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByUserOrderByOrderIndexAsc(User user);
}
