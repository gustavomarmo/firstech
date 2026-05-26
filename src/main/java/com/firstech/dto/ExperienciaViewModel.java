package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExperienciaViewModel {
    private Long id;
    private String cargo;
    private EmpresaViewModel empresa;
    private String periodo;
    private String descricao;
    private int orderIndex;
}
