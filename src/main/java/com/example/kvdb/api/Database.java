package com.example.kvdb.api;

/**
 * Интерфейс управления таблицами: создание, открытие, удаление, перечисление и закрытие.
 */
public interface Database {
    /**
     * Создать новую таблицу с заданным именем и опциями.
     * @param name имя таблицы
     * @param options параметры таблицы
     * @throws IllegalArgumentException если таблица с таким именем уже существует
     */
    void createTable(String name, TableOptions options);

    /**
     * Открыть существующую таблицу по имени.
     * @param name имя таблицы
     * @return KeyValueStore для доступа к данным таблицы
     * @throws IllegalArgumentException если таблица не существует
     */
    KeyValueStore<byte[]> openTable(String name);

    /**
     * Удалить таблицу по имени со всеми данными.
     * @param name имя таблицы
     * @throws IllegalArgumentException если таблица не существует
     */
    void dropTable(String name);

    /**
     * Получить список всех таблиц.
     * @return список имен существующих таблиц
     */
    java.util.List<String> listTables();

    /**
     * Закрыть базу данных, освободив ресурсы и записав данные на диск.
     */
    void close();
}
