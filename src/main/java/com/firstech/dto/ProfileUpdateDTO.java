package com.firstech.dto;

public record ProfileUpdateDTO(
        String name,
        String headline,
        String city,
        String about
) {}
