package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Index;
import smhdd.data.Interval;
import smhdd.data.Item;
import smhdd.data.Pattern;

public final class Initialization {

    private Initialization(){
        // Private constructor to prevent instantiation
    }

    public static Pattern[] dimension1(D dataset){
        Pattern[] P0 = new Pattern[dataset.getItemCount()];
        
        for(int i = 0; i < dataset.getItemCount(); i++){
            HashSet<Item> items = new HashSet<>();
            int attributeIndex = dataset.getItemAttributesInt()[i];
            Item item;
            if(D.getVariableTypes()[attributeIndex] == Const.TYPE_CATEGORICAL){
                int valueIndex = dataset.getItemValuesInt()[i];
                item = new Index(attributeIndex, valueIndex);
            }else{
                double[] interval = (double[]) dataset.getItemValuesObj()[i];
                item = new Interval(attributeIndex, interval[0], interval[1]);
            }
            items.add(item);

            P0[i] = new Pattern(items);
        }        
        return P0;
    }

}
