package com.teafactory.model;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Класс представляет партию чая на различных стадиях обработки
 */
public class TeaBatch {
    private static final AtomicInteger counter = new AtomicInteger(0);

    private final int id;
    private final TeaType type;
    private volatile String stage; // RAW, PROCESSED, PACKED

    public TeaBatch(TeaType type) {
        if (type == null) {
            throw new IllegalArgumentException("TeaType не может быть null");
        }
        this.id = counter.incrementAndGet();
        this.type = type;
        this.stage = "RAW";
    }

    // Этот метод НУЖЕН - вызывается в воркерах
    public void setStage(String stage) {
        if (stage == null || stage.trim().isEmpty()) {
            throw new IllegalArgumentException("Stage не может быть пустым");
        }
        this.stage = stage;
    }

    @Override
    public String toString() {
        return String.format("Партия #%d [%s] (%s)", id, type.getDisplayName(), stage);
    }
}