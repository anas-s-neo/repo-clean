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
@Table(name = "repositories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;
    @Column(nullable = false)
    private String name;
    @Column(name = "full_name", nullable = false, unique = true)
    private String fullName;
    @Column(name = "html_url")
    private String htmlUrl;
    private String description;
    @Column(nullable = false)
    @Builder.Default
    private Boolean archived = false;
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StaleBranch> staleBranches = new ArrayList<>();
}
