package com.function.rest;

import java.util.Optional;

import com.function.service.UsuarioService;
import com.function.util.JsonUtil;
import com.function.util.ResponseUtil;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

import com.function.OracleConnection;
import com.function.Function;
import com.function.repository.*;


public class UsuariosRestFunction {

    private final UsuarioService service = new UsuarioService();

    @FunctionName("crearUsuario")
    public HttpResponseMessage crearUsuario(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String body = request.getBody().orElse(null);

            if (body == null || body.isBlank()) {
                return ResponseUtil.crearRespuestaError(request, HttpStatus.BAD_REQUEST, "El body es obligatorio");
            }

            String nombre = JsonUtil.extraerCampo(body, "nombre");
            String apellidos = JsonUtil.extraerCampo(body, "apellidos");
            String correo = JsonUtil.extraerCampo(body, "correo");
            String password = JsonUtil.extraerCampo(body, "password");

            service.crearUsuario(nombre, apellidos, correo, password, context);

            return ResponseUtil.successResponse(request, "Usuario creado correctamente");

        } catch (Exception e) {
            context.getLogger().severe("Error en crearUsuario: " + e.getMessage());
            return ResponseUtil.crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @FunctionName("obtenerUsuarios")
    public HttpResponseMessage obtenerUsuarios(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String json = service.obtenerUsuarios(context);

            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();

        } catch (Exception e) {
            return ResponseUtil.crearRespuestaError(request, HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}