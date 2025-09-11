package dev.miguel.model.utils.sqs.gateway;

import dev.miguel.model.utils.sqs.SQSMessage;
import reactor.core.publisher.Mono;

public interface ISQSService {

    Mono<String> send(String queueAlias, SQSMessage message);
}
