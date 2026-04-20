package com.payable.demotest.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * GitHub MCP Tools service.
 * Provides tools for interacting with GitHub via MCP.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GitHubMcpTools {

    @Value("${github.token}")
    private String githubToken;

    private final RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        headers.set("Accept", "application/vnd.github.v3+json");
        return headers;
    }

    @Tool(description = "List all repositories for the authenticated user")
    public List<Map<String, Object>> listMyRepositories() {
        log.info("MCP Tool: listMyRepositories called");
        String url = "https://api.github.com/user/repos";
        
        try {
            ResponseEntity<List> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders()),
                    List.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error listing repositories: {}", e.getMessage());
            return List.of(Map.of("error", e.getMessage()));
        }
    }

    @Tool(description = "Get repository details by owner and name")
    public Map<String, Object> getRepositoryDetails(String owner, String repo) {
        log.info("MCP Tool: getRepositoryDetails called for {}/{}", owner, repo);
        String url = String.format("https://api.github.com/repos/%s/%s", owner, repo);
        
        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(getHeaders()),
                    Map.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting repository details: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        }
    }
}
