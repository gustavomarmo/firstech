package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PostResponseDTO {
    private Long id;
    private String content;
    private String title;
    private List<String> tags;
    private PostAutorDTO autor;
    private int likes;
    private boolean likedByMe;
    private boolean isProject;
    private String githubUrl;
    private int commentCount;
    private String tempoRelativo;
    private Long linkedJobId;
    private String linkedJobTitle;
    private LocalDateTime createdAt;
}
