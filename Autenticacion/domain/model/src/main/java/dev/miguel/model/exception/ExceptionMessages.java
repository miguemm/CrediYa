package dev.miguel.model.exception;

public final class ExceptionMessages {

    private ExceptionMessages() {
        throw new AssertionError("No instances");
    }

    public static final String CORREO_YA_EXISTE = "Correo ya existe";
    public static final String ROL_NO_EXISTE = "Rol no existe";
    public static final String CAMPO_NOMBRE_INVALIDO = "El campo nombre no puede ser nulo o vacío";
    public static final String CAMPO_APELLIDO_INVALIDO = "El campo apellido no puede ser nulo o vacío";
    public static final String CAMPO_SALARIO_INVALIDO = "El salario base debe estar entre 0 y 15.000.000";
    public static final String CAMPO_EMAIL_INVALIDO = "El campo correo electrónico no puede ser nulo o vacío";
    public static final String FORMATO_EMAIL_INVALIDO = "El campo correo electrónico debe tener un formato de email válido";
    public static final String CAMPO_CONTRASENIA_INVALIDO = "El campo contraseña no puede ser nulo o vacío";

    public static final String USUARIO_CORREO_NO_EXISTE = "El correo no esta registrado.";
    public static final String USUARIO_CONTRASENIA_INCORRECTA = "Contraseña incorrecta.";

    public static final String USUARIO_ID = "El usuario no esta registrado.";
}
