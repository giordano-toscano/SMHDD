package smhdd.main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import smhdd.data.Pattern;
import smhdd.data.Const;
import smhdd.data.D;
import smhdd.evolutionary.Evaluation;

public class Simulation {

    public static void main(String[] args){

        try {
            
            // quantity of returned subgroups
            byte k = 10;
            // setting the maximum number of subgroups that are similar to another 
            Pattern.setMaxSimilarQuantity((byte) 2);
            // setting the evaluation metric
            //String evaluationMetric = Const.METRIC_WRACC_NORMALIZED;
			String evaluationMetric = Const.METRIC_QG;
            // setting the similarity measure
            byte similarityMeasure = Const.SIMILARIDADE_JACCARD; 
            // setting threshold for determining when two subgroups are considered similar to each other
            float minSimilarity = 0.90f; 
			// time limit in seconds for the execution of the algorithm (-1 for no limit)
			//int timeLimit = -1;
            // setting the type of each attribute
            // byte[] attributeTypes = {Const.TYPE_CATEGORICAL, Const.TYPE_CATEGORICAL, Const.TYPE_NUMERICAL};

			byte repetitionNumber = 10;

			String discretizationType = "freq"; // "width" or "freq"
			int numBins = 2; // number of bins for discretization (used only if discretizationType is "width")
			String representation = "nominal"; // "binary"

			//float rate = 0.05f; // 
			float[] rates = {0.05f, 0.1f, 0.2f};

            System.out.println("\nRunning SMHDD...");
			run(k, repetitionNumber, evaluationMetric, similarityMeasure, minSimilarity, rates, discretizationType, numBins, representation);
            
        } catch (IOException ex) {
        }
    }
	
	
	
	public static void run(byte k, byte repetitionNumber, String evaluationMetric, byte similarityMeasure,
		float minSimilarity, float[] rates, String discretizationType, int numBins, String representation) throws IOException {

		File directory = new File("datasets/");
		File[] files = directory.listFiles((d, name) -> name.toLowerCase().endsWith(".csv"));
		if (files == null) {
			System.out.println("No CSV files found in the datasets directory.");
			return;
		}

		String fileName = getResultFileName(evaluationMetric, discretizationType, Integer.toString(numBins), representation);
		Path OUTPUT_PATH = Paths.get("results", fileName);
		// Make sure "results" directory exists
		if (OUTPUT_PATH.getParent() != null) {
			Files.createDirectories(OUTPUT_PATH.getParent());
		}
		boolean newFile = Files.notExists(OUTPUT_PATH);

		try (BufferedWriter writer = Files.newBufferedWriter(OUTPUT_PATH, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

			if (newFile) {
				writer.write("algorithm,dataset,repetition,quality,supp+,time");
				writer.newLine();
			}

			for (File file : files){

				String datasetPath = file.getAbsolutePath();
				System.out.println("Dataset: " + file.getName());

				for(float rate : rates ){

					for (int rep = 1; rep <= repetitionNumber; rep++) {

						D dataset = new D(datasetPath, ",");

						Const.random = new Random(Const.SEEDS[rep]);

						long initialTime = System.currentTimeMillis();
						Pattern[] topk = SMHDD.run(dataset, k, evaluationMetric, similarityMeasure, minSimilarity, rate,  discretizationType, numBins, representation);
						double executionTime = (System.currentTimeMillis() - initialTime) / 1000.0;
			
						String datasetName = dataset.getName();
						double averageQuality = Evaluation.calculateAverageQuality(topk, k);
						double globalPositiveSupport = Evaluation.globalPositiveSupport(topk, k, dataset);

						StringBuilder algorithmName = new StringBuilder();
						algorithmName.append("SMHDD").append("_").append(Float.toString(rate));
						writeRow(writer, algorithmName.toString(), datasetName, rep, averageQuality, globalPositiveSupport, executionTime);
						writer.flush();
					} // end of repetition
				}
			} // end of dataset
		}
		System.out.println("Simulação concluída");
	}

    public static String getResultFileName(String evaluationMetric, String discretizationType, String numBins, String representation) {
		StringBuilder resultFileName = new StringBuilder();
		resultFileName.append("simulation-results_").append(evaluationMetric.toLowerCase()).append("_").append(discretizationType).append("-").append(numBins).append("_").append(representation).append(".csv");
		return resultFileName.toString();
    }

	private static void writeRow(BufferedWriter writer, String algorithmName, String databaseName, int repetition,
			double qualityMeasure, double suppPlus, double executionTime) throws IOException {

		// Use Locale.US to fix the decimal separator as '.' for CSV
		String wraccStr = Double.toString(qualityMeasure);
		String suppPlusStr = Double.toString(suppPlus);
		String line = new StringBuilder().append(algorithmName).append(',').append(databaseName).append(',')
				.append(repetition).append(',').append(wraccStr).append(',').append(suppPlusStr).append(',')
				.append(executionTime).toString();

		writer.write(line);
		writer.newLine();
	}

}
