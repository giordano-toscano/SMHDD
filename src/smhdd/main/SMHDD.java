package smhdd.main;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.data.Pattern;

public class SMHDD {

    public static void main(String[] args){
        try {
            String directory = "datasets/";
            String file = "teste_dataset.csv";
            String filepath = directory+file;
            int k = 10;
            Const.random = new Random(Const.SEEDS[0]); 
            
            D dataset = new D(filepath, ",");

            // System.err.println("TESTE NODE: ");
            // System.err.println("TESTE NODE: ");
            // Item<?>[] array = new Item[10];
            // array[0] = new Item<Integer>(1, 2);
            // array[1] = new Item<Integer[]>(1, new Integer[]{11, 12});
            // System.err.println(array[1].getValue().getClass().getSimpleName());            

            // .getClass().getSimpleName());

            
            System.out.println(
                "### Data set:" + dataset.getName() + 
                " (|I|= " + dataset.getItemCount() + 
                "; |A|= " + dataset.getAttributeCount() +
                "; |D|= " + dataset.getExampleCount() +
                "; |D+|= " + dataset.getPositiveExampleCount() +
                "; |D-|= " + dataset.getNegativeExampleCount() +
                ")"); 

            System.out.println("Running SMHDD...");
            //long t0 = System.currentTimeMillis(); //Initial time
            //Pattern[] p = SSDPplus.run(k, tipoAvaliacao, similaridade, maxTimeSecond);

        } catch (IOException ex) {
        }
    }

    public static Pattern[] run(int k, String tipoAvaliacao){
        return run(k, tipoAvaliacao, -1);
        
    }

    public static Pattern[] run(int k, String tipoAvaliacao, double maxTimeSegundos){
        long t0 = System.currentTimeMillis(); //Initial time
        
        Pattern[] Pk = new Pattern[k];                
        Pattern[] P = null;
        
        //Inicializa Pk com indivíduos vazios
        for(int i = 0; i < Pk.length;i++){
            Pk[i] = new Pattern(new HashSet<Integer>());
        }
        
        //System.out.println("Inicializando população...");
        //Inicializa garantindo que P maior que Pk sempre! em bases pequenas isso nem sempre ocorre
        Pattern[] Paux = INICIALIZAR.D1(tipoAvaliacao);//P recebe população inicial
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
        
        
        //System.arraycopy(P, 0, Pk, 0, k); //Inicializa Pk com os melhores indivíduos da população inicial
        SELECAO.salvandoRelevantesDPmais(Pk, P, similaridade);
                     
//        System.out.println("P0");        
//        System.out.println("Qualidade média k/P: " + Avaliador.avaliarMedia(Pk,k) + "/" + Avaliador.avaliarMedia(P,P.length));
//        System.out.println("Dimensão média k/P: " + Avaliador.avaliarMediaDimensoes(Pk,k) + "/" + Avaliador.avaliarMediaDimensoes(P,P.length));        
//        System.out.println("Cobertura +: " + Avaliador.coberturaPositivo(Pk,k));       
//        Avaliador.imprimirDimensaoQuantidade(Pk, k, 15);
//        Avaliador.imprimirDimensaoQuantidade(P, P.length, 15);

        int numeroGeracoesSemMelhoraPk = 0;
        int indiceGeracoes = 1;
        
        //Laço do AG
        Pattern[] Pnovo = null;
        Pattern[] PAsterisco = null;
        
        int tamanhoPopulacao = P.length;
        
        //System.out.println("Buscas...");
        for(int numeroReinicializacoes = 0; numeroReinicializacoes < 3; numeroReinicializacoes++){//Controle número de reinicializações
            //System.out.println("Reinicialização: " + numeroReinicializacoes);
            if(numeroReinicializacoes > 0){
                P = INICIALIZAR.aleatorio1_D_Pk(tipoAvaliacao, tamanhoPopulacao, Pk);
            }
        
            double mutationTax = 0.4; //Mutação inicia em 0.4. Crossover é sempre 1-mutationTax.
            //System.out.println("============================");
            while(numeroGeracoesSemMelhoraPk < 3){

                if(indiceGeracoes == 1){
                    Pnovo = CRUZAMENTO.ANDduasPopulacoes(P, P, tipoAvaliacao);
                    indiceGeracoes++; 
                }else{
                    Pnovo = CRUZAMENTO.uniforme2Pop(P, mutationTax, tipoAvaliacao);                 
                }                   
                PAsterisco = SELECAO.selecionarMelhores(P, Pnovo); 
                P = PAsterisco;   

                int novosK = SELECAO.salvandoRelevantesDPmais(Pk, PAsterisco, similaridade);//Atualizando Pk e coletando número de indivíduos substituídos
                double tempo = (System.currentTimeMillis() - t0)/1000.0; //time
                if(maxTimeSegundos > 0 && tempo > maxTimeSegundos){
                    return Pk;
                }
                //System.out.println("Modificações em Pk: " + novosK);
                //Definição automática de mutação de crossover
                if(novosK > 0 && mutationTax > 0.0){//Aumenta cruzamento se Pk estiver evoluindo e se mutação não não for a menos possível.
                    mutationTax -= 0.2;
                }else if(novosK == 0 && mutationTax < 1.0){//Aumenta mutação caso Pk não tenha evoluido e mutação não seja maior que o limite máximo.
                     mutationTax += 0.2;
                }
                //Critério de parada: 3x sem evoluir Pk com taxa de mutação 1.0
                if(novosK == 0 && mutationTax == 1.0){
                    numeroGeracoesSemMelhoraPk++;

                }else{
                    numeroGeracoesSemMelhoraPk = 0;
                }

                //Impriminto resultados
                
                //Avaliador.imprimirRegrasSimilares(Pk,k);
                //Avaliador.imprimirRegras(Pk,k);

    //            System.out.println("P" + indiceGeracoes);        
                //System.out.println("Qualidade média k/P: " + Avaliador.avaliarMedia(Pk,k) + "/" + Avaliador.avaliarMedia(P,P.length));
                //System.out.println("Dimensão média k/P: " + Avaliador.avaliarMediaDimensoes(Pk,k) + "/" + Avaliador.avaliarMediaDimensoes(P,P.length));        
                //System.out.println("Cobertura +: " + Avaliador.coberturaPositivo(Pk,k));
    //            System.out.println("Novos k: " + novosK);

    //            System.out.println("P" + indiceGeracoes);        
    //            System.out.println(Avaliador.avaliarMedia(P,P.length));
    //            System.out.println(Avaliador.avaliarMediaDimensoes(P,P.length));        
    //            Avaliador.imprimirDimensaoQuantidade(P, P.length, 15);         
    //                        
    //            System.out.println("K" + indiceGeracoes);        
    //            System.out.println(Avaliador.avaliarMedia(Pk,k));
    //            System.out.println(Avaliador.avaliarMediaDimensoes(Pk,k));        
    //            Avaliador.imprimirDimensaoQuantidade(Pk, k, 15);
                //Acompanhamento de taxa de mutação e cruzamento
                //System.out.println("Melhorias:" + novosK  + ",M:" + mutationTax + ",C:" + (1.0-mutationTax));                         

            } 
            
            numeroGeracoesSemMelhoraPk = 0;
        }
        
               
        
        //return Pbest;
        return Pk;
    }

}
