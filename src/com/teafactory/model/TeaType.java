package com.teafactory.model;

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
        TeaType[] values = values();
        return values[(int)(Math.random() * values.length)];
    }
}
