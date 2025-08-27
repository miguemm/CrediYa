package dev.miguel.model.tipoprestamo;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class TipoPrestamo {

    private String nombre;
    private BigDecimal montoMinimo;
    private BigDecimal montoMaximo;
    private double tasaInteres;
    private boolean validacionAutomatica;
}
