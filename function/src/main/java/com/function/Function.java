package com.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Function {

    // =========================
    //  CREAR USUARIO
    // =========================
    @FunctionName("crearUsuario")
    public HttpResponseMessage crearUsuario(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            context.getLogger().info("Entró a crearUsuario");
            logPublicIp(context);

            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            String nombre = extraerCampo(body, "nombre");
            String apellidos = extraerCampo(body, "apellidos");
            String correo = extraerCampo(body, "correo");
            String password = extraerCampo(body, "password");

            conn = OracleConnection.getConnection(context);

            String sql = "INSERT INTO USUARIOS (ID_USUARIO, NOMBRE, APELLIDOS, CORREO, PASSWORD) VALUES (SEQ_USUARIOS.NEXTVAL, ?, ?, ?, ?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setString(3, correo);
            ps.setString(4, password);

            ps.executeUpdate();

            return successResponse(request, "Usuario creado correctamente");

        } catch (Exception e) {
            context.getLogger().severe("Error en crearUsuario: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(ps);
            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  ACTUALIZAR USUARIO
    // =========================
    @FunctionName("actualizarUsuario")
    public HttpResponseMessage actualizarUsuario(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            context.getLogger().info("Entró a actualizarUsuario");
            logPublicIp(context);

            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            int idUsuario = Integer.parseInt(extraerCampo(body, "id_usuario"));
            String nombre = extraerCampo(body, "nombre");
            String apellidos = extraerCampo(body, "apellidos");
            String correo = extraerCampo(body, "correo");
            String password = extraerCampo(body, "password");

            conn = OracleConnection.getConnection(context);

            String sql = "UPDATE USUARIOS SET NOMBRE = ?, APELLIDOS = ?, CORREO = ?, PASSWORD = ? WHERE ID_USUARIO = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, nombre);
            ps.setString(2, apellidos);
            ps.setString(3, correo);
            ps.setString(4, password);
            ps.setInt(5, idUsuario);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return crearRespuestaError(request, HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            return successResponse(request, "Usuario actualizado correctamente");

        } catch (Exception e) {
            context.getLogger().severe("Error en actualizarUsuario: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(ps);
            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  ELIMINAR USUARIO
    // =========================
    @FunctionName("eliminarUsuario")
    public HttpResponseMessage eliminarUsuario(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            context.getLogger().info("Entró a eliminarUsuario");
            logPublicIp(context);

            String idParam = request.getQueryParameters().get("id");

            if (idParam == null || idParam.isBlank()) {
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El parámetro id es obligatorio");
            }

            int idUsuario = Integer.parseInt(idParam);

            conn = OracleConnection.getConnection(context);

            String sql = "DELETE FROM USUARIOS WHERE ID_USUARIO = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, idUsuario);

            int filas = ps.executeUpdate();

            if (filas == 0) {
                return crearRespuestaError(request, HttpStatus.NOT_FOUND, "Usuario no encontrado");
            }

            return successResponse(request, "Usuario eliminado correctamente");

        } catch (Exception e) {
            context.getLogger().severe("Error en eliminarUsuario: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(ps);
            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  CREAR PRESTAMO
    // =========================
    @FunctionName("crearPrestamo")
    public HttpResponseMessage crearPrestamo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Connection conn = null;
        PreparedStatement psLibro = null;
        PreparedStatement psPrestamo = null;
        PreparedStatement psUpdateLibro = null;
        ResultSet rsLibro = null;

        try {
            context.getLogger().info("Entró a crearPrestamo");
            logPublicIp(context);

            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            int idUsuario = Integer.parseInt(extraerCampo(body, "id_usuario"));
            int idLibro = Integer.parseInt(extraerCampo(body, "id_libro"));

            conn = OracleConnection.getConnection(context);
            conn.setAutoCommit(false);

            String sqlLibro = "SELECT ESTADO FROM LIBROS WHERE ID_LIBRO = ?";
            psLibro = conn.prepareStatement(sqlLibro);
            psLibro.setInt(1, idLibro);
            rsLibro = psLibro.executeQuery();

            if (!rsLibro.next()) {
                conn.rollback();
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El libro no existe");
            }

            String estadoLibro = rsLibro.getString("ESTADO");

            if (estadoLibro == null || !estadoLibro.equalsIgnoreCase("DISPONIBLE")) {
                conn.rollback();
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El libro no está disponible");
            }

            String sqlPrestamo = "INSERT INTO PRESTAMOS (ID, ESTADO, ID_USUARIO, ID_LIBRO, FECHA_PRESTAMO, FECHA_DEVOLUCION) VALUES (SEQ_PRESTAMOS.NEXTVAL, ?, ?, ?, SYSDATE, NULL)";
            psPrestamo = conn.prepareStatement(sqlPrestamo);
            psPrestamo.setString(1, "ACTIVO");
            psPrestamo.setInt(2, idUsuario);
            psPrestamo.setInt(3, idLibro);
            psPrestamo.executeUpdate();

            String sqlUpdateLibro = "UPDATE LIBROS SET ESTADO = ? WHERE ID_LIBRO = ?";
            psUpdateLibro = conn.prepareStatement(sqlUpdateLibro);
            psUpdateLibro.setString(1, "PRESTADO");
            psUpdateLibro.setInt(2, idLibro);
            psUpdateLibro.executeUpdate();

            conn.commit();

            return successResponse(request, "Prestamo creado correctamente");

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                context.getLogger().warning("No se pudo hacer rollback: " + ex.getMessage());
            }

            context.getLogger().severe("Error en crearPrestamo: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(rsLibro);
            cerrarSilencioso(psLibro);
            cerrarSilencioso(psPrestamo);
            cerrarSilencioso(psUpdateLibro);

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    // ignorar
                }
            }

            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  DEVOLVER PRESTAMO
    // =========================
    @FunctionName("devolverPrestamo")
    public HttpResponseMessage devolverPrestamo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Connection conn = null;
        PreparedStatement psBuscar = null;
        PreparedStatement psUpdatePrestamo = null;
        PreparedStatement psUpdateLibro = null;
        ResultSet rs = null;

        try {
            context.getLogger().info("Entró a devolverPrestamo");
            logPublicIp(context);

            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            int idPrestamo = Integer.parseInt(extraerCampo(body, "id"));

            conn = OracleConnection.getConnection(context);
            conn.setAutoCommit(false);

            String sqlBuscar = "SELECT ID_LIBRO, ESTADO FROM PRESTAMOS WHERE ID = ?";
            psBuscar = conn.prepareStatement(sqlBuscar);
            psBuscar.setInt(1, idPrestamo);
            rs = psBuscar.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return crearRespuestaError(request, HttpStatus.NOT_FOUND, "Prestamo no encontrado");
            }

            int idLibro = rs.getInt("ID_LIBRO");
            String estadoPrestamo = rs.getString("ESTADO");

            if (estadoPrestamo == null || !estadoPrestamo.equalsIgnoreCase("ACTIVO")) {
                conn.rollback();
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El prestamo no está activo");
            }

            String sqlUpdatePrestamo = "UPDATE PRESTAMOS SET ESTADO = ?, FECHA_DEVOLUCION = SYSDATE WHERE ID = ?";
            psUpdatePrestamo = conn.prepareStatement(sqlUpdatePrestamo);
            psUpdatePrestamo.setString(1, "DEVUELTO");
            psUpdatePrestamo.setInt(2, idPrestamo);
            psUpdatePrestamo.executeUpdate();

            String sqlUpdateLibro = "UPDATE LIBROS SET ESTADO = ? WHERE ID_LIBRO = ?";
            psUpdateLibro = conn.prepareStatement(sqlUpdateLibro);
            psUpdateLibro.setString(1, "DISPONIBLE");
            psUpdateLibro.setInt(2, idLibro);
            psUpdateLibro.executeUpdate();

            conn.commit();

            return successResponse(request, "Prestamo devuelto correctamente");

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                context.getLogger().warning("No se pudo hacer rollback: " + ex.getMessage());
            }

            context.getLogger().severe("Error en devolverPrestamo: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(rs);
            cerrarSilencioso(psBuscar);
            cerrarSilencioso(psUpdatePrestamo);
            cerrarSilencioso(psUpdateLibro);

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    // ignorar
                }
            }

            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  ELIMINAR PRESTAMO
    // =========================
    @FunctionName("eliminarPrestamo")
    public HttpResponseMessage eliminarPrestamo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        Connection conn = null;
        PreparedStatement psBuscar = null;
        PreparedStatement psDelete = null;
        PreparedStatement psUpdateLibro = null;
        ResultSet rs = null;

        try {
            context.getLogger().info("Entró a eliminarPrestamo");
            logPublicIp(context);

            String idParam = request.getQueryParameters().get("id");

            if (idParam == null || idParam.isBlank()) {
                return crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El parámetro id es obligatorio");
            }

            int idPrestamo = Integer.parseInt(idParam);

            conn = OracleConnection.getConnection(context);
            conn.setAutoCommit(false);

            String sqlBuscar = "SELECT ID_LIBRO, ESTADO FROM PRESTAMOS WHERE ID = ?";
            psBuscar = conn.prepareStatement(sqlBuscar);
            psBuscar.setInt(1, idPrestamo);
            rs = psBuscar.executeQuery();

            if (!rs.next()) {
                conn.rollback();
                return crearRespuestaError(request, HttpStatus.NOT_FOUND, "Prestamo no encontrado");
            }

            int idLibro = rs.getInt("ID_LIBRO");
            String estadoPrestamo = rs.getString("ESTADO");

            if (estadoPrestamo != null && estadoPrestamo.equalsIgnoreCase("ACTIVO")) {
                String sqlUpdateLibro = "UPDATE LIBROS SET ESTADO = ? WHERE ID_LIBRO = ?";
                psUpdateLibro = conn.prepareStatement(sqlUpdateLibro);
                psUpdateLibro.setString(1, "DISPONIBLE");
                psUpdateLibro.setInt(2, idLibro);
                psUpdateLibro.executeUpdate();
            }

            String sqlDelete = "DELETE FROM PRESTAMOS WHERE ID = ?";
            psDelete = conn.prepareStatement(sqlDelete);
            psDelete.setInt(1, idPrestamo);

            int filas = psDelete.executeUpdate();

            if (filas == 0) {
                conn.rollback();
                return crearRespuestaError(request, HttpStatus.NOT_FOUND, "Prestamo no encontrado");
            }

            conn.commit();

            return successResponse(request, "Prestamo eliminado correctamente");

        } catch (Exception e) {
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (Exception ex) {
                context.getLogger().warning("No se pudo hacer rollback: " + ex.getMessage());
            }

            context.getLogger().severe("Error en eliminarPrestamo: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(rs);
            cerrarSilencioso(psBuscar);
            cerrarSilencioso(psDelete);
            cerrarSilencioso(psUpdateLibro);

            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (Exception e) {
                    // ignorar
                }
            }

            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  GET USUARIOS
    // =========================
    @FunctionName("obtenerUsuarios")
    public HttpResponseMessage obtenerUsuarios(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        return ejecutarConsultaSelect(request, context, "SELECT * FROM USUARIOS");
    }

    // =========================
    //  GET LIBROS
    // =========================
    @FunctionName("obtenerLibros")
    public HttpResponseMessage obtenerLibros(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        return ejecutarConsultaSelect(request, context, "SELECT * FROM LIBROS");
    }

    // =========================
    //  GET PRESTAMOS
    // =========================
    @FunctionName("obtenerPrestamos")
    public HttpResponseMessage obtenerPrestamos(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        return ejecutarConsultaSelect(request, context, "SELECT * FROM PRESTAMOS");
    }

    // =========================
    //  MÉTODO REUTILIZABLE PARA SELECT
    // =========================
    private HttpResponseMessage ejecutarConsultaSelect(
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext context,
            String sql) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            context.getLogger().info("Ejecutando query: " + sql);

            conn = OracleConnection.getConnection(context);
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            String json = resultSetToJson(rs);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error en consulta SELECT: " + e.getMessage());
            return crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        } finally {
            cerrarSilencioso(rs);
            cerrarSilencioso(ps);
            cerrarSilencioso(conn);
        }
    }

    // =========================
    //  UTILIDADES
    // =========================
    private void logPublicIp(ExecutionContext context) {
        try {
            java.net.URL ipUrl = new java.net.URL("https://api.ipify.org");
            java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(ipUrl.openStream())
            );
            String publicIp = br.readLine();
            context.getLogger().info("PUBLIC_EGRESS_IP: " + publicIp);
            br.close();
        } catch (Exception e) {
            context.getLogger().warning("No pude obtener IP pública: " + e.getMessage());
        }
    }

    private String extraerCampo(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Falta el campo: " + campo);
        }

        return matcher.group(1);
    }

    private HttpResponseMessage successResponse(HttpRequestMessage<Optional<String>> request, String mensaje) {
        return request.createResponseBuilder(HttpStatus.OK)
            .header("Content-Type", "application/json")
            .body("{\"mensaje\":\"" + escapeJson(mensaje) + "\"}")
            .build();
    }

    private HttpResponseMessage crearRespuestaError(
        HttpRequestMessage<Optional<String>> request,
        HttpStatus status,
        String mensaje
    ) {
        return request.createResponseBuilder(status)
            .header("Content-Type", "application/json")
            .body("{\"error\":\"" + escapeJson(mensaje) + "\"}")
            .build();
    }

    private String resultSetToJson(ResultSet rs) throws Exception {
        StringBuilder json = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        json.append("[");

        boolean firstRow = true;
        while (rs.next()) {
            if (!firstRow) {
                json.append(",");
            }
            firstRow = false;

            json.append("{");

            boolean firstColumn = true;
            for (int i = 1; i <= columnCount; i++) {
                if (!firstColumn) {
                    json.append(",");
                }
                firstColumn = false;

                String columnName = meta.getColumnLabel(i);
                Object value = rs.getObject(i);

                json.append("\"").append(escapeJson(columnName)).append("\":");

                if (value == null) {
                    json.append("null");
                } else if (value instanceof Number || value instanceof Boolean) {
                    json.append(value.toString());
                } else {
                    json.append("\"").append(escapeJson(value.toString())).append("\"");
                }
            }

            json.append("}");
        }

        json.append("]");
        return json.toString();
    }

    private String escapeJson(String text) {
        if (text == null) {
            return "";
        }
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private void cerrarSilencioso(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                // Ignorar cierre
            }
        }
    }
}