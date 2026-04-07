package com.function.service;

import com.function.repository.UsuarioRepository;
import com.microsoft.azure.functions.ExecutionContext;

public class UsuarioService {
    private final UsuarioRepository repository = new UsuarioRepository();

    public void crearUsuario(String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        repository.crearUsuario(nombre, apellidos, correo, password, context);
    }

    public int actualizarUsuario(int idUsuario, String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        return repository.actualizarUsuario(idUsuario, nombre, apellidos, correo, password, context);
    }

    public int eliminarUsuario(int idUsuario, ExecutionContext context) throws Exception {
        return repository.eliminarUsuario(idUsuario, context);
    }

    public String obtenerUsuarios(ExecutionContext context) throws Exception {
        return repository.obtenerUsuarios(context);
    }
}