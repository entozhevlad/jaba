package com.example.kvdb.api;

/**
 * Минимальный контракт CRUD-операций key-value хранилища.
 * @param <V> тип значения, хранимого в базе (в необработанном виде обычно byte[]).
 */
public interface KeyValueStore<V> {
    /**
     * Сохранить значение по заданному ключу.
     * @param key ключ
     * @param value значение
     */
    void put(String key, V value);

    /**
     * Получить значение по ключу.
     * @param key ключ
     * @return значение или {@code null}, если ключ не найден
     */
    V get(String key);

    /**
     * Удалить значение по ключу.
     * @param key ключ
     */
    void delete(String key);

    /**
     * Проверить существование ключа.
     * @param key ключ
     * @return {@code true}, если ключ присутствует
     */
    boolean containsKey(String key);
}
