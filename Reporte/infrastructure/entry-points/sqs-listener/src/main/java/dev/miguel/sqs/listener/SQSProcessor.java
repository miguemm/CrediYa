package dev.miguel.sqs.listener;

import dev.miguel.usecase.reporte.gateways.IReporteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SQSProcessor implements Function<Message, Mono<Void>> {

     private final IReporteUseCase reporteUseCase;

    @Override
    public Mono<Void> apply(Message message) {

        System.out.println(message.body());

        return reporteUseCase.incrementar();
    }
}
