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
            String file = "toy_example_en_US.csv";
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
            byte[] attributeTypes = {Const.TYPE_CATEGORICAL, Const.TYPE_CATEGORICAL, Const.TYPE_NUMERICAL};
            D.setVariableTypes(attributeTypes);

            System.out.println("Loading data set...");
            D dataset = new D(filepath, ",");
            
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
            Pattern[] p = SMHDD.run(dataset, k);
            double execution_time = (System.currentTimeMillis() - t0)/1000.0; // Total execution time          

        } catch (IOException ex) {
        }
    }

    // public static Pattern[] run(D dataset, int k, String tipoAvaliacao){
    //     return run(dataset, k, tipoAvaliacao, -1);
    // }

    public static Pattern[] run(D dataset, int k){
        
        Pattern[] topK = new Pattern[k];                
        Pattern[] population = null;
        
        //Inicializa Pk com indivíduos vazios
        for(int i = 0; i < topK.length;i++){
            topK[i] = new Pattern(new HashSet<>());
        }
        
        //Inicializa garantindo que P maior que Pk sempre! em bases pequenas isso nem sempre ocorre
        Pattern[] populationAux = Initialization.generateDimension1Patterns(dataset); //P recebe população inicial (PRECISA SER AVALIADA !!!!!!!!!!!!)
        if(populationAux.length < k){
            population = new Pattern[k];            
            for(int i = 0; i < k; i++){
                if(i < populationAux.length)
                    population[i] = populationAux[i];
                else
                    population[i] = populationAux[Const.random.nextInt(populationAux.length-1)];                
            }                
        }else{
            population = populationAux;
        }      
        Evaluation.evaluatePopulation(population, dataset);
        
        Arrays.sort(population);

        System.out.println("\nPRINT examplesList");
        for (Pattern row : population) {
            System.out.println(row);
        }

        Selection.saveRelevantPatterns(topK, population, dataset);

        int numeroGeracoesSemMelhoraPk = 0;
        int indiceGeracoes = 1;
        
        //Laço do AG
        Pattern[] Pnovo = null;
        Pattern[] PAsterisco = null;
        
        int tamanhoPopulacao = population.length;
        
       
        return topK;
    }

}
