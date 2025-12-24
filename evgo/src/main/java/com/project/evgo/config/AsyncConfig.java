package com.project.evgo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Async configuration to enable @Async annotation for async email sending.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}
