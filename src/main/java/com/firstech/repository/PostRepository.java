package com.firstech.repository;

import com.firstech.model.Post;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findAllByOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);

    @Query("""
        SELECT p FROM Post p
        WHERE :q = '' OR LOWER(p.content) LIKE LOWER(CONCAT('%',:q,'%'))
                      OR LOWER(p.title)   LIKE LOWER(CONCAT('%',:q,'%'))
        ORDER BY p.createdAt DESC
        """)
    List<Post> searchByQuery(@Param("q") String q);
}
