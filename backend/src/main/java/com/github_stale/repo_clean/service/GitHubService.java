package com.github_stale.repo_clean.service;

import com.github_stale.repo_clean.domain.Organisation;
import com.github_stale.repo_clean.domain.Repository;
import com.github_stale.repo_clean.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubService {

    private final GitHub gitHub;
    private final RepositoryRepository repositoryRepository;

    public GHOrganization getOrganisation(String orgName) throws IOException {
        return gitHub.getOrganization(orgName);
    }

    public List<GHRepository> listRepositories(GHOrganization ghOrg) throws IOException {
        return ghOrg.listRepositories(100).toList().stream()
                .filter(r -> !r.isArchived()).toList();
    }

    public List<GHRepository> listUserRepositories(String username) throws IOException {
        return gitHub.getUser(username).listRepositories(100).toList().stream()
                .filter(r -> !r.isArchived()).toList();
    }

    public Map<String, GHBranch> listBranches(GHRepository repo) throws IOException {
        return repo.getBranches();
    }

    public GHCommit getCommit(GHRepository repo, String sha) throws IOException {
        return repo.getCommit(sha);
    }

    public void deleteBranch(String repoFullName, String branchName) throws IOException {
        log.info("Deleting branch refs/heads/{} from {}", branchName, repoFullName);
        gitHub.getRepository(repoFullName).getRef("heads/" + branchName).delete();
    }

    @Transactional
    public Repository upsertRepository(GHRepository ghRepo, Organisation organisation) {
        return repositoryRepository.findByFullName(ghRepo.getFullName())
                .map(existing -> {
                    existing.setHtmlUrl(ghRepo.getHtmlUrl().toString());
                    existing.setArchived(ghRepo.isArchived());
                    return repositoryRepository.save(existing);
                })
                .orElseGet(() -> repositoryRepository.save(
                        Repository.builder()
                                .organisation(organisation)
                                .name(ghRepo.getName())
                                .fullName(ghRepo.getFullName())
                                .htmlUrl(ghRepo.getHtmlUrl().toString())
                                .archived(ghRepo.isArchived())
                                .build()));
    }
}