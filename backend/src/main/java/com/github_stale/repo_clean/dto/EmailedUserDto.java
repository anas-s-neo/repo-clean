package com.github_stale.repo_clean.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailedUserDto {
    private String email, name, lastSentAt;
    private long emailCount;
}