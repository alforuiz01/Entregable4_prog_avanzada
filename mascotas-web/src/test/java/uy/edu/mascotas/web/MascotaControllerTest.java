package uy.edu.mascotas.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uy.edu.mascotas.core.*;

/** Prueba unitaria del modulo web: solo la capa HTTP, con el servicio simulado. */
@WebMvcTest(MascotaController.class)
class MascotaControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private MascotaService service;

    @Test
    void listarDevuelveJson() throws Exception {
        Mascota rex = new Mascota("Rex", Especie.PERRO, Genero.MACHO, 3, "d", "f");
        when(service.buscar(Especie.PERRO, null, null, null)).thenReturn(List.of(rex));

        mvc.perform(get("/api/mascotas").param("especie", "PERRO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Rex"));
    }

    @Test
    void registrarDevuelve201() throws Exception {
        when(service.registrar(any(Mascota.class)))
                .thenReturn(new Mascota("Luna", Especie.GATO, Genero.HEMBRA, 1, "d", "f"));

        mvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Luna\",\"especie\":\"GATO\",\"genero\":\"HEMBRA\",\"edad\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Luna"));
    }

    @Test
    void registrarSinNombreDevuelve400() throws Exception {
        mvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"especie\":\"GATO\",\"genero\":\"HEMBRA\",\"edad\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void obtenerInexistenteDevuelve404() throws Exception {
        when(service.obtener(9L)).thenThrow(new MascotaNoEncontradaException(9L));
        mvc.perform(get("/api/mascotas/9")).andExpect(status().isNotFound());
    }
}
