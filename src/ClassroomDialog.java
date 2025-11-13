import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ClassroomDialog extends JDialog {
    
    private JTextField txtId, txtName;
    private JComboBox<Major> cbMajor;
    private JComboBox<Teacher> cbTeacher;
    private JComboBox<ClassroomType> cbType;
    private Classroom savedClassroom = null;
    private boolean isEditMode;

    private ArrayList<Major> majorList;
    private ArrayList<Teacher> teacherList;

    public ClassroomDialog(JFrame parent, String title, Classroom existingClassroom, ArrayList<Major> majors, ArrayList<Teacher> teachers) {
        super(parent, title, true);
        this.isEditMode = (existingClassroom != null);
        this.majorList = majors;
        this.teacherList = teachers;
        
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // --- Fields ---
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Classroom ID:"), gbc);
        gbc.gridx = 1; txtId = new JTextField(20); formPanel.add(txtId, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Classroom Name:"), gbc);
        gbc.gridx = 1; txtName = new JTextField(20); formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Major:"), gbc);
        gbc.gridx = 1; cbMajor = new JComboBox<>(majors.toArray(new Major[0])); formPanel.add(cbMajor, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Homeroom Teacher:"), gbc);
        gbc.gridx = 1; cbTeacher = new JComboBox<>(teachers.toArray(new Teacher[0])); formPanel.add(cbTeacher, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Classroom Type:"), gbc);
        gbc.gridx = 1; 
        cbType = new JComboBox<>(ClassroomType.values());
        formPanel.add(cbType, gbc);
        
        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());

        if (isEditMode) {
            txtId.setText(existingClassroom.id);
            txtId.setEditable(false);
            txtName.setText(existingClassroom.name);
            
            for (Major m : majorList) {
                if (m.id.equals(existingClassroom.majorId)) {
                    cbMajor.setSelectedItem(m);
                    break;
                }
            }
            for (Teacher t : teacherList) {
                if (t.id.equals(existingClassroom.teacherId)) {
                    cbTeacher.setSelectedItem(t);
                    break;
                }
            }
            if (existingClassroom.type != null) {
                cbType.setSelectedItem(existingClassroom.type);
            }
        }
    }

    private void onSave() {
        String id = txtId.getText().trim();
        String name = txtName.getText().trim();
        
        if (id.isEmpty() || name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Classroom ID and Name are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Major selectedMajor = (Major) cbMajor.getSelectedItem();
        Teacher selectedTeacher = (Teacher) cbTeacher.getSelectedItem();
        ClassroomType selectedType = (ClassroomType) cbType.getSelectedItem(); 
        String majorId = (selectedMajor != null) ? selectedMajor.id : null;
        String teacherId = (selectedTeacher != null) ? selectedTeacher.id : null;
        this.savedClassroom = new Classroom(id, name, teacherId, null, majorId, null, selectedType); 
        
        dispose();
    }

    public Classroom getClassroom() {
        return savedClassroom;
    }
}