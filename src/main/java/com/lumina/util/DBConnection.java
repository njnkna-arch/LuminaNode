package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * データベース接続管理。
 * RailwayのVariablesから最新の情報を読み取り、
 * MySQL 9.x系の認証方式（allowPublicKeyRetrieval等）に完全対応させた版です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. ドライバのロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new Exception("MySQL JDBC Driverが見つかりません。pom.xmlを確認してください。");
        }
        
        // 2. Railwayの設定画面（Variables）から最新の値を読み取る
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        // 3. 接続 URL の構築
        String url;
        if (host != null && !host.isEmpty()) {
            // Railway内部ネットワークでは3306ポートが標準です
            // もしVariablesのMYSQLPORTが3306以外（外部用）でも、内部接続なら3306を優先します
            String connectPort = host.contains("railway.internal") ? "3306" : (port != null ? port : "3306");
            url = String.format("jdbc:mysql://%s:%s/%s", host, connectPort, dbName);
        } else {
            // ローカルPC（Eclipse等）でのテスト用
            url = "jdbc:mysql://localhost:3306/luminadb";
            user = "root";
            pass = "root";
        }

        // 4. 接続プロパティの設定
        Properties props = new Properties();
        props.setProperty("user", user != null ? user : "root");
        props.setProperty("password", pass != null ? pass : "");
        props.setProperty("serverTimezone", "JST");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        
        // 【重要】認証とSSLに関するエラーを回避する設定
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("useSSL", "false");
        
        // サーバーの応答が遅い場合に備えたタイムアウト設定（ミリ秒）
        props.setProperty("connectTimeout", "20000"); // 20秒待機
        props.setProperty("socketTimeout", "30000");  // 30秒待機

        System.out.println("🔄 [DB接続試行] Target: " + url);

        try {
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("✅ [DB接続成功]");
            return conn;
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            System.err.println("❌ [DB接続失敗] 理由: " + errorMsg);
            
            if (errorMsg.contains("Access denied")) {
                throw new Exception("【認証エラー】パスワードまたはユーザー名が正しくありません。RailwayのVariablesを再確認し、MySQLを再起動してください。");
            } else if (errorMsg.contains("communications link failure") || errorMsg.contains("Could not create connection")) {
                throw new Exception("【通信エラー】データベースサーバーが応答していません。Railway上でMySQLサービスをRestartしてください。");
            }
            throw new Exception("データベース接続に失敗しました: " + errorMsg);
        }
    }
}
