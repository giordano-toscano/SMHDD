package smhdd.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class D {

    private String name; // dataset name

    private static String targetValue = "p";
    private String[] variableNames;   // equivalent to nomeVariaveis 
    private int[] items;
    private int[][] dp; // positive examples
    private int[][] dn; // negative examples

    // Counters
    private int attributeCount;
    private int exampleCount;

    public D(String path, String delimiter) throws IOException{
        String[][] examplesStr = this.loadFile(path, delimiter);
        int[][] examplesInt = convertExamplesFromStrToInt(examplesStr);
        generateDpAndDn(examplesStr, examplesInt, "p");
    }

    //CHECKED !!!
    private String[][] loadFile(String filePath, String delimiter) throws IOException{    
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Column names in the first line
            line = br.readLine();
            this.variableNames = line.split(delimiter);

            while ((line = br.readLine()) != null) {
                // Split the line by commas (or other delimiter if necessary)
                data.add(line.split(delimiter));
            }
        }
        // Convert List<String[]> to String[][]
        String[][] exampleStrMatrix = data.toArray(String[][]::new);

        // Initializing attributes
        this.attributeCount = this.variableNames.length - 1;
        this.exampleCount = exampleStrMatrix.length;

        // PRINT AREA
        System.out.println("PRINT dadosStr:");
        String[][] array = exampleStrMatrix;
        for (String[] row : array) {
            for (String cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
            
        }
        System.out.println("PRINT nomeVariaveis:");
        for (String row : variableNames) {
            System.out.print(row + "\t");
            System.out.println();
        }
        // END PRINT AREA

        return exampleStrMatrix;
    }

    private int[][] convertExamplesFromStrToInt(String[][] examplesStr){
        int itemCount = 0;
                
        @SuppressWarnings("unchecked")
        HashSet<String>[] distinctAttributeValues = new HashSet[this.attributeCount]; // stores the distinct values of each attribute
        HashSet<String> distinctValues; // stores distinct values of a single attribute

        for(int i = 0; i < this.attributeCount; i++){
            distinctValues = new HashSet<>();
            for(int j = 0; j < this.exampleCount; j++){
                distinctValues.add(examplesStr[j][i]);
            }
            itemCount += distinctValues.size();
            
            distinctAttributeValues[i] = distinctValues; //Adiciona lista de valores distintos do atributo de índice i na posição i do atributo atributosEvalores
        }
        
        // creates 2 arrays to store attributes and values in their original format (String)
        String[] itemAtributoStr = new String[itemCount];
        String[] itemValorStr = new String[itemCount];
        // creates another 2 arrays to store attributes and values mapped to integer values
        int[] itemAtributo = new int[itemCount];
        int[] itemValor = new int[itemCount];
            
        int[][] examplesInt = new int[this.exampleCount][this.attributeCount]; // matrix of examples in integer format
        int indiceItem = 0; //Indice vai de zero ao número de itens total
        for(int indiceAtributo = 0; indiceAtributo < this.attributeCount; indiceAtributo++){
            Iterator valoresDistintosAtributoIterator = distinctAttributeValues[indiceAtributo].iterator(); //Capturando valores distintos do atributo de indice i
            int indiceValor = 0; //vai mapear um inteiro distinto para cada valor distinto de cada variável
            
            while(valoresDistintosAtributoIterator.hasNext()){
                itemAtributoStr[indiceItem] = this.variableNames[indiceAtributo]; //
                itemValorStr[indiceItem] = (String)valoresDistintosAtributoIterator.next();
                itemAtributo[indiceItem] = indiceAtributo;
                itemValor[indiceItem] = indiceValor;               
                
                //Preenche respectivo item (atributo, Valor) na matrix examplesInt com inteiro que mapeia valor categórico da base
                for(int m = 0; m < this.exampleCount; m++){ 
                    if(examplesStr[m][indiceAtributo].equals(itemValorStr[indiceItem])){
                        examplesInt[m][indiceAtributo] = itemValor[indiceItem];
                    }
                }
                indiceValor++;
                indiceItem++;
            }     
        } 

        this.items = new int[itemCount];
        for(int l = 0; l < itemCount; l++){
            this.items[l] = l;
        }
        // PRINT AREA
        System.out.println("PRINT itemValor");
        for (int cell : itemValor) {
            System.out.print(cell + "\t");
        }

        System.out.println("\nPRINT dadosInt");
        int[][] array = examplesInt;
        for (int[] row : array) {
            for (int cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
        // END PRINT AREA
        return examplesInt;
    }

    private void generateDpAndDn(String[][] examplesStr, int[][] examplesInt, String targetValue){

        int labelIndex = this.variableNames.length - 1;

        //Counting the number of positive and negative examples
        int positiveExamplesCount = 0;
        int negativeExamplesCount = 0;
        String label;
        for(int i = 0; i < this.exampleCount; i++){
            label = examplesStr[i][labelIndex];
            if(label.equals(targetValue)){
                positiveExamplesCount++;
            }else{
                negativeExamplesCount++;
            }
        }
        
        // initializing Dp e Dn
        this.dp = new int[positiveExamplesCount][this.attributeCount];
        this.dn = new int[negativeExamplesCount][this.attributeCount];
        
        int indiceDp = 0;
        int indiceDn = 0;
        for(int i = 0; i < this.exampleCount; i++){
            label = examplesStr[i][labelIndex];
    
            if(label.equals(targetValue)){
                dp[indiceDp] = examplesInt[i];
                indiceDp++;
            }else{
                dn[indiceDn] = examplesInt[i];
                indiceDn++;            
            }
        }
        // PRINT AREA
        System.out.println("PRINT Dp");
        int[][] array = dp;
        for (int[] row : array) {
            for (int cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }

        System.out.println("PRINT Dn");
        array = dn;
        for (int[] row : array) {
            for (int cell : row) {
                System.out.print(cell + "\t");
            }
            System.out.println();
        }
        // END PRINT AREA
    }

    // Get methods

    public String getName(){
        return this.name;
    }

    public String[] getVariableNames(){
        return this.variableNames;
    }

    public int getAttributeCount(){
        return this.attributeCount;
    }

    public int getExampleCount(){
        return this.exampleCount;
    }


    // Auxiliary Functions

    public static boolean isNumber(String str) {
        if (str == null || str.isEmpty()) return false;

        int len = str.length();
        boolean hasDot = false, hasDigit = false;

        for (int i = 0; i < len; i++) {
            char ch = str.charAt(i);

            if (ch >= '0' && ch <= '9') {
                hasDigit = true; // At least one digit must be present
            } else if (ch == '.' && !hasDot) {
                hasDot = true; // Only one dot allowed
            } else if (i == 0 && ch == '-') {
                // Allow leading negative sign, but only at index 0
            } else {
                return false; // If any other character appears, it's not a number
            }
        }
        return hasDigit; // Must contain at least one digit
    }

}

/*
 * 
 * public class CSVToArrayOptimized {
    public static String[][] readCSV(String filePath) throws IOException {
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                data.add(parseLine(line));
            }
        }
        return data.toArray(new String[0][]);
    }

    // Manually parse a CSV line into fields
    private static String[] parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;

        for (char ch : line.toCharArray()) {
            if (ch == '"') {
                // Toggle the inQuotes flag if a double-quote is encountered
                inQuotes = !inQuotes;
            } else if (ch == ',' && !inQuotes) {
                // Add the field if we hit a comma outside quotes
                fields.add(field.toString());
                field.setLength(0); // Clear the StringBuilder for the next field
            } else {
                // Append the character to the current field
                field.append(ch);
            }
        }
        // Add the last field
        fields.add(field.toString());
        return fields.toArray(new String[0]);
    }
 */
