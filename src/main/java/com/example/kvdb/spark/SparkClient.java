package com.example.kvdb.spark;

import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SparkClient {
    public static void runLab() {
        try {
            // 1. Prepare sample data: generate a CSV file with 5-10 columns and >=100,000 rows
            String dataDir = "data/spark_lab2";
            String csvPath = dataDir + "/input.csv";
            generateCsvData(csvPath, 100000);  // generate 100k rows of sample CSV data

            // 2. Initialize SparkSession (local mode)
            SparkSession spark = SparkSession.builder()
                    .appName("SparkLab2")
                    .master("local[*]")      // run locally using all cores
                    .getOrCreate();
            // Reduce log verbosity
            spark.sparkContext().setLogLevel("WARN");

            // 3. Read the raw CSV data (as strings) into a DataFrame
            Dataset<Row> df = spark.read()
                    .option("header", "true")       // first line is header
                    .option("inferSchema", "false") // treat all columns as strings initially
                    .csv(csvPath);

            // 4. Simple transformation: cast columns to proper types and add a new derived column
            df = df.withColumn("id", functions.col("id").cast(DataTypes.IntegerType))
                    .withColumn("col1", functions.col("col1").cast(DataTypes.IntegerType))
                    .withColumn("col2", functions.col("col2").cast(DataTypes.DoubleType))
                    .withColumn("col5", functions.col("col5").cast(DataTypes.BooleanType))
                    .withColumn("double_col2", functions.col("col2").multiply(functions.lit(2.0)));

            // Now df schema is updated: id, col1 as int; col2 as double; col3, col4 as string; col5 as boolean; plus new double_col2.

            // 5. Write the transformed data in Parquet and ORC formats
            String parquetPath = dataDir + "/output.parquet";
            String orcPath = dataDir + "/output.orc";
            df.write().mode("overwrite").parquet(parquetPath);
            df.write().mode("overwrite").orc(orcPath);

            // 6. Evaluate storage efficiency: file sizes before and after
            long csvSize = new File(csvPath).length();
            long parquetSize = getDirectorySize(new File(parquetPath));
            long orcSize = getDirectorySize(new File(orcPath));

            // 7. Measure read times for each format (3 runs each, then average)
            int runs = 3;
            // Warm up (optional): read each format once before timing to avoid first-run overhead (optional, can skip)
            spark.read().option("header", "true").option("inferSchema", "false").csv(csvPath).count();
            spark.read().parquet(parquetPath).count();
            spark.read().orc(orcPath).count();
            // Now measure multiple runs
            long csvTotalTime = 0, parquetTotalTime = 0, orcTotalTime = 0;
            for (int i = 1; i <= runs; i++) {
                // CSV read + count
                long t0 = System.nanoTime();
                spark.read().option("header", "true").option("inferSchema", "false")
                        .csv(csvPath).count();
                csvTotalTime += (System.nanoTime() - t0);
                // Parquet read + count
                t0 = System.nanoTime();
                spark.read().parquet(parquetPath).count();
                parquetTotalTime += (System.nanoTime() - t0);
                // ORC read + count
                t0 = System.nanoTime();
                spark.read().orc(orcPath).count();
                orcTotalTime += (System.nanoTime() - t0);
            }
            double csvAvgMs = csvTotalTime / (runs * 1e6);
            double parquetAvgMs = parquetTotalTime / (runs * 1e6);
            double orcAvgMs = orcTotalTime / (runs * 1e6);

            // 8. Print out the results
            System.out.printf("Original CSV file size: %d bytes (%.2f MB)%n", csvSize, csvSize / (1024.0 * 1024.0));
            System.out.printf("Parquet file size: %d bytes (%.2f MB)%n", parquetSize, parquetSize / (1024.0 * 1024.0));
            System.out.printf("ORC file size: %d bytes (%.2f MB)%n", orcSize, orcSize / (1024.0 * 1024.0));
            System.out.println();
            System.out.printf("Average CSV read time: %.2f ms%n", csvAvgMs);
            System.out.printf("Average Parquet read time: %.2f ms%n", parquetAvgMs);
            System.out.printf("Average ORC read time: %.2f ms%n", orcAvgMs);

            // 9. Stop Spark session
            spark.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Generates a CSV file with the specified number of rows.
     * Columns: id, col1, col2, col3, col4, col5
     *  - id: integer (as string in file)
     *  - col1: random integer (0-999)
     *  - col2: random double (0-100) with one decimal
     *  - col3: random category string from set {"A","B","C","D","E"}
     *  - col4: random category string from set {"X","Y","Z"}
     *  - col5: random boolean ("true"/"false")
     */
    private static void generateCsvData(String filePath, int numRows) throws IOException {
        File dir = new File(filePath).getParentFile();
        if (dir != null) {
            dir.mkdirs();  // create directory if not exists
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write header
            writer.write("id,col1,col2,col3,col4,col5");
            writer.newLine();
            // Define possible categorical values
            String[] col3Values = {"A", "B", "C", "D", "E"};
            String[] col4Values = {"X", "Y", "Z"};
            for (int i = 1; i <= numRows; i++) {
                int col1Val = (int) (Math.random() * 1000);            // random int [0, 1000)
                double col2Val = Math.random() * 100.0;                // random double [0, 100)
                String col3Val = col3Values[(int) (Math.random() * col3Values.length)];
                String col4Val = col4Values[(int) (Math.random() * col4Values.length)];
                String col5Val = Math.random() < 0.5 ? "true" : "false";
                // Write the row as CSV
                writer.write(i + "," + col1Val + ","
                        + String.format("%.1f", col2Val) + ","
                        + col3Val + "," + col4Val + "," + col5Val);
                writer.newLine();
            }
        }
    }

    /**
     * Recursively calculates total size of a directory (or returns file length if it's a file).
     */
    private static long getDirectorySize(File file) {
        if (file.isFile()) {
            return file.length();
        }
        long total = 0;
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                total += getDirectorySize(f);
            }
        }
        return total;
    }
}
