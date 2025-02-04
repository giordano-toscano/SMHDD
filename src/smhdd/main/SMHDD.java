package smhdd.main;
import java.io.IOException;
import smhdd.data.D;
import smhdd.data.Item;

public class SMHDD {
    public static void main(String[] args){
        try {
            D dataset = new D("datasets/teste_dataset.csv", ",");

            System.err.println("TESTE NODE: ");
            System.err.println("TESTE NODE: ");
            Item<?>[] array = new Item[10];
            array[0] = new Item<Integer>(1, 2);
            array[1] = new Item<Integer[]>(1, new Integer[]{11, 12});
            System.err.println(array[1].getValue().getClass().getSimpleName());            

            // .getClass().getSimpleName());

            
            //Pattern.numeroIndividuosGerados = 0; //Initializing count of generated individuals
            // System.out.println(
            //     "### Data set:" + D.nomeBase + 
            //     "(|I|=" + D.numeroItens + 
            //     "; |A|=" + dataset.getAttributeCount() +
            //     "; |D|=" + dataset.getExampleCount() +
            //     "; |D+|=" + D.numeroExemplosPositivo +
            //     "; |D-|=" + D.numeroExemplosNegativo +
            //     ")"); 
            // for (String row : dataset.getVariableNames()) {
            //     System.out.print(row + "\t");
            //     System.out.println();
            // }
        } catch (IOException ex) {
        }
    }

}
