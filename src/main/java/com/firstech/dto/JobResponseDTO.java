package com.firstech.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class JobResponseDTO {
    private Long id;
    private String title;
    private String company;
    private String companyLogo;
    private String location;
    private String modality;
    private String level;
    private String salary;
    private String description;
    private List<String> tags;
    private String status;
    private LocalDate createdAt;
    private String recruiterName;
}
