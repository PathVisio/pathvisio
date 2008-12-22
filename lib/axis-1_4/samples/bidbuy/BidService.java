package samples.bidbuy;

/**
 * Big/PurchaseOrder Service
 */
public class BidService {

    static int nextReceiptNumber = 9000;

    /**
     * Request a quote for a given quantity of a specified product
     * @param productName name of product
     * @param quantity number desired
     * @return Total amount in US$ for complete purchase
     */
    public double RequestForQuote(String productName, int quantity) {
        if (quantity < 100) {
           return 1.0 * quantity;
        } if (quantity < 1000) {
           return 0.8 * quantity;
        } else {
           return 0.7 * quantity;
        }
 
    }

    /**
     * Purchase a given quantity of a specified product
     * @param productName name of product
     * @param quantity number desired
     * @param price desired price (!!!)
     * @param customerId who you are
     * @param shipTo where you want the goods to go
     * @param date where you want the goods to go
     * @return Receipt
     */
    public String SimpleBuy(String productName, String address, int quantity) {
        return Integer.toString(nextReceiptNumber++) + "\n" +
            quantity + " " + productName;
    }

    /**
     * Process a purchase order.
     * @return Receipt
     */
    public String Buy(PurchaseOrder PO) {
        String receipt = Integer.toString(nextReceiptNumber++);

        for (int i=0; i<PO.getItems().length; i++) {
            LineItem item = PO.getItems()[i];
            receipt += "\n  " + item.getQuantity() + " " + item.getName();
        }

        return receipt;
    }

    /**
     * Let the world know that we are still alive...
     */
    public void Ping() {
    }

}
