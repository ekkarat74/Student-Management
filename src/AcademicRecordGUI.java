import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

/**
 * ⭐️ (ไฟล์ใหม่)
 * หน้าต่างนี้ใช้สำหรับจัดการผลการเรียน (Academic Record) ของนักศึกษาทีละคน
 * ถูกเรียกใช้จาก StudentManagementGUI
 */
public class AcademicRecordGUI extends JDialog {

    private DatabaseManager dbManager;
    private String studentId;
    private String studentName;

    private DefaultTableModel gradesModel;
    private JTable gradesTable;
    private JLabel gpaLabel;

    public AcademicRecordGUI(Frame parent, DatabaseManager dbManager, String studentId, String studentName) {
        super(parent, "Academic Record: " + studentName, true); // 'true' for modal
        this.dbManager = dbManager;
        this.studentId = studentId;
        this.studentName = studentName;

        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- Panel ด้านบน (แสดงชื่อและ GPAX) ---
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel nameLabel = new JLabel("Student: " + this.studentName + " (" + this.studentId + ")");
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        gpaLabel = new JLabel("Overall GPAX: 0.00");
        gpaLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        gpaLabel.setForeground(Color.BLUE);
        topPanel.add(nameLabel, BorderLayout.WEST);
        topPanel.add(gpaLabel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // --- Panel กลาง (ตารางเกรด) ---
        gradesModel = new DefaultTableModel(new Object[]{"Enroll ID", "Subject ID", "Subject Name", "Credits", "Grade"}, 0);
        gradesTable = new JTable(gradesModel);
        add(new JScrollPane(gradesTable), BorderLayout.CENTER);

        // --- Panel ล่าง (ปุ่ม) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton enrollButton = new JButton("➕ Enroll Student in Course");
        JButton setGradeButton = new JButton("✏️ Set/Update Grade");
        JButton closeButton = new JButton("Close");

        enrollButton.addActionListener(e -> enrollCourse());
        setGradeButton.addActionListener(e -> setGrade());
        closeButton.addActionListener(e -> dispose()); // ปิดหน้าต่าง

        buttonPanel.add(enrollButton);
        buttonPanel.add(setGradeButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // โหลดข้อมูลเมื่อเปิดหน้าต่าง
        loadData();
    }

    /**
     * โหลดข้อมูลเกรดและ GPAX ของนักศึกษา
     */
    private void loadData() {
        // 1. ล้างตาราง
        gradesModel.setRowCount(0);
        
        // 2. ดึงข้อมูลการลงทะเบียน
        ArrayList<EnrollmentRecord> records = dbManager.getEnrollmentsForStudent(this.studentId);
        for (EnrollmentRecord record : records) {
            gradesModel.addRow(new Object[]{
                record.enrollmentId,
                record.subjectId,
                record.subjectName,
                record.credits,
                record.grade
            });
        }
        
        // 3. คำนวณและอัปเดต GPAX
        // ⭐️ เมธอดนี้จะคำนวณและอัปเดตลง DB ให้อัตโนมัติ
        double gpax = dbManager.calculateAndUpdatStudentGPA(this.studentId);
        gpaLabel.setText(String.format("Overall GPAX: %.2f", gpax));
    }

    /**
     * Logic การลงทะเบียนวิชาใหม่
     */
    private void enrollCourse() {
        String subjectId = JOptionPane.showInputDialog(this, 
                "Enter Subject ID to enroll (e.g., CS101):", 
                "Enroll in Course", 
                JOptionPane.PLAIN_MESSAGE);
        
        if (subjectId != null && !subjectId.trim().isEmpty()) {
            boolean success = dbManager.enrollStudentInCourse(this.studentId, subjectId.trim());
            if (success) {
                JOptionPane.showMessageDialog(this, "Student enrolled successfully in " + subjectId);
                loadData(); // รีเฟรชตาราง
            } else {
                JOptionPane.showMessageDialog(this, "Failed to enroll. Subject ID might be invalid or student is already enrolled.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Logic การให้เกรด
     */
    private void setGrade() {
        int viewRow = gradesTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to set grade.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = gradesTable.convertRowIndexToModel(viewRow);
        int enrollmentId = (int) gradesModel.getValueAt(modelRow, 0); // ID อยู่ Column 0
        String subjectName = (String) gradesModel.getValueAt(modelRow, 2);

        // ⭐️ สร้าง Dropdown สำหรับเกรด
        String[] gradeOptions = {"A", "B+", "B", "C+", "C", "D+", "D", "F", "W", "N/A"};
        String newGrade = (String) JOptionPane.showInputDialog(this, 
                "Select grade for " + subjectName + ":", 
                "Set Grade", 
                JOptionPane.PLAIN_MESSAGE, 
                null, 
                gradeOptions, 
                gradeOptions[0]);

        if (newGrade != null) {
            boolean success = dbManager.updateGrade(enrollmentId, newGrade);
            if (success) {
                JOptionPane.showMessageDialog(this, "Grade updated successfully.");
                loadData(); // ⭐️ รีเฟรชตารางและคำนวณ GPAX ใหม่
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update grade.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}