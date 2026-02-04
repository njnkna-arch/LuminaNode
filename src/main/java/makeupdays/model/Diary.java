package makeupdays.model; // ★「com.」を削除しました

public class Diary {
    private int id;
    private String date;
    private String photoPath;
    private String bodyText;
    private String emotionCode;

    public Diary(String date, String photoPath, String bodyText, String emotionCode) {
        this.date = date;
        this.photoPath = photoPath;
        this.bodyText = bodyText;
        this.emotionCode = emotionCode;
    }

    // Getter / Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }
    public String getEmotionCode() { return emotionCode; }
    public void setEmotionCode(String emotionCode) { this.emotionCode = emotionCode; }
}