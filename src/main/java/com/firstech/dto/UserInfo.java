package com.firstech.dto;

import java.util.Set;

public record UserInfo(
        Long id,
        String name,
        String email,
        Set<String> roles
) {}
