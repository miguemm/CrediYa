package dev.miguel.model.metrica;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class ReporteMetricas {
    private Integer aprobadosCantidad;
    private BigDecimal aprobadosMontoTotal;
}
