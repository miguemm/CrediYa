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

    @Override
    public Mono<String> send(SQSMessage message) {
        return Mono.fromCallable(() -> buildRequest(new Gson().toJson(message)))
                .flatMap(request -> Mono.fromFuture(client.sendMessage(request)))
                .doOnNext(response -> log.debug("Message sent {}", response.messageId()))
                .map(SendMessageResponse::messageId);
    }

    private SendMessageRequest buildRequest(String message) {
        return SendMessageRequest.builder()
                .queueUrl(properties.queueUrl())
                .messageBody(message)
                .build();
    }
}
