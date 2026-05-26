package com.firstech.repository;

import com.firstech.model.Job;
import com.firstech.model.JobStatus;
import com.firstech.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByStatusOrderByIdDesc(JobStatus status);
    List<Job> findByRecruiterOrderByIdDesc(User recruiter);
}
