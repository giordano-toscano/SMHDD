package smhdd.main;
import java.io.IOException;
import smhdd.data.D;

public class SMHDD {
    public static void main(String[] args){
        try {
            D dataset = new D("datasets/teste_dataset.csv", ",");
            
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
