/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aa;

import java.util.ArrayList;

/**
 *
 * @author Juntao
 */
public class MatchingAlgoUtil {
    public static ArrayList<Ask> addNewAsk(ArrayList<Ask> unfulfilledAsks, Ask ask){
        //adding new ask using binary search (for a sorted list)
        //ArrayList<Ask> returnedList = unfulfilledAsks;
        
        int listSize = unfulfilledAsks.size();
        
        if(listSize == 0){
            unfulfilledAsks.add(ask);
            return unfulfilledAsks;
        }else if(listSize == 1){
            Ask comparedAsk = unfulfilledAsks.get(0);
            if(comparedAsk.getPrice()>ask.getPrice()){
                unfulfilledAsks.add(0,ask);
            }else{
                unfulfilledAsks.add(ask);
            }
            return unfulfilledAsks;
        }
        
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
    
    
}
