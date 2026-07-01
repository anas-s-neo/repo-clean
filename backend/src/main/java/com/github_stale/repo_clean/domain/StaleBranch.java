package com.github_stale.repo_clean.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stale_branches", uniqueConstraints = @UniqueConstraint(columnNames = { "repository_id", "branch_name",
        "detection_run_id" }))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaleBranch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repository repository;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_run_id", nullable = false)
    private DetectionRun detectionRun;

    @Column(name = "branch_name", nullable = false, length = 512)
    private String branchName;
    @Column(name = "last_commit_sha", length = 40)
    private String lastCommitSha;
    @Column(name = "last_commit_message", columnDefinition = "TEXT")
    private String lastCommitMessage;
    @Column(name = "last_commit_date")
    private LocalDateTime lastCommitDate;
    @Column(name = "committer_name")
    private String committerName;
    @Column(name = "committer_email")
    private String committerEmail;
    @Column(name = "committer_github_login")
    private String committerGithubLogin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private BranchStatus status = BranchStatus.PENDING;

    @Column(name = "approval_token", columnDefinition = "TEXT")
    private String approvalToken;
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    @Column(name = "denied_at")
    private LocalDateTime deniedAt;
    @Column(name = "reminder_sent_at")
    private LocalDateTime reminderSentAt;
    @Column(name = "scheduled_delete_at")
    private LocalDateTime scheduledDeleteAt;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "staleBranch", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmailAudit> emailAudits = new ArrayList<>();

    public enum BranchStatus {
        PENDING, EMAIL_SENT, APPROVED, DENIED, REMINDER_SENT, DELETED, FAILED
    }
}
