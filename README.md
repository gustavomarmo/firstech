# &lt;first&gt;tech

Plataforma de conexão entre talentos e recrutadores do ecossistema tecnológico brasileiro.  
Candidatos constroem seu portfólio técnico, recrutadores publicam vagas e toda a comunidade interage por um feed social — tudo em uma única aplicação.

---

## Sumário

- [Visão Geral](#visão-geral)
- [Stack Tecnológica](#stack-tecnológica)
- [Funcionalidades](#funcionalidades)
- [Arquitetura](#arquitetura)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Pré-requisitos](#pré-requisitos)
- [Como Executar](#como-executar)
- [Dados Iniciais (Seed)](#dados-iniciais-seed)
- [Endpoints da API](#endpoints-da-api)
- [Páginas (Thymeleaf)](#páginas-thymeleaf)
- [Segurança e Autenticação](#segurança-e-autenticação)
- [Regras de Negócio](#regras-de-negócio)
- [Testes](#testes)
- [Variáveis de Configuração](#variáveis-de-configuração)

---

## Visão Geral

A Firstech é uma rede profissional focada em tecnologia que oferece:

- **Portfólio técnico** para candidatos — experiências, habilidades, certificações e projetos
- **Publicação de vagas** para recrutadores, com link direto para candidatura
- **Feed social** com posts, curtidas e comentários
- **Rede de conexões** entre profissionais
- **Mensagens diretas** entre usuários
- **Painel administrativo** completo para gestão da plataforma

---

## Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2.5 |
| Segurança | Spring Security 6 + JWT (jjwt 0.12.3) |
| Templates | Thymeleaf 3 + thymeleaf-extras-springsecurity6 |
| Estilo | Tailwind CSS (via CDN) |
| Persistência | Spring Data JPA + Hibernate |
| Banco de dados | H2 (in-memory) |
| Validação | Jakarta Bean Validation |
| E-mail | Spring Mail (SMTP Gmail) |
| Documentação | SpringDoc OpenAPI 2.5 (Swagger UI) |
| Utilitários | Lombok 1.18.32 |
| Build | Maven |
| Testes | JUnit 5 + Mockito + Spring Security Test |

---

## Funcionalidades

### Candidato
- Cadastro e login com escolha de perfil
- Onboarding guiado (modelo de trabalho, momento de carreira, tecnologias)
- Portfólio completo: foto, banner, headline, seção "Sobre", experiências, habilidades, certificações e projetos
- Upload de avatar e banner em base64 (limite de 2 MB por imagem)
- Feed de posts: criar, curtir, comentar e publicar projetos pessoais
- Visualizar e se candidatar a vagas ativas
- Enviar/aceitar/recusar conexões
- Mensagens diretas com outros usuários
- Busca de usuários e vagas

### Recrutador
- Cadastro com perfil de empresa (nome, porte, website, tags de cultura)
- Publicar, editar e encerrar vagas com link externo de candidatura
- Feed de posts vinculados a vagas
- Visualizar perfis públicos de candidatos
- Conexões e mensagens diretas

### Administrador
- Dashboard com totais de usuários, vagas e posts
- Gestão completa de usuários: editar, ativar/desativar, excluir
- Gestão de vagas: listar e ativar/desativar
- Moderação de posts: listar e excluir

---

## Arquitetura

```
Requisição HTTP
      │
      ▼
JwtAuthenticationFilterUtil   ← extrai token do header Authorization ou cookie "jwt"
      │
      ▼
Spring Security Filter Chain  ← verifica permissões por URL e método
      │
      ▼
Controller (REST ou Thymeleaf)
      │
      ▼
Service (lógica de negócio)
      │
      ▼
Repository (Spring Data JPA)
      │
      ▼
H2 Database (in-memory)
```

### Autenticação

O sistema usa **autenticação stateless com JWT**:

1. O cliente faz `POST /api/auth/login` e recebe `accessToken` + `refreshToken`
2. O `accessToken` é armazenado como cookie `HttpOnly` (navegação) e também retornado no corpo (APIs/mobile)
3. Cada requisição passa pelo `JwtAuthenticationFilterUtil` que valida o token e popula o `SecurityContext`
4. Após 15 minutos o access token expira; o cliente usa o refresh token (7 dias) para renová-lo via `POST /api/auth/refresh`

---

## Estrutura do Projeto

```
src/
├── main/
│   ├── java/com/firstech/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java          # Filter chain, CORS, permissões por URL
│   │   │   └── DataInitializer.java         # Seed de dados (admin, recrutadores, candidatos, vagas, posts)
│   │   ├── controller/
│   │   │   ├── AuthController.java          # /api/auth/**
│   │   │   ├── JobController.java           # /api/jobs/**
│   │   │   ├── PostController.java          # /api/posts/**
│   │   │   ├── PortfolioController.java     # /api/portfolio/**
│   │   │   ├── ProfileController.java       # /api/profile/**
│   │   │   ├── ConnectionController.java    # /api/connections/**
│   │   │   ├── MessageController.java       # /api/messages/**
│   │   │   ├── SearchController.java        # /api/search/**
│   │   │   ├── UserProfileController.java   # /api/users/**
│   │   │   ├── AdminController.java         # /admin/** (Thymeleaf)
│   │   │   ├── AdminUserApiController.java  # /api/admin/users/**
│   │   │   ├── AdminJobApiController.java   # /api/admin/jobs/**
│   │   │   ├── AdminPostApiController.java  # /api/admin/posts/**
│   │   │   └── ViewController.java          # Páginas Thymeleaf (/, /dashboard, /profile/*)
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── JobService.java
│   │   │   ├── PostService.java
│   │   │   ├── PortfolioService.java
│   │   │   ├── ProfileService.java
│   │   │   ├── ConnectionService.java
│   │   │   ├── MessageService.java
│   │   │   ├── SearchService.java
│   │   │   ├── AdminService.java
│   │   │   └── EmailService.java
│   │   ├── model/
│   │   │   ├── User.java                    # Entidade principal (implements UserDetails)
│   │   │   ├── Role.java                    # Enum: CANDIDATO, RECRUTADOR, ADMINISTRADOR
│   │   │   ├── Job.java                     # Vaga de emprego
│   │   │   ├── Post.java                    # Post do feed
│   │   │   ├── PostComment.java             # Comentário de post
│   │   │   ├── Connection.java              # Conexão entre usuários
│   │   │   ├── Message.java                 # Mensagem direta
│   │   │   ├── Experience.java              # Experiência profissional
│   │   │   ├── Skill.java                   # Habilidade do portfólio
│   │   │   ├── Certification.java           # Certificação
│   │   │   ├── Project.java                 # Projeto pessoal
│   │   │   ├── TechSkill.java               # Catálogo de tecnologias
│   │   │   ├── RefreshToken.java
│   │   │   ├── PasswordResetToken.java
│   │   │   ├── JobStatus.java               # Enum: ATIVA, ENCERRADA
│   │   │   └── ConnectionStatus.java        # Enum: PENDING, ACCEPTED
│   │   ├── dto/                             # Records de entrada e saída das APIs
│   │   ├── repository/                      # Interfaces Spring Data JPA
│   │   ├── exception/                       # Exceções customizadas + GlobalExceptionHandler
│   │   ├── imp/
│   │   │   └── UserDetailsServiceImp.java   # Carrega usuário por e-mail para o Spring Security
│   │   └── util/
│   │       ├── JwtUtil.java                 # Geração e validação de tokens
│   │       └── JwtAuthenticationFilterUtil.java
│   └── resources/
│       ├── application.properties
│       └── templates/
│           ├── auth/                        # Login, cadastro, recuperação de senha
│           ├── main/                        # Layout, dashboard, fragmentos (feed, vagas, mensagens, portfólio)
│           ├── admin/                       # Painel administrativo
│           └── profile/                     # Página de perfil público
└── test/
    └── java/com/firstech/
        ├── controller/                      # 14 testes @WebMvcTest
        ├── service/                         # 10 testes @ExtendWith(MockitoExtension)
        └── util/                            # Testes JwtUtil
```

---

## Pré-requisitos

- **Java 17+** instalado e configurado no `PATH`
- **Maven 3.8+** (ou usar o wrapper `./mvnw`)
- Conta Gmail com [Senha de App](https://myaccount.google.com/apppasswords) habilitada (para envio de e-mails)

---

## Como Executar

```bash
# 1. Clone o repositório
git clone <url-do-repositorio>
cd firstech

# 2. Configure o e-mail em src/main/resources/application.properties
#    (veja a seção Variáveis de Configuração)

# 3. Execute a aplicação
./mvnw spring-boot:run
# ou
mvn spring-boot:run
```

A aplicação sobe na porta **8081**:

| URL | Descrição |
|---|---|
| http://localhost:8081 | Página de login |
| http://localhost:8081/dashboard | Dashboard principal |
| http://localhost:8081/admin/dashboard | Painel administrativo |
| http://localhost:8081/h2-console | Console do banco H2 |
| http://localhost:8081/swagger-ui.html | Documentação interativa da API |

> **Banco de dados**: H2 in-memory — os dados são recriados a cada reinicialização com o seed automático.

---

## Dados Iniciais (Seed)

Ao iniciar, a aplicação popula automaticamente o banco com:

### Usuários de teste

| Papel | E-mail | Senha |
|---|---|---|
| Administrador | `admin@example.com` | `Admin@123` |
| Recrutadora | `amanda@techcorp.com` | `Admin@123` |
| Recrutador | `lucas@inovax.io` | `Admin@123` |
| Candidato | `candidato@example.com` | `Admin@123` |
| Candidata (Sênior) | `ana.silva@email.com` | `Admin@123` |
| Candidato (Full Stack) | `carlos.mendes@email.com` | `Admin@123` |
| Candidata (Estudante/IA) | `beatriz.santos@email.com` | `Admin@123` |
| Candidato (Mobile) | `rafael.oliveira@email.com` | `Admin@123` |
| Candidata (Estágio) | `mariana.costa@email.com` | `Admin@123` |
| Candidato (DevOps) | `pedro.alves@email.com` | `Admin@123` |

### Conteúdo criado

- **8 vagas** em diversas stacks (Java, React, DevOps, Flutter, Data Science, Full Stack, Estágio)
- **16 posts** de candidatos e recrutadores com curtidas
- **Catálogo de ~80 tecnologias** organizadas por categoria (linguagens, frameworks, bancos de dados, cloud, etc.)

---

## Endpoints da API

### Autenticação — `/api/auth`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| POST | `/api/auth/register` | Público | Cadastrar novo usuário |
| POST | `/api/auth/login` | Público | Login — retorna access + refresh token |
| POST | `/api/auth/logout` | Autenticado | Invalida tokens |
| POST | `/api/auth/refresh` | Público | Renova o access token |
| POST | `/api/auth/forgot-password` | Público | Solicita redefinição de senha por e-mail |
| POST | `/api/auth/reset-password` | Público | Redefine a senha via token |
| GET | `/api/auth/me` | Autenticado | Retorna dados do usuário logado |

### Vagas — `/api/jobs`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/jobs` | Público | Listar vagas ativas |
| POST | `/api/jobs` | RECRUTADOR | Publicar nova vaga |
| PUT | `/api/jobs/{id}` | RECRUTADOR (dono) | Editar vaga |
| PATCH | `/api/jobs/{id}/fechar` | RECRUTADOR (dono) | Encerrar vaga |
| DELETE | `/api/jobs/{id}` | RECRUTADOR (dono) | Excluir vaga |

### Posts — `/api/posts`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/posts` | Público | Listar posts do feed |
| POST | `/api/posts` | Autenticado | Criar post |
| DELETE | `/api/posts/{id}` | Autenticado (autor) | Excluir post |
| POST | `/api/posts/{id}/like` | Autenticado | Curtir / descurtir (toggle) |
| POST | `/api/posts/{id}/comment` | Autenticado | Comentar post |
| GET | `/api/posts/{id}/comments` | Autenticado | Listar comentários |

### Portfólio — `/api/portfolio`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/portfolio/skills/catalog` | Autenticado | Catálogo de tecnologias |
| PUT | `/api/portfolio/profile` | Autenticado | Atualizar headline, cidade, sobre, tecnologias |
| POST | `/api/portfolio/avatar` | Autenticado | Upload de foto de perfil (multipart, max 2 MB) |
| POST | `/api/portfolio/banner` | Autenticado | Upload de banner (multipart, max 2 MB) |
| POST | `/api/portfolio/experience` | Autenticado | Adicionar experiência |
| PUT | `/api/portfolio/experience/{id}` | Autenticado (dono) | Editar experiência |
| DELETE | `/api/portfolio/experience/{id}` | Autenticado (dono) | Excluir experiência |
| POST | `/api/portfolio/skill` | Autenticado | Adicionar habilidade |
| DELETE | `/api/portfolio/skill/{id}` | Autenticado (dono) | Excluir habilidade |
| POST | `/api/portfolio/certification` | Autenticado | Adicionar certificação |
| PUT | `/api/portfolio/certification/{id}` | Autenticado (dono) | Editar certificação |
| DELETE | `/api/portfolio/certification/{id}` | Autenticado (dono) | Excluir certificação |
| POST | `/api/portfolio/project` | Autenticado | Adicionar projeto |
| PUT | `/api/portfolio/project/{id}` | Autenticado (dono) | Editar projeto |
| DELETE | `/api/portfolio/project/{id}` | Autenticado (dono) | Excluir projeto |

### Perfil Público — `/api/users`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/users/{id}/profile` | Público | Retorna perfil completo com status de conexão |

### Conexões — `/api/connections`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/connections/my` | Autenticado | Listar conexões aceitas |
| GET | `/api/connections/pending` | Autenticado | Listar solicitações pendentes |
| POST | `/api/connections/request/{id}` | Autenticado | Enviar solicitação de conexão |
| POST | `/api/connections/accept/{id}` | Autenticado | Aceitar solicitação |
| POST | `/api/connections/reject/{id}` | Autenticado | Recusar solicitação |

### Mensagens — `/api/messages`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/messages/unread-count` | Autenticado | Contagem de mensagens não lidas |
| GET | `/api/messages/conversations` | Autenticado | Listar conversas |
| GET | `/api/messages/{userId}/history` | Autenticado | Histórico com um usuário |
| POST | `/api/messages/send` | Autenticado | Enviar mensagem |
| POST | `/api/messages/{userId}/mark-read` | Autenticado | Marcar conversa como lida |

### Busca — `/api/search`

| Método | Endpoint | Auth | Descrição |
|---|---|---|---|
| GET | `/api/search?q={termo}` | Autenticado | Busca global (vagas + usuários) |
| GET | `/api/search/users?q={termo}` | Autenticado | Busca somente usuários |

### Admin — `/api/admin`

> Todos os endpoints abaixo exigem a role **ADMINISTRADOR**.

| Método | Endpoint | Descrição |
|---|---|---|
| GET | `/api/admin/users` | Listar todos os usuários |
| PUT | `/api/admin/users/{id}` | Editar nome, e-mail ou role |
| PATCH | `/api/admin/users/{id}/toggle-status` | Ativar / desativar usuário |
| DELETE | `/api/admin/users/{id}` | Excluir usuário permanentemente |
| GET | `/api/admin/jobs` | Listar todas as vagas |
| PATCH | `/api/admin/jobs/{id}/toggle-status` | Ativar / encerrar vaga |
| GET | `/api/admin/posts` | Listar todos os posts |
| DELETE | `/api/admin/posts/{id}` | Excluir post |

---

## Páginas (Thymeleaf)

| Rota | Acesso | Descrição |
|---|---|---|
| `GET /` | Público | Redireciona para `/login` |
| `GET /login` | Público | Página de login |
| `GET /register` | Público | Página de cadastro |
| `GET /forgot-password` | Público | Solicitar recuperação de senha |
| `GET /reset-password?token=...` | Público | Redefinir senha |
| `GET /dashboard` | Autenticado | Dashboard principal (feed, vagas, portfólio, mensagens) |
| `GET /profile/{id}` | Autenticado | Perfil público de outro usuário |
| `GET /admin/dashboard` | ADMINISTRADOR | Painel com estatísticas |
| `GET /admin/users` | ADMINISTRADOR | Gestão de usuários |
| `GET /admin/jobs` | ADMINISTRADOR | Gestão de vagas |
| `GET /admin/posts` | ADMINISTRADOR | Moderação de posts |

> O dashboard é uma SPA-like com telas trocadas via JavaScript, sem recarregar a página — incluindo as abas: **Home**, **Vagas**, **Busca**, **Mensagens** e **Portfólio**.

---

## Segurança e Autenticação

### Controle de acesso por URL

```
/api/auth/**         → permitAll()
/api/jobs            → permitAll()  (somente GET)
/api/posts           → permitAll()  (somente GET)
/api/users/*/profile → permitAll()
/admin/**            → hasAuthority("ADMINISTRADOR")
/api/admin/**        → hasAuthority("ADMINISTRADOR")
(demais)             → authenticated()
```

### Proteções implementadas

- **Senhas** armazenadas com BCrypt (strength 12)
- **CSRF** desabilitado (sessão stateless)
- **CORS** configurado para `http://localhost:3000`
- **Cookies JWT** com flag `HttpOnly` (não acessível via JavaScript)
- **Refresh tokens** salvos no banco — invalidados no logout
- **Tokens de reset de senha** com expiração de 1 hora
- **Usuário desativado** (`enabled = false`) não consegue autenticar

### Fluxo de tokens

```
┌──────────────┐  POST /api/auth/login  ┌─────────────────┐
│    Cliente   │ ─────────────────────► │    Servidor     │
│              │ ◄───────────────────── │                 │
│              │  accessToken (15 min)  │  Salva refresh  │
│              │  refreshToken (7 dias) │  token no banco │
└──────────────┘                        └─────────────────┘
       │
       │  (após 15 min, access token expira)
       │
       ▼
POST /api/auth/refresh  →  novo accessToken
```

---

## Regras de Negócio

### Usuários e Perfis
- Cada e-mail só pode ser cadastrado **uma vez**
- O role padrão no cadastro é **CANDIDATO**; pode-se escolher **RECRUTADOR** durante o registro
- A role **ADMINISTRADOR** só pode ser atribuída via painel admin
- Um usuário desativado **não consegue fazer login**
- O administrador **não pode desativar ou excluir a própria conta**
- Perfis públicos são acessíveis por qualquer usuário autenticado

### Vagas
- Apenas **RECRUTADOR** pode publicar vagas
- Apenas o **recrutador dono** da vaga pode editá-la, encerrá-la ou excluí-la
- Vagas com status `ENCERRADA` não aparecem na listagem pública
- O campo `jobCompany` da vaga sobrescreve o nome da empresa do recrutador na exibição
- Salários no formato `"3000-5000"` são automaticamente formatados para `"R$ 3.000 – R$ 5.000"`
- Logo da empresa: usa URL se fornecida; caso contrário, gera gradiente de cor determinístico baseado no nome da empresa

### Posts e Feed
- Qualquer usuário autenticado pode criar posts
- O **autor** pode excluir seus próprios posts; **administradores** também
- Curtida é um **toggle** — curtir novamente remove a curtida
- Posts marcados como `isProject = true` aparecem na aba **Projetos** do portfólio do candidato
- Recrutadores podem vincular posts a vagas

### Portfólio
- Upload de imagens: somente `image/jpeg` ou `image/png`, máximo **2 MB**
- Imagens armazenadas como **Base64** diretamente no banco de dados
- A `tagline` (headline) é preservada se o campo enviado estiver em branco na atualização de perfil
- Experiências, habilidades, certificações e projetos só podem ser editados/excluídos pelo **próprio dono**

### Conexões
- Um usuário **não pode se conectar consigo mesmo**
- Se já existe uma conexão no sentido inverso (`B → A`), a nova solicitação `A → B` é automaticamente **aceita**
- Status possíveis: `NONE`, `PENDING_SENT`, `PENDING_RECEIVED`, `ACCEPTED`, `SELF`

### Mensagens
- Conteúdo em branco é **rejeitado**
- Só é possível enviar mensagens para usuários **existentes** no banco

### Busca
- Consulta simultânea em vagas, usuários e posts
- Resultados retornam com tipo identificado (`VAGA`, `USUARIO`, `POST`)

---

## Testes

O projeto possui **104 testes automatizados** divididos em:

| Tipo | Qtd | Tecnologia | O que cobre |
|---|---|---|---|
| Unitários de service | 10 classes | `@ExtendWith(MockitoExtension)` | AuthService, JobService, PostService, PortfolioService, ProfileService, ConnectionService, MessageService, SearchService, AdminService, EmailService |
| Slice de controller | 14 classes | `@WebMvcTest` | Todos os controllers REST e Thymeleaf |
| Utilitários | 1 classe | JUnit 5 | JwtUtil (geração e validação de tokens) |

### Executar os testes

```bash
# Todos os testes
mvn test

# Um teste específico
mvn test -Dtest="AuthServiceTest"

# Apenas compilar sem executar
mvn test-compile
```

---

## Variáveis de Configuração

Arquivo: `src/main/resources/application.properties`

```properties
# Servidor
server.port=8081

# Banco de dados H2 (in-memory)
spring.datasource.url=jdbc:h2:mem:authdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.username=sa
spring.datasource.password=1234
spring.jpa.hibernate.ddl-auto=create-drop   # recria o schema a cada inicialização

# JWT
app.jwt.secret=<chave-secreta-com-256-bits-minimo>
app.jwt.access-token-expiration=900000       # 15 minutos em ms
app.jwt.refresh-token-expiration=604800000   # 7 dias em ms

# E-mail (Gmail com Senha de App)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=<seu-email>@gmail.com
spring.mail.password=<senha-de-app-gmail>

# URL base para links nos e-mails de recuperação de senha
app.frontend.url=http://localhost:8081

# Expiração do token de recuperação de senha
app.password-reset.expiration=3600000        # 1 hora em ms
```

> **Atenção**: nunca faça commit de credenciais reais. Em produção, substitua por variáveis de ambiente.

---

## Diagrama de Entidades

```
User ──────── RefreshToken
 │  └──────── PasswordResetToken
 │
 ├── (CANDIDATO)
 │    ├──── Experience
 │    ├──── Skill
 │    ├──── Certification
 │    └──── Project
 │
 ├── (RECRUTADOR)
 │    └──── Job ──── JobStatus (ATIVA | ENCERRADA)
 │
 ├──────── Post ──── PostComment
 │           └──── Job (vínculo opcional)
 │
 ├──────── Connection (from ──► to)
 │           └──── ConnectionStatus (PENDING | ACCEPTED)
 │
 └──────── Message (from ──► to)
```

---

*Projeto acadêmico — Engenharia de Computação, LP2 · 2025*
