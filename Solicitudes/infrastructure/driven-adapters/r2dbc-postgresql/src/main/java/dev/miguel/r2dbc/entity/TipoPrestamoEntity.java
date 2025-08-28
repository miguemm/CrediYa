package dev.miguel.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("tipo_prestamo")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class TipoPrestamoEntity {

    @Id
    @Column("tipo_prestamo_id")
    private Long id;

    private String nombre;

    @Column("monto_minimo")
    private BigDecimal montoMinimo;

    @Column("monto_maximo")
    private BigDecimal montoMaximo;

    @Column("tasa_interes")
    private double tasaInteres;

    @Column("validacion_automatica")
    private boolean validacionAutomatica;

}
