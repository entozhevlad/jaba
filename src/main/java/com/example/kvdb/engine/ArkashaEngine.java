package com.example.kvdb.engine;

import com.example.kvdb.api.*;
import com.example.kvdb.core.InMemoryKeyValueStore;
import com.example.kvdb.core.TableImpl;
import com.example.kvdb.util.Serializer;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Основной класс движка базы данных "Аркаша".
 * Реализует интерфейсы StorageEngine, TableRegistry, Distributed.
 * Содержит в памяти все таблицы и обеспечивает потокобезопасный доступ, персистентность и WAL.
 */
public class ArkashaEngine implements StorageEngine, TableRegistry, Distributed {
    private final DatabaseConfig config;
    private final ArkashaMetrics metrics;
    private final ArkashaPersistenceManager persistenceManager;
    private final ArkashaWriteAheadLog writeAheadLog;
    // Отображение имен таблиц на хранилища данных
    private final Map<String, InMemoryKeyValueStore> tables;
    // Реестр сериализаторов для TableRegistry
    private final Map<String, Serializer<?>> serializers;
    private boolean closed = false;

    public ArkashaEngine(DatabaseConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("DatabaseConfig must not be null");
        }
        this.config = config;
        this.metrics = new ArkashaMetrics();
        this.tables = new HashMap<>();
        this.serializers = new HashMap<>();
        // Ensure data directory exists
        File dataDir = new File(config.getDataPath());
        dataDir.mkdirs();
        // Initialize WAL and PersistenceManager
        this.writeAheadLog = new ArkashaWriteAheadLog(this, new File(dataDir, "arkasha.wal"));
        this.persistenceManager = new ArkashaPersistenceManager(this, new File(dataDir, "arkasha.dat"));
        // Load existing data (snapshot) and replay WAL if needed
        writeAheadLog.setActive(false);
        persistenceManager.load();
        int replayed = writeAheadLog.replay(this);
        writeAheadLog.setActive(true);
        if (replayed > 0) {
            persistenceManager.flush();
        }
    }

    @Override
    public synchronized void createTable(String name, TableOptions options) {
        if (closed) {
            throw new IllegalStateException("Database is closed");
        }
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Table name must not be null or empty");
        }
        if (tables.containsKey(name)) {
            throw new IllegalArgumentException("Table '" + name + "' already exists");
        }
        if (options == null) {
            options = new TableOptions();
        }
        InMemoryKeyValueStore store = new InMemoryKeyValueStore(name, options, writeAheadLog, metrics);
        tables.put(name, store);
    }

    @Override
    public synchronized KeyValueStore<byte[]> openTable(String name) {
        if (closed) {
            throw new IllegalStateException("Database is closed");
        }
        InMemoryKeyValueStore store = tables.get(name);
        if (store == null) {
            throw new IllegalArgumentException("Table '" + name + "' not found");
        }
        return store;
    }

    @Override
    public synchronized void dropTable(String name) {
        if (closed) {
            throw new IllegalStateException("Database is closed");
        }
        InMemoryKeyValueStore store = tables.remove(name);
        if (store == null) {
            throw new IllegalArgumentException("Table '" + name + "' not found");
        }
        // Calculate total keys and bytes from the removed table to update metrics
        List<String> keys = store.keys();
        long keyCount = 0;
        long bytes = 0;
        for (String key : keys) {
            byte[] value = store.get(key);
            if (value != null) {
                keyCount++;
                bytes += (key.getBytes(java.nio.charset.StandardCharsets.UTF_8).length + value.length);
            }
        }
        if (keyCount > 0) {
            metrics.decrementKeyCount(keyCount);
            metrics.addDataSize(-bytes);
        }
        // Clear the data in the dropped table to free memory
        store.clearData();
    }

    @Override
    public synchronized List<String> listTables() {
        if (closed) {
            throw new IllegalStateException("Database is closed");
        }
        return new ArrayList<>(tables.keySet());
    }

    @Override
    public void close() {
        ArkashaPersistenceManager pm;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            pm = persistenceManager;
        }
        pm.flush();
    }

    @Override
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    @Override
    public WriteAheadLog getWriteAheadLog() {
        return writeAheadLog;
    }

    @Override
    public DatabaseConfig getConfig() {
        return config;
    }

    @Override
    public Metrics getMetrics() {
        return metrics;
    }

    @Override
    public <T> void register(String tableName, Class<T> type, Serializer<T> serializer) {
        if (closed) {
            throw new IllegalStateException("Database is closed");
        }
        if (!tables.containsKey(tableName)) {
            throw new IllegalArgumentException("Table '" + tableName + "' does not exist");
        }
        if (serializer == null || type == null) {
            throw new IllegalArgumentException("Type and serializer must not be null");
        }
        if (serializers.containsKey(tableName)) {
            throw new IllegalArgumentException("Serializer for table '" + tableName + "' already registered");
        }
        serializers.put(tableName, serializer);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Table<T> openTable(String tableName, Class<T> type) {
        if (closed) {
            throw new IllegalStateException("Database is closed");
        }
        Serializer<?> ser = serializers.get(tableName);
        if (ser == null) {
            throw new IllegalArgumentException("No serializer registered for table '" + tableName + "'");
        }
        Serializer<T> serializer;
        try {
            serializer = (Serializer<T>) ser;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Serializer type mismatch for table '" + tableName + "'");
        }
        KeyValueStore<byte[]> rawStore = openTable(tableName);
        return new TableImpl<>(rawStore, serializer);
    }

    // Implementation of Distributed (no-op stubs)
    @Override
    public void replicate(String tableName, String key, byte[] value) {
        // No-op (not implemented)
    }

    @Override
    public void addNode(String nodeId) {
        // No-op (not implemented)
    }

    // Internal methods for engine internals (accessible within package)
    InMemoryKeyValueStore getStore(String tableName) {
        return tables.get(tableName);
    }

    List<String> getAllTableNames() {
        return new ArrayList<>(tables.keySet());
    }

    TableOptions getTableOptions(String tableName) {
        InMemoryKeyValueStore store = tables.get(tableName);
        return (store != null ? store.getOptions() : null);
    }
}
