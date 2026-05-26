package com.firstech.repository;

import com.firstech.model.TechSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechSkillRepository extends JpaRepository<TechSkill, Long> {

    List<TechSkill> findAllByOrderByNameAsc();

    boolean existsByName(String name);
}
