package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmpresaViewModel {
    private String nome;
    private String corLogo;   // CSS gradient
    private String inicial;   // primeira letra para o avatar
}
