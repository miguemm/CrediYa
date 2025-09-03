package dev.miguel.model.solicitud.proyections;

public interface findAllSolicitudes {
    Long getSolicitudId();
    java.math.BigDecimal getMonto();
    Integer getPlazo();
    String getCorreoElectronico();
    Long getEstadoId();
    Long getTipoPrestamoId();
    Long getUsuarioId();
}
