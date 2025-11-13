package com.example.kvdb.spark;

import com.example.kvdb.api.DatabaseConfig;
import com.example.kvdb.api.KeyValueStore;
import com.example.kvdb.engine.ArkashaEngine;
import com.example.kvdb.core.InMemoryKeyValueStore;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.DataTypes;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Лабораторная №2 по Spark, "пожененная" с проектом Arkasha.
 *
 * Идея:
 *  1) Берём реальные данные из таблицы Arkasha (по умолчанию "test") и выгружаем их в CSV.
 *     В CSV пишем:
 *         key         – ключ в KV-базе
 *         value_size  – размер значения в байтах
 *  2) Spark читает этот CSV как "сырые" данные, приводит типы и добавляет вычисляемое поле.
 *  3) Те же данные сохраняем в Parquet и ORC.
 *  4) Сравниваем размер файлов и скорость чтения CSV / Parquet / ORC.
 */
public class SparkClient {

    public static void runLab() {
        String tableName = "test";
        String dataDir = "/tmp/spark_lab2";
        String csvPath = dataDir + "/input.csv";
        String parquetPath = dataDir + "/parquet_output";
        String orcPath = dataDir + "/orc_output";

        try {
            // 1. Готовим CSV из реальной БД Arkasha
            new File(dataDir).mkdirs();
            int exported = exportTableToCsv(tableName, csvPath);

            if (exported == 0) {
                System.out.println("Таблица '" + tableName + "' пуста — сгенерируем синтетические данные для примера.");
                generateCsvData(csvPath, 100_000);
            } else {
                System.out.println("Экспортировано " + exported + " записей из таблицы '" + tableName + "' в " + csvPath);
            }

            // 2. Создаём SparkSession.
            // master не хардкодим: при запуске через spark-submit значение придёт из параметров.
            SparkSession spark = SparkSession.builder()
                    .appName("SparkLab2-Arkasha")
                    .getOrCreate();

            // 3. Читаем сырые данные CSV в DataFrame
            Dataset<Row> df = spark.read()
                    .option("header", "true")
                    .option("inferSchema", "false")
                    .option("sep", ";")
                    .csv(csvPath);

            // 4. Простое преобразование:
            //    - приводим value_size к int
            //    - добавляем колонку is_large (true, если значение больше 1 КБ)
            df = df.withColumn("value_size", functions.col("value_size").cast(DataTypes.IntegerType))
                    .withColumn("is_large", functions.col("value_size").gt(1024));

            df.printSchema();
            System.out.println("Пример строк после преобразования:");
            df.show(5, false);
            deleteIfExists(new File(parquetPath));
            deleteIfExists(new File(orcPath));
            // 5. Записываем DataFrame в Parquet и ORC
            df.write().mode("overwrite").parquet(parquetPath);
            df.write().mode("overwrite").orc(orcPath);

            // 6. Оцениваем эффективность хранения: размер файлов
            long csvSize = new File(csvPath).length();
            long parquetSize = getDirectorySize(new File(parquetPath));
            long orcSize = getDirectorySize(new File(orcPath));

            System.out.printf("CSV file size: %d bytes (%.2f MB)%n", csvSize, csvSize / (1024.0 * 1024.0));
            System.out.printf("Parquet file size: %d bytes (%.2f MB)%n", parquetSize, parquetSize / (1024.0 * 1024.0));
            System.out.printf("ORC file size: %d bytes (%.2f MB)%n", orcSize, orcSize / (1024.0 * 1024.0));

            // 7. Замеряем скорость чтения (простое count() для каждого формата)
            int runs = 3;

            // Прогрев
            spark.read().option("header", "true").option("inferSchema", "false").option("sep", ";").csv(csvPath).count();
            spark.read().parquet(parquetPath).count();
            spark.read().orc(orcPath).count();

            long csvTotalTime = 0;
            long parquetTotalTime = 0;
            long orcTotalTime = 0;

            for (int i = 0; i < runs; i++) {
                long t0, t1;

                t0 = System.nanoTime();
                spark.read().option("header", "true").option("inferSchema", "false").option("sep", ";").csv(csvPath).count();
                t1 = System.nanoTime();
                csvTotalTime += (t1 - t0);

                t0 = System.nanoTime();
                spark.read().parquet(parquetPath).count();
                t1 = System.nanoTime();
                parquetTotalTime += (t1 - t0);

                t0 = System.nanoTime();
                spark.read().orc(orcPath).count();
                t1 = System.nanoTime();
                orcTotalTime += (t1 - t0);
            }

            double csvAvgMs = csvTotalTime / (runs * 1e6);
            double parquetAvgMs = parquetTotalTime / (runs * 1e6);
            double orcAvgMs = orcTotalTime / (runs * 1e6);

            System.out.printf("Average read time CSV    : %.2f ms%n", csvAvgMs);
            System.out.printf("Average read time Parquet: %.2f ms%n", parquetAvgMs);
            System.out.printf("Average read time ORC    : %.2f ms%n", orcAvgMs);

            // 8. Завершаем SparkSession
            spark.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Экспортирует данные из таблицы Arkasha в CSV.
     *
     * Формат:
     *   key;value_size
     *
     * Возвращает количество записей, выгруженных в файл.
     */
    private static int exportTableToCsv(String tableName, String filePath) throws IOException {
        DatabaseConfig config = new DatabaseConfig("data");
        ArkashaEngine db = new ArkashaEngine(config);

        try {
            if (!db.listTables().contains(tableName)) {
                System.out.println("Таблица '" + tableName + "' не найдена. Экспорт невозможен.");
                return 0;
            }

            @SuppressWarnings("unchecked")
            KeyValueStore<byte[]> table = db.openTable(tableName);
            InMemoryKeyValueStore store = (InMemoryKeyValueStore) table;

            List<String> keys = store.keys();
            if (keys.isEmpty()) {
                System.out.println("Таблица '" + tableName + "' существует, но пуста.");
                return 0;
            }

            File outFile = new File(filePath);
            File parent = outFile.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Не удалось создать директорию " + parent);
            }

            int count = 0;
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
                writer.write("key;value_size");
                writer.newLine();

                for (String key : keys) {
                    byte[] value = table.get(key);
                    int size = (value == null ? 0 : value.length);

                    // на всякий случай экранируем перевод строки и разделитель
                    String safeKey = key
                            .replace("\n", " ")
                            .replace("\r", " ")
                            .replace(";", "_");

                    writer.write(safeKey);
                    writer.write(";");
                    writer.write(Integer.toString(size));
                    writer.newLine();
                    count++;
                }
            }

            return count;
        } finally {
            db.close();
        }
    }

    /**
     * Фоллбек: генерация синтетического CSV,
     * если в БД пока нет данных.
     */
    private static void generateCsvData(String filePath, int numRows) throws IOException {
        File outFile = new File(filePath);
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Не удалось создать директорию " + parent);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFile))) {
            writer.write("key;value_size");
            writer.newLine();
            for (int i = 1; i <= numRows; i++) {
                String key = "synthetic_" + i;
                int size = (int) (Math.random() * 4096); // до 4 КБ
                writer.write(key);
                writer.write(";");
                writer.write(Integer.toString(size));
                writer.newLine();
            }
        }
    }

    /**
     * Рекурсивно считает размер директории (или файла).
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
    private static void deleteIfExists(File f) {
        if (!f.exists()) return;
        if (f.isFile()) {
            f.delete();
            return;
        }
        File[] children = f.listFiles();
        if (children != null) {
            for (File c : children) {
                deleteIfExists(c);
            }
        }
        f.delete();
    }

}
