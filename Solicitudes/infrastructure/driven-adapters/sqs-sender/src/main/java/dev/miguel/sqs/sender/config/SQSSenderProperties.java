package dev.miguel.sqs.sender.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "sqs")
public record SQSSenderProperties(
     String region,
     String queueUrl,
     String endpoint,
     String accessKey,
     String secretKey
){}
