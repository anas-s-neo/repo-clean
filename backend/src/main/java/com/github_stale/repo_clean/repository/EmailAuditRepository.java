package com.github_stale.repo_clean.repository;

import com.github_stale.repo_clean.domain.EmailAudit;
import com.github_stale.repo_clean.domain.StaleBranch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailAuditRepository extends JpaRepository<EmailAudit, Long> {
    List<EmailAudit> findByStaleBranch(StaleBranch staleBranch);

    List<EmailAudit> findByRecipientOrderBySentAtDesc(String recipient);

    long countByRecipient(String recipient);
}
