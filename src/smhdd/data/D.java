package smhdd.data;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import smhdd.data.NumericalItem.Interval;

public class D {

    private String name; // dataset name
    private double[][] numericalColumns;
    private double[][] examples;
    private boolean[] labels;
    private String[] variableNames; 
    private byte[] attributeTypes;

    // Items
    private String[] itemAttributes;
    private String[] categoricalItemValues; 
    private int[] itemAttributeIndexes; 
    private int[] categoricalItemValueIndexes; 
    private NumericalItemMemory numericalItemMemory;

    // Counters
    private int itemCounter;
    private int coreItemCount;
    private int attributeCount;
    private int exampleCount;
    private int positiveExampleCount;
    private int negativeExampleCount;

    public boolean hasNumericalAttributes;

    // Constructors
    public D(String path, String delimiter, String targetValue, byte datasetType) throws IOException{
        String[][] examplesStr = this.loadFile(path, delimiter, datasetType);
        this.generateItems(examplesStr);
        this.convertExamplesFromStrToDouble(examplesStr);
        this.extractLabels(examplesStr, targetValue);
        this.extractNumericalColumns();
        this.hasNumericalAttributes = this.numericalAttributeExists();
    }
    public D(String path, String delimiter, String targetValue) throws IOException{
        this(path, delimiter, targetValue, Const.DATASET_TYPE_CATEGORICAL);
    }
    public D(String path, String delimiter, byte datasetType) throws IOException{
        this(path, delimiter, "p", datasetType);
    }
    public D(String path, String delimiter) throws IOException{
        this(path, delimiter, "p", Const.DATASET_TYPE_AUTO_DETECT);
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

        private String[][] loadFile(String filePath, String delimiter, byte datasetType) throws FileNotFoundException{
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
        this.attributeTypes = datasetType == Const.DATASET_TYPE_CATEGORICAL ? retrieveAllCategoricalVariableTypes() : retrieveAutoDetectedVariableTypes(dadosStr);

        scanner.close(); 
        return dadosStr;
    }

    private byte[] retrieveAutoDetectedVariableTypes(String[][] examplesStr){
        byte[] types = new byte[this.variableNames.length-1];
        for(int i = 0; i < types.length; i++){
            if (this.isNumber(examplesStr[0][i])) {
                types[i] = Const.TYPE_NUMERICAL;
            }else{
                types[i] = Const.TYPE_CATEGORICAL;
            }
        }
        return types;
    }

    private byte[] retrieveAllCategoricalVariableTypes(){
        byte[] types = new byte[this.variableNames.length-1];
        for(int i = 0; i < types.length; i++)
            types[i] = Const.TYPE_CATEGORICAL;
        return types;
    }

    private void generateItems(String[][] examplesStr){
        //this.itemCounter = 0;
                
        @SuppressWarnings("unchecked")
        HashSet<Object>[] distinctAttributeValues = new HashSet[this.attributeCount]; // stores the distinct values of each attribute

        for(int i = 0; i < this.attributeCount; i++){
            HashSet<Object> distinctValuesSingleAttribute = new HashSet<>();         // stores distinct values of a single attribute
            if(this.attributeTypes[i] == Const.TYPE_CATEGORICAL) 
                distinctValuesSingleAttribute.addAll(this.gatherCategoricalColumnValues(examplesStr, i));
            else                                            
                distinctValuesSingleAttribute.addAll(this.transformNumericalColumnIntoIntervals(examplesStr, i));
            this.itemCounter += distinctValuesSingleAttribute.size();
            distinctAttributeValues[i] = distinctValuesSingleAttribute; // adds list of distinct values of attribute i in the i-th position of the "distinctAttributeValues" array
        }

        this.coreItemCount = itemCounter; 

        // creates 2 arrays to store attributes and values in their original format (String)
        this.itemAttributes = new String[coreItemCount];
        this.categoricalItemValues = new String[coreItemCount];
        // creates another 2 arrays to store attributes and values mapped to integer values
        this.itemAttributeIndexes = new int[coreItemCount];
        this.categoricalItemValueIndexes = new int[coreItemCount];

        this.numericalItemMemory = new NumericalItemMemory(coreItemCount);

        int itemIndex = 0;
        for(int attributeIndex = 0; attributeIndex < this.attributeCount; attributeIndex++){
            HashSet<Object> distinctValues = distinctAttributeValues[attributeIndex];
            int valueIndex = 0;
            for(Object value : distinctValues){
                if(this.attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL){
                    this.categoricalItemValues[itemIndex] =  (String) value;
                    this.categoricalItemValueIndexes[itemIndex] = valueIndex;
                }else{
                    Interval interval = (Interval) value;
                    this.numericalItemMemory.put(itemIndex, new NumericalItem(attributeIndex, interval));
                }

                itemAttributes[itemIndex] = this.variableNames[attributeIndex];
                itemAttributeIndexes[itemIndex] = attributeIndex;

                itemIndex++;
                valueIndex++;
            }
        }
    }

    private List<Interval> transformNumericalColumnIntoIntervals(String[][] examplesStr, int attributeIndex){
        String[] array = new String[this.exampleCount]; 
        for(int i = 0; i < this.exampleCount; i++)
            array[i] = examplesStr[i][attributeIndex];
        Interval result = D.getInterval(array);
        List<Interval> list = new ArrayList<>();
        list.add(result);
        return list;
    }

    private static Interval getInterval(String[] stringArray) {

        if (stringArray == null) 
            throw new IllegalArgumentException("Input array cannot be null.");
  
        // Convert String array to double array
        double[] doubleArray = new double[stringArray.length];
        for (int i = 0; i < stringArray.length; i++) 
            doubleArray[i] = Double.parseDouble(stringArray[i]);
        
        // Sort the double array and get [min, max] interval
        Arrays.sort(doubleArray);
        Interval result = new Interval(doubleArray[0], doubleArray[doubleArray.length - 1], true, true);
        return result;
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
        for (int attributeIndex : this.itemAttributeIndexes){
            if(this.attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL){ // categorical attribute
                for(int i = 0; i < this.exampleCount; i++){ 
                    if(examplesStr[i][attributeIndex].equals(this.categoricalItemValues[itemIndex])){
                        examples[i][attributeIndex] = this.categoricalItemValueIndexes[itemIndex];
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
        int numericalColumnsRowIndex = 0;

        for(int j =  0; j < this.attributeCount; j++){

            if(this.attributeTypes[j] == Const.TYPE_NUMERICAL){

                for (int i = 0; i < this.exampleCount; i++)
                    this.numericalColumns[numericalColumnsRowIndex][i] = this.examples[i][j];
                numericalColumnsRowIndex++;
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
    private boolean isNumber(String str) {
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

    private static void displayTransposedMatrix(double[][] matrix) {
        // Iterate over columns first
        for (int col = 0; col < matrix[0].length; col++) {
            // Iterate over rows
            for (double[] matrix1 : matrix) 
                System.out.print(matrix1[col] + "\t");
            System.out.println();
        }
    }

    public void displayPatterns(Pattern[] patterns){
        Pattern emptyPattern = new Pattern(new HashSet<>());
        System.out.println(emptyPattern.display(this));
        for (Pattern pattern : patterns) 
            System.out.println(pattern.display(this));     
    }

    // public String retrieveItemValueAsString(int itemIndex){
    //     int attributeIndex = this.getItemAttributeIndexes()[itemIndex];
    //     if(this.getAttributeTypes()[attributeIndex] == Const.TYPE_NUMERICAL){
    //     }
    //     return "";
    // }

    private boolean numericalAttributeExists(){
        for(byte type : this.attributeTypes){
            if(type == Const.TYPE_NUMERICAL)
                return true;
        }
        return false;
    }

    public static void saveToFile(NumericalItemMemory memory, String fileName) {
        try {
            Files.writeString(
                Path.of(fileName),
                memory.toString()
            );
        } catch (IOException e) {
            e.printStackTrace();
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
        return this.itemCounter;
    }

    public int getCoreItemCount(){
        return this.coreItemCount;
    }

    public void addOneToItemCount(){
        this.itemCounter = this.itemCounter + 1 ;
    }
    
    public void substractOneFromItemCount(){
        this.itemCounter = this.itemCounter - 1 ;
    }

    public double[][] getNumericalColumns(){
        return this.numericalColumns;
    }

    public double[][] getExamples(){
        return this.examples;
    }

    public int[] getItemAttributeIndexes() {
        return this.itemAttributeIndexes;
    }

    public int[] getCategoricalItemValueIndexes() {
        return this.categoricalItemValueIndexes;
    }

    public String[] getItemAttributes() {
        return this.itemAttributes;
    }

    public String[] getCategoricalItemValues() {
        return this.categoricalItemValues;
    }

    public byte[] getAttributeTypes(){
        return this.attributeTypes;
    }
    
    public boolean[] getLabels(){
        return this.labels;
    }

    public NumericalItemMemory getNumericalItemMemory(){
        return this.numericalItemMemory;
    }

    public int getItemAttributeIndex(int item) {
        int attributeIndex = item < this.coreItemCount ? this.itemAttributeIndexes[item] : this.numericalItemMemory.getAttributeIndex(item);
        return attributeIndex;
    }

    public static void displayDoubleArray(double[][] array) {
        // Iterate through the outer array
        for (int i = 0; i < array.length; i++) {
            // Check if the inner array has exactly 2 elements
            if (array[i].length == 2) {
                // Display the two elements of the inner array
                System.out.println("Element " + i + ": [" + array[i][0] + ", " + array[i][1] + "]");
            } else {
                // Handle cases where the inner array does not have exactly 2 elements
                System.out.println("Element " + i + ": Invalid size (expected 2 elements)");
            }
        }
    }

}

