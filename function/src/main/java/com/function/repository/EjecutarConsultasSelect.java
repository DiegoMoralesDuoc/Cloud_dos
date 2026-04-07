package com.function.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.function.OracleConnection;
import com.function.util.JsonUtil;
import com.microsoft.azure.functions.ExecutionContext;

public class EjecutarConsultasSelect {

    public String ejecutarConsultaSelect(String sql, ExecutionContext context) throws Exception {
        try (Connection conn = OracleConnection.getConnection(context);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            context.getLogger().info("Ejecutando query: " + sql);
            return JsonUtil.resultSetToJson(rs);
        }
    }
}