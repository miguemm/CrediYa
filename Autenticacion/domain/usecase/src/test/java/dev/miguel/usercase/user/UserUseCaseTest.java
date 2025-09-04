package dev.miguel.usercase.user;

import dev.miguel.model.rol.gateways.RolRepository;
import dev.miguel.model.user.Token;
import dev.miguel.model.user.User;
import dev.miguel.model.user.gateways.ISecurityProvider;
import dev.miguel.model.user.gateways.UserRepository;
import dev.miguel.model.utils.exceptions.BusinessException;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import dev.miguel.model.utils.exceptions.UnauthorizedException;
import dev.miguel.model.utils.userContext.UserDetails;
import dev.miguel.usecase.user.UserUseCase;
import dev.miguel.usecase.user.validation.ValidatorUserUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @InjectMocks
    UserUseCase useCase;

    @Mock
    ValidatorUserUseCase validator;
    @Mock
    UserRepository userRepository;
    @Mock
    RolRepository rolRepository;
    @Mock
    ISecurityProvider securityProvider;

    // Helpers
    private User validUser() {
        return User.builder()
                .nombres("Miguel")
                .apellidos("Mosquera")
                .salarioBase(new BigDecimal("2500000"))
                .correoElectronico("miguel@test.com")
                .contrasenia("12345")
                .rolId(1L)
                .build();
    }

    private UserDetails validUserDetails() {
        return UserDetails.builder()
                .nombres("Miguel")
                .apellidos("Mosquera")
                .salarioBase(new BigDecimal("2500000"))
                .correoElectronico("miguel@test.com")
                .build();
    }

    @Nested
    @DisplayName("Create")
    class create {

        @Test
        void createUser_ok() {
            var input = validUser();
            var encrypted = "ENC-123";

            when(validator.validateCreateBody(input))
                    .thenReturn(Mono.empty());

            when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                    .thenReturn(Mono.empty());

            when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                    .thenReturn(Mono.empty());

            when(rolRepository.existsById(input.getRolId()))
                    .thenReturn(Mono.just(true));

            when(securityProvider.encryptPassword(input))
                    .thenReturn(Mono.just(encrypted));

            when(userRepository.saveUser(input))
                    .thenReturn(Mono.just(input));

            StepVerifier.create(useCase.createUser(input))
                    .verifyComplete();
        }

        @Test
        void createUser_error_whenEmailExists_throwsBusinessException() {
            var input = validUser();
            var existing = validUser();

            when(validator.validateCreateBody(input))
                    .thenReturn(Mono.empty());

            when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                    .thenReturn(Mono.just(existing));

            var result = useCase.createUser(input);

            StepVerifier.create(result)
                    .expectError(BusinessException.class)
                    .verify();
        }

        @Test
        void createUser_error_whenRolDoesntExists_throwsBusinessException() {
            var input = validUser();

            when(validator.validateCreateBody(input))
                    .thenReturn(Mono.empty());

            when(userRepository.findUserByEmail(input.getCorreoElectronico()))
                    .thenReturn(Mono.empty());

            when(rolRepository.existsById(input.getRolId()))
                    .thenReturn(Mono.just(false));

            var result = useCase.createUser(input);

            StepVerifier.create(result)
                    .expectError(BusinessException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("getUserById")
    class getUserById {

        @Test
        void getUserById_ok() {
            var output = validUserDetails();

            when(userRepository.findUserById(1L))
                    .thenReturn(Mono.just(output));

            StepVerifier.create(useCase.getUserById(1L))
                    .expectNext(output)
                    .verifyComplete();
        }

        @Test
        void getUserById_notFound() {

            when(userRepository.findUserById(0L))
                    .thenReturn(Mono.empty());

            var result = useCase.getUserById(0L);

            StepVerifier.create(result)
                    .expectError(BusinessException.class)
                    .verify();
        }

    }

    @Nested
    @DisplayName("login")
    class loginTests {

            @Test
            @DisplayName("debe autenticar y devolver token cuando email existe y contraseña es válida")
            void login_ok() {
                String email = "test@example.com";
                String rawPassword = "secret";
                User user = validUser();
                Token token = new Token("jwt-123");

                when(userRepository.findUserByEmail(email)).thenReturn(Mono.just(user));
                when(securityProvider.validatePassword(user, rawPassword)).thenReturn(Mono.just(true));
                when(securityProvider.generateToken(user)).thenReturn(Mono.just(token));

                StepVerifier.create(useCase.login(email, rawPassword))
                        .expectNext(token)
                        .verifyComplete();
            }

            @Test
            @DisplayName("debe fallar con Unauthorized cuando el correo no existe")
            void login_fail_email_no_existe() {
                String email = "missing@example.com";
                String rawPassword = "secret";

                when(userRepository.findUserByEmail(email)).thenReturn(Mono.empty());

                StepVerifier.create(useCase.login(email, rawPassword))
                        .expectErrorSatisfies(err -> {
                            assertTrue(err instanceof UnauthorizedException);
                            assertTrue(err.getMessage().contains(ExceptionMessages.USUARIO_CORREO_NO_EXISTE));
                        })
                        .verify();
            }

            @Test
            @DisplayName("debe fallar con Unauthorized cuando la contraseña es incorrecta")
            void login_fail_password_incorrecta() {
                String email = "test@example.com";
                String rawPassword = "badpass";
                User user = validUser();

                when(userRepository.findUserByEmail(email)).thenReturn(Mono.just(user));
                when(securityProvider.validatePassword(user, rawPassword)).thenReturn(Mono.just(false));

                StepVerifier.create(useCase.login(email, rawPassword))
                        .expectErrorSatisfies(err -> {
                            assertTrue(err instanceof UnauthorizedException);
                            assertTrue(err.getMessage().contains(ExceptionMessages.USUARIO_CONTRASENIA_INCORRECTA));
                        })
                        .verify();

            }

            @Test
            @DisplayName("propaga error si validatePassword emite error")
            void login_fail_validatePassword_error() {
                String email = "test@example.com";
                String rawPassword = "secret";
                User user = validUser();
                RuntimeException cause = new RuntimeException("crypto down");

                when(userRepository.findUserByEmail(email)).thenReturn(Mono.just(user));
                when(securityProvider.validatePassword(user, rawPassword)).thenReturn(Mono.error(cause));

                StepVerifier.create(useCase.login(email, rawPassword))
                        .expectErrorMatches(t -> t == cause)
                        .verify();
            }

            @Test
            @DisplayName("propaga error si generateToken emite error")
            void login_fail_generateToken_error() {
                String email = "test@example.com";
                String rawPassword = "secret";
                User user = validUser();
                RuntimeException cause = new RuntimeException("jwt service down");

                when(userRepository.findUserByEmail(email)).thenReturn(Mono.just(user));
                when(securityProvider.validatePassword(user, rawPassword)).thenReturn(Mono.just(true));
                when(securityProvider.generateToken(user)).thenReturn(Mono.error(cause));

                StepVerifier.create(useCase.login(email, rawPassword))
                        .expectErrorMatches(t -> t == cause)
                        .verify();

            }

            // Helper
            private User validUser() {
                var u = new User();
                u.setId(1L);
                u.setNombres("Juan");
                u.setApellidos("Pérez");
                u.setCorreoElectronico("test@example.com");
                u.setContrasenia("hash");
                u.setRolId(1L);
                return u;
            }

    }

}
