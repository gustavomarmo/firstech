package com.firstech.controller;

import com.firstech.dto.AdminDashboardDTO;
import com.firstech.model.Role;
import com.firstech.model.User;
import com.firstech.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Serve as páginas do painel administrativo.
 * Acessível apenas por usuários com a role ADMINISTRADOR.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        AdminDashboardDTO stats = adminService.getDashboard();
        model.addAttribute("stats", stats);

        // Pré-processamento dos dados para os gráficos Chart.js
        // (listas separadas de labels e valores para o JS consumir diretamente)
        List<String> topPosterNames  = stats.topPosters().stream()
                .map(AdminDashboardDTO.TopPosterEntry::nome).collect(Collectors.toList());
        List<Long>   topPosterValues = stats.topPosters().stream()
                .map(AdminDashboardDTO.TopPosterEntry::totalPosts).collect(Collectors.toList());

        model.addAttribute("topPosterNames",  topPosterNames);
        model.addAttribute("topPosterValues", topPosterValues);

        // Dados do admin logado para o topbar
        if (userDetails instanceof User u) {
            model.addAttribute("adminNome",  u.getName());
            model.addAttribute("adminEmail", u.getEmail());
        } else {
            model.addAttribute("adminNome",  "Admin");
            model.addAttribute("adminEmail", "");
        }

        return "admin/dashboard";
    }
}
