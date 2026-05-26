package com.firstech.controller;

import com.firstech.dto.ConnectionStatusDTO;
import com.firstech.dto.PublicProfileDTO;
import com.firstech.model.User;
import com.firstech.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connections")
@RequiredArgsConstructor
public class ConnectionController {

    private final ConnectionService connectionService;

    /** Envia pedido de conexão para o usuário {id}. */
    @PostMapping("/request/{id}")
    public ResponseEntity<ConnectionStatusDTO> sendRequest(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(connectionService.sendRequest(id, currentUser));
    }

    /** Aceita pedido de conexão do usuário {id}. */
    @PostMapping("/accept/{id}")
    public ResponseEntity<ConnectionStatusDTO> acceptRequest(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(connectionService.acceptRequest(id, currentUser));
    }

    /** Remove ou rejeita conexão com o usuário {id}. */
    @DeleteMapping("/{id}")
    public ResponseEntity<ConnectionStatusDTO> removeConnection(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(connectionService.removeConnection(id, currentUser));
    }

    /** Lista conexões aceitas do usuário atual (para "nova conversa"). */
    @GetMapping("/my")
    public ResponseEntity<List<PublicProfileDTO>> myConnections(
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(connectionService.getMyConnections(currentUser));
    }

    /** Pedidos de conexão recebidos e ainda pendentes. */
    @GetMapping("/pending-received")
    public ResponseEntity<List<PublicProfileDTO>> pendingReceived(
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(connectionService.getPendingReceived(currentUser));
    }
}
