package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;

public final class Initialization {

    private Initialization(){
        // Private constructor to prevent instantiation
    }

    public static Pattern[] generateDimension1Patterns(D dataset){ //é possivel paralelizar
        int itemCount = dataset.getItemCount();
        Pattern[] P0 = new Pattern[itemCount];
        
        for(int i = 0; i < itemCount; i++){
            HashSet<Integer> items = new HashSet<>();
            items.add(i);
            P0[i] = new Pattern(items);
        }        
        return P0;
    }

    public static Pattern[] initializeUsingTopK(D dataset, int populationSize, Pattern[] topK){
        int dimensionNumber =  (int) Evaluation.calculateAverageDimension(topK, topK.length);
        dimensionNumber = dimensionNumber < 2 ? 2 : dimensionNumber;      
        Pattern[] newPopulation = new Pattern[populationSize];
        int itemCount = dataset.getItemCount();

        //Adicionando aleatoriamente com até numeroDimensoes itens
        int populationSizePercentage = 9*populationSize/10;
        for(int i = 0; i < populationSizePercentage; i++){
            HashSet<Integer> items = new HashSet<>();
            
            while(items.size() < dimensionNumber){
                int item = Const.random.nextInt(itemCount);
                items.add(item);
            }            
            
            newPopulation[i] = new Pattern(items);
        }        
        
        //Coletanto todos os itens distintos da população Pk.
        HashSet<Integer> itemsTopK = new HashSet<>();
        for (Pattern patternTopK : topK)
            itemsTopK.addAll(patternTopK.getItems());

        int[] itemsTopKArray = new int[itemsTopK.size()];
        int n = 0;
        for(int itemTopK : itemsTopK)
            itemsTopKArray[n++] = itemTopK;
        
        
        // generating part of the population using the items present in the top-k         
        for(int j = populationSizePercentage; j < populationSize; j++){
            HashSet<Integer> items = new HashSet<>();
    
            while(items.size() < dimensionNumber){
                if(itemsTopKArray.length > dimensionNumber){
                    items.add(itemsTopKArray[Const.random.nextInt(itemsTopKArray.length)]);
                }else{//Caso especial: existem menos itens nas top-k do que o tamanho exigido para o invíduo             
                    if(Const.random.nextBoolean()){
                        items.add(itemsTopKArray[Const.random.nextInt(itemsTopKArray.length)]);
                    }else{
                        int item = Const.random.nextInt(itemCount);
                        items.add(item);
                    }
                }
            }     
            newPopulation[j] = new Pattern(items);
        }        
        return newPopulation;
    }
    
    // // maybe put it in Item class...
    // static Item retrieveItemFromIndex(D dataset, int itemIndex){ 
    //     int attributeIndex = dataset.getItemAttributesInt()[itemIndex];
    //     Item item;
    //     if(dataset.getAttributeTypes()[attributeIndex] == Const.TYPE_CATEGORICAL){
    //         int valueIndex = dataset.getItemValuesInt()[itemIndex];
    //         item = new Index(attributeIndex, valueIndex);
    //     }else{
    //         double[] interval = (double[]) dataset.getItemValuesObj()[itemIndex];
    //         item = new Interval(attributeIndex, interval[0], interval[1]);
    //     }
    //     return item;
    // }

}
