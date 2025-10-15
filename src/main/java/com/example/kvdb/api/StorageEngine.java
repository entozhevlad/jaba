package com.example.kvdb.api;

/**
 * Движок базы данных, объединяющий все основные подсистемы.
 * Главный интерфейс, который реализует Database и предоставляет доступ к менеджеру персистентности, WAL, конфигурации и метрикам.
 */
public interface StorageEngine extends Database {
    /**
     * Получить менеджер персистентности (сохранение/загрузка данных).
     */
    PersistenceManager getPersistenceManager();

    /**
     * Получить журнал предзаписи (WAL) для операций.
     */
    WriteAheadLog getWriteAheadLog();

    /**
     * Получить конфигурацию базы данных.
     */
    DatabaseConfig getConfig();

    /**
     * Получить объект метрик для наблюдения за состоянием базы.
     */
    Metrics getMetrics();
}
