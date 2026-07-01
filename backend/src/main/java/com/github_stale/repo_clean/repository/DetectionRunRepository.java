package com.github_stale.repo_clean.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github_stale.repo_clean.domain.DetectionRun;
import com.github_stale.repo_clean.domain.Organisation;

@Repository
public interface DetectionRunRepository extends JpaRepository<DetectionRun, Long> {
    List<DetectionRun> findByOrganisationOrderByStartedAtDesc(Organisation organisation);

    Optional<DetectionRun> findTopByOrganisationOrderByStartedAtDesc(Organisation org);
}