package com.example.kvdb;

import com.example.kvdb.api.*;
import com.example.kvdb.engine.ArkashaEngine;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
import java.nio.charset.StandardCharsets;

public class MetricsTest {

    @Test
    void testMetricsCounting() {
        String dataDir = System.getProperty("java.io.tmpdir") + "/arkashaTest-" + System.nanoTime();
        StorageEngine db = new ArkashaEngine(new DatabaseConfig(dataDir));
        db.createTable("A", new TableOptions().setWalEnabled(false));
        db.createTable("B", new TableOptions().setWalEnabled(false));
        KeyValueStore<byte[]> tableA = db.openTable("A");
        KeyValueStore<byte[]> tableB = db.openTable("B");

        // Insert keys into A
        tableA.put("key1", "alpha".getBytes(StandardCharsets.UTF_8));
        tableA.put("key2", "beta".getBytes(StandardCharsets.UTF_8));
        // Insert keys into B
        tableB.put("keyX", "gamma".getBytes(StandardCharsets.UTF_8));

        Metrics metrics = db.getMetrics();
        // 3 keys total
        assertThat(metrics.getKeyCount()).isEqualTo(3);
        // Calculate expected bytes (sum of key lengths + value lengths)
        int expectedBytes = 0;
        expectedBytes += "key1".getBytes(StandardCharsets.UTF_8).length + "alpha".getBytes(StandardCharsets.UTF_8).length;
        expectedBytes += "key2".getBytes(StandardCharsets.UTF_8).length + "beta".getBytes(StandardCharsets.UTF_8).length;
        expectedBytes += "keyX".getBytes(StandardCharsets.UTF_8).length + "gamma".getBytes(StandardCharsets.UTF_8).length;
        assertThat(metrics.getDataSize()).isEqualTo(expectedBytes);

        // Update a value in A (same length)
        tableA.put("key1", "alphx".getBytes(StandardCharsets.UTF_8)); // 5 bytes, same length as "alpha"
        assertThat(metrics.getKeyCount()).isEqualTo(3);
        assertThat(metrics.getDataSize()).isEqualTo(expectedBytes); // dataSize unchanged

        // Update a value in B (different length)
        tableB.put("keyX", "delta!".getBytes(StandardCharsets.UTF_8)); // 6 bytes, 1 byte longer than "gamma"
        expectedBytes += 1;
        assertThat(metrics.getKeyCount()).isEqualTo(3);
        assertThat(metrics.getDataSize()).isEqualTo(expectedBytes);

        // Delete a key from A
        tableA.delete("key2");
        expectedBytes -= ("key2".getBytes(StandardCharsets.UTF_8).length + "beta".getBytes(StandardCharsets.UTF_8).length);
        assertThat(metrics.getKeyCount()).isEqualTo(2);
        assertThat(metrics.getDataSize()).isEqualTo(expectedBytes);

        // Drop table B, metrics should now reflect only table A
        db.dropTable("B");
        // After dropping B, only "key1" remains in A
        int remainingBytes = "key1".getBytes(StandardCharsets.UTF_8).length + "alphx".getBytes(StandardCharsets.UTF_8).length;
        assertThat(metrics.getKeyCount()).isEqualTo(1);
        assertThat(metrics.getDataSize()).isEqualTo(remainingBytes);

        db.close();
    }
}
