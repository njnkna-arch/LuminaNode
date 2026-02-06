package com.lumina.util;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * データベース接続管理。
 * 「環境変数の取得失敗」を隠さず、即座に報告する検証用構成です。
 */
public class DBConnection {
    public static Connection getConnection() throws Exception {
        // 1. ドライバのロード確認
        Class.forName("com.mysql.cj.jdbc.Driver");
        
        // 2. 環境変数からの取得（ここが「拒絶」の分岐点）
        String host = System.getenv("MYSQLHOST");
        String pass = System.getenv("MYSQLPASSWORD");
        String db   = System.getenv("MYSQLDATABASE");
        String user = System.getenv("MYSQLUSER");

        // --- 【真の原因特定セクション】 ---
        // 取得した値が一つでもnull（設定ミス）なら、接続を試みる前に例外を投げます。
        if (host == null || pass == null || db == null || user == null) {
            StringBuilder missing = new StringBuilder("【致命的欠点】以下の環境変数がRailwayで見つかりません: ");
            if (host == null) missing.append("MYSQLHOST ");
            if (pass == null) missing.append("MYSQLPASSWORD ");
            if (db == null)   missing.append("MYSQLDATABASE ");
            if (user == null) missing.append("MYSQLUSER ");
            
            // これが出た場合は、プログラムではなくRailwayのVariables画面の設定名ミスです
            throw new Exception(missing.toString());
        }

        // 3. 接続URLの組み立て
        // 前回のSSLエラー(CERT_E_UNTRUSTEDROOT)を確実に回避するための設定を付与
        String url = String.format("jdbc:mysql://%s:3306/%s" + 
                                   "?useSSL=false" + 
                                   "&allowPublicKeyRetrieval=true" + 
                                   "&serverTimezone=JST" + 
                                   "&characterEncoding=UTF-8", 
                                    host, db);

        // 4. 接続実行
        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (Exception e) {
            // ここで止まる場合は「値は取れているが、その値（パスワード等）が間違っている」ことが確定します
            throw new Exception("値は取得できましたが、MySQLに拒絶されました。パスワードが古い可能性があります: " + e.getMessage());
        }
    }
}
