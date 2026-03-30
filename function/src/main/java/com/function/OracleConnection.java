package com.function;

import com.microsoft.azure.functions.ExecutionContext;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class OracleConnection {

    private static final String[] WALLET_FILES = {
        "cwallet.sso",
        "ewallet.p12",
        "tnsnames.ora",
        "sqlnet.ora",
        "ojdbc.properties"
    };

    private static String finalWalletPath = null;
    private static boolean initialized = false;

    private static synchronized void initializeWallet(ExecutionContext context) throws Exception {
        if (initialized) {
            return;
        }

        File tempDir = new File(
            System.getProperty("java.io.tmpdir"),
            "oracle_wallet_" + System.currentTimeMillis()
        );

        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IllegalStateException("No se pudo crear el directorio temporal del wallet");
        }

        context.getLogger().info("Extrayendo wallet a: " + tempDir.getAbsolutePath());

        for (String fileName : WALLET_FILES) {
            try (InputStream is = OracleConnection.class.getClassLoader().getResourceAsStream("wallet/" + fileName)) {

                if (is == null) {
                    throw new IllegalStateException("No se encontró el archivo wallet/" + fileName + " en resources");
                }

                File targetFile = new File(tempDir, fileName);
                Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                context.getLogger().info("Archivo copiado: " + fileName);
            }
        }

        finalWalletPath = tempDir.getAbsolutePath();

        System.setProperty("oracle.net.tns_admin", finalWalletPath);
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");

        initialized = true;

        context.getLogger().info("Wallet listo en: " + finalWalletPath);
        context.getLogger().info("oracle.net.tns_admin = " + System.getProperty("oracle.net.tns_admin"));
    }

    public static Connection getConnection(ExecutionContext context) throws Exception {
        initializeWallet(context);

        String user = System.getenv("DB_USER");
        String pass = System.getenv("DB_PASSWORD");
        String url = System.getenv("DB_URL");

        if (user == null || user.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno DB_USER");
        }

        if (pass == null || pass.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno DB_PASSWORD");
        }

        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno DB_URL");
        }

        context.getLogger().info("Intentando conexión con DB_URL: " + url);
        context.getLogger().info("Usando TNS_ADMIN: " + finalWalletPath);

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("oracle.net.tns_admin", finalWalletPath);
        props.setProperty(
            "oracle.net.wallet_location",
            "(SOURCE=(METHOD=FILE)(METHOD_DATA=(DIRECTORY=" + finalWalletPath + ")))"
        );

        return DriverManager.getConnection(url, props);
    }
}