package com.function.util;

import java.util.Optional;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;

public class ResponseUtil {

    public static HttpResponseMessage successResponse(
            HttpRequestMessage<Optional<String>> request,
            String mensaje) {

        return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"mensaje\":\"" + JsonUtil.escapeJson(mensaje) + "\"}")
                .build();
    }

    public static HttpResponseMessage crearRespuestaError(
            HttpRequestMessage<Optional<String>> request,
            HttpStatus status,
            String mensaje) {

        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"" + JsonUtil.escapeJson(mensaje) + "\"}")
                .build();
    }
}