import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints; // ⭐️ (ใหม่)
import java.awt.GridBagLayout; // ⭐️ (ใหม่)
import java.awt.Insets; // ⭐️ (ใหม่)
import java.util.ArrayList;
import javax.swing.JButton; // ⭐️ (ใหม่)
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane; // ⭐️ (ใหม่)
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea; // ⭐️ (ใหม่)
import javax.swing.JTextField; // ⭐️ (ใหม่)
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;

public class TeacherPortalGUI extends JFrame {

    private String currentUsername;
    private Role currentUserRole;
    private DatabaseManager dbManager;
    private Teacher currentTeacher;
    private DefaultTableModel homeroomModel;
    private JTable homeroomTable;

    public TeacherPortalGUI(Role role, String username) {
        this.currentUserRole = role;
        this.currentUsername = username;
        this.dbManager = new DatabaseManager();

        this.currentTeacher = dbManager.getTeacherById(this.currentUsername);
        
        setTitle("👨‍🏫 Teacher Portal (Welcome: " + (currentTeacher != null ? currentTeacher.name : username) + ")");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + (currentTeacher != null ? currentTeacher.name : username));
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(welcomeLabel);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📋 My Homeroom", createHomeroomTab());
        tabbedPane.addTab("🗓️ Request Leave", createLeaveRequestTab()); // ⭐️ (ใหม่)
        
        add(tabbedPane, BorderLayout.CENTER);

        loadHomeroomStudents();
        setVisible(true);
    }

    private JPanel createHomeroomTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        homeroomModel = new DefaultTableModel(new Object[]{"ID", "Name", "Major", "Year", "GPA", "Status"}, 0);
        homeroomTable = new JTable(homeroomModel);
        panel.add(new JScrollPane(homeroomTable), BorderLayout.CENTER);
        return panel;
    }

    // ⭐️ (ใหม่) สร้าง Tab สำหรับส่งใบลา
    private JPanel createLeaveRequestTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);
        JTextArea reasonArea = new JTextArea(5, 20);
        reasonArea.setLineWrap(true);
        reasonArea.setWrapStyleWord(true);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(startDateField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(endDateField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Reason:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(new JScrollPane(reasonArea), gbc);

        JButton submitButton = new JButton("Submit Leave Request");
        submitButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        submitButton.addActionListener(e -> {
            String start = startDateField.getText();
            String end = endDateField.getText();
            String reason = reasonArea.getText();
            if (start.isEmpty() || end.isEmpty() || reason.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = dbManager.submitLeaveRequest(this.currentUsername, start, end, reason);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Leave request submitted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                startDateField.setText("");
                endDateField.setText("");
                reasonArea.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit request.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(formPanel, BorderLayout.NORTH);
        panel.add(submitButton, BorderLayout.SOUTH);
        return panel;
    }

    private void loadHomeroomStudents() {
        homeroomModel.setRowCount(0);
        ArrayList<Student> students = dbManager.getHomeroomStudents(this.currentUsername);
        for (Student s : students) {
            homeroomModel.addRow(new Object[]{
                s.id, s.name, s.major, s.year, s.gpa, s.status.name()
            });
        }
    }
}