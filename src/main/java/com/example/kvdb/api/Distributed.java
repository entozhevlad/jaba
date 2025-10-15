package com.example.kvdb.api;

/**
 * Интерфейс для расширений: репликация и шардинг (не реализован в данной версии).
 */
public interface Distributed {
    /**
     * (Будущая реализация) Реплицировать запись на другие узлы.
     * @param tableName имя таблицы
     * @param key ключ записи
     * @param value значение записи
     */
    void replicate(String tableName, String key, byte[] value);

    /**
     * (Будущая реализация) Добавить новый узел для шардинга/репликации.
     * @param nodeId идентификатор узла
     */
    void addNode(String nodeId);
}
