package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        // ポート番号の自動調整
        // mysql.railway.internal（内部ネットワーク）を使う場合は、ポートを強制的に3306にします
        if (host != null && host.contains("railway.internal")) {
            port = "3306";
        }

        String url;
        if (host != null && !host.isEmpty()) {
            url = String.format(
                "jdbc:mysql://%s:%s/%s?serverTimezone=JST&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true&useSSL=false&connectTimeout=5000", 
                host, port, dbName
            );
            System.out.println("🚀 [Railway] 接続先: " + host + ":" + port + " (" + dbName + ")");
        } else {
            url = "jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST&allowPublicKeyRetrieval=true&useSSL=false";
            user = "root"; pass = "root";
        }

        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            System.err.println("❌ 接続エラー詳細: " + e.getMessage());
            // エラーをフロントエンドにわかりやすく伝える
            throw new Exception("データベースに繋がらないよ！(Host:" + host + ", Port:" + port + ") " + e.getMessage());
        }
    }
}
