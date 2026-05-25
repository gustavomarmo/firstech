package com.firstech.controller;

import com.firstech.dto.UsuarioViewModel;
import com.firstech.model.User;
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
 */
@Controller
public class ViewController {

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

    /**
     * Lê o token da query string e passa para o template,
     * que coloca num campo hidden para o JS enviar ao backend.
     */
    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        model.addAttribute("token", token != null ? token : "");
        return "auth/reset-password";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        UsuarioViewModel usuario;
        if (userDetails instanceof User user) {
            usuario = UsuarioViewModel.builder()
                    .nome(user.getName())
                    .nomeCompleto(user.getName())
                    .build();
        } else {
            usuario = UsuarioViewModel.builder().build();
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("posts", List.of());
        model.addAttribute("vagasRecomendadas", List.of());
        model.addAttribute("vagas", List.of());
        model.addAttribute("recrutadores", List.of());
        model.addAttribute("totalVagas", 0);
        model.addAttribute("termoBusca", "");
        model.addAttribute("totalResultados", 0);
        model.addAttribute("resultados", List.of());
        return "main/index";
    }
}
