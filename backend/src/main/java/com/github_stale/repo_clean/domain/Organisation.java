package com.github_stale.repo_clean.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "organisations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organisation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(name = "github_org", nullable = false, unique = true)
    private String githubOrg;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Repository> repositories = new ArrayList<>();
    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<DetectionRun> detectionRuns = new ArrayList<>();
}