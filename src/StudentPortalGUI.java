import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList; 
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable; 
import javax.swing.table.DefaultTableModel; 

public class StudentPortalGUI extends JFrame {

    private String currentUsername;
    private Role currentUserRole;
    private DatabaseManager dbManager;
    private Student currentStudent;
    private Teacher homeroomTeacher; 
    private DefaultTableModel scheduleModel;
    private JTable scheduleTable;

    public StudentPortalGUI(Role role, String username) {
        this.currentUserRole = role;
        this.currentUsername = username;
        this.dbManager = new DatabaseManager();

        this.currentStudent = dbManager.getStudentById(this.currentUsername);
        this.homeroomTeacher = dbManager.getHomeroomTeacher(this.currentUsername); 

        if (this.currentStudent == null) {
            JOptionPane.showMessageDialog(null, "Error: Could not find student data for ID: " + this.currentUsername);
            System.exit(0);
            return;
        }

        setTitle("🎓 Student Portal (Welcome: " + currentStudent.name + ")");
        setSize(800, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel welcomeLabel = new JLabel("Welcome, " + currentStudent.name);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("🏠 My Profile", createProfileTab());
        tabbedPane.addTab("🗓️ My Schedule", createScheduleTab()); 
        
        add(tabbedPane, BorderLayout.CENTER);

        loadStudentSchedule(); 
        setVisible(true);
    }

    private JPanel createProfileTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel photoLabel = new JLabel();
        photoLabel.setPreferredSize(new Dimension(150, 150));
        photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        photoLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        
        try {
            if (currentStudent.photoPath != null && !currentStudent.photoPath.isEmpty() && new File(currentStudent.photoPath).exists()) {
                ImageIcon icon = new ImageIcon(currentStudent.photoPath);
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                photoLabel.setIcon(new ImageIcon(img));
                photoLabel.setText("");
            } else {
                photoLabel.setText("No Photo");
            }
        } catch (Exception e) {
            photoLabel.setText("Photo Error");
            e.printStackTrace();
        }
        
        panel.add(photoLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        int y = 0;
        
        String hrTeacherName = (homeroomTeacher != null) ? homeroomTeacher.name : "N/A";
        infoPanel.add(createReadOnlyField("Homeroom Teacher:", hrTeacherName, Color.BLUE), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Student ID:", currentStudent.id), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Full Name:", currentStudent.name), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Email:", currentStudent.email), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Phone:", currentStudent.phone), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Major:", currentStudent.major), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Year:", String.valueOf(currentStudent.year)), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Status:", currentStudent.status.name()), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Overall GPA:", String.format("%.2f", currentStudent.gpa)), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Previous School:", currentStudent.previousSchool), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Application Doc:", currentStudent.docApplicationPath), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("ID Card Doc:", currentStudent.docIdCardPath), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Transcript Doc:", currentStudent.docTranscriptPath), createGbc(0, y++));

        GridBagConstraints gbc = createGbc(0, y);
        gbc.weighty = 1.0; 
        infoPanel.add(Box.createVerticalGlue(), gbc);

        panel.add(new JScrollPane(infoPanel), BorderLayout.CENTER); 
        
        return panel;
    }

    // ⭐️ (อัปเดต)
    private JPanel createScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // ⭐️ (อัปเดต) เพิ่มคอลัมน์ "Schedule"
        scheduleModel = new DefaultTableModel(new Object[]{"Subject ID", "Subject Name", "Teacher", "Schedule"}, 0);
        scheduleTable = new JTable(scheduleModel);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        return panel;
    }

    // ⭐️ (อัปเดต)
    private void loadStudentSchedule() {
        scheduleModel.setRowCount(0);
        ArrayList<StudentSchedule> scheduleList = dbManager.getStudentSchedule(this.currentUsername);
        for (StudentSchedule s : scheduleList) {
            scheduleModel.addRow(new Object[]{
                s.subjectId, s.subjectName, s.teacherName, s.scheduleInfo // ⭐️ (ใหม่)
            });
        }
    }

    // --- Helper Methods ---
    private GridBagConstraints createGbc(int x, int y) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = x; gbc.gridy = y;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0; gbc.insets = new Insets(5, 5, 5, 5);
        return gbc;
    }
    private JPanel createReadOnlyField(String label, String value) {
        return createReadOnlyField(label, value, Color.BLACK);
    }
    private JPanel createReadOnlyField(String label, String value, Color fgColor) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblLabel.setPreferredSize(new Dimension(120, 25)); 
        JTextField txtValue = new JTextField(value != null ? value : "");
        txtValue.setEditable(false);
        txtValue.setBorder(BorderFactory.createEtchedBorder());
        txtValue.setBackground(Color.WHITE);
        txtValue.setForeground(fgColor); 
        panel.add(lblLabel, BorderLayout.WEST);
        panel.add(txtValue, BorderLayout.CENTER);
        return panel;
    }
}