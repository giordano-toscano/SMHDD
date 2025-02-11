package smhdd.evolutionary;

import java.util.HashSet;
import java.util.Iterator;

import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Example;
import smhdd.data.Pattern;

public final class Evaluation {

    private static byte evaluationMetric;
    private static byte similarityMeasure;
    private static float minSimilarity;
    
    private Evaluation (){
        // Private constructor to prevent instantiation
    }
    
    public static void evaluate(Pattern[] patterns, byte evaluationMetric, D dataset){
        
    }


    public void evaluatePattern(Pattern p, D dataset){
                 
        boolean[] positiveCoverageArray = Evaluation.vetorResultantePositivoAND(p.getItems()); //Saber se somina ou é dominado. Isso ajuda!
        boolean[] negativeCoverageArray = Evaluation.vetorResultanteNegativoAND(p.getItems()); //Saber se somina ou é dominado. Isso ajuda!
       
        this.TP = Avaliador.TP(this.vrP);
        this.FP = Avaliador.FP(this.vrN);
        this.qualidade = Avaliador.avaliar(this.TP, this.FP, this.tipoAvaliacao);
        
        if(tipoAvaliacao.equals(Avaliador.METRICA_AVALIACAO_WRACC_OVER_SIZE)){
            if(itens.size() == 0){
                this.qualidade = 0;
            }else{
                this.qualidade = this.qualidade/(double)itens.size();
            }            
        }
        //this.sinonimos = new ArrayList<>();
        //this.subPatterns = new ArrayList<>();
        
        Pattern.numeroIndividuosGerados++;
    }

    public static boolean[] getPositiveAndNegativeCoverageArrays(HashSet<Integer> itens){
        boolean[] vetorResultantePositivo = new boolean[D.numeroExemplosPositivo];
        
        for(int i = 0; i < D.numeroExemplosPositivo; i++){            
            vetorResultantePositivo[i] = Avaliador.patternContemplaExemploAND(itens, D.Dp[i]);            
        }      
        
        return vetorResultantePositivo;
    }

    private static int countPositivesAndNegatives(HashSet<Integer> items, D dataset){
        int tpCount;
        int fpCount;
        int attributeIndex;
        int itemValue;
        Example[] examples = dataset.getExampleLists();
        int[] itemAttributes = dataset.getItemAttributesInt();
        int[] itemValues = dataset.getItemValuesInt();
        boolean isExampleCovered = true;
        
        for(int[] example : examples){       
            
            for(int item : items){
                attributeIndex = itemAttributes[item];
                itemValue = itemValues[item];

                if(example[attributeIndex] != itemValue){
                    isExampleCovered = false;
                    break;               
                } 
            }
            if(isExampleCovered == true){
                if(example[-1] == 2){

                } 
            }

        }      
        
        return vetorResultantePositivo;
    }

    // private static boolean patternContemplaExemploAND(HashSet<Integer> items, int[] exemplo){
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

        public static double calculateSimilarity(Pattern p1, Pattern p2){
        //REF: A Survey of Binary Similarity and Distance Measures (http://www.iiisci.org/Journal/CV$/sci/pdfs/GS315JG.pdf)
        double onlyA = 0.0;
        double onlyB = 0.0;
        double bothAB = 0.0;
        double neitherAB = 0.0;
        double Acount = 0.0;
        double Bcount =0.0;
        
        //POSITIVO
        boolean[] A = p1.getVrP();
        boolean[] B = p2.getVrP();
        for(int i = 0; i < A.length; i++){
            if(A[i]){
                Acount++;
            }
            if(B[i]){
                Bcount++;
            }
            if(A[i] && !B[i]){
                onlyA++;
            }
            if(B[i] && !A[i]){
                onlyB++;
            }
            if(A[i] && B[i]){
                bothAB++;
            }
            if(!A[i] && !B[i]){
                neitherAB++;                        
            }
        }
        
        
        //NEGATIVO
        A = p1.getVrN();
        B = p2.getVrN();
        for(int i = 0; i < A.length; i++){
            if(A[i]){
                Acount++;
            }
            if(B[i]){
                Bcount++;
            }
            if(A[i] && !B[i]){
                onlyA++;
            }
            if(B[i] && !A[i]){
                onlyB++;
            }
            if(A[i] && B[i]){
                bothAB++;
            }
            if(!A[i] && !B[i]){
                neitherAB++;                        
            }
        }
        
        double valor = 0;
        switch(Evaluation.similarityMeasure){
            case Const.SIMILARIDADE_JACCARD -> 
                valor = bothAB/(onlyA + onlyB + bothAB);
            case Const.SIMILARIDADE_SOKAL_MICHENER -> 
                valor = (bothAB + neitherAB) / (onlyA + onlyB + bothAB + neitherAB);
        }
        
        return valor;
        
    }

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
