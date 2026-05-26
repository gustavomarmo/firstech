package com.firstech.repository;
import com.firstech.model.Message;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender = :a AND m.recipient = :b)
           OR (m.sender = :b AND m.recipient = :a)
        ORDER BY m.createdAt ASC
        """)
    List<Message> findConversation(@Param("a") User a, @Param("b") User b);

    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.recipient = :user AND m.read = false
        """)
    long countUnread(@Param("user") User user);

    @Modifying
    @Query("""
        UPDATE Message m SET m.read = true
        WHERE m.recipient = :recipient AND m.sender = :sender AND m.read = false
        """)
    void markAsRead(@Param("recipient") User recipient, @Param("sender") User sender);

    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender = :user OR m.recipient = :user)
        ORDER BY m.id DESC
        """)
    List<Message> findAllByUser(@Param("user") User user);
}
