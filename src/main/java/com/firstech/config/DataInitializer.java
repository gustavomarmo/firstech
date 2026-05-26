package com.firstech.config;

import com.firstech.model.Role;
import com.firstech.model.User;
import com.firstech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = User.builder()
                    .name("Administrador")
                    .email("admin@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(Set.of(Role.ADMINISTRADOR, Role.RECRUTADOR))
                    .company("Firstech")
                    .build();
            userRepository.save(admin);
            log.info("Usuário admin (recrutador) criado: admin@example.com / Admin@123");
        }

        if (!userRepository.existsByEmail("candidato@example.com")) {
            User candidato = User.builder()
                    .name("Dev Candidato")
                    .email("candidato@example.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .roles(Set.of(Role.CANDIDATO))
                    .careerMoment("junior")
                    .city("São Paulo, SP")
                    .tagline("Desenvolvedor Java apaixonado por tecnologia")
                    .build();
            userRepository.save(candidato);
            log.info("Usuário candidato criado: candidato@example.com / Admin@123");
        }
    }
}
