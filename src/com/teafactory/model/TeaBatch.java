package com.teafactory.model;

/**
 * Класс представляет партию чая на различных стадиях обработки
 */
public class TeaBatch {
    private static int counter = 0;
    
    private final int id;
    private final TeaType type;
    private String stage; // RAW, PROCESSED, PACKED
    
    public TeaBatch(TeaType type) {
        this.id = ++counter;
        this.type = type;
        this.stage = "RAW";
    }
    
    public int getId() {
        return id;
    }
    
    public TeaType getType() {
        return type;
    }
    
    public String getStage() {
        return stage;
    }
    
    public void setStage(String stage) {
        this.stage = stage;
    }
    
    @Override
    public String toString() {
        return String.format("Партия #%d [%s] (%s)", id, 
type.getDisplayName(), stage);
    }
}
