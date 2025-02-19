package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Example;
import smhdd.data.Item;
import smhdd.data.Pattern;

public final class Evaluation {

    private static byte evaluationMetric;
    private static byte similarityMeasure;
    private static float minSimilarity;
    
    private Evaluation (){
        // Private constructor to prevent instantiation
    }

    public static void evaluatePopulation(Pattern[] population, D dataset){
        for(Pattern pattern : population){
            Evaluation.evaluatePattern(pattern, dataset);
        }
    }
    public static void evaluatePattern(Pattern p, D dataset){
        int[] result = Evaluation.getPositiveAndNegativeCount(p, dataset);
        p.setFP(result[0]);      
        p.setTP(result[1]);
    
        p.setQuality(Evaluation.calculateQuality(p, dataset));
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
            if(isCovered && example.getLabel() == true){
                positiveCount++;
            }else if(isCovered && example.getLabel() == false){
                negativeCount++;
            }     
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
            if(isCovered && example.getLabel() == true){
                positiveCoverageArray[i] = true;
            }else if(isCovered && example.getLabel() == false){
                negativeCoverageArray[i] = true;
            }
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

    private static double calculateQuality(Pattern p, D dataset){
        double quality = 0.0;
        int tp = p.getTP();
        int fp = p.getFP();

        switch(Evaluation.evaluationMetric){
            case Const.METRIC_QG -> quality = Evaluation.calculateQg(tp, fp);
            case Const.METRIC_WRACC -> quality = Evaluation.calculateWRAcc(tp, fp, dataset);
            case Const.METRIC_WRACC_NORMALIZED -> quality = Evaluation.calculateWRAccN(tp, fp, dataset);
            case Const.METRIC_WRACC_OVER_SIZE -> quality = Evaluation.calculateWRAcc(tp, fp, dataset) / p.getItems().size();
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
        double Bcount =0.0;

        boolean[] a, b;

        boolean[][] resultA = Evaluation.getPositiveAndNegativeCoverageArrays(p1, dataset);
        boolean[][] resultB = Evaluation.getPositiveAndNegativeCoverageArrays(p2, dataset);
        
        //POSITIVO
        a = resultA[1];
        b = resultB[1];
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
        a = resultA[0];
        b = resultB[0];
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

    // private static boolean patternContemplaExemploAND(HashSet<Integer> items, Example exemplo){
    //     //System.out.println("### Novo Exemplo ###");
    //     Iterator iterator = items.iterator();
    //     while(iterator.hasNext()){
    //         int item = (int)iterator.next();
    //         int itemAtributo = D.itemAtributo[item];
    //         int itemValor = D.itemValor[item];


    //         // System.out.println("D.itemAtributo: "+ D.itemAtributo[item]);
    //         // System.out.println("D.itemValor: "+ D.itemValor[item]);
    //         // System.out.println("D.itemAtributoStr: "+ D.itemAtributoStr[item]);
    //         // System.out.println("D.itemValorStr: "+ D.itemValorStr[item]);
    //         // System.out.println("D.itemValorStr: "+ D.numericAttributes.contains(D.itemAtributo[item]));
    //         if(exemplo[itemAtributo] != itemValor){

    //             return false;                    
    //         } 
    //     }       
    //     return true; 
    // }

    public static void setEvaluationMetric(byte evaluationMetric) {
        // Ensure it's set only once
        if (Evaluation.evaluationMetric == 0) { // compares to default value
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
