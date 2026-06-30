package com.processmap.ai.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@Configuration
@Slf4j
public class GeminiStartupValidator {

    @Value("${spring.ai.google.genai.api-key:}")
    private String apiKey;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void validate() {
        if (isTestEnvironment()) {
            log.info("Skipping Gemini API key validation in test context");
            return;
        }
        if (apiKey == null || apiKey.trim().isEmpty() || "mock-key".equalsIgnoreCase(apiKey.trim()) || "mock-test-key".equalsIgnoreCase(apiKey.trim())) {
            log.error("CRITICAL STARTUP ERROR: GEMINI_API_KEY environment variable is missing, blank, or set to mock-key! Application startup aborted.");
            throw new IllegalStateException("CRITICAL STARTUP ERROR: GEMINI_API_KEY environment variable is missing or blank. Please configure a valid Google AI Studio API key.");
        }
        log.info("Gemini Provider Initialized");
    }

    private boolean isTestEnvironment() {
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            return true;
        }
        if (environment.containsProperty("org.springframework.boot.test.context.SpringBootTestContextBootstrapper")) {
            return true;
        }
        // Check if any class in the stacktrace is JUnit, TestNG, or Surefire
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (className.startsWith("org.junit.") || 
                className.startsWith("org.testng.") || 
                className.startsWith("org.apache.maven.surefire.")) {
                return true;
            }
        }
        return false;
    }
}
