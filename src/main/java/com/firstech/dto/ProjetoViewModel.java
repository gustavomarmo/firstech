package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjetoViewModel {
    private Long   id;
    private String nome;
    private String descricao;
    private String corThumb;          // CSS gradient (fallback quando não há imagem)
    private String icone;             // ex: "ti-code" (fallback)
    private String thumbImageBase64;  // Data-URL da imagem de thumbnail (prioritário)
    private String githubUrl;         // Link do repositório no GitHub
    private int    orderIndex;
}
