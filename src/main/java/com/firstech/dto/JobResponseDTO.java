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
    /** Resolved company name (falls back to recruiter's company / name). */
    private String company;
    /** CSS background value: linear-gradient or url(...) center/cover. */
    private String companyLogo;
    /** Raw image URL (null when using gradient). Used to decide whether to show initial letter. */
    private String companyLogoUrl;
    /** Raw stored company name override (for pre-filling the edit modal). */
    private String jobCompany;
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
