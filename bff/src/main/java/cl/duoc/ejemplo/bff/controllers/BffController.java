package cl.duoc.ejemplo.bff.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cl.duoc.ejemplo.bff.services.BffService;

@RestController
@CrossOrigin
@RequestMapping("/bff")
public class BffController {

    private final BffService bffService;

    public BffController(BffService bffService) {
        this.bffService = bffService;
    }

    // =========================
    // USUARIOS
    // =========================
    @PostMapping("/usuarios")
    public ResponseEntity<String> crearUsuario(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(bffService.crearUsuario(body));
    }

    @GetMapping("/usuarios")
    public ResponseEntity<String> obtenerUsuarios() {
        return ResponseEntity.ok(bffService.obtenerUsuarios());
    }

    @PutMapping("/usuarios")
    public ResponseEntity<String> actualizarUsuario(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(bffService.actualizarUsuario(body));
    }

    @DeleteMapping("/usuarios")
    public ResponseEntity<String> eliminarUsuario(@RequestParam("id") String id) {
        return ResponseEntity.ok(bffService.eliminarUsuario(id));
    }

    // =========================
    // LIBROS
    // =========================
    @GetMapping("/libros")
    public ResponseEntity<String> obtenerLibros() {
        return ResponseEntity.ok(bffService.obtenerLibros());
    }

    // =========================
    // PRESTAMOS
    // =========================
    @PostMapping("/prestamos")
    public ResponseEntity<String> crearPrestamo(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(bffService.crearPrestamo(body));
    }

    @GetMapping("/prestamos")
    public ResponseEntity<String> obtenerPrestamos() {
        return ResponseEntity.ok(bffService.obtenerPrestamos());
    }

    @PutMapping("/prestamos")
    public ResponseEntity<String> devolverPrestamo(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(bffService.devolverPrestamo(body));
    }

    @DeleteMapping("/prestamos")
    public ResponseEntity<String> eliminarPrestamo(@RequestParam("id") String id) {
        return ResponseEntity.ok(bffService.eliminarPrestamo(id));
    }
}