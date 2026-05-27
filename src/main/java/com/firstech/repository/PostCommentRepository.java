package com.firstech.repository;

import com.firstech.model.PostComment;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
    List<PostComment> findByPostIdOrderByCreatedAtAsc(Long postId);

    /** Deleta comentários feitos pelo usuário (em posts de outros). */
    @Modifying
    @Query("DELETE FROM PostComment pc WHERE pc.author = :user")
    void deleteByAuthor(@Param("user") User user);

    /** Deleta comentários de outros usuários nos posts do usuário. */
    @Modifying
    @Query("DELETE FROM PostComment pc WHERE pc.post IN (SELECT p FROM Post p WHERE p.author = :user)")
    void deleteByPostAuthor(@Param("user") User user);
}
