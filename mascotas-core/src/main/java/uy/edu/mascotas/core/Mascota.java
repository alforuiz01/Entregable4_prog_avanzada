package uy.edu.mascotas.core;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Mascota encontrada y registrada para adopcion. */
@Entity
@Table(name = "mascotas")
public class Mascota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Especie especie;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Genero genero;

    /** Edad aproximada en anios. */
    private int edad;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 500)
    private String fotoUrl;

    private LocalDate fechaRegistro;

    private boolean adoptada;

    protected Mascota() { }

    public Mascota(String nombre, Especie especie, Genero genero, int edad,
                   String descripcion, String fotoUrl) {
        this.nombre = nombre;
        this.especie = especie;
        this.genero = genero;
        this.edad = edad;
        this.descripcion = descripcion;
        this.fotoUrl = fotoUrl;
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public Especie getEspecie() { return especie; }
    public Genero getGenero() { return genero; }
    public int getEdad() { return edad; }
    public String getDescripcion() { return descripcion; }
    public String getFotoUrl() { return fotoUrl; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public boolean isAdoptada() { return adoptada; }

    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    public void setAdoptada(boolean adoptada) { this.adoptada = adoptada; }
}
