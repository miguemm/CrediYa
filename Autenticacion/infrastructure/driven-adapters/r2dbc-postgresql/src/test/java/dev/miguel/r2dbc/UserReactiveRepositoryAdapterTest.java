package dev.miguel.r2dbc;

import dev.miguel.model.user.User;
import dev.miguel.model.utils.userContext.UserDetails;
import dev.miguel.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    @InjectMocks
    UserRepositoryAdapter userRepositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private final UserEntity userEntity = UserEntity.builder()
            .id(1L)
            .nombres("Miguel")
            .apellidos("Mosquera")
            .fechaNacimiento(LocalDate.of(1996, 7, 12))
            .direccion("Cll 15 # 17a - 115")
            .telefono("+573225864404")
            .correoElectronico("mandresmosquera@gmail.com")
            .salarioBase(BigDecimal.valueOf(10000000))
            .build();

    private final User user = User.builder()
            .id(1L)
            .nombres("Miguel")
            .apellidos("Mosquera")
            .fechaNacimiento(LocalDate.of(1996, 7, 12))
            .direccion("Cll 15 # 17a - 115")
            .telefono("+573225864404")
            .correoElectronico("mandresmosquera@gmail.com")
            .salarioBase(BigDecimal.valueOf(10000000))
            .build();


    @Test
    void findUserById_whenEmpty_thenCompleteWithoutValue() {
        when(repository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(userRepositoryAdapter.findUserById(999L))
                .verifyComplete();

    }


    @Test
    void shouldSaveUser() {
        when(mapper.map(user, UserEntity.class)).thenReturn(userEntity);
        when(repository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(mapper.map(userEntity, User.class)).thenReturn(user);

        Mono<User> result = userRepositoryAdapter.saveUser(user);

        StepVerifier.create(result)
                .expectNext(user)
                .verifyComplete();
    }

    @Test
    void shouldFindUserByEmail() {
        // Arrange: preparamos un email de búsqueda
        String email = "mandresmosquera@gmail.com";

        when(mapper.map(any(User.class), eq(UserEntity.class)))
                .thenAnswer(inv -> {
                    User src = inv.getArgument(0);
                    return UserEntity.builder().correoElectronico(src.getCorreoElectronico()).build();
                });
        when(mapper.map(userEntity, User.class)).thenReturn(user);
        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(userEntity));


        Mono<User> result = userRepositoryAdapter.findUserByEmail(email);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(found ->
                        found.getId().equals(1L) &&
                                found.getCorreoElectronico().equals("mandresmosquera@gmail.com")
                )
                .verifyComplete();
    }


}
