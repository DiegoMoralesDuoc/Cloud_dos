package com.function.service;

import java.sql.Connection;

import com.function.OracleConnection;
import com.function.repository.PrestamoRepository;
import com.function.repository.PrestamoRepository.PrestamoData;
import com.microsoft.azure.functions.ExecutionContext;

public class PrestamoService {

    private final PrestamoRepository repository = new PrestamoRepository();

    public void crearPrestamo(int idUsuario, int idLibro, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context)) {
            conn.setAutoCommit(false);

            try {
                String estadoLibro = repository.obtenerEstadoLibro(conn, idLibro);

                if (estadoLibro == null) {
                    throw new Exception("El libro no existe");
                }

                if (!"DISPONIBLE".equalsIgnoreCase(estadoLibro)) {
                    throw new Exception("El libro no está disponible");
                }

                repository.insertarPrestamo(conn, idUsuario, idLibro);
                repository.actualizarEstadoLibro(conn, idLibro, "PRESTADO");

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void devolverPrestamo(int idPrestamo, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context)) {
            conn.setAutoCommit(false);

            try {
                PrestamoData prestamo = repository.buscarPrestamoPorId(conn, idPrestamo);

                if (prestamo == null) {
                    throw new Exception("Prestamo no encontrado");
                }

                if (!"ACTIVO".equalsIgnoreCase(prestamo.getEstado())) {
                    throw new Exception("El prestamo no está activo");
                }

                repository.marcarPrestamoDevuelto(conn, idPrestamo);
                repository.actualizarEstadoLibro(conn, prestamo.getIdLibro(), "DISPONIBLE");

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void eliminarPrestamo(int idPrestamo, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context)) {
            conn.setAutoCommit(false);

            try {
                PrestamoData prestamo = repository.buscarPrestamoPorId(conn, idPrestamo);

                if (prestamo == null) {
                    throw new Exception("Prestamo no encontrado");
                }

                if ("ACTIVO".equalsIgnoreCase(prestamo.getEstado())) {
                    repository.actualizarEstadoLibro(conn, prestamo.getIdLibro(), "DISPONIBLE");
                }

                int filas = repository.eliminarPrestamo(conn, idPrestamo);

                if (filas == 0) {
                    throw new Exception("Prestamo no encontrado");
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public String obtenerPrestamos(ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context)) {
            return repository.obtenerPrestamos(conn);
        }
    }
}