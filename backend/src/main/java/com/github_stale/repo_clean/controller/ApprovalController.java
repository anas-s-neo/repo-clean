package com.github_stale.repo_clean.controller;

import com.github_stale.repo_clean.config.AppConfig.AppProperties;
import com.github_stale.repo_clean.domain.StaleBranch;
import com.github_stale.repo_clean.domain.StaleBranch.BranchStatus;
import com.github_stale.repo_clean.repository.StaleBranchRepository;
import com.github_stale.repo_clean.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ApprovalController {

    private final StaleBranchRepository staleBranchRepo;
    private final TokenService tokenService;
    private final AppProperties appProperties;

    @GetMapping("/approve")
    public String showConfirmation(@RequestParam String token, @RequestParam String action, Model model) {
        Long id = tokenService.validateAndExtractBranchId(token);
        if (id == null) {
            model.addAttribute("error", "This link has expired or is invalid.");
            return "approval/error";
        }
        StaleBranch branch = staleBranchRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (branch.getStatus() == BranchStatus.DELETED) {
            model.addAttribute("message", "Branch already deleted.");
            model.addAttribute("branchName", branch.getBranchName());
            return "approval/already-done";
        }
        if (branch.getStatus() == BranchStatus.DENIED) {
            model.addAttribute("message", "You previously chose to keep this branch.");
            model.addAttribute("branchName", branch.getBranchName());
            return "approval/already-done";
        }
        if (branch.getStatus() == BranchStatus.APPROVED || branch.getStatus() == BranchStatus.REMINDER_SENT) {
            model.addAttribute("message",
                    "Already approved. Scheduled for deletion at: " + branch.getScheduledDeleteAt());
            model.addAttribute("branchName", branch.getBranchName());
            return "approval/already-done";
        }

        String a = action.toUpperCase();
        if (!a.equals("APPROVE") && !a.equals("DENY")) {
            model.addAttribute("error", "Invalid action.");
            return "approval/error";
        }

        model.addAttribute("branch", branch);
        model.addAttribute("repoFullName", branch.getRepository().getFullName());
        model.addAttribute("action", a);
        model.addAttribute("token", token);
        model.addAttribute("isApprove", a.equals("APPROVE"));
        model.addAttribute("deletionDelayHours", appProperties.getDeletionDelayHours());
        return "approval/confirm";
    }

    @PostMapping("/approve/confirm")
    public String processConfirmation(@RequestParam String token, @RequestParam String action, Model model) {
        Long id = tokenService.validateAndExtractBranchId(token);
        if (id == null) {
            model.addAttribute("error", "Link expired during confirmation.");
            return "approval/error";
        }
        StaleBranch branch = staleBranchRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        LocalDateTime now = LocalDateTime.now();

        if ("APPROVE".equalsIgnoreCase(action)) {
            branch.setStatus(BranchStatus.APPROVED);
            branch.setApprovedAt(now);
            branch.setScheduledDeleteAt(now.plusHours(appProperties.getDeletionDelayHours()));
            model.addAttribute("success", true);
            model.addAttribute("message", "Branch '" + branch.getBranchName() + "' approved for deletion in "
                    + appProperties.getDeletionDelayHours() + " hours.");
            log.info("APPROVED: {}/{}", branch.getRepository().getFullName(), branch.getBranchName());
        } else {
            branch.setStatus(BranchStatus.DENIED);
            branch.setDeniedAt(now);
            model.addAttribute("success", false);
            model.addAttribute("message", "Branch '" + branch.getBranchName() + "' will be kept.");
            log.info("DENIED: {}/{}", branch.getRepository().getFullName(), branch.getBranchName());
        }
        staleBranchRepo.save(branch);
        model.addAttribute("branchName", branch.getBranchName());
        model.addAttribute("repoFullName", branch.getRepository().getFullName());
        model.addAttribute("action", action.toUpperCase());
        return "approval/result";
    }
}