package com.github_stale.repo_clean.scheduler;

import com.github_stale.repo_clean.domain.StaleBranch.BranchStatus;
import com.github_stale.repo_clean.repository.StaleBranchRepository;
import com.github_stale.repo_clean.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final StaleBranchRepository staleBranchRepo;
    private final EmailService emailService;

    @Scheduled(fixedRateString = "${scheduling.reminder-check-rate-ms:900000}")
    @Transactional
    public void sendReminders() {
        log.info("=== Scheduler 3 START — reminder check ===");
        LocalDateTime now = LocalDateTime.now();
        // var toRemind =
        // staleBranchRepo.findByStatusAndScheduledDeleteAtBetween(BranchStatus.APPROVED,
        // now,
        // now.plusHours(1));
        var toRemind = staleBranchRepo.findByStatusAndScheduledDeleteAtBetween(BranchStatus.PENDING, now,
                now.plusHours(24));
        if (toRemind.isEmpty())
            return;
        log.info("Scheduler 3 — {} reminder(s) to send", toRemind.size());
        toRemind.forEach(branch -> {
            try {
                emailService.sendReminderEmail(branch.getCommitterEmail(), branch);
                branch.setStatus(BranchStatus.REMINDER_SENT);
                branch.setReminderSentAt(now);
                staleBranchRepo.save(branch);
            } catch (Exception e) {
                log.error("Reminder failed for {}: {}", branch.getBranchName(), e.getMessage());
            }
        });
    }
}
