package dev.miguel.model.utils.sqs;

public enum QueueAlias {
    SOLICITUD_ACTUALIZADA("solicitudActualizada"),
    CAPACIDAD_ENDEUDAMIENTO("capacidadEndeudamiento"),
    REPORTE_SOLICITUD_APROBADA("solicitudAprobada");

    private final String alias;

    QueueAlias(String alias) {
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }
}
