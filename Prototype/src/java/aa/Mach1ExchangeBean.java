package aa;
//
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import javax.naming.NamingException;
 
public class Mach1ExchangeBean implements Serializable {

  // location of log files - change if necessary
  private final String MATCH_LOG_FILE = "c:\\matched.log";
  private final String REJECTED_BUY_ORDERS_LOG_FILE = "c:\rejected.log";
  private final String UNSENT_MATCH_LOG_FILE = "c:\\unsent_matched.log";

  // used to calculate remaining credit available for buyers
  private final int DAILY_CREDIT_LIMIT_FOR_BUYERS = 1000000;
  
  // timeout value
  private final int TIME_OUT = 3;

  // used for keeping track of unfulfilled asks and bids in the system.
  // once asks or bids are matched, they must be removed from these arraylists.
  private ArrayList<Ask> unfulfilledAsks = new ArrayList<Ask>();
  private ArrayList<Bid> unfulfilledBids = new ArrayList<Bid>();

  // used to keep track of all matched transactions (asks/bids) in the system
  // matchedTransactions is cleaned once the records are written to the log file successfully
  private ArrayList<MatchedTransaction> matchedTransactions = new ArrayList<MatchedTransaction>();
  private ArrayList<MatchedTransaction> unsentTransactions = new ArrayList<MatchedTransaction>();

  // keeps track of the latest price for each of the 3 stocks
  private int latestPriceForSmu = -1;
  private int latestPriceForNus = -1;
  private int latestPriceForNtu = -1;
  
  // Declare Fair Policy Lock
  private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);

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
    WriteLock writeLock = lock.writeLock();
    
    try {
        writeLock.lock();
        

        // reset attributes
        latestPriceForSmu = -1;
        latestPriceForNus = -1;
        latestPriceForNtu = -1;

        // dump all unfulfilled buy and sell orders
        unfulfilledAsks.clear();
        unfulfilledBids.clear();
        
        // Send unsent transactions again
        resendTransactions();

        if (!unsentTransactions.isEmpty()) {
            logUnsentMatchedTransactions();
        }

        // reset all credit limits of users
        //creditRemaining.clear();
        // Reset the credit in database #SD#.
        DbBean.executeUpdate("delete from credit");
    } finally {
        writeLock.unlock();
    }
  }
  
  // returns a String of unfulfilled bids for a particular stock
  // returns an empty string if no such bid
  // bids are separated by <br> for display on HTML page
  public String getUnfulfilledBidsForDisplay(String stock) {
    ReadLock readLock = lock.readLock();
    readLock.lock();
    
    String returnString = "";
    for (int i = 0; i < unfulfilledBids.size(); i++) {
      Bid bid = unfulfilledBids.get(i);
      if (bid.getStock().equals(stock)) {
        returnString += bid.toString() + "<br />";
      }
    }
    readLock.unlock();
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
        status = port.processTransaction("G3T3", "garlic", txnDescription);
        return status;
      }
      catch (Exception ex) {
          // may come here if a time out or any other exception occurs
          // what should you do here??
      }
      return false; // failure due to exception
  }

    
  public String getUnfulfilledAsks(String stock) {
    ReadLock readLock = lock.readLock();
    readLock.lock();
    
    String returnString = "";
    for (int i = 0; i < unfulfilledAsks.size(); i++) {
      Ask ask = unfulfilledAsks.get(i);
      if (ask.getStock().equals(stock)) {
        returnString += ask.toString() + "<br />";
      }
    }
    readLock.unlock();
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
    ReadLock readLock = lock.readLock();
    readLock.lock();
    
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
    readLock.unlock();
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
    ReadLock readLock = lock.readLock();
    readLock.lock();
    
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
    readLock.unlock();
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
    WriteLock writeLock = lock.writeLock();
    writeLock.lock();
    
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
    } finally {
        writeLock.unlock();
    }
  }
  
  // Log to file for unsent transactions at end of day
  private void logUnsentMatchedTransactions() {
    WriteLock writeLock = lock.writeLock();
    writeLock.lock();
      
    try {
      PrintWriter outFile = new PrintWriter(new FileWriter(UNSENT_MATCH_LOG_FILE, false));
      for (MatchedTransaction m : unsentTransactions) {
        outFile.append(m.toString() + "\n");
      }
      unsentTransactions.clear(); // clean this out
      outFile.close();
    } catch (IOException e) {
      // Think about what should happen here...
      System.out.println("IO EXCEPTIOn: Cannot write to file");
      e.printStackTrace();
    } catch (Exception e) {
      // Think about what should happen here...
      System.out.println("EXCEPTION: Cannot write to file");
      e.printStackTrace();
    } finally {
        writeLock.unlock();
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
    
    WriteLock writeLock = lock.writeLock();
    
    writeLock.lock();
    
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
        
        // Try sending this current matched transaction
        boolean status = false;

        try {
            status = sendTransaction(match);
        } catch (Exception e) {

        }

        if (!status) {
            unsentTransactions.add(match);
        }

        updateLatestPrice(match);
        logMatchedTransactions();
    }

    //if cant match, add bid to unfulfilled bids
    else{
        unfulfilledBids = MatchingAlgoUtil.addNewBid(unfulfilledBids, newBid);
    }
    writeLock.unlock();
    return true; // this bid is acknowledged
  }
  
  // Send all accumulated unsent transactions
  private void resendTransactions() {
      WriteLock writeLock = lock.writeLock();
      writeLock.lock();
      
      ArrayList<MatchedTransaction> unsentAgain = new ArrayList<MatchedTransaction>();
      
      // Loop through all unsent transactions
      for (int i = 0; i < unsentTransactions.size(); i++) {
          MatchedTransaction m = unsentTransactions.get(i);
          
          boolean status = false;
          
          // Try sending again
          try {
              status = sendTransaction(m);
          } catch (Exception e) {
              
          }
          
          if (!status) {
              unsentAgain.add(m);
          }
      }
      
      // Overwrite existing unsent transactions with any unsent transactions
      unsentTransactions = unsentAgain;
      
      writeLock.unlock();
  }
  
  private boolean sendTransaction(final MatchedTransaction match) throws IOException {
      ExecutorService service = Executors.newSingleThreadExecutor();
      
      try {
          Runnable r = new Runnable() {
              @Override
              public void run() {
                  sendToBackOffice("stock: " + match.getStock()
                    + ", amt: " + match.getPrice() + ", bidder userId: " + match.getBuyerId()
                    + ", seller userId: " + match.getSellerId() + ", date: " + match.getDate());
              }
          };
          
          Future<?> f = service.submit(r);
          
          f.get(TIME_OUT, TimeUnit.SECONDS);  // attempt the task for three seconds
          
      } catch (InterruptedException e) {
          return false;
      } catch (TimeoutException e) {
          return false;
      } catch (ExecutionException e) {
          return false;
      }
      
      return true;
  }

  // call this method immediatley when a new ask (selling order) comes in
  public void placeNewAskAndAttemptMatch(Ask newAsk) {
    WriteLock writeLock = lock.writeLock();

    writeLock.lock();
    
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
        
        // Try sending this current matched transaction
        boolean status = false;

        try {
            status = sendTransaction(match);
        } catch (Exception e) {

        }

        if (!status) {
            unsentTransactions.add(match);
        }

        updateLatestPrice(match);
        logMatchedTransactions();
    }

    //if cant match, add bid to unfulfilled asks
    else{
        unfulfilledAsks = MatchingAlgoUtil.addNewAsk(unfulfilledAsks, newAsk);
    }

    writeLock.unlock();
  }

  // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
  // based on the MatchedTransaction object passed in
  private void updateLatestPrice(MatchedTransaction m) {
    WriteLock writeLock = lock.writeLock();
    writeLock.lock();
    
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
    writeLock.unlock();
  }

  // updates either latestPriceForSmu, latestPriceForNus or latestPriceForNtu
  // based on the MatchedTransaction object passed in
  public int getLatestPrice(String stock) {
    ReadLock readLock = lock.readLock();
    readLock.lock();
    
    if (stock.equals("smu")) {
      return latestPriceForSmu;
    } else if (stock.equals("nus")) {
      return latestPriceForNus;
    } else if (stock.equals("ntu")) {
      return latestPriceForNtu;
    }
    readLock.unlock();
    return -1; // no such stock
  }
  
  
}
