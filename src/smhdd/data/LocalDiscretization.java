package smhdd.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import smhdd.data.NumericalItem.Interval;
import smhdd.evolutionary.Evaluation;

public class LocalDiscretization {

    public static int localIndex;
    
    // For testing purposes only
    public static int numBins;
    public static String discretizationType;

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
                Interval[] result = discretizationType.equals("width") ? equalWidthDiscretization(doubleArray, numBins) : equalFrequencyDiscretization(doubleArray, numBins);
                
                bestQuality = Evaluation.calculateQuality(new Pattern(itemsAux), evaluationMetric, dataset);
                int bestItemIndex = item;

                for(Interval interval : result){
                    NumericalItem newItem = new NumericalItem(attributeIndex, interval);
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
                newPattern.setSimilars(null);
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

    /**
     * Equal-width discretization over the numeric domain.
     * Returns half-open bins [a,b) except the last which is [a,b].
     */
    public static Interval[] equalWidthDiscretization(double[] inputArray, int numIntervals) {
        final int n = (inputArray == null) ? 0 : inputArray.length;
        if (n == 0 || numIntervals <= 0) return new Interval[0];

        final double min = inputArray[0];
        final double max = inputArray[n - 1];

        if (Double.doubleToLongBits(min) == Double.doubleToLongBits(max)) {
        return new Interval[] { new Interval(min, max, true, true) };
        }

        final int k = numIntervals;
        final double width = (max - min) / k;

        // Extremely tiny range could underflow to 0 width; keep it sane.
        if (width == 0.0d) {
        return new Interval[] { new Interval(min, max, true, true) };
        }

        final Interval[] out = new Interval[k];
        for (int i = 0; i < k; i++) {
        final double lower = min + (i * width);
        final double upper = (i == k - 1) ? max : (min + ((i + 1) * width));
        final boolean upperClosed = (i == k - 1);
        out[i] = new Interval(lower, upper, true, upperClosed);
        }
        return out;
    }

    /**
     * Equal-frequency discretization using index-based quantile cuts on an already-sorted array.
     * Returns half-open bins [a,b) except the last which is [a,b].
     *
     * Constraint: if the produced result has 2+ identical intervals, only one is kept
     * (this also skips empty intervals of the form [x,x) which can arise from duplicates).
     */
    public static Interval[] equalFrequencyDiscretization(double[] inputArray, int numIntervals) {
        final int n = (inputArray == null) ? 0 : inputArray.length;
        if (n == 0 || numIntervals <= 0) return new Interval[0];

        final double min = inputArray[0];
        final double max = inputArray[n - 1];

        // Fast path: all identical values.
        if (Double.doubleToLongBits(min) == Double.doubleToLongBits(max)) {
        return new Interval[] { new Interval(min, max, true, true) };
        }

        // No point creating more bins than points; dedup may shrink further anyway.
        final int k = Math.min(numIntervals, n);
        final Interval[] tmp = new Interval[k];
        int outSize = 0;

        // Track last emitted interval to cheaply drop identical consecutive intervals.
        long lastLowerBits = 0L, lastUpperBits = 0L;
        boolean lastLowerClosed = false, lastUpperClosed = false;
        boolean haveLast = false;

        for (int i = 0; i < k; i++) {
        final int start = (int) (((long) i * (long) n) / (long) k);
        final int nextStart = (int) (((long) (i + 1) * (long) n) / (long) k);

        if (start >= n) break;

        final double lower = inputArray[start];

        final double upper;
        final boolean upperClosed;
        if (i == k - 1 || nextStart >= n) {
            upper = max;
            upperClosed = true;
        } else {
            upper = inputArray[nextStart];
            upperClosed = false;
        }

        // Skip empty interval [x,x) (common when many duplicates).
        if (!upperClosed && Double.doubleToLongBits(lower) == Double.doubleToLongBits(upper)) {
            continue;
        }

        final long lowerBits = Double.doubleToLongBits(lower);
        final long upperBits = Double.doubleToLongBits(upper);
        final boolean lowerClosed = true;

        // Dedup identical consecutive intervals (in sorted/discretized output, identical ones will be adjacent).
        if (haveLast
            && lowerBits == lastLowerBits
            && upperBits == lastUpperBits
            && lowerClosed == lastLowerClosed
            && upperClosed == lastUpperClosed) {
            continue;
        }

        tmp[outSize++] = new Interval(lower, upper, lowerClosed, upperClosed);

        haveLast = true;
        lastLowerBits = lowerBits;
        lastUpperBits = upperBits;
        lastLowerClosed = lowerClosed;
        lastUpperClosed = upperClosed;
        }

        return (outSize == tmp.length) ? tmp : Arrays.copyOf(tmp, outSize);
    }

    public static void reset(){
        localIndex = 0;
    }
}
