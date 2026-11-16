package uy.edu.mascotas.core;

public class MascotaNoEncontradaException extends RuntimeException {
    public MascotaNoEncontradaException(Long id) {
        super("No existe la mascota con id " + id);
    }
}
