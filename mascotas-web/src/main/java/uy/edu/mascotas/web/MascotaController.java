package uy.edu.mascotas.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import uy.edu.mascotas.core.*;

@RestController
@RequestMapping("/api/mascotas")
public class MascotaController {

    private final MascotaService service;

    public MascotaController(MascotaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mascota registrar(@Valid @RequestBody MascotaRequest request) {
        return service.registrar(request.toEntity());
    }

    @GetMapping
    public List<Mascota> buscar(@RequestParam(required = false) Especie especie,
                                @RequestParam(required = false) Genero genero,
                                @RequestParam(required = false) Integer edadMin,
                                @RequestParam(required = false) Integer edadMax) {
        return service.buscar(especie, genero, edadMin, edadMax);
    }

    @GetMapping("/{id}")
    public Mascota obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/adoptar")
    public Mascota adoptar(@PathVariable Long id) {
        return service.adoptar(id);
    }
}
