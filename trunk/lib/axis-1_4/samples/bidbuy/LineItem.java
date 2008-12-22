package samples.bidbuy;

import java.math.BigDecimal;

public class LineItem {

    // constructors

    public LineItem() {};

    public LineItem(String name, int quantity, BigDecimal price) {
         this.name=name;
         this.quantity=quantity;
         this.price=price;
    }

    public LineItem(String name, int quantity, String price) {
         this.name=name;
         this.quantity=quantity;
         this.price=new BigDecimal(price);
    }

    // properties

    private String name;
    public String getName() { return name; }
    public void setName(String value) { name=value; }

    private int quantity;
    public int getQuantity() { return quantity; }
    public void setQuantity(int value) { quantity=value; }

    private BigDecimal price;
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal value) { price=value; }

}
