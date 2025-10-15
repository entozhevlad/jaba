package com.example.kvdb.api;

/**
 * Конфигурация базы данных.
 * Определяет основные параметры работы движка.
 */
public class DatabaseConfig {
    private final String dataPath;
    private final int cacheSize;
    // (Можно добавить параметры fsync и др. при необходимости)

    public DatabaseConfig(String dataPath) {
        this(dataPath, 0);
    }

    public DatabaseConfig(String dataPath, int cacheSize) {
        if (dataPath == null) {
            throw new IllegalArgumentException("Data path must not be null");
        }
        this.dataPath = dataPath;
        this.cacheSize = cacheSize;
    }

    public String getDataPath() {
        return dataPath;
    }

    public int getCacheSize() {
        return cacheSize;
    }
}
