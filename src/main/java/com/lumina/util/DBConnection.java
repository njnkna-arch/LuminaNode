package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * データベース接続管理。
 * Railwayの内部ネットワーク（ポート3306）を使用し、
 * 環境変数から取得した情報のみで接続を確立する最小限の構成です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. JDBCドライバのロード
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // 2. RailwayのVariablesから値を直接取得
        String host = System.getenv("MYSQLHOST");
        String user = System.getenv("MYSQLUSER");
        String pass = System.getenv("MYSQLPASSWORD");
        String dbName = System.getenv("MYSQLDATABASE");

        // 3. 接続URLの組み立て
        // Railway内部ではポート3306で固定です。
        // 最新MySQLの認証エラーを避けるための2つの必須オプションのみ付与しています。
        String url = String.format("jdbc:mysql://%s:3306/%s?serverTimezone=JST&allowPublicKeyRetrieval=true&useSSL=false", 
                                    host, dbName);

        // 4. 接続を実行して返す（失敗時は例外がそのままServletへ飛びます）
        return DriverManager.getConnection(url, user, pass);
    }
}
