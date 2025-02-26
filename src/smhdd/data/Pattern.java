package smhdd.data;

import java.util.Arrays;
import java.util.HashSet;

public class Pattern implements Comparable<Pattern> {

    private static int generatedPatternCount;
    private static byte maxSimilarQuantity;

    private HashSet<Item> items;
    private double quality;
    private boolean[] positiveCoverageArray;
    private boolean[] negativeCoverageArray;
    private Pattern[] similars;

    public Pattern(HashSet<Item> items){
        this.items = items;
        Pattern.generatedPatternCount++;
    }

    // GETs and SETs
    
    public static int getGeneratedPatternCount(){
        return Pattern.generatedPatternCount;
    }

    public static void setMaxSimilarQuantity(byte quantity){
        Pattern.maxSimilarQuantity = Pattern.maxSimilarQuantity == 0 ? quantity : Pattern.maxSimilarQuantity;
    }

    public HashSet<Item> getItems() {
        return this.items;
    }

    public void setItems(HashSet<Item> items) {
        this.items = items;
    }

    public double getQuality() {
        return this.quality;
    }

    public void setQuality(double quality) {
        this.quality = quality;
    }

    public Pattern[] getSimilars() {
        return similars;
    }

    public void setSimilars(Pattern[] similars) {
        this.similars = similars;
    }

    // public int getTP() {
    //     return this.tp;
    // }

    // public void setTP(int tp) {
    //     this.tp = tp;
    // }
    
    // public int getFP() {
    //     return this.fp;
    // }

    // public void setFP(int fp) {
    //     this.fp = fp;
    // }

    public boolean[] getPositiveCoverageArray(){
        return this.positiveCoverageArray;
    }

    public void setPositiveCoverageArray(boolean[] array){
        this.positiveCoverageArray = array;
    }

    public boolean[] getNegativeCoverageArray(){
        return this.negativeCoverageArray;
    }

    public void setNegativeCoverageArray(boolean[] array){
        this.negativeCoverageArray = array;
    }

    // Utils

    public boolean addSimilar(Pattern similar){
        if(this.similars == null){    // if its the first similar
            this.similars = new Pattern[Pattern.maxSimilarQuantity];
            this.similars[0] = new Pattern(similar.getItems()); 
            this.similars[0].setQuality(similar.getQuality());
            this.similars[0].setNegativeCoverageArray(similar.getNegativeCoverageArray());
            this.similars[0].setPositiveCoverageArray(similar.getPositiveCoverageArray());
            //Preencher demais com vazio
            for(int i = 1; i < Pattern.maxSimilarQuantity; i++){
                this.similars[i] = new Pattern(new HashSet<>()); 
            }
            return true; //the pattern has been added
        }else{                       // if its not the first similar 
            if(similar.getQuality() > this.similars[Pattern.maxSimilarQuantity-1].getQuality()){
                if(!this.findInSimilars(similar)){
                    this.similars[Pattern.maxSimilarQuantity-1] = new Pattern(similar.getItems());
                    this.similars[Pattern.maxSimilarQuantity-1].setQuality(similar.getQuality());
                    this.similars[Pattern.maxSimilarQuantity-1].setNegativeCoverageArray(similar.getNegativeCoverageArray());
                    this.similars[Pattern.maxSimilarQuantity-1].setPositiveCoverageArray(similar.getPositiveCoverageArray());
                    Arrays.sort(this.similars);
                    return true; // the pattern has been added
                }
            }
        }
        return false; // pattern has not been added
    }

    public boolean findInSimilars(Pattern p){
        for(int i = 0; i < Pattern.maxSimilarQuantity; i++){
            if(p.isEqualTo(this.similars[i])){
                return true;
            }
        }
        return false;        
    }

    @Override
    public String toString(){
        String result = "Pattern(items={";
        for(Item e : this.items){
            result = result + e + ",";
        }
        result = result + "}"+", quality="+this.quality+")";
        return result;

    }

    // Comparations
    @Override
    public int compareTo(Pattern p) {
        double pQuality = p.getQuality(); 
        if(this.quality < pQuality){
            return 1;
        }else if(this.quality > pQuality){
            return -1;
        }else{
            return 0;
        }   
    }

    public boolean isEqualTo(Pattern p){
        return (p.getItems().containsAll(this.items) && p.getItems().size() == this.items.size());        
    }

}
