package com.firstech.service;

import com.firstech.dto.OnboardingProfileDTO;
import com.firstech.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço responsável por persistir os dados extras do Progressive Profiling.
 *
 * ─── ESTADO ATUAL ─────────────────────────────────────────────────────────
 * Este serviço está pronto para receber os dados; a persistência em banco
 * é um stub (apenas log) até que a entidade de perfil seja modelada.
 *
 * ─── PRÓXIMOS PASSOS SUGERIDOS ────────────────────────────────────────────
 * 1. Crie a entidade `TalentProfile` (ou `RecruiterProfile`) com os campos
 *    abaixo e um relacionamento @OneToOne com User.
 * 2. Substitua o bloco de log pelo save() no repositório correspondente.
 * 3. Para candidatos, persista as tecnologias numa tabela @ElementCollection.
 * 4. Para recrutadores, persista empresa, porte e cultureTags de forma similar.
 * ──────────────────────────────────────────────────────────────────────────
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    @Transactional
    public void saveOnboardingProfile(User user, OnboardingProfileDTO dto) {

        log.info("Salvando perfil de onboarding para o usuário: {} (role: {})",
                user.getEmail(), dto.roleType());

        if ("CANDIDATO".equalsIgnoreCase(dto.roleType())) {
            saveTalentProfile(user, dto);
        } else if ("RECRUTADOR".equalsIgnoreCase(dto.roleType())) {
            saveRecruiterProfile(user, dto);
        } else {
            log.warn("roleType desconhecido: {}", dto.roleType());
        }
    }

    private void saveTalentProfile(User user, OnboardingProfileDTO dto) {
        /*
         * TODO: Implemente a persistência da entidade TalentProfile.
         *
         * Exemplo do que será salvo:
         *   phone         = dto.phone()
         *   linkedin      = dto.linkedin()
         *   github        = dto.github()
         *   careerMoment  = dto.careerMoment()   // "estagio" | "junior" | "studying"
         *   workModel     = dto.workModel()       // "remote"  | "hybrid" | "onsite"
         *   city          = dto.city()
         *   tagline       = dto.tagline()
         *   technologies  = dto.technologies()   // List<String>
         */
        log.info("[Talent] tagline='{}' | workModel='{}' | technologies={}",
                dto.tagline(), dto.workModel(), dto.technologies());
    }

    private void saveRecruiterProfile(User user, OnboardingProfileDTO dto) {
        /*
         * TODO: Implemente a persistência da entidade RecruiterProfile.
         *
         * Exemplo do que será salvo:
         *   phone        = dto.phone()
         *   company      = dto.company()
         *   companySize  = dto.companySize()
         *   website      = dto.website()
         *   cultureTags  = dto.cultureTags()    // List<String>
         *   goals        = dto.goals()          // List<String>
         */
        log.info("[Recruiter] empresa='{}' | porte='{}' | cultura={}",
                dto.company(), dto.companySize(), dto.cultureTags());
    }
}
