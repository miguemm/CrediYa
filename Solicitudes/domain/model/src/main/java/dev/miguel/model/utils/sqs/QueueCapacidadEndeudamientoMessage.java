package dev.miguel.model.utils.sqs;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class QueueCapacidadEndeudamientoMessage {
    Long solicitudId;
    String correoElectronico;
    BigDecimal salarioBase;
}
