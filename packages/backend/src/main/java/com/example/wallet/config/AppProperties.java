package com.example.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        boolean demoMode,
        List<String> corsAllowedOrigins,
        boolean trustedProxy
) {
}
