import javax.swing.*;
import java.awt.*;

public class TeacherDialog extends JDialog {
    private JTextField idField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField officeField;
    private JButton saveButton;
    private JButton cancelButton;
    private Teacher newTeacher = null;

    public TeacherDialog(Frame parent) {
        super(parent, "Add New Teacher", true);
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Teacher ID (Username):"), gbc);
        gbc.gridx = 1; idField = new JTextField(20); formPanel.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(20); formPanel.add(nameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; emailField = new JTextField(20); formPanel.add(emailField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Office:"), gbc);
        gbc.gridx = 1; officeField = new JTextField(20); formPanel.add(officeField, gbc);

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
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String office = officeField.getText().trim();
        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "ID, Name, and Email are required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        this.newTeacher = new Teacher(id, name, email, office);
        dispose();
    }
    public Teacher getTeacher() { return newTeacher; }
}