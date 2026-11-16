package uy.edu.mascotas.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import uy.edu.mascotas.core.Especie;
import uy.edu.mascotas.core.Genero;
import uy.edu.mascotas.core.Mascota;

public record MascotaRequest(
        @NotBlank String nombre,
        @NotNull Especie especie,
        @NotNull Genero genero,
        int edad,
        String descripcion,
        String fotoUrl) {

    public Mascota toEntity() {
        return new Mascota(nombre, especie, genero, edad, descripcion, fotoUrl);
    }
}
