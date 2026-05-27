package com.firstech.dto;
import java.util.List;

public record PublicProfileDTO(
    Long id,
    String nome,
    String headline,
    String cidade,
    String sobre,
    String avatarBase64,
    String bannerBase64,
    String corAvatar,
    String inicial,
    int totalConexoes,
    List<String> tecnologias,
    String connectionStatus,  // "NONE" | "PENDING_SENT" | "PENDING_RECEIVED" | "ACCEPTED" | "SELF"
    boolean recrutador,
    List<ExperienciaViewModel>  experiencias,
    List<HabilidadeViewModel>   habilidades,
    List<CertificacaoViewModel> certificacoes,
    List<ProjetoViewModel>      projetos,
    List<VagaResumoDTO>         vagasPublicadas
) {}
