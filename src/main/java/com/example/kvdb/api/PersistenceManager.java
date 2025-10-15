package com.example.kvdb.api;

/**
 * Управляет сохранением данных на диск.
 */
public interface PersistenceManager {
    /**
     * Сбросить текущие данные всех таблиц на диск (сделать снапшот).
     */
    void flush();

    /**
     * Загрузить данные с диска при старте (восстановить снапшот).
     */
    void load();
}
