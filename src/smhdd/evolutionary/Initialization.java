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
            int attributeIndex = dataset.getItemAttributesInt()[i];
            Item item;
            if(D.getVariableTypes()[attributeIndex] == Const.TYPE_CATEGORICAL){
                int valueIndex = dataset.getItemValuesInt()[i];
                item = new Index(attributeIndex, valueIndex);
            }else{
                double[] interval = (double[]) dataset.getItemValuesObj()[i];
                item = new Interval(attributeIndex, interval[0], interval[1]);
            }
            items.add(item);

            P0[i] = new Pattern(items);
        }        
        return P0;
    }

    public static Pattern[] initializeUsingTopK(D dataset, int populationSize, Pattern[] Pk){
        //Ajeitar isso!!!
        int numeroDimensoes =  (int) Evaluation.calculateAverageDimension(Pk, Pk.length);
        if(numeroDimensoes < 2){
            numeroDimensoes = 2;
        }
        int item;
        //População que será retornada        
        Pattern[] P0 = new Pattern[populationSize];
        
        //Adicionando aleatoriamente com até numeroDimensoes itens
        int i = 0;
        for(; i < 9*populationSize/10; i++){
            HashSet<Item> itens = new HashSet<Item>();
            
            while(itens.size() < numeroDimensoes){
                item = D.itensUtilizados[Const.random.nextInt(D.numeroItensUtilizados)];
                
                itens.add(item);
            }            
            
            P0[i] = new Pattern(itens);
        }        
        
        
        //Coletanto todos os itens distintos da população Pk.
        HashSet<Integer> itensPk = new HashSet<>();
        for(int n = 0; n < Pk.length; n++){
            itensPk.addAll(Pk[n].getItens());
        }
        int[] itensPkArray = new int[itensPk.size()];
        
        Iterator iterator = itensPk.iterator();
        int n = 0;        
        while(iterator.hasNext()){
            itensPkArray[n++] = (int)iterator.next();
        }
        
        //Gerando parte da população utilizando os itens presentes em Pk        
        for(int j = i; j < populationSize; j++){
            HashSet<Integer> itens = new HashSet<Integer>();
            
            while(itens.size() < numeroDimensoes){
                if(itensPkArray.length > numeroDimensoes){
                    itens.add(itensPkArray[Const.random.nextInt(itensPkArray.length)]);
                }else{//Caso especial: existem menos itens nas top-k do que o tamanho exigido para o invíduo             
                    if(Const.random.nextBoolean()){
                        itens.add(itensPkArray[Const.random.nextInt(itensPkArray.length)]);
                    }else{
                        itens.add(D.itensUtilizados[Const.random.nextInt(D.numeroItensUtilizados)]);
                    }
                }
                
            }
                  
            P0[j] = new Pattern(itens);
        }        
        return P0;
    }

}
