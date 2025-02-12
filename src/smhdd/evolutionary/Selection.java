package smhdd.evolutionary;

import java.util.Arrays;
import smhdd.data.Pattern;

public final class Selection {

    private Selection(){
        // Private constructor to prevent instantiation
    }

    public static int savingRelevantPatterns(Pattern[] pk, Pattern[] pAsterisk){
        int novosk = 0;
        double similaridade;
        for( int i = 0; i < pAsterisk.length && (pAsterisk[i].getQuality() > pk[pk.length-1].getQuality()); i++){
            Pattern p_PAsterisco = pAsterisk[i];
            //Três possibilidades
            //(1) igual a alguma DP de Pk: descartar
            //(2) não similar a nenhum de Pk: troca por Pk[Pk-length-1]
            //(3) similar
            //(3.1) similar com p_Pk maior: p_Pk engloba como similar e não muda a ordem das DPs em Pk
            //(3.2) similar com p_PAsterisco maior: p_PAsterisco engloba como similar p_Pk, ocupa a vaga em Pk[i] e reordena-se Pk
            for(int j = 0; j < pk.length; j++){
                Pattern p_Pk = pk[j];
                //double similaridade = SELECAO.similaridadeDPpositivo(p_PAsterisco, p_Pk);
                similaridade = Evaluation.calculateSimilarity(p_Pk, p_PAsterisco);
                if(similaridade >= Evaluation.getMinSimilarity()){// Houve similaridade
                    //Se eles tiverem os mesmos itens, descartar! (1)
                    if(p_PAsterisco.isEqualTo(p_Pk)){
                        break; //sair do for que itera Pk 
                    }else{
                        //Se não, Se pk melhor que p* ou (pk == p* and pk.size <= p*.size)
                        if(p_Pk.getQuality() > p_PAsterisco.getQuality() ||
                                (p_Pk.getQuality() == p_PAsterisco.getQuality() && p_Pk.getItems().size() <= p_PAsterisco.getItems().size()) 
                                ){
                            boolean aproveitadoEmPk = p_Pk.addSimilar(p_PAsterisco);
                            if(aproveitadoEmPk){
                                novosk++;
                            }
                        }else{
                            pk[j] = new Pattern(p_PAsterisco.getItems()); // GERAR AVALICAO DESTE !!!!!!!!!
                            //Adicionando p_Pk como filho de P_PAsterisco
                            pk[j].addSimilar(p_Pk);
                            
                            //filhos de p_Pk podem ser adicionado a Pk.
                            if(p_Pk.getSimilars() != null){
                                Selection.savingRelevantPatterns(pk, p_Pk.getSimilars());
                            }                            
                            Arrays.sort(pk);
                            novosk++;
                        }                        
                        break; //sair do for que itera Pk
                    }
                                        
                }else if(j == pk.length-1){//Se dp.new não for similar a nenhuma DP de Pk, então ele substitui a última
                    pk[pk.length-1] = new Pattern(p_PAsterisco.getItems()); // GERAR AVALICAO DESTE !!!!!!!!!
                    Arrays.sort(pk);                                    
                    novosk++;                    
                }       
            }//for percorre Pk     
        }
        return novosk;
    }

}
