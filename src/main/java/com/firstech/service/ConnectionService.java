package com.firstech.service;

import com.firstech.dto.*;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.*;
import com.firstech.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final UserRepository       userRepository;
    private final PortfolioService     portfolioService;
    private final JobRepository        jobRepository;
    private final PostService          postService;

    private static final String[] AVATAR_GRADIENTS = {
        "linear-gradient(135deg,#6d28d9,#8b5cf6)",
        "linear-gradient(135deg,#1a56db,#3a86ff)",
        "linear-gradient(135deg,#0f6e56,#1d9e75)",
        "linear-gradient(135deg,#854f0b,#ef9f27)",
        "linear-gradient(135deg,#0e7490,#06b6d4)",
        "linear-gradient(135deg,#be185d,#f472b6)",
        "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    /** Retorna o perfil público completo de um usuário com status de conexão em relação ao viewer. */
    @Transactional(readOnly = true)
    public PublicProfileDTO getPublicProfile(Long targetId, User viewer) {
        User target = userRepository.findById(targetId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        String status     = resolveConnectionStatus(viewer, target);
        int totalConexoes = connectionRepository.countAcceptedConnections(target);

        String nome      = target.getName() != null ? target.getName() : "Usuário";
        String inicial   = nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase();
        String corAvatar = AVATAR_GRADIENTS[Math.abs(nome.hashCode() % AVATAR_GRADIENTS.length)];

        boolean isRecruiter = target.getRoles() != null && target.getRoles().contains(Role.RECRUTADOR);

        List<VagaResumoDTO> vagasPublicadas = isRecruiter
            ? jobRepository.findByRecruiterOrderByIdDesc(target).stream()
                .map(j -> new VagaResumoDTO(
                    j.getId(),
                    j.getTitle(),
                    j.getJobCompany() != null ? j.getJobCompany() : nome,
                    companyLogo(j),
                    j.getJobCompanyLogoUrl(),
                    j.getModality(),
                    j.getLevel(),
                    j.getLocation(),
                    j.getStatus() == JobStatus.ATIVA,
                    j.getApplyUrl()
                ))
                .toList()
            : List.of();

        return new PublicProfileDTO(
            target.getId(),
            nome,
            target.getTagline()      != null ? target.getTagline()      : "",
            target.getCity()         != null ? target.getCity()         : "",
            target.getSobre()        != null ? target.getSobre()        : "",
            target.getAvatarBase64(),
            target.getBannerBase64(),
            corAvatar,
            inicial,
            totalConexoes,
            target.getTechnologies() != null ? target.getTechnologies() : List.of(),
            status,
            isRecruiter,
            portfolioService.getExperiences(target),
            portfolioService.getSkills(target),
            portfolioService.getCertifications(target),
            isRecruiter ? List.of() : portfolioService.getProjects(target),
            vagasPublicadas,
            isRecruiter ? List.of() : postService.getProjectPostsByAuthor(target),
            isRecruiter ? List.of() : postService.getRegularPostsByAuthor(target)
        );
    }

    /** Pedidos de conexão pendentes recebidos pelo usuário. */
    @Transactional(readOnly = true)
    public List<PublicProfileDTO> getPendingReceived(User user) {
        return connectionRepository.findPendingReceived(user)
            .stream()
            .map(c -> getPublicProfile(c.getFromUser().getId(), user))
            .toList();
    }

    /** Lista as conexões aceitas do usuário atual (para "nova conversa"). */
    @Transactional(readOnly = true)
    public List<PublicProfileDTO> getMyConnections(User user) {
        return connectionRepository.findByUserAndStatus(user, ConnectionStatus.ACCEPTED)
            .stream()
            .map(c -> {
                User other = c.getFromUser().getId().equals(user.getId())
                    ? c.getToUser() : c.getFromUser();
                return getPublicProfile(other.getId(), user);
            })
            .toList();
    }

    /** Envia pedido de conexão de `from` para `to`. */
    @Transactional
    public ConnectionStatusDTO sendRequest(Long toId, User from) {
        User to = userRepository.findById(toId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        if (from.getId().equals(toId))
            throw new IllegalArgumentException("Não pode se conectar consigo mesmo.");

        // Se já existe conexão no sentido inverso (PENDING) → aceita automaticamente
        Optional<Connection> reverse = connectionRepository.findByFromUserAndToUser(to, from);
        if (reverse.isPresent() && reverse.get().getStatus() == ConnectionStatus.PENDING) {
            reverse.get().setStatus(ConnectionStatus.ACCEPTED);
            connectionRepository.save(reverse.get());
            return new ConnectionStatusDTO("ACCEPTED", connectionRepository.countAcceptedConnections(from));
        }

        // Se já existe no mesmo sentido, ignora
        if (connectionRepository.findByFromUserAndToUser(from, to).isPresent()) {
            return new ConnectionStatusDTO(resolveConnectionStatus(from, to),
                connectionRepository.countAcceptedConnections(from));
        }

        connectionRepository.save(Connection.builder().fromUser(from).toUser(to).build());
        return new ConnectionStatusDTO("PENDING_SENT", connectionRepository.countAcceptedConnections(from));
    }

    /** Aceita pedido de conexão enviado por `fromId` para `currentUser`. */
    @Transactional
    public ConnectionStatusDTO acceptRequest(Long fromId, User currentUser) {
        User fromUser = userRepository.findById(fromId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        Connection conn = connectionRepository.findByFromUserAndToUser(fromUser, currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Pedido não encontrado."));

        conn.setStatus(ConnectionStatus.ACCEPTED);
        connectionRepository.save(conn);
        return new ConnectionStatusDTO("ACCEPTED", connectionRepository.countAcceptedConnections(currentUser));
    }

    /** Remove ou rejeita uma conexão (em qualquer direção). */
    @Transactional
    public ConnectionStatusDTO removeConnection(Long otherId, User currentUser) {
        User other = userRepository.findById(otherId)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        connectionRepository.findByFromUserAndToUser(currentUser, other)
            .ifPresent(connectionRepository::delete);
        connectionRepository.findByFromUserAndToUser(other, currentUser)
            .ifPresent(connectionRepository::delete);

        return new ConnectionStatusDTO("NONE", connectionRepository.countAcceptedConnections(currentUser));
    }

    /** Retorna o total de conexões aceitas de um usuário. */
    @Transactional(readOnly = true)
    public int countConnections(User user) {
        return connectionRepository.countAcceptedConnections(user);
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private static final String[] JOB_GRADIENTS = {
        "linear-gradient(135deg,#6d28d9,#8b5cf6)",
        "linear-gradient(135deg,#1a56db,#3a86ff)",
        "linear-gradient(135deg,#0f6e56,#1d9e75)",
        "linear-gradient(135deg,#854f0b,#ef9f27)",
        "linear-gradient(135deg,#0e7490,#06b6d4)",
        "linear-gradient(135deg,#be185d,#f472b6)",
        "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    private static String companyLogo(Job j) {
        if (j.getJobCompanyLogoUrl() != null && !j.getJobCompanyLogoUrl().isBlank()) {
            return "url('" + j.getJobCompanyLogoUrl() + "') center/cover";
        }
        String key = j.getJobCompany() != null ? j.getJobCompany() : j.getTitle();
        return JOB_GRADIENTS[Math.abs(key.hashCode() % JOB_GRADIENTS.length)];
    }

    public String resolveConnectionStatus(User viewer, User target) {
        if (viewer == null || viewer.getId().equals(target.getId())) return "SELF";

        Optional<Connection> sentOpt     = connectionRepository.findByFromUserAndToUser(viewer, target);
        Optional<Connection> receivedOpt = connectionRepository.findByFromUserAndToUser(target, viewer);

        if (sentOpt.isPresent()) {
            return sentOpt.get().getStatus() == ConnectionStatus.ACCEPTED ? "ACCEPTED" : "PENDING_SENT";
        }
        if (receivedOpt.isPresent()) {
            return receivedOpt.get().getStatus() == ConnectionStatus.ACCEPTED ? "ACCEPTED" : "PENDING_RECEIVED";
        }
        return "NONE";
    }
}
