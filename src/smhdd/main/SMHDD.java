package smhdd.main;
import java.io.IOException;
import smhdd.data.D;

public class SMHDD {
    public static void main(String[] args){
        try {
            D dataset = new D("datasets/teste_datasets.csv", ",");
            String[][] array = dataset.getExamples();
            
            // Example usage: Print the array
            for (String[] row : array) {
                for (String cell : row) {
                    System.out.print(cell + "\t");
                }
                System.out.println();
            }
            
            
            for (String row : dataset.getColumns()) {
                System.out.print(row + "\t");
                System.out.println();
            }
        } catch (IOException ex) {
        }
    }
}
