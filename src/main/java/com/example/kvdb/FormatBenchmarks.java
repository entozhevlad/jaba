package com.example.kvdb;

import com.example.kvdb.model.User;
import com.example.kvdb.util.AvroUserSerializer;
import com.example.kvdb.util.ProtoStructUserSerializer;
import com.example.kvdb.util.Serializer;
import com.example.kvdb.util.TlvUserSerializer;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FormatBenchmarks {

    public static void main(String[] args) {
        int N = args.length > 0 ? Integer.parseInt(args[0]) : 50_000;
        List<User> users = generate(N);

        List<Map.Entry<String, Serializer<User>>> impls = List.of(
                Map.entry("Protobuf(Struct)", (Serializer<User>) new ProtoStructUserSerializer()),
                Map.entry("Avro(Generic)",    (Serializer<User>) new AvroUserSerializer()),
                Map.entry("Custom(TLV)",      (Serializer<User>) new TlvUserSerializer())
        );

        System.out.printf("Records: %,d%n", N);

        for (var e : impls) {
            String name = e.getKey();
            Serializer<User> ser = e.getValue();

            // encode
            Instant t0 = Instant.now();
            List<byte[]> encoded = new ArrayList<>(N);
            long totalBytes = 0;
            for (User u : users) {
                byte[] b = ser.serialize(u);
                totalBytes += b.length;
                encoded.add(b);
            }
            long encMs = Duration.between(t0, Instant.now()).toMillis();

            // decode
            Instant t1 = Instant.now();
            long check = 0;
            for (byte[] b : encoded) {
                User u = ser.deserialize(b);
                check += (u.age + (u.active ? 1 : 0));
            }
            long decMs = Duration.between(t1, Instant.now()).toMillis();

            System.out.printf(
                    "%-18s | size: %8.2f KB/rec | enc: %7.1f Krec/s | dec: %7.1f Krec/s%n",
                    name,
                    (totalBytes / 1024.0) / N,
                    N / Math.max(1.0, encMs) * 1.0,   // Krec/s ≈ N / ms
                    N / Math.max(1.0, decMs) * 1.0
            );
        }
    }

    private static List<User> generate(int n) {
        List<User> list = new ArrayList<>(n);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = 0; i < n; i++) {
            int age = rnd.nextInt(16, 80);
            boolean active = rnd.nextBoolean();
            String id = UUID.randomUUID().toString();
            String name = randomString(rnd, rnd.nextInt(5, 15));
            String email = name.toLowerCase() + "@example.com";
            List<String> tags = List.of(randomString(rnd, 4), randomString(rnd, 6));
            list.add(new User(id, name, email, age, active, tags));
        }
        return list;
    }

    private static String randomString(ThreadLocalRandom rnd, int len) {
        char[] a = new char[len];
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < len; i++) a[i] = alphabet.charAt(rnd.nextInt(alphabet.length()));
        return new String(a);
    }
}
