package com.firstech.controller;

import com.firstech.dto.PublicProfileDTO;
import com.firstech.dto.UsuarioViewModel;
import com.firstech.model.Role;
import com.firstech.model.User;
import com.firstech.service.ConnectionService;
import com.firstech.service.JobService;
import com.firstech.service.PortfolioService;
import com.firstech.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Mapeia as rotas de view para os templates Thymeleaf.
 * Não tem lógica de negócio — apenas serve as páginas HTML.
 * A autenticação de fato acontece via JS → POST /api/auth/*.
 *
 * O JWT é enviado como cookie HttpOnly pelo browser em toda navegação,
 * então @AuthenticationPrincipal é populado mesmo em páginas públicas.
 */
@Controller
@RequiredArgsConstructor
public class ViewController {

    private final JobService        jobService;
    private final PostService       postService;
    private final PortfolioService  portfolioService;
    private final ConnectionService connectionService;

    @GetMapping({"/", "/login"})
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "auth/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token != null ? token : "");
        return "auth/reset-password";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        UsuarioViewModel usuario;
        boolean isRecruiter = false;

        if (userDetails instanceof User user) {
            isRecruiter = user.getRoles().contains(Role.RECRUTADOR);

            String cargo   = isRecruiter ? "Recrutador" : (user.getCareerMoment() != null
                    ? labelCareerMoment(user.getCareerMoment()) : "Desenvolvedor");
            String empresa = isRecruiter
                    ? (user.getCompany() != null ? user.getCompany() : "")
                    : "";

            usuario = UsuarioViewModel.builder()
                    .userId(user.getId())
                    .nome(user.getName())
                    .nomeCompleto(user.getName())
                    .headline(user.getTagline() != null ? user.getTagline() : "")
                    .localizacao(user.getCity() != null ? user.getCity() : "")
                    .sobre(user.getSobre() != null ? user.getSobre() : "")
                    .avatarBase64(user.getAvatarBase64())
                    .bannerBase64(user.getBannerBase64())
                    .cargo(cargo)
                    .empresa(empresa)
                    .recrutador(isRecruiter)
                    .experiencias(portfolioService.getExperiences(user))
                    .habilidades(portfolioService.getSkills(user))
                    .certificacoes(portfolioService.getCertifications(user))
                    .projetos(portfolioService.getProjects(user))
                    .build();
        } else {
            usuario = UsuarioViewModel.builder().build();
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("posts", userDetails instanceof User u ? postService.getAllPosts(u) : postService.getAllPosts());
        model.addAttribute("vagasRecomendadas", List.of());
        model.addAttribute("recrutadores", List.of());
        model.addAttribute("termoBusca", "");
        model.addAttribute("totalResultados", 0);
        model.addAttribute("resultados", List.of());

        // Vagas ativas para candidatos; vagas do recrutador para o próprio recrutador
        if (isRecruiter && userDetails instanceof User user) {
            var minhasVagas = jobService.getRecruiterJobs(user);
            model.addAttribute("vagas", minhasVagas);
            model.addAttribute("minhasVagas", minhasVagas);
            model.addAttribute("totalVagas", minhasVagas.size());
        } else {
            var vagas = jobService.getActiveJobs();
            model.addAttribute("vagas", vagas);
            model.addAttribute("vagasRecomendadas", vagas.stream().limit(4).toList());
            model.addAttribute("minhasVagas", List.of());
            model.addAttribute("totalVagas", vagas.size());
        }

        return "main/index";
    }

    /** Página pública de perfil — acessível por qualquer usuário logado. */
    @GetMapping("/profile/{id}")
    public String publicProfile(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        User viewer = userDetails instanceof User u ? u : null;

        // Perfil do usuário visitado
        PublicProfileDTO profile = connectionService.getPublicProfile(id, viewer);
        model.addAttribute("profile", profile);

        // Dados do usuário logado para a topbar
        if (viewer != null) {
            boolean isRec = viewer.getRoles().contains(Role.RECRUTADOR);
            model.addAttribute("usuario", UsuarioViewModel.builder()
                    .userId(viewer.getId())
                    .nome(viewer.getName())
                    .nomeCompleto(viewer.getName())
                    .headline(viewer.getTagline() != null ? viewer.getTagline() : "")
                    .avatarBase64(viewer.getAvatarBase64())
                    .cargo(isRec ? "Recrutador" : "")
                    .recrutador(isRec)
                    .build());
        } else {
            model.addAttribute("usuario", UsuarioViewModel.builder().build());
        }

        return "profile/view";
    }

    private static String labelCareerMoment(String careerMoment) {
        return switch (careerMoment) {
            case "estagio"  -> "Estagiário";
            case "junior"   -> "Dev Júnior";
            case "studying" -> "Estudante";
            default         -> "Desenvolvedor";
        };
    }
}
