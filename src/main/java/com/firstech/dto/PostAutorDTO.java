package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostAutorDTO {
    private Long id;
    private String nome;
    private String cargo;
    private String corAvatar; // CSS background value (gradient)
    private String inicial;   // Primeira letra em maiúsculo para o avatar
}
