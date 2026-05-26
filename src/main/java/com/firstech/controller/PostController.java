package com.firstech.controller;

import com.firstech.dto.PostRequestDTO;
import com.firstech.dto.PostResponseDTO;
import com.firstech.model.User;
import com.firstech.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public ResponseEntity<List<PostResponseDTO>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @PostMapping
    public ResponseEntity<PostResponseDTO> createPost(
            @Valid @RequestBody PostRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(dto, user));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostResponseDTO> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody PostRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(postService.updatePost(id, dto, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        postService.deletePost(id, user);
        return ResponseEntity.noContent().build();
    }
}
