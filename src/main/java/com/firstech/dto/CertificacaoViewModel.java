package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CertificacaoViewModel {
    private Long id;
    private String emoji;
    private String nome;
    private String emissor;
    private String dataEmissao;
    private int orderIndex;
}
