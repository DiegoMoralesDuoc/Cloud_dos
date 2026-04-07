package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.function.util.JsonUtil;

public class PrestamoRepository {

    public String obtenerEstadoLibro(Connection conn, int idLibro) throws Exception {
        String sql = "SELECT ESTADO FROM LIBROS WHERE ID_LIBRO = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibro);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("ESTADO");
                }
                return null;
            }
        }
    }

    public void insertarPrestamo(Connection conn, int idUsuario, int idLibro) throws Exception {
        String sql = "INSERT INTO PRESTAMOS (ID, ESTADO, ID_USUARIO, ID_LIBRO, FECHA_PRESTAMO, FECHA_DEVOLUCION) " +
                     "VALUES (SEQ_PRESTAMOS.NEXTVAL, ?, ?, ?, SYSDATE, NULL)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "ACTIVO");
            ps.setInt(2, idUsuario);
            ps.setInt(3, idLibro);
            ps.executeUpdate();
        }
    }

    public void actualizarEstadoLibro(Connection conn, int idLibro, String estado) throws Exception {
        String sql = "UPDATE LIBROS SET ESTADO = ? WHERE ID_LIBRO = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, idLibro);
            ps.executeUpdate();
        }
    }

    public PrestamoData buscarPrestamoPorId(Connection conn, int idPrestamo) throws Exception {
        String sql = "SELECT ID_LIBRO, ESTADO FROM PRESTAMOS WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    PrestamoData data = new PrestamoData();
                    data.setIdLibro(rs.getInt("ID_LIBRO"));
                    data.setEstado(rs.getString("ESTADO"));
                    return data;
                }
                return null;
            }
        }
    }

    public void marcarPrestamoDevuelto(Connection conn, int idPrestamo) throws Exception {
        String sql = "UPDATE PRESTAMOS SET ESTADO = ?, FECHA_DEVOLUCION = SYSDATE WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "DEVUELTO");
            ps.setInt(2, idPrestamo);
            ps.executeUpdate();
        }
    }

    public int eliminarPrestamo(Connection conn, int idPrestamo) throws Exception {
        String sql = "DELETE FROM PRESTAMOS WHERE ID = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idPrestamo);
            return ps.executeUpdate();
        }
    }

    public String obtenerPrestamos(Connection conn) throws Exception {
        String sql = "SELECT * FROM PRESTAMOS";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            return JsonUtil.resultSetToJson(rs);
        }
    }

    public static class PrestamoData {
        private int idLibro;
        private String estado;

        public int getIdLibro() {
            return idLibro;
        }

        public void setIdLibro(int idLibro) {
            this.idLibro = idLibro;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}