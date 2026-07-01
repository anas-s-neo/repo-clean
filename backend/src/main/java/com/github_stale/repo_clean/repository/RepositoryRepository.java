package com.github_stale.repo_clean.repository;

import com.github_stale.repo_clean.domain.Organisation;
import com.github_stale.repo_clean.domain.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    Optional<Repository> findByFullName(String fullName);

    List<Repository> findByOrganisation(Organisation organisation);
}