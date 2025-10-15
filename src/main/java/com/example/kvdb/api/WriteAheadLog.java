package com.example.kvdb.api;

/**
 * Журнал предзаписи (WAL) для фиксации изменений.
 */
public interface WriteAheadLog {
    /**
     * Добавить запись в лог.
     * @param record запись WAL (операция)
     */
    void append(LogRecord record);

    /**
     * Зафиксировать (fsync) все записанные изменения на диск.
     */
    void sync();

    /**
     * Прочитать журнал и восстановить состояние системы после сбоя.
     */
    void replay();

    /**
     * Запись в WAL (внутреннее представление операции).
     */
    class LogRecord {
        public enum Type { PUT, DELETE }
        public final Type type;
        public final String tableName;
        public final String key;
        public final byte[] value;

        private LogRecord(Type type, String tableName, String key, byte[] value) {
            this.type = type;
            this.tableName = tableName;
            this.key = key;
            this.value = value;
        }

        public static LogRecord put(String tableName, String key, byte[] value) {
            return new LogRecord(Type.PUT, tableName, key, value);
        }

        public static LogRecord delete(String tableName, String key) {
            return new LogRecord(Type.DELETE, tableName, key, null);
        }
    }
}
