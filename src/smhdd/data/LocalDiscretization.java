package smhdd.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import smhdd.evolutionary.Evaluation;

public class LocalDiscretization {

    public static int localIndex;
    
    public static Pattern[] run(D dataset, String evaluationMetric, Pattern[] patterns, float rate){
        if(localIndex == 0){
            localIndex = dataset.getCoreItemCount();
        }
        
        byte[] attributeTypes = dataset.getAttributeTypes();
        NumericalItemMemory numericalMemory = dataset.getNumericalItemMemory();
        Pattern[] newPatterns = new Pattern[patterns.length];
        float repetitions = patterns.length * rate;

        int k = 0;

        for (int i = 0; i < repetitions; i++){

            Pattern pattern = patterns[i];
            HashSet<Integer> items = pattern.getItems();
            double originalPatternQuality = pattern.getQuality();
            
            Pattern newPattern = null;
            HashSet<Integer> finalItems = new HashSet<>();
            double bestQuality = Double.MIN_VALUE;

            // Fill newItems with the categorical items of the pattern
            for(int item : items){
                int attributeIndex = dataset.getItemAttributeIndex(item);
                if (attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL) 
                    finalItems.add(item);
            }

            // Iterate over the items of the pattern and try to discretize the numerical ones, keeping the categorical ones as they are
            for(int item : items){
                int attributeIndex = dataset.getItemAttributeIndex(item);
                if(attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL)
                    continue;

                bestQuality = Double.MIN_VALUE;
                
                HashSet<Integer> itemsAux = new HashSet<>(finalItems);
                itemsAux.add(item);
                List<double[]> coveredExamples = getExamplesCoveredByItems(dataset, itemsAux);

                if (coveredExamples.size() == 0){
                    break;
                }

                double[] doubleArray = new double[coveredExamples.size()];
                for(int j = 0; j < coveredExamples.size(); j++)
                    doubleArray[j] = coveredExamples.get(j)[attributeIndex];
                
                Arrays.sort(doubleArray);
                double[][] result = D.equalWidthDiscretization(doubleArray, 2);
                
                bestQuality = Evaluation.calculateQuality(new Pattern(itemsAux), evaluationMetric, dataset);
                int bestItemIndex = item;

                for(double[] interval : result){
                    NumericalItem newItem = new NumericalItem(attributeIndex, interval[0], interval[1]);
                    int newItemIndex = numericalMemory.put(localIndex, newItem, dataset);
                    HashSet<Integer> itemsAux2 = new HashSet<>(finalItems);
                    itemsAux2.add(newItemIndex);
                    double quality = Evaluation.calculateQuality(new Pattern(itemsAux2), evaluationMetric, dataset);
                    boolean wasAdded = newItemIndex == localIndex;
                    if(quality > bestQuality){
                        bestQuality = quality;
                        bestItemIndex = newItemIndex;
                        localIndex = wasAdded ? (localIndex + 1) : localIndex; // Increment local index only when a new item is added and improves the quality
                    }else if(wasAdded){ // This means that a new item was added but did not improve the quality, so it should be removed
                        numericalMemory.remove(localIndex, dataset);
                    }
                }

                finalItems.add(bestItemIndex);
                
            }

            if(bestQuality > originalPatternQuality){
                //Pattern[] similars = pattern.getSimilars();
                newPattern = new Pattern(finalItems);
                newPattern.setQuality(bestQuality);
                //newPattern.setSimilars(null);
            }else{
                newPattern = pattern;
            }
            newPatterns[k++] = newPattern;
        }
        for(int l = k; l < newPatterns.length; l++){
            newPatterns[l] = patterns[l];
        }

        return newPatterns;
        
    }
    
    private static List<double[]> getExamplesCoveredByItems(D dataset, HashSet<Integer> items){
        int[] itemValues = dataset.getCategoricalItemValueIndexes();
        byte[] attributeTypes = dataset.getAttributeTypes();
        double[][] examples = dataset.getExamples();
        NumericalItemMemory numericalMemory = dataset.getNumericalItemMemory();
        List<double[]> coveredExamples = new ArrayList<>();

        for(var example : examples){
            boolean isCovered = true;
            for(Integer item : items){
                int attributeIndex = dataset.getItemAttributeIndex(item);
                double exampleAttributeValue = example[attributeIndex];

                if (attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL) {
                    int itemValue = itemValues[item];
                    if(itemValue != exampleAttributeValue)
                        isCovered = false;
                }else{
                    NumericalItem itemValue = numericalMemory.getNumericalItem(item);
                    if(!itemValue.contains(exampleAttributeValue))
                        isCovered = false;
                }
            }
            if(isCovered == true)
                coveredExamples.add(example);
        }       
        return coveredExamples; 
    }
}
