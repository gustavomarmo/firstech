package com.firstech.service;

import com.firstech.dto.ConversationDTO;
import com.firstech.dto.MessageDTO;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.*;
import com.firstech.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository    userRepository;

    private static final String[] AVATAR_GRADIENTS = {
        "linear-gradient(135deg,#6d28d9,#8b5cf6)",
        "linear-gradient(135deg,#1a56db,#3a86ff)",
        "linear-gradient(135deg,#0f6e56,#1d9e75)",
        "linear-gradient(135deg,#854f0b,#ef9f27)",
        "linear-gradient(135deg,#0e7490,#06b6d4)",
        "linear-gradient(135deg,#be185d,#f472b6)",
        "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    /** Retorna a lista de conversas do usuário (última mensagem de cada conversa). */
    @Transactional(readOnly = true)
    public List<ConversationDTO> getConversations(User user) {
        List<Message> allMessages = messageRepository.findAllByUser(user);

        // Group by partner, keep only the last message per partner
        Map<Long, Message> lastPerPartner = new LinkedHashMap<>();
        for (Message m : allMessages) {
            User partner = m.getSender().getId().equals(user.getId()) ? m.getRecipient() : m.getSender();
            // findAllByUser is ordered by id DESC, so first occurrence = latest
            lastPerPartner.putIfAbsent(partner.getId(), m);
        }

        List<ConversationDTO> result = new ArrayList<>();
        for (Map.Entry<Long, Message> entry : lastPerPartner.entrySet()) {
            Message m = entry.getValue();
            User partner = m.getSender().getId().equals(user.getId()) ? m.getRecipient() : m.getSender();

            // Count unread from this partner
            long unread = messageRepository.findConversation(user, partner)
                .stream().filter(msg -> msg.getRecipient().getId().equals(user.getId()) && !msg.isRead()).count();

            String nome    = partner.getName() != null ? partner.getName() : "Usuário";
            String inicial = nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase();
            String corAvatar = AVATAR_GRADIENTS[Math.abs(nome.hashCode() % AVATAR_GRADIENTS.length)];

            result.add(new ConversationDTO(
                partner.getId(),
                nome,
                inicial,
                corAvatar,
                partner.getAvatarBase64(),
                truncate(m.getContent(), 50),
                relativeTime(m.getCreatedAt()),
                (int) unread
            ));
        }
        return result;
    }

    /** Retorna as mensagens entre dois usuários e marca as recebidas como lidas. */
    @Transactional
    public List<MessageDTO> getMessages(Long partnerId, User currentUser) {
        User partner = userRepository.findById(partnerId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        // Marca como lidas
        messageRepository.markAsRead(currentUser, partner);

        return messageRepository.findConversation(currentUser, partner)
            .stream().map(m -> toDTO(m, currentUser.getId())).toList();
    }

    /** Envia uma mensagem. */
    @Transactional
    public MessageDTO sendMessage(Long recipientId, String content, User sender) {
        User recipient = userRepository.findById(recipientId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Mensagem não pode ser vazia.");

        Message msg = Message.builder()
            .sender(sender)
            .recipient(recipient)
            .content(content.trim())
            .build();

        return toDTO(messageRepository.save(msg), sender.getId());
    }

    /** Total de mensagens não lidas para o usuário. */
    @Transactional(readOnly = true)
    public long countUnread(User user) {
        return messageRepository.countUnread(user);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private MessageDTO toDTO(Message m, Long currentUserId) {
        return new MessageDTO(
            m.getId(),
            m.getSender().getId(),
            m.getContent(),
            relativeTime(m.getCreatedAt()),
            m.getSender().getId().equals(currentUserId),
            m.isRead()
        );
    }

    private static String relativeTime(LocalDateTime dt) {
        if (dt == null) return "";
        long minutes = ChronoUnit.MINUTES.between(dt, LocalDateTime.now());
        if (minutes < 1)   return "agora";
        if (minutes < 60)  return minutes + "m";
        long hours = minutes / 60;
        if (hours < 24)    return hours + "h";
        return DateTimeFormatter.ofPattern("dd/MM").format(dt);
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
