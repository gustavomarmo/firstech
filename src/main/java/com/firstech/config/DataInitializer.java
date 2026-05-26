package com.firstech.config;

import com.firstech.model.Role;
import com.firstech.model.TechSkill;
import com.firstech.model.User;
import com.firstech.repository.TechSkillRepository;
import com.firstech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository     userRepository;
    private final TechSkillRepository techSkillRepository;
    private final PasswordEncoder    passwordEncoder;

    // ── Catálogo de habilidades tecnológicas ──────────────────────────────
    private static final List<String[]> TECH_SKILLS = List.of(
            // {name, category}
            // Linguagens
            new String[]{"Java",         "Linguagem"},
            new String[]{"Python",        "Linguagem"},
            new String[]{"JavaScript",    "Linguagem"},
            new String[]{"TypeScript",    "Linguagem"},
            new String[]{"C",             "Linguagem"},
            new String[]{"C++",           "Linguagem"},
            new String[]{"C#",            "Linguagem"},
            new String[]{"Go",            "Linguagem"},
            new String[]{"Rust",          "Linguagem"},
            new String[]{"Kotlin",        "Linguagem"},
            new String[]{"Swift",         "Linguagem"},
            new String[]{"PHP",           "Linguagem"},
            new String[]{"Ruby",          "Linguagem"},
            new String[]{"Scala",         "Linguagem"},
            new String[]{"Dart",          "Linguagem"},
            new String[]{"R",             "Linguagem"},
            new String[]{"Bash/Shell",    "Linguagem"},
            new String[]{"Lua",           "Linguagem"},
            // Frameworks Frontend
            new String[]{"React",         "Framework Frontend"},
            new String[]{"Angular",       "Framework Frontend"},
            new String[]{"Vue.js",        "Framework Frontend"},
            new String[]{"Next.js",       "Framework Frontend"},
            new String[]{"Nuxt.js",       "Framework Frontend"},
            new String[]{"Svelte",        "Framework Frontend"},
            new String[]{"Remix",         "Framework Frontend"},
            new String[]{"Tailwind CSS",  "Framework Frontend"},
            new String[]{"Bootstrap",     "Framework Frontend"},
            // Frameworks Backend
            new String[]{"Spring Boot",   "Framework Backend"},
            new String[]{"Node.js",       "Framework Backend"},
            new String[]{"Express.js",    "Framework Backend"},
            new String[]{"NestJS",        "Framework Backend"},
            new String[]{"Django",        "Framework Backend"},
            new String[]{"Flask",         "Framework Backend"},
            new String[]{"FastAPI",       "Framework Backend"},
            new String[]{"Laravel",       "Framework Backend"},
            new String[]{"Ruby on Rails", "Framework Backend"},
            new String[]{"ASP.NET Core",  "Framework Backend"},
            new String[]{"Gin",           "Framework Backend"},
            new String[]{"Quarkus",       "Framework Backend"},
            // Mobile
            new String[]{"Flutter",       "Mobile"},
            new String[]{"React Native",  "Mobile"},
            new String[]{"Android",       "Mobile"},
            new String[]{"iOS (Swift)",   "Mobile"},
            // Bancos de Dados
            new String[]{"MySQL",         "Banco de Dados"},
            new String[]{"PostgreSQL",    "Banco de Dados"},
            new String[]{"MongoDB",       "Banco de Dados"},
            new String[]{"Redis",         "Banco de Dados"},
            new String[]{"SQLite",        "Banco de Dados"},
            new String[]{"Oracle DB",     "Banco de Dados"},
            new String[]{"SQL Server",    "Banco de Dados"},
            new String[]{"Firebase",      "Banco de Dados"},
            new String[]{"Cassandra",     "Banco de Dados"},
            new String[]{"DynamoDB",      "Banco de Dados"},
            new String[]{"Elasticsearch", "Banco de Dados"},
            new String[]{"Neo4j",         "Banco de Dados"},
            new String[]{"MariaDB",       "Banco de Dados"},
            // Cloud & DevOps
            new String[]{"AWS",           "Cloud & DevOps"},
            new String[]{"Azure",         "Cloud & DevOps"},
            new String[]{"GCP",           "Cloud & DevOps"},
            new String[]{"Docker",        "Cloud & DevOps"},
            new String[]{"Kubernetes",    "Cloud & DevOps"},
            new String[]{"GitHub Actions","Cloud & DevOps"},
            new String[]{"Jenkins",       "Cloud & DevOps"},
            new String[]{"Terraform",     "Cloud & DevOps"},
            new String[]{"Ansible",       "Cloud & DevOps"},
            new String[]{"Linux",         "Cloud & DevOps"},
            new String[]{"Nginx",         "Cloud & DevOps"},
            // Ferramentas & Conceitos
            new String[]{"Git",           "Ferramenta"},
            new String[]{"REST APIs",     "Ferramenta"},
            new String[]{"GraphQL",       "Ferramenta"},
            new String[]{"WebSocket",     "Ferramenta"},
            new String[]{"Microsserviços","Ferramenta"},
            new String[]{"TDD",           "Ferramenta"},
            new String[]{"Clean Code",    "Ferramenta"},
            new String[]{"SOLID",         "Ferramenta"},
            new String[]{"Design Patterns","Ferramenta"},
            new String[]{"Agile/Scrum",   "Ferramenta"},
            new String[]{"CI/CD",         "Ferramenta"},
            new String[]{"Kafka",         "Ferramenta"},
            new String[]{"RabbitMQ",      "Ferramenta"},
            new String[]{"gRPC",          "Ferramenta"}
    );

    @Override
    public void run(String... args) {
        seedUsers();
        seedTechSkills();
    }

    private void seedUsers() {
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

    private void seedTechSkills() {
        long existing = techSkillRepository.count();
        if (existing > 0) return; // já populado

        TECH_SKILLS.forEach(s -> techSkillRepository.save(
                TechSkill.builder().name(s[0]).category(s[1]).build()
        ));
        log.info("Catálogo de habilidades populado: {} skills", TECH_SKILLS.size());
    }
}
