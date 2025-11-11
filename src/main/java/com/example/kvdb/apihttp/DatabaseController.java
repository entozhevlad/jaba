package com.example.kvdb.apihttp;

import com.example.kvdb.api.KeyValueStore;
import com.example.kvdb.api.StorageEngine;
import com.example.kvdb.api.TableOptions;
import com.example.kvdb.core.InMemoryKeyValueStore;
import com.example.kvdb.apihttp.dto.BatchRequest;
import com.example.kvdb.apihttp.dto.BatchResponse;
import com.example.kvdb.apihttp.dto.KeysRequest;
import com.example.kvdb.apihttp.dto.PutManyRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class DatabaseController {

    private final StorageEngine db;

    public DatabaseController(StorageEngine db) {
        this.db = db;
    }

    // 1) Создать новую таблицу
    @PostMapping(path = "/tables", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> createTable(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        if (name == null || name.isEmpty()) {
            return ResponseEntity.badRequest().body("Table name is required");
        }
        try {
            TableOptions opts = new TableOptions()
                    .setWalEnabled(true)
                    .setMaxValueSize(-1);
            db.createTable(name, opts);
            return ResponseEntity.ok("created: " + name);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("already exists: " + name);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("error: " + e.getMessage());
        }
    }

    // 2) Список таблиц
    @GetMapping(path = "/tables", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listTables() {
        return ResponseEntity.ok(db.listTables());
    }

    // 3) Получить значение по ключу
    @GetMapping(path = "/tables/{tableName}/get/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> get(@PathVariable String tableName, @PathVariable String key) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            byte[] v = table.get(key);
            return ResponseEntity.ok(v == null ? "" : new String(v, StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Table not found: " + tableName);
        }
    }

    // 4) Положить значение по ключу
    @PostMapping(path = "/tables/{tableName}/put/{key}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> put(@PathVariable String tableName, @PathVariable String key, @RequestBody String body) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            table.put(key, body.getBytes(StandardCharsets.UTF_8));
            return ResponseEntity.ok("ok");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Table not found: " + tableName);
        }
    }

    // 5) Удалить ключ
    @DeleteMapping(path = "/tables/{tableName}/delete/{key}", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<?> delete(@PathVariable String tableName, @PathVariable String key) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            table.delete(key);
            return ResponseEntity.ok("ok");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Table not found: " + tableName);
        }
    }

    // 6) Вывести ключи (только для InMemory)
    @GetMapping(path = "/tables/{tableName}/keys", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> listKeys(@PathVariable String tableName) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            if (table instanceof InMemoryKeyValueStore memStore) {
                List<String> keys = memStore.keys();
                return ResponseEntity.ok(keys);
            } else {
                return ResponseEntity.ok("Listing keys not supported for this engine");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("Table not found: " + tableName);
        }
    }

    // 7) МАССОВАЯ ВСТАВКА
    @PostMapping(path="/tables/{tableName}/put-many",
            consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> putMany(@PathVariable String tableName,
                                     @RequestBody PutManyRequest request) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            Map<String, byte[]> toPut = new HashMap<>();
            if (request.items()!=null) {
                for (var e : request.items().entrySet()) {
                    toPut.put(e.getKey(), e.getValue()==null ? null :
                            e.getValue().getBytes(StandardCharsets.UTF_8));
                }
            }
            table.putAll(toPut);
            return ResponseEntity.ok(Map.of("inserted", toPut.size()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error","Table not found","table",tableName));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
        }
    }

    // 8) МАССОВОЕ УДАЛЕНИЕ
    @PostMapping(path="/tables/{tableName}/delete-many",
            consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deleteMany(@PathVariable String tableName,
                                        @RequestBody KeysRequest request) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            int count = request.keys()==null ? 0 : request.keys().size();
            table.deleteAll(request.keys());
            return ResponseEntity.ok(Map.of("deleted", count));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error","Table not found","table",tableName));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
        }
    }

    @PostMapping(path="/tables/{tableName}/get-many",
            consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getMany(@PathVariable String tableName,
                                     @RequestBody com.example.kvdb.apihttp.dto.KeysRequest request) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            java.util.Map<String, byte[]> raw = table.getAll(request.keys());
            java.util.Map<String, String> out = new java.util.HashMap<>();
            if (raw != null) {
                for (var e : raw.entrySet()) {
                    out.put(e.getKey(),
                            e.getValue()==null ? null :
                                    new String(e.getValue(), java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            return ResponseEntity.ok(java.util.Map.of("items", out));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(java.util.Map.of("error","Table not found","table",tableName));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(java.util.Map.of(
                    "error", ex.getClass().getSimpleName(),
                    "message", ex.getMessage()
            ));
        }
    }

    // 9) BATCH: put/get/delete за один запрос
    @PostMapping(path="/tables/{tableName}/batch",
            consumes=MediaType.APPLICATION_JSON_VALUE,
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> batch(@PathVariable String tableName,
                                   @RequestBody BatchRequest request) {
        try {
            KeyValueStore<byte[]> table = db.openTable(tableName);
            BatchResponse out = new BatchResponse();

            if (request.ops()!=null) {
                for (BatchRequest.Op op : request.ops()) {
                    String type = op.op()==null? "" : op.op().toLowerCase();
                    String key  = op.key();
                    try {
                        switch (type) {
                            case "put" -> {
                                if (key==null) { out.addError("put", null, "key is required"); break; }
                                String val = op.value();
                                if (val==null) { out.addError("put", key, "value is required"); break; }
                                table.put(key, val.getBytes(StandardCharsets.UTF_8));
                                out.addOk("put", key, val);
                            }
                            case "get" -> {
                                if (key==null) { out.addError("get", null, "key is required"); break; }
                                byte[] v = table.get(key);
                                out.addOk("get", key, v==null? null : new String(v, StandardCharsets.UTF_8));
                            }
                            case "delete" -> {
                                if (key==null) { out.addError("delete", null, "key is required"); break; }
                                table.delete(key);
                                out.addOk("delete", key, null);
                            }
                            default -> out.addError(type, key, "unknown op (use put|get|delete)");
                        }
                    } catch (Exception ex) {
                        out.addError(type, key, ex.getMessage());
                    }
                }
            }

            return ResponseEntity.ok(java.util.Map.of("results", out.results()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(404).body(Map.of("error","Table not found","table",tableName));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of("error", ex.getClass().getSimpleName(), "message", ex.getMessage()));
        }
    }
}
