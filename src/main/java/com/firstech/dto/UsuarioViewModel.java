package com.firstech.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioViewModel {

    @Builder.Default private Long   userId         = null;
    @Builder.Default private String nome          = "";
    @Builder.Default private String nomeCompleto  = "";
    @Builder.Default private String headline      = "";
    @Builder.Default private String localizacao   = "";
    @Builder.Default private String sobre         = "";
    @Builder.Default private String cargo         = "";
    @Builder.Default private String empresa       = "";
    @Builder.Default private boolean recrutador   = false;

    @Builder.Default private int conexoes         = 0;
    @Builder.Default private int visualizacoes    = 0;
    @Builder.Default private int impressoes       = 0;

    @Builder.Default private List<?> experiencias  = List.of();
    @Builder.Default private List<?> habilidades   = List.of();
    @Builder.Default private List<?> certificacoes = List.of();
    @Builder.Default private List<?> projetos      = List.of();

    public String getInicial() {
        if (nome == null || nome.isBlank()) return "?";
        return nome.substring(0, 1).toUpperCase();
    }

    public String getIniciais() {
        if (nome == null || nome.isBlank()) return "?";
        String[] parts = nome.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        }
        return nome.length() >= 2 ? nome.substring(0, 2).toUpperCase() : nome.toUpperCase();
    }
}
