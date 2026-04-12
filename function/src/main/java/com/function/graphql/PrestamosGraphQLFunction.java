package com.function.graphql;

import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.function.service.PrestamoService;
import com.function.util.JsonUtil;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

public class PrestamosGraphQLFunction {

    private final PrestamoService prestamoService = new PrestamoService();

    @FunctionName("prestamosGraphQL")
    public HttpResponseMessage ejecutar(
            @HttpTrigger(
                name = "req",
                methods = { HttpMethod.POST },
                authLevel = AuthorizationLevel.ANONYMOUS,
                route = "graphql/prestamos"
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

            context.getLogger().info("GraphQL prestamos query: " + query);

            if (query.contains("obtenerPrestamos")) {
                String resultado = prestamoService.obtenerPrestamos(context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"obtenerPrestamos\":" + resultado + "}}")
                        .build();
            }

            if (query.contains("crearPrestamo")) {
                Integer idUsuario = extraerEntero(query, "idUsuario");
                Integer idLibro = extraerEntero(query, "idLibro");

                if (idUsuario == null || idLibro == null) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body("{\"error\":\"Faltan parámetros para crearPrestamo\"}")
                            .build();
                }

                prestamoService.crearPrestamo(idUsuario, idLibro, context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"crearPrestamo\":\"Préstamo creado correctamente\"}}")
                        .build();
            }

            if (query.contains("devolverPrestamo")) {
                Integer idPrestamo = extraerEntero(query, "idPrestamo");

                if (idPrestamo == null) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body("{\"error\":\"Falta parámetro idPrestamo\"}")
                            .build();
                }

                prestamoService.devolverPrestamo(idPrestamo, context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"devolverPrestamo\":\"Préstamo devuelto correctamente\"}}")
                        .build();
            }

            if (query.contains("eliminarPrestamo")) {
                Integer idPrestamo = extraerEntero(query, "idPrestamo");

                if (idPrestamo == null) {
                    return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                            .header("Content-Type", "application/json")
                            .body("{\"error\":\"Falta parámetro idPrestamo\"}")
                            .build();
                }

                prestamoService.eliminarPrestamo(idPrestamo, context);

                return request.createResponseBuilder(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body("{\"data\":{\"eliminarPrestamo\":\"Préstamo eliminado correctamente\"}}")
                        .build();
            }

            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\":\"Consulta GraphQL no soportada\"}")
                    .build();

        } catch (Exception e) {
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

    private String escaparJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}