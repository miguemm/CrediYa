package dev.miguel.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "sqs")
public record SQSSenderProperties(
     String region,
     String endpoint,
     Map<String, String> queues
){}
