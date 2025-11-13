import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CourseDialog extends JDialog {

    private JTextField idField, nameField, creditsField, roomField, timeField;
    private JComboBox<Major> majorBox;
    private JComboBox<Semester> semesterBox;
    private JComboBox<Teacher> teacherBox;
    private JComboBox<String> dayBox;
    
    private JButton saveButton, cancelButton;
    private CourseData courseData = null;

    public CourseDialog(Frame parent, ArrayList<Major> majors, ArrayList<Semester> semesters, ArrayList<Teacher> teachers) {
        super(parent, "Add New Course", true);
        
        setSize(500, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Subject ID:"), gbc);
        gbc.gridx = 1; idField = new JTextField(20); formPanel.add(idField, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Subject Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(20); formPanel.add(nameField, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Credits:"), gbc);
        gbc.gridx = 1; creditsField = new JTextField(20); formPanel.add(creditsField, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Major:"), gbc);
        gbc.gridx = 1; majorBox = new JComboBox<>(majors.toArray(new Major[0])); 
        formPanel.add(majorBox, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Semester:"), gbc);
        gbc.gridx = 1; semesterBox = new JComboBox<>(semesters.toArray(new Semester[0]));
        formPanel.add(semesterBox, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Teacher:"), gbc);
        gbc.gridx = 1; teacherBox = new JComboBox<>(teachers.toArray(new Teacher[0]));
        teacherBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Teacher) {
                    setText(((Teacher) value).name);
                }
                return this;
            }
        });
        formPanel.add(teacherBox, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Room:"), gbc);
        gbc.gridx = 1; roomField = new JTextField(20); formPanel.add(roomField, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Day:"), gbc);
        gbc.gridx = 1; dayBox = new JComboBox<>(new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "N/A"});
        formPanel.add(dayBox, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Time (e.g., 09:00-12:00):"), gbc);
        gbc.gridx = 1; timeField = new JTextField(20); formPanel.add(timeField, gbc); y++;


        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton = new JButton("💾 Save");
        cancelButton = new JButton("❌ Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
    }

    private void onSave() {
        // 1. ดึงข้อมูล
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String creditsStr = creditsField.getText().trim();
        Major major = (Major) majorBox.getSelectedItem();
        Semester semester = (Semester) semesterBox.getSelectedItem();
        Teacher teacher = (Teacher) teacherBox.getSelectedItem();
        String room = roomField.getText().trim();
        String day = (String) dayBox.getSelectedItem();
        String time = timeField.getText().trim();

        // 2. ตรวจสอบ
        if (id.isEmpty() || name.isEmpty() || creditsStr.isEmpty() || room.isEmpty() || time.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        int credits;
        try {
            credits = Integer.parseInt(creditsStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Credits must be a number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3. สร้าง CourseData
        this.courseData = new CourseData(id, name, credits, major.id, semester.id, teacher.id, room, day, time);
        dispose();
    }

    public CourseData getCourseData() {
        return courseData;
    }
}