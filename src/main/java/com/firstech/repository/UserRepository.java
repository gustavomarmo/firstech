package com.firstech.repository;

import com.firstech.model.Role;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("""
        SELECT DISTINCT u FROM User u JOIN u.roles r
        WHERE r = :role
          AND (:q = '' OR LOWER(u.name)    LIKE LOWER(CONCAT('%',:q,'%'))
                       OR LOWER(u.tagline) LIKE LOWER(CONCAT('%',:q,'%'))
                       OR LOWER(u.city)    LIKE LOWER(CONCAT('%',:q,'%')))
        ORDER BY u.name ASC
        """)
    List<User> searchByRole(@Param("role") Role role, @Param("q") String q);
}
