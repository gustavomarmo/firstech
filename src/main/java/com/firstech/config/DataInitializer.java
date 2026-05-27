package com.firstech.config;

import com.firstech.model.*;
import com.firstech.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository      userRepository;
    private final TechSkillRepository techSkillRepository;
    private final JobRepository       jobRepository;
    private final PostRepository      postRepository;
    private final PasswordEncoder     passwordEncoder;

    // ── Catálogo de habilidades tecnológicas ──────────────────────────────
    private static final List<String[]> TECH_SKILLS = List.of(
            // Linguagens
            new String[]{"Java",          "Linguagem"},
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
            new String[]{"Git",            "Ferramenta"},
            new String[]{"REST APIs",      "Ferramenta"},
            new String[]{"GraphQL",        "Ferramenta"},
            new String[]{"WebSocket",      "Ferramenta"},
            new String[]{"Microsserviços", "Ferramenta"},
            new String[]{"TDD",            "Ferramenta"},
            new String[]{"Clean Code",     "Ferramenta"},
            new String[]{"SOLID",          "Ferramenta"},
            new String[]{"Design Patterns","Ferramenta"},
            new String[]{"Agile/Scrum",    "Ferramenta"},
            new String[]{"CI/CD",          "Ferramenta"},
            new String[]{"Kafka",          "Ferramenta"},
            new String[]{"RabbitMQ",       "Ferramenta"},
            new String[]{"gRPC",           "Ferramenta"}
    );

    @Override
    public void run(String... args) {
        seedTechSkills();
        User       admin       = seedAdmin();
        List<User> recruiters  = seedRecruiters();
        List<User> candidatos  = seedCandidatos();
        List<Job>  jobs        = seedJobs(recruiters);
        seedPosts(admin, candidatos, recruiters, jobs);
    }

    // ── Tech Skills ───────────────────────────────────────────────────────
    private void seedTechSkills() {
        TECH_SKILLS.forEach(s -> techSkillRepository.save(
                TechSkill.builder().name(s[0]).category(s[1]).build()
        ));
        log.info("Catálogo de habilidades populado: {} skills", TECH_SKILLS.size());
    }

    // ── Administrador ─────────────────────────────────────────────────────
    private User seedAdmin() {
        String pwd = passwordEncoder.encode("Admin@123");
        User admin = userRepository.save(User.builder()
                .name("Administrador")
                .email("admin@example.com")
                .password(pwd)
                .roles(Set.of(Role.ADMINISTRADOR))
                .tagline("Administrador da plataforma Firstech")
                .build());
        log.info("Admin criado: {}", admin.getEmail());
        return admin;
    }

    // ── Recrutadores ──────────────────────────────────────────────────────
    private List<User> seedRecruiters() {
        String pwd = passwordEncoder.encode("Admin@123");

        User amanda = userRepository.save(User.builder()
                .name("Amanda Ferreira")
                .email("amanda@techcorp.com")
                .password(pwd)
                .roles(Set.of(Role.RECRUTADOR))
                .company("TechCorp Brasil")
                .companySize("201-500")
                .website("https://techcorp.com.br")
                .tagline("Head of Talent Acquisition · TechCorp Brasil")
                .city("São Paulo, SP")
                .cultureTags(List.of("Diversidade", "Flexibilidade", "Crescimento"))
                .build());

        User lucas = userRepository.save(User.builder()
                .name("Lucas Mendonça")
                .email("lucas@inovax.io")
                .password(pwd)
                .roles(Set.of(Role.RECRUTADOR))
                .company("InovaX")
                .companySize("51-200")
                .website("https://inovax.io")
                .tagline("Talent Partner @ InovaX · Startups & Scale-ups")
                .city("Florianópolis, SC")
                .cultureTags(List.of("Startup", "Autonomia", "Impacto"))
                .build());

        log.info("Recrutadores criados: {}", 2);
        return List.of(amanda, lucas);
    }

    // ── Candidatos ────────────────────────────────────────────────────────
    private List<User> seedCandidatos() {
        String pwd = passwordEncoder.encode("Admin@123");

        User devCandidato = userRepository.save(User.builder()
                .name("Dev Candidato")
                .email("candidato@example.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("junior")
                .city("São Paulo, SP")
                .tagline("Desenvolvedor Java apaixonado por tecnologia")
                .technologies(List.of("Java", "Spring Boot", "MySQL"))
                .sobre("Desenvolvedor júnior com foco em backend Java. Apaixonado por boas práticas e Clean Code.")
                .build());

        User ana = userRepository.save(User.builder()
                .name("Ana Silva")
                .email("ana.silva@email.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("senior")
                .workModel("remote")
                .city("Belo Horizonte, MG")
                .tagline("Senior Backend Dev · Java · Spring · AWS · 7 anos de xp")
                .technologies(List.of("Java", "Spring Boot", "AWS", "Kubernetes", "PostgreSQL", "Kafka", "Docker"))
                .sobre("Desenvolvedora backend sênior com 7 anos de experiência em sistemas distribuídos e microsserviços. Apaixonada por arquitetura limpa e escalabilidade.")
                .build());

        User carlos = userRepository.save(User.builder()
                .name("Carlos Mendes")
                .email("carlos.mendes@email.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("pleno")
                .workModel("hybrid")
                .city("Curitiba, PR")
                .tagline("Full Stack Developer · React · Node.js · TypeScript")
                .technologies(List.of("React", "TypeScript", "Node.js", "NestJS", "PostgreSQL", "Docker"))
                .sobre("Desenvolvedor full stack com 4 anos de experiência. Especializado em aplicações React com backend em Node.js/NestJS.")
                .build());

        User beatriz = userRepository.save(User.builder()
                .name("Beatriz Santos")
                .email("beatriz.santos@email.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("studying")
                .city("Porto Alegre, RS")
                .tagline("Estudante de Ciência da Computação · Entusiasta de IA/ML")
                .technologies(List.of("Python", "TensorFlow", "Pandas", "Git", "JavaScript"))
                .sobre("Estudante de CC no 5º semestre, apaixonada por inteligência artificial e machine learning. Procuro estágio para aplicar conhecimentos em projetos reais.")
                .build());

        User rafael = userRepository.save(User.builder()
                .name("Rafael Oliveira")
                .email("rafael.oliveira@email.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("junior")
                .workModel("remote")
                .city("Recife, PE")
                .tagline("Mobile Developer · Flutter · Dart · Firebase")
                .technologies(List.of("Flutter", "Dart", "Firebase", "Git", "REST APIs"))
                .sobre("Desenvolvedor mobile júnior focado em Flutter. Já publiquei 2 apps na Play Store e busco oportunidades para crescer na área.")
                .build());

        User mariana = userRepository.save(User.builder()
                .name("Mariana Costa")
                .email("mariana.costa@email.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("estagio")
                .city("Rio de Janeiro, RJ")
                .tagline("Estagiária de Desenvolvimento · Vue.js · Python")
                .technologies(List.of("Vue.js", "Python", "Django", "MySQL", "Git"))
                .sobre("Cursando Sistemas de Informação, busco meu primeiro estágio. Tenho projetos pessoais com Vue.js e Django e muita vontade de aprender!")
                .build());

        User pedro = userRepository.save(User.builder()
                .name("Pedro Alves")
                .email("pedro.alves@email.com")
                .password(pwd)
                .roles(Set.of(Role.CANDIDATO))
                .careerMoment("pleno")
                .workModel("hybrid")
                .city("São Paulo, SP")
                .tagline("DevOps Engineer · Kubernetes · Terraform · AWS")
                .technologies(List.of("AWS", "Kubernetes", "Docker", "Terraform", "Linux", "Python", "CI/CD"))
                .sobre("Engenheiro DevOps com 3 anos de experiência em infraestrutura como código e automação de pipelines CI/CD. Certificado AWS Solutions Architect.")
                .build());

        log.info("Candidatos criados: {}", 7);
        return List.of(devCandidato, ana, carlos, beatriz, rafael, mariana, pedro);
    }

    // ── Vagas ─────────────────────────────────────────────────────────────
    private List<Job> seedJobs(List<User> recruiters) {
        User amanda = recruiters.get(0);
        User lucas  = recruiters.get(1);

        Job j1 = jobRepository.save(Job.builder()
                .title("Desenvolvedor Java Júnior")
                .description("Buscamos um dev Java apaixonado por tecnologia para integrar nosso time de backend. Você vai trabalhar com Spring Boot, APIs REST e banco de dados relacional em um ambiente ágil e colaborativo.\n\nResponsabilidades:\n• Desenvolvimento e manutenção de APIs REST com Spring Boot\n• Escrita de testes unitários e de integração (JUnit, Mockito)\n• Participação em code reviews e cerimônias Scrum\n• Colaboração com o time de frontend na definição de contratos de API\n\nRequisitos:\n• Java 17+\n• Spring Boot 3+\n• MySQL ou PostgreSQL\n• Git e familiaridade com GitHub Flow")
                .location("São Paulo, SP")
                .modality("Híbrido")
                .level("Júnior")
                .salary("R$ 4.000 – 6.000")
                .jobCompany("Firstech")
                .tags(List.of("Java", "Spring Boot", "MySQL", "REST APIs", "Git"))
                .recruiter(amanda)
                .build());

        Job j2 = jobRepository.save(Job.builder()
                .title("Frontend Developer React Pleno")
                .description("Estamos contratando um desenvolvedor React pleno para liderar o desenvolvimento das nossas interfaces de usuário. Você vai trabalhar com um design system proprietário e integrar com APIs REST.\n\nResponsabilidades:\n• Desenvolvimento de componentes reutilizáveis em React + TypeScript\n• Integração com APIs REST e GraphQL\n• Garantir performance e acessibilidade das interfaces\n• Mentoria de devs júniores\n\nRequisitos:\n• React 18+\n• TypeScript\n• Tailwind CSS ou CSS-in-JS\n• Experiência com testes (Jest, React Testing Library)")
                .location("São Paulo, SP")
                .modality("Remoto")
                .level("Pleno")
                .salary("R$ 8.000 – 12.000")
                .jobCompany("TechCorp Brasil")
                .tags(List.of("React", "TypeScript", "Next.js", "Tailwind CSS", "GraphQL"))
                .recruiter(amanda)
                .build());

        Job j3 = jobRepository.save(Job.builder()
                .title("DevOps Engineer")
                .description("Procuramos um engenheiro DevOps para modernizar nossa infraestrutura e acelerar o ciclo de entrega. Você vai trabalhar com Kubernetes, Terraform e pipelines CI/CD num ambiente cloud-first.\n\nResponsabilidades:\n• Gerenciar clusters Kubernetes em produção (EKS)\n• Criar e manter pipelines CI/CD com GitHub Actions\n• Infraestrutura como código com Terraform\n• Monitoramento com Datadog e alertas proativos\n\nRequisitos:\n• AWS (preferencialmente certificado)\n• Kubernetes e Helm\n• Terraform\n• Docker")
                .location("Florianópolis, SC")
                .modality("Híbrido")
                .level("Pleno")
                .salary("R$ 10.000 – 15.000")
                .jobCompany("InovaX")
                .tags(List.of("AWS", "Kubernetes", "Terraform", "Docker", "CI/CD", "Linux"))
                .recruiter(lucas)
                .build());

        Job j4 = jobRepository.save(Job.builder()
                .title("Java Backend Sênior — Microsserviços")
                .description("Vaga para desenvolvedor Java sênior especializado em arquitetura de microsserviços. Você vai liderar decisões técnicas e trabalhar com sistema de alta disponibilidade.\n\nResponsabilidades:\n• Design e implementação de microsserviços com Spring Boot e Quarkus\n• Integração via Kafka e RabbitMQ\n• Revisão de código e definição de boas práticas de engenharia\n• Entrevistas técnicas e onboarding de novos desenvolvedores\n\nRequisitos:\n• Java 17+ (5+ anos de experiência)\n• Microsserviços e arquitetura orientada a eventos\n• Kafka ou RabbitMQ\n• Kubernetes e Docker")
                .location("Remoto")
                .modality("Remoto")
                .level("Pleno")
                .salary("R$ 15.000 – 22.000")
                .jobCompany("TechCorp Brasil")
                .tags(List.of("Java", "Spring Boot", "Kafka", "Kubernetes", "Microsserviços", "PostgreSQL"))
                .recruiter(amanda)
                .build());

        Job j5 = jobRepository.save(Job.builder()
                .title("Data Scientist — ML & IA")
                .description("Buscamos um cientista de dados para desenvolver modelos de machine learning que vão impactar produtos usados por milhões de usuários.\n\nResponsabilidades:\n• Desenvolvimento e treinamento de modelos de ML (classificação, regressão, NLP)\n• Feature engineering e análise exploratória de dados\n• Deploy de modelos em produção com MLflow e AWS SageMaker\n• Comunicação de resultados para stakeholders não técnicos\n\nRequisitos:\n• Python (scikit-learn, PyTorch ou TensorFlow)\n• SQL avançado\n• Estatística e probabilidade\n• Experiência com dados em produção")
                .location("São Paulo, SP")
                .modality("Híbrido")
                .level("Pleno")
                .salary("R$ 12.000 – 18.000")
                .jobCompany("InovaX")
                .tags(List.of("Python", "Machine Learning", "TensorFlow", "AWS", "SQL", "Statistics"))
                .recruiter(lucas)
                .build());

        Job j6 = jobRepository.save(Job.builder()
                .title("Estágio em Desenvolvimento Web")
                .description("Oportunidade de estágio para estudantes que querem dar os primeiros passos na área. Você vai trabalhar em projetos reais, com mentoria de desenvolvedores seniores e muito aprendizado.\n\nO que você vai fazer:\n• Desenvolvimento de funcionalidades com Vue.js e Django\n• Participação nas cerimônias da equipe (daily, planning, retrospectiva)\n• Code review com apoio de mentor sênior\n\nRequisitos:\n• Cursando graduação em TI, Ciência da Computação ou afins\n• Conhecimentos básicos de HTML, CSS e JavaScript\n• Vontade de aprender!")
                .location("Belo Horizonte, MG")
                .modality("Presencial")
                .level("Estágio")
                .salary("R$ 1.500 + benefícios")
                .jobCompany("Firstech")
                .tags(List.of("Vue.js", "Django", "Python", "JavaScript", "Git"))
                .recruiter(amanda)
                .build());

        Job j7 = jobRepository.save(Job.builder()
                .title("Mobile Developer Flutter Júnior")
                .description("Estamos expandindo nosso time mobile e buscamos um desenvolvedor Flutter para construir experiências incríveis para iOS e Android.\n\nResponsabilidades:\n• Desenvolvimento de features nos apps iOS e Android com Flutter/Dart\n• Integração com APIs REST e Firebase\n• Escrita de testes unitários e de widget\n• Publicação de releases na App Store e Play Store\n\nRequisitos:\n• Flutter e Dart\n• Firebase\n• Git\n• Conhecimento básico de UX/UI")
                .location("Curitiba, PR")
                .modality("Híbrido")
                .level("Júnior")
                .salary("R$ 4.500 – 7.000")
                .jobCompany("InovaX")
                .tags(List.of("Flutter", "Dart", "Firebase", "REST APIs", "Git"))
                .recruiter(lucas)
                .build());

        Job j8 = jobRepository.save(Job.builder()
                .title("Full Stack Developer Node.js + React")
                .description("Precisamos de um desenvolvedor full stack para trabalhar em nosso produto SaaS B2B, do backend ao frontend.\n\nResponsabilidades:\n• Desenvolvimento de APIs com NestJS (Node.js + TypeScript)\n• Frontend com React 18 e TanStack Query\n• Banco de dados PostgreSQL com Prisma ORM\n• Deploy e monitoramento no AWS (ECS + RDS)\n\nRequisitos:\n• Node.js com TypeScript\n• React 18\n• SQL e experiência com ORM\n• Docker")
                .location("Remoto")
                .modality("Remoto")
                .level("Pleno")
                .salary("R$ 9.000 – 13.000")
                .jobCompany("TechCorp Brasil")
                .tags(List.of("Node.js", "TypeScript", "React", "PostgreSQL", "Docker", "AWS"))
                .recruiter(amanda)
                .build());

        log.info("Vagas criadas: {}", 8);
        return List.of(j1, j2, j3, j4, j5, j6, j7, j8);
    }

    // ── Posts ─────────────────────────────────────────────────────────────
    private void seedPosts(User admin, List<User> candidatos, List<User> recruiters, List<Job> jobs) {
        // candidatos: [devCandidato, ana, carlos, beatriz, rafael, mariana, pedro]
        User devCandidato = candidatos.get(0);
        User ana          = candidatos.get(1);
        User carlos       = candidatos.get(2);
        User beatriz      = candidatos.get(3);
        User rafael       = candidatos.get(4);
        User mariana      = candidatos.get(5);
        User pedro        = candidatos.get(6);
        // recruiters: [amanda, lucas]
        User amanda = recruiters.get(0);
        User lucas  = recruiters.get(1);

        // vagas para linkar em posts de recrutadores
        Job vagaJava       = jobs.get(0);
        Job vagaReact      = jobs.get(1);
        Job vagaDevOps     = jobs.get(2);
        Job vagaEstágio    = jobs.get(5);
        Job vagaFlutter    = jobs.get(6);

        // ── posts de candidatos ────────────────────────────────────────
        save(Post.builder()
                .author(ana)
                .content("Hoje completei 7 anos trabalhando com Java e ainda me surpreendo com o quanto tem pra aprender. " +
                         "Recentemente mergulhei fundo em Virtual Threads (Project Loom) no Java 21 e os ganhos de " +
                         "throughput em aplicações IO-bound foram impressionantes.\n\n" +
                         "Se você ainda não experimentou, vale muito a pena! A API é compatível com código existente " +
                         "e a migração costuma ser simples.")
                .tags(List.of("Java", "Java21", "ProjectLoom", "Backend"))
                .likedByUserIds(setOf(carlos.getId(), pedro.getId(), rafael.getId(), admin.getId()))
                .build());

        save(Post.builder()
                .author(carlos)
                .content("Acabei de terminar uma migração completa de Create React App para Vite + React 18. " +
                         "Resultado: build de 4 minutos → 18 segundos. Hot reload quase instantâneo.\n\n" +
                         "Se você ainda está sofrendo com CRA em 2025, é hora de migrar. Feliz em ajudar com " +
                         "dúvidas sobre o processo nos comentários!")
                .tags(List.of("React", "Vite", "Frontend", "Performance"))
                .likedByUserIds(setOf(ana.getId(), beatriz.getId(), mariana.getId(), amanda.getId()))
                .build());

        save(Post.builder()
                .author(pedro)
                .content("Dica de DevOps: se você ainda está fazendo deploy manual, considere GitOps com ArgoCD + Kubernetes. " +
                         "A mudança de mindset é grande, mas depois que o pipeline está configurado, o time inteiro " +
                         "ganha autonomia para fazer deploys seguros sem precisar de acesso direto ao cluster.\n\n" +
                         "Na última empresa que trabalhei, reduzimos o tempo de deploy de 40 minutos para 3 minutos " +
                         "com essa abordagem. 🚀")
                .tags(List.of("DevOps", "Kubernetes", "GitOps", "ArgoCD", "CI/CD"))
                .likedByUserIds(setOf(ana.getId(), carlos.getId(), lucas.getId()))
                .build());

        save(Post.builder()
                .author(beatriz)
                .content("Compartilhando meu TCC! Desenvolvi um modelo de detecção de emoções em texto usando " +
                         "BERT fine-tuned em português, atingindo 87% de F1-score no conjunto de teste.\n\n" +
                         "Foram 6 meses de pesquisa, leituras de papers, experimentos que não funcionaram " +
                         "e muita xícara de café. Mas valeu cada segundo. ☕\n\n" +
                         "O código está aberto no GitHub — link na bio!")
                .tags(List.of("MachineLearning", "NLP", "Python", "BERT", "IA"))
                .likedByUserIds(setOf(ana.getId(), carlos.getId(), rafael.getId(), lucas.getId(), amanda.getId()))
                .build());

        save(Post.builder()
                .author(rafael)
                .content("Acabei de publicar meu segundo app Flutter na Play Store! 🎉\n\n" +
                         "O app é um rastreador de hábitos offline-first, com sync em background via Firebase. " +
                         "Aprendi muito sobre state management com Riverpod e testes de widget.\n\n" +
                         "Para quem está começando em Flutter: o ecossistema é incrível, a curva de aprendizado " +
                         "inicial é íngreme, mas compensa muito.")
                .tags(List.of("Flutter", "Dart", "Firebase", "Mobile", "PlayStore"))
                .likedByUserIds(setOf(mariana.getId(), beatriz.getId(), carlos.getId()))
                .build());

        save(Post.builder()
                .author(mariana)
                .content("Primeira contribuição para um projeto open source aceita! 🟢\n\n" +
                         "Era uma correção simples de bug no Django, mas o processo de abrir a issue, " +
                         "discutir com os maintainers, fazer o PR e ver sendo mergeado foi incrível.\n\n" +
                         "Para quem está começando: não existe contribuição pequena. Comece pelos " +
                         "\"good first issues\" e vá subindo a dificuldade.")
                .tags(List.of("OpenSource", "Django", "Python", "GitHub"))
                .likedByUserIds(setOf(beatriz.getId(), carlos.getId(), rafael.getId(), ana.getId()))
                .build());

        save(Post.builder()
                .author(devCandidato)
                .content("Estudando para a certificação AWS Cloud Practitioner. Alguma dica de quem já passou?\n\n" +
                         "Estou usando o curso da Udemy do Stephane Maarek + os simulados do Whizlabs. " +
                         "Planejando fazer em 3 semanas.")
                .tags(List.of("AWS", "Certificação", "Cloud", "Estudos"))
                .likedByUserIds(setOf(pedro.getId(), ana.getId()))
                .build());

        save(Post.builder()
                .author(ana)
                .content("Thread sobre Kafka para desenvolvedores backend: 🧵\n\n" +
                         "1/ Kafka não é uma fila de mensagens — é um log distribuído. Essa distinção importa na hora de desenhar sua arquitetura.\n\n" +
                         "2/ Consumers leem de offsets, não deletam mensagens. Isso permite replay de eventos históricos.\n\n" +
                         "3/ Partições são a unidade de paralelismo. Mais partições = mais consumidores simultâneos.\n\n" +
                         "4/ Mas cuidado: redistribuir partições em produção é doloroso. Planejar antes é mais barato.")
                .tags(List.of("Kafka", "Java", "EventDriven", "Arquitetura", "Backend"))
                .likedByUserIds(setOf(carlos.getId(), pedro.getId(), lucas.getId(), admin.getId(), devCandidato.getId()))
                .build());

        save(Post.builder()
                .author(carlos)
                .content("Alguém mais está usando Zustand no lugar do Redux? Mudei em dois projetos e " +
                         "a redução de boilerplate é absurda. API minimalista, funciona com TypeScript " +
                         "sem configuração extra e o bundle é microscópico.\n\n" +
                         "Redux ainda faz sentido para apps muito grandes com time grande, mas para " +
                         "projetos médios o Zustand está me deixando muito mais produtivo.")
                .tags(List.of("React", "Zustand", "Redux", "StateManagement", "TypeScript"))
                .likedByUserIds(setOf(mariana.getId(), beatriz.getId(), ana.getId()))
                .build());

        save(Post.builder()
                .author(pedro)
                .content("Errei feio num projeto e vou compartilhar para não acontecer com vocês:\n\n" +
                         "Coloquei um Kubernetes cluster de produção sem limite de recursos nos containers. " +
                         "Um único serviço com memory leak consumiu a memória de 3 nodes, derrubando " +
                         "outros serviços críticos às 2h da manhã.\n\n" +
                         "Lição: SEMPRE configure requests e limits. SEMPRE. Sem exceção.")
                .tags(List.of("Kubernetes", "DevOps", "SRE", "PostMortem", "Lessons"))
                .likedByUserIds(setOf(ana.getId(), lucas.getId(), admin.getId(), carlos.getId()))
                .build());

        save(Post.builder()
                .author(beatriz)
                .content("Recurso do Python que mudou minha vida em análise de dados: dataclasses + " +
                         "__slots__.\n\n" +
                         "Antes eu usava dicts para tudo e meu código ficava difícil de manter. " +
                         "Dataclasses trazem type hints, repr automático e __eq__ de graça. " +
                         "Com __slots__, você economiza memória significativa quando tem muitos objetos.\n\n" +
                         "Pequena mudança, grande impacto em legibilidade.")
                .tags(List.of("Python", "DataScience", "CleanCode", "Dicas"))
                .likedByUserIds(setOf(mariana.getId(), carlos.getId(), rafael.getId()))
                .build());

        // ── posts de recrutadores (vagas) ─────────────────────────────
        save(Post.builder()
                .author(amanda)
                .content("🚀 Firstech tem vaga aberta para Desenvolvedor Java Júnior!\n\n" +
                         "Buscamos alguém com conhecimento em Spring Boot, que goste de trabalhar em equipe " +
                         "e queira crescer rápido em um ambiente ágil.\n\n" +
                         "Modelo híbrido em São Paulo, salário competitivo. Candidate-se abaixo!")
                .tags(List.of("Vaga", "Java", "SpringBoot", "Júnior", "SãoPaulo"))
                .linkedJob(vagaJava)
                .likedByUserIds(setOf(devCandidato.getId(), mariana.getId(), beatriz.getId()))
                .build());

        save(Post.builder()
                .author(amanda)
                .content("💼 TechCorp Brasil está contratando React Developer Pleno!\n\n" +
                         "Vaga 100% remota, time incrível, produto usado por +500k usuários. " +
                         "Buscamos alguém que domine React + TypeScript e queira fazer parte de uma " +
                         "empresa em forte crescimento.\n\n" +
                         "Link para candidatura no botão da vaga. Indicações também são bem-vindas!")
                .tags(List.of("Vaga", "React", "TypeScript", "Remoto", "Frontend"))
                .linkedJob(vagaReact)
                .likedByUserIds(setOf(carlos.getId(), mariana.getId(), beatriz.getId(), ana.getId()))
                .build());

        save(Post.builder()
                .author(lucas)
                .content("⚙️ InovaX está buscando um DevOps Engineer!\n\n" +
                         "Se você respira Kubernetes, ama automatizar tudo e quer trabalhar em uma " +
                         "startup que cresce 15% ao mês, vem conversar com a gente.\n\n" +
                         "Pacote de compensação inclui salário acima do mercado + equity + home office flexível.")
                .tags(List.of("Vaga", "DevOps", "Kubernetes", "AWS", "Startup"))
                .linkedJob(vagaDevOps)
                .likedByUserIds(setOf(pedro.getId(), ana.getId(), admin.getId()))
                .build());

        save(Post.builder()
                .author(amanda)
                .content("📣 Vaga de estágio aberta na Firstech!\n\n" +
                         "Para estudantes que querem dar seus primeiros passos no desenvolvimento web com " +
                         "Vue.js e Django. Terá mentor dedicado, projetos reais e ambiente de aprendizado.\n\n" +
                         "Estágio presencial em Belo Horizonte, BH.")
                .tags(List.of("Estágio", "VueJS", "Python", "Django", "BH"))
                .linkedJob(vagaEstágio)
                .likedByUserIds(setOf(mariana.getId(), beatriz.getId()))
                .build());

        save(Post.builder()
                .author(lucas)
                .content("📱 InovaX abre vaga para Mobile Developer Flutter Júnior!\n\n" +
                         "Quer construir apps que chegam a milhares de usuários desde o início da carreira? " +
                         "Nosso time mobile está crescendo e queremos alguém com energia e vontade de aprender.\n\n" +
                         "Vaga híbrida em Curitiba. Candidate-se!")
                .tags(List.of("Vaga", "Flutter", "Mobile", "Júnior", "Curitiba"))
                .linkedJob(vagaFlutter)
                .likedByUserIds(setOf(rafael.getId(), mariana.getId()))
                .build());

        log.info("Posts criados: {}", 16);
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Post save(Post post) {
        // Garante que createdAt está preenchido (PrePersist pode não ser chamado via builder)
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(LocalDateTime.now());
        }
        return postRepository.save(post);
    }

    @SafeVarargs
    private static <T> java.util.Set<T> setOf(T... items) {
        java.util.Set<T> set = new java.util.HashSet<>();
        for (T item : items) set.add(item);
        return set;
    }
}
