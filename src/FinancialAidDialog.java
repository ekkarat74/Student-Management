import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class FinancialAidDialog extends JDialog {

    private JComboBox<Invoice> invoiceComboBox;
    private JTextField amountField;
    private JComboBox<String> aidTypeComboBox;
    private JTextField descriptionField;
    private JButton saveButton;
    private JButton cancelButton;

    private boolean isSaved = false;

    public FinancialAidDialog(Frame parent, String studentName, ArrayList<Invoice> pendingInvoices) {
        super(parent, "Add Financial Aid for " + studentName, true);
        setSize(450, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int y = 0;
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Apply to Invoice:"), gbc);
        gbc.gridx = 1; invoiceComboBox = new JComboBox<>(pendingInvoices.toArray(new Invoice[0]));
        formPanel.add(invoiceComboBox, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Aid Type:"), gbc);
        gbc.gridx = 1; aidTypeComboBox = new JComboBox<>(new String[]{"SCHOLARSHIP", "DISCOUNT", "WAIVER"});
        formPanel.add(aidTypeComboBox, gbc); y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Amount:"), gbc);
        gbc.gridx = 1; amountField = new JTextField();
        formPanel.add(amountField, gbc); y++;
        
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1; descriptionField = new JTextField();
        formPanel.add(descriptionField, gbc); y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton = new JButton("Save Aid");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> onSave());
        cancelButton.addActionListener(e -> dispose());
    }

    private void onSave() {
        if (getAmount() <= 0) {
            JOptionPane.showMessageDialog(this, "Amount must be a positive number.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (getSelectedInvoice() == null) {
            JOptionPane.showMessageDialog(this, "You must select an invoice to apply the aid to.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        isSaved = true;
        dispose();
    }

    public boolean isSaved() { return isSaved; }
    public Invoice getSelectedInvoice() { return (Invoice) invoiceComboBox.getSelectedItem(); }
    public String getAidType() { return (String) aidTypeComboBox.getSelectedItem(); }
    public String getDescription() { return descriptionField.getText(); }
    public double getAmount() {
        try {
            return Double.parseDouble(amountField.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}