package com.processmap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration
@EnableRetry
public class SpringAiConfig {
    // Retry capability registration for @Retryable execution
}
