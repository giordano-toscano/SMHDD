package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.D;
import smhdd.data.Pattern;

public final class Initialization {

    private Initialization(){
        // Private constructor to prevent instantiation
    }

    public static Pattern[] dimension1(D dataset){
        Pattern[] P0 = new Pattern[dataset.getItemCount()];
        
        for(int i = 0; i < dataset.getItemCount(); i++){
            HashSet<Integer> itens = new HashSet<>();
            itens.add(dataset.getItems()[i]);
            P0[i] = new Pattern(itens);
        }        
        return P0;
    }

}
