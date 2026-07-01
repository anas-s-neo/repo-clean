package com.github_stale.repo_clean.scheduler;

import com.github_stale.repo_clean.domain.StaleBranch;
import com.github_stale.repo_clean.domain.StaleBranch.BranchStatus;
import com.github_stale.repo_clean.repository.StaleBranchRepository;
import com.github_stale.repo_clean.service.EmailService;
import com.github_stale.repo_clean.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailDispatchScheduler {

    private final StaleBranchRepository staleBranchRepo;
    private final EmailService emailService;
    private final TokenService tokenService;

    @Scheduled(cron = "${scheduling.email-dispatch-cron:0 0 8 * * SUN}", zone = "UTC")
    @Transactional
    public void sendApprovalEmails() {
        log.info("=== Scheduler 2 START — approval email dispatch ===");
        List<StaleBranch> pending = staleBranchRepo.findPendingForEmailDispatch();
        if (pending.isEmpty()) {
            log.info("No pending branches.");
            return;
        }

        // Group: committerEmail → repoFullName → [branches]
        Map<String, Map<String, List<StaleBranch>>> grouped = pending.stream()
                .collect(Collectors.groupingBy(StaleBranch::getCommitterEmail,
                        Collectors.groupingBy(b -> b.getRepository().getFullName())));

        int sent = 0, failed = 0;
        for (var ce : grouped.entrySet()) {
            for (var re : ce.getValue().entrySet()) {
                List<StaleBranch> branches = re.getValue();
                try {
                    branches.forEach(b -> b.setApprovalToken(tokenService.generateApprovalToken(b.getId())));
                    staleBranchRepo.saveAll(branches);
                    emailService.sendApprovalRequestEmail(ce.getKey(), re.getKey(), branches);
                    branches.forEach(b -> b.setStatus(BranchStatus.EMAIL_SENT));
                    staleBranchRepo.saveAll(branches);
                    sent++;
                } catch (Exception e) {
                    log.error("Email failed to {} for {}: {}", ce.getKey(), re.getKey(), e.getMessage());
                    failed++;
                }
            }
        }
        log.info("=== Scheduler 2 END — {} sent, {} failed ===", sent, failed);
    }
}
