package dev.miguel.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("solicitud")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SolicitudEntity {

    @Id
    @Column("solicitud_id")
    private Long id;

    private BigDecimal monto;

    private int plazo;

    @Column("correo_electronico")
    private String correoElectronico;

    @Column("estado_id")
    private Long estadoId;

    @Column("tipo_prestamo_id")
    private Long tipoPrestamoId;

    @Column("usuario_id")
    private Long usuarioId;
}
