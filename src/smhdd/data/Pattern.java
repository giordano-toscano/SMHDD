package smhdd.data;

import java.util.Arrays;
import java.util.HashSet;
import smhdd.evolutionary.Evaluation;

public class Pattern implements Comparable<Pattern> {

    private static int generatedPatternCount;
    private static byte maxSimilarQuantity;

    private HashSet<Integer> items;
    private double quality;
    private boolean[] positiveCoverageArray;
    private boolean[] negativeCoverageArray;
    private Pattern[] similars;

    public Pattern(HashSet<Integer> items){
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

    public HashSet<Integer> getItems() {
        return this.items;
    }

    public void setItems(HashSet<Integer> items) {
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

    // public String display(D dataset){
    //     String result = "Pattern(items={";
    //     for(Integer e : this.items){
    //         result = result + "(a:" + dataset.getItemAttributesInt()[e] + ", v:"+ dataset.getItemValuesInt()[e]+"),";
    //     }
    //     result = result + "}"+", quality="+this.quality+")";
    //     return result;

    // }
    public String display(D dataset) {
        int[] attributeIndexes = dataset.getItemAttributesInt();
        String[] categoricalItemAttribute = dataset.getItemAttributesStr();
        //String[] categoricalItemValue = dataset.getItemValuesObj();
        byte[] attributeTypes = dataset.getAttributeTypes();
        //Capturando e ordenando conteúdo
        Integer[] itemsArray = this.items.toArray(Integer[]::new);    
        Arrays.sort(itemsArray);

        //Salvando em string
        StringBuilder str = new StringBuilder("{");

        int i = 0;
        do { 
            //int attributeIndex = attributeIndexes[itemsArray[i]];    
            //if(attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL)
                //str.append(categoricalItemAttribute[itemsArray[i]] + " = " + dataset.getItemValuesObj[itemsArray[i]]);
            //else
                //str.append(dataset.getItemAttributesInt()[itemsArray[i]] + " = " + dataset.getItemValuesObj[itemsArray[i]]);
            

            if(i < itemsArray.length-1)
                str.append(",");
            i++;
        } while (i < itemsArray.length);
        int[] result = Evaluation.getPositiveAndNegativeCount(this, dataset);
        int falsePositive = result[0];
        int truePositive = result[1];

        str.append("} -> ");
        str.append(this.quality);
        str.append("(");
        str.append(truePositive);
        str.append("p,");
        str.append(falsePositive);
        str.append("n)");  
        str.append("(conf=");
        //str.append(DPinfo.conf(this));
        str.append(")");
        
        return str.toString();
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
