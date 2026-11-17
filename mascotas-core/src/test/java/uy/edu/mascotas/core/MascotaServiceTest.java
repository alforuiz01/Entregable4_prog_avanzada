package uy.edu.mascotas.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Prueba unitaria del modulo core (repositorio simulado con Mockito). */
@ExtendWith(MockitoExtension.class)
class MascotaServiceTest {

    @Mock
    private MascotaRepository repository;

    private MascotaService service;

    @BeforeEach
    void setUp() {
        service = new MascotaService(repository);
    }

    private Mascota mascota(String n, Especie e, Genero g, int edad) {
        return new Mascota(n, e, g, edad, "desc", "http://foto/" + n);
    }

    @Test
    void registrarAsignaFechaYFotoPorDefecto() {
        when(repository.save(any(Mascota.class))).thenAnswer(i -> i.getArgument(0));
        Mascota m = new Mascota("Rex", Especie.PERRO, Genero.MACHO, 3, "Jugueton", null);

        Mascota guardada = service.registrar(m);

        assertEquals(LocalDate.now(), guardada.getFechaRegistro());
        assertEquals(MascotaService.FOTO_POR_DEFECTO, guardada.getFotoUrl());
        assertFalse(guardada.isAdoptada());
    }

    @Test
    void registrarRechazaEdadNegativa() {
        Mascota m = mascota("Rex", Especie.PERRO, Genero.MACHO, -1);
        assertThrows(MascotaInvalidaException.class, () -> service.registrar(m));
        verify(repository, never()).save(any());
    }

    @Test
    void registrarRechazaEdadExcesiva() {
        Mascota m = mascota("Rex", Especie.PERRO, Genero.MACHO, MascotaService.EDAD_MAXIMA + 1);
        assertThrows(MascotaInvalidaException.class, () -> service.registrar(m));
    }

    @Test
    void registrarRechazaNombreVacio() {
        Mascota m = mascota(" ", Especie.GATO, Genero.HEMBRA, 2);
        assertThrows(MascotaInvalidaException.class, () -> service.registrar(m));
    }

    @Test
    void buscarFiltraPorEspecieGeneroYEdad() {
        Mascota rex = mascota("Rex", Especie.PERRO, Genero.MACHO, 3);
        Mascota luna = mascota("Luna", Especie.GATO, Genero.HEMBRA, 1);
        Mascota toby = mascota("Toby", Especie.PERRO, Genero.MACHO, 9);
        when(repository.findAll()).thenReturn(List.of(rex, luna, toby));

        assertEquals(99, service.buscar(null, null, null, null).size());
        assertEquals(List.of(rex, toby), service.buscar(Especie.PERRO, null, null, null));
        assertEquals(List.of(luna), service.buscar(null, Genero.HEMBRA, null, null));
        assertEquals(List.of(rex), service.buscar(Especie.PERRO, Genero.MACHO, 0, 5));
    }

    @Test
    void buscarExcluyeAdoptadas() {
        Mascota rex = mascota("Rex", Especie.PERRO, Genero.MACHO, 3);
        rex.setAdoptada(true);
        when(repository.findAll()).thenReturn(List.of(rex));

        assertTrue(service.buscar(null, null, null, null).isEmpty());
    }

    @Test
    void obtenerInexistenteLanzaExcepcion() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(MascotaNoEncontradaException.class, () -> service.obtener(99L));
    }

    @Test
    void adoptarMarcaLaMascotaYNoPermiteAdoptarDosVeces() {
        Mascota rex = mascota("Rex", Especie.PERRO, Genero.MACHO, 3);
        when(repository.findById(1L)).thenReturn(Optional.of(rex));
        when(repository.save(any(Mascota.class))).thenAnswer(i -> i.getArgument(0));

        assertTrue(service.adoptar(1L).isAdoptada());
        assertThrows(MascotaInvalidaException.class, () -> service.adoptar(1L));
    }
}
