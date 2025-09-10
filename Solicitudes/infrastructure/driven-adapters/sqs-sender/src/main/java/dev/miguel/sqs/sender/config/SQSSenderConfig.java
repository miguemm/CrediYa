package dev.miguel.sqs.sender.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
@ConditionalOnMissingBean(SqsAsyncClient.class)
public class SQSSenderConfig {

    @Bean
    public SqsAsyncClient configSqs(SQSSenderProperties properties, MetricPublisher publisher) {
        return SqsAsyncClient.builder()
                .endpointOverride(resolveEndpoint(properties))
                .region(Region.of(properties.region()))
                .overrideConfiguration(o -> o.addMetricPublisher(publisher))
                .credentialsProvider(getProviderChain(properties))
                .build();
    }

    private AwsCredentialsProviderChain getProviderChain(SQSSenderProperties properties) {
        AwsCredentialsProviderChain.Builder chain = AwsCredentialsProviderChain.builder();

        if (properties.accessKey() != null && properties.secretKey() != null) {
            chain.addCredentialsProvider(
                    StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
                    )
            );
        }

        chain.addCredentialsProvider(EnvironmentVariableCredentialsProvider.create());
        chain.addCredentialsProvider(SystemPropertyCredentialsProvider.create());
        chain.addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create());
        chain.addCredentialsProvider(ProfileCredentialsProvider.create());
        chain.addCredentialsProvider(ContainerCredentialsProvider.builder().build());
        chain.addCredentialsProvider(InstanceProfileCredentialsProvider.create());

        return chain.build();
    }

    private URI resolveEndpoint(SQSSenderProperties properties) {
        if (properties.endpoint() != null) {
            return URI.create(properties.endpoint());
        }
        return null;
    }
}
