package com.example.kvdb.api;

import com.example.kvdb.util.Serializer;

/**
 * Реестр таблиц для регистрации сериализаторов и открытия таблиц с автоматической обработкой типов.
 */
public interface TableRegistry {
    /**
     * Зарегистрировать сериализатор для указанной таблицы.
     * После регистрации можно открывать таблицу как типизированную.
     * @param tableName имя таблицы
     * @param type класс типа значения
     * @param serializer сериализатор для типа значения
     * @param <T> тип значения
     * @throws IllegalArgumentException если таблица не существует или уже зарегистрирована
     */
    <T> void register(String tableName, Class<T> type, Serializer<T> serializer);

    /**
     * Открыть таблицу как типизированную.
     * @param tableName имя таблицы
     * @param type класс типа значения
     * @param <T> тип значения
     * @return Table<T> для доступа к таблице с автоматической сериализацией
     * @throws IllegalArgumentException если для таблицы не зарегистрирован сериализатор данного типа
     */
    <T> Table<T> openTable(String tableName, Class<T> type);
}
