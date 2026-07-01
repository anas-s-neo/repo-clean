package com.github_stale.repo_clean.controller;

import com.github_stale.repo_clean.dto.*;
import com.github_stale.repo_clean.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    @GetMapping("/deleted-branches")
    public ResponseEntity<List<DeletedBranchDto>> getDeleted() {
        return ResponseEntity.ok(dashboardService.getDeletedBranches());
    }

    @GetMapping("/emailed-users")
    public ResponseEntity<List<EmailedUserDto>> getEmailedUsers() {
        return ResponseEntity.ok(dashboardService.getEmailedUsers());
    }

    @GetMapping("/stale-branches/today")
    public ResponseEntity<List<StaleBranchDto>> getToday() {
        return ResponseEntity.ok(dashboardService.getTodaysStaleBranches());
    }

    @GetMapping("/stale-branches")
    public ResponseEntity<List<StaleBranchDto>> getAll() {
        return ResponseEntity.ok(dashboardService.getAllStaleBranches());
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}
