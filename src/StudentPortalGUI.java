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
import javax.swing.JMenu; 
import javax.swing.JMenuBar;
import javax.swing.JMenuItem; 
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JTable; 
import javax.swing.table.DefaultTableModel; 

public class StudentPortalGUI extends JFrame {

    private String currentUsername;
    private DatabaseManager dbManager;
    private Student currentStudent;
    private Teacher homeroomTeacher; 
    private Classroom classroom;
    private DefaultTableModel scheduleModel;
    private JTable scheduleTable;

private DefaultTableModel transcriptModel;
    private JTable transcriptTable;
    private DefaultTableModel assignmentModel;
    private JTable assignmentTable; 
    private JLabel gpaLabel;
    private ArrayList<EnrollmentRecord> enrollmentRecords;

    public StudentPortalGUI(Role role, String username) {
        this.currentUsername = username;
        this.dbManager = new DatabaseManager();

        this.currentStudent = dbManager.getStudentById(this.currentUsername);
        this.homeroomTeacher = dbManager.getHomeroomTeacher(this.currentUsername); 
        this.classroom = dbManager.getClassroomById(this.currentStudent.classroomId);

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

        setJMenuBar(createMenuBar());

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
        tabbedPane.addTab("🎓 My Grades", createTranscriptTab());
        
        add(tabbedPane, BorderLayout.CENTER);

        loadStudentSchedule(); 
        loadTranscript();
        setVisible(true);
    }

private JPanel createTranscriptTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        transcriptModel = new DefaultTableModel(new Object[]{"Course ID", "Course Name", "Credits", "Final Grade"}, 0);
        transcriptTable = new JTable(transcriptModel);
        transcriptTable.setFont(new Font("SansSerif", Font.PLAIN, 14));
        transcriptTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        transcriptTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane transcriptScrollPane = new JScrollPane(transcriptTable);

        assignmentModel = new DefaultTableModel(new Object[]{"Assignment", "Score", "Max Score"}, 0);
        assignmentTable = new JTable(assignmentModel);
        assignmentTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane assignmentScrollPane = new JScrollPane(assignmentTable);
        assignmentScrollPane.setBorder(BorderFactory.createTitledBorder("Assignment Scores (Select a course above)"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transcriptScrollPane, assignmentScrollPane);
        splitPane.setDividerLocation(300);
        panel.add(splitPane, BorderLayout.CENTER);

        gpaLabel = new JLabel("Overall GPA: 0.00");
        gpaLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        gpaLabel.setBorder(new EmptyBorder(10, 5, 5, 5));
        panel.add(gpaLabel, BorderLayout.SOUTH);

        transcriptTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = transcriptTable.getSelectedRow();
                if (selectedRow >= 0) {
                    EnrollmentRecord selectedEnrollment = enrollmentRecords.get(selectedRow);
                    loadAssignmentGrades(selectedEnrollment.enrollmentId);
                } else {
                    assignmentModel.setRowCount(0);
                }
            }
        });

        return panel;
    }

    private void loadTranscript() {
        transcriptModel.setRowCount(0);
        
        enrollmentRecords = dbManager.getEnrollmentsForStudent(this.currentUsername);
        
        for (EnrollmentRecord er : enrollmentRecords) {
            transcriptModel.addRow(new Object[]{
                er.subjectId,
                er.subjectName,
                er.credits,
                er.grade
            });
        }
        
        if (currentStudent != null) {
            gpaLabel.setText(String.format("Overall GPA: %.2f", currentStudent.gpa));
        }
    }

    private void loadAssignmentGrades(int enrollmentId) {
        assignmentModel.setRowCount(0);
        ArrayList<AssignmentGrade> grades = dbManager.getAssignmentGradesForEnrollment(enrollmentId);
        for (AssignmentGrade grade : grades) {
            assignmentModel.addRow(new Object[]{
                grade.assignmentName,
                grade.score,
                grade.maxScore
            });
        }
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
        String classroomName = (classroom != null) ? classroom.name : "N/A";
        infoPanel.add(createReadOnlyField("Homeroom Teacher:", hrTeacherName, Color.BLUE), createGbc(0, y++));
        infoPanel.add(createReadOnlyField("Classroom:", classroomName), createGbc(0, y++));
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

    private JPanel createScheduleTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        scheduleModel = new DefaultTableModel(new Object[]{"Subject ID", "Subject Name", "Teacher", "Schedule"}, 0);
        scheduleTable = new JTable(scheduleModel);
        panel.add(new JScrollPane(scheduleTable), BorderLayout.CENTER);
        return panel;
    }

    private void loadStudentSchedule() {
        scheduleModel.setRowCount(0);
        ArrayList<StudentSchedule> scheduleList = dbManager.getStudentSchedule(this.currentUsername);
        for (StudentSchedule s : scheduleList) {
            scheduleModel.addRow(new Object[]{
                s.subjectId, s.subjectName, s.teacherName, s.scheduleInfo
            });
        }
    }

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

private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("🔄 Refresh Data");
        refreshItem.addActionListener(e -> {
            this.currentStudent = dbManager.getStudentById(this.currentUsername);
            loadStudentSchedule();
            loadTranscript();
            assignmentModel.setRowCount(0);
        });
        fileMenu.add(refreshItem);
        
        JMenu securityMenu = new JMenu("🔒 Security");
        JMenuItem logoutItem = new JMenuItem("🚪 Logout");

        logoutItem.addActionListener(e -> {
            StudentPortalGUI.this.dispose();
            MainApp.main(null); 
        });

        securityMenu.add(logoutItem);
        menuBar.add(fileMenu); 
        menuBar.add(securityMenu);
        return menuBar;
    }
}