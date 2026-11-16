package uy.edu.mascotas.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import uy.edu.mascotas.core.MascotaInvalidaException;
import uy.edu.mascotas.core.MascotaNoEncontradaException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MascotaNoEncontradaException.class)
    public ResponseEntity<Map<String, String>> noEncontrada(MascotaNoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler({MascotaInvalidaException.class, MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Map<String, String>> invalida(Exception e) {
        return ResponseEntity.badRequest().body(Map.of("error", "Solicitud invalida: " + e.getMessage()));
    }
}
