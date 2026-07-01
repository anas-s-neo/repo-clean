package com.github_stale.repo_clean.service;

import com.github_stale.repo_clean.domain.*;
import com.github_stale.repo_clean.dto.*;
import com.github_stale.repo_clean.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

        private final StaleBranchRepository staleBranchRepo;
        private final DetectionRunRepository detectionRunRepo;
        private final OrganisationRepository organisationRepo;

        public DashboardStatsDto getStats() {
                return DashboardStatsDto.builder()
                                .totalPending(staleBranchRepo.countByStatus(StaleBranch.BranchStatus.PENDING))
                                .totalEmailSent(staleBranchRepo.countByStatus(StaleBranch.BranchStatus.EMAIL_SENT))
                                .totalApproved(staleBranchRepo.countByStatus(StaleBranch.BranchStatus.APPROVED))
                                .totalDenied(staleBranchRepo.countByStatus(StaleBranch.BranchStatus.DENIED))
                                .totalReminderSent(
                                                staleBranchRepo.countByStatus(StaleBranch.BranchStatus.REMINDER_SENT))
                                .totalDeleted(staleBranchRepo.countByStatus(StaleBranch.BranchStatus.DELETED))
                                .build();
        }

        public List<DeletedBranchDto> getDeletedBranches() {
                return staleBranchRepo.findAllDeleted().stream()
                                .map(b -> DeletedBranchDto.builder().id(b.getId()).branchName(b.getBranchName())
                                                .repoName(b.getRepository().getName())
                                                .repoFullName(b.getRepository().getFullName())
                                                .committerName(b.getCommitterName())
                                                .committerEmail(b.getCommitterEmail())
                                                .lastCommitDate(b.getLastCommitDate()).deletedAt(b.getDeletedAt())
                                                .build())
                                .collect(Collectors.toList());
        }

        public List<EmailedUserDto> getEmailedUsers() {
                return staleBranchRepo.findEmailedUserSummary().stream()
                                .map(row -> EmailedUserDto.builder().email((String) row[0]).name((String) row[1])
                                                .emailCount(((Number) row[2]).longValue()).lastSentAt(row[3].toString())
                                                .build())
                                .collect(Collectors.toList());
        }

        public List<StaleBranchDto> getTodaysStaleBranches() {
                return organisationRepo.findAll().stream()
                                .flatMap(org -> detectionRunRepo.findTopByOrganisationOrderByStartedAtDesc(org)
                                                .map(run -> staleBranchRepo.findByDetectionRunId(run.getId()).stream()
                                                                .map(this::toDto))
                                                .orElse(Stream.empty()))
                                .collect(Collectors.toList());
        }

        public List<StaleBranchDto> getAllStaleBranches() {
                return staleBranchRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
        }

        private StaleBranchDto toDto(StaleBranch b) {
                return StaleBranchDto.builder().id(b.getId()).branchName(b.getBranchName())
                                .repoName(b.getRepository().getName()).repoFullName(b.getRepository().getFullName())
                                .committerName(b.getCommitterName()).committerEmail(b.getCommitterEmail())
                                .committerLogin(b.getCommitterGithubLogin()).lastCommitSha(b.getLastCommitSha())
                                .lastCommitMessage(b.getLastCommitMessage()).lastCommitDate(b.getLastCommitDate())
                                .status(b.getStatus().name()).detectedAt(b.getCreatedAt()).build();
        }
}