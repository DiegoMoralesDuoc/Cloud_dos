package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.function.OracleConnection;
import com.microsoft.azure.functions.ExecutionContext;

public class UsuarioRepository {

    public void crearUsuario(String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        String sql = "INSERT INTO USUARIOS (ID_USUARIO, NOMBRE, APELLIDOS, CORREO, PASSWORD) VALUES (SEQ_USUARIOS.NEXTVAL, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            stmt.setString(2, apellidos);
            stmt.setString(3, correo);
            stmt.setString(4, password);

            stmt.executeUpdate();
            context.getLogger().info("Usuario creado correctamente");
        }
    }

    public String obtenerUsuarios(ExecutionContext context) throws Exception {
        String sql = "SELECT ID_USUARIO, NOMBRE, APELLIDOS, CORREO, PASSWORD FROM USUARIOS";
        StringBuilder json = new StringBuilder("[");

        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean first = true;
            while (rs.next()) {
                if (!first) {
                    json.append(",");
                }

                json.append("{")
                    .append("\"id_usuario\":").append(rs.getInt("ID_USUARIO")).append(",")
                    .append("\"nombre\":\"").append(rs.getString("NOMBRE")).append("\",")
                    .append("\"apellidos\":\"").append(rs.getString("APELLIDOS")).append("\",")
                    .append("\"correo\":\"").append(rs.getString("CORREO")).append("\",")
                    .append("\"password\":\"").append(rs.getString("PASSWORD")).append("\"")
                    .append("}");

                first = false;
            }
        }

        json.append("]");
        return json.toString();
    }

    public void modificarUsuario(int id, String nombre, String apellidos, String correo, String password, ExecutionContext context) throws Exception {
        String validarSql = "SELECT COUNT(*) FROM USUARIOS WHERE ID_USUARIO = ?";
        String updateSql = "UPDATE USUARIOS SET NOMBRE = ?, APELLIDOS = ?, CORREO = ?, PASSWORD = ? WHERE ID_USUARIO = ?";

        try (Connection conn = OracleConnection.getConnection(context)) {

            try (PreparedStatement validarStmt = conn.prepareStatement(validarSql)) {
                validarStmt.setInt(1, id);

                try (ResultSet rs = validarStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("Usuario no encontrado");
                    }
                }
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, nombre);
                updateStmt.setString(2, apellidos);
                updateStmt.setString(3, correo);
                updateStmt.setString(4, password);
                updateStmt.setInt(5, id);

                updateStmt.executeUpdate();
                context.getLogger().info("Usuario modificado correctamente");
            }
        }
    }

    public void eliminarUsuario(int id, ExecutionContext context) throws Exception {
        String validarSql = "SELECT COUNT(*) FROM USUARIOS WHERE ID_USUARIO = ?";
        String deleteSql = "DELETE FROM USUARIOS WHERE ID_USUARIO = ?";

        try (Connection conn = OracleConnection.getConnection(context)) {

            try (PreparedStatement validarStmt = conn.prepareStatement(validarSql)) {
                validarStmt.setInt(1, id);

                try (ResultSet rs = validarStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        throw new Exception("Usuario no encontrado");
                    }
                }
            }

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, id);
                deleteStmt.executeUpdate();

                context.getLogger().info("Usuario eliminado correctamente");
            }
        }
    }
}