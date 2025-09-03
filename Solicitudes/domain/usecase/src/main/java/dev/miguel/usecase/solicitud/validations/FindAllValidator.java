package dev.miguel.usecase.solicitud.validations;

import dev.miguel.model.utils.exception.ArgumentException;
import dev.miguel.usecase.solicitud.utils.ExceptionMessages;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

@AllArgsConstructor
public class FindAllValidator {

    public Mono<Void> validate (String correoElectronico, Long tipoPrestamoId, Long estadoId, Integer page, Integer size){
            return Flux.concat(
                    validateEmail(correoElectronico)
            ).collectList()
            .flatMap(errors -> {
                var real = errors.stream().filter(Objects::nonNull).toList();
                return real.isEmpty()
                        ? Mono.empty()
                        : Mono.error(new ArgumentException(real));
            });
    }

    private Mono<String> validateEmail(String email) {
        if (email == null) {
            return Mono.empty();
        }

        String value = email.trim();
        if (value.isEmpty()) {
            return Mono.just(ExceptionMessages.CAMPO_EMAIL_INVALIDO);
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            return Mono.just(ExceptionMessages.FORMATO_EMAIL_INVALIDO);
        }

        return Mono.empty();
    }

}
