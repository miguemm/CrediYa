package dev.miguel.sqs.sender;

import com.google.gson.Gson;
import dev.miguel.model.utils.sqs.SQSMessage;
import dev.miguel.model.utils.sqs.gateway.ISQSService;
import dev.miguel.sqs.sender.config.SQSSenderProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Service
@Log4j2
@RequiredArgsConstructor
public class SQSSender implements ISQSService {

    private final SQSSenderProperties properties;
    private final SqsAsyncClient client;
    private final Gson gson = new Gson();

    @Override
    public Mono<String> send(String queueAlias, SQSMessage message) {
        String queueUrl = properties.queues().get(queueAlias);
        if (queueUrl == null) {
            return Mono.error(new IllegalArgumentException("Alias de cola no configurado: " + queueAlias));
        }

        return Mono.fromCallable(() -> buildRequest(queueUrl, gson.toJson(message)))
                .flatMap(req -> Mono.fromFuture(client.sendMessage(req)))
                .doOnNext(resp -> log.debug("Message sent to {} id={}", queueAlias, resp.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String queueUrl, String body) {
        return SendMessageRequest.builder()
                .queueUrl(queueUrl)
                .messageBody(body)
                .build();
    }
}
