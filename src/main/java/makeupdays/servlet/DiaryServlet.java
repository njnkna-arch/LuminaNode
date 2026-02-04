package makeupdays.servlet;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import makeupdays.dao.DiaryDAO;

@WebServlet("/diary")
@MultipartConfig
public class DiaryServlet extends HttpServlet {
    private DiaryDAO dao = new DiaryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        try {
            resp.getWriter().write(dao.getAllDiariesAsJson());
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        System.out.println("====== 保存処理スタート ======"); // ★ログ出力

        try {
            String date = req.getParameter("date");
            String bodyText = req.getParameter("bodyText");
            String emotionCode = req.getParameter("emotionCode");
            
            System.out.println("データ受信: 日付=" + date + ", 本文=" + bodyText); // ★ログ出力

            Part filePart = req.getPart("photoFile");
            String photoPath = ""; 

            if (filePart != null && filePart.getSize() > 0) {
                String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
                String appPath = req.getServletContext().getRealPath("");
                String saveDir = appPath + File.separator + "uploads";
                
                System.out.println("写真保存場所: " + saveDir); // ★ログ出力

                File fileSaveDir = new File(saveDir);
                if (!fileSaveDir.exists()) {
                    fileSaveDir.mkdir();
                    System.out.println("フォルダを作成しました"); // ★ログ出力
                }

                filePart.write(saveDir + File.separator + fileName);
                photoPath = "uploads/" + fileName;
                System.out.println("写真保存完了: " + photoPath); // ★ログ出力
            } else {
                System.out.println("写真は選択されていません"); // ★ログ出力
            }

            // DAO呼び出し
            System.out.println("データベース保存を開始します..."); // ★ログ出力
            boolean success = dao.saveDiary(date, photoPath, bodyText, emotionCode);
            System.out.println("データベース保存結果: " + success); // ★ログ出力
            
            if (success) {
                resp.getWriter().write("{\"success\": true}");
            } else {
                System.err.println("【失敗】DAOが false を返しました"); // ★エラーログ
                resp.getWriter().write("{\"success\": false}");
            }

        } catch (Exception e) {
            System.err.println("【例外発生】Servletでエラーが起きました"); // ★エラーログ
            e.printStackTrace(); 
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}");
        }
        System.out.println("====== 保存処理終了 ======");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // (省略: 以前と同じでOK)
        resp.setContentType("application/json; charset=UTF-8");
        try {
            String date = req.getParameter("date");
            boolean success = dao.deleteDiary(date);
            resp.getWriter().write("{\"success\": " + success + "}");
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("{\"success\": false}");
        }
    }
}