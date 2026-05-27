package com.firstech.controller;

import com.firstech.dto.SearchResultDTO;
import com.firstech.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResultDTO>> search(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(searchService.search(q));
    }

    /** Busca apenas usuários (candidatos + recrutadores). Usado pela aba Pessoas na tela de Rede. */
    @GetMapping("/users")
    public ResponseEntity<List<SearchResultDTO>> searchUsers(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(searchService.searchUsers(q));
    }
}
