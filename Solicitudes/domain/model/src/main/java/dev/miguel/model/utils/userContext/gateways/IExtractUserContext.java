package dev.miguel.model.utils.userContext.gateways;

import dev.miguel.model.utils.userContext.UserContext;
import reactor.core.publisher.Mono;

public interface IExtractUserContext {
    Mono<UserContext> toUserContext(Mono<?> p);
}
