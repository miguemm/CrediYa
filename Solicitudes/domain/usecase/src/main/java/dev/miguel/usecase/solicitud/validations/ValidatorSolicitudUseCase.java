package dev.miguel.usecase.solicitud.validations;

import dev.miguel.model.utils.exceptions.ArgumentException;
import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.model.utils.exceptions.ExceptionMessages;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class ValidatorSolicitudUseCase {

    private static final BigDecimal MONTO_MIN = BigDecimal.ZERO;

    public Mono<Void> validateCreateBody(Solicitud solicitud){
            return Flux.concat(
                    validateMonto(solicitud),
                    validatePlazo(solicitud),
                    validateEmail(solicitud)
            ).collectList()
            .flatMap(errors -> {
                var real = errors.stream().filter(Objects::nonNull).toList();
                return real.isEmpty()
                        ? Mono.empty()
                        : Mono.error(new ArgumentException(real));
            });
    }

    private Mono<String> validateMonto (Solicitud solicitud) {
        return (solicitud.getMonto() == null || solicitud.getMonto().compareTo(MONTO_MIN) <= 0)
                ? Mono.just(ExceptionMessages.CAMPO_MONTO_INVALIDO)
                : Mono.empty();
    }

    private Mono<String> validatePlazo (Solicitud solicitud) {
        return (solicitud.getPlazo() == null || solicitud.getPlazo() <= 0)
                ? Mono.just(ExceptionMessages.CAMPO_PLAZO_INVALIDO)
                : Mono.empty();
    }

    private Mono<String> validateEmail(Solicitud solicitud) {
        if (solicitud.getCorreoElectronico() == null || solicitud.getCorreoElectronico().isEmpty()) {
            return Mono.just(ExceptionMessages.CAMPO_EMAIL_INVALIDO);
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!solicitud.getCorreoElectronico().matches(emailRegex)) {
            return Mono.just(ExceptionMessages.FORMATO_EMAIL_INVALIDO);
        }

        return Mono.empty();
    }

    public Mono<Void> validateFindAll (String correoElectronico, Long tipoPrestamoId, Long estadoId, Integer page, Integer size){
        return Flux.concat(
                        validateFindAllEmail(correoElectronico)
                ).collectList()
                .flatMap(errors -> {
                    var real = errors.stream().filter(Objects::nonNull).toList();
                    return real.isEmpty()
                            ? Mono.empty()
                            : Mono.error(new ArgumentException(real));
                });
    }

    private Mono<String> validateFindAllEmail(String email) {
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
