package com.function.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void logPublicIp(ExecutionContext context) {
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

    public static String extraerCampo(String json, String campo) {
        Pattern pattern = Pattern.compile("\"" + campo + "\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IllegalArgumentException("Falta el campo: " + campo);
        }

        return matcher.group(1);
    }

    public static <T> T fromJson(String json, TypeReference<T> typeReference) throws Exception {
        return mapper.readValue(json, typeReference);
    }

    public static String toJson(Object obj) throws Exception {
        return mapper.writeValueAsString(obj);
    }

    public static String resultSetToJson(ResultSet rs) throws Exception {
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

    public static String escapeJson(String text) {
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
}