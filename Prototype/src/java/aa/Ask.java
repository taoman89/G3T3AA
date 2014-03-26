package aa;

import java.io.Serializable;
import java.util.Date;

// represents an Ask (in a sell order)
public class Ask  implements Serializable, Comparable<Ask>{

  private String stock;
  private int price; // ask price
  private String userId; // user who made this sell order
  private Date date;

  // constructor
  public Ask(String stock, int price, String userId) {
    this.stock = stock;
    this.price = price;
    this.userId = userId;
    this.date = new Date();
  }

  // getters
  public String getStock() {
    return stock;
  }

  public int getPrice() {
    return price;
  }

  public String getUserId() {
    return userId;
  }

  public Date getDate() {
    return date;
  }

  // toString
  public String toString() {
    return "stock: " + stock + ", price: " + price + ", userId: " + userId + ", date: " + date;
  }

   public int compareTo(Ask other) {
        
        if(this.price != other.getPrice()){
            return this.price - other.getPrice();
        } else if (this.price == other.getPrice()){
            return this.date.compareTo(other.getDate());
        }
        return 0;
    }

}