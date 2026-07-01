package com.github_stale.repo_clean.service;

import com.github_stale.repo_clean.config.AppConfig.AppProperties;
import com.github_stale.repo_clean.domain.EmailAudit;
import com.github_stale.repo_clean.domain.StaleBranch;
import com.github_stale.repo_clean.repository.EmailAuditRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailAuditRepository emailAuditRepository;
    private final AppProperties appProperties;

    @Async
    public void sendApprovalRequestEmail(String toEmail, String repoFullName, List<StaleBranch> branches) {
        String subject = "Action required: stale branches in " + repoFullName;
        try {
            Context ctx = new Context();
            ctx.setVariable("repoFullName", repoFullName);
            ctx.setVariable("committerName", branches.get(0).getCommitterName());
            ctx.setVariable("branches", buildBranchModels(branches));
            send(toEmail, subject, templateEngine.process("email/approval-request", ctx));
            branches.forEach(b -> audit(b, EmailAudit.EmailType.APPROVAL_REQUEST, toEmail, subject, null));
        } catch (Exception e) {
            log.error("Approval email failed to {}: {}", toEmail, e.getMessage());
            branches.forEach(b -> audit(b, EmailAudit.EmailType.APPROVAL_REQUEST, toEmail, subject, e.getMessage()));
            throw new RuntimeException("Email send failed", e);
        }
    }

    @Async
    public void sendReminderEmail(String toEmail, StaleBranch branch) {
        String subject = "Reminder: branch '" + branch.getBranchName() + "' will be deleted in 12 hours";
        try {
            Context ctx = new Context();
            ctx.setVariable("branch", branch);
            ctx.setVariable("repoFullName", branch.getRepository().getFullName());
            ctx.setVariable("deleteAt", branch.getScheduledDeleteAt());
            ctx.setVariable("committerName", branch.getCommitterName());
            send(toEmail, subject, templateEngine.process("email/reminder", ctx));
            audit(branch, EmailAudit.EmailType.REMINDER, toEmail, subject, null);
        } catch (Exception e) {
            log.error("Reminder email failed to {}: {}", toEmail, e.getMessage());
            audit(branch, EmailAudit.EmailType.REMINDER, toEmail, subject, e.getMessage());
            throw new RuntimeException("Reminder failed", e);
        }
    }

    private void send(String to, String subject, String html) throws MessagingException {
        MimeMessage msg = mailSender.createMimeMessage();
        MimeMessageHelper h = new MimeMessageHelper(msg, true, "UTF-8");
        h.setFrom(appProperties.getMailFrom());
        h.setTo(to);
        h.setSubject(subject);
        h.setText(html, true);
        mailSender.send(msg);
        log.info("Email sent → {}", to);
    }

    private List<Map<String, Object>> buildBranchModels(List<StaleBranch> branches) {
        return branches.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", b.getBranchName());
            m.put("commitSha",
                    b.getLastCommitSha() != null
                            ? b.getLastCommitSha().substring(0, Math.min(7, b.getLastCommitSha().length()))
                            : "");
            m.put("commitMessage", b.getLastCommitMessage());
            m.put("commitDate", b.getLastCommitDate());
            m.put("approveUrl",
                    appProperties.getBaseUrl() + "/approve?token=" + b.getApprovalToken() + "&action=APPROVE");
            m.put("denyUrl", appProperties.getBaseUrl() + "/approve?token=" + b.getApprovalToken() + "&action=DENY");
            return m;
        }).toList();
    }

    private void audit(StaleBranch b, EmailAudit.EmailType type, String to, String subject, String error) {
        emailAuditRepository.save(EmailAudit.builder()
                .staleBranch(b).emailType(type).recipient(to).subject(subject)
                .status(error == null ? EmailAudit.EmailStatus.SENT : EmailAudit.EmailStatus.FAILED)
                .errorMessage(error).build());
    }
}
