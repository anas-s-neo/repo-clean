CREATE TABLE organisations
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    github_org VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE repositories
(
    id              BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT       NOT NULL REFERENCES organisations (id) ON DELETE CASCADE,
    name            VARCHAR(255) NOT NULL,
    full_name       VARCHAR(512) NOT NULL UNIQUE,
    html_url        TEXT,
    description     TEXT,
    archived        BOOLEAN                  DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE TABLE detection_runs
(
    id              BIGSERIAL PRIMARY KEY,
    organisation_id BIGINT      NOT NULL REFERENCES organisations (id) ON DELETE CASCADE,
    started_at      TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    completed_at    TIMESTAMP WITH TIME ZONE,
    total_repos     INT                      DEFAULT 0,
    total_stale     INT                      DEFAULT 0,
    status          VARCHAR(32) NOT NULL     DEFAULT 'RUNNING',
    error_message   TEXT,
    CONSTRAINT chk_detection_run_status CHECK (status IN ('RUNNING', 'COMPLETED', 'FAILED'))
);

CREATE TABLE stale_branches
(
    id                     BIGSERIAL PRIMARY KEY,
    repository_id          BIGINT       NOT NULL REFERENCES repositories (id) ON DELETE CASCADE,
    detection_run_id       BIGINT       NOT NULL REFERENCES detection_runs (id) ON DELETE CASCADE,
    branch_name            VARCHAR(512) NOT NULL,
    last_commit_sha        VARCHAR(40),
    last_commit_message    TEXT,
    last_commit_date       TIMESTAMP WITH TIME ZONE,
    committer_name         VARCHAR(255),
    committer_email        VARCHAR(255),
    committer_github_login VARCHAR(255),
    status                 VARCHAR(32)  NOT NULL    DEFAULT 'PENDING',
    approval_token         TEXT,
    approved_at            TIMESTAMP WITH TIME ZONE,
    denied_at              TIMESTAMP WITH TIME ZONE,
    reminder_sent_at       TIMESTAMP WITH TIME ZONE,
    scheduled_delete_at    TIMESTAMP WITH TIME ZONE,
    deleted_at             TIMESTAMP WITH TIME ZONE,
    created_at             TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    updated_at             TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT chk_stale_branch_status
        CHECK (status IN ('PENDING', 'EMAIL_SENT', 'APPROVED', 'DENIED', 'REMINDER_SENT', 'DELETED', 'FAILED')),
    CONSTRAINT uq_repo_branch_run UNIQUE (repository_id, branch_name, detection_run_id)
);

CREATE TABLE email_audit
(
    id              BIGSERIAL PRIMARY KEY,
    stale_branch_id BIGINT       REFERENCES stale_branches (id) ON DELETE SET NULL,
    email_type      VARCHAR(32)  NOT NULL,
    recipient       VARCHAR(255) NOT NULL,
    subject         VARCHAR(512),
    sent_at         TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    status          VARCHAR(32)  NOT NULL    DEFAULT 'SENT',
    error_message   TEXT,
    CONSTRAINT chk_email_type CHECK (email_type IN ('APPROVAL_REQUEST', 'REMINDER', 'DELETED_NOTICE')),
    CONSTRAINT chk_email_status CHECK (status IN ('SENT', 'FAILED'))
);

CREATE INDEX idx_repositories_org ON repositories (organisation_id);
CREATE INDEX idx_detection_runs_org ON detection_runs (organisation_id);
CREATE INDEX idx_stale_branches_status ON stale_branches (status);
CREATE INDEX idx_stale_branches_email ON stale_branches (committer_email);
CREATE INDEX idx_stale_branches_sched ON stale_branches (scheduled_delete_at);
CREATE INDEX idx_stale_branches_repo ON stale_branches (repository_id);
CREATE INDEX idx_stale_branches_run ON stale_branches (detection_run_id);
CREATE INDEX idx_email_audit_branch ON email_audit (stale_branch_id);

INSERT INTO organisations (name, github_org)
VALUES ('My Organisation', 'my-github-org') ON CONFLICT DO NOTHING;