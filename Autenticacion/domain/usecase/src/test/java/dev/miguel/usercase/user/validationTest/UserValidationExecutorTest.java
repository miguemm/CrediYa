package dev.miguel.usercase.user.validationTest;

import dev.miguel.model.user.User;
import dev.miguel.usecase.exception.ValidationException;
import dev.miguel.usecase.user.validation.UserValidation;
import dev.miguel.usecase.user.validation.UserValidationExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserValidationExecutorTest {

    @Test
    void whenAllValidationsPass_shouldComplete() {
        User user = new User();

        UserValidation ok1 = u -> Mono.empty();
        UserValidation ok2 = u -> Mono.empty();

        StepVerifier.create(
                UserValidationExecutor.validateAll(user, List.of(ok1, ok2))
        ).verifyComplete();
    }

    @Test
    void whenOneValidationFails_shouldErrorWithSingleMessage() {
        User user = new User();

        UserValidation ok = u -> Mono.empty();
        UserValidation fail = u -> Mono.error(new IllegalArgumentException("msg-uno"));

        StepVerifier.create(
                UserValidationExecutor.validateAll(user, List.of(ok, fail))
        ).expectErrorSatisfies(t -> {
            assertTrue(t instanceof ValidationException);
            ValidationException ve = (ValidationException) t;
            assertEquals(List.of("msg-uno"), ve.getErrors());
        }).verify();
    }

    @Test
    void whenMultipleValidationsFail_shouldErrorWithAllMessages() {
        User user = new User();

        UserValidation fail1 = u -> Mono.error(new IllegalArgumentException("err-1"));
        UserValidation ok = u -> Mono.empty();
        UserValidation fail2 = u -> Mono.error(new IllegalArgumentException("err-2"));

        StepVerifier.create(
                UserValidationExecutor.validateAll(user, List.of(fail1, ok, fail2))
        ).expectErrorSatisfies(t -> {
            assertTrue(t instanceof ValidationException);
            var errors = ((ValidationException) t).getErrors();
            // No dependemos del orden por si en el futuro se cambia flatMap→parallel.
            assertTrue(errors.containsAll(List.of("err-1", "err-2")));
            assertEquals(2, errors.size());
        }).verify();
    }

    @Test
    void whenNoValidations_shouldComplete() {
        User user = new User();

        StepVerifier.create(
                UserValidationExecutor.validateAll(user, List.of())
        ).verifyComplete();
    }
}
