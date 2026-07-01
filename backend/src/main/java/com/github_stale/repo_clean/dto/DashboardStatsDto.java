package com.github_stale.repo_clean.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDto {
    private long totalPending, totalEmailSent, totalApproved, totalDenied, totalReminderSent, totalDeleted;
}
