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
    private String[] itemAttributes;
    private String[] categoricalItemValues; 
    private int[] itemAttributeIndexes; 
    private int[] categoricalItemValueIndexes; 
    private NumericalItemMemory numericalItemMemory;

    // Counters
    private int itemCount;
    private int attributeCount;
    private int exampleCount;
    private int positiveExampleCount;
    private int negativeExampleCount;

    // Constructors
    public D(String path, String delimiter, String targetValue, byte datasetType, int numBins) throws IOException{
        String[][] examplesStr = this.loadFile(path, delimiter, datasetType);
        this.generateItems(examplesStr, numBins);
        this.convertExamplesFromStrToDouble(examplesStr);
        this.extractLabels(examplesStr, targetValue);
        this.extractNumericalColumns();
    }
    public D(String path, String delimiter, String targetValue) throws IOException{
        this(path, delimiter, targetValue, Const.DATASET_TYPE_CATEGORICAL, Const.DEFAULT_NUM_BINS);
    }
    public D(String path, String delimiter) throws IOException{
        this(path, delimiter, "p", Const.DATASET_TYPE_CATEGORICAL , Const.DEFAULT_NUM_BINS);
    }
    public D(String path, String delimiter, int numBins) throws IOException{
        this(path, delimiter, "p", Const.DATASET_TYPE_AUTO_DETECT, numBins);
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



    private void generateItems(String[][] examplesStr, int numBins){
        this.itemCount = 0;
                
        @SuppressWarnings("unchecked")
        HashSet<Object>[] distinctAttributeValues = new HashSet[this.attributeCount]; // stores the distinct values of each attribute

        for(int i = 0; i < this.attributeCount; i++){
            HashSet<Object> distinctValuesSingleAttribute = new HashSet<>();         // stores distinct values of a single attribute
            if(this.attributeTypes[i] == Const.TYPE_CATEGORICAL) // categorical attribute
                distinctValuesSingleAttribute.addAll(this.gatherCategoricalColumnValues(examplesStr, i));
            else                                            // numerical attribute
                distinctValuesSingleAttribute.addAll(this.transformNumericalColumnIntoIntervals(examplesStr, i, numBins));
            this.itemCount += distinctValuesSingleAttribute.size();
            distinctAttributeValues[i] = distinctValuesSingleAttribute; // adds list of distinct values of attribute i in the i-th position of the "distinctAttributeValues" array
        }
        
        // creates 2 arrays to store attributes and values in their original format (String)
        this.itemAttributes = new String[itemCount];
        this.categoricalItemValues = new String[itemCount];
        // creates another 2 arrays to store attributes and values mapped to integer values
        this.itemAttributeIndexes = new int[itemCount];
        this.categoricalItemValueIndexes = new int[itemCount];

        this.numericalItemMemory = new NumericalItemMemory(itemCount);

        int itemIndex = 0;
        for(int attributeIndex = 0; attributeIndex < this.attributeCount; attributeIndex++){
            HashSet<Object> distinctValues = distinctAttributeValues[attributeIndex];
            int valueIndex = 0;
            for(Object value : distinctValues){
                if(this.attributeTypes[attributeIndex] == Const.TYPE_CATEGORICAL){
                    categoricalItemValues[itemIndex] =  (String) value;
                    categoricalItemValueIndexes[itemIndex] = valueIndex;
                }else{
                    double[] interval = (double[]) value;
                    this.numericalItemMemory.put(itemIndex, new NumericalItem(attributeIndex, interval[0], interval[1]));
                }

                itemAttributes[itemIndex] = this.variableNames[attributeIndex];
                itemAttributeIndexes[itemIndex] = attributeIndex;

                itemIndex++;
                valueIndex++;
            }
        }
    }

    private List<double[]> transformNumericalColumnIntoIntervals(String[][] examplesStr, int attributeIndex, int numBins){
        String[] array = new String[this.exampleCount]; 
        for(int i = 0; i < this.exampleCount; i++)
            array[i] = examplesStr[i][attributeIndex];
        double[][] result = D.equalFrequencyBins(array, numBins);
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

    private static double[][] equalFrequencyBins(String[] stringArray, int numBins) {

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

    public static double[][] equalWidthDiscretization(double[] array, int numBins) {
        int n = array.length;
        if (n == 0 || numBins <= 0) throw new IllegalArgumentException("Invalid input parameters");

        // Find min and max in a single pass (O(n))
        double min = array[0], max = array[0];
        for (int i = 1; i < n; i++) {
            if (array[i] < min) min = array[i];
            if (array[i] > max) max = array[i];
        }

        double width = (max - min) / numBins;
        if (width == 0) return new double[][]{{min, max}}; // All values are the same

        double[][] intervals = new double[numBins][2];

        // Compute bin intervals
        for (int i = 0; i < numBins; i++) {
            intervals[i][0] = min + i * width;          // Lower bound
            intervals[i][1] = min + (i + 1) * width;    // Upper bound
        }

        return intervals;
    }

    public static void imprimirRegras(D dataset, Pattern[] patterns){
        Pattern emptyPattern = new Pattern(new HashSet<>());
        System.out.println(emptyPattern.display(dataset));
        for (Pattern pattern : patterns) 
            System.out.println(pattern.display(dataset));     
    }

    public String retrieveItemValueAsString(int itemIndex){
        int attributeIndex = this.getItemAttributeIndexes()[itemIndex];
        if(this.getAttributeTypes()[attributeIndex] == Const.TYPE_NUMERICAL){
        }
        return "";
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

    // Main method
    public static void main(String[] args) throws IOException {
        String directory = "datasets/";
        String file = "alon.csv";
        String filepath = directory+file;
        D dataset  = new D(filepath, ",");

        // System.out.println("PRINT nomeVariaveis:");
        // for (String row : dataset.getVariableNames()) {
        //     System.out.print(row + "\t");
        //     System.out.println();
        // }

        // System.out.println("\nPRINT itemAtributosStr");
        // for (String cell : dataset.getItemAttributes()) {
        //     System.out.print(cell + "\t");
        // }
        // System.out.println("\nPRINT itemAtributosInt");
        // for (int cell : dataset.getItemAttributeIndexes()) {
        //     System.out.print(cell + "\t");
        // }

        // System.out.println("\nPRINT itemValuesObj");
        // for (Object cell : dataset.getCategoricalItemValues()) {
        //     System.out.print(cell + "\t");
        // }
        // System.out.println("\nPRINT itemValuesInt");
        // for (int cell : dataset.getCategoricalItemValueIndexes()) {
        //     System.out.print(cell + "\t");
        // }
        // System.out.println("\nPRINT NumericalValues");
        // System.out.println(dataset.getNumericalItemMemory());

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
        double[] doubleArray = {
            -0.530,
            0.858,
            0.510,
            0.800,
           -0.680,
           -0.290,
            3.020,
            1.150,
            0.000,
            0.490,
            1.160,
            0.350,
           -0.430,
            2.300,
           -2.230,
            0.390,
           -1.460,
           -1.220,
           -1.990,
           -1.690,
           -1.340,
            1.160,
           -0.100,
            2.120,
            1.750,
            0.570,
           -2.080,
           -1.940,
           -2.370,
           -2.570,
           -3.760,
            1.980,
           -2.320,
           -2.830,
            1.320,
            0.620,
            1.640,
           -0.350,
            0.820,
           -0.870,
           -0.800,
           -0.640,
            0.732,
            1.530,
            1.050,
            1.440,
           -0.520,
            0.680,
           -1.630,
           -0.320,
           -1.860,
           -1.740,
           -2.530,
            0.880,
           -0.850,
            0.290,
            0.470,
            0.540,
            0.560,
            1.160,
            0.360,
           -0.890,
           -1.570,
           -0.690,
           -1.700,
            1.080,
           -0.540,
           -0.770,
            1.230,
            0.860,
            1.080,
            0.970,
            0.700,
            2.310,
            0.000,
           -1.680,
            2.540,
            1.530,
            2.060,
            1.790,
           -1.770,
           -1.700,
           -0.011,
           -1.300,
            0.550
        };
        Arrays.sort(doubleArray);
        displayDoubleArray(equalFrequencyDiscretization(doubleArray, 2));
        displayDoubleArray(equalFrequencyBins(doubleArray, 2));

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

