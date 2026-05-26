package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProjetoViewModel {
    private Long id;
    private String nome;
    private String descricao;
    private String corThumb;   // CSS gradient
    private String icone;      // ex: "ti-code" (sem o prefixo "ti ")
    private int orderIndex;
}
