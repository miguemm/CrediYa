package dev.miguel.model.utils.sqs;

public enum QueueAlias {
    SOLICITUD_ACTUALIZADA("solicitudActualizada"),
    CAPACIDAD_ENDEUDAMIENTO("capacidadEndeudamiento");

    private final String alias;

    QueueAlias(String alias) {
        this.alias = alias;
    }

    public String alias() {
        return alias;
    }
}
