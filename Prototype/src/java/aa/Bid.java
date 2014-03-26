package aa;

import java.io.Serializable;
import java.util.Date;

// represents a bid (in a buy order)
public class Bid implements Serializable, Comparable<Bid>{

  private String stock;
  private int price; // bid price
  private String userId; // user who made this buy order
  private Date date;

  // constructor
  public Bid(String stock, int price, String userId) {
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

    public int compareTo(Bid other) {
        
        if(this.price != other.getPrice()){
            return this.price - other.getPrice();
        } else if (this.price == other.getPrice()){
            return other.getDate().compareTo(this.date);
        }
        return 0;
    }
}
