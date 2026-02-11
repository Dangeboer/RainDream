package com.dangeboer.raindream.base;

import java.util.Arrays;
import java.util.Objects;

public interface IBaseEnum<V> {

    V getValue();

    String getLabel();

    /**
     * 根据 value 获取枚举实例（找不到返回 null）
     */
    static <E extends Enum<E> & IBaseEnum<V>, V> E fromValue(V value, Class<E> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        if (value == null) return null;

        return Arrays.stream(clazz.getEnumConstants())
                .filter(e -> Objects.equals(e.getValue(), value))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据 value 获取 label（找不到返回 null）
     */
    static <E extends Enum<E> & IBaseEnum<V>, V> String labelOf(V value, Class<E> clazz) {
        E e = fromValue(value, clazz);
        return e == null ? null : e.getLabel();
    }

    /**
     * 根据 label 获取 value（找不到返回 null）
     */
    static <E extends Enum<E> & IBaseEnum<V>, V> V valueOfLabel(String label, Class<E> clazz) {
        Objects.requireNonNull(clazz, "clazz must not be null");
        if (label == null) return null;

        return Arrays.stream(clazz.getEnumConstants())
                .filter(e -> Objects.equals(e.getLabel(), label))
                .map(IBaseEnum::getValue)
                .findFirst()
                .orElse(null);
    }
}
