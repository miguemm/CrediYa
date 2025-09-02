package dev.miguel.r2dbc.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Table("usuario")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    @Column("usuario_id")
    private Long id;

    private String nombres;

    private String apellidos;

    @Column("fecha_nacimiento")
    private LocalDate fechaNacimiento;

    private String direccion;
    
    private String telefono;

    @Column("correo_electronico")
    private String correoElectronico;

    private String contrasenia;

    @Column("salario_base")
    private BigDecimal salarioBase;

    @Column("rol_id")
    private Long rolId;

}
