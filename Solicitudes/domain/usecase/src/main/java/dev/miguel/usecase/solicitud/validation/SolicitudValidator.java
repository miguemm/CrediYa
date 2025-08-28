package dev.miguel.usecase.solicitud.validation;

import dev.miguel.model.solicitud.Solicitud;
import dev.miguel.usecase.exception.ArgumentException;
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
        return (solicitud.getMonto() == null || solicitud.getMonto().compareTo(MONTO_MIN) < 0)
                ? Mono.just("El monto de la solicitud no es valido")
                : Mono.empty();
    }

    private Mono<String> validatePlazo (Solicitud solicitud) {
        return (solicitud.getPlazo() == null || solicitud.getPlazo() <= 0)
                ? Mono.just("El monto deber ser un numero positivo")
                : Mono.empty();
    }

    private Mono<String> validateEmail(Solicitud solicitud) {
        if (solicitud.getCorreoElectronico() == null || solicitud.getCorreoElectronico().isEmpty()) {
            return Mono.just("El campo correo_electronico no puede ser nulo o vacío");
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!solicitud.getCorreoElectronico().matches(emailRegex)) {
            return Mono.just("El campo correo_electronico debe tener un formato de email válido");
        }

        return Mono.empty();
    }

}
