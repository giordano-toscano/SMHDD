package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;

public final class Mutation {
    
    private Mutation(){
        // Private constructor to prevent instantiation
    }

    public static Pattern mutate(Pattern p, D dataset){
        HashSet<Integer> items = p.getItems();
        int itemCount = dataset.getItemCount();
        
        if(items.isEmpty()){//Se indivíduo não tiver gene, retorne um novo aleatório de 1D
            int item = Const.random.nextInt(itemCount);
            items.add(item);            
            return new Pattern(items);
        }
        
        HashSet<Integer> newItems = new HashSet<>();
        double r = Const.random.nextDouble();
        if(r < 0.33){
            //Excluir gene
            int excludedItemIndex = Const.random.nextInt(items.size());
            int i = 0;
            for(int item : items){
                if(i != excludedItemIndex)
                    newItems.add(item);
                i++;
            }
            
        }else if(r > 0.66){//Troca gene por outro aleatório
            //Excluir gene
            int excludedItemIndex = Const.random.nextInt(items.size());
            int i = 0;
            for(int item : items){
                if(i != excludedItemIndex)
                    newItems.add(item);
                i++;
            }
                   
            //Adiciona novo gene
            while(newItems.size() < items.size()){
                int item = Const.random.nextInt(itemCount);
                newItems.add(item);            
            }
        }else{//Adiciona gene aleatoriamente
            //Adiciona novo gene
            newItems.addAll(items);
            while(newItems.size() < items.size() + 1){
                int item = Const.random.nextInt(itemCount);
                newItems.add(item);            
            }
        }                          
              
        Pattern pNovo = new Pattern(newItems);
        return pNovo;
    }

}
