package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HabilidadeViewModel {
    private Long id;
    private String nome;
    private boolean destaque;
    private int orderIndex;
}
