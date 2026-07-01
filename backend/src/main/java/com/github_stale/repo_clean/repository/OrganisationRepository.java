package com.github_stale.repo_clean.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github_stale.repo_clean.domain.Organisation;

@Repository
public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    Optional<Organisation> findByGithubOrg(String githubOrg);
}
