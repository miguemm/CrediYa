package dev.miguel.model.estado;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class Estado {

    private Long id;
    private String nombre;
    private String descripcion;
}
