import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class GradeManagementDialog extends JDialog {

    private DatabaseManager dbManager;
    private int enrollmentId;
    private String initialFinalGrade;

    private DefaultTableModel assignmentModel;
    private JTable assignmentTable;
    private ArrayList<AssignmentGrade> currentGrades;
    private JTextField txtFinalGrade;

    public GradeManagementDialog(JFrame parent, DatabaseManager dbManager, int enrollmentId, String studentName, String subjectName, String finalGrade) {
        super(parent, "Manage Grades for " + studentName, true);
        this.dbManager = dbManager;
        this.enrollmentId = enrollmentId;
        this.initialFinalGrade = finalGrade;

        setSize(600, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JLabel titleLabel = new JLabel("Subject: " + subjectName, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(titleLabel, BorderLayout.NORTH);

        // --- ตารางคะแนนย่อย ---
        assignmentModel = new DefaultTableModel(new Object[]{"Assignment", "Score", "Max Score"}, 0);
        assignmentTable = new JTable(assignmentModel);
        JScrollPane scrollPane = new JScrollPane(assignmentTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Assignment Scores"));
        add(scrollPane, BorderLayout.CENTER);

        // --- Panel ปุ่มด้านขวา (สำหรับตาราง) ---
        JPanel tableButtonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton btnAddScore = new JButton("Add Score");
        JButton btnEditScore = new JButton("Edit Score");
        JButton btnDeleteScore = new JButton("Delete Score");
        tableButtonPanel.add(btnAddScore);
        tableButtonPanel.add(btnEditScore);
        tableButtonPanel.add(btnDeleteScore);
        add(tableButtonPanel, BorderLayout.EAST);

        // --- Panel ด้านล่าง (สำหรับเกรด) ---
        JPanel finalGradePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        finalGradePanel.setBorder(BorderFactory.createTitledBorder("Final Grade"));
        finalGradePanel.add(new JLabel("Final Grade (e.g., A, B+):"));
        txtFinalGrade = new JTextField(initialFinalGrade, 5);
        JButton btnSaveFinalGrade = new JButton("Save Final Grade");
        finalGradePanel.add(txtFinalGrade);
        finalGradePanel.add(btnSaveFinalGrade);
        add(finalGradePanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnAddScore.addActionListener(e -> addScore());
        btnEditScore.addActionListener(e -> editScore());
        btnDeleteScore.addActionListener(e -> deleteScore());
        btnSaveFinalGrade.addActionListener(e -> saveFinalGrade());

        // --- โหลดข้อมูล ---
        loadAssignmentGrades();
    }

    private void loadAssignmentGrades() {
        assignmentModel.setRowCount(0);
        currentGrades = dbManager.getAssignmentGradesForEnrollment(enrollmentId);
        for (AssignmentGrade grade : currentGrades) {
            assignmentModel.addRow(new Object[]{grade.assignmentName, grade.score, grade.maxScore});
        }
    }

    private void addScore() {
        String name = JOptionPane.showInputDialog(this, "Enter assignment name (e.g., Homework 1):");
        if (name == null || name.trim().isEmpty()) return;

        try {
            double score = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter score:"));
            double maxScore = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter max score (e.g., 100):"));
            
            if (dbManager.addAssignmentGrade(enrollmentId, name.trim(), score, maxScore)) {
                loadAssignmentGrades();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add score.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editScore() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assignment to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AssignmentGrade selectedGrade = currentGrades.get(selectedRow);

        String name = (String) JOptionPane.showInputDialog(this, "Enter assignment name:", "Edit Score", JOptionPane.PLAIN_MESSAGE, null, null, selectedGrade.assignmentName);
        if (name == null || name.trim().isEmpty()) return;

        try {
            double score = Double.parseDouble((String) JOptionPane.showInputDialog(this, "Enter score:", "Edit Score", JOptionPane.PLAIN_MESSAGE, null, null, selectedGrade.score));
            double maxScore = Double.parseDouble((String) JOptionPane.showInputDialog(this, "Enter max score:", "Edit Score", JOptionPane.PLAIN_MESSAGE, null, null, selectedGrade.maxScore));

            if (dbManager.updateAssignmentGrade(selectedGrade.gradeId, name.trim(), score, maxScore)) {
                loadAssignmentGrades();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update score.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteScore() {
        int selectedRow = assignmentTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an assignment to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        AssignmentGrade selectedGrade = currentGrades.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete score for '" + selectedGrade.assignmentName + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbManager.deleteAssignmentGrade(selectedGrade.gradeId)) {
                loadAssignmentGrades();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete score.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFinalGrade() {
        String newGrade = txtFinalGrade.getText().trim().toUpperCase();
        if (newGrade.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Final Grade cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (dbManager.updateGrade(enrollmentId, newGrade)) {
            // (ดึง studentId มาจาก EnrollmentRecord ใน GUI หลัก)
            // เราต้องหา studentId... 
            // อ้อ! เราไม่ได้ส่ง studentId มา...
            // ไม่เป็นไร เราจะให้ GUI หลักเป็นคนคำนวณ GPA ทีหลัง
            JOptionPane.showMessageDialog(this, "Final grade saved successfully!");
            dispose(); // ปิดหน้าต่าง
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save final grade.", "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}