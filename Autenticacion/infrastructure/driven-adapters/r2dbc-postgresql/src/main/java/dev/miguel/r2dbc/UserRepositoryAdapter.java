package dev.miguel.r2dbc;

import dev.miguel.model.usuario.User;
import dev.miguel.model.usuario.gateways.UserRepository;
import dev.miguel.r2dbc.entity.UserEntity;
import dev.miguel.r2dbc.helper.ReactiveAdapterOperations;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class UserRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        Long,
        UserReactiveRepository
> implements UserRepository {

    public UserRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        super(repository, mapper, entity -> mapper.map(entity, User.class));
    }

    @Override
    public Mono<User> saveUser(User user) {
        return super.save(user);
    }

    @Override
    public Mono<User> findUserById(Long id) {
        return super.findById(id);
    }

}
