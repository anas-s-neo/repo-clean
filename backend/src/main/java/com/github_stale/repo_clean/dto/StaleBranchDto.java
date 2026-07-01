package com.github_stale.repo_clean.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StaleBranchDto {
    private Long id;
    private String branchName, repoName, repoFullName, committerName, committerEmail, committerLogin;
    private String lastCommitSha, lastCommitMessage, status;
    private LocalDateTime lastCommitDate, detectedAt;
}
