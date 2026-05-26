package com.firstech.repository;

import com.firstech.model.Job;
import com.firstech.model.JobStatus;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatusOrderByIdDesc(JobStatus status);
    List<Job> findByRecruiterOrderByIdDesc(User recruiter);

    @Query("""
        SELECT j FROM Job j
        WHERE j.status = com.firstech.model.JobStatus.ATIVA
          AND (:q = '' OR LOWER(j.title)      LIKE LOWER(CONCAT('%',:q,'%'))
                       OR LOWER(j.jobCompany) LIKE LOWER(CONCAT('%',:q,'%'))
                       OR LOWER(j.location)   LIKE LOWER(CONCAT('%',:q,'%')))
        ORDER BY j.id DESC
        """)
    List<Job> searchAtivas(@Param("q") String q);
}
