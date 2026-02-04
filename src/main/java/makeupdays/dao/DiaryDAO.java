package makeupdays.dao; // ★「com.」を削除しました

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson; 

public class DiaryDAO {
    // 接続設定 (パスワードが空の場合は "" にしてください)
    private static final String JDBC_URL = "jdbc:mariadb://localhost:3306/makeupdays_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=JST";
    private static final String DB_USER = "root"; 
    private static final String DB_PASS = "2Tooput5"; // ★違う場合は "" や "password" に変更！

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            return DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver not found", e);
        }
    }

    // 保存・更新
    public boolean saveDiary(String date, String photoPath, String bodyText, String emotionCode) {
        String sqlCheck = "SELECT count(*) FROM DIARY WHERE date = ?";
        String sqlInsert = "INSERT INTO DIARY (date, photo_path, body_text, emotion_code) VALUES (?, ?, ?, ?)";
        String sqlUpdateWithPhoto = "UPDATE DIARY SET photo_path=?, body_text=?, emotion_code=? WHERE date=?";
        String sqlUpdateTextOnly = "UPDATE DIARY SET body_text=?, emotion_code=? WHERE date=?";

        try (Connection conn = getConnection()) {
            PreparedStatement psCheck = conn.prepareStatement(sqlCheck);
            psCheck.setString(1, date);
            ResultSet rs = psCheck.executeQuery();
            rs.next();
            boolean exists = rs.getInt(1) > 0;

            PreparedStatement ps;
            if (exists) {
                if (photoPath != null && !photoPath.isEmpty()) {
                    ps = conn.prepareStatement(sqlUpdateWithPhoto);
                    ps.setString(1, photoPath);
                    ps.setString(2, bodyText);
                    ps.setString(3, emotionCode);
                    ps.setString(4, date);
                } else {
                    ps = conn.prepareStatement(sqlUpdateTextOnly);
                    ps.setString(1, bodyText);
                    ps.setString(2, emotionCode);
                    ps.setString(3, date);
                }
            } else {
                ps = conn.prepareStatement(sqlInsert);
                ps.setString(1, date);
                ps.setString(2, (photoPath == null) ? "" : photoPath);
                ps.setString(3, bodyText);
                ps.setString(4, emotionCode);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 削除
    public boolean deleteDiary(String date) {
        String sql = "DELETE FROM DIARY WHERE date = ?";
        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, date);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 全件取得
    public String getAllDiariesAsJson() {
        List<Map<String, String>> list = new ArrayList<>();
        String sql = "SELECT * FROM DIARY";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, String> map = new HashMap<>();
                map.put("date", rs.getString("date"));
                map.put("photoPath", rs.getString("photo_path"));
                map.put("bodyText", rs.getString("body_text"));
                map.put("emotionCode", rs.getString("emotion_code"));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new Gson().toJson(list);
    }
}