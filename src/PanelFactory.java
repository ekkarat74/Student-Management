import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelFactory {

    public static JPanel createStudentListPanel(StudentManagementGUI gui) {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(gui.table), BorderLayout.CENTER);
        tablePanel.add(gui.createButtonPanel(), BorderLayout.SOUTH);
        return tablePanel;
    }

    public static JPanel createCoursePanel(StudentManagementGUI gui) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gui.courseModel = new DefaultTableModel(new Object[]{
            "ID", "Course Name", "Credits", "Major", "Semester", "Teacher", "Schedule", "Prerequisites"
        }, 0);
        gui.courseTable = new JTable(gui.courseModel);
        gui.courseTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(new JScrollPane(gui.courseTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddCourse = new JButton("➕ Add Course");
        JButton btnEditCourse = new JButton("✏️ Edit Course");
        JButton btnSetPrereq = new JButton("🔗 Set Prerequisites");
        
        btnAddCourse.addActionListener(e -> gui.addCourse()); 
        btnEditCourse.addActionListener(e -> gui.editCourse());
        btnSetPrereq.addActionListener(e -> gui.setPrerequisites());
        
        boolean canWrite = (gui.currentUserRole == Role.ADMIN);
        btnAddCourse.setEnabled(canWrite);
        btnEditCourse.setEnabled(canWrite);
        btnSetPrereq.setEnabled(canWrite);

        buttonPanel.add(btnAddCourse);
        buttonPanel.add(btnEditCourse);
        buttonPanel.add(btnSetPrereq);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    public static JPanel createClassroomPanel(StudentManagementGUI gui) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gui.classroomModel = new DefaultTableModel(new Object[]{"ID", "Classroom Name", "Type", "Major", "Homeroom Teacher"}, 0);
        gui.classroomTable = new JTable(gui.classroomModel);
        gui.classroomTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        gui.classroomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(gui.classroomTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAdd = new JButton("➕ Add Classroom");
        JButton btnEdit = new JButton("✏️ Edit Classroom");
        JButton btnDelete = new JButton("🗑️ Delete Classroom");

        btnAdd.addActionListener(e -> gui.addClassroom());
        btnEdit.addActionListener(e -> gui.editClassroom());
        btnDelete.addActionListener(e -> gui.deleteClassroom());
        
        boolean canWrite = (gui.currentUserRole == Role.ADMIN);
        btnAdd.setEnabled(canWrite);
        btnEdit.setEnabled(canWrite);
        btnDelete.setEnabled(canWrite);

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnDelete);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    public static JPanel createLeavePanel(StudentManagementGUI gui) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gui.leaveModel = new DefaultTableModel(new Object[]{
            "ID", "Teacher Name", "Start Date", "End Date", "Reason", "Status"
        }, 0);
        gui.leaveTable = new JTable(gui.leaveModel);
        gui.leaveTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        panel.add(new JScrollPane(gui.leaveTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnApprove = new JButton("👍 Approve");
        JButton btnDeny = new JButton("👎 Deny");
        
        btnApprove.addActionListener(e -> gui.updateLeaveStatus("APPROVED"));
        btnDeny.addActionListener(e -> gui.updateLeaveStatus("DENIED"));
        
        boolean canWrite = (gui.currentUserRole == Role.ADMIN);
        btnApprove.setEnabled(canWrite);
        btnDeny.setEnabled(canWrite);

        buttonPanel.add(btnApprove);
        buttonPanel.add(btnDeny);
        
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    public static JPanel createFinancePanel(StudentManagementGUI gui) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        gui.financeModel = new DefaultTableModel(new Object[]{
            "Student ID", "Student Name", "Total Due", "Total Paid", "Balance", "Status"
        }, 0);
        gui.financeTable = new JTable(gui.financeModel);
        gui.financeTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        gui.financeTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel detailPanel = createFinanceDetailPanel(gui);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(gui.financeTable), detailPanel);
        splitPane.setDividerLocation(300);
        panel.add(splitPane, BorderLayout.CENTER);
        panel.add(createFinanceButtonPanel(gui), BorderLayout.SOUTH);

        gui.financeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = gui.financeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String studentId = (String) gui.financeModel.getValueAt(selectedRow, 0);
                    String studentName = (String) gui.financeModel.getValueAt(selectedRow, 1);
                    gui.updateFinanceDetailView(studentId, studentName);
                } else {
                    gui.clearFinanceDetailView();
                }
            }
        });

        return panel;
    }

    private static JPanel createFinanceDetailPanel(StudentManagementGUI gui) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        gui.financeDetailLabel = new JLabel("Select a student to view details", SwingConstants.CENTER);
        gui.financeDetailLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        panel.add(gui.financeDetailLabel, BorderLayout.NORTH);

        gui.invoiceDetailModel = new DefaultTableModel(new Object[]{"Invoice ID", "Issue Date", "Due Date", "Amount", "Status"}, 0);
        gui.invoiceDetailTable = new JTable(gui.invoiceDetailModel);

        gui.transactionDetailModel = new DefaultTableModel(new Object[]{"Transaction ID", "Payment Date", "Amount Paid", "Method"}, 0);
        gui.transactionDetailTable = new JTable(gui.transactionDetailModel);

        JTabbedPane detailTabs = new JTabbedPane();
        detailTabs.addTab("Invoices", new JScrollPane(gui.invoiceDetailTable));
        detailTabs.addTab("Payment History", new JScrollPane(gui.transactionDetailTable));

        panel.add(detailTabs, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createFinanceButtonPanel(StudentManagementGUI gui) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JButton btnGenerateInvoices = new JButton("Generate Semester Invoices...");
        JButton btnAddPayment = new JButton("Add Payment / Transaction...");
        JButton btnAddAid = new JButton("Add Scholarship / Discount...");
        JButton btnViewInvoiceDetails = new JButton("View Invoice Details");

        btnGenerateInvoices.addActionListener(e -> gui.generateInvoices());
        btnAddPayment.addActionListener(e -> gui.addPayment());
        btnAddAid.addActionListener(e -> gui.addFinancialAid());
        btnViewInvoiceDetails.addActionListener(e -> gui.viewInvoiceDetails());

        boolean canWrite = (gui.currentUserRole == Role.ADMIN);
        btnGenerateInvoices.setEnabled(canWrite);
        btnAddPayment.setEnabled(canWrite);
        btnAddAid.setEnabled(canWrite);

        buttonPanel.add(btnGenerateInvoices);
        buttonPanel.add(btnAddPayment);
        buttonPanel.add(btnAddAid);
        buttonPanel.add(btnViewInvoiceDetails);
        
        return buttonPanel;
    }

    public static JPanel createReportDashboardPanel(StudentManagementGUI gui) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JTabbedPane reportTabs = new JTabbedPane();
        
        reportTabs.addTab("📈 Overview", gui.createOverviewPanel());
        reportTabs.addTab("📋 Academic & Finance Reports", gui.createTextReportPanel());
        reportTabs.addTab("📊 Charts (JFreeChart needed)", gui.createChartPanelStub());
        
        mainPanel.add(reportTabs, BorderLayout.CENTER);
        return mainPanel;
    }

    public static JPanel createSecurityAdminPanel(StudentManagementGUI gui) {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JTabbedPane adminTabs = new JTabbedPane();
        
        adminTabs.addTab("👤 User Management", gui.createUserManagementPanel());
        adminTabs.addTab("📜 Activity Log", gui.createActivityLogPanel());
        adminTabs.addTab("💾 Backup / Restore", gui.createBackupPanel()); 

        mainPanel.add(adminTabs, BorderLayout.CENTER);
        return mainPanel;
    }
}