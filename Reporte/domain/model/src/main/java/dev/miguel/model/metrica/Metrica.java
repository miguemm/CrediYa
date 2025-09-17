package dev.miguel.model.metrica;
import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class Metrica {
    private String metrica;
    private Integer cantidad;
    private BigDecimal monto;
}
