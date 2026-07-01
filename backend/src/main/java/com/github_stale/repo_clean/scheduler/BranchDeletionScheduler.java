package com.github_stale.repo_clean.scheduler;

import com.github_stale.repo_clean.domain.StaleBranch.BranchStatus;
import com.github_stale.repo_clean.repository.StaleBranchRepository;
import com.github_stale.repo_clean.service.GitHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class BranchDeletionScheduler {

    private final StaleBranchRepository staleBranchRepo;
    private final GitHubService gitHubService;

    @Scheduled(fixedRateString = "${scheduling.deletion-check-rate-ms:900000}")
    @Transactional
    public void deleteApprovedBranches() {
        LocalDateTime now = LocalDateTime.now();
        var toDelete = staleBranchRepo.findByStatusAndScheduledDeleteAtBefore(BranchStatus.REMINDER_SENT, now);
        if (toDelete.isEmpty())
            return;
        log.info("Scheduler 4 — {} branch(es) to delete", toDelete.size());
        toDelete.forEach(branch -> {
            try {
                gitHubService.deleteBranch(branch.getRepository().getFullName(), branch.getBranchName());
                branch.setStatus(BranchStatus.DELETED);
                branch.setDeletedAt(now);
                log.info("Deleted {}/{}", branch.getRepository().getFullName(), branch.getBranchName());
            } catch (Exception e) {
                branch.setStatus(BranchStatus.FAILED);
                log.error("Delete failed {}/{}: {}", branch.getRepository().getFullName(), branch.getBranchName(),
                        e.getMessage());
            }
            staleBranchRepo.save(branch);
        });
    }
}
