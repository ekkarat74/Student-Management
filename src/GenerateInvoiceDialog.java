import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GenerateInvoiceDialog extends JDialog {

    private JComboBox<Semester> semesterComboBox;
    private JTextField baseFeeField;
    private JTextField issueDateField;
    private JTextField dueDateField;
    private JButton generateButton;
    private JButton cancelButton;
    
    private boolean isGenerated = false;

    public GenerateInvoiceDialog(Frame parent, ArrayList<Semester> semesters) {
        super(parent, "Generate Semester Invoices", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Select Semester:"), gbc);
        gbc.gridx = 1; semesterComboBox = new JComboBox<>(semesters.toArray(new Semester[0]));
        formPanel.add(semesterComboBox, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Base Tuition Fee:"), gbc);
        gbc.gridx = 1; baseFeeField = new JTextField("15000.00");
        formPanel.add(baseFeeField, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Issue Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; issueDateField = new JTextField();
        formPanel.add(issueDateField, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; dueDateField = new JTextField();
        formPanel.add(dueDateField, gbc); y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        generateButton = new JButton("Generate");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(generateButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        generateButton.addActionListener(e -> onGenerate());
        cancelButton.addActionListener(e -> dispose());
    }

    private void onGenerate() {
        if (getBaseFee() <= 0) {
            JOptionPane.showMessageDialog(this, "Base Fee must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (getIssueDate().isEmpty() || getDueDate().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dates cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        isGenerated = true;
        dispose();
    }

    public boolean isGenerated() { return isGenerated; }
    public Semester getSelectedSemester() { return (Semester) semesterComboBox.getSelectedItem(); }
    public double getBaseFee() {
        try {
            return Double.parseDouble(baseFeeField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public String getIssueDate() { return issueDateField.getText(); }
    public String getDueDate() { return dueDateField.getText(); }
}