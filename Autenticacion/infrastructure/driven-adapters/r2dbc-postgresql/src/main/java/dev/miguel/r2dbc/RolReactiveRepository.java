package dev.miguel.r2dbc;

import dev.miguel.r2dbc.entity.RolEntity;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RolReactiveRepository extends ReactiveCrudRepository<RolEntity, Long >, ReactiveQueryByExampleExecutor<RolEntity> {

}
