/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aa;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Juntao
 */
public class MatchingAlgoUtil implements Serializable {
    public static ArrayList<Ask> addNewAsk(ArrayList<Ask> unfulfilledAsks, Ask ask){
        //adding new ask using binary search (for a sorted list)
        //ArrayList<Ask> returnedList = unfulfilledAsks;
        
        int listSize = unfulfilledAsks.size();
        
        
        for(int i = 0; i < unfulfilledAsks.size(); i ++){
            
            int currentAskPrice = unfulfilledAsks.get(i).getPrice();
            if(currentAskPrice> ask.getPrice()){
                unfulfilledAsks.add(i, ask);
                
                return unfulfilledAsks;
            }
        }
        
        
        unfulfilledAsks.add(ask);
        return unfulfilledAsks;
    
    }
    
    public static ArrayList<Bid> addNewBid(ArrayList<Bid> unfulfilledBids, Bid bid){
        //adding new ask using binary search (for a sorted list)
        //ArrayList<Ask> returnedList = unfulfilledAsks;
        
        int listSize = unfulfilledBids.size();
        
        
        for(int i = unfulfilledBids.size()-1; i >=0 ; i --){
            
            int currentAskPrice = unfulfilledBids.get(i).getPrice();
            if(currentAskPrice < bid.getPrice()){
                unfulfilledBids.add(i+1, bid);
                
                return unfulfilledBids;
            }
        }
        
        
        unfulfilledBids.add(0,bid);
        return unfulfilledBids;
    
    }
    
    public static MatchedTransaction matchBid(Bid bid, ArrayList<Ask> unfulfilledAsks){
        String stock = bid.getStock();
        Ask bestAsk = null;
        for(int i = 0; i < unfulfilledAsks.size(); i ++){
            Ask currentAsk = unfulfilledAsks.get(i);
            if(stock.equals(currentAsk.getStock())){
                bestAsk = currentAsk;
                break;
            }
        }
        
        if(bestAsk == null){
            return null;
        }
        // A match happens if the highest bid is bigger or equal to the lowest ask
        else if (bid.getPrice() >= bestAsk.getPrice()) {
            MatchedTransaction match = new MatchedTransaction(bid, bestAsk, bid.getDate(), bestAsk.getPrice());
            return match;
        }
        return null;
    }
    
    public static MatchedTransaction matchAsk(Ask ask, ArrayList<Bid> unfulfilledBids){
        String stock = ask.getStock();
        Bid bestBid = null;
        for(int i = unfulfilledBids.size()-1; i >=0 ; i --){
            Bid currentBid = unfulfilledBids.get(i);
            if(stock.equals(currentBid.getStock())){
                bestBid = currentBid;
                break;
            }
        }
        
        if(bestBid == null){
            return null;
        }
        // A match happens if the highest bid is bigger or equal to the lowest ask
        else if (bestBid.getPrice() >= ask.getPrice()) {
            MatchedTransaction match = new MatchedTransaction(bestBid, ask, ask.getDate(), bestBid.getPrice());
            return match;
        }
        return null;
    }
    
    public static ArrayList<Ask> removeBestAsk(ArrayList<Ask> unfulfilledAsks, String stockName){

        for(int i = 0; i < unfulfilledAsks.size(); i ++){
            Ask currentAsk = unfulfilledAsks.get(i);
            if(stockName.equals(currentAsk.getStock())){
                unfulfilledAsks.remove(i);
                return unfulfilledAsks;
            }
        }
        return unfulfilledAsks;
    } 
    public static ArrayList<Bid> removeBestBid(ArrayList<Bid> unfulfilledBids,String stockName){

        for(int i = unfulfilledBids.size()-1; i >=0 ; i --){
            Bid currentBid = unfulfilledBids.get(i);
            if(stockName.equals(currentBid.getStock())){
                unfulfilledBids.remove(i);
                return unfulfilledBids;
            }
        }
        return unfulfilledBids;
    } 
    
}
