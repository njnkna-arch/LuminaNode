package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class DBConnection {
    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Railwayの環境変数を取得
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        // ホストが空の場合はローカルとみなす
        if (host == null || host.isEmpty()) {
            System.out.println("🏠 ローカルDB接続モード");
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST", "root", "root");
        }

        // ポートの自動判定
        // 内部ホスト(railway.internal)なら3306、外部ホストならそのまま使用
        String finalPort = host.contains("railway.internal") ? "3306" : port;

        // 接続URLの組み立て
        String url = String.format("jdbc:mysql://%s:%s/%s", host, finalPort, dbName);
        
        // 接続プロパティの設定（最新MySQL 8.0/9.0対応）
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("serverTimezone", "JST");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("useSSL", "false");
        props.setProperty("connectTimeout", "10000"); // 10秒待機

        System.out.println("🔍 [診断] 接続を試みます: " + url + " (User: " + user + ")");

        try {
            Connection conn = DriverManager.getConnection(url, props);
            System.out.println("✅ [成功] データベースに繋がりました！");
            return conn;
        } catch (Exception e) {
            System.err.println("❌ [失敗] 接続エラーが発生しました。");
            System.err.println("   エラー内容: " + e.getMessage());
            // エラーをブラウザ側にも詳しく伝える
            throw new Exception("DB接続失敗: " + e.getMessage() + " (URL: " + url + ")", e);
        }
    }
}
