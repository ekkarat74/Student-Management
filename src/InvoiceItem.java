public class InvoiceItem {
    int id;
    String description;
    double amount;

    public InvoiceItem(int id, String description, double amount) {
        this.id = id;
        this.description = description;
        this.amount = amount;
    }
}