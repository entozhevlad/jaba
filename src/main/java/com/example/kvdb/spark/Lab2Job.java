package com.example.kvdb.spark;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.SaveMode;

import static org.apache.spark.sql.functions.upper;
import static org.apache.spark.sql.functions.col;

public class Lab2Job {

    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder()
                .appName("ArkashaLab2")
                .getOrCreate();

        String path  = "data";
        String table = "test";

        Dataset<Row> df = spark.read()
                .format("com.example.kvdb.spark.source.ArkashaDataSource")
                .option("path", path)
                .option("table", table)
                .load();

        System.out.println("=== RAW DATA FROM ARKASHA ===");
        df.printSchema();
        df.show(false);

        Dataset<Row> transformed = df
                .withColumn("upper_key", upper(col("key")))
                .select("key", "upper_key", "value_size", "is_large");

        System.out.println("=== TRANSFORMED DATA ===");
        transformed.show(false);

        String parquetPath = "lab2_parquet";
        String orcPath     = "lab2_orc";

        transformed.write()
                .mode(SaveMode.Overwrite)
                .parquet(parquetPath);

        transformed.write()
                .mode(SaveMode.Overwrite)
                .orc(orcPath);

        Dataset<Row> p = spark.read().parquet(parquetPath);
        Dataset<Row> o = spark.read().orc(orcPath);

        long t1 = System.nanoTime();
        long pc = p.count();
        long t2 = System.nanoTime();

        long t3 = System.nanoTime();
        long oc = o.count();
        long t4 = System.nanoTime();

        System.out.println("Parquet count = " + pc + ", read time = " + (t2 - t1) / 1e6 + " ms");
        System.out.println("ORC     count = " + oc + ", read time = " + (t4 - t3) / 1e6 + " ms");

        spark.stop();
    }
}
