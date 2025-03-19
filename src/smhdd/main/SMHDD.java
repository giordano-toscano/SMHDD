package smhdd.main;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;
import smhdd.evolutionary.Crossover;
import smhdd.evolutionary.Evaluation;
import smhdd.evolutionary.Initialization;
import smhdd.evolutionary.Selection;

public class SMHDD {

    public static void main(String[] args){

        try {
            String directory = "datasets/";
            String file = "sun_og_labeled.csv";
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
            // byte[] attributeTypes = {Const.TYPE_CATEGORICAL, Const.TYPE_CATEGORICAL, Const.TYPE_NUMERICAL};

            System.out.println("Loading data set...");
            D dataset = new D(filepath, ",", 8);
            
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
            Pattern[] topk = SMHDD.run(dataset, k,-1);
            double execution_time = (System.currentTimeMillis() - t0)/1000.0; // Total execution time        
            
            // NEW AREAAA
            System.out.println("\n### Top-k subgroups:");
            D.imprimirRegras(dataset, topk); 
            
            //Informations about top-k DPs:  
            System.out.println("\nAverage " + Evaluation.getEvaluationMetric() + ": " + Evaluation.avaliarMedia(topk, k));
            System.out.println("Time(s): " + execution_time);
            // System.out.println("Average size: " + Avaliador.avaliarMediaDimensoes(p,k));        
            // System.out.println("Coverage of all Pk DPs in relation to D+: " + Avaliador.coberturaPositivo(p, k)*100 + "%");
            // System.out.println("Description Redundancy Item Dominador (|itemDominador|/k): " + DPinfo.descritionRedundancyDominator(p));
            // System.out.println("Number of individuals generated: " + Pattern.numeroIndividuosGerados);
            
            System.out.println("\n### Top-k and caches");
            //Avaliador.imprimirRegrasSimilares(p, k); 
            // String[] metricas = {
            //     Const.METRICA_QUALIDADE,
            //     Const.METRICA_SIZE,
            //     //Const.METRICA_WRACC,
            //     //Const.METRICA_Qg,
            //     //Const.METRICA_DIFF_SUP,
            //     //Const.METRICA_LIFT,
            //     //Const.METRICA_CHI_QUAD,
            //     //Const.METRICA_P_VALUE,
            //     //Const.METRICA_SUPP_POSITIVO,
            //     //Const.METRICA_SUPP_NEGATIVO,
            //     //Const.METRICA_COV,
            //     //Const.METRICA_CONF            
            // };
            // Avaliador.imprimirRegras(p, k, metricas, false, false, true);

            // NEW AREAAA

        } catch (IOException ex) {
        }
    }

    public static Pattern[] run(D dataset, int k, double maxTimeSegundos){
        
        Pattern[] topK = new Pattern[k];                
        // Initializes top-k with empty individuals
        for(int i = 0; i < topK.length;i++){
            topK[i] = new Pattern(new HashSet<>());
        }

        // Initializes ensuring that the population is larger than the top-k always! (in small datasets this is not always the case)
        Pattern[] population;
        Pattern[] populationAux = Initialization.generateDimension1Patterns(dataset); 
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

        Evaluation.setPositiveAndNegativeCoverageArrays(topK, population, dataset);
        Selection.saveRelevantPatterns(topK, population, dataset); 

        int generationsWithoutImprovementCount = 0;
        boolean isFirstGeneration = true;

        // GA Loop
        int populationSize = population.length;

        for(int resetCount = 0; resetCount < 3; resetCount++){ // Controls the number of resets
            if(resetCount > 0){
                population = Initialization.initializeUsingTopK(dataset, populationSize, topK);
                Evaluation.evaluatePopulation(population, dataset);    
            }

            double mutationRate = 0.4; // Mutation rate starts at 0.4. Crossover rate is always 1-mutationRate.
            
            while(generationsWithoutImprovementCount < 3){
                Pattern[] newPopulation;
                if(isFirstGeneration){
                    newPopulation = Crossover.mergeChromosomesInPopulation(population);
                    isFirstGeneration = false;  
                }else{
                    newPopulation = Crossover.applyUniformCrossoverInPopulation(population, mutationRate, dataset);        
                }  
                Evaluation.evaluatePopulation(newPopulation, dataset);
        
                Pattern[] populationBest = Selection.selectBest(population, newPopulation); 
                population = populationBest;   

                Evaluation.setPositiveAndNegativeCoverageArrays(topK, populationBest, dataset);
                int newlyAddedToTopk = Selection.saveRelevantPatterns(topK, populationBest, dataset); // Updating top-k and saving the number of added individuals

                // Automatic adjustment of mutation and crossover rates
                if(newlyAddedToTopk > 0 && mutationRate > 0.0) // Increases crossover rate if top-k is evolving and the mutation rate is higher than the lower limit
                    mutationRate -= 0.2;
                else if(newlyAddedToTopk == 0 && mutationRate < 1.0) // Increases mutation rate if top-k has not improved and mutation rate is not higher than the maximum limit.
                     mutationRate += 0.2;
                // Stop criterion: 3x without top-k improvement with mutation rate 1.0
                if(newlyAddedToTopk == 0 && mutationRate == 1.0)
                    generationsWithoutImprovementCount++;
                else
                    generationsWithoutImprovementCount = 0;
                                
            } 
            generationsWithoutImprovementCount = 0;
        }

        return topK;
    }
}
