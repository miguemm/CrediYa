package dev.miguel.sqs.listener;

import com.google.gson.Gson;
import dev.miguel.usecase.reporte.gateways.IReporteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.math.BigDecimal;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Log4j2
public class SQSProcessor implements Function<Message, Mono<Void>> {

     private final IReporteUseCase reporteUseCase;
    private final Gson gson = new Gson();

    @Override
    public Mono<Void> apply(Message message) {

        log.info("Message receive in the SQS Listener de Reportes {}", message.body());

        return Mono.fromCallable(() ->
                gson.fromJson(message.body(), BigDecimal.class)
            )
            .doOnNext(monto -> log.info("Solicitud parseada: {}", monto))
            .flatMap(monto -> {

                Mono<Void> repoteAprobadosCantidad = reporteUseCase.incrementarMetricaAprobados();
                Mono<Void> repoteAprobadosMonto = reporteUseCase.incrementarMetricaMonto(monto);

                return Mono.when(repoteAprobadosCantidad, repoteAprobadosMonto);
            })
            .then();
    }
}
