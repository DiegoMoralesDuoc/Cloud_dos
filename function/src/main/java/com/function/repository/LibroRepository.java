package com.function.repository;

import java.util.Optional;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import com.function.OracleConnection;
import com.function.util.JsonUtil;
import com.function.Function;
import com.function.repository.*;

public class LibroRepository {
    
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
}
