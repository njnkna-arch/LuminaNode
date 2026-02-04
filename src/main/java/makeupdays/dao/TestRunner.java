package makeupdays.dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=== 診断スタート ===");

        // 1. ドライバがあるかチェック
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            System.out.println("✅ OK: MariaDBのドライバは見つかりました");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ NG: ドライバが見つかりません！");
            System.err.println("→ mariadb-java-client.jar がビルドパスに含まれていません");
            return;
        }

        // 2. データベースに繋がるかチェック
        String url = "jdbc:mariadb://localhost:3306/makeupdays_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=JST";
        String user = "root";
        String pass = "2Tooput5"; // ★あなたのパスワードに合わせてください！

        try (Connection conn = DriverManager.getConnection(url, user, pass)) {
            System.out.println("✅ OK: データベース接続に成功しました！");
            System.out.println("→ パスワードも合っています");
        } catch (Exception e) {
            System.err.println("❌ NG: データベース接続に失敗しました");
            System.err.println("エラー内容: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== 診断終了 ===");
    }
}