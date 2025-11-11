import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * (ไฟล์ใหม่)
 * คลาส DTO (Data Transfer Object) สำหรับเก็บข้อมูลสรุปการเงิน
 * ใช้สำหรับแสดงผลในตาราง financeTable (ตารางหลัก)
 */
public class FinanceSummary {
    String studentId;
    String studentName;
    double totalDue;
    double totalPaid;
    double balance;
    String status; // "PAID", "PENDING", "OVERDUE"

    public FinanceSummary(String studentId, String studentName, double totalDue, double totalPaid) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.totalDue = totalDue;
        this.totalPaid = totalPaid;
        this.balance = totalDue - totalPaid;
        
        if (this.balance <= 0) {
            this.status = "PAID";
        } else {
            this.status = "PENDING"; // (คุณสามารถเพิ่ม Logic "OVERDUE" ทีหลังได้)
        }
    }
}

/**
 * (ไฟล์ใหม่)
 * คลาส Model สำหรับเก็บข้อมูลใบแจ้งหนี้ (Invoice)
 */
class Invoice {
    int id;
    String studentId;
    String semesterId;
    String issueDate;
    String dueDate;
    double totalAmount;
    String status; // PENDING, PAID, CANCELED

    public Invoice(int id, String studentId, String semesterId, String issueDate, String dueDate, double totalAmount, String status) {
        this.id = id;
        this.studentId = studentId;
        this.semesterId = semesterId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.status = status;
    }
    
    // Override toString() เพื่อให้แสดงผลใน JComboBox ได้ง่าย
    @Override
    public String toString() {
        return String.format("Invoice #%d (Amount: %.2f, Status: %s)", id, totalAmount, status);
    }
}

/**
 * (ไฟล์ใหม่)
 * คลาส Model สำหรับเก็บข้อมูลการชำระเงิน (Transaction)
 */
class Transaction {
    int id;
    int invoiceId;
    String studentId;
    String paymentDate;
    double amountPaid;
    String paymentMethod; // ONLINE, COUNTER, TRANSFER
    String referenceCode;

    // Constructor สำหรับสร้าง Transaction ใหม่ (ยังไม่มี ID)
    public Transaction(int invoiceId, String studentId, double amountPaid, String paymentMethod, String referenceCode) {
        this.invoiceId = invoiceId;
        this.studentId = studentId;
        this.amountPaid = amountPaid;
        this.paymentMethod = paymentMethod;
        this.referenceCode = referenceCode;
        this.paymentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    // Constructor สำหรับดึงข้อมูลจาก DB (มี ID แล้ว)
    public Transaction(int id, int invoiceId, String studentId, String paymentDate, double amountPaid, String paymentMethod, String referenceCode) {
        this(invoiceId, studentId, amountPaid, paymentMethod, referenceCode);
        this.id = id;
        this.paymentDate = paymentDate;
    }
    class FinancialAid {
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

/**
 * (ไฟล์ใหม่)
 * คลาส Model สำหรับเก็บรายการย่อยใน Invoice
 */
class InvoiceItem {
    int id;
    String description;
    double amount;

    public InvoiceItem(int id, String description, double amount) {
        this.id = id;
        this.description = description;
        this.amount = amount;
    }
}
}