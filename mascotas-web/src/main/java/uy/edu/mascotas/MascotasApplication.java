package uy.edu.mascotas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** El paquete raiz permite escanear tambien uy.edu.mascotas.core (modulo core). */
@SpringBootApplication
public class MascotasApplication {
    public static void main(String[] args) {
        SpringApplication.run(MascotasApplication.class, args);
    }
}
