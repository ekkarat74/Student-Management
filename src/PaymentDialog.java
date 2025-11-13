import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PaymentDialog extends JDialog {

    private JComboBox<Invoice> invoiceComboBox;
    private JTextField amountField;
    private JComboBox<String> methodComboBox;
    private JTextField referenceField;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean isSaved = false;

    public PaymentDialog(Frame parent, String studentName, double balance, ArrayList<Invoice> pendingInvoices) {
        super(parent, "Add Payment for " + studentName, true);
        setSize(450, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Student:"), gbc);
        gbc.gridx = 1; formPanel.add(new JLabel(studentName), gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Current Balance:"), gbc);
        gbc.gridx = 1; JLabel balanceLabel = new JLabel(String.format("%.2f", balance));
        balanceLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        balanceLabel.setForeground(balance > 0 ? Color.RED : new Color(0, 128, 0));
        formPanel.add(balanceLabel, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Select Invoice:"), gbc);
        gbc.gridx = 1; invoiceComboBox = new JComboBox<>(pendingInvoices.toArray(new Invoice[0]));
        formPanel.add(invoiceComboBox, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Payment Amount:"), gbc);
        gbc.gridx = 1; amountField = new JTextField();
        formPanel.add(amountField, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1; methodComboBox = new JComboBox<>(new String[]{"COUNTER", "ONLINE", "TRANSFER"});
        formPanel.add(methodComboBox, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Reference Code:"), gbc);
        gbc.gridx = 1; referenceField = new JTextField();
        formPanel.add(referenceField, gbc); y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton = new JButton("Save Payment");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
    }

    private void onSave() {
        if (getAmountPaid() <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (getSelectedInvoice() == null) {
            JOptionPane.showMessageDialog(this, "You must select an invoice.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        isSaved = true;
        dispose();
    }

    public boolean isSaved() { return isSaved; }
    public Invoice getSelectedInvoice() { return (Invoice) invoiceComboBox.getSelectedItem(); }
    public double getAmountPaid() {
        try {
            return Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public String getPaymentMethod() { return (String) methodComboBox.getSelectedItem(); }
    public String getReferenceCode() { return referenceField.getText(); }
}