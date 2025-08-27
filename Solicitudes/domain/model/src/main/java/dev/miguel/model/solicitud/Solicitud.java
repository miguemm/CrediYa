package dev.miguel.model.solicitud;
import dev.miguel.model.estado.Estado;
import dev.miguel.model.tipoprestamo.TipoPrestamo;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class Solicitud {

    private BigDecimal monto;
    private int plazo;
    private String correoElectronico;
    private Estado estado;
    private TipoPrestamo tipoPrestamo;
}
