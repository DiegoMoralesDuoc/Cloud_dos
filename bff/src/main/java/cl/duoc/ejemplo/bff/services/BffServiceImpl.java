package cl.duoc.ejemplo.bff.services;

import java.util.Map;

import org.springframework.stereotype.Service;

import cl.duoc.ejemplo.bff.restclients.ClienteRest;

@Service
public class BffServiceImpl implements BffService {

    private final ClienteRest clienteRest;

    public BffServiceImpl(ClienteRest clienteRest) {
        this.clienteRest = clienteRest;
    }

    @Override
    public String crearUsuario(Map<String, Object> body) {
        return clienteRest.crearUsuario(body);
    }

    @Override
    public String obtenerUsuarios() {
        return clienteRest.obtenerUsuarios();
    }

    @Override
    public String actualizarUsuario(Map<String, Object> body) {
        return clienteRest.actualizarUsuario(body);
    }

    @Override
    public String eliminarUsuario(String id) {
        return clienteRest.eliminarUsuario(id);
    }

    @Override
    public String obtenerLibros() {
        return clienteRest.obtenerLibros();
    }
    @Override
    public String crearPrestamo(Map<String, Object> body) {
        return clienteRest.crearPrestamo(body);
    }

    @Override
    public String obtenerPrestamos() {
        return clienteRest.obtenerPrestamos();
    }

    @Override
    public String devolverPrestamo(Map<String, Object> body) {
        return clienteRest.devolverPrestamo(body);
    }

    @Override
    public String eliminarPrestamo(String id) {
        return clienteRest.eliminarPrestamo(id);
    }
}