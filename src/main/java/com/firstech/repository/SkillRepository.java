package com.firstech.repository;

import com.firstech.model.Skill;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> {
    List<Skill> findByUserOrderByOrderIndexAsc(User user);
    void deleteAllByUser(User user);
}
