package com.firstech.repository;
import com.firstech.model.Connection;
import com.firstech.model.ConnectionStatus;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ConnectionRepository extends JpaRepository<Connection, Long> {
    Optional<Connection> findByFromUserAndToUser(User from, User to);

    @Query("SELECT c FROM Connection c WHERE (c.fromUser = :u OR c.toUser = :u) AND c.status = :status")
    List<Connection> findByUserAndStatus(@Param("u") User user, @Param("status") ConnectionStatus status);

    @Query("SELECT COUNT(c) FROM Connection c WHERE (c.fromUser = :u OR c.toUser = :u) AND c.status = 'ACCEPTED'")
    int countAcceptedConnections(@Param("u") User user);

    @Query("SELECT c FROM Connection c WHERE c.toUser = :u AND c.status = 'PENDING'")
    List<Connection> findPendingReceived(@Param("u") User user);

    /** Pedidos enviados pelo usuário com determinado status. */
    List<Connection> findByFromUserAndStatus(User fromUser, ConnectionStatus status);

    /** Total de conexões aceitas em toda a plataforma. */
    @Query("SELECT COUNT(c) FROM Connection c WHERE c.status = 'ACCEPTED'")
    long countAllAccepted();
}
