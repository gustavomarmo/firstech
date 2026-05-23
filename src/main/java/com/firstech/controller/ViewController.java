package com.firstech.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    /** Exemplo de rota protegida — substitua pelo seu dashboard real. */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "main/index"; // crie templates/dashboard.html
    }
}
