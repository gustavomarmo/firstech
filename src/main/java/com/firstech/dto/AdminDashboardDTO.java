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
        Map<String, Long> vagasPorModalidade,
        Map<String, Long> vagasPorNivel,
        List<TopPosterEntry> topPosters
) {
    /** Top postador: nome, total de posts e se é recrutador. */
    public record TopPosterEntry(String nome, long totalPosts, boolean recrutador) {}
}
