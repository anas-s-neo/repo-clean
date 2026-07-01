package com.github_stale.repo_clean.scheduler;

import com.github_stale.repo_clean.config.AppConfig.AppProperties;
import com.github_stale.repo_clean.domain.*;
import com.github_stale.repo_clean.repository.*;
import com.github_stale.repo_clean.service.GitHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class BranchDetectionScheduler {

    private final GitHubService gitHubService;
    private final OrganisationRepository organisationRepository;
    private final DetectionRunRepository detectionRunRepository;
    private final StaleBranchRepository staleBranchRepository;
    private final AppProperties appProperties;

    @Scheduled(cron = "${scheduling.branch-detection-cron:0 0 2 * * SUN}", zone = "UTC")
    public void detectStaleBranches() {
        log.info("=== Scheduler 1 START — stale branch detection ===");
        organisationRepository.findAll().forEach(this::processOrganisation);
        log.info("=== Scheduler 1 END ===");
    }

    @Transactional
    public void processOrganisation(Organisation org) {
        log.info("Processing org/user: {}", org.getGithubOrg());
        DetectionRun run = detectionRunRepository.save(
                DetectionRun.builder().organisation(org).status(DetectionRun.Status.RUNNING).build());
        try {
            List<GHRepository> repos = new ArrayList<>();
            String targetName = org.getGithubOrg();

            try {
                log.info("Attempting to fetch organization repositories for: {}", targetName);
                GHOrganization ghOrg = gitHubService.getOrganisation(targetName);
                repos = gitHubService.listRepositories(ghOrg);
            } catch (Exception e) {
                log.warn("Organization lookup failed or not found ({})! Attempting Personal User fallback...",
                        e.getMessage());
                // Call our new clean service-level method
                repos = gitHubService.listUserRepositories(targetName);
            }

            run.setTotalRepos(repos.size());
            log.info("Found {} repositories to scan for target '{}'", repos.size(), targetName);

            int total = 0;
            // LocalDateTime cutoff =
            // LocalDateTime.now().minusDays(appProperties.getStaleDays());
            LocalDateTime cutoff = LocalDateTime.now().plusDays(1);
            for (GHRepository ghRepo : repos) {
                try {
                    Repository dbRepo = gitHubService.upsertRepository(ghRepo, org);
                    total += scanBranches(ghRepo, dbRepo, run, cutoff);
                } catch (Exception e) {
                    log.error("Error in repo {}: {}", ghRepo.getFullName(), e.getMessage());
                }
            }
            run.complete(total);
        } catch (Exception e) {
            log.error("Detection failed for {}: {}", org.getGithubOrg(), e.getMessage());
            run.fail(e.getMessage());
        } finally {
            detectionRunRepository.save(run);
        }
    }

    private int scanBranches(GHRepository ghRepo, Repository dbRepo, DetectionRun run, LocalDateTime cutoff)
            throws IOException {
        int count = 0;
        for (Map.Entry<String, GHBranch> entry : gitHubService.listBranches(ghRepo).entrySet()) {
            String name = entry.getKey();
            if (isProtected(name))
                continue;
            try {
                GHCommit commit = gitHubService.getCommit(ghRepo, entry.getValue().getSHA1());
                LocalDateTime date = commit.getCommitDate().toInstant().atZone(ZoneOffset.UTC).toLocalDateTime();
                if (date.isBefore(cutoff)
                        && !staleBranchRepository.existsByRepositoryAndBranchNameAndDetectionRun(dbRepo, name, run)) {
                    GHCommit.ShortInfo info = commit.getCommitShortInfo();
                    staleBranchRepository.save(StaleBranch.builder()
                            .repository(dbRepo).detectionRun(run).branchName(name)
                            .lastCommitSha(entry.getValue().getSHA1())
                            .lastCommitMessage(info.getMessage()).lastCommitDate(date)
                            .committerName(info.getCommitter().getName())
                            .committerEmail(info.getCommitter().getEmail())
                            .status(StaleBranch.BranchStatus.PENDING).build());
                    count++;
                }
            } catch (Exception e) {
                log.warn("Skip branch {}/{}: {}", ghRepo.getName(), name, e.getMessage());
            }
        }
        return count;
    }

    private boolean isProtected(String name) {
        return Set.of("main", "master", "develop", "development", "staging", "production").contains(name)
                || name.startsWith("release/") || name.startsWith("hotfix/");
    }
}