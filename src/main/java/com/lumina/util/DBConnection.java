package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * データベース接続管理。
 * 接続エラーを確実に回避するため、パスワードを直接指定したデバッグ用最終設定です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. JDBCドライバのロード
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new Exception("MySQLドライバが見つかりません。");
        }
        
        // 2. 接続情報の直接指定（デバッグ用）
        // ※ 本来は環境変数から読み取りますが、確実に繋ぐために直接書き込みます。
        String host = "mysql.railway.internal";
        String port = "3306";
        String dbName = "railway";
        String user = "root";
        
        // あなたが教えてくれたパスワードをここに直接入れます
        String pass = "NhwnfJGGBSSkcfrnsiUuEhFYmWeakAir";

        // 3. 接続URLの構築
        String url = String.format("jdbc:mysql://%s:%s/%s", host, port, dbName);

        // MySQL 9.4のセキュリティ設定を確実に突破するための設定
        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", pass);
        props.setProperty("serverTimezone", "JST");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        
        // 【最重要】パスワード送信を許可し、SSLエラーを無視する
        props.setProperty("allowPublicKeyRetrieval", "true"); 
        props.setProperty("useSSL", "false"); 
        
        // 接続待機設定
        props.setProperty("connectTimeout", "15000");
        props.setProperty("socketTimeout", "30000");

        System.out.println("🔍 [DB接続] 直接指定されたパスワードで接続を試みます: " + url);
        
        try {
            return DriverManager.getConnection(url, props);
        } catch (Exception e) {
            String msg = e.getMessage();
            System.err.println("❌ [接続失敗] 理由: " + msg);
            
            if (msg.contains("Access denied")) {
                throw new Exception("【接続拒否】直書きしたパスワードがMySQL側と一致しません。");
            }
            throw new Exception("データベース接続失敗: " + msg);
        }
    }
}
