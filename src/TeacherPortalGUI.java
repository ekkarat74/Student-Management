import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets; 
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane; 
import javax.swing.JMenu;         
import javax.swing.JMenuBar;    
import javax.swing.JMenuItem;     
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea; 
import javax.swing.JTextField; 
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;

public class TeacherPortalGUI extends JFrame {

    private String currentUsername;
    private DatabaseManager dbManager;
    private Teacher currentTeacher;
    private DefaultTableModel homeroomModel;
    private JTable homeroomTable;

    private DefaultTableModel gradebookCourseModel;
    private JTable gradebookCourseTable;
    private DefaultTableModel gradebookStudentModel;
    private JTable gradebookStudentTable;
    private ArrayList<Subject> teacherSubjects; 
    private ArrayList<EnrollmentRecord> currentSubjectEnrollments;

    public TeacherPortalGUI(Role role, String username) {
        this.currentUsername = username;
        this.dbManager = new DatabaseManager();

        this.currentTeacher = dbManager.getTeacherById(this.currentUsername);
        
        setTitle("👨‍🏫 Teacher Portal (Welcome: " + (currentTeacher != null ? currentTeacher.name : username) + ")");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        setJMenuBar(createMenuBar());

        JPanel headerPanel = new JPanel();
        headerPanel.setBorder(new EmptyBorder(10, 15, 10, 15));
        JLabel welcomeLabel = new JLabel("Welcome, " + (currentTeacher != null ? currentTeacher.name : username));
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerPanel.add(welcomeLabel);
        add(headerPanel, BorderLayout.NORTH);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("📋 My Homeroom", createHomeroomTab());
        tabbedPane.addTab("📚 My Gradebook", createGradebookTab());
        tabbedPane.addTab("🗓️ Request Leave", createLeaveRequestTab()); 
        
        add(tabbedPane, BorderLayout.CENTER);

        loadHomeroomStudents();
        loadTeacherCourses();
        setVisible(true);
    }

    private JPanel createGradebookTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        gradebookCourseModel = new DefaultTableModel(new Object[]{"ID", "Course Name", "Semester"}, 0);
        gradebookCourseTable = new JTable(gradebookCourseModel);
        gradebookCourseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        gradebookStudentModel = new DefaultTableModel(new Object[]{"Student ID", "Student Name", "Current Grade"}, 0);
        gradebookStudentTable = new JTable(gradebookStudentModel);
        gradebookStudentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane courseScrollPane = new JScrollPane(gradebookCourseTable);
        courseScrollPane.setBorder(BorderFactory.createTitledBorder("My Courses"));

        JScrollPane studentScrollPane = new JScrollPane(gradebookStudentTable);
        studentScrollPane.setBorder(BorderFactory.createTitledBorder("Enrolled Students"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, courseScrollPane, studentScrollPane);
        splitPane.setDividerLocation(200);
        panel.add(splitPane, BorderLayout.CENTER);

        JButton manageGradesButton = new JButton("Manage Scores / Set Final Grade");
        manageGradesButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(manageGradesButton, BorderLayout.SOUTH);

        gradebookCourseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = gradebookCourseTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String subjectId = (String) gradebookCourseModel.getValueAt(selectedRow, 0);
                    loadStudentsForCourse(subjectId);
                }
            }
        });

        manageGradesButton.addActionListener(e -> openGradeManagementDialog());
        
        return panel;
    }
    
    private void loadTeacherCourses() {
        gradebookCourseModel.setRowCount(0);
        teacherSubjects = dbManager.getSubjectsForTeacher(this.currentUsername); 
        
        for (Subject s : teacherSubjects) {
            gradebookCourseModel.addRow(new Object[]{
                s.id,
                s.name,
                s.semesterName
            });
        }
    }

    private void loadStudentsForCourse(String subjectId) {
        gradebookStudentModel.setRowCount(0);
        currentSubjectEnrollments = dbManager.getEnrollmentsForSubject(subjectId);
        
        for (EnrollmentRecord er : currentSubjectEnrollments) {
            gradebookStudentModel.addRow(new Object[]{
                er.studentId,
                er.studentName,
                er.grade
            });
        }
    }
    
    private void openGradeManagementDialog() {
        int courseRow = gradebookCourseTable.getSelectedRow();
        int studentRow = gradebookStudentTable.getSelectedRow();

        if (courseRow < 0 || studentRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select both a course and a student.", "Selection Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        EnrollmentRecord selectedEnrollment = currentSubjectEnrollments.get(studentRow);
        Subject selectedSubject = teacherSubjects.get(courseRow);
        
        GradeManagementDialog dialog = new GradeManagementDialog(
            this, 
            dbManager, 
            selectedEnrollment.enrollmentId,
            selectedEnrollment.studentName,
            selectedSubject.name,
            selectedEnrollment.grade
        );
        dialog.setVisible(true);

        dbManager.calculateAndUpdatStudentGPA(selectedEnrollment.studentId);
        loadStudentsForCourse(selectedSubject.id);
    }

    private JPanel createHomeroomTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        homeroomModel = new DefaultTableModel(new Object[]{"ID", "Name", "Major", "Year", "GPA", "Status"}, 0);
        homeroomTable = new JTable(homeroomModel);
        panel.add(new JScrollPane(homeroomTable), BorderLayout.CENTER);
        return panel;
    }

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

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem refreshItem = new JMenuItem("🔄 Refresh Data");
        refreshItem.addActionListener(e -> {
            loadHomeroomStudents();
            loadTeacherCourses();
            if (gradebookStudentTable.getRowCount() > 0) {
                gradebookStudentModel.setRowCount(0);
            }
        });
        fileMenu.add(refreshItem);
        
        JMenu securityMenu = new JMenu("🔒 Security");
        JMenuItem logoutItem = new JMenuItem("🚪 Logout");

        logoutItem.addActionListener(e -> {
            TeacherPortalGUI.this.dispose();
            MainApp.main(null); 
        });

        securityMenu.add(logoutItem);
        menuBar.add(fileMenu);
        menuBar.add(securityMenu);
        return menuBar;
    }
}