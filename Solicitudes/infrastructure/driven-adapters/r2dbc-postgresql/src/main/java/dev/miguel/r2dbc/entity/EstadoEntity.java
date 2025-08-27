package dev.miguel.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("estado")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class EstadoEntity {

    @Id
    @Column("estado_id")
    private Long id;

    private String nombre;

    private String descripcion;
}
