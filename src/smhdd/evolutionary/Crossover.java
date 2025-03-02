package smhdd.evolutionary;

import java.util.HashSet;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;

public final class Crossover {

    private Crossover(){
        // Private constructor to prevent instantiation
    }
    
    public static Pattern[] mergeChromosomesInPopulation(Pattern[] parentPopulation){
        int populationSize = parentPopulation.length;       
        Pattern[] offspringPopulation = new Pattern[populationSize];
        int[] populationIndexes1 = Selection.binaryTournament(populationSize, parentPopulation);
        int[] populationIndexes2 = Selection.binaryTournament(populationSize, parentPopulation);
        
        for(int i = 0; i < populationSize; i++){
            Pattern parentPattern1 = parentPopulation[populationIndexes1[i]];       
            Pattern parentPattern2 = parentPopulation[populationIndexes2[i]];
            offspringPopulation[i] = Crossover.chromosomeFusion(parentPattern1, parentPattern2);
        }        
        return offspringPopulation;
    }  

    public static Pattern chromosomeFusion(Pattern parent1, Pattern parent2){
        HashSet<Integer> newItems = new HashSet<>();
        newItems.addAll(parent1.getItems());
        newItems.addAll(parent2.getItems());
        Pattern child = new Pattern(newItems);
        return child;
    }


    public static Pattern[] applyUniformCrossoverInPopulation(Pattern[] parentPopulation, double taxaMutacao, D dataset){
        int populationSize = parentPopulation.length;
        Pattern[] offspringPopulation = new Pattern[populationSize];
        
        //int[] selecao = SELECAO.proporcao25_75(tamanhoPopulacao);
        int[] selecao = Selection.binaryTournament(populationSize, parentPopulation);           
        
        int indiceSelecao = 0;
        int indicePnovo = 0;
        while(indicePnovo < offspringPopulation.length-1){//Cuidado para não acessar índices maiores que o tamanho do array                
            if(Const.random.nextDouble() > taxaMutacao){                    
                Pattern[] novos = Crossover.uniformCrossover(parentPopulation[selecao[indiceSelecao]], parentPopulation[selecao[indiceSelecao+1]]);
                indiceSelecao += 2;
                offspringPopulation[indicePnovo++] = novos[0];                    
                if(indicePnovo < offspringPopulation.length){
                    offspringPopulation[indicePnovo++] = novos[1];                                                        
                }
                
            }else{
                offspringPopulation[indicePnovo++] = Mutation.mutate(parentPopulation[selecao[indiceSelecao++]], dataset);                                                       
            }         
        }
        
        if(indicePnovo < offspringPopulation.length){
            offspringPopulation[indicePnovo] = Mutation.mutate(parentPopulation[selecao[indiceSelecao++]], dataset);                                                                   
        }
                     
        return offspringPopulation;
    }

    private static Pattern[] uniformCrossover(Pattern parent1, Pattern parent2){
        Pattern[] offspring = new Pattern[2];
        HashSet<Integer> child1Items = new HashSet<>();
        HashSet<Integer> child2Items = new HashSet<>();
        
        HashSet<Integer> parent1Items = parent1.getItems();
        for(Integer item : parent1Items){
            if(Const.random.nextBoolean()){
                child1Items.add(item);
            }else{          
                child2Items.add(item);
            }  
        }

        HashSet<Integer> parent2Items = parent2.getItems();
        for(Integer item : parent2Items){
            if(Const.random.nextBoolean()){
                child1Items.add(item);
            }else{          
                child2Items.add(item);
            }  
        }
        offspring[0] = new Pattern(child1Items);
        offspring[1] = new Pattern(child2Items);
        return offspring;           
    }



}
