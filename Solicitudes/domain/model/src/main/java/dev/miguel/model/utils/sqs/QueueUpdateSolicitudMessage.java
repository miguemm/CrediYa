package dev.miguel.model.utils.sqs;

import dev.miguel.model.solicitud.proyections.SolicitudDto;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class QueueUpdateSolicitudMessage {
    Long solicitudId;
    String correoElectronico;
    String estado;
    BigDecimal monto;
    Integer plazo;
    double tasaInteres;
    List<SolicitudDto> solicitudesActivas;
}
