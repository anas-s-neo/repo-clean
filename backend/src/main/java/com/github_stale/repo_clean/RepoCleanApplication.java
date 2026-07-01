package com.github_stale.repo_clean;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.github_stale.repo_clean")
@EnableScheduling
@EnableJpaAuditing
public class RepoCleanApplication {

    public static void main(String[] args) {
        SpringApplication.run(RepoCleanApplication.class, args);
    }

}
