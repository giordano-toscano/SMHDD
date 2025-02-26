package smhdd.evolutionary;

import java.util.Arrays;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;

public final class Selection {

    private Selection(){
        // Private constructor to prevent instantiation
    }

    public static int loopCount = 0;

    public static int saveRelevantPatterns(Pattern[] topK, Pattern[] pAsterisk, D dataset){
        int novosk = 0;
        double similaridade;
        for( int i = 0; i < pAsterisk.length && (pAsterisk[i].getQuality() > topK[topK.length-1].getQuality()); i++){
            Pattern p_PAsterisco = pAsterisk[i];
            //Três possibilidades
            //(1) igual a alguma DP de Pk: descartar
            //(2) não similar a nenhum de Pk: troca por Pk[Pk-length-1]
            //(3) similar
            //(3.1) similar com p_Pk maior: p_Pk engloba como similar e não muda a ordem das DPs em Pk
            //(3.2) similar com p_PAsterisco maior: p_PAsterisco engloba como similar p_Pk, ocupa a vaga em Pk[i] e reordena-se Pk
            for(int j = 0; j < topK.length; j++){
                Pattern p_Pk = topK[j];
                //double similaridade = SELECAO.similaridadeDPpositivo(p_PAsterisco, p_Pk);
                similaridade = Evaluation.calculateSimilarity(p_Pk, p_PAsterisco, dataset);
                if(similaridade >= Evaluation.getMinSimilarity()){// Houve similaridade
                    //Se eles tiverem os mesmos itens, descartar! (1)
                    if(p_PAsterisco.isEqualTo(p_Pk)){
                        break; //sair do for que itera Pk 
                    }else{
                        //Se não, Se pk melhor que p* ou (pk == p* and pk.size <= p*.size)
                        if(p_Pk.getQuality() > p_PAsterisco.getQuality() || (p_Pk.getQuality() == p_PAsterisco.getQuality() && p_Pk.getItems().size() <= p_PAsterisco.getItems().size())){
                            boolean aproveitadoEmPk = p_Pk.addSimilar(p_PAsterisco);
                            if(aproveitadoEmPk){
                                novosk++;
                            }
                        }else{
                            topK[j] = new Pattern(p_PAsterisco.getItems()); // GERAR AVALICAO DESTE !!!!!!!!!
                            topK[j].setQuality(p_PAsterisco.getQuality());
                            topK[j].setNegativeCoverageArray(p_PAsterisco.getNegativeCoverageArray());
                            topK[j].setPositiveCoverageArray(p_PAsterisco.getPositiveCoverageArray());
                            //Evaluation.setCoverageArraysInPattern(topK[j], dataset);
                            //Adicionando p_Pk como filho de P_PAsterisco
                            topK[j].addSimilar(p_Pk);
                            
                            //filhos de p_Pk podem ser adicionado a Pk.
                            if(p_Pk.getSimilars() != null){
                                Selection.saveRelevantPatterns(topK, p_Pk.getSimilars(), dataset);
                            }                            
                            Arrays.sort(topK);
                            novosk++;
                        }                        
                        break; //sair do for que itera Pk
                    }
                                        
                }else if(j == topK.length-1){//Se dp.new não for similar a nenhuma DP de Pk, então ele substitui a última
                    topK[topK.length-1] = new Pattern(p_PAsterisco.getItems());
                    topK[topK.length-1].setQuality(p_PAsterisco.getQuality());
                    topK[topK.length-1].setNegativeCoverageArray(p_PAsterisco.getNegativeCoverageArray());
                    topK[topK.length-1].setPositiveCoverageArray(p_PAsterisco.getPositiveCoverageArray());
                    Arrays.sort(topK);                                    
                    novosk++;                    
                }       
            }//for percorre Pk     
        }
        return novosk;
    }

    public static int[] binaryTournament(int populationSize, Pattern[] population){
        int[] indices = new int[populationSize];
        for(int i = 0; i < indices.length; i++){
            int indiceP1 = Const.random.nextInt(population.length);
            int indiceP2 = Const.random.nextInt(population.length);
            if(population[indiceP1].getQuality() > population[indiceP2].getQuality()){
                indices[i] = indiceP1;
            }else{
                indices[i] = indiceP2;         
            }
        }
        return indices;
    }

    public static Pattern[] selectBest(Pattern[] population, Pattern[] newPopulation){
        int populationSize = population.length;
        Pattern[] populationAsterisk = new Pattern[populationSize];        
        Pattern[] populationAux = new Pattern[2*populationSize];        
        System.arraycopy(population, 0, populationAux, 0, population.length);        
        System.arraycopy(newPopulation, 0, populationAux, population.length, newPopulation.length);        
        Arrays.sort(populationAux);                
        System.arraycopy(populationAux, 0, populationAsterisk, 0, populationAsterisk.length);                
        return populationAsterisk;
    }

}
