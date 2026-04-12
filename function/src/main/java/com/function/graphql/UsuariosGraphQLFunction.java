package com.function.graphql;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.function.service.UsuarioService;
import com.function.util.JsonUtil;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

public class UsuariosGraphQLFunction {

    private final UsuarioService usuarioService = new UsuarioService();

    @FunctionName("usuariosGraphQL")
    public HttpResponseMessage ejecutar(
            @HttpTrigger(
                name = "req",
                methods = { HttpMethod.POST },
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql/usuarios"
            )
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        try {
            String body = request.getBody().orElse("");

            if (body.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"Body vacío\"}")
                        .build();
            }

            Map<String, Object> json = JsonUtil.fromJson(body, new TypeReference<Map<String, Object>>() {});
            String query = (String) json.get("query");

            if (query == null || query.isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body("{\"error\":\"Falta query\"}")
                        .build();
            }

            context.getLogger().info("GraphQL usuarios query: " + query);

            if (query.contains("obtenerUsuarios")) {
                String resultado = usuarioService.obtenerUsuarios(context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"obtenerUsuarios\":" + resultado + "}}")
                        .build();
            }

            if (query.contains("crearUsuario")) {
                String nombre = extraerString(query, "nombre");
                String apellidos = extraerString(query, "apellidos");
                String correo = extraerString(query, "correo");
                String password = extraerString(query, "password");

                if (nombre == null || apellidos == null || correo == null || password == null) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body("{\"error\":\"Faltan parámetros para crearUsuario\"}")
                            .build();
                }

                usuarioService.crearUsuario(nombre, apellidos, correo, password, context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"crearUsuario\":\"Usuario creado correctamente\"}}")
                        .build();
            }

            if (query.contains("modificarUsuario")) {
                Integer idUsuario = extraerEntero(query, "idUsuario");
                String nombre = extraerString(query, "nombre");
                String apellidos = extraerString(query, "apellidos");
                String correo = extraerString(query, "correo");
                String password = extraerString(query, "password");

                if (idUsuario == null || nombre == null || apellidos == null || correo == null || password == null) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body("{\"error\":\"Faltan parámetros para modificarUsuario\"}")
                            .build();
                }

                usuarioService.modificarUsuario(idUsuario, nombre, apellidos, correo, password, context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"modificarUsuario\":\"Usuario modificado correctamente\"}}")
                        .build();
            }

            if (query.contains("eliminarUsuario")) {
                Integer idUsuario = extraerEntero(query, "idUsuario");

                if (idUsuario == null) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body("{\"error\":\"Falta parámetro idUsuario\"}")
                            .build();
                }

                usuarioService.eliminarUsuario(idUsuario, context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"eliminarUsuario\":\"Usuario eliminado correctamente\"}}")
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Consulta GraphQL no soportada\"}")
                    .build();

        } catch (Exception e) {
            context.getLogger().severe("Error en usuariosGraphQL: " + e.getMessage());

            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"" + escaparJson(e.getMessage()) + "\"}")
                    .build();
        }
    }

    private Integer extraerEntero(String query, String campo) {
        try {
            String patron = campo + ":";
            int inicio = query.indexOf(patron);
            if (inicio == -1) return null;

            inicio += patron.length();
            while (inicio < query.length() && Character.isWhitespace(query.charAt(inicio))) {
                inicio++;
            }

            int fin = inicio;
            while (fin < query.length() && Character.isDigit(query.charAt(fin))) {
                fin++;
            }

            return Integer.parseInt(query.substring(inicio, fin));
        } catch (Exception e) {
            return null;
        }
    }

    private String extraerString(String query, String campo) {
        try {
            String patron = campo + ":";
            int inicio = query.indexOf(patron);
            if (inicio == -1) return null;

            inicio = query.indexOf("\"", inicio);
            if (inicio == -1) return null;

            int fin = query.indexOf("\"", inicio + 1);
            if (fin == -1) return null;

            return query.substring(inicio + 1, fin);
        } catch (Exception e) {
            return null;
        }
    }

    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}