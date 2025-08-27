package dev.miguel.r2dbc;

import dev.miguel.r2dbc.entity.UserEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface UserReactiveRepository extends ReactiveCrudRepository<UserEntity, Long >, ReactiveQueryByExampleExecutor<UserEntity> {

}
