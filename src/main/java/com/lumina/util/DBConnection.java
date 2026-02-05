package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * データベース接続管理。
 * RailwayのMySQL 9.x系で発生する「Public Key Retrieval」や「SSL」のエラーを
 * 完全に回避するための接続文字列を設定しています。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // JDBCドライバのロード
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // Railwayの環境変数を取得
        String host = System.getenv("MYSQLHOST");
        String port = System.getenv("MYSQLPORT");
        String dbName = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");

        String url;
        if (host != null && !host.isEmpty()) {
            // 【重要】最新のMySQLに対応するためのURLオプションをすべて含めています
            url = String.format(
                "jdbc:mysql://%s:%s/%s?serverTimezone=JST" +
                "&useUnicode=true&characterEncoding=UTF-8" +
                "&allowPublicKeyRetrieval=true" +
                "&useSSL=false", 
                host, port, dbName
            );
            System.out.println("🚀 Railway DBに接続中: " + host);
        } else {
            // ローカル（Eclipse）環境用
            url = "jdbc:mysql://localhost:3306/luminadb?serverTimezone=JST&allowPublicKeyRetrieval=true&useSSL=false";
            user = "root";
            pass = "root"; 
        }

        return DriverManager.getConnection(url, user, pass);
    }
}
