package dev.miguel.usecase.solicitud.validation;

import dev.miguel.model.exception.ArgumentException;
import dev.miguel.model.solicitud.Solicitud;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Objects;

@AllArgsConstructor
public class SolicitudValidator {

    private static final BigDecimal MONTO_MIN = BigDecimal.ZERO;

    public Mono<Void> validateAll (Solicitud solicitud){
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

}
