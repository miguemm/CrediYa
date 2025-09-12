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
public class QueueCapacidadEndeudamientoMessage {
    BigDecimal monto;
    Integer plazo;
    double tasaInteres;
    String correoElectronico;
    String nombreCompleto;
    BigDecimal ingresosTotales;
    List<SolicitudDto> solicitudesActivas;
}
