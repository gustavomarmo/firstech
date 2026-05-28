package com.firstech.controller;

import com.firstech.dto.AdminCommentDTO;
import com.firstech.dto.AdminPostDTO;
import com.firstech.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST endpoints para gestão de posts e comentários pelo administrador.
 * Protegido por {@code hasAuthority("ADMINISTRADOR")} no SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/posts")
@RequiredArgsConstructor
public class AdminPostApiController {

    private final AdminService adminService;

    /** Lista todos os posts da plataforma. */
    @GetMapping
    public ResponseEntity<List<AdminPostDTO>> listPosts() {
        return ResponseEntity.ok(adminService.getAllPosts());
    }

    /** Exclui permanentemente um post e seus comentários. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Long id) {
        try {
            adminService.deletePost(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Lista os comentários de um post específico. */
    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<AdminCommentDTO>> listComments(@PathVariable Long postId) {
        return ResponseEntity.ok(adminService.getCommentsByPost(postId));
    }

    /** Exclui um comentário específico (decrementa o contador do post). */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        try {
            adminService.deleteComment(commentId);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
