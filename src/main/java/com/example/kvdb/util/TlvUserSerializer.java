package com.example.kvdb.util;

import com.example.kvdb.model.User;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class TlvUserSerializer implements Serializer<User> {
    // Tags: 1=id, 2=name, 3=email, 4=age, 5=active, 6=tags (list of string)
    // WireType: 0=varint, 1=string, 2=list<string>

    @Override
    public byte[] serialize(User u) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(64);
        // id
        VarInts.writeVarInt(out, 1); out.write(1); VarInts.writeString(out, u.id == null ? "" : u.id);
        // name
        VarInts.writeVarInt(out, 2); out.write(1); VarInts.writeString(out, u.name == null ? "" : u.name);
        // email
        VarInts.writeVarInt(out, 3); out.write(1); VarInts.writeString(out, u.email == null ? "" : u.email);
        // age (zigzag varint)
        VarInts.writeVarInt(out, 4); out.write(0);
        VarInts.writeVarInt(out, VarInts.zigZagEncodeInt(u.age));
        // active (0/1)
        VarInts.writeVarInt(out, 5); out.write(0);
        VarInts.writeVarInt(out, u.active ? 1 : 0);
        // tags (list)
        List<String> tags = (u.tags == null ? List.of() : u.tags);
        VarInts.writeVarInt(out, 6); out.write(2);
        VarInts.writeVarInt(out, tags.size());
        for (String t : tags) VarInts.writeString(out, t == null ? "" : t);
        return out.toByteArray();
    }

    @Override
    public User deserialize(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        User u = new User();
        u.tags = new ArrayList<>();
        while (buf.hasRemaining()) {
            int tag = VarInts.readVarInt(buf);
            int wt  = buf.get() & 0xFF;
            switch (tag) {
                case 1 -> u.id = VarInts.readString(buf);
                case 2 -> u.name = VarInts.readString(buf);
                case 3 -> u.email = VarInts.readString(buf);
                case 4 -> u.age = VarInts.zigZagDecodeInt(VarInts.readVarInt(buf));
                case 5 -> u.active = (VarInts.readVarInt(buf) != 0);
                case 6 -> {
                    int n = VarInts.readVarInt(buf);
                    for (int i = 0; i < n; i++) u.tags.add(VarInts.readString(buf));
                }
                default -> throw new IllegalArgumentException("Unknown tag: " + tag);
            }
        }
        return u;
    }
}
