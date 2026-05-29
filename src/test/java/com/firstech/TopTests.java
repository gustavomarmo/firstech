package com.firstech;

import com.firstech.dto.*;
import com.firstech.exception.EmailAlreadyExistsException;
import com.firstech.model.*;
import com.firstech.repository.*;
import com.firstech.service.*;
import com.firstech.util.JwtUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Os 10 testes mais importantes do sistema Firstech.
 * Cada par de testes cobre uma regra de negócio crítica de um serviço diferente.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Top 10 Testes Essenciais")
class TopTests {

    // ─────────────────────────────────────────────────────────────────────────
    // TESTES 1 & 2 — AuthService: integridade do cadastro e segurança do logout
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("AuthService")
    class AuthServiceTests {

        @Mock UserRepository               userRepository;
        @Mock RefreshTokenRepository       refreshTokenRepository;
        @Mock PasswordResetTokenRepository passwordResetTokenRepository;
        @Mock JwtUtil                      jwtUtil;
        @Mock PasswordEncoder              passwordEncoder;
        @Mock AuthenticationManager        authenticationManager;
        @Mock EmailService                 emailService;
        @InjectMocks AuthService authService;

        @Test
        @DisplayName("1 — register com e-mail duplicado deve lançar EmailAlreadyExistsException e nunca salvar o usuário")
        void register_emailDuplicado_lancaExcecaoESemSalvar() {
            RegisterRequestDTO req = new RegisterRequestDTO(
                    "Alice", "alice@test.com", "senha1234",
                    null, null, null, null, null, null,
                    null, null, null, null, null, null, null, null);

            when(userRepository.existsByEmail("alice@test.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register(req))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("E-mail");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("2 — logout deve deletar todos os refresh tokens do usuário")
        void logout_deletaTodosRefreshTokensDoUsuario() {
            User user = User.builder().id(1L).email("user@test.com").build();
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

            authService.logout("user@test.com");

            verify(refreshTokenRepository).deleteAllByUser(user);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTES 3 & 4 — JobService: edição de vaga respeitando ownership
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("JobService")
    class JobServiceTests {

        @Mock JobRepository jobRepository;
        @InjectMocks JobService jobService;

        @Test
        @DisplayName("3 — updateJob deve atualizar o título quando chamado pelo dono da vaga")
        void updateJob_atualizaTitulo_quandoChamadoPeloDono() {
            User owner = User.builder().id(1L).name("Recrutador").build();
            Job  job   = Job.builder()
                    .id(10L).title("Título Antigo").recruiter(owner).tags(new ArrayList<>()).build();

            when(jobRepository.findById(10L)).thenReturn(Optional.of(job));
            when(jobRepository.save(any(Job.class))).thenAnswer(inv -> inv.getArgument(0));

            JobRequestDTO dto = new JobRequestDTO(
                    "Título Novo", null, null, null, null, null, null, null, null, null);

            JobResponseDTO result = jobService.updateJob(10L, dto, owner);

            assertThat(result.getTitle()).isEqualTo("Título Novo");
            verify(jobRepository).save(job);
        }

        @Test
        @DisplayName("4 — updateJob deve lançar AccessDeniedException quando chamado por um usuário que não é o dono")
        void updateJob_lancaAccessDenied_quandoNaoDono() {
            User owner    = User.builder().id(1L).name("Dono").build();
            User intruso  = User.builder().id(2L).name("Intruso").build();
            Job  job      = Job.builder()
                    .id(10L).title("Vaga").recruiter(owner).tags(new ArrayList<>()).build();

            when(jobRepository.findById(10L)).thenReturn(Optional.of(job));

            JobRequestDTO dto = new JobRequestDTO(
                    "Hack", null, null, null, null, null, null, null, null, null);

            assertThatThrownBy(() -> jobService.updateJob(10L, dto, intruso))
                    .isInstanceOf(AccessDeniedException.class);
            verify(jobRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTES 5 & 6 — PostService: exclusão de post com controle de autoria
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("PostService")
    class PostServiceTests {

        @Mock PostRepository        postRepository;
        @Mock PostCommentRepository commentRepository;
        @Mock JobRepository         jobRepository;
        @Mock ConnectionRepository  connectionRepository;
        @InjectMocks PostService postService;

        @Test
        @DisplayName("5 — deletePost deve permitir que o autor exclua o próprio post")
        void deletePost_autorPodeExcluirSeuPost() {
            User author = User.builder().id(1L).name("Autor").build();
            Post post   = Post.builder()
                    .id(10L).content("Conteúdo").author(author)
                    .likedByUserIds(new HashSet<>()).tags(new ArrayList<>()).build();

            when(postRepository.findById(10L)).thenReturn(Optional.of(post));

            assertThatCode(() -> postService.deletePost(10L, author)).doesNotThrowAnyException();
            verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("6 — deletePost deve lançar AccessDeniedException quando o solicitante não é o autor")
        void deletePost_lancaAccessDenied_quandoNaoAutor() {
            User author   = User.builder().id(1L).name("Autor").build();
            User estranho = User.builder().id(2L).name("Estranho").build();
            Post post     = Post.builder()
                    .id(10L).content("Post").author(author)
                    .likedByUserIds(new HashSet<>()).tags(new ArrayList<>()).build();

            when(postRepository.findById(10L)).thenReturn(Optional.of(post));

            assertThatThrownBy(() -> postService.deletePost(10L, estranho))
                    .isInstanceOf(AccessDeniedException.class);
            verify(postRepository, never()).delete(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTES 7 & 8 — ConnectionService: aceitar e remover conexão
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("ConnectionService")
    class ConnectionServiceTests {

        @Mock ConnectionRepository connectionRepository;
        @Mock UserRepository       userRepository;
        @Mock PortfolioService     portfolioService;
        @Mock JobRepository        jobRepository;
        @Mock PostService          postService;
        @InjectMocks ConnectionService connectionService;

        @Test
        @DisplayName("7 — acceptRequest deve mudar o status de PENDING para ACCEPTED e salvar")
        void acceptRequest_mudaStatusParaAccepted() {
            User fromUser    = User.builder().id(1L).name("Solicitante").build();
            User currentUser = User.builder().id(2L).name("Receptor").build();
            Connection conn  = Connection.builder()
                    .fromUser(fromUser).toUser(currentUser)
                    .status(ConnectionStatus.PENDING).build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(fromUser));
            when(connectionRepository.findByFromUserAndToUser(fromUser, currentUser))
                    .thenReturn(Optional.of(conn));
            when(connectionRepository.countAcceptedConnections(currentUser)).thenReturn(1);

            ConnectionStatusDTO result = connectionService.acceptRequest(1L, currentUser);

            assertThat(result.status()).isEqualTo("ACCEPTED");
            assertThat(conn.getStatus()).isEqualTo(ConnectionStatus.ACCEPTED);
            verify(connectionRepository).save(conn);
        }

        @Test
        @DisplayName("8 — removeConnection deve deletar a conexão e retornar status NONE")
        void removeConnection_excluiConexaoERetornaNone() {
            User currentUser = User.builder().id(1L).name("Usuário").build();
            User outro       = User.builder().id(2L).name("Outro").build();
            Connection conn  = Connection.builder()
                    .fromUser(currentUser).toUser(outro)
                    .status(ConnectionStatus.ACCEPTED).build();

            when(userRepository.findById(2L)).thenReturn(Optional.of(outro));
            when(connectionRepository.findByFromUserAndToUser(currentUser, outro))
                    .thenReturn(Optional.of(conn));
            when(connectionRepository.findByFromUserAndToUser(outro, currentUser))
                    .thenReturn(Optional.empty());
            when(connectionRepository.countAcceptedConnections(currentUser)).thenReturn(0);

            ConnectionStatusDTO result = connectionService.removeConnection(2L, currentUser);

            assertThat(result.status()).isEqualTo("NONE");
            verify(connectionRepository).delete(conn);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TESTES 9 & 10 — AdminService: integridade na exclusão de dados
    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("AdminService")
    class AdminServiceTests {

        @Mock UserRepository          userRepository;
        @Mock JobRepository           jobRepository;
        @Mock PostRepository          postRepository;
        @Mock PostCommentRepository   postCommentRepository;
        @Mock ConnectionRepository    connectionRepository;
        @Mock MessageRepository       messageRepository;
        @Mock ExperienceRepository    experienceRepository;
        @Mock SkillRepository         skillRepository;
        @Mock CertificationRepository certificationRepository;
        @Mock ProjectRepository       projectRepository;
        @InjectMocks AdminService adminService;

        @Test
        @DisplayName("9 — deletePost deve remover comentários antes de excluir o post (ordem correta)")
        void deletePost_removeComentariosAntesDeletarPost() {
            User author = User.builder().id(1L).build();
            Post post   = Post.builder()
                    .id(10L).author(author)
                    .likedByUserIds(new HashSet<>()).tags(new ArrayList<>()).build();

            when(postRepository.findById(10L)).thenReturn(Optional.of(post));
            InOrder ordem = inOrder(postCommentRepository, postRepository);

            adminService.deletePost(10L);

            ordem.verify(postCommentRepository).deleteByPostId(10L);
            ordem.verify(postRepository).delete(post);
        }

        @Test
        @DisplayName("10 — deleteUser de recrutador deve excluir vagas e posts antes de deletar o usuário")
        void deleteUser_recrutador_excluiVagasEPostsAntesDoDeletarUsuario() {
            User recrutador = User.builder()
                    .id(5L)
                    .roles(new HashSet<>(Set.of(Role.RECRUTADOR)))
                    .build();

            when(userRepository.findById(5L)).thenReturn(Optional.of(recrutador));
            when(jobRepository.findByRecruiterOrderByIdDesc(recrutador)).thenReturn(List.of());
            when(postRepository.findByAuthorOrderByCreatedAtDesc(recrutador)).thenReturn(List.of());

            InOrder ordem = inOrder(jobRepository, postRepository, userRepository);

            adminService.deleteUser(5L);

            ordem.verify(jobRepository).deleteAll(any());
            ordem.verify(postRepository).deleteAll(any());
            ordem.verify(userRepository).delete(recrutador);
        }
    }
}
