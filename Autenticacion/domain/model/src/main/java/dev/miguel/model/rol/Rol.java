package dev.miguel.model.rol;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class Rol {
    private Long id;
    private String nombre;
    private String descripcion;
}
