package smhdd.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class D {

    private String name; // dataset name
    private double[][] numericalColumns;
    private double[][] examples;
    private boolean[] labels;
    private String[] variableNames; 
    private byte[] attributeTypes;

    // Items
    private String[] itemAttributesStr;
    private Object[] itemValuesObj; 
    private int[] itemAttributesInt; 
    private int[] itemValuesInt; 

    // Counters
    private int itemCount;
    private int attributeCount;
    private int exampleCount;
    private int positiveExampleCount;
    private int negativeExampleCount;

    // Constructors
    public D(String path, String delimiter, byte[] attributeTypes, String targetValue) throws IOException{
        this.attributeTypes = attributeTypes;
        String[][] examplesStr = this.loadFile(path, delimiter);
        this.generateItems(examplesStr);
        this.convertExamplesFromStrToDouble(examplesStr);
        this.extractLabels(examplesStr, targetValue);
        this.extractNumericalColumns();
    }
    public D(String path, String delimiter, byte[] attributeTypes) throws IOException{
        this(path, delimiter, attributeTypes, "p"); // "\"p\""
    }
    public D(String path, String delimiter, String targetValue) throws IOException{
        this(path, delimiter, null, targetValue);
    }
    public D(String path, String delimiter) throws IOException{
        this(path, delimiter, null, "p");
    }

    // Main Methods
    // private String[][] loadFile(String filePath, String delimiter) throws IOException{    
    //     List<String[]> data = new ArrayList<>();
    //     try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
    //         String line;
    //         // column names in the first line
    //         line = br.readLine();
    //         this.variableNames = line.split(delimiter);

    //         while ((line = br.readLine()) != null) {
    //             // split the line by commas (or other delimiter if necessary)
    //             data.add(line.split(delimiter));
    //         }
    //     }
    //     // convert List<String[]> to String[][]
    //     String[][] exampleStrMatrix = data.toArray(String[][]::new);

    //     // initializing attributes
    //     this.attributeCount = this.variableNames.length - 1;
    //     this.exampleCount = exampleStrMatrix.length;
    //     this.attributeTypes = this.attributeTypes == null ? detectVariableTypes() : this.attributeTypes;

    //     return exampleStrMatrix;
    // }

        private String[][] loadFile(String filePath, String delimiter) throws FileNotFoundException{
        //Lendo arquivo no formato padrão
        Scanner scanner = new Scanner(new FileReader(filePath))
                       .useDelimiter("\\n");
        ArrayList<String[]> dadosString = new ArrayList<>();        
              
        
        String[] palavras = filePath.split("\\\\");
        if(palavras.length == 1){
            palavras = filePath.split("/");//Caso separador de pastas seja / e  não \\
        }
        
        this.name = palavras[palavras.length-1].replace(".CSV", "");//Nome do arquivo é a última palavra (caso .CSV)
        this.name = this.name.replace(".csv", "");//(caso .csv)
                
        this.variableNames = scanner.next().split(delimiter); //1º linha: nome das variáveis
        //Lipando nomes dos atributos
        for(int i = 0; i < this.variableNames.length; i++){
            this.variableNames[i] = this.variableNames[i].replaceAll("[\"\r\']", "");
        }
        
        this.attributeCount = this.variableNames.length-1; //último atributo é o rótulo
        while (scanner.hasNext()) {
            dadosString.add(scanner.next().split(delimiter));
        }
        this.exampleCount = dadosString.size();
        
        String[][] dadosStr = new String[this.exampleCount][this.attributeCount+1];
        for(int i = 0; i < dadosString.size(); i++){
            String[] exemploBase = dadosString.get(i);//recebe linha de dados
            for(int j = 0; j < exemploBase.length; j++){
                dadosStr[i][j] = exemploBase[j].replaceAll("[\"\r\']", "");
            }
        }       
        // initializing attributes
        this.attributeCount = this.variableNames.length - 1;
        this.exampleCount = dadosStr.length;
        this.attributeTypes = this.attributeTypes == null ? detectVariableTypes() : this.attributeTypes;

        scanner.close(); // COLOQUEI
        return dadosStr;
    }



    private byte[] detectVariableTypes(){
        byte[] types = new byte[this.variableNames.length-1];
        for(int i = 0; i < types.length; i++){
            types[i] = Const.TYPE_CATEGORICAL;
        }
        return types;
    }


    private void generateItems(String[][] examplesStr){
        this.itemCount = 0;
                
        @SuppressWarnings("unchecked")
        HashSet<Object>[] distinctAttributeValues = new HashSet[this.attributeCount]; // stores the distinct values of each attribute

        for(int i = 0; i < this.attributeCount; i++){
            HashSet<Object> distinctValuesSingleAttribute = new HashSet<>();         // stores distinct values of a single attribute
            if(this.attributeTypes[i] == Const.TYPE_CATEGORICAL) // categorical attribute
                distinctValuesSingleAttribute.addAll(this.gatherCategoricalColumnValues(examplesStr, i));
            else                                            // numerical attribute
                distinctValuesSingleAttribute.addAll(this.transformNumericalColumnIntoIntervals(examplesStr, i));
            this.itemCount += distinctValuesSingleAttribute.size();
            distinctAttributeValues[i] = distinctValuesSingleAttribute; // adds list of distinct values of attribute i in the i-th position of the "distinctAttributeValues" array
        }
        
        // creates 2 arrays to store attributes and values in their original format (String)
        this.itemAttributesStr = new String[itemCount];
        this.itemValuesObj = new Object[itemCount];
        // creates another 2 arrays to store attributes and values mapped to integer values
        this.itemAttributesInt = new int[itemCount];
        this.itemValuesInt = new int[itemCount];

        int itemIndex = 0;
        for(int attributeIndex = 0; attributeIndex < this.attributeCount; attributeIndex++){
            HashSet<Object> distinctValues = distinctAttributeValues[attributeIndex];
            int valueIndex = 0;
            for(Object valueStr : distinctValues){
                itemAttributesStr[itemIndex] = this.variableNames[attributeIndex]; 
                itemValuesObj[itemIndex] = valueStr;
                itemAttributesInt[itemIndex] = attributeIndex;
                itemValuesInt[itemIndex] = valueIndex;

                itemIndex++;
                valueIndex++;
            }
        }
    }

    private List<double[]> transformNumericalColumnIntoIntervals(String[][] examplesStr, int attributeIndex){
        String[] array = new String[this.exampleCount]; 
        for(int i = 0; i < this.exampleCount; i++)
            array[i] = examplesStr[i][attributeIndex];
        double[][] result = D.equalFrequencyBins(array, 3);
        List<double[]> list = new ArrayList<>();
        list.addAll(Arrays.asList(result));
        return list;
    }

    private List<String> gatherCategoricalColumnValues(String[][] examplesStr, int attributeIndex){
        List<String> list = new ArrayList<>();
        for(int j = 0; j < this.exampleCount; j++)
            list.add(examplesStr[j][attributeIndex]);
        return list;
    }

    private void convertExamplesFromStrToDouble(String[][] examplesStr){

        this.examples = new double[this.exampleCount][this.attributeCount]; // matrix of examples in integer format
        int itemIndex = 0;
        for (int attributeIndex : this.itemAttributesInt){
            if(this.attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL){ // categorical attribute
                for(int i = 0; i < this.exampleCount; i++){ 
                    if(examplesStr[i][attributeIndex].equals(this.itemValuesObj[itemIndex])){
                        examples[i][attributeIndex] = this.itemValuesInt[itemIndex];
                    }
                }
            }else{ // numerical attribute
                for(int i = 0; i < this.exampleCount; i++)
                    examples[i][attributeIndex] = Double.parseDouble(examplesStr[i][attributeIndex]);
            }
            itemIndex++;
        }
    }

    private void extractNumericalColumns(){
        int numericalCount = 0;
        for(byte type : this.attributeTypes){
            if(type == Const.TYPE_NUMERICAL)
                numericalCount++;
        }
        this.numericalColumns = new double[numericalCount][this.exampleCount];
        for(int i =  0; i < this.exampleCount; i++){
            for (int j = 0; j < this.attributeCount; j++){  
                if(this.attributeTypes[j] == Const.TYPE_NUMERICAL) 
                    this.numericalColumns[j][i] = this.examples[i][j];
            }
        }
    }

    private void extractLabels(String[][] examplesStrMatrix, String targetValue){
        this.labels = new boolean[this.exampleCount];
        int labelIndex = this.variableNames.length - 1;

        for(int i = 0; i < this.exampleCount; i++){
            String label = examplesStrMatrix[i][labelIndex];
            boolean isPositive = label.equals(targetValue);
            // counting the number of positive and negative examples
            if(isPositive){
                this.positiveExampleCount++;
            }else{
                this.negativeExampleCount++;
            }
            // setting example's label
            this.labels[i] = isPositive;
        }
    }

    // Utils //
    private static boolean isNumber(String str) {
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

    public static void displayExamplesTransposed(double[][] matrix) {
        // Iterate over columns first
        for (int col = 0; col < matrix[0].length; col++) {
            // Iterate over rows
            for (double[] matrix1 : matrix) {
                System.out.print(matrix1[col] + "\t");
            }
            System.out.println();
        }
    }


    public static double[][] equalFrequencyBins(String[] stringArray, int numBins) {

        if (stringArray == null) {
            throw new IllegalArgumentException("Input array cannot be null.");
        }

        // Step 1: Convert String array to double array
        double[] doubleArray = new double[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) {
            doubleArray[i] = Double.parseDouble(stringArray[i]);
        }

        // Step 2: Sort the double array
        Arrays.sort(doubleArray);

        return equalFrequencyBins(doubleArray, numBins);
    }

    // DeepSeek
    public static double[][] equalFrequencyBins(double[] sortedArray, int numBins) {
        if (sortedArray == null || sortedArray.length == 0 || numBins <= 0) {
            throw new IllegalArgumentException("Invalid input parameters.");
        }

        int n = sortedArray.length;
        int binSize = n / numBins;
        int remainder = n % numBins;

        double[][] bins = new double[numBins][2];
        int startIndex = 0;

        for (int i = 0; i < numBins; i++) {
            int endIndex = startIndex + binSize - 1;
            if (remainder > 0) {
                endIndex++;
                remainder--;
            }

            bins[i][0] = sortedArray[startIndex];
            bins[i][1] = sortedArray[endIndex];

            startIndex = endIndex + 1;
        }

        return bins;
    }

    // ChatGPT
    public static double[][] equalFrequencyDiscretization(double[] sortedArray, int numBins) {
        int n = sortedArray.length;
        if (n == 0 || numBins <= 0) throw new IllegalArgumentException("Invalid input parameters");

        double[][] intervals = new double[numBins][2];
        int binSize = n / numBins;
        int remainder = n % numBins; // Handle cases where n is not exactly divisible

        int index = 0;
        for (int i = 0; i < numBins; i++) {
            int nextIndex = index + binSize + (i < remainder ? 1 : 0) - 1;
            intervals[i][0] = sortedArray[index]; // Lower bound
            intervals[i][1] = sortedArray[nextIndex]; // Upper bound
            index = nextIndex + 1;
        }
        return intervals;
    }
    public static void imprimirRegras(D dataset, Pattern[] p){
        Pattern emptyPattern = new Pattern(new HashSet<Integer>());
        System.out.println(emptyPattern.display(dataset));
        for(int i = 0; i < p.length; i++){
            System.out.println(p[i].display(dataset));        
        }        
    }

    // GETs
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

    public int getPositiveExampleCount(){
        return this.positiveExampleCount;
    }

    public int getNegativeExampleCount(){
        return this.negativeExampleCount;
    }

    public int getItemCount(){
        return this.itemCount;
    }

    public double[][] getNumericalColumns(){
        return this.numericalColumns;
    }

    public double[][] getExamples(){
        return this.examples;
    }

    public int[] getItemAttributesInt() {
        return this.itemAttributesInt;
    }

    public int[] getItemValuesInt() {
        return this.itemValuesInt;
    }

    public String[] getItemAttributesStr() {
        return this.itemAttributesStr;
    }

    public Object[] getItemValuesObj() {
        return this.itemValuesObj;
    }

    public byte[] getAttributeTypes(){
        return this.attributeTypes;
    }
    
    public boolean[] getLabels(){
        return this.labels;
    }

    // SETs
    public void setAttributeTypes(byte[] types){
        this.attributeTypes = this.attributeTypes == null ? types : this.attributeTypes;
    }

    public String retrieveItemValueAsString(int itemIndex){
        int attributeIndex = this.getItemAttributesInt()[itemIndex];
        if(this.getAttributeTypes()[attributeIndex] == Const.TYPE_NUMERICAL){
        }
        return "";

    }

    // Main method
    public static void main(String[] args) throws IOException {
        // String directory = "datasets/";
        // String file = "toy_example_en_US.csv";
        // String filepath = directory+file;
        // D dataset  = new D(filepath, ",");
        // //byte[] attributeTypes = {Const.TYPE_CATEGORICAL, Const.TYPE_CATEGORICAL, Const.TYPE_NUMERICAL};
        // //dataset.setAttributeTypes(attributeTypes);

        // System.out.println("PRINT nomeVariaveis:");
        // for (String row : dataset.getVariableNames()) {
        //     System.out.print(row + "\t");
        //     System.out.println();
        // }

        // System.out.println("\nPRINT itemAtributosStr");
        // for (String cell : dataset.getItemAttributesStr()) {
        //     System.out.print(cell + "\t");
        // }
        // System.out.println("\nPRINT itemAtributosInt");
        // for (int cell : dataset.getItemAttributesInt()) {
        //     System.out.print(cell + "\t");
        // }

        // System.out.println("\nPRINT itemValuesObj");
        // for (Object cell : dataset.getItemValuesObj()) {
        //     System.out.print(cell + "\t");
        // }
        // System.out.println("\nPRINT itemValuesInt");
        // for (int cell : dataset.getItemValuesInt()) {
        //     System.out.print(cell + "\t");
        // }

        // System.out.println("\n\nBEFORE SORTING");
    
        // System.out.println("\nPRINT examplesTransposed");
        // D.displayExamplesTransposed(dataset.getExamplesTransposed());

        // System.out.println("\nPRINT examplesList");
        // for (Example row : dataset.getExampleLists()) {
        //     row.display();
        // }

        // System.out.println("\nAFTER SORTING");
        // Arrays.sort(dataset.getExamplesTransposed()[2]);

        // System.out.println("\nPRINT examplesTransposed");
        // D.displayExamplesTransposed(dataset.getExamplesTransposed());

        // System.out.println("\nPRINT examplesList");
        // for (Example row : dataset.getExampleLists()) {
        //     row.display();
        // }

    }

}

