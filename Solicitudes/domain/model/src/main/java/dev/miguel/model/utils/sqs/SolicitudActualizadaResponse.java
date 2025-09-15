package dev.miguel.model.utils.sqs;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class SolicitudActualizadaResponse {

    private Long solicitudId;
    private Long estadoId;

}
