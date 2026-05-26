package com.firstech.controller;

import com.firstech.dto.UsuarioViewModel;
import com.firstech.model.Role;
import com.firstech.model.User;
import com.firstech.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final JobService jobService;

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
                    .nome(user.getName())
                    .nomeCompleto(user.getName())
                    .headline(user.getTagline() != null ? user.getTagline() : "")
                    .localizacao(user.getCity() != null ? user.getCity() : "")
                    .cargo(cargo)
                    .empresa(empresa)
                    .recrutador(isRecruiter)
                    .build();
        } else {
            usuario = UsuarioViewModel.builder().build();
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("posts", List.of());
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

    private static String labelCareerMoment(String careerMoment) {
        return switch (careerMoment) {
            case "estagio"  -> "Estagiário";
            case "junior"   -> "Dev Júnior";
            case "studying" -> "Estudante";
            default         -> "Desenvolvedor";
        };
    }
}
