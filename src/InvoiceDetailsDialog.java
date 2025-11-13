import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;

public class InvoiceDetailsDialog extends JDialog {

    private DatabaseManager dbManager;
    private DefaultTableModel itemsModel;
    private JTable itemsTable;
    private DefaultTableModel paymentsModel;
    private JTable paymentsTable;

    public InvoiceDetailsDialog(Frame parent, DatabaseManager dbManager, int invoiceId, String studentName) {
        super(parent, "Details for Invoice #" + invoiceId, true);
        this.dbManager = dbManager;
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = createHeaderPanel(invoiceId, studentName);
        add(headerPanel, BorderLayout.NORTH);

        itemsModel = new DefaultTableModel(new Object[]{"Description", "Amount"}, 0);
        itemsTable = new JTable(itemsModel);

        paymentsModel = new DefaultTableModel(new Object[]{"Date", "Amount Paid", "Method"}, 0);
        paymentsTable = new JTable(paymentsModel);

        JScrollPane itemsPane = new JScrollPane(itemsTable);
        itemsPane.setBorder(BorderFactory.createTitledBorder("Invoice Items"));
        
        JScrollPane paymentsPane = new JScrollPane(paymentsTable);
        paymentsPane.setBorder(BorderFactory.createTitledBorder("Payments Received for this Invoice"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, itemsPane, paymentsPane);
        splitPane.setDividerLocation(200);
        add(splitPane, BorderLayout.CENTER);

        loadInvoiceDetails(invoiceId);
    }

    private JPanel createHeaderPanel(int invoiceId, String studentName) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        Invoice invoice = dbManager.getInvoiceById(invoiceId);

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Student:"));
        gbc.gridx = 1; panel.add(new JLabel("<html><b>" + studentName + "</b></html>"));
        
        gbc.gridx = 0; gbc.gridy = 1; panel.add(new JLabel("Invoice ID:"));
        gbc.gridx = 1; panel.add(new JLabel(String.valueOf(invoiceId)));
        
        gbc.gridx = 0; gbc.gridy = 2; panel.add(new JLabel("Issue Date:"));
        gbc.gridx = 1; panel.add(new JLabel(invoice.issueDate));
        
        gbc.gridx = 2; gbc.gridy = 2; panel.add(new JLabel("Due Date:"));
        gbc.gridx = 3; panel.add(new JLabel(invoice.dueDate));
        
        gbc.gridx = 0; gbc.gridy = 3; panel.add(new JLabel("Total Amount:"));
        gbc.gridx = 1; panel.add(new JLabel(String.format("<html><b>%.2f</b></html>", invoice.totalAmount)));
        
        gbc.gridx = 2; gbc.gridy = 3; panel.add(new JLabel("Status:"));
        gbc.gridx = 3; JLabel statusLabel = new JLabel("<html><b>" + invoice.status + "</b></html>");
        statusLabel.setForeground(invoice.status.equals("PAID") ? new Color(0, 128, 0) : Color.RED);
        panel.add(statusLabel);

        return panel;
    }

    private void loadInvoiceDetails(int invoiceId) {
        itemsModel.setRowCount(0);
        ArrayList<InvoiceItem> items = dbManager.getInvoiceItems(invoiceId);
        for (InvoiceItem item : items) {
            itemsModel.addRow(new Object[]{item.description, item.amount});
        }

        paymentsModel.setRowCount(0);
        ArrayList<Transaction> payments = dbManager.getTransactionsForInvoice(invoiceId);
        for (Transaction tx : payments) {
            paymentsModel.addRow(new Object[]{tx.paymentDate, tx.amountPaid, tx.paymentMethod});
        }
    }
}