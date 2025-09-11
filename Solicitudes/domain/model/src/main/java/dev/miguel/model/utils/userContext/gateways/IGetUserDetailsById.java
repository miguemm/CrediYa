package dev.miguel.model.utils.userContext.gateways;

import dev.miguel.model.utils.userContext.UserDetails;
import reactor.core.publisher.Mono;

public interface IGetUserDetailsById {
    Mono<UserDetails> getUserDetailsById(Long userId);
}
