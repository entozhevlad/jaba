package com.example.kvdb.apihttp;

import com.example.kvdb.api.DatabaseConfig;
import com.example.kvdb.api.StorageEngine;
import com.example.kvdb.api.TableOptions;
import com.example.kvdb.engine.ArkashaEngine;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.kvdb.spark.SparkClient;

@SpringBootApplication
public class ArkashaApiApplication {

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("lab2")) {
            SparkClient.runLab();
            return;
        }
        SpringApplication.run(ArkashaApiApplication.class, args);
    }
    @Bean
    public StorageEngine arkashaEngine() {
        DatabaseConfig config = new DatabaseConfig("data");
        StorageEngine db = new ArkashaEngine(config);
        if (!db.listTables().contains("test")) {
            TableOptions options = new TableOptions().setWalEnabled(true).setMaxValueSize(-1);
            db.createTable("test", options);
        }
        return db;
    }
}
