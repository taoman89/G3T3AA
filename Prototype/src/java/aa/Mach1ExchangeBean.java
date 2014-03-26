package aa;
//
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import javax.naming.NamingException;
 
public class Mach1ExchangeBean implements Serializable {

  // location of log files - change if necessary
  private final String MATCH_LOG_FILE = "c:\\temp\\matched.log";
  private final String REJECTED_BUY_ORDERS_LOG_FILE = "c:\\temp\\rejected.log";

  // used to calculate remaining credit available for buyers
  private final int DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000;

  // used for keeping track of unfulfilled asks and bids in the system.
  // once asks or bids are matched, they must be removed from these arraylists.
  private ArrayList<Ask> unfulfilledAsks = new ArrayList<Ask>();
  private ArrayList<Bid> unfulfilledBids = new ArrayList<Bid>();

  // used to keep track of all matched transactions (asks/bids) in the system
  // matchedTransactions is cleaned once the records are written to the log file successfully
  private ArrayList<MatchedTransaction> matchedTransactions = new ArrayList<MatchedTransaction>();

  // keeps track of the latest price for each of the 3 stocks
  private int latestPriceForSmu = -1;
  private int latestPriceForNus = -1;
  private int latestPriceForNtu = -1;

  // keeps track of the remaining credit limits of each buyer. This should be
  // checked every time a buy order is submitted. Buy orders that breach the
  // credit limit should be rejected and logged
  // The key for this Hashtable is the user ID of the buyer, and the corresponding value is the REMAINING credit limit
  // the remaining credit limit should not go below 0 under any circumstance!
  // --- Credit is now stored in database. ----
  //private Hashtable <String, Integer> creditRemaining = new Hashtable<String, Integer>();


  // this method is called once at the end of each trading day. It can be called manually, or by a timed daemon
  // this is a good chance to "clean up" everything to get ready for the next trading day
  public void endTradingDay() throws Exception{
    // reset attributes
    latestPriceForSmu = -1;
    latestPriceForNus = -1;
    latestPriceForNtu = -1;

    // dump all unfulfilled buy and sell orders
    unfulfilledAsks.clear();
    unfulfilledBids.clear();

    // reset all credit limits of users
    //creditRemaining.clear();
    // Reset the credit in database #SD#.
    DbBean.executeUpdate("delete from credit");
  }

  // returns a String of unfulfilled bids for a particular stock
  // returns an empty string if no such bid
  // bids are separated by <br> for display on HTML page
  public String getUnfulfilledBidsForDisplay(String stock) {
    String returnString = "";
    for (int i = 0; i < unfulfilledBids.size(); i++) {
      Bid bid = unfulfilledBids.get(i);
      if (bid.getStock().equals(stock)) {
        returnString += bid.toString() + "<br />";
      }
    }
    return returnString;
  }

  // returns a String of unfulfilled asks for a particular stock
  // returns an empty string if no such ask
  // asks are separated by <br> for display on HTML page
  
    public boolean sendToBackOffice(String txnDescription){
      aa.Service service = new aa.Service();
      boolean status = false;
      
      try {
        // create new instances of remote Service objects
        aa.ServiceSoap port = service.getServiceSoap();

        // invoke the remote method by calling port.processTransaction().
        // processTransaction() will return false if the teamID &/or password is wrong
        // it will return true if the web service is correctly called
        status = port.processTransaction("GxTy", "password", txnDescription);
        return status;
      }
      catch (Exception ex) {
          // may come here if a time out or any other exception occurs
          // what should you do here??
      }
      return false; // failure due to exception
  }

    
  public String getUnfulfilledAsks(String stock) {
    String returnString = "";
    for (int i = 0; i < unfulfilledAsks.size(); i++) {
      Ask ask = unfulfilledAsks.get(i);
      if (ask.getStock().equals(stock)) {
        returnString += ask.toString() + "<br />";
      }
    }
    return returnString;
  }

  // returns the highest bid for a particular stock
  // returns -1 if there is no bid at all
  public int getHighestBidPrice(String stock) {
    Bid highestBid = getHighestBid(stock);
    if (highestBid == null) {
      return -1;
    } else {
      return highestBid.getPrice();
    }
  }

  // retrieve unfulfiled current (highest) bid for a particular stock
  // returns null if there is no unfulfiled bid for this stock
  private Bid getHighestBid(String stock) {
    Bid highestBid = new Bid(null, 0, null);
    for (int i = 0; i < unfulfilledBids.size(); i++) {
      Bid bid = unfulfilledBids.get(i);
      if (bid.getStock().equals(stock) && bid.getPrice() >= highestBid.getPrice()) {
        // if there are 2 bids of the same amount, the earlier one is considered the highest bid
        if (bid.getPrice() == highestBid.getPrice()) {
          // compare dates
          if (bid.getDate().getTime() < highestBid.getDate().getTime()) {
            highestBid = bid;
          }
        } else {
          highestBid = bid;
        }
      }
    }
    if (highestBid.getUserId() == null) {
      return null; // there's no unfulfilled bid at all!
    }
    return highestBid;
  }

  // returns the lowest ask for a particular stock
  // returns -1 if there is no ask at all
  public int getLowestAskPrice(String stock) {
    Ask lowestAsk = getLowestAsk(stock);
    if (lowestAsk == null) {
      return -1;
    } else {
      return lowestAsk.getPrice();
    }
  }

  // retrieve unfulfiled current (lowest) ask for a particular stock
  // returns null if there is no unfulfiled asks for this stock
  private Ask getLowestAsk(String stock) {
    Ask lowestAsk = new Ask(null, Integer.MAX_VALUE, null);
    for (int i = 0; i < unfulfilledAsks.size(); i++) {
      Ask ask = unfulfilledAsks.get(i);
      if (ask.getStock().equals(stock) && ask.getPrice() <= lowestAsk.getPrice()) {
        // if there are 2 asks of the same ask amount, the earlier one is considered the highest ask
        if (ask.getPrice() == lowestAsk.getPrice()) {
          // compare dates
          if (ask.getDate().getTime() < lowestAsk.getDate().getTime()) {
            lowestAsk = ask;
          }
        } else {
          lowestAsk = ask;
        }
      }
    }
    if (lowestAsk.getUserId() == null) {
      return null; // there's no unfulfilled asks at all!
    }
    return lowestAsk;
  }

  // get credit remaining for a particular buyer
  private int getCreditRemaining(String buyerUserId) throws ClassNotFoundException, SQLException, NamingException{
//    if (!(creditRemaining.containsKey(buyerUserId))){
//      // this buyer is not in the hash table yet. hence create a new entry for him
//      creditRemaining.put(buyerUserId, DAILY_CREDIT_LIMIT_FOR_BUYERS);
//    }
//    return creditRemaining.get(buyerUserId);
    
   //read the credit limit from database #SD#
   ResultSet rs = DbBean.executeSql(String.format("select credit_limit from credit where userid='%s'", buyerUserId));
    
    if (rs.next())
    {
        return rs.getInt("credit_limit");        
    }    
    else
    {
        DbBean.executeUpdate(
                String.format("insert into credit (userid,credit_limit) values('%s',%s)",
                buyerUserId, DAILY_CREDIT_LIMIT_FOR_BUYERS));
        
        return DAILY_CREDIT_LIMIT_FOR_BUYERS;
    }
    
  }

  // check if a buyer is eligible to place an order based on his credit limit
  // if he is eligible, this method adjusts his credit limit and returns true
  // if he is not eligible, this method logs the bid and returns false
  private boolean validateCreditLimit(Bid b) throws ClassNotFoundException, SQLException,NamingException{
    // calculate the total price of this bid
    int totalPriceOfBid = b.getPrice() * 1000; // each bid is for 1000 shares
    int remainingCredit = getCreditRemaining(b.getUserId());
    int newRemainingCredit = remainingCredit - totalPriceOfBid;

    if (newRemainingCredit < 0){
      // no go - log failed bid and return false
      logRejectedBuyOrder(b);
      return false;
    }
    else {
      // it's ok - adjust credit limit and return true
      //creditRemaining.put(b.getUserId(), newRemainingCredit);
      
     //Update the credit limit in the database. #SD#
      DbBean.executeUpdate(String.format("update credit set credit_limit=%s where userid='%s'", newRemainingCredit, b.getUserId()));
      
      return true;
    }
  }

  // call this to append all rejected buy orders to log file
  private void logRejectedBuyOrder(Bid b) {
    try {
      PrintWriter outFile = new PrintWriter(new FileWriter(REJECTED_BUY_ORDERS_LOG_FILE, true));
      outFile.append(b.toString() + "\n");
      outFile.close();
    } catch (IOException e) {
      // Think about what should happen here...
      System.out.println("IO EXCEPTIOn: Cannot write to file");
      e.printStackTrace();
    } catch (Exception e) {
      // Think about what should happen here...
      System.out.println("EXCEPTION: Cannot write to file");
      e.printStackTrace();
    }
  }

  // call this to append all matched transactions in matchedTransactions to log file and clear matchedTransactions
  private void logMatchedTransactions() {
    try {
      PrintWriter outFile = new PrintWriter(new FileWriter(MATCH_LOG_FILE, true));
      for (MatchedTransaction m : matchedTransactions) {
        outFile.append(m.toString() + "\n");
      }
      matchedTransactions.clear(); // clean this out
      outFile.close();
    } catch (IOException e) {
      // Think about what should happen here...
      System.out.println("IO EXCEPTIOn: Cannot write to file");
      e.printStackTrace();
    } catch (Exception e) {
      // Think about what should happen here...
      System.out.println("EXCEPTION: Cannot write to file");
      e.printStackTrace();
    }
  }

  // returns a string of HTML table rows code containing the list of user IDs and their remaining credits
  // this method is used by viewOrders.jsp for debugging purposes
  public String getAllCreditRemainingForDisplay() throws Exception{
    String returnString = "";

//    Enumeration items = creditRemaining.keys();
//
//    while (items.hasMoreElements()){
//      String key = (String)items.nextElement();
//      int value = creditRemaining.get(key);
//      returnString += "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
//    }
    
    //get the credit limit for all users from database. #SD#
    ResultSet rs = DbBean.executeSql("select * from credit");
    while(rs.next())
    {
        returnString += "<tr><td>" + rs.getString("userid") + "</td><td>" + rs.getInt("credit_limit") + "</td></tr>";        
    }
    return returnString;
  }

  // call this method immediatley when a new bid (buying order) comes in
  // this method returns false if this buy order has been rejected because of a credit limit breach
  // it returns true if the bid has been successfully added
  public boolean placeNewBidAndAttemptMatch(Bid newBid) throws Exception{
    // step 0: check if this bid is valid based on the buyer's credit limit
    boolean okToContinue = validateCreditLimit(newBid);
    if (!okToContinue){
      return false; 
    }
    
    String stockName = newBid.getStock();
    // try to match

    Ask bestAsk = null;
    int bestAskPosition = -1;
    for(int i = 0; i < unfulfilledAsks.size(); i ++){
        Ask currentAsk = unfulfilledAsks.get(i);
        if(stockName.equals(currentAsk.getStock())){
            bestAsk = currentAsk;
            bestAskPosition = i;
            break;
        }
    }

    // A match happens if the highest bid is bigger or equal to the lowest ask
    if (bestAskPosition!= -1 && newBid.getPrice() >= bestAsk.getPrice()) {
        MatchedTransaction match = new MatchedTransaction(newBid, bestAsk, newBid.getDate(), bestAsk.getPrice());
        //remove best ask 
        unfulfilledAsks.remove(bestAskPosition);
        matchedTransactions.add(match);

        // to be included here: inform Back Office Server of match
        // to be done in v1.0

        updateLatestPrice(match);
        logMatchedTransactions();
    }

    //if cant match, add bid to unfulfilled bids
    else{
        unfulfilledBids = MatchingAlgoUtil.addNewBid(unfulfilledBids, newBid);
    }

    return true; // this bid is acknowledged
  }

  // call this method immediatley when a new ask (selling order) comes in
  public void placeNewAskAndAttemptMatch(Ask newAsk) {
    String stockName = newAsk.getStock();
    // try to match
    Bid bestBid = null;
    int bestBidPosition = -1;
    for(int i = unfulfilledBids.size()-1; i >=0 ; i --){
        Bid currentBid = unfulfilledBids.get(i);
        if(stockName.equals(currentBid.getStock())){
            bestBid = currentBid;
            bestBidPosition=i;
            break;
        }
    }
    
    if (bestBidPosition!= -1 && bestBid.getPrice() >= newAsk.getPrice()) {
        MatchedTransaction match = new MatchedTransaction(bestBid, newAsk, newAsk.getDate(), bestBid.getPrice());
        //remove best ask 
        unfulfilledBids.remove(bestBidPosition);
        matchedTransactions.add(match);

        // to be included here: inform Back Office Server of match
        // to be done in v1.0

        updateLatestPrice(match);
        logMatchedTransactions();
    }

    //if cant match, add bid to unfulfilled asks
    else{
        unfulfilledAsks = MatchingAlgoUtil.addNewAsk(unfulfilledAsks, newAsk);
    }

  }

  // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
  // based on the MatchedTransaction object passed in
  private void updateLatestPrice(MatchedTransaction m) {
    String stock = m.getStock();
    int price = m.getPrice();
    // update the correct attribute
    if (stock.equals("smu")) {
      latestPriceForSmu = price;
    } else if (stock.equals("nus")) {
      latestPriceForNus = price;
    } else if (stock.equals("ntu")) {
      latestPriceForNtu = price;
    }
  }

  // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
  // based on the MatchedTransaction object passed in
  public int getLatestPrice(String stock) {
    if (stock.equals("smu")) {
      return latestPriceForSmu;
    } else if (stock.equals("nus")) {
      return latestPriceForNus;
    } else if (stock.equals("ntu")) {
      return latestPriceForNtu;
    }
    return -1; // no such stock
  }
  
  
}
