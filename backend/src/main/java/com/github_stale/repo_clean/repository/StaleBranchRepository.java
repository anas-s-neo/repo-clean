package com.github_stale.repo_clean.repository;

import com.github_stale.repo_clean.domain.*;
import com.github_stale.repo_clean.domain.StaleBranch.BranchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Repository
public interface StaleBranchRepository extends JpaRepository<StaleBranch, Long> {

        List<StaleBranch> findByStatus(BranchStatus status);

        List<StaleBranch> findByStatusIn(List<BranchStatus> statuses);

        boolean existsByRepositoryAndBranchNameAndDetectionRun(
                        Repository repository, String branchName, DetectionRun run);

        Optional<StaleBranch> findByApprovalToken(String token);

        List<StaleBranch> findByStatusAndScheduledDeleteAtBefore(BranchStatus status, LocalDateTime before);

        List<StaleBranch> findByStatusAndScheduledDeleteAtBetween(
                        BranchStatus status, LocalDateTime from, LocalDateTime to);

        List<StaleBranch> findByDetectionRun(DetectionRun run);

        @Query("SELECT sb FROM StaleBranch sb JOIN FETCH sb.repository r " +
                        "WHERE sb.status = 'DELETED' ORDER BY sb.deletedAt DESC")
        List<StaleBranch> findAllDeleted();

        @Query("SELECT sb FROM StaleBranch sb JOIN FETCH sb.repository " +
                        "WHERE sb.detectionRun.id = :runId ORDER BY sb.repository.name, sb.branchName")
        List<StaleBranch> findByDetectionRunId(@Param("runId") Long runId);

        long countByStatus(BranchStatus status);

        @Query("SELECT sb.committerEmail, sb.committerName, COUNT(sb), MAX(sb.createdAt) " +
                        "FROM StaleBranch sb WHERE sb.status <> 'PENDING' " +
                        "GROUP BY sb.committerEmail, sb.committerName ORDER BY MAX(sb.createdAt) DESC")
        List<Object[]> findEmailedUserSummary();

        @Query("SELECT sb FROM StaleBranch sb " +
                        "JOIN FETCH sb.repository r JOIN FETCH r.organisation " +
                        "WHERE sb.status = 'PENDING' ORDER BY sb.committerEmail, r.fullName")
        List<StaleBranch> findPendingForEmailDispatch();
}
