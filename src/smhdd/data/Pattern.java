package smhdd.data;

import java.util.HashSet;

public class Pattern {
    private static int generatedPatternCount = 0;
    private static byte maxSimilarQuantity;

    private HashSet<Integer> items;
    private int quality;
    private Pattern[] similars;

    public Pattern(HashSet<Integer> items){
        this.items = items;
        generatedPatternCount++;
    }


    public int getGeneratedPatternCount(){
        return this.generatedPatternCount;
    }

}
