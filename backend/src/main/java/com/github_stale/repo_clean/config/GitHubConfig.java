package com.github_stale.repo_clean.config;

import lombok.Data;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.RateLimitHandler;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(GitHubConfig.GitHubProperties.class)
public class GitHubConfig {

    @Bean
    public GitHub gitHubClient(GitHubProperties props) throws IOException {
        return new GitHubBuilder()
                .withEndpoint(props.getApiUrl())
                .withOAuthToken(props.getToken())
                .withRateLimitHandler(RateLimitHandler.WAIT)
                .build();
    }

    @Data
    @ConfigurationProperties(prefix = "github")
    public static class GitHubProperties {
        private String token;
        private String apiUrl = "https://api.github.com";
        private String organisations;
    }
}
