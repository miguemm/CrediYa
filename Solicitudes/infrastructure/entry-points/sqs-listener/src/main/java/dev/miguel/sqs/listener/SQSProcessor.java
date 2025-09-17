package dev.miguel.sqs.listener;

import com.google.gson.Gson;
import dev.miguel.model.utils.sqs.SolicitudActualizadaResponse;
import dev.miguel.usecase.solicitud.gateways.ISolicitudUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class SQSProcessor implements Function<Message, Mono<Void>> {

    private final ISolicitudUseCase solicitudUseCase;
    private final Gson gson = new Gson();

    @Override
    public Mono<Void> apply(Message message) {
        log.info("Message receive in the SQS Listener de Solicitudes{}", message.body());

        return Mono.fromCallable(() ->
                        gson.fromJson(message.body(), SolicitudActualizadaResponse.class)
                )
                .doOnNext(solicitud -> log.info("Solicitud parseada: {}", solicitud))
                .flatMap(solicitud ->
                        solicitudUseCase.updateSolicitud(solicitud.getSolicitudId(), solicitud.getEstadoId())
                )
                .then();
    }
}
