package dev.miguel.model.tipoprestamo;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class TipoPrestamo {

    private Long id;
    private String nombre;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private double tasaInteres;
    private boolean validacionAutomatica;
}
