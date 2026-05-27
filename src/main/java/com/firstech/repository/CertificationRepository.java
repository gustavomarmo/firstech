package com.firstech.repository;

import com.firstech.model.Certification;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByUserOrderByOrderIndexAsc(User user);

    @Modifying
    @Query("DELETE FROM Certification c WHERE c.user = :user")
    void deleteByUser(@Param("user") User user);
}
