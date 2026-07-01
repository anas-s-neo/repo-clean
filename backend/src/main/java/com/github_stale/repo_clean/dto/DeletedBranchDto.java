package com.github_stale.repo_clean.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DeletedBranchDto {
    private Long id;
    private String branchName, repoName, repoFullName, committerName, committerEmail;
    private LocalDateTime lastCommitDate, deletedAt;
}