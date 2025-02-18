package smhdd.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class D {

    private String name; // dataset name

    private String targetValue = "p";
    private String[] variableNames; 
    private Example.Node[][] examplesTransposed;
    private Example[] exampleLists;
    private static byte[] variableTypes;

    // Items
    private int[] items;
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

    public D(String path, String delimiter) throws IOException{
        String[][] examplesStr = this.loadFile(path, delimiter);
        this.generateItems(examplesStr);
        this.wrapInExampleLists(this.convertExamplesFromStrToDouble(examplesStr));
        this.labelExamples(examplesStr);
    }

    private String[][] loadFile(String filePath, String delimiter) throws IOException{    
        List<String[]> data = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // column names in the first line
            line = br.readLine();
            this.variableNames = line.split(delimiter);

            while ((line = br.readLine()) != null) {
                // split the line by commas (or other delimiter if necessary)
                data.add(line.split(delimiter));
            }
        }
        // convert List<String[]> to String[][]
        String[][] exampleStrMatrix = data.toArray(String[][]::new);

        // initializing attributes
        this.attributeCount = this.variableNames.length - 1;
        this.exampleCount = exampleStrMatrix.length;

        return exampleStrMatrix;
    }

    private void generateItems(String[][] examplesStr){
        this.itemCount = 0;
                
        @SuppressWarnings("unchecked")
        HashSet<Object>[] distinctAttributeValues = new HashSet[this.attributeCount]; // stores the distinct values of each attribute

        for(int i = 0; i < this.attributeCount; i++){
            HashSet<Object> distinctValuesSingleAttribute = new HashSet<>();         // stores distinct values of a single attribute
            if(D.variableTypes[i] == Const.TYPE_CATEGORICAL) // categorical attribute
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
        this.items = new int[itemCount];
        for(int l = 0; l < itemCount; l++){
            this.items[l] = l;
        }
    }

    private List<double[]> transformNumericalColumnIntoIntervals(String[][] examplesStr, int attributeIndex){
        String[] array = new String[this.exampleCount]; 
        for(int j = 0; j < this.exampleCount; j++)
            array[j] = examplesStr[j][attributeIndex];
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

    private double[][] convertExamplesFromStrToDouble(String[][] examplesStr){

        double[][] examplesDoubleMatrix = new double[this.exampleCount][this.attributeCount]; // matrix of examples in integer format
        int itemIndex = 0;
        for (int attributeIndex : this.itemAttributesInt){
            if(D.variableTypes[attributeIndex] == Const.TYPE_CATEGORICAL){ // categorical attribute
                for(int i = 0; i < this.exampleCount; i++){ 
                    if(examplesStr[i][attributeIndex].equals(this.itemValuesObj[itemIndex])){
                        examplesDoubleMatrix[i][attributeIndex] = this.itemValuesInt[itemIndex];
                    }
                }
            }else{ // numerical attribute
                for(int i = 0; i < this.exampleCount; i++)
                    examplesDoubleMatrix[i][attributeIndex] = Double.parseDouble(examplesStr[i][attributeIndex]);
            }
            itemIndex++;
        }
        return examplesDoubleMatrix;
    }

    private void wrapInExampleLists(double[][] examplesDouble){
        this.examplesTransposed = new Example.Node[this.attributeCount][this.exampleCount];
        this.exampleLists = new Example[this.exampleCount]; 

        for(int i =  0; i < this.exampleCount; i++){
            Example example = new Example();
            for (int j = 0; j < this.attributeCount; j++){   
                Example.Node node = new Example.Node(examplesDouble[i][j]);
                this.examplesTransposed[j][i] = node;

                example.insertNode(node);

            }
            exampleLists[i] = example;
        }
    }

    private void labelExamples(String[][] examplesStrMatrix){

        int labelIndex = this.variableNames.length - 1;

        for(int i = 0; i < this.exampleCount; i++){
            String label = examplesStrMatrix[i][labelIndex];
            boolean isPositive = label.equals(this.targetValue);
            // counting the number of positive and negative examples
            if(isPositive){
                this.positiveExampleCount++;
            }else{
                this.negativeExampleCount++;
            }
            // setting example's label
            this.exampleLists[i].setLabel(isPositive);
        }
    }

    // Utils //
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

    public static void displayExamplesTransposed(Example.Node[][] matrix) {
        // Iterate over columns first
        for (int col = 0; col < matrix[0].length; col++) {
            // Iterate over rows
            for (int row = 0; row < matrix.length; row++) {
                System.out.print(matrix[row][col] + "\t");
            }
            System.out.println();
        }
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

    public int[] getItems(){
        return this.items;
    }

    public Example.Node[][] getExamplesTransposed(){
        return this.examplesTransposed;
    }

    public Example[] getExampleLists(){
        return this.exampleLists;
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

    public static byte[] getVariableTypes(){
        return D.variableTypes;
    }

    // SETs
    public static void setVariableTypes(byte[] types){
        D.variableTypes = D.variableTypes == null ? types : D.variableTypes;
    }

    // Main method
    public static void main(String[] args) throws IOException {
        String directory = "datasets/";
        String file = "toy_example_en_US.csv";
        String filepath = directory+file;
        byte[] attributeTypes = {Const.TYPE_CATEGORICAL, Const.TYPE_CATEGORICAL, Const.TYPE_NUMERICAL};
        D.setVariableTypes(attributeTypes);
        D dataset  = new D(filepath, ",");

        System.out.println("PRINT nomeVariaveis:");
        for (String row : dataset.getVariableNames()) {
            System.out.print(row + "\t");
            System.out.println();
        }
        System.out.println("\nPRINT itemValuesObj");
        for (Object cell : dataset.getItemValuesObj()) {
            System.out.print(cell + "\t");
        }
        System.out.println("\nPRINT itemValuesInt");
        for (int cell : dataset.getItemValuesInt()) {
            System.out.print(cell + "\t");
        }
        System.out.println("\nPRINT itemAtributosStr");
        for (String cell : dataset.getItemAttributesStr()) {
            System.out.print(cell + "\t");
        }
        System.out.println("\nPRINT itemAtributosInt");
        for (int cell : dataset.getItemAttributesInt()) {
            System.out.print(cell + "\t");
        }

        System.out.println("\n\nBEFORE SORTING");
    
        System.out.println("\nPRINT examplesTransposed");
        D.displayExamplesTransposed(dataset.getExamplesTransposed());

        System.out.println("\nPRINT examplesList");
        for (Example row : dataset.getExampleLists()) {
            row.display();
        }

        System.out.println("\nAFTER SORTING");
        Arrays.sort(dataset.getExamplesTransposed()[2]);

        System.out.println("\nPRINT examplesTransposed");
        D.displayExamplesTransposed(dataset.getExamplesTransposed());

        System.out.println("\nPRINT examplesList");
        for (Example row : dataset.getExampleLists()) {
            row.display();
        }

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

/* 
 private void labelExamples(String[][] examplesStrMatrix, Example[] exampleLists){

    int labelIndex = this.variableNames.length - 1;

    //Counting the number of positive and negative examples
    this.positiveExampleCount = 0;
    this.negativeExampleCount = 0;

    for(int i = 0; i < this.exampleCount; i++){
        String label = examplesStrMatrix[i][labelIndex];
        if(label.equals(this.targetValue)){
            this.positiveExampleCount++;
        }else{
            this.negativeExampleCount++;
        }
        exampleLists[i].setLabel(label);
    }
    
    // initializing Dp e Dn
    this.dp = new int[this.positiveExampleCount][this.attributeCount];
    this.dn = new int[this.negativeExampleCount][this.attributeCount];
    
    int indiceDp = 0;
    int indiceDn = 0;
    for(int i = 0; i < this.exampleCount; i++){
        label = examplesStrMatrix[i][labelIndex];

        if(label.equals(targetValue)){
            dp[indiceDp] = examplesIntMatrix[i];
            indiceDp++;
        }else{
            dn[indiceDn] = examplesIntMatrix[i];
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
*/
