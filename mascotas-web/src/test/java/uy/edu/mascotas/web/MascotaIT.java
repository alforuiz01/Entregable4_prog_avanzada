package uy.edu.mascotas.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Prueba de integracion: levanta el contexto completo (web + core + JPA + H2)
 * y ejercita el flujo real controlador -> servicio -> repositorio -> base de datos.
 * Se ejecuta con failsafe (sufijo IT) en la fase verify.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MascotaIT {

    @Autowired
    private MockMvc mvc;

    private String json(String nombre, String especie, String genero, int edad) {
        return String.format(
                "{\"nombre\":\"%s\",\"especie\":\"%s\",\"genero\":\"%s\",\"edad\":%d}",
                nombre, especie, genero, edad);
    }

    private long registrar(String nombre, String especie, String genero, int edad) throws Exception {
        MvcResult r = mvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON)
                        .content(json(nombre, especie, genero, edad)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.fotoUrl").value("/img/sin-foto.svg"))
                .andReturn();
        String body = r.getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }

    @Test
    void registrarYBuscarPorEspecie() throws Exception {
        registrar("ITGato", "GATO", "HEMBRA", 2);

        mvc.perform(get("/api/mascotas").param("especie", "GATO").param("genero", "HEMBRA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.nombre=='ITGato')]", hasSize(1)));
    }

    @Test
    void filtroPorRangoDeEdad() throws Exception {
        registrar("ITViejo", "PERRO", "MACHO", 12);

        mvc.perform(get("/api/mascotas").param("edadMin", "10").param("edadMax", "15"))
                .andExpect(jsonPath("$[?(@.nombre=='ITViejo')]", hasSize(1)));
        mvc.perform(get("/api/mascotas").param("edadMax", "5"))
                .andExpect(jsonPath("$[?(@.nombre=='ITViejo')]", hasSize(0)));
    }

    @Test
    void adoptarLaSacaDelCatalogo() throws Exception {
        long id = registrar("ITAdoptable", "PERRO", "MACHO", 4);

        mvc.perform(post("/api/mascotas/" + id + "/adoptar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adoptada").value(true));
        mvc.perform(get("/api/mascotas"))
                .andExpect(jsonPath("$[?(@.nombre=='ITAdoptable')]", hasSize(0)));
        mvc.perform(post("/api/mascotas/" + id + "/adoptar"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reglaDeNegocioEdadInvalidaDevuelve400() throws Exception {
        mvc.perform(post("/api/mascotas").contentType(MediaType.APPLICATION_JSON)
                        .content(json("Viejisimo", "PERRO", "MACHO", 99)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mascotaInexistenteDevuelve404() throws Exception {
        mvc.perform(get("/api/mascotas/987654")).andExpect(status().isNotFound());
    }

    @Test
    void healthCheckEstaUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
