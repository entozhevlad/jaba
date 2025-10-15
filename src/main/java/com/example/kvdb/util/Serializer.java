package com.example.kvdb.util;

/**
 * Сериализатор значения типа T в двоичный формат и обратно.
 * @param <T> тип сериализуемого значения
 */
public interface Serializer<T> {
    /**
     * Сериализовать значение в массив байт.
     * @param value значение
     * @return массив байт, представляющий значение
     */
    byte[] serialize(T value);

    /**
     * Десериализовать значение из массива байт.
     * @param bytes данные
     * @return восстановленный объект типа T
     */
    T deserialize(byte[] bytes);
}
