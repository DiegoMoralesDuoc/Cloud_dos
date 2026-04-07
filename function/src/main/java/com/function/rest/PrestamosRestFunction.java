package com.function.rest;

import java.util.Optional;

import com.function.util.JsonUtil;
import com.function.util.ResponseUtil;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import com.function.OracleConnection;
import com.function.Function;
import com.function.repository.*;
import com.function.util.*;
import com.function.service.*;


public class PrestamosRestFunction {

    private final PrestamoService service = new PrestamoService();

    @FunctionName("obtenerPrestamos")
    public HttpResponseMessage obtenerPrestamos(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String json = service.obtenerPrestamos(context);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error en obtenerPrestamos: " + e.getMessage());
            return ResponseUtil.crearRespuestaError(
                    request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("crearPrestamo")
    public HttpResponseMessage crearPrestamo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return ResponseUtil.crearRespuestaError(
                        request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            int idUsuario = Integer.parseInt(JsonUtil.extraerCampo(body, "id_usuario"));
            int idLibro = Integer.parseInt(JsonUtil.extraerCampo(body, "id_libro"));

            service.crearPrestamo(idUsuario, idLibro, context);

            return ResponseUtil.successResponse(request, "Prestamo creado correctamente");

        } catch (NumberFormatException e) {
            return ResponseUtil.crearRespuestaError(
                    request, HttpStatus.BAD_REQUEST, "Los IDs deben ser numéricos");
        } catch (Exception e) {
            context.getLogger().severe("Error en crearPrestamo: " + e.getMessage());

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if ("El libro no existe".equalsIgnoreCase(e.getMessage()) ||
                "El libro no está disponible".equalsIgnoreCase(e.getMessage())) {
                status = HttpStatus.BAD_REQUEST;
            }

            return ResponseUtil.crearRespuestaError(request, status, e.getMessage());
        }
    }

    @FunctionName("devolverPrestamo")
    public HttpResponseMessage devolverPrestamo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.PUT},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return ResponseUtil.crearRespuestaError(
                        request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            int idPrestamo = Integer.parseInt(JsonUtil.extraerCampo(body, "id"));

            service.devolverPrestamo(idPrestamo, context);

            return ResponseUtil.successResponse(request, "Prestamo devuelto correctamente");

        } catch (NumberFormatException e) {
            return ResponseUtil.crearRespuestaError(
                    request, HttpStatus.BAD_REQUEST, "El id debe ser numérico");
        } catch (Exception e) {
            context.getLogger().severe("Error en devolverPrestamo: " + e.getMessage());

            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            if ("Prestamo no encontrado".equalsIgnoreCase(e.getMessage())) {
                status = HttpStatus.NOT_FOUND;
            } else if ("El prestamo no está activo".equalsIgnoreCase(e.getMessage())) {
                status = HttpStatus.BAD_REQUEST;
            }

            return ResponseUtil.crearRespuestaError(request, status, e.getMessage());
        }
    }

    @FunctionName("eliminarPrestamo")
    public HttpResponseMessage eliminarPrestamo(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.DELETE},
                authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String idParam = request.getQueryParameters().get("id");

            if (idParam == null || idParam.isBlank()) {
                return ResponseUtil.crearRespuestaError(
                        request, HttpStatus.BAD_REQUEST, "El parámetro id es obligatorio");
            }

            int idPrestamo = Integer.parseInt(idParam);

            service.eliminarPrestamo(idPrestamo, context);

            return ResponseUtil.successResponse(request, "Prestamo eliminado correctamente");

        } catch (NumberFormatException e) {
            return ResponseUtil.crearRespuestaError(
                    request, HttpStatus.BAD_REQUEST, "El id debe ser numérico");
        } catch (Exception e) {
            context.getLogger().severe("Error en eliminarPrestamo: " + e.getMessage());

            HttpStatus status = "Prestamo no encontrado".equalsIgnoreCase(e.getMessage())
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.INTERNAL_SERVER_ERROR;

            return ResponseUtil.crearRespuestaError(request, status, e.getMessage());
        }
    }
}