import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;

public class StudentDialog extends JDialog {
    private JTextField idField, nameField, emailField, photoPathField, ageField, gpaField;
    private JTextArea addressArea; 
    private JTextField phoneField;  
    private JButton photoButton;     
    private JComboBox<String> majorDropdown, yearDropdown, statusDropdown;
    private JComboBox<Classroom> classroomDropdown;
    private ArrayList<Classroom> classroomList;
    private JButton saveButton, cancelButton;
    private JTextField previousSchoolField;
    private JTextField docAppField;
    private JButton docAppButton;
    private JTextField docIdField;
    private JButton docIdButton;
    private JTextField docTranscriptField;
    private JButton docTranscriptButton;
    private Student student = null;
    private StudentManagementGUI parentGUI;
    private Student existingStudent;
    private boolean isNewStudent;

    public StudentDialog(StudentManagementGUI parentGUI, String title,
                         String[] majorOptions, String[] yearOptions, String[] statusOptions,
                         Student existingStudent, ArrayList<Classroom> classrooms) {
        super(parentGUI, title, true);
        this.parentGUI = parentGUI;
        this.existingStudent = existingStudent;
        this.isNewStudent = (existingStudent == null);
        this.classroomList = (classrooms != null) ? classrooms : new ArrayList<>();

        setSize(550, 800);
        setLocationRelativeTo(parentGUI);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);

        JPanel formPanel = createFormPanel(majorOptions, yearOptions, statusOptions);
        add(new JScrollPane(formPanel), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton = new JButton("💾 Save");
        cancelButton = new JButton("❌ Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        if (!isNewStudent) {
            idField.setText(existingStudent.id);
            idField.setEnabled(false);
            nameField.setText(existingStudent.name);
            addressArea.setText(existingStudent.address); 
            phoneField.setText(existingStudent.phone);   
            emailField.setText(existingStudent.email);
            photoPathField.setText(existingStudent.photoPath); 
            ageField.setText(String.valueOf(existingStudent.age));
            gpaField.setText(String.valueOf(existingStudent.gpa));
            majorDropdown.setSelectedItem(existingStudent.major);
            yearDropdown.setSelectedItem(String.valueOf(existingStudent.year));
            statusDropdown.setSelectedItem(existingStudent.status.name());
            
            if (existingStudent.classroomId != null) {
                for (Classroom c : this.classroomList) {
                    if (c.id.equals(existingStudent.classroomId)) {
                        classroomDropdown.setSelectedItem(c);
                        break;
                    }
                }
            }
            previousSchoolField.setText(existingStudent.previousSchool);
            docAppField.setText(existingStudent.docApplicationPath);
            docIdField.setText(existingStudent.docIdCardPath);
            docTranscriptField.setText(existingStudent.docTranscriptPath);
        }

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
        photoButton.addActionListener(e -> onSelectFile(photoPathField, "Images"));
        docAppButton.addActionListener(e -> onSelectFile(docAppField, "Documents"));
        docIdButton.addActionListener(e -> onSelectFile(docIdField, "Documents"));
        docTranscriptButton.addActionListener(e -> onSelectFile(docTranscriptField, "Documents"));
    }
    
    private void onSelectFile(JTextField pathField, String fileType) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select File");
        if (fileType.equals("Images")) {
            fc.setFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif"));
        } else {
            fc.setFileFilter(new FileNameExtensionFilter("Documents", "pdf", "doc", "docx", "jpg", "png"));
        }
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            pathField.setText(file.getAbsolutePath());
        }
    }

    private JPanel createFormPanel(String[] majorOptions, String[] yearOptions, String[] statusOptions) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        int y = 0;

        gbc.gridy = y; panel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; idField = new JTextField(20); panel.add(idField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(20); panel.add(nameField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Address:"), gbc);
        gbc.gridx = 1; addressArea = new JTextArea(3, 20); addressArea.setLineWrap(true);
        addressArea.setWrapStyleWord(true); JScrollPane scrollPane = new JScrollPane(addressArea);
        gbc.fill = GridBagConstraints.BOTH; panel.add(scrollPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL; y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; phoneField = new JTextField(20); panel.add(phoneField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; emailField = new JTextField(20); panel.add(emailField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Photo Path:"), gbc);
        gbc.gridx = 1; photoPathField = new JTextField(15); photoButton = new JButton("...");
        panel.add(createBrowsePanel(photoPathField, photoButton), gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Age:"), gbc);
        gbc.gridx = 1; ageField = new JTextField(20); panel.add(ageField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("GPA:"), gbc);
        gbc.gridx = 1; gpaField = new JTextField(20); panel.add(gpaField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Major:"), gbc);
        gbc.gridx = 1; majorDropdown = new JComboBox<>(majorOptions); panel.add(majorDropdown, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1; yearDropdown = new JComboBox<>(yearOptions); panel.add(yearDropdown, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Classroom:"), gbc);
        gbc.gridx = 1; 
        classroomDropdown = new JComboBox<>(classroomList.toArray(new Classroom[0]));
        panel.add(classroomDropdown, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1; statusDropdown = new JComboBox<>(statusOptions); panel.add(statusDropdown, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Previous School:"), gbc);
        gbc.gridx = 1; previousSchoolField = new JTextField(20); panel.add(previousSchoolField, gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Application Doc:"), gbc);
        gbc.gridx = 1; docAppField = new JTextField(15); docAppButton = new JButton("...");
        panel.add(createBrowsePanel(docAppField, docAppButton), gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("ID Card Doc:"), gbc);
        gbc.gridx = 1; docIdField = new JTextField(15); docIdButton = new JButton("...");
        panel.add(createBrowsePanel(docIdField, docIdButton), gbc); y++;
        gbc.gridx = 0; gbc.gridy = y; panel.add(new JLabel("Transcript Doc:"), gbc);
        gbc.gridx = 1; docTranscriptField = new JTextField(15); docTranscriptButton = new JButton("...");
        panel.add(createBrowsePanel(docTranscriptField, docTranscriptButton), gbc); y++;

        return panel;
    }

    private JPanel createBrowsePanel(JTextField field, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(field, BorderLayout.CENTER);
        panel.add(button, BorderLayout.EAST);
        return panel;
    }

    private void onSave() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String address = addressArea.getText().trim(); 
        String phone = phoneField.getText().trim();   
        String email = emailField.getText().trim();
        String photoPath = photoPathField.getText().trim(); 
        String ageStr = ageField.getText().trim();
        String gpaStr = gpaField.getText().trim();
        String major = (String) majorDropdown.getSelectedItem();
        String yearStr = (String) yearDropdown.getSelectedItem();
        String status = (String) statusDropdown.getSelectedItem();
        Classroom selectedClassroom = (Classroom) classroomDropdown.getSelectedItem();
        String classroomId = (selectedClassroom != null) ? selectedClassroom.id : null;
        String previousSchool = previousSchoolField.getText().trim();
        String docApp = docAppField.getText().trim();
        String docId = docIdField.getText().trim();
        String docTranscript = docTranscriptField.getText().trim();

        String validation = parentGUI.validateInput(id, name, ageStr, gpaStr, major, email, yearStr, status, isNewStudent);
        if (validation != null) {
            JOptionPane.showMessageDialog(this, validation, "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int age = Integer.parseInt(ageStr);
        double gpa = Double.parseDouble(gpaStr);
        int year = Integer.parseInt(yearStr.replace("+", ""));
        StudentStatus statusEnum = StudentStatus.valueOf(status);

        if (isNewStudent) {
            String dateAdded = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
            student = new Student(id, name, address, phone, email, photoPath, age, gpa, year, statusEnum, major,
                                  dateAdded,
                                  previousSchool, docApp, docId, docTranscript,
                                  classroomId);
        } else {
            existingStudent.name = name;
            existingStudent.address = address;
            existingStudent.phone = phone;
            existingStudent.email = email;
            existingStudent.photoPath = photoPath;
            existingStudent.age = age;
            existingStudent.gpa = gpa;
            existingStudent.year = year;
            existingStudent.status = statusEnum;
            existingStudent.major = major;
            existingStudent.classroomId = classroomId;
            existingStudent.previousSchool = previousSchool;
            existingStudent.docApplicationPath = docApp;
            existingStudent.docIdCardPath = docId;
            existingStudent.docTranscriptPath = docTranscript;
            student = existingStudent;
        }
        dispose();
    }

    public Student getStudent() { return student; }
}