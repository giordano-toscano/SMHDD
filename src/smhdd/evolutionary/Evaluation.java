package smhdd.evolutionary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.NumericalItem;
import smhdd.data.NumericalItemMemory;
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
        IntStream.range(0, populationSize).parallel().forEach(i -> {
            Pattern pattern = population[i];
            pattern.setQuality(Evaluation.calculateQuality(pattern, dataset));
        });
    }

    public static int[] getPositiveAndNegativeCount(Pattern p, D dataset){
        HashSet<Integer> items = p.getItems();
        int exampleCount = dataset.getExampleCount();
        double[][] examples = dataset.getExamples();
        boolean[] labels = dataset.getLabels();

        int positiveCount = 0;
        int negativeCount = 0;

        for(int i = 0; i < exampleCount; i++){        
            boolean isCovered = Evaluation.isExampleCoveredByPattern(dataset, items, examples[i]);  
            if(isCovered && labels[i] == true)
                positiveCount++;
            else if(isCovered && labels[i] == false)
                negativeCount++;    
        }      
        return new int[]{negativeCount, positiveCount};
    }

    private static boolean[][] getPositiveAndNegativeCoverageArrays(Pattern p, D dataset){
        HashSet<Integer> items = p.getItems();
        int exampleCount = dataset.getExampleCount();
        double[][] examples = dataset.getExamples();
        boolean[] labels = dataset.getLabels();
        
        boolean[] positiveCoverageArray = new boolean[dataset.getPositiveExampleCount()];
        boolean[] negativeCoverageArray = new boolean[dataset.getNegativeExampleCount()];
        int positiveArrayIndex = 0;
        int negativeArrayIndex = 0;

        for(int i = 0; i < exampleCount; i++){      
            boolean isCovered = Evaluation.isExampleCoveredByPattern(dataset, items, examples[i]);  
            if(labels[i] == true)
                positiveCoverageArray[positiveArrayIndex++] = isCovered;
            else
                negativeCoverageArray[negativeArrayIndex++] = isCovered;
        }      
        return new boolean[][]{negativeCoverageArray, positiveCoverageArray};
    
    }

    private static boolean isExampleCoveredByPattern(D dataset, HashSet<Integer> items, double[] example){
        int[] attributeIndexes = dataset.getItemAttributeIndexes();
        int[] itemValues = dataset.getCategoricalItemValueIndexes();
        int itemCount = dataset.getItemCount();
        byte[] attributeTypes = dataset.getAttributeTypes();
        NumericalItemMemory numericalMemory = dataset.getNumericalItemMemory();
        for(Integer item : items){
            int attributeIndex = item < itemCount ? attributeIndexes[item] : numericalMemory.getAttributeIndex(item);
            double exampleAttributeValue = example[attributeIndex];
            if (attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL) {
                int itemValue = itemValues[item];
                if(itemValue != exampleAttributeValue)
                    return false;
            }else{
                NumericalItem itemValue = numericalMemory.getNumericalItem(item);
                if(!itemValue.contains(exampleAttributeValue))
                    return false;
            }
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

    public static void setCoverageArraysInPattern(Pattern pattern, D dataset){
        boolean[][] result = Evaluation.getPositiveAndNegativeCoverageArrays(pattern, dataset);
        pattern.setNegativeCoverageArray(result[0]);
        pattern.setPositiveCoverageArray(result[1]);
    }

    public static void setPositiveAndNegativeCoverageArrays(Pattern[] topK, Pattern[] pAsterisk, D dataset){
        List<Pattern> topkAndSimilars = new ArrayList<>(Arrays.asList(topK));
        for(Pattern pattern : topK){
            Pattern[] similarsArray = pattern.getSimilars();
            if(similarsArray != null){
                List<Pattern> similars = Arrays.asList(pattern.getSimilars());
                topkAndSimilars.addAll(similars);
            }
        }
        Pattern[] topkAndSimilarsArray = topkAndSimilars.toArray(Pattern[]::new);
        
        int endIndex = getEndIndex(topK, pAsterisk, dataset);

        Pattern[] totalArray = new Pattern[endIndex+topkAndSimilarsArray.length];
        System.arraycopy(pAsterisk, 0, totalArray, 0, endIndex);        
        System.arraycopy(topkAndSimilarsArray, 0, totalArray, endIndex, topkAndSimilarsArray.length);  

        IntStream.range(0, totalArray.length).parallel().forEach(i -> Evaluation.setCoverageArraysInPattern(totalArray[i], dataset));
    }

    private static double calculateQuality(Pattern pattern, D dataset){
        int[] result = Evaluation.getPositiveAndNegativeCount(pattern, dataset);
        int fp = result[0];
        int tp = result[1];
        double quality = 0.0;

        switch(Evaluation.evaluationMetric){
            case Const.METRIC_QG -> quality = Evaluation.calculateQg(tp, fp);
            case Const.METRIC_WRACC -> quality = Evaluation.calculateWRAcc(tp, fp, dataset);
            case Const.METRIC_WRACC_NORMALIZED -> quality = Evaluation.calculateWRAccN(tp, fp, dataset);
            case Const.METRIC_WRACC_OVER_SIZE -> quality = Evaluation.calculateWRAcc(tp, fp, dataset) / pattern.getItems().size();
            case Const.METRIC_SUB -> quality = Evaluation.calculateSub(tp, fp);

        }
        return quality;
    }   

    private static double calculateWRAcc(int tp, int fp, D dataset){
        int globalExampleCount = dataset.getExampleCount();
        int globalPositiveExampleCount = dataset.getPositiveExampleCount();

        if(tp==0 && fp==0){
            return 0.0;
        }
        double sup = (double)(tp+fp) / (double)globalExampleCount;
        double conf = (double)tp / (double)(tp+fp);
        double confD = (double)globalPositiveExampleCount / (double)globalExampleCount;
        double wracc = sup * ( conf  - confD);
                       
        return wracc;
    }
    
    private static double calculateWRAccN(int tp, int fp, D dataset){             
        return 4 * calculateWRAcc(tp, fp, dataset);
    }
    
    private static double calculateQg(int tp, int fp){
        double qg = (double)tp/(double)(fp+1);
        return qg;
    }
    
    private static double calculateSub(int tp, int fp){
        double sub = tp-fp;
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
            default -> 0;
        };
        
        return valor;
        
    }

    public static double calculateConfidence(double tp, double fp){
        double confidence = tp / (tp+fp);
        return confidence;
    }   

    public static double calculateAverageDimension(Pattern[] p, int k){
        int total = 0;
        int i = 0;
        for(; i < k; i++){
            total += p[i].getItems().size();
        }
        return (double)total/(double)i;
    }

    public static double calculateAverageQuality(Pattern[] p, int k){
        double total = 0.0;
        int i = 0;
        for(; i < k; i++){
            total += p[i].getQuality();
        }
        return total/(double)i;
    }

    public static double globalPositiveSupport(Pattern[] p, int k, D dataset){
        HashSet<Integer> coveredExamples = new HashSet<>();
        
        for(int i = 0; i < k; i++){
            boolean[] vrpItem = p[i].getPositiveCoverageArray();
            for(int j = 0; j < vrpItem.length; j++){
                if(vrpItem[j]){
                    coveredExamples.add(j);
                }
            }
        }
        
        double globalPositiveSupport = (double) coveredExamples.size() / dataset.getPositiveExampleCount();
        return globalPositiveSupport;
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

    // Method 'getPositiveAndNegativeCoverageArrays' option 1 using IntStream
    // private static boolean[][] getPositiveAndNegativeCoverageArrays(Pattern p, D dataset) {
    //     HashSet<Integer> items = p.getItems();
    //     int exampleCount = dataset.getExampleCount();
    //     double[][] examples = dataset.getExamples();
    //     boolean[] labels = dataset.getLabels();

    //     boolean[] positiveCoverageArray = new boolean[dataset.getPositiveExampleCount()];
    //     boolean[] negativeCoverageArray = new boolean[dataset.getNegativeExampleCount()];

    //     // Step 2: Parallel computation with fixed indices
    //     IntStream.range(0, exampleCount).parallel().forEach(i -> {
    //         boolean isCovered = Evaluation.isExampleCoveredByPattern(dataset, items, examples[i]);

    //         if (labels[i]) {
    //             positiveCoverageArray[positiveIndices[i]] = isCovered;
    //         } else {
    //             negativeCoverageArray[negativeIndices[i]] = isCovered;
    //         }
    //     });

    //     return new boolean[][]{negativeCoverageArray, positiveCoverageArray};
    // }
    // public static void setPositiveAndNegativeIndexes(D dataset){
    //     boolean[] labels = dataset.getLabels();
    //     // Step 1: Precompute indices
    //     Evaluation.positiveIndices = new int[dataset.getExampleCount()];
    //     Evaluation.negativeIndices = new int[dataset.getExampleCount()];

    //     int positiveIndex = 0, negativeIndex = 0;
    //     for (int i = 0; i < dataset.getExampleCount(); i++) {
    //         if (labels[i]) 
    //             positiveIndices[i] = positiveIndex++;
    //         else 
    //             negativeIndices[i] = negativeIndex++;
    //     }
    // }

    // Method 'getPositiveAndNegativeCoverageArrays' option 2 using ForkJoinPool
    // static class EvaluatePopulationTask extends RecursiveAction {
    //     private final Pattern pattern;
    //     private final int start, end;
    //     private final D dataset;
    //     public static boolean[] positiveCoverageArray = null;
    //     public static boolean[] negativeCoverageArray = null;
    //     // Global atomic counters to track the next free index in each array.

    //     public EvaluatePopulationTask(Pattern pattern, int start, int end, D dataset) {
    //         this.pattern = pattern;
    //         this.start = start;
    //         this.end = end;
    //         this.dataset = dataset;
    //     }

    //     @Override
    //     protected void compute() {
    //         if (end - start <= THRESHOLD) { // Directly process small chunks
    //             boolean[] labels = dataset.getLabels();
    //             double[][] examples = dataset.getExamples();
    //             for (int i = start; i < end; i++) {
    //                 boolean isCovered = Evaluation.isExampleCoveredByPattern(dataset, pattern.getItems(), examples[i]);
    //                 if (labels[i]) 
    //                     positiveCoverageArray[positiveIndices[i]] = isCovered;
    //                 else 
    //                     negativeCoverageArray[negativeIndices[i]] = isCovered;    
    //             }
    //         } else {
    //             int mid = (start + end) / 2;
    //             EvaluatePopulationTask leftTask = new EvaluatePopulationTask(pattern, start, mid, dataset);
    //             EvaluatePopulationTask rightTask = new EvaluatePopulationTask(pattern, mid, end, dataset);
                
    //             invokeAll(leftTask, rightTask); // Parallel execution
    //         }
    //     }
    // }

    // public static void getPositiveAndNegativeCoverageArrays(Pattern p, D dataset) {
    //     int exampleCount = dataset.getExampleCount();
    //     boolean[] positiveCoverageArray = new boolean[dataset.getPositiveExampleCount()];
    //     boolean[] negativeCoverageArray = new boolean[dataset.getNegativeExampleCount()];
    //     p.setNegativeCoverageArray(negativeCoverageArray);
    //     p.setPositiveCoverageArray(positiveCoverageArray);


    //     EvaluatePopulationTask.positiveCoverageArray = p.getPositiveCoverageArray();
    //     EvaluatePopulationTask.negativeCoverageArray = p.getNegativeCoverageArray();
    //     Evaluation.pool.invoke(new EvaluatePopulationTask(p, 0, exampleCount, dataset));
    // }

    // public static void setPositiveAndNegativeIndexes(D dataset){
    //     boolean[] labels = dataset.getLabels();
    //     // Step 1: Precompute indices
    //     Evaluation.positiveIndices = new int[dataset.getExampleCount()];
    //     Evaluation.negativeIndices = new int[dataset.getExampleCount()];

    //     int positiveIndex = 0, negativeIndex = 0;
    //     for (int i = 0; i < dataset.getExampleCount(); i++) {
    //         if (labels[i]) 
    //             positiveIndices[i] = positiveIndex++;
    //         else 
    //             negativeIndices[i] = negativeIndex++;
    //     }
    // }
}
