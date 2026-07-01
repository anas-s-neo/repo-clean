package com.github_stale.repo_clean.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApprovalResultDto {
    private String action, branchName, repoFullName, message;
    private boolean success;
}
