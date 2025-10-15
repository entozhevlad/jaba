package com.example.kvdb.api;

/**
 * Обёртка над KeyValueStore, автоматически сериализующая и десериализующая значения.
 * @param <T> тип значения, представляемый таблицей
 */
public interface Table<T> extends KeyValueStore<T> {
    // Использует методы KeyValueStore<T> для операций.
}
