package smhdd.main;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;
import smhdd.evolutionary.Evaluation;
import smhdd.evolutionary.Initialization;
import smhdd.evolutionary.Selection;

public class SMHDD {

    public static void main(String[] args){
        try {
            String directory = "datasets/";
            String file = "teste_dataset.csv";
            String filepath = directory+file;

            Const.random = new Random(Const.SEEDS[0]); 
            
            // quantity of returned subgroups
            byte k = 10;
            // setting the maximum number of subgroups that are similar to another 
            Pattern.setMaxSimilarQuantity((byte) 5);
            // setting the evaluation metric
            Evaluation.setEvaluationMetric(Const.METRIC_WRACC);
            // setting the similarity measure
            Evaluation.setSimilarityMeasure(Const.SIMILARIDADE_JACCARD); 
            // setting threshold for determining when two subgroups are considered similar to each other
            Evaluation.setMinSimilarity(0.90f); 
            // setting the type of each attribute
            byte[] attributeTypes = {Const.TYPE_NUMERIC, Const.TYPE_STRING};
            
            System.out.println("Loading data set...");
            D dataset = new D(filepath, ",");
            dataset.setVariableTypes(attributeTypes);
            
            // displaying dataset info
            System.out.println(
                "### Data set:" + dataset.getName() + 
                " (|I|= " + dataset.getItemCount() + 
                "; |A|= " + dataset.getAttributeCount() +
                "; |D|= " + dataset.getExampleCount() +
                "; |D+|= " + dataset.getPositiveExampleCount() +
                "; |D-|= " + dataset.getNegativeExampleCount() +
                ")"); 

            System.out.println("Running SMHDD...");


            long t0 = System.currentTimeMillis(); //Initial time
            //Pattern[] p = SMHDD.run(dataset, k);
            double execution_time = (System.currentTimeMillis() - t0)/1000.0; // Total execution time 
                
    

            // System.err.println("TESTE NODE: ");
            // System.err.println("TESTE NODE: ");
            // Item<?>[] array = new Item[10];
            // array[0] = new Item<Integer>(1, 2);
            // array[1] = new Item<Integer[]>(1, new Integer[]{11, 12});
            // System.err.println(array[1].getValue().getClass().getSimpleName());            

            // .getClass().getSimpleName());


        } catch (IOException ex) {
        }
    }

    // public static Pattern[] run(D dataset, int k, String tipoAvaliacao){
    //     return run(dataset, k, tipoAvaliacao, -1);
    // }

    public static Pattern[] run(D dataset, int k){
        
        Pattern[] Pk = new Pattern[k];                
        Pattern[] P = null;
        
        //Inicializa Pk com indivíduos vazios
        for(int i = 0; i < Pk.length;i++){
            Pk[i] = new Pattern(new HashSet<>());
        }
        
        //Inicializa garantindo que P maior que Pk sempre! em bases pequenas isso nem sempre ocorre
        Pattern[] Paux = Initialization.dimension1(dataset); //P recebe população inicial
        if(Paux.length < k){
            P = new Pattern[k];            
            for(int i = 0; i < k; i++){
                if(i < Paux.length){
                    P[i] = Paux[i];
                }else{
                    P[i] = Paux[Const.random.nextInt(Paux.length-1)];
                }                
            }                
        }else{
            P = Paux;
        }      

        Arrays.sort(P);

        Selection.savingRelevatPatterns(Pk, P);
        
       
        return Pk;
    }

}
