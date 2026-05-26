package com.firstech.controller;

import com.firstech.dto.PublicProfileDTO;
import com.firstech.model.User;
import com.firstech.service.ConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final ConnectionService connectionService;

    @GetMapping("/{id}/profile")
    public ResponseEntity<PublicProfileDTO> getProfile(
        @PathVariable Long id,
        @AuthenticationPrincipal User viewer
    ) {
        return ResponseEntity.ok(connectionService.getPublicProfile(id, viewer));
    }
}
