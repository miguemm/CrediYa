package dev.miguel.usercase.user;

import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.usecase.user.UserUseCase;
import dev.miguel.usecase.user.validation.UserValidationExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @InjectMocks
    UserUseCase userUseCase;

    @Mock
    UserRepository userRepository;

    private static User sample() {
        return User.builder()
                .id(1L)
                .nombres("Miguel")
                .apellidos("Mosquera")
                .correoElectronico("miguel@mail.com")
                .salarioBase(new BigDecimal("1000000"))
                .build();
    }

    @Test
    void createUser_success() {
        User input = sample();
        UserUseCase useCase = new UserUseCase(userRepository);

        try (MockedStatic<UserValidationExecutor> mocked = mockStatic(UserValidationExecutor.class)) {

            mocked.when(() -> UserValidationExecutor.validateAll(any(), any()))
                    .thenReturn(Mono.empty());

            when(userRepository.saveUser(input)).thenReturn(Mono.just(input));

            StepVerifier.create(useCase.createUser(input))
                    .expectNext(input)
                    .verifyComplete();

            mocked.verify(() -> UserValidationExecutor.validateAll(eq(input), any()), times(1));
            verify(userRepository).saveUser(input);
            verifyNoMoreInteractions(userRepository);
        }
    }

}
