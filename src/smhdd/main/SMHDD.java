package smhdd.main;
import java.io.IOException;
import java.util.ArrayList;
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
    public static int testCount;
    public static ArrayList<Double> tests;

    public static void main(String[] args){

        testCount = 40; //50
        tests = new ArrayList<>();
        
        try {
            String directory = "datasets/";
            String file = "alon-clean50-pn-width-2.csv";
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

            System.out.println("Loading data set...");
            D dataset = new D(filepath, ",");
            // setting the type of each attribute
            // byte[] attributeTypes = {Const.TYPE_CATEGORICAL, Const.TYPE_CATEGORICAL, Const.TYPE_NUMERICAL};
            // dataset.setAttributeTypes(attributeTypes);
            
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
            //Avaliador.imprimirRegras(p, k); 
            
            //Informations about top-k DPs:  
            System.out.println("Average " + Evaluation.getEvaluationMetric() + ": " + Evaluation.avaliarMedia(topk, k));
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

    // public static Pattern[] run(D dataset, int k, String tipoAvaliacao){
    //     return run(dataset, k, tipoAvaliacao, -1);
    // }

    public static Pattern[] run(D dataset, int k, double maxTimeSegundos){
        //long t0 = System.currentTimeMillis(); //Initial time
        Pattern[] topK = new Pattern[k];                
        Pattern[] population = null;
        
        //Inicializa Pk com indivíduos vazios
        for(int i = 0; i < topK.length;i++){
            topK[i] = new Pattern(new HashSet<>());
        }
        
        //Inicializa garantindo que P maior que Pk sempre! em bases pequenas isso nem sempre ocorre
        Pattern[] populationAux = Initialization.generateDimension1Patterns(dataset); //P recebe população inicial
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

        Selection.saveRelevantPatterns(topK, population, dataset); 

        int generationsWithoutImprovementCount = 0;
        boolean isFirstGeneration = true;
        //Laço do AG
        Pattern[] newPopulation = null;
        Pattern[] populationAsterisk = null;
        
        int populationSize = population.length;

        //System.out.println("Buscas...");
        for(int resetCount = 0; resetCount < 3; resetCount++){//Controle número de reinicializações
            //System.out.println("Reinicialização: " + numeroReinicializacoes);
            if(resetCount > 0){
                population = Initialization.initializeUsingTopK(dataset, populationSize, topK);
                Evaluation.evaluatePopulation(population, dataset);
            }
        
            double mutationTax = 0.4; //Mutação inicia em 0.4. Crossover é sempre 1-mutationTax.
        
            while(generationsWithoutImprovementCount < 3){
                if(isFirstGeneration){
                    newPopulation = Crossover.mergeChromosomesInPopulation(population);
                    isFirstGeneration = false;  
                }else{
                    newPopulation = Crossover.applyUniformCrossoverInPopulation(population, mutationTax, dataset);                 
                }       
                // TESTE ZONE
                if(tests.size()+1 == testCount){
                    for (Pattern row : newPopulation) {
                        System.out.println(row);
                    }
                }
                long t0 = System.currentTimeMillis(); //Initial time
                Evaluation.evaluatePopulation(newPopulation, dataset);       
                double execution_time = (System.currentTimeMillis() - t0)/1000.0; // Total execution time   
                tests.add(execution_time);
                if(tests.size() == testCount){
                    double soma = 0;
                    for(double test : tests)
                        soma = soma + test;
                    System.out.println("Média de tempo: "+soma/testCount);
                    int var = 2;
                    if(var == 2){
                        throw new Error(); 
                    }  
                }
                // END TEST ZONE

                populationAsterisk = Selection.selectBest(population, newPopulation); 
                population = populationAsterisk;   
                int novosK = Selection.saveRelevantPatterns(topK, populationAsterisk, dataset);//Atualizando Pk e coletando número de indivíduos substituídos 
                //double time = (System.currentTimeMillis() - t0)/1000.0; //time
               // if(maxTimeSegundos > 0 && time > maxTimeSegundos){
                //    return topK;
                //}
                //System.out.println("Modificações em Pk: " + novosK);
                //Definição automática de mutação de crossover
                if(novosK > 0 && mutationTax > 0.0)//Aumenta cruzamento se Pk estiver evoluindo e se mutação não não for a menos possível.
                    mutationTax -= 0.2;
                else if(novosK == 0 && mutationTax < 1.0)//Aumenta mutação caso Pk não tenha evoluido e mutação não seja maior que o limite máximo.
                     mutationTax += 0.2;
                //Critério de parada: 3x sem evoluir Pk com taxa de mutação 1.0
                if(novosK == 0 && mutationTax == 1.0)
                    generationsWithoutImprovementCount++;
                else
                    generationsWithoutImprovementCount = 0;
                                
            } 
            generationsWithoutImprovementCount = 0;
        }
        return topK;
    }

}
