package com.firstech.dto;

import java.util.List;
import java.util.Map;

/**
 * Snapshot de métricas da plataforma para o dashboard administrativo.
 */
public record AdminDashboardDTO(
        long totalUsuarios,
        long totalCandidatos,
        long totalRecrutadores,
        long totalVagas,
        long vagasAtivas,
        long vagasEncerradas,
        long totalPosts,
        long totalConexoesAceitas,
        Map<String, Long> vagasPorModalidade,
        Map<String, Long> vagasPorNivel,
        List<TopPosterEntry>  topPosters,
        List<UserResumoEntry> ultimosUsuarios,
        List<VagaResumoEntry> ultimasVagas
) {

    /** Top postador: nome, total de posts e se é recrutador. */
    public record TopPosterEntry(String nome, long totalPosts, boolean recrutador) {}

    /** Resumo de usuário para a tabela de últimos cadastros. */
    public record UserResumoEntry(Long id, String nome, boolean recrutador, String email) {}

    /** Resumo de vaga para a tabela de vagas recentes. */
    public record VagaResumoEntry(
            Long id, String titulo, String empresa,
            String nivel, String modalidade, boolean ativa
    ) {}
}
