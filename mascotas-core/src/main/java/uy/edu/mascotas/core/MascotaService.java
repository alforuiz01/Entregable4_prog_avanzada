package uy.edu.mascotas.core;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class MascotaService {

    public static final int EDAD_MAXIMA = 40;
    public static final String FOTO_POR_DEFECTO = "/img/sin-foto.svg";

    private final MascotaRepository repository;

    public MascotaService(MascotaRepository repository) {
        this.repository = repository;
    }

    public Mascota registrar(Mascota mascota) {
        if (mascota.getNombre() == null || mascota.getNombre().isBlank()) {
            throw new MascotaInvalidaException("El nombre es obligatorio");
        }
        if (mascota.getEdad() < 0 || mascota.getEdad() > EDAD_MAXIMA) {
            throw new MascotaInvalidaException("La edad debe estar entre 0 y " + EDAD_MAXIMA);
        }
        if (mascota.getFotoUrl() == null || mascota.getFotoUrl().isBlank()) {
            mascota.setFotoUrl(FOTO_POR_DEFECTO);
        }
        mascota.setFechaRegistro(LocalDate.now());
        mascota.setAdoptada(false);
        return repository.save(mascota);
    }

    public Mascota obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new MascotaNoEncontradaException(id));
    }

    public List<Mascota> buscar(Especie especie, Genero genero, Integer edadMin, Integer edadMax) {
        return repository.findAll().stream()
                .filter(m -> !m.isAdoptada())
                .filter(m -> especie == null || m.getEspecie() == especie)
                .filter(m -> genero == null || m.getGenero() == genero)
                .filter(m -> edadMin == null || m.getEdad() >= edadMin)
                .filter(m -> edadMax == null || m.getEdad() <= edadMax)
                .toList();
    }

    public Mascota adoptar(Long id) {
        Mascota m = obtener(id);
        if (m.isAdoptada()) {
            throw new MascotaInvalidaException("La mascota ya fue adoptada");
        }
        m.setAdoptada(true);
        return repository.save(m);
    }
}
