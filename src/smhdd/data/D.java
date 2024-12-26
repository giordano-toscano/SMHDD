package smhdd.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class D {

    public D(String path, String delimiter){

    }

    public String[][] loadFile(String filePath, String delimiter) throws IOException{
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by commas (or other delimiter if necessary)
                String[] row = line.split(delimiter);
                data.add(row);
            }
        }
   
        // Convert List<String[]> to String[][]
        return data.toArray(String[][]::new);


    }


}
