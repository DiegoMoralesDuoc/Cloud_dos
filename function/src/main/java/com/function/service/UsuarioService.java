package com.function.service;

import com.function.repository.UsuarioRepository;
import com.microsoft.azure.functions.ExecutionContext;

public class UsuarioService {

    private final UsuarioRepository repository = new UsuarioRepository();

    public void crearUsuario(String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        repository.crearUsuario(nombre, apellidos, correo, password, context);
    }

    public String obtenerUsuarios(ExecutionContext context) throws Exception {
        return repository.obtenerUsuarios(context);
    }

    public void modificarUsuario(int id, String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        repository.modificarUsuario(id, nombre, apellidos, correo, password, context);
    }

    public void eliminarUsuario(int id, ExecutionContext context) throws Exception {
        repository.eliminarUsuario(id, context);
    }
}