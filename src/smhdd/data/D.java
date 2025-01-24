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
                String[] row = line.split(delimiter);
                data.add(row);
            }
        }
        // Convert List<String[]> to String[][]
        String[][] exampleStrMatrix = data.toArray(String[][]::new);

        // Initializing attributes
        this.attributeCount = this.variableNames.length - 1;
        this.exampleCount = exampleStrMatrix.length;

        return exampleStrMatrix;
    }

    private int[][] convertExamplesFromStrToInt(String[][] examplesStr){
        int numeroItens = 0;
                
        //Capturando os valores distintos de cada atributo
        ArrayList<HashSet<String>> valoresDistintosAtributos = new ArrayList<>(); //Amazena os valores distintos de cada atributo em um linha
        
        for(int i = 0; i < this.attributeCount; i++){
            HashSet<String> valoresDistintosAtributo = new HashSet<>(); //Armazena valores distintos de apenas um atributo. Criar HashSet para armezenar valores distintos de um atributo. Não admite valores repetidos!
            for(int j = 0; j < this.exampleCount; j++){
                valoresDistintosAtributo.add(examplesStr[j][i]); //Coleção não admite valores repetidos a baixo custo computacional.
            }
            numeroItens += valoresDistintosAtributo.size();
            
            valoresDistintosAtributos.add(valoresDistintosAtributo); //Adiciona lista de valores distintos do atributo de índice i na posição i do atributo atributosEvalores
        }
        
        //Gera 4 arrays para armazenar o universo deatributos e valores no formato original (String) e mapeado para inteiro.
        String[] itemAtributoStr = new String[numeroItens];
        String[] itemValorStr = new String[numeroItens];
        int[] itemAtributo = new int[numeroItens];
        int[] itemValor = new int[numeroItens];
            
        //Carrega arrays com universos de itens com valores reais e respectivos inteiros mapeados
        int[][] examplesInt = new int[this.exampleCount][this.attributeCount]; //dados no formato inteiro: mais rápido compararinteiros que strings
        int indiceItem = 0; //Indice vai de zero ao número de itens total
        for(int indiceAtributo = 0; indiceAtributo < valoresDistintosAtributos.size(); indiceAtributo++){
            Iterator valoresDistintosAtributoIterator = valoresDistintosAtributos.get(indiceAtributo).iterator(); //Capturando valores distintos do atributo de indice i
            int indiceValor = 0; //vai mapear um inteiro distinto para cada valor distinto de cada variável
            
            //Para cada atributo: 
            //Atribui inteiro para atributo e a cada valor do atributo.  
            //Realizar mapeamento na matriz de dados no formato inteiro
            while(valoresDistintosAtributoIterator.hasNext()){
                itemAtributoStr[indiceItem] = this.variableNames[indiceAtributo]; //
                itemValorStr[indiceItem] = (String)valoresDistintosAtributoIterator.next();

                // if(isNumber(D.itemValorStr[indiceItem])){
                //     D.numericAttributes.add(indiceAtributo);
                // }

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

        this.items = new int[numeroItens];
        for(int l = 0; l < numeroItens; l++){
            this.items[l] = l;
        }
        // Print Items
        for(int n = 0; n < numeroItens; n++){
            System.out.print(this.items[n] + "\t");
        }
        // End Print Items
        return examplesInt;
    }

    private void generateDpAndDn(String[][] examplesStr, int[][] examplesInt, String targetValue){
        //Capturar número de exemplo positivos (y="p") e negativos (y="n")
        int labelIndex = this.variableNames.length - 1;
        int attributeCount = this.variableNames.length - 1;
        int examplesCount = examplesStr.length;

        //Counting the number of positive and negative examples
        int positiveExamplesCount = 0;
        int negativeExamplesCount = 0;
        String label;
        for(int i = 0; i < examplesCount; i++){
            label = examplesStr[i][labelIndex];
            if(label.equals(targetValue)){
                positiveExamplesCount++;
            }else{
                negativeExamplesCount++;
            }
        }
        
        //inicializando Dp e Dn
        this.dp = new int[positiveExamplesCount][attributeCount];
        this.dn = new int[negativeExamplesCount][attributeCount];
        
        int indiceDp = 0;
        int indiceDn = 0;
        for(int i = 0; i < examplesCount; i++){
            label = examplesStr[i][labelIndex];
    
            if(label.equals(targetValue)){
                for(int j = 0; j < attributeCount; j++){
                    dp[indiceDp][j] = examplesInt[i][j];
                }
                indiceDp++;
            }else{
                for(int j = 0; j < attributeCount; j++){
                    dn[indiceDn][j] = examplesInt[i][j];
                }
                indiceDn++;            
            }
        }
    }


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
