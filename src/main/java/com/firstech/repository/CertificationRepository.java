package com.firstech.repository;

import com.firstech.model.Certification;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByUserOrderByOrderIndexAsc(User user);
}
