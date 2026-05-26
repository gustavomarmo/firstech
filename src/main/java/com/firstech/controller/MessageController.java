package com.firstech.controller;

import com.firstech.dto.ConversationDTO;
import com.firstech.dto.MessageDTO;
import com.firstech.model.User;
import com.firstech.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /** Lista conversas do usuário atual. */
    @GetMapping("/conversations")
    public ResponseEntity<List<ConversationDTO>> getConversations(
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(messageService.getConversations(currentUser));
    }

    /** Mensagens entre o usuário atual e {partnerId}. Marca como lidas. */
    @GetMapping("/{partnerId}")
    public ResponseEntity<List<MessageDTO>> getMessages(
        @PathVariable Long partnerId,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(messageService.getMessages(partnerId, currentUser));
    }

    /** Envia mensagem para {recipientId}. Body: {"content":"..."} */
    @PostMapping("/{recipientId}")
    public ResponseEntity<MessageDTO> sendMessage(
        @PathVariable Long recipientId,
        @RequestBody Map<String, String> body,
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(messageService.sendMessage(recipientId, body.get("content"), currentUser));
    }

    /** Contagem de não lidas. */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(
        @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(Map.of("count", messageService.countUnread(currentUser)));
    }
}
