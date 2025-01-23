package smhdd.main;
import java.io.IOException;
import smhdd.data.D;

public class SMHDD {
    public static void main(String[] args){
        try {
            D dataset = new D("datasets/teste_dataset.csv", ",");
                        
            for (String row : dataset.getVariableNames()) {
                System.out.print(row + "\t");
                System.out.println();
            }
        } catch (IOException ex) {
        }
    }
}
