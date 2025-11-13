import java.util.Date;
import java.text.SimpleDateFormat;

public class FinancialAid {
    int id;
    String studentId;
    String semesterId;
    int invoiceId; // ⭐️ ใช้เพื่อบอกว่าส่วนลดนี้จะไปหักกับบิลไหน
    String aidType; // SCHOLARSHIP, DISCOUNT
    String description;
    double amount;
    String applyDate;

    public FinancialAid(String studentId, String semesterId, int invoiceId, String aidType, String description, double amount) {
        this.studentId = studentId;
        this.semesterId = semesterId;
        this.invoiceId = invoiceId;
        this.aidType = aidType;
        this.description = description;
        this.amount = amount;
        this.applyDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }
}