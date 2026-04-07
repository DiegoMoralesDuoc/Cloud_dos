package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.function.OracleConnection;
import com.function.util.JsonUtil;
import com.microsoft.azure.functions.ExecutionContext;

public class LibroRepository {

    public String obtenerLibros(ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM LIBROS");
             ResultSet rs = ps.executeQuery()) {

            return JsonUtil.resultSetToJson(rs);
        }
    }
}