package smhdd.main;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;


public class AggregateResults {

//	private static final Path INPUT_PATH = Paths.get("results", "simulation_results.csv");
//	private static final Path OUTPUT_PATH = Paths.get("results", "aggregated_results.csv");

    public static void main(String[] args) throws IOException {
        Path inputPath = Paths.get("results", "simulation-results_qg_width-2_binary.csv");
        Path outputPath = Paths.get("results", "aggregated-result_qg_width-2_binary.csv");

        Map<String, Stats> statsByAlgorithm = computeStatsByAlgorithm(inputPath);
        writeStatsCsv(statsByAlgorithm, outputPath);
    }

    /**
     * Reads the input CSV and aggregates results by algorithm.
     */
    private static Map<String, Stats> computeStatsByAlgorithm(Path inputPath) throws IOException {
        Map<String, Stats> statsByAlg = new LinkedHashMap<>();

        try (Stream<String> lines = Files.lines(inputPath, StandardCharsets.UTF_8)) {
            Iterator<String> it = lines.iterator();
            if (!it.hasNext()) {
                throw new IllegalArgumentException("Input file is empty: " + inputPath);
            }

            // Read header
            String headerLine = it.next();
            String[] headerCols = headerLine.split(",");

            int idxAlgorithm = indexOf(headerCols, "algorithm");
            int idxQuality   = indexOf(headerCols, "quality");
            int idxSupp      = indexOf(headerCols, "supp+");
            int idxTime      = indexOf(headerCols, "time");

            if (idxAlgorithm < 0 || idxQuality < 0 || idxSupp < 0 || idxTime < 0) {
                throw new IllegalArgumentException(
                    "Required columns not found. Expected at least: algorithm, quality, supp+, time"
                );
            }

            // Process each data line
            while (it.hasNext()) {
                String line = it.next().trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split(",");
                if (parts.length <= Math.max(Math.max(idxAlgorithm, idxQuality),
                                             Math.max(idxSupp, idxTime))) {
                    // Malformed line, skip or handle as you wish
                    continue;
                }

                String algorithm = parts[idxAlgorithm];
                double quality   = Double.parseDouble(parts[idxQuality]);
                double supp      = Double.parseDouble(parts[idxSupp]);
                double time      = Double.parseDouble(parts[idxTime]);

                Stats stats = statsByAlg.computeIfAbsent(algorithm, k -> new Stats());
                stats.add(quality, supp, time);
            }
        }

        return statsByAlg;
    }

    /**
     * Writes the aggregated CSV.
     */
    private static void writeStatsCsv(Map<String, Stats> statsByAlg, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write("algorithm,average_quality,standard_deviation,average_supp+,average_time");
            writer.newLine();

            for (Map.Entry<String, Stats> entry : statsByAlg.entrySet()) {
                String algorithm = entry.getKey();
                Stats s = entry.getValue();

                double avgQuality = s.getAverageQuality();
                double stdDev     = s.getStdDevQuality(); // sample std-dev (N-1)
                double avgSupp    = s.getAverageSupp();
                double avgTime    = s.getAverageTime();

                // Locale.US to guarantee "." as decimal separator in CSV
                String line = String.format(Locale.US,
                        "%s,%.2f,%.2f,%.2f,%.2f",
                        algorithm, avgQuality, stdDev, avgSupp, avgTime);

                writer.write(line);
                writer.newLine();
            }
        }
    }


    /**
     * Helper: find column index by exact name.
     */
    private static int indexOf(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (target.equals(array[i].trim())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Accumulator for statistics of one algorithm.
     */
    private static class Stats {
        private double sumQuality = 0.0;
        private double sumQualitySq = 0.0; // for standard deviation
        private double sumSupp = 0.0;
        private double sumTime = 0.0;
        private int count = 0;

        void add(double quality, double supp, double time) {
            sumQuality   += quality;
            sumQualitySq += quality * quality;
            sumSupp      += supp;
            sumTime      += time;
            count++;
        }

        double getAverageQuality() {
            return count == 0 ? Double.NaN : sumQuality / count;
        }

        /**
         * Sample standard deviation (N-1) of the quality column.
         * Change denominator to 'count' if you want population std-dev.
         */
        double getStdDevQuality() {
            if (count < 2) {
                return 0.0;
            }
            double mean = sumQuality / count;
            double variance = (sumQualitySq - count * mean * mean) / (count - 1);
            if (variance < 0) {
                // Can happen due to floating point rounding
                variance = 0;
            }
            return Math.sqrt(variance);
        }

        double getAverageSupp() {
            return count == 0 ? Double.NaN : sumSupp / count;
        }

        double getAverageTime() {
            return count == 0 ? Double.NaN : sumTime / count;
        }
    }
}
