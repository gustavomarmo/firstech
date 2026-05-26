package com.firstech.dto;

/**
 * Resultado unificado de busca (pessoa, vaga ou publicação).
 *
 * @param id           ID da entidade
 * @param nome         Título principal exibido no card
 * @param subtitulo    Segunda linha de informação
 * @param tipo         "pessoa" | "vaga" | "post"
 * @param tipoLabel    Label PT exibida no badge ("Pessoa" / "Vaga" / "Post")
 * @param corAvatar    CSS background (gradiente) para o avatar/ícone
 * @param inicial      Letra(s) iniciais para o avatar quando não há foto
 * @param avatarBase64 Data-URL da foto (null quando não disponível)
 */
public record SearchResultDTO(
        Long   id,
        String nome,
        String subtitulo,
        String tipo,
        String tipoLabel,
        String corAvatar,
        String inicial,
        String avatarBase64
) {}
