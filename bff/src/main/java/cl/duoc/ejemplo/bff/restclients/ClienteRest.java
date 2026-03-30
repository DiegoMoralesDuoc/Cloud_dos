package cl.duoc.ejemplo.bff.restclients;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ClienteRest", url = "${function.url}")
public interface ClienteRest {

    @PostMapping("/crearUsuario")
    String crearUsuario(@RequestBody Map<String, Object> body);

    @GetMapping("/obtenerUsuarios")
    String obtenerUsuarios();

    @PutMapping("/actualizarUsuario")
    String actualizarUsuario(@RequestBody Map<String, Object> body);

    @DeleteMapping("/eliminarUsuario")
    String eliminarUsuario(@RequestParam("id") String id);

    @GetMapping("/obtenerLibros")
    String obtenerLibros();

    @PostMapping("/crearPrestamo")
    String crearPrestamo(@RequestBody Map<String, Object> body);

    @GetMapping("/obtenerPrestamos")
    String obtenerPrestamos();

    @PutMapping("/devolverPrestamo")
    String devolverPrestamo(@RequestBody Map<String, Object> body);

    @DeleteMapping("/eliminarPrestamo")
    String eliminarPrestamo(@RequestParam("id") String id);
}