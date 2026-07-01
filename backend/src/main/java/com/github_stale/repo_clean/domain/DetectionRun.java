package com.github_stale.repo_clean.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "detection_runs")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetectionRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;
    @CreatedDate
    @Column(name = "started_at", updatable = false)
    private LocalDateTime startedAt;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    @Column(name = "total_repos")
    @Builder.Default
    private Integer totalRepos = 0;
    @Column(name = "total_stale")
    @Builder.Default
    private Integer totalStale = 0;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.RUNNING;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    @OneToMany(mappedBy = "detectionRun", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StaleBranch> staleBranches = new ArrayList<>();

    public enum Status {
        RUNNING, COMPLETED, FAILED
    }

    public void complete(int staleCount) {
        this.status = Status.COMPLETED;
        this.totalStale = staleCount;
        this.completedAt = LocalDateTime.now();
    }

    public void fail(String message) {
        this.status = Status.FAILED;
        this.errorMessage = message;
        this.completedAt = LocalDateTime.now();
    }
}
