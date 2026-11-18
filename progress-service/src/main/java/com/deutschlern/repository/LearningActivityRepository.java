package com.deutschlern.repository;

import com.deutschlern.entity.LearningActivity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningActivityRepository
        extends JpaRepository<LearningActivity, Long> {

    long countByStudentId(Long studentId);
}
