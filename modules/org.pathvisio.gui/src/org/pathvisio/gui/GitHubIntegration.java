package org.pathvisio.gui;

import org.kohsuke.github.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GitHubIntegration {
    private GitHub github;

    public GitHubIntegration(String token) throws Exception {
        github = GitHub.connectUsingOAuth(token);
    }

    public GitHubIntegration() throws Exception {
        // Load token from a properties file
        String token = loadTokenFromProperties();
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("GitHub token not found in config.properties");
        }
        github = GitHub.connectUsingOAuth(token);
    }

    private String loadTokenFromProperties() {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            return properties.getProperty("github.token");
        } catch (IOException e) {
            System.err.println("Failed to load GitHub token from config.properties: " + e.getMessage());
            return null;
        }
    }

    public void saveToGitHub(String repoName, String filePath, String content, String commitMessage) throws Exception {
        GHRepository repo = github.getRepository(repoName);
        repo.createContent(content, commitMessage, filePath);
    }
}
