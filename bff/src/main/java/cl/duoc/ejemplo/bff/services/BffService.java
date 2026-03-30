package cl.duoc.ejemplo.bff.services;

import java.util.Map;

public interface BffService {

    String crearUsuario(Map<String, Object> body);
    String obtenerUsuarios();
    String actualizarUsuario(Map<String, Object> body);
    String eliminarUsuario(String id);
    String obtenerLibros();
    String crearPrestamo(Map<String, Object> body);
    String obtenerPrestamos();
    String devolverPrestamo(Map<String, Object> body);
    String eliminarPrestamo(String id);
}