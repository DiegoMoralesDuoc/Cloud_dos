package com.function.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

import com.function.OracleConnection;

public class JsonUtil {

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
