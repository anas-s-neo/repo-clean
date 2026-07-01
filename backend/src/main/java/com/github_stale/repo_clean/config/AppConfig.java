package com.github_stale.repo_clean.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(AppConfig.AppProperties.class)
public class AppConfig {

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("stale-branch-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    public WebMvcConfigurer corsConfigurer(AppProperties props) {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(props.getFrontendUrl())
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*").allowCredentials(false).maxAge(3600);
            }
        };
    }

    @Data
    @ConfigurationProperties(prefix = "app")
    public static class AppProperties {
        private String baseUrl = "http://localhost:8080";
        private String frontendUrl = "http://localhost:5173";
        private String tokenSecret = "changeme";
        private int tokenExpiryDays = 7;
        private int staleDays = 90;
        private String mailFrom = "noreply@yourorg.com";
        private String mailFromName = "Branch Cleanup Bot";
        private int deletionDelayHours = 12;
    }
}