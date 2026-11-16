package uy.edu.mascotas.core;

/** Violacion de una regla de negocio al registrar o modificar una mascota. */
public class MascotaInvalidaException extends RuntimeException {
    public MascotaInvalidaException(String mensaje) { super(mensaje); }
}
