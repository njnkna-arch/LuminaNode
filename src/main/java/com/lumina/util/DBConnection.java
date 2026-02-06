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
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // 2. Railwayの設定画面（Variables）から最新の値を読み取る
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        // 3. Railway上での動作なら内部ネットワーク(3306)を、ローカルならlocalhostを使用
        String url;
        if (host != null && !host.isEmpty()) {
            // Railway内部では常に3306ポートを使用するのが最も安定します
            url = String.format("jdbc:mysql://%s:3306/%s", host, dbName);
        } else {
            // あなたのPCでのテスト用（Eclipse用）
            url = "jdbc:mysql://localhost:3306/luminadb";
            user = "root";
            pass = "root"; // ローカルのパスワード
        }

        // 4. 最新のMySQL/MariaDBで「接続拒否」を防ぐための重要なプロパティ設定
        Properties props = new Properties();
        props.setProperty("user", user != null ? user : "root");
        props.setProperty("password", pass != null ? pass : "");
        props.setProperty("serverTimezone", "JST");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");
        
        // 【重要】これがないとパスワードが合っていても拒否されることがあります
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("useSSL", "false");
        
        // サーバーが重い場合に備えてタイムアウトを長めに設定
        props.setProperty("connectTimeout", "15000");

        try {
            return DriverManager.getConnection(url, props);
        } catch (Exception e) {
            // エラーが出た場合、原因を特定しやすくするために詳細なメッセージを投げます
            String errorMsg = e.getMessage();
            if (errorMsg.contains("Access denied")) {
                throw new Exception("【接続拒否】パスワードがMySQL側と一致しません。RailwayのVariablesを再確認してください。");
            }
            throw new Exception("DB接続エラー: " + errorMsg);
        }
    }
}
