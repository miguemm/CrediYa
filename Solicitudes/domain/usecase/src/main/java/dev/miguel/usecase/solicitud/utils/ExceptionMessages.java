package dev.miguel.usecase.solicitud.utils;

public final class ExceptionMessages {

    private ExceptionMessages() {
        throw new AssertionError("No instances");
    }

    public static final String TIPO_PRESTAMO_NO_EXISTE = "El tipo de préstamo no existe";
    public static final String ESTADO_DE_LA_SOLICITUD_NO_EXISTE = "El estado de la solicitud no existe";
    public static final String CAMPO_PLAZO_INVALIDO = "El plazo debe ser un numero positivo mayor a 0";
    public static final String CAMPO_MONTO_INVALIDO = "El monto debe ser un numero positivo mayor a 0";
    public static final String CAMPO_EMAIL_INVALIDO = "El campo correo electrónico no puede ser nulo o vacío";
    public static final String FORMATO_EMAIL_INVALIDO = "El campo correo electrónico debe tener un formato de email válido";

}
