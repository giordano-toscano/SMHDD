package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Item;
import smhdd.data.Pattern;

public final class Mutation {
    
    private Mutation(){
        // Private constructor to prevent instantiation
    }

    public static Pattern mutate(Pattern p, D dataset){
        HashSet<Item> items = p.getItems();
        int itemCount = dataset.getItemCount();
        
        if(items.isEmpty()){//Se indivíduo não tiver gene, retorne um novo aleatório de 1D
            int itemIndex = Const.random.nextInt(itemCount);
            items.add(Initialization.retrieveItemFromIndex(dataset, itemIndex));            
            return new Pattern(items);
        }
        
        HashSet<Item> newItems = new HashSet<>();
        double r = Const.random.nextDouble();
        if(r < 0.33){
            //Excluir gene
            int excludedItemIndex = Const.random.nextInt(items.size());
            int i = 0;
            for(Item item : items){
                if(i != excludedItemIndex)
                    newItems.add(item);
                i++;
            }
            
        }else if(r > 0.66){//Troca gene por outro aleatório
            //Excluir gene
            int excludedItemIndex = Const.random.nextInt(items.size());
            int i = 0;
            for(Item item : items){
                if(i != excludedItemIndex)
                    newItems.add(item);
                i++;
            }
                   
            //Adiciona novo gene
            while(newItems.size() < items.size()){
                int itemIndex = Const.random.nextInt(itemCount);
                newItems.add(Initialization.retrieveItemFromIndex(dataset, itemIndex));            
            }
        }else{//Adiciona gene aleatoriamente
            //Adiciona novo gene
            newItems.addAll(items);
            while(newItems.size() < items.size() + 1){
                int itemIndex = Const.random.nextInt(itemCount);
                newItems.add(Initialization.retrieveItemFromIndex(dataset, itemIndex));            
            }
        }                          
              
        Pattern pNovo = new Pattern(newItems);
        //Imprimir itens nos idivíduos gerados via cruzamento
        //DPinfo.imprimirItens(p);
        //System.out.print(r + "->");
        //DPinfo.imprimirItens(pNovo);
        //System.out.println();
        return pNovo;
    }

}
