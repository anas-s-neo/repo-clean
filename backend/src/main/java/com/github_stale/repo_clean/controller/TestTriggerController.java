package com.github_stale.repo_clean.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github_stale.repo_clean.scheduler.BranchDeletionScheduler;
import com.github_stale.repo_clean.scheduler.BranchDetectionScheduler;
import com.github_stale.repo_clean.scheduler.EmailDispatchScheduler;
import com.github_stale.repo_clean.scheduler.ReminderScheduler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestTriggerController {

    private final BranchDetectionScheduler detectionScheduler;
    private final EmailDispatchScheduler emailScheduler;
    private final ReminderScheduler reminderScheduler;
    private final BranchDeletionScheduler deletionScheduler;

    @PostMapping("/trigger/detection")
    public String triggerDetection() {
        detectionScheduler.detectStaleBranches();
        return "Scheduler 1 (detection) triggered — check logs";
    }

    @PostMapping("/trigger/email")
    public String triggerEmail() {
        emailScheduler.sendApprovalEmails();
        return "Scheduler 2 (email dispatch) triggered — check logs";
    }

    @PostMapping("/trigger/reminder")
    public String triggerReminder() {
        reminderScheduler.sendReminders();
        return "Scheduler 3 (reminder) triggered — check logs";
    }

    @PostMapping("/trigger/deletion")
    public String triggerDeletion() {
        deletionScheduler.deleteApprovedBranches();
        return "Scheduler 4 (deletion) triggered — check logs";
    }
}