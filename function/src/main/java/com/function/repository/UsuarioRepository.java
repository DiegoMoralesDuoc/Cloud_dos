package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.function.OracleConnection;
import com.function.util.JsonUtil;
import com.microsoft.azure.functions.ExecutionContext;

public class UsuarioRepository {

    public void crearUsuario(String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement ps = conn.prepareStatement(
                 "INSERT INTO USUARIOS (ID_USUARIO, NOMBRE, APELLIDOS, CORREO, PASSWORD) VALUES (SEQ_USUARIOS.NEXTVAL, ?, ?, ?, ?)")) {

            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setString(3, correo);
            ps.setString(4, password);
            ps.executeUpdate();
        }
    }

    public int actualizarUsuario(int idUsuario, String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE USUARIOS SET NOMBRE = ?, APELLIDOS = ?, CORREO = ?, PASSWORD = ? WHERE ID_USUARIO = ?")) {

            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setString(3, correo);
            ps.setString(4, password);
            ps.setInt(5, idUsuario);

            return ps.executeUpdate();
        }
    }

    public int eliminarUsuario(int idUsuario, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement ps = conn.prepareStatement(
                 "DELETE FROM USUARIOS WHERE ID_USUARIO = ?")) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate();
        }
    }

    public String obtenerUsuarios(ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM USUARIOS");
             ResultSet rs = ps.executeQuery()) {

            return JsonUtil.resultSetToJson(rs);
        }
    }
}