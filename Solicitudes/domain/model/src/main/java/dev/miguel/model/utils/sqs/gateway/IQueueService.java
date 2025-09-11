package dev.miguel.model.utils.sqs.gateway;

import reactor.core.publisher.Mono;

public interface IQueueService {

    <T> Mono<String> send(String queueAlias, T message);
}
