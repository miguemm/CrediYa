package dev.miguel.model.metrica;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class Metrica {
    private String metrica;
    private Integer cantidad;
}
