package smhdd.evolutionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Example;
import smhdd.data.Item;
import smhdd.data.Pattern;

public final class Evaluation {

    private static String evaluationMetric;
    private static byte similarityMeasure;
    private static float minSimilarity;
    
    private Evaluation (){
        // Private constructor to prevent instantiation
    }
    public static void evaluatePopulation(Pattern[] population, D dataset){
        int populationSize = population.length;
        IntStream.range(0, populationSize).parallel().forEach(i -> Evaluation.evaluatePattern(population[i], dataset));
        
    }
    
    private static void evaluatePattern(Pattern p, D dataset){
        int[] result = Evaluation.getPositiveAndNegativeCount(p, dataset);
        int fp = result[0];
        int tp = result[1];
        p.setQuality(Evaluation.calculateQuality(fp, tp, dataset));
    }

    private static int[] getPositiveAndNegativeCount(Pattern p, D dataset){
        HashSet<Item> items = p.getItems();
        int exampleCount = dataset.getExampleCount();
        Example[] examples = dataset.getExampleLists();

        int positiveCount = 0;
        int negativeCount = 0;

        for(int i = 0; i < exampleCount; i++){    
            Example example = examples[i];        
            boolean isCovered = Evaluation.patternContemplaExemploAND(items, example);  
            if(isCovered && example.getLabel() == true)
                positiveCount++;
            else if(isCovered && example.getLabel() == false)
                negativeCount++;    
        }      
        return new int[]{negativeCount, positiveCount};
    }

    private static boolean[][] getPositiveAndNegativeCoverageArrays(Pattern p, D dataset){
        HashSet<Item> items = p.getItems();
        int exampleCount = dataset.getExampleCount();
        Example[] examples = dataset.getExampleLists();
        
        boolean[] positiveCoverageArray = new boolean[exampleCount];
        boolean[] negativeCoverageArray = new boolean[exampleCount];

        for(int i = 0; i < exampleCount; i++){    
            Example example = examples[i];        
            boolean isCovered = Evaluation.patternContemplaExemploAND(items, example);  
            if(isCovered && example.getLabel() == true)
                positiveCoverageArray[i] = true;
            else if(isCovered && example.getLabel() == false)
                negativeCoverageArray[i] = true;
        }      
        return new boolean[][]{negativeCoverageArray, positiveCoverageArray};
    }

    private static boolean patternContemplaExemploAND(HashSet<Item> items, Example example){
        //System.out.println("### Novo Exemplo ###");
        for(Item item : items){
            int attributeIndex = item.getAttributeIndex();
            double exampleAttributeValue = example.get(attributeIndex);
            if(item.contains(exampleAttributeValue) == false)
                return false;
        }       
        return true; 
    }

    private static int getEndIndex(Pattern[] topK, Pattern[] pAsterisk, D dataset){
        int endIndex = pAsterisk.length;
        for( int i = 0; i < pAsterisk.length; i++){
            if(pAsterisk[i].getQuality() <= topK[topK.length-1].getQuality()){
                endIndex = i;
                break;
            }
        }
        return endIndex;
    }
    public static void setCoverageArraysInPattern(Pattern p, D dataset){
        boolean[][] result = Evaluation.getPositiveAndNegativeCoverageArrays(p, dataset);
        p.setNegativeCoverageArray(result[0]);
        p.setPositiveCoverageArray(result[1]);
    }

    public static void setPositiveAndNegativeCoverageArrays(Pattern[] topK, Pattern[] pAsterisk, D dataset){
        int endIndex = getEndIndex(topK, pAsterisk, dataset);
        IntStream.range(0, endIndex).parallel().forEach(i -> Evaluation.setCoverageArraysInPattern(pAsterisk[i], dataset));
        
        List<Pattern> topkAndSimilars = new ArrayList<>(Arrays.asList(topK));
        for(Pattern pattern : topK){
            Pattern[] similarsArray = pattern.getSimilars();
            if(similarsArray != null){
                List<Pattern> similars = Arrays.asList(pattern.getSimilars());
                topkAndSimilars.addAll(similars);
            }
        }
        Pattern[] topkAndSimilarsArray = topkAndSimilars.toArray(Pattern[]::new);
        IntStream.range(0, topkAndSimilarsArray.length).parallel().forEach(i -> Evaluation.setCoverageArraysInPattern(topkAndSimilarsArray[i], dataset));
    }

    private static double calculateQuality(int fp, int tp, D dataset){
        double quality = 0.0;

        switch(Evaluation.evaluationMetric){
            case Const.METRIC_QG -> quality = Evaluation.calculateQg(tp, fp);
            case Const.METRIC_WRACC -> quality = Evaluation.calculateWRAcc(tp, fp, dataset);
            case Const.METRIC_WRACC_NORMALIZED -> quality = Evaluation.calculateWRAccN(tp, fp, dataset);
            //case Const.METRIC_WRACC_OVER_SIZE -> quality = Evaluation.calculateWRAcc(tp, fp, dataset) / p.getItems().size();
            case Const.METRIC_SUB -> quality = Evaluation.calculateSub(tp, fp);
            // case Evaluation.METRICA_AVALIACAO_CHI_QUAD:
            //     quality = Evaluation.chi_quad(tp, fp);
            //     break;
            // case Evaluation.METRICA_AVALIACAO_CHI_QUAD:
            //     quality = Evaluation.chi_quad(tp, fp);
            //     break;
        }
        return quality;
    }   

    private static double calculateWRAcc(int TP, int FP, D dataset){
        int globalExampleCount = dataset.getExampleCount();
        int globalPositiveExampleCount = dataset.getPositiveExampleCount();

        if(TP==0 && FP==0){
            return 0.0;
        }
        double sup = (double)(TP+FP) / (double)globalExampleCount;
        double conf = (double)TP / (double)(TP+FP);
        double confD = (double)globalPositiveExampleCount / (double)globalExampleCount;
        double wracc = sup * ( conf  - confD);
                       
        return wracc;
    }
    
    private static double calculateWRAccN(int TP, int FP, D dataset){
        int globalExampleCount = dataset.getExampleCount();
        int globalPositiveExampleCount = dataset.getPositiveExampleCount();

        if(TP==0 && FP==0){
            return 0.0;
        }
        double sup = (double)(TP+FP) / (double)globalExampleCount;
        double conf = (double)TP / (double)(TP+FP);
        double confD = (double)globalPositiveExampleCount / (double)globalExampleCount;
        double wracc = sup * ( conf  - confD);
                       
        return 4 * wracc;
    }
    
    private static double calculateQg(int TP, int FP){
        double qg = (double)TP/(double)(FP+1);
        return qg;
    }
    
    private static double calculateSub(int TP, int FP){
        double sub = TP-FP;
        return sub;
    }

    public static double calculateSimilarity(Pattern p1, Pattern p2, D dataset){
        //REF: A Survey of Binary Similarity and Distance Measures (http://www.iiisci.org/Journal/CV$/sci/pdfs/GS315JG.pdf)
        double onlyA = 0.0;
        double onlyB = 0.0;
        double bothAB = 0.0;
        double neitherAB = 0.0;
        double Acount = 0.0;
        double Bcount = 0.0;

        boolean[] a, b;
  
        //POSITIVO
        a = p1.getPositiveCoverageArray();
        b = p2.getPositiveCoverageArray();
        for(int i = 0; i < a.length; i++){
            if(a[i])
                Acount++;
            if(b[i])
                Bcount++;
            if(a[i] && !b[i])
                onlyA++;
            if(b[i] && !a[i])
                onlyB++;
            if(a[i] && b[i])
                bothAB++;
            if(!a[i] && !b[i])
                neitherAB++;                        
        }
        
        //NEGATIVO
        a = p1.getNegativeCoverageArray();
        b = p2.getNegativeCoverageArray();
        for(int i = 0; i < a.length; i++){
            if(a[i])
                Acount++;
            if(b[i])
                Bcount++;
            if(a[i] && !b[i])
                onlyA++;
            if(b[i] && !a[i])
                onlyB++;
            if(a[i] && b[i])
                bothAB++;
            if(!a[i] && !b[i])
                neitherAB++;                        
        }
        
        double valor = switch(Evaluation.similarityMeasure){
            case Const.SIMILARIDADE_JACCARD -> bothAB/(onlyA + onlyB + bothAB);
            case Const.SIMILARIDADE_SOKAL_MICHENER -> (bothAB + neitherAB) / (onlyA + onlyB + bothAB + neitherAB);
            default -> 0;
        };
        
        return valor;
        
    }

    public static double calculateAverageDimension(Pattern[] p, int k){
        int total = 0;
        int i = 0;
        for(; i < k; i++){
            total += p[i].getItems().size();
        }
        return (double)total/(double)i;
    }

    public static double avaliarMedia(Pattern[] p, int k){
        double total = 0.0;
        int i = 0;
        for(; i < k; i++){
            total += p[i].getQuality();
        }
        return total/(double)i;
    }

    public static String getEvaluationMetric() {
        // Ensure it's set only once
        return Evaluation.evaluationMetric;
    }

    public static void setEvaluationMetric(String evaluationMetric) {
        // Ensure it's set only once
        if (Evaluation.evaluationMetric == null) { // compares to default value
            Evaluation.evaluationMetric = evaluationMetric;
        }
    }
    public static void setSimilarityMeasure(byte similarityMeasure) {
        // Ensure it's set only once
        if (Evaluation.similarityMeasure == 0) { // compares to default value
            Evaluation.similarityMeasure = similarityMeasure;
        }
    }
    public static void setMinSimilarity(float minSimilarity) {
        if (Evaluation.minSimilarity == 0.0f) { // Ensure it's set only once
            Evaluation.minSimilarity = minSimilarity;
        }
    }

    public static float getMinSimilarity(){
        return Evaluation.minSimilarity;
    }
}
