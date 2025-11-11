package com.example.kvdb.apihttp.dto;
import java.util.List;

public record BatchRequest(List<Op> ops) {
    // op: "put" | "get" | "delete"
    // key: обязателен для всех
    // value: обязателен только для "put"
    public record Op(String op, String key, String value) {}
}
