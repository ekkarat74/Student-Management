import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class StudentManagementGUI extends JFrame {

    private static final String[] MAJOR_OPTIONS = {"Computer Science", "Business", "Arts", "Engineering", "Medicine", "Other"};
    private static final String[] YEAR_OPTIONS = {"1", "2", "3", "4", "5+"};
    private static final String[] STATUS_OPTIONS = {"ENROLLED", "GRADUATED", "ON_LEAVE", "DROPPED"};
    private DatabaseManager dbManager;
    private DataManager dataManager;
    private DefaultTableModel model;
    private JTable table;
    private Role currentUserRole;
    private String currentUsername;
    private JTextField searchField;
    private TableRowSorter<DefaultTableModel> sorter;
    private boolean isDarkMode = false;
    private JButton addBtn, editBtn, deleteBtn;
    private JMenuItem importItem;
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;

    private DefaultTableModel courseModel;
    private JTable courseTable;

    private DefaultTableModel leaveModel;
    private JTable leaveTable;
    
    private JComboBox<String> yearFilterComboBox;
    
    // ---===[ เพิ่มส่วนนี้เข้ามา ]===---
    private DefaultTableModel financeModel;
    private JTable financeTable;
    private DefaultTableModel invoiceDetailModel;
    private JTable invoiceDetailTable;
    private DefaultTableModel transactionDetailModel;
    private JTable transactionDetailTable;
    private JLabel financeDetailLabel;
    private DefaultTableModel userModel;
    private JTable userTable;
    private DefaultTableModel logModel;
    private JTable logTable;
    // ---===[ สิ้นสุดส่วนที่เพิ่ม ]===---


    public StudentManagementGUI(Role role, String username) {
        this.currentUserRole = role;
        this.currentUsername = username; 
        dbManager = new DatabaseManager();
        dataManager = new DataManager(); 

        setTitle("🎓 Student Management System (User: " + this.currentUsername + " | Role: " + role.name() + ")");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setJMenuBar(createMenuBar());
        add(createStatusBar(), BorderLayout.SOUTH);

        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JPanel filterControlsPanel = new JPanel(new BorderLayout(10, 5));

        searchField = new JTextField();
        filterControlsPanel.add(searchField, BorderLayout.CENTER);

        String[] yearFilters = {"All Years", "1", "2", "3", "4", "5+"};
        yearFilterComboBox = new JComboBox<>(yearFilters);
        filterControlsPanel.add(yearFilterComboBox, BorderLayout.EAST);

        topPanel.add(new JLabel("🔍 Filter:"), BorderLayout.WEST);
        topPanel.add(filterControlsPanel, BorderLayout.CENTER);

        JPanel mainHeaderPanel = new JPanel(new BorderLayout());
        mainHeaderPanel.add(createToolBar(), BorderLayout.NORTH); // เอา ToolBar ไว้บน
        mainHeaderPanel.add(topPanel, BorderLayout.SOUTH);

        add(mainHeaderPanel, BorderLayout.NORTH);
 
        model = new DefaultTableModel(new Object[]{"ID", "Name", "Major", "Year", "Status", "GPA", "Email", "Phone", "Date Added", "Homeroom Teacher"}, 0);
        table = new JTable(model);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.setAutoCreateRowSorter(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        
        setupTabs(); 
        refreshTable();
        refreshCourseTable(); 
        refreshLeaveTable(); 
        
        // ---===[ เพิ่มส่วนนี้เข้ามา ]===---
        refreshFinanceTable();
        // ---===[ สิ้นสุดส่วนที่เพิ่ม ]===---
        
        setupContextMenu(); 
        
        yearFilterComboBox.addActionListener(e -> filterTable());
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void insertUpdate(DocumentEvent e) { filterTable(); }
        });
        
        updatePermissions();
        setVisible(true);
    }
    
    private void updatePermissions() {
        boolean canWrite = (currentUserRole == Role.ADMIN || currentUserRole == Role.OFFICER); 
        boolean isGuest = (currentUserRole == Role.GUEST);
        if (addBtn != null) addBtn.setEnabled(canWrite);
        if (editBtn != null) editBtn.setEnabled(canWrite);
        if (deleteBtn != null) deleteBtn.setEnabled(canWrite);
        if (importItem != null) importItem.setEnabled(canWrite);
        if (isGuest) {
            setTitle("🎓 Student Management System (User: " + this.currentUsername + " | Role: GUEST - Read Only)");
            if (addBtn != null) addBtn.setEnabled(false);
            if (editBtn != null) editBtn.setEnabled(false);
            if (deleteBtn != null) deleteBtn.setEnabled(false);
            if (importItem != null) importItem.setEnabled(false);
        }
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        importItem = new JMenuItem("📂 Import...");
        importItem.addActionListener(e -> showImportDialog());
        JMenu exportMenu = new JMenu("💾 Export");
        JMenuItem exportJsonItem = new JMenuItem("Export to JSON (.json)");
        exportJsonItem.addActionListener(e -> exportToJson());
        JMenuItem exportXmlItem = new JMenuItem("Export to XML (.xml)");
        exportXmlItem.addActionListener(e -> exportToXml());
        exportMenu.add(exportJsonItem);
        exportMenu.add(exportXmlItem);
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(importItem);
        fileMenu.add(exportMenu);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        JMenu editMenu = new JMenu("Edit");
        editMenu.add(new JMenuItem("(Undo/Redo disabled)"));
        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleThemeItem = new JMenuItem("Toggle Dark/Light Mode");
        toggleThemeItem.addActionListener(e -> toggleTheme());
        viewMenu.add(toggleThemeItem);
        JMenu toolsMenu = new JMenu("🧰 Tools");
        JMenuItem refreshDbItem = new JMenuItem("🔄 Refresh Database");
        refreshDbItem.addActionListener(e -> {
            refreshTable();
            refreshCourseTable();
            refreshLeaveTable(); 
            
            // ---===[ เพิ่มส่วนนี้เข้ามา ]===---
            refreshFinanceTable();
            // ---===[ สิ้นสุดส่วนที่เพิ่ม ]===---
        });
        toolsMenu.add(refreshDbItem);
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(toolsMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(createSecurityMenu());
        menuBar.add(createHelpMenu());
        return menuBar;
    }
    
    private void addCourse() {
        ArrayList<Major> majors = dbManager.getAllMajors();
        ArrayList<Semester> semesters = dbManager.getAllSemesters();
        ArrayList<Teacher> teachers = dbManager.getAllTeachers();

        CourseDialog dialog = new CourseDialog(this, majors, semesters, teachers);
        dialog.setVisible(true);
        
        CourseData newCourseData = dialog.getCourseData();

        if (newCourseData != null) {
            boolean success = dbManager.addCourse(newCourseData);
        if (success) {
                dbManager.logActivity(currentUsername, "Created course: " + newCourseData.id);
                JOptionPane.showMessageDialog(this, "Course " + newCourseData.name + " created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCourseTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create course. Check if ID already exists.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void addTeacher() {
        TeacherDialog dialog = new TeacherDialog(this);
        dialog.setVisible(true);
        Teacher newTeacher = dialog.getTeacher();
        if (newTeacher != null) {
            String tempPassword = JOptionPane.showInputDialog(this, "Enter a temporary password for teacher: " + newTeacher.id, "Create Login Account", JOptionPane.PLAIN_MESSAGE);
            if (tempPassword == null || tempPassword.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Password cannot be empty. Teacher creation cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            boolean userCreated = dbManager.createUser(newTeacher.id, tempPassword, Role.TEACHER);
            if (!userCreated) {
                JOptionPane.showMessageDialog(this, "Failed to create login user (perhaps username " + newTeacher.id + " already exists?).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            dbManager.addTeacher(newTeacher);
            dbManager.logActivity(currentUsername, "Created teacher: " + newTeacher.id); // ⭐️ เพิ่มบรรทัดนี้
            JOptionPane.showMessageDialog(this, "Teacher " + newTeacher.name + " and login account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

private void addStudent() {
    StudentDialog dialog = new StudentDialog(this, "Add New Student", MAJOR_OPTIONS, YEAR_OPTIONS, STATUS_OPTIONS, null);
    dialog.setVisible(true);
    Student newStudent = dialog.getStudent();
    
    if (newStudent != null) {
        String tempPassword = JOptionPane.showInputDialog(this, "Enter a temporary password for student: " + newStudent.id, "Create Login Account", JOptionPane.PLAIN_MESSAGE);
        
        if (tempPassword == null || tempPassword.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Password cannot be empty. Student creation cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
            return; 
        }

        boolean success = dbManager.createStudentWithLogin(newStudent, tempPassword);

        if (success) {
            // นี่คือส่วนของ Success
            dbManager.logActivity(currentUsername, "Created student: " + newStudent.id);
            JOptionPane.showMessageDialog(this, "Student " + newStudent.name + " and login account created successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable(); 
            checkLowGpaWarning(newStudent.gpa);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create student (ID may already exist or DB error).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

    private void editStudent() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) { JOptionPane.showMessageDialog(this, "Select a student to edit."); return; }
        int modelRow = table.convertRowIndexToModel(viewRow);
        String studentId = (String) model.getValueAt(modelRow, 0);
        Student studentToEdit = dbManager.getStudentById(studentId);
        if (studentToEdit == null) { JOptionPane.showMessageDialog(this, "Could not find student data in DB."); return; }
        StudentDialog dialog = new StudentDialog(this, "Edit Student", MAJOR_OPTIONS, YEAR_OPTIONS, STATUS_OPTIONS, studentToEdit);
        dialog.setVisible(true);
        Student editedStudent = dialog.getStudent();
        if (editedStudent != null) {
            dbManager.updateStudent(editedStudent);
            dbManager.logActivity(currentUsername, "Edited student: " + editedStudent.id); // ⭐️ เพิ่มบรรทัดนี้
            refreshTable();
            checkLowGpaWarning(editedStudent.gpa);
        }
    }

        private void filterTable() {
        if (tabbedPane.getSelectedIndex() != 0) { 
            sorter.setRowFilter(null); // ถ้าอยู่แท็บอื่น ให้เคลียร์ filter ทิ้ง
            return;
        }
        
        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        String text = searchField.getText();

        if (text != null && !text.trim().isEmpty()) {
            try {
                List<RowFilter<Object, Object>> textOrFilters = new ArrayList<>();
                String searchText = "(?i)" + text; // (?i) = case-insensitive
                
                textOrFilters.add(RowFilter.regexFilter(searchText, 0)); // คอลัมน์ 0: ID
                textOrFilters.add(RowFilter.regexFilter(searchText, 1)); // คอลัมน์ 1: Name
                textOrFilters.add(RowFilter.regexFilter(searchText, 2)); // คอลัมน์ 2: Major
                textOrFilters.add(RowFilter.regexFilter(searchText, 6)); // คอลัมน์ 6: Email
                textOrFilters.add(RowFilter.regexFilter(searchText, 9)); // คอลัมน์ 9: Homeroom Teacher
                
                filters.add(RowFilter.orFilter(textOrFilters));
                
            } catch (java.util.regex.PatternSyntaxException e) {
            }
        }

        String yearFilter = (String) yearFilterComboBox.getSelectedItem();
        if (yearFilter != null && !yearFilter.equals("All Years")) {
            try {
                int year = Integer.parseInt(yearFilter.replace("+", ""));
                // คอลัมน์ 3: Year
                filters.add(RowFilter.numberFilter(RowFilter.ComparisonType.EQUAL, year, 3)); 
            } catch (NumberFormatException e) {
                // ไม่ต้องทำอะไรถ้าเลือกปีไม่ถูกต้อง
            }
        }

        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            RowFilter<Object, Object> combinedFilter = RowFilter.andFilter(filters);
            sorter.setRowFilter(combinedFilter);
        }
    }

    public String validateInput(String id, String name, String ageStr, String gpaStr, String major, String email, String yearStr, String status, boolean isNew) {
        if (id.isEmpty() || name.isEmpty() || ageStr.isEmpty() || gpaStr.isEmpty() || major.isEmpty() || email.isEmpty() || yearStr.isEmpty() || status.isEmpty()) { return "All core fields are required."; }
        try { int age = Integer.parseInt(ageStr); if (age <= 0) return "Age must be greater than 0."; } catch (NumberFormatException e) { return "Age must be a valid number."; }
        try { double gpa = Double.parseDouble(gpaStr); if (gpa < 0.0 || gpa > 4.0) return "GPA must be between 0.0 and 4.0."; } catch (NumberFormatException e) { return "GPA must be a valid number (e.g., 3.75)."; }
        try { int year = Integer.parseInt(yearStr.replace("+", "")); if (year <= 0) return "Year must be greater than 0."; } catch (NumberFormatException e) { return "Year must be a valid number."; }
        try { StudentStatus.valueOf(status); } catch (IllegalArgumentException e) { return "Invalid student status."; }
        if (!email.isEmpty() && (!email.contains("@") || !email.contains("."))) { return "Email must be a valid address (e.g., user@domain.com)."; }
        if (isNew) { if (dbManager.isStudentIdExists(id)) { return "Student ID already exists in the database."; } }
        return null;
    }
    private void checkLowGpaWarning(double gpa) {
        if (gpa < 2.0) { JOptionPane.showMessageDialog(this, "⚠️ Warning: This student has a low GPA (" + gpa + ").", "Low GPA Alert", JOptionPane.WARNING_MESSAGE); }
    }
    private void deleteStudent() {
        int viewRow = table.getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            String studentId = (String) model.getValueAt(modelRow, 0);
            String name = (String) model.getValueAt(modelRow, 1);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete " + name + "?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dbManager.deleteStudent(studentId);
                dbManager.logActivity(currentUsername, "Deleted student: " + studentId); // ⭐️ เพิ่มบรรทัดนี้
                refreshTable();
            }
        } else { JOptionPane.showMessageDialog(this, "Select a student to delete."); }
    }
    private void showStatistics() {
        List<Student> students = dbManager.getAllStudents();
        if (students.isEmpty()) { JOptionPane.showMessageDialog(this, "No data."); return; }
        double total = 0, max = Double.MIN_VALUE, min = Double.MAX_VALUE;
        for (Student s : students) { total += s.gpa; max = Math.max(max, s.gpa); min = Math.min(min, s.gpa); }
        double avg = total / students.size();
        StringBuilder chart = new StringBuilder("\n📈 GPA Chart (ASCII):\n");
        for (Student s : students) {
            int bars = (int) (s.gpa * 5);
            chart.append(String.format("%-12s | %s %.2f%n", s.name, "#".repeat(Math.max(0, bars)), s.gpa));
        }
        JOptionPane.showMessageDialog(this, String.format("Total Students: %d\nAverage GPA: %.2f\nMax GPA: %.2f\nMin GPA: %.2f\n%s", students.size(), avg, max, min, chart));
    }
    private void toggleTheme() {
        try {
            if (isDarkMode) { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); } else { UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); }
            SwingUtilities.updateComponentTreeUI(this);
            isDarkMode = !isDarkMode;
        } catch (Exception e) { System.err.println("Failed to set LookAndFeel"); }
    }
    private void showImportDialog() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Import Students File");
        fc.setFileFilter(new FileNameExtensionFilter("Data Files (.txt, .csv, .xml)", "txt", "csv", "xml"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            String ext = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            List<Student> importedStudents = null;
            switch (ext.toLowerCase()) {
                case "txt" -> importedStudents = dataManager.loadFromFile(file);
                case "csv" -> importedStudents = dataManager.loadFromCsv(file);
                case "xml" -> importedStudents = dataManager.loadFromXml(file);
                default -> JOptionPane.showMessageDialog(this, "Unsupported file type.", "Error", JOptionPane.ERROR_MESSAGE);
            }
            if (importedStudents != null) {
                int countAdded = 0, countSkipped = 0;
                for (Student s : importedStudents) {
                    if (!dbManager.isStudentIdExists(s.id)) {
                        dbManager.addStudent(s);
                        countAdded++;
                    } else { countSkipped++; }
                }
                refreshTable();
                JOptionPane.showMessageDialog(this, "Import complete.\nAdded: " + countAdded + "\nSkipped (ID exists): " + countSkipped);
            } else { JOptionPane.showMessageDialog(this, "Failed to import file.", "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }
    private void exportToJson() {
        List<Student> students = dbManager.getAllStudents();
        if (dataManager.exportToJson(students)) { JOptionPane.showMessageDialog(this, "Exported to students.json", "Export Success", JOptionPane.INFORMATION_MESSAGE); } else { JOptionPane.showMessageDialog(this, "Failed to export to JSON", "Error", JOptionPane.ERROR_MESSAGE); }
    }
    private void exportToXml() {
        List<Student> students = dbManager.getAllStudents();
        if (dataManager.exportToXml(students)) { JOptionPane.showMessageDialog(this, "Exported to students.xml", "Export Success", JOptionPane.INFORMATION_MESSAGE); } else { JOptionPane.showMessageDialog(this, "Failed to export to XML", "Error", JOptionPane.ERROR_MESSAGE); }
    }
private void refreshTable() {
        model.setRowCount(0);
        ArrayList<StudentDisplayRecord> students = dbManager.getAllStudentsForDisplay(); 
        for (StudentDisplayRecord s : students) {
            model.addRow(new Object[]{ 
                s.id, s.name, s.major, s.year, s.status.name(), s.gpa, s.email, s.phone, 
                s.dateAdded,
                s.homeroomTeacherName != null ? s.homeroomTeacherName : "N/A"
            });
        }
    }

    private void refreshCourseTable() {
        courseModel.setRowCount(0);
        ArrayList<Subject> subjects = dbManager.getAllSubjects();
        for (Subject s : subjects) {
            courseModel.addRow(new Object[]{
                s.id, s.name, s.credits, s.majorName, s.semesterName,
                s.teacherName, s.scheduleInfo, s.prerequisites
            });
        }
    }
    
    private void showNotImplemented(String featureName) { JOptionPane.showMessageDialog(this, "<html><b>Feature Not Implemented:</b> " + featureName + "</html>", "Feature Not Implemented", JOptionPane.INFORMATION_MESSAGE); }
    private void exportToExcelStub() { showNotImplemented("Export to Excel (Requires Apache POI)"); }
    private void exportToPdfStub() { showNotImplemented("Export Report (Requires iText or PDFBox)"); }
    private void showGraphStub() { showNotImplemented("GPA Graph (Requires JFreeChart)"); }


    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) { e.printStackTrace(); }

        DatabaseManager dbManagerForLogin = new DatabaseManager();
        dbManagerForLogin.initDatabase();

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        int option = JOptionPane.showConfirmDialog(null, panel, "Login", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String user = usernameField.getText();
            String pass = new String(passwordField.getPassword());
            Role userRole = dbManagerForLogin.checkLogin(user, pass);

            if (userRole != Role.UNKNOWN) {
                switch (userRole) {
                    case ADMIN:
                        new StudentManagementGUI(userRole, user);
                        break;
                    case OFFICER:
                        new StudentManagementGUI(userRole, user);
                        break;
                    case TEACHER:
                        new TeacherPortalGUI(userRole, user); 
                        break;
                    case STUDENT:
                        new StudentPortalGUI(userRole, user); 
                        break;
                    case GUEST:
                        new StudentManagementGUI(userRole, user);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, "ไม่พบ Role ที่กำหนดไว้!");
                        System.exit(0);
                }
            } else {
                JOptionPane.showMessageDialog(null, "❌ Wrong username or password!");
                System.exit(0);
            }
        } else {
            System.exit(0); 
        }
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton addStudentTool = new JButton("➕ Add Student");
        addStudentTool.setToolTipText("Add Student");
        addStudentTool.addActionListener(e -> addStudent());
        
        JButton addTeacherTool = new JButton("➕ Add Teacher");
        addTeacherTool.setToolTipText("Add Teacher");
        addTeacherTool.addActionListener(e -> addTeacher());

        JButton editTool = new JButton("✏️ Edit Student");
        editTool.setToolTipText("Edit Selected Student");
        editTool.addActionListener(e -> editStudent());

        JButton delTool = new JButton("🗑️ Delete Student");
        delTool.setToolTipText("Delete Selected Student");
        delTool.addActionListener(e -> deleteStudent());

        JButton themeTool = new JButton("🌓");
        themeTool.setToolTipText("Toggle Theme");
        themeTool.addActionListener(e -> toggleTheme());

        toolBar.add(addStudentTool);
        toolBar.add(addTeacherTool); 
        toolBar.add(editTool);
        toolBar.add(delTool);
        toolBar.addSeparator();
        toolBar.add(themeTool);
        
        boolean canWrite = (currentUserRole == Role.ADMIN);
        addStudentTool.setEnabled(canWrite);
        addTeacherTool.setEnabled(canWrite);
        editTool.setEnabled(canWrite);
        delTool.setEnabled(canWrite);

        return toolBar;
    }

    private JPanel createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        statusLabel = new JLabel("Ready.");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        Timer timer = new Timer(1000, e -> {
            statusLabel.setText("Total: " + dbManager.getStudentCount() + " students | " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
        });
        timer.start();
        return statusPanel;
    }
    private void setupTabs() {
        tabbedPane = new JTabbedPane();
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(table), BorderLayout.CENTER);
        tablePanel.add(createButtonPanel(), BorderLayout.SOUTH);
        JPanel dashboard = createDashboardPanel();
        JPanel coursePanel = createCoursePanel(); 
        JPanel leavePanel = createLeavePanel(); 

        // ---===[ เพิ่มส่วนนี้เข้ามา ]===---
        JPanel financePanel = createFinancePanel();
        // ---===[ สิ้นสุดส่วนที่เพิ่ม ]===---

        tabbedPane.addTab("📋 Student List", tablePanel);
        tabbedPane.addTab("📚 Course Management", coursePanel); 
        tabbedPane.addTab("🗓️ Leave Management", leavePanel); 
        
        // ---===[ เพิ่มส่วนนี้เข้ามา ]===---
        tabbedPane.addTab("💸 Finance & Payments", financePanel);
        // ---===[ สิ้นสุดส่วนที่เพิ่ม ]===---
        
        JPanel reportDashboardPanel = createReportDashboardPanel();
        tabbedPane.addTab("📊 Reports & Dashboard", reportDashboardPanel);
        if (currentUserRole == Role.ADMIN || currentUserRole == Role.OFFICER) {
            JPanel securityAdminPanel = createSecurityAdminPanel();
            tabbedPane.addTab("🛡️ Security & Admin", securityAdminPanel);
            
            // โหลดข้อมูลสำหรับแท็บนี้
            refreshUserTable();
            refreshLogTable();
        }
        add(tabbedPane, BorderLayout.CENTER);

        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String title = tabbedPane.getTitleAt(selectedIndex);

            if (title.equals("📊 Reports & Dashboard")) { 
                // Refresh ข้อมูลใหม่ทุกครั้งที่กดแท็บนี้
                JPanel newDashboard = createReportDashboardPanel();
                tabbedPane.setComponentAt(selectedIndex, newDashboard);
            }

            if (title.equals("🗓️ Leave Management")) {
                refreshLeaveTable();
            }

            if (title.equals("🛡️ Security & Admin")) {
                refreshUserTable();
                refreshLogTable();
            }
        });
    }
    private JPanel createReportDashboardPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JTabbedPane reportTabs = new JTabbedPane();
        
        // แท็บที่ 1: ภาพรวม (อันเดิม)
        reportTabs.addTab("📈 Overview", createOverviewPanel());
        
        // แท็บที่ 2: รายงาน (Text)
        reportTabs.addTab("📋 Academic & Finance Reports", createTextReportPanel());
        
        // แท็บที่ 3: กราฟ (Stub)
        reportTabs.addTab("📊 Charts (JFreeChart needed)", createChartPanelStub());

        mainPanel.add(reportTabs, BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * (ใหม่) สร้าง Panel ภาพรวม (อันเดียวกับ Dashboard เดิม)
     */
    private JPanel createOverviewPanel() {
        List<Student> students = dbManager.getAllStudents(); // ⭐️ ดึงข้อมูลมาใหม่
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        panel.add(createCard("👩‍🎓 Total Students", String.valueOf(students.size())));
        
        double avgGpa = students.stream().mapToDouble(s -> s.gpa).average().orElse(0);
        panel.add(createCard("📈 Average GPA", String.format("%.2f", avgGpa)));
        
        long lowGpaCount = students.stream().filter(s -> s.gpa < 2.0).count();
        panel.add(createCard("⚠️ Low GPA (<2.0)", String.valueOf(lowGpaCount)));
        
        return panel;
    }

    private JScrollPane createTextReportPanel() {
        JTextArea reportArea = new JTextArea();
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        StringBuilder sb = new StringBuilder();
        
        // --- 1. รายงาน Academic ---
        sb.append("--- ACADEMIC REPORTS ---\n");
        
        // นักศึกษาต่อสาขา
        sb.append("\n[ Student Count per Major ]\n");
        ArrayList<DataPoint> majorCounts = dbManager.getStudentCountPerMajor();
        for (DataPoint dp : majorCounts) {
            sb.append(String.format("  %-20s : %d students\n", dp.label, dp.count));
        }

        // อัตราการจบ
        sb.append("\n[ Student Status & Graduation Rate ]\n");
        ArrayList<DataPoint> statusCounts = dbManager.getStudentStatusCount();
        int total = 0;
        int graduated = 0;
        for (DataPoint dp : statusCounts) {
            sb.append(String.format("  %-20s : %d students\n", dp.label, dp.count));
            if (dp.label.equals("GRADUATED")) {
                graduated = dp.count;
            }
            total += dp.count;
        }
        double gradRate = (total == 0) ? 0 : ((double) graduated / total) * 100;
        sb.append(String.format("  --------------------------------------\n"));
        sb.append(String.format("  Graduation Rate: %.2f%%\n", gradRate));

        // วิชาที่ตกบ่อย
        sb.append("\n[ Top 5 Most Failed Courses (Grade 'F') ]\n");
        ArrayList<DataPoint> failedCourses = dbManager.getMostFailedCourses();
        if (failedCourses.isEmpty()) {
            sb.append("  No 'F' grades found.\n");
        }
        for (DataPoint dp : failedCourses) {
            sb.append(String.format("  %-25s : %d fails\n", dp.label, dp.count));
        }

        // --- 2. รายงาน Financial ---
        sb.append("\n\n--- FINANCIAL REPORTS ---\n");
        FinancialReport finReport = dbManager.getFinancialSummaryReport();
        sb.append(String.format("  Total Invoiced (Due):    %,.2f\n", finReport.totalDue));
        sb.append(String.format("  Total Received (Paid):   %,.2f\n", finReport.totalPaid));
        sb.append(String.format("  --------------------------------------\n"));
        sb.append(String.format("  Net Balance (Paid - Due): %,.2f\n", finReport.netBalance));
        sb.append(String.format("\n  Total Transactions:    %d\n", finReport.totalTransactions));

        reportArea.setText(sb.toString());
        
        return new JScrollPane(reportArea); // ⬅️ คุณ Return JScrollPane
    }
    
    /**
     * (ใหม่) สร้าง Panel แจ้งเตือนเรื่อง JFreeChart
     */
    private JPanel createChartPanelStub() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        
        String message = """
            <html>
            <div style='text-align: center; padding: 20px; font-size: 12px;'>
                <b style='font-size: 16px;'>📊 Graphs & Charts Not Available</b>
                <br><br>
                This feature requires the external <b>JFreeChart</b> library.
                <br><br>
                <u>To enable graphs:</u>
                <ol style='text-align: left; margin-left: 50px;'>
                    <li>Download <b>jfreechart-1.5.3.jar</b> (or newer)</li>
                    <li>Download <b>jcommon-1.0.24.jar</b> (or newer)</li>
                    <li>Add both .jar files to your project's build path / classpath.</li>
                </ol>
            </div>
            </html>
            """;
        
        JLabel label = new JLabel(message);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true));
        panel.add(label);
        
        return panel;
    }
    
    /**
     * (ใหม่) Helper method สำหรับสร้าง Card (เหมือนของเดิม)
     */
    private JPanel createCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        JLabel lblValue = new JLabel(value, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }
    
    private JPanel createCoursePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        courseModel = new DefaultTableModel(new Object[]{
            "ID", "Course Name", "Credits", "Major", "Semester", "Teacher", "Schedule", "Prerequisites"
        }, 0);
        courseTable = new JTable(courseModel);
        courseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(new JScrollPane(courseTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddCourse = new JButton("➕ Add Course");
        JButton btnEditCourse = new JButton("✏️ Edit Course");
        JButton btnSetPrereq = new JButton("🔗 Set Prerequisites");
        
        btnAddCourse.addActionListener(e -> addCourse()); 
        
        btnEditCourse.addActionListener(e -> editCourse()); // แก้ไขบรรทัดนี้
        btnSetPrereq.addActionListener(e -> setPrerequisites());
        
        boolean canWrite = (currentUserRole == Role.ADMIN);
        btnAddCourse.setEnabled(canWrite);
        btnEditCourse.setEnabled(canWrite);
        btnSetPrereq.setEnabled(canWrite);

        buttonPanel.add(btnAddCourse);
        buttonPanel.add(btnEditCourse);
        buttonPanel.add(btnSetPrereq);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private JPanel createLeavePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leaveModel = new DefaultTableModel(new Object[]{
            "ID", "Teacher Name", "Start Date", "End Date", "Reason", "Status"
        }, 0);
        leaveTable = new JTable(leaveModel);
        leaveTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(new JScrollPane(leaveTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnApprove = new JButton("👍 Approve");
        JButton btnDeny = new JButton("👎 Deny");
        
        btnApprove.addActionListener(e -> updateLeaveStatus("APPROVED"));
        btnDeny.addActionListener(e -> updateLeaveStatus("DENIED"));
        
        boolean canWrite = (currentUserRole == Role.ADMIN);
        btnApprove.setEnabled(canWrite);
        btnDeny.setEnabled(canWrite);

        buttonPanel.add(btnApprove);
        buttonPanel.add(btnDeny);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void refreshLeaveTable() {
        if (leaveModel == null) return; 
        leaveModel.setRowCount(0);
        ArrayList<LeaveRequest> requests = dbManager.getAllLeaveRequests();
        for (LeaveRequest req : requests) {
            leaveModel.addRow(new Object[]{
                req.id,
                req.teacherName,
                req.startDate,
                req.endDate,
                req.reason,
                req.status
            });
        }
    }

    private void updateLeaveStatus(String status) {
        int viewRow = leaveTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a leave request to " + status.toLowerCase() + ".");
            return;
        }

        int modelRow = leaveTable.convertRowIndexToModel(viewRow);
        int leaveId = (int) leaveModel.getValueAt(modelRow, 0);
        String currentStatus = (String) leaveModel.getValueAt(modelRow, 5);
        
        if (currentStatus.equals("APPROVED") || currentStatus.equals("DENIED")) {
             JOptionPane.showMessageDialog(this, "This request has already been processed.", "Already Processed", JOptionPane.WARNING_MESSAGE);
             return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to " + status.toLowerCase() + " this request (ID: " + leaveId + ")?",
            "Confirm Action", 
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = dbManager.updateLeaveRequestStatus(leaveId, status);
            if (success) {
            dbManager.logActivity(currentUsername, "Updated leave request #" + leaveId + " to " + status); // ⭐️ เพิ่มบรรทัดนี้
            JOptionPane.showMessageDialog(this, "Request " + status.toLowerCase() + " successfully.");
                refreshLeaveTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update status.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ---===[ เพิ่ม Method ใหม่ทั้งหมดนี้ ]===---

    /**
     * สร้าง Panel หลักสำหรับระบบการเงิน
     */
    private JPanel createFinancePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. ตารางสรุปสถานะการเงินของนักศึกษา (Master Table)
        financeModel = new DefaultTableModel(new Object[]{
            "Student ID", "Student Name", "Total Due", "Total Paid", "Balance", "Status"
        }, 0);
        financeTable = new JTable(financeModel);
        financeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        financeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 2. Panel สำหรับแสดงรายละเอียด (Detail Panel)
        JPanel detailPanel = createFinanceDetailPanel();

        // 3. JSplitPane สำหรับแบ่งหน้าจอ
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(financeTable), detailPanel);
        splitPane.setDividerLocation(300);
        panel.add(splitPane, BorderLayout.CENTER);

        // 4. Panel ปุ่มคำสั่งสำหรับ Admin
        panel.add(createFinanceButtonPanel(), BorderLayout.SOUTH);

        // 5. เพิ่ม Listener ให้ตาราง Master
        financeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = financeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String studentId = (String) financeModel.getValueAt(selectedRow, 0);
                    String studentName = (String) financeModel.getValueAt(selectedRow, 1);
                    updateFinanceDetailView(studentId, studentName);
                } else {
                    clearFinanceDetailView();
                }
            }
        });

        return panel;
    }

    /**
     * สร้าง Panel ส่วนล่างสำหรับแสดงรายละเอียด (Invoices, Transactions)
     */
    private JPanel createFinanceDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        financeDetailLabel = new JLabel("Select a student to view details", SwingConstants.CENTER);
        financeDetailLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(financeDetailLabel, BorderLayout.NORTH);

        // 2.1 ตาราง Invoice
        invoiceDetailModel = new DefaultTableModel(new Object[]{"Invoice ID", "Issue Date", "Due Date", "Amount", "Status"}, 0);
        invoiceDetailTable = new JTable(invoiceDetailModel);

        // 2.2 ตาราง Transaction
        transactionDetailModel = new DefaultTableModel(new Object[]{"Transaction ID", "Payment Date", "Amount Paid", "Method"}, 0);
        transactionDetailTable = new JTable(transactionDetailModel);

        JTabbedPane detailTabs = new JTabbedPane();
        detailTabs.addTab("Invoices", new JScrollPane(invoiceDetailTable));
        detailTabs.addTab("Payment History", new JScrollPane(transactionDetailTable));
        
        // TODO: เพิ่มแท็บสำหรับ Scholarships / Discounts ที่นี่

        panel.add(detailTabs, BorderLayout.CENTER);
        return panel;
    }

    /**
     * สร้าง Panel ปุ่มคำสั่งด้านล่าง
     */
    private JPanel createFinanceButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnGenerateInvoices = new JButton("Generate Semester Invoices...");
        JButton btnAddPayment = new JButton("Add Payment / Transaction...");
        JButton btnAddAid = new JButton("Add Scholarship / Discount...");
        JButton btnViewInvoiceDetails = new JButton("View Invoice Details");

        btnGenerateInvoices.addActionListener(e -> generateInvoices());
        btnAddPayment.addActionListener(e -> addPayment());
        btnAddAid.addActionListener(e -> addFinancialAid());
        btnViewInvoiceDetails.addActionListener(e -> viewInvoiceDetails());

        // จำกัดสิทธิ์เฉพาะ Admin
        boolean canWrite = (currentUserRole == Role.ADMIN);
        btnGenerateInvoices.setEnabled(canWrite);
        btnAddPayment.setEnabled(canWrite);
        btnAddAid.setEnabled(canWrite);

        buttonPanel.add(btnGenerateInvoices);
        buttonPanel.add(btnAddPayment);
        buttonPanel.add(btnAddAid);
        buttonPanel.add(btnViewInvoiceDetails);
        
        return buttonPanel;
    }
    
    /**
     * Refresh ตารางสรุปการเงิน
     */
    private void refreshFinanceTable() {
        if (financeModel == null) return;
        financeModel.setRowCount(0);
        
        // ⭐️ ดึงข้อมูลจริงจาก DB
        ArrayList<FinanceSummary> summaries = dbManager.getAllStudentFinanceSummary();
        
        for (FinanceSummary summary : summaries) {
            financeModel.addRow(new Object[]{
                summary.studentId,
                summary.studentName,
                summary.totalDue,
                summary.totalPaid,
                summary.balance,
                summary.status
            });
        }
        
        clearFinanceDetailView();
    }

    /**
     * Update ส่วน Detail เมื่อเลือกนักศึกษา
     */
    private void updateFinanceDetailView(String studentId, String studentName) {
        financeDetailLabel.setText("Financial Details for: " + studentName + " (ID: " + studentId + ")");
        
        // ⭐️ ดึง Invoices จริง
        invoiceDetailModel.setRowCount(0);
        ArrayList<Invoice> invoices = dbManager.getInvoicesForStudent(studentId);
        for (Invoice inv : invoices) {
            invoiceDetailModel.addRow(new Object[]{inv.id, inv.issueDate, inv.dueDate, inv.totalAmount, inv.status});
        }

        // ⭐️ ดึง Transactions จริง
        transactionDetailModel.setRowCount(0);
        ArrayList<Transaction> transactions = dbManager.getTransactionsForStudent(studentId);
        for (Transaction tx : transactions) {
            transactionDetailModel.addRow(new Object[]{tx.id, tx.paymentDate, tx.amountPaid, tx.paymentMethod});
        }
    }
    
    /**
     * ล้างข้อมูลส่วน Detail
     */
    private void clearFinanceDetailView() {
        financeDetailLabel.setText("Select a student to view details");
        invoiceDetailModel.setRowCount(0);
        transactionDetailModel.setRowCount(0);
    }
    
    // --- Placeholder Methods (ต้องเขียนโค้ดเพิ่ม) ---
    
    private void generateInvoices() {
        // 1. ดึงข้อมูล Semesters
        ArrayList<Semester> semesters = dbManager.getAllSemesters();
        if (semesters == null || semesters.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No semesters found in database. Cannot generate invoices.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // 2. เปิด Dialog
        GenerateInvoiceDialog dialog = new GenerateInvoiceDialog(this, semesters);
        dialog.setVisible(true);
        
        // 3. ถ้ากดยกเลิก
        if (!dialog.isGenerated()) {
            return;
        }

        // 4. ถ้ากด Generate
        Semester selectedSemester = dialog.getSelectedSemester();
        double baseFee = dialog.getBaseFee();
        String issueDate = dialog.getIssueDate();
        String dueDate = dialog.getDueDate();
        
        // 5. เรียก DB Manager
        try {
        int count = dbManager.generateInvoicesForSemester(selectedSemester.id, baseFee, issueDate, dueDate);
        if (count >= 0) {
            dbManager.logActivity(currentUsername, "Generated " + count + " invoices for semester " + selectedSemester.id); // ⭐️ เพิ่มบรรทัดนี้
            JOptionPane.showMessageDialog(this, "Successfully generated " + count + " invoices.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshFinanceTable(); // ⭐️ Refresh ตาราง
            } else {
                JOptionPane.showMessageDialog(this, "An error occurred while generating invoices.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addPayment() {
        // 1. ตรวจสอบว่าเลือกนักศึกษาหรือยัง
        int selectedRow = financeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String studentId = (String) financeModel.getValueAt(selectedRow, 0);
        String studentName = (String) financeModel.getValueAt(selectedRow, 1);
        double balance = (double) financeModel.getValueAt(selectedRow, 4);

        // 2. ดึง Invoice ที่ยังค้างชำระ
        ArrayList<Invoice> pendingInvoices = dbManager.getPendingInvoicesForStudent(studentId);
        if (pendingInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This student has no pending invoices.", "No Pending Invoices", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 3. เปิด Dialog
        PaymentDialog dialog = new PaymentDialog(this, studentName, balance, pendingInvoices);
        dialog.setVisible(true);

        // 4. ถ้ากดยกเลิก
        if (!dialog.isSaved()) {
            return;
        }

        // 5. ถ้ากด Save
        try {
            Invoice selectedInvoice = dialog.getSelectedInvoice();
            double amountPaid = dialog.getAmountPaid();
            String paymentMethod = dialog.getPaymentMethod();
            String referenceCode = dialog.getReferenceCode();

            // 6. สร้าง Transaction DTO
            Transaction newTx = new Transaction(
                selectedInvoice.id,
                studentId,
                amountPaid,
                paymentMethod,
                referenceCode
            );

            // 7. เรียก DB Manager
            boolean success = dbManager.addPayment(newTx);
                if (success) {
                    dbManager.logActivity(currentUsername, "Added payment of " + amountPaid + " for student " + studentId); // ⭐️ เพิ่มบรรทัดนี้
                    JOptionPane.showMessageDialog(this, "Payment of " + amountPaid + " added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                refreshFinanceTable();
                updateFinanceDetailView(studentId, studentName);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add payment.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void addFinancialAid() {
        // 1. ตรวจสอบว่าเลือกนักศึกษาหรือยัง
        int selectedRow = financeTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student from the table first.", "No Student Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String studentId = (String) financeModel.getValueAt(selectedRow, 0);
        String studentName = (String) financeModel.getValueAt(selectedRow, 1);

        // 2. ดึง Invoice ที่ยังค้างชำระ (เพื่อใช้เป็นเป้าหมาย)
        ArrayList<Invoice> pendingInvoices = dbManager.getPendingInvoicesForStudent(studentId);
        if (pendingInvoices.isEmpty()) {
            JOptionPane.showMessageDialog(this, "This student has no pending invoices to apply aid to.", "No Pending Invoices", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 3. เปิด Dialog
        FinancialAidDialog dialog = new FinancialAidDialog(this, studentName, pendingInvoices);
        dialog.setVisible(true);

        // 4. ถ้ากดยกเลิก
        if (!dialog.isSaved()) {
            return;
        }

        // 5. ถ้ากด Save
        try {
            Invoice selectedInvoice = dialog.getSelectedInvoice();
            
            // 6. สร้าง FinancialAid DTO
            FinancialAid aid = new FinancialAid(
                studentId,
                selectedInvoice.semesterId,
                selectedInvoice.id,
                dialog.getAidType(),
                dialog.getDescription(),
                dialog.getAmount()
            );

            // 7. เรียก DB Manager
            boolean success = dbManager.addFinancialAid(aid);

            if (success) {
            dbManager.logActivity(currentUsername, "Added financial aid (" + aid.aidType + ") of " + aid.amount + " for student " + studentId); // ⭐️ เพิ่มบรรทัดนี้
            JOptionPane.showMessageDialog(this, "Financial aid (" + aid.amount + ") applied successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

                refreshFinanceTable();
                updateFinanceDetailView(studentId, studentName);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to apply financial aid.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void viewInvoiceDetails() {
        // 1. ตรวจสอบว่าเลือก Invoice ในตาราง *ด้านล่าง* หรือยัง
        int detailRow = invoiceDetailTable.getSelectedRow();
        if (detailRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an invoice from the 'Invoices' detail table below.", "No Invoice Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // 2. ตรวจสอบว่าเลือกนักศึกษาในตาราง *ด้านบน* (ซึ่งควรจะถูกเลือกอยู่แล้ว)
        int masterRow = financeTable.getSelectedRow();
        if (masterRow < 0) {
             JOptionPane.showMessageDialog(this, "Please select a student first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            // 3. ดึงข้อมูล
            int invoiceId = (int) invoiceDetailModel.getValueAt(detailRow, 0);
            String studentName = (String) financeModel.getValueAt(masterRow, 1);

            // 4. เปิด Dialog
            InvoiceDetailsDialog dialog = new InvoiceDetailsDialog(this, dbManager, invoiceId, studentName);
            dialog.setVisible(true);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error opening invoice details: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    // ---===[ สิ้นสุดส่วนที่เพิ่ม ]===---

    private JPanel createDashboardPanel() {
        List<Student> students = dbManager.getAllStudents();
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(createCard("👩‍🎓 Total Students", String.valueOf(students.size())));
        double avgGpa = students.stream().mapToDouble(s -> s.gpa).average().orElse(0);
        panel.add(createCard("📈 Average GPA", String.format("%.2f", avgGpa)));
        long lowGpaCount = students.stream().filter(s -> s.gpa < 2.0).count();
        panel.add(createCard("⚠️ Low GPA (<2.0)", String.valueOf(lowGpaCount)));
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton statBtn = new JButton("📊 Statistics");
        addBtn = new JButton();
        editBtn = new JButton();
        deleteBtn = new JButton();
        btnPanel.add(statBtn);
        statBtn.setPreferredSize(new Dimension(120, 30));
        statBtn.addActionListener(e -> showStatistics());
        return btnPanel;
    }
    
    private JMenu createAdminMenu() {
        JMenu adminMenu = new JMenu("👤 Admin");
        JMenuItem userMgmtItem = new JMenuItem("Manage Users...");
        userMgmtItem.addActionListener(e -> showUserManagement());
        JMenuItem dbBackupItem = new JMenuItem("Database Backup...");
        dbBackupItem.addActionListener(e -> performDatabaseBackup());
        adminMenu.add(userMgmtItem);
        adminMenu.add(dbBackupItem);
        return adminMenu;
    }
    private JMenu createHelpMenu() {
        JMenu helpMenu = new JMenu("❓ Help");
        JMenuItem helpItem = new JMenuItem("View Help");
        helpItem.addActionListener(e -> showHelpDialog());
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(helpItem);
        helpMenu.add(aboutItem);
        return helpMenu;
    }
    private JMenu createSecurityMenu() {
        JMenu securityMenu = new JMenu("🔒 Security");
        JMenuItem changePassItem = new JMenuItem("🔑 Change Password...");
        changePassItem.addActionListener(e -> showChangePasswordDialog());
        JMenuItem logoutItem = new JMenuItem("🚪 Logout");
        logoutItem.addActionListener(e -> performLogout());
        securityMenu.add(changePassItem);
        securityMenu.add(logoutItem);
        return securityMenu;
    }

    private void showChangePasswordDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        JPasswordField oldPassField = new JPasswordField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();
        panel.add(new JLabel("Old Password:"));
        panel.add(oldPassField);
        panel.add(new JLabel("New Password:"));
        panel.add(newPassField);
        panel.add(new JLabel("Confirm New Password:"));
        panel.add(confirmPassField);
        int option = JOptionPane.showConfirmDialog(this, panel, "Change Password for " + currentUsername, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (option == JOptionPane.OK_OPTION) {
            String oldPass = new String(oldPassField.getPassword());
            String newPass = new String(newPassField.getPassword());
            String confirmPass = new String(confirmPassField.getPassword());
            if (newPass.isEmpty() || !newPass.equals(confirmPass)) { JOptionPane.showMessageDialog(this, "New passwords do not match or are empty.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            Role checkRole = dbManager.checkLogin(currentUsername, oldPass);
            if (checkRole == Role.UNKNOWN) { JOptionPane.showMessageDialog(this, "Incorrect old password.", "Error", JOptionPane.ERROR_MESSAGE); return; }
            try {
                dbManager.updatePassword(currentUsername, newPass);
                JOptionPane.showMessageDialog(this, "Password changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Failed to update password. Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE); }
        }
    }
    private void performLogout() {
        this.dispose();
        main(null);
    }
    
    private void setupContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem editItem = new JMenuItem("✏️ Edit Student");
        JMenuItem deleteItem = new JMenuItem("🗑️ Delete Student");
        JMenuItem recordItem = new JMenuItem("🎓 View Academic Record"); 
        
        boolean canWrite = (currentUserRole == Role.ADMIN);
        editItem.setEnabled(canWrite);
        deleteItem.setEnabled(canWrite);
        recordItem.setEnabled(canWrite); 

        editItem.addActionListener(e -> editStudent());
        deleteItem.addActionListener(e -> deleteStudent());
        recordItem.addActionListener(e -> showAcademicRecord()); 
        
        popupMenu.add(editItem);
        popupMenu.add(deleteItem);
        popupMenu.addSeparator(); 
        popupMenu.add(recordItem); 

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < table.getRowCount()) {
                        table.getSelectionModel().setSelectionInterval(row, row);
                    } else {
                        table.clearSelection();
                    }
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });
    }

    private void showUserManagement() { showNotImplemented("User Management"); }
    private void performDatabaseBackup() {
        String sourceDbFile = dbManager.getDatabaseFileName();
        File sourceFile = new File(sourceDbFile);
        if (!sourceFile.exists()) { JOptionPane.showMessageDialog(this, "Error: Database file not found!", "Backup Error", JOptionPane.ERROR_MESSAGE); return; }
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Save Database Backup");
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultName = "backup_" + timestamp + ".db";
        fc.setSelectedFile(new File(defaultName));
        fc.setFileFilter(new FileNameExtensionFilter("Database Files (*.db)", "db"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File destFile = fc.getSelectedFile();
            try {
                java.nio.file.Path sourcePath = sourceFile.toPath();
                java.nio.file.Path destPath = destFile.toPath();
                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                JOptionPane.showMessageDialog(this, "Database backup successful!", "Backup Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) { ex.printStackTrace(); JOptionPane.showMessageDialog(this, "Backup failed: " + ex.getMessage(), "Backup Error", JOptionPane.ERROR_MESSAGE); }
        }
    }
    private void showHelpDialog() {
        JOptionPane.showMessageDialog(this, "<html><b>How to Use:</b><br>1. Use 'Add', 'Edit', 'Delete' buttons to manage students.<br>2. Use the search bar for real-time filtering.<br>3. Use 'File' > 'Import/Export' for data transfer.</html>", "Help", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showAboutDialog() {
        String aboutText = "🎓 Student Management System\nVersion 3.0 (Relational DB Edition)\nDeveloped by: Ekkarat T.";
        JOptionPane.showMessageDialog(this, aboutText, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAcademicRecord() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a student to view records.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(viewRow);
        String studentId = (String) model.getValueAt(modelRow, 0);
        String studentName = (String) model.getValueAt(modelRow, 1);

        AcademicRecordGUI recordDialog = new AcademicRecordGUI(this, dbManager, studentId, studentName);
        recordDialog.setVisible(true);

        refreshTable();
    }
    private JPanel createSecurityAdminPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JTabbedPane adminTabs = new JTabbedPane();
        
        adminTabs.addTab("👤 User Management", createUserManagementPanel());
        adminTabs.addTab("📜 Activity Log", createActivityLogPanel());
        adminTabs.addTab("💾 Backup / Restore", createBackupPanel()); // ⭐️ ย้าย Backup มาที่นี่

        mainPanel.add(adminTabs, BorderLayout.CENTER);
        return mainPanel;
    }

    /**
     * (ใหม่) สร้าง Panel จัดการ User
     */
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        userModel = new DefaultTableModel(new Object[]{"Username", "Role", "Last Login"}, 0);
        userTable = new JTable(userModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnResetPass = new JButton("Reset Password...");
        JButton btnChangeRole = new JButton("Change Role...");
        JButton btnDeleteUser = new JButton("Delete User");
        
        btnResetPass.addActionListener(e -> resetPassword());
        btnChangeRole.addActionListener(e -> changeRole());
        btnDeleteUser.addActionListener(e -> deleteUser());
        
        buttonPanel.add(btnResetPass);
        buttonPanel.add(btnChangeRole);
        buttonPanel.add(btnDeleteUser);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * (ใหม่) สร้าง Panel แสดง Log
     */
    private JPanel createActivityLogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        
        logModel = new DefaultTableModel(new Object[]{"Timestamp", "User", "Action"}, 0);
        logTable = new JTable(logModel);
        panel.add(new JScrollPane(logTable), BorderLayout.CENTER);
        
        JButton btnRefreshLog = new JButton("Refresh Log");
        btnRefreshLog.addActionListener(e -> refreshLogTable());
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.add(btnRefreshLog);
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    /**
     * (ใหม่) สร้าง Panel สำหรับ Backup (ย้ายของเก่ามา)
     */
    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        JButton btnBackup = new JButton("Perform Database Backup...");
        btnBackup.addActionListener(e -> performDatabaseBackup());
        
        JButton btnRestore = new JButton("Restore Database...");
        btnRestore.addActionListener(e -> showNotImplemented("Database Restore (Dangerous Operation)"));
        
        panel.add(btnBackup);
        panel.add(btnRestore);
        
        return panel;
    }

    /**
     * (ใหม่) Refresh ตาราง User
     */
    private void refreshUserTable() {
        if (userModel == null) return;
        userModel.setRowCount(0);
        ArrayList<UserAccount> users = dbManager.getAllUserAccounts();
        for (UserAccount user : users) {
            userModel.addRow(new Object[]{user.username, user.role, user.lastLogin});
        }
    }

    /**
     * (ใหม่) Refresh ตาราง Log
     */
    private void refreshLogTable() {
        if (logModel == null) return;
        logModel.setRowCount(0);
        ArrayList<ActivityLog> logs = dbManager.getActivityLogs();
        for (ActivityLog log : logs) {
            logModel.addRow(new Object[]{log.timestamp, log.username, log.action});
        }
    }

    /**
     * (ใหม่) Action: Reset Password
     */
    private void resetPassword() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to reset.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) userModel.getValueAt(row, 0);
        String newPassword = JOptionPane.showInputDialog(this, "Enter new temporary password for " + username + ":", "Reset Password", JOptionPane.PLAIN_MESSAGE);
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (dbManager.resetUserPassword(username, newPassword.trim())) {
                dbManager.logActivity(currentUsername, "Reset password for user: " + username);
                JOptionPane.showMessageDialog(this, "Password reset successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to reset password.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * (ใหม่) Action: Change Role
     */
    private void changeRole() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to change role.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) userModel.getValueAt(row, 0);
        Role currentRole = Role.valueOf((String) userModel.getValueAt(row, 0));

        // สร้าง Array ของ Role ทั้งหมด (ยกเว้น UNKNOWN)
        Role[] allRoles = {Role.ADMIN, Role.OFFICER, Role.TEACHER, Role.STUDENT, Role.GUEST};
        
        Role newRole = (Role) JOptionPane.showInputDialog(
            this, "Select new role for " + username + ":", "Change Role",
            JOptionPane.PLAIN_MESSAGE, null, allRoles, currentRole
        );

        if (newRole != null && newRole != currentRole) {
            if (dbManager.updateUserRole(username, newRole)) {
                dbManager.logActivity(currentUsername, "Changed role for " + username + " to " + newRole.name());
                JOptionPane.showMessageDialog(this, "Role updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUserTable();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update role.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * (ใหม่) Action: Delete User
     */
    private void deleteUser() {
        int row = userTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No User Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String username = (String) userModel.getValueAt(row, 0);
        Role role = Role.valueOf((String) userModel.getValueAt(row, 1));
        
        if (username.equals(currentUsername)) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this, 
            "Are you sure you want to PERMANENTLY delete user '" + username + "'?\n" +
            "This will delete all associated student/teacher records, enrollments, etc.\n" +
            "THIS ACTION CANNOT BE UNDONE.",
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dbManager.deleteUser(username, role)) {
                dbManager.logActivity(currentUsername, "DELETED user: " + username);
                JOptionPane.showMessageDialog(this, "User deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshUserTable();
                refreshTable(); // Refresh ตารางนักเรียน/ครูด้วย
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // ⭐️ (ใหม่) เมธอดสำหรับแก้ไขวิชา
    private void editCourse() {
        int viewRow = courseTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = courseTable.convertRowIndexToModel(viewRow);
        String subjectId = (String) courseModel.getValueAt(modelRow, 0);

        // 1. ดึงข้อมูลดิบ (ที่มี ID)
        CourseData existingData = dbManager.getSubjectAndAssignmentData(subjectId);
        if (existingData == null) {
            JOptionPane.showMessageDialog(this, "Could not find course data for ID: " + subjectId, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2. ดึงข้อมูลสำหรับ ComboBoxes
        ArrayList<Major> majors = dbManager.getAllMajors();
        ArrayList<Semester> semesters = dbManager.getAllSemesters();
        ArrayList<Teacher> teachers = dbManager.getAllTeachers();

        // 3. สร้าง GUI Panel (เหมือน CourseDialog)
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 5));
        
        JTextField txtId = new JTextField(existingData.id);
        txtId.setEditable(false); // ห้ามแก้ ID
        JTextField txtName = new JTextField(existingData.name);
        JTextField txtCredits = new JTextField(String.valueOf(existingData.credits));
        JTextField txtRoom = new JTextField(existingData.room != null ? existingData.room : "");
        JTextField txtDay = new JTextField(existingData.day != null ? existingData.day : "");
        JTextField txtTime = new JTextField(existingData.time != null ? existingData.time : "");

        JComboBox<Major> cbMajor = new JComboBox<>(majors.toArray(new Major[0]));
        JComboBox<Semester> cbSemester = new JComboBox<>(semesters.toArray(new Semester[0]));
        JComboBox<Teacher> cbTeacher = new JComboBox<>(teachers.toArray(new Teacher[0]));
        
        // 4. ตั้งค่า ComboBox ให้ตรงกับข้อมูลเก่า
        for (Major m : majors) { if (m.id.equals(existingData.majorId)) { cbMajor.setSelectedItem(m); break; } }
        for (Semester s : semesters) { if (s.id.equals(existingData.semesterId)) { cbSemester.setSelectedItem(s); break; } }
        for (Teacher t : teachers) { if (t.id.equals(existingData.teacherId)) { cbTeacher.setSelectedItem(t); break; } }

        panel.add(new JLabel("Subject ID:")); panel.add(txtId);
        panel.add(new JLabel("Subject Name:")); panel.add(txtName);
        panel.add(new JLabel("Credits:")); panel.add(txtCredits);
        panel.add(new JLabel("Major:")); panel.add(cbMajor);
        panel.add(new JLabel("Semester:")); panel.add(cbSemester);
        panel.add(new JLabel("Teacher:")); panel.add(cbTeacher);
        panel.add(new JLabel("Room:")); panel.add(txtRoom);
        panel.add(new JLabel("Schedule Day:")); panel.add(txtDay);
        panel.add(new JLabel("Schedule Time:")); panel.add(txtTime);

        // 5. แสดง Dialog
        int result = JOptionPane.showConfirmDialog(this, panel, "Edit Course: " + subjectId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try {
                // 6. อ่านข้อมูลใหม่
                String name = txtName.getText();
                if (name == null || name.trim().isEmpty()) { // ⭐️ ควรเพิ่ม Check นี้
                    JOptionPane.showMessageDialog(this, "Course Name cannot be empty.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return; 
                }
                int credits = Integer.parseInt(txtCredits.getText());
                String majorId = ((Major) cbMajor.getSelectedItem()).id;
                String semesterId = ((Semester) cbSemester.getSelectedItem()).id;
                String teacherId = ((Teacher) cbTeacher.getSelectedItem()).id;
                String room = txtRoom.getText();
                String day = txtDay.getText();
                String time = txtTime.getText();
                
                CourseData updatedData = new CourseData(subjectId, name, credits, majorId, semesterId, teacherId, room, day, time);
                
                // 7. อัปเดตฐานข้อมูล
                if (dbManager.updateCourse(updatedData)) {
                    dbManager.logActivity(currentUsername, "Edited course: " + subjectId);
                    JOptionPane.showMessageDialog(this, "Course updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshCourseTable();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update course.", "Database Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid number for Credits.", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "An error occurred: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // ⭐️ (ใหม่) เมธอดสำหรับตั้งค่า Prerequisites
    private void setPrerequisites() {
        int viewRow = courseTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course to set its prerequisites.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int modelRow = courseTable.convertRowIndexToModel(viewRow);
        
        String mainSubjectId = (String) courseModel.getValueAt(modelRow, 0);
        String mainSubjectName = (String) courseModel.getValueAt(modelRow, 1);
        String prereqsStr = (String) courseModel.getValueAt(modelRow, 7); // คอลัมน์ที่ 7
        
        List<String> currentPrereqIds = new ArrayList<>();
        if (prereqsStr != null && !prereqsStr.isEmpty() && !prereqsStr.equals("N/A")) {
            currentPrereqIds.addAll(List.of(prereqsStr.split(",")));
        }

        // 1. ดึงวิชาทั้งหมด
        ArrayList<Subject> allSubjects = dbManager.getAllSubjects();
        
        // 2. สร้าง List ของวิชา (ไม่รวมตัวเอง)
        DefaultListModel<Subject> listModel = new DefaultListModel<>();
        List<Subject> availableSubjects = new ArrayList<>();
        for (Subject s : allSubjects) {
            if (!s.id.equals(mainSubjectId)) {
                listModel.addElement(s); // ใช้ toString() ที่เรา override ไว้
                availableSubjects.add(s);
            }
        }
        
        JList<Subject> subjectList = new JList<>(listModel);
        subjectList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        // 3. ตั้งค่าที่เลือกไว้ (Highlight ตัวที่เลือกไว้แล้ว)
        List<Integer> indicesToSelect = new ArrayList<>();
        for (String prereqId : currentPrereqIds) {
            for (int i = 0; i < availableSubjects.size(); i++) {
                if (availableSubjects.get(i).id.equals(prereqId)) {
                    indicesToSelect.add(i);
                    break;
                }
            }
        }
        // แปลง List<Integer> เป็น int[]
        subjectList.setSelectedIndices(indicesToSelect.stream().mapToInt(i -> i).toArray());

        // 4. สร้าง Dialog
        JScrollPane scrollPane = new JScrollPane(subjectList);
        scrollPane.setPreferredSize(new Dimension(400, 300));
        
        int result = JOptionPane.showConfirmDialog(
            this, 
            scrollPane, 
            "Set Prerequisites for: " + mainSubjectName, 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            // 5. อ่านข้อมูลที่เลือก
            List<Subject> selectedSubjects = subjectList.getSelectedValuesList();
            List<String> selectedSubjectIds = new ArrayList<>();
            for (Subject s : selectedSubjects) {
                selectedSubjectIds.add(s.id);
            }
            
            // 6. อัปเดตฐานข้อมูล
            if (dbManager.setPrerequisites(mainSubjectId, selectedSubjectIds)) {
                dbManager.logActivity(currentUsername, "Set prerequisites for " + mainSubjectId + " to: " + String.join(",", selectedSubjectIds));
                JOptionPane.showMessageDialog(this, "Prerequisites updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshCourseTable(); // Refresh ตาราง
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update prerequisites.", "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}