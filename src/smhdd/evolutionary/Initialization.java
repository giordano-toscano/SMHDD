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

    public static Pattern[] generateDimension1Patterns(D dataset){
        Pattern[] P0 = new Pattern[dataset.getItemCount()];
        
        for(int i = 0; i < dataset.getItemCount(); i++){
            HashSet<Item> items = new HashSet<>();
            Item item = Initialization.retrieveItemFromIndex(dataset, i);
            items.add(item);

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
            HashSet<Item> items = new HashSet<>();
            
            while(items.size() < dimensionNumber){
                int itemIndex = Const.random.nextInt(itemCount);
                Item item = Initialization.retrieveItemFromIndex(dataset, itemIndex);
                items.add(item);
            }            
            
            newPopulation[i] = new Pattern(items);
        }        
        
        //Coletanto todos os itens distintos da população Pk.
        HashSet<Item> itemsTopK = new HashSet<>();
        for (Pattern patternTopK : topK)
            itemsTopK.addAll(patternTopK.getItems());

        Item[] itemsTopKArray = new Item[itemsTopK.size()];
        int n = 0;
        for(Item itemTopK : itemsTopK)
            itemsTopKArray[n++] = itemTopK;
        
        
        // generating part of the population using the items present in the top-k         
        for(int j = populationSizePercentage; j < populationSize; j++){
            HashSet<Item> items = new HashSet<>();
    
            while(items.size() < dimensionNumber){
                if(itemsTopKArray.length > dimensionNumber){
                    items.add(itemsTopKArray[Const.random.nextInt(itemsTopKArray.length)]);
                }else{//Caso especial: existem menos itens nas top-k do que o tamanho exigido para o invíduo             
                    if(Const.random.nextBoolean()){
                        items.add(itemsTopKArray[Const.random.nextInt(itemsTopKArray.length)]);
                    }else{
                        int itemIndex = Const.random.nextInt(itemCount);
                        items.add(Initialization.retrieveItemFromIndex(dataset, itemIndex));
                    }
                }
            }     
            newPopulation[j] = new Pattern(items);
        }        
        return newPopulation;
    }
    
    // maybe put it in Item class...
    static Item retrieveItemFromIndex(D dataset, int itemIndex){ 
        int attributeIndex = dataset.getItemAttributesInt()[itemIndex];
        Item item;
        if(dataset.getAttributeTypes()[attributeIndex] == Const.TYPE_CATEGORICAL){
            int valueIndex = dataset.getItemValuesInt()[itemIndex];
            item = new Index(attributeIndex, valueIndex);
        }else{
            double[] interval = (double[]) dataset.getItemValuesObj()[itemIndex];
            item = new Interval(attributeIndex, interval[0], interval[1]);
        }
        return item;
    }

}
