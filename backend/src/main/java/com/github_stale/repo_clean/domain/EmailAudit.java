package com.github_stale.repo_clean.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_audit")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stale_branch_id")
    private StaleBranch staleBranch;
    @Enumerated(EnumType.STRING)
    @Column(name = "email_type", nullable = false, length = 32)
    private EmailType emailType;
    @Column(nullable = false)
    private String recipient;
    private String subject;
    @CreatedDate
    @Column(name = "sent_at", updatable = false)
    private LocalDateTime sentAt;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private EmailStatus status = EmailStatus.SENT;
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    public enum EmailType {
        APPROVAL_REQUEST, REMINDER, DELETED_NOTICE
    }

    public enum EmailStatus {
        SENT, FAILED
    }
}