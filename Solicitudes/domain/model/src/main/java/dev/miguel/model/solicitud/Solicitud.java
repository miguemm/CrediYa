package dev.miguel.model.solicitud;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Solicitud {

    private Long id;

    private BigDecimal monto;

    private Integer plazo;

    private String correoElectronico;

    private Long estadoId;

    private Long tipoPrestamoId;

    private Long usuarioId;
}
