package com.teafactory.model;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Перечисление типов чая, производимых на фабрике
 */
public enum TeaType {
    PUER("Пуэр"),
    OOLONG("Улун"),
    GREEN("Зелёный"),
    MATCHA("Матча"),
    BLACK("Чёрный"),
    WHITE("Белый");

    private final String displayName;

    // Кэшируем массив для производительности
    private static final TeaType[] VALUES = values();

    TeaType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Получить случайный тип чая
     */
    public static TeaType random() {
        int index = ThreadLocalRandom.current().nextInt(VALUES.length);
        return VALUES[index];
    }
}