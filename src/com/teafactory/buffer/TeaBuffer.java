package com.teafactory.buffer;

import com.teafactory.model.TeaBatch;

import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Потокобезопасный буфер с ограниченной ёмкостью.
 * Использует ReentrantLock и Condition для полноценной реализации
 * механизма "producer-consumer" без synchronized.
 * ✔ Поддерживает блокирующие операции put() и take()
 * ✔ Корректно работает с множеством производителей и потребителей
 *  Предотвращает гонки данных и ложные пробуждения
 * ✔ Fair-lock гарантирует честный порядок (важно при 3 Buyers!)
 */
public class TeaBuffer {

    private final ArrayDeque<TeaBatch> deque;
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    private final int capacity;
    private final String name; // имя буфера для логов (если нужно)

    public TeaBuffer(int capacity, String name) {
        this.capacity = capacity;
        this.name = name;
        this.deque = new ArrayDeque<>(capacity);

        // fair = true — важен при множестве конкурентов
        this.lock = new ReentrantLock(true);

        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }

    /**
     * Блокирующее добавление элемента.
     * Если буфер полон → поток ждёт.
     */
    public void put(TeaBatch batch) throws InterruptedException {
        lock.lock();
        try {
            while (deque.size() >= capacity) {
                notFull.await();
            }

            deque.addLast(batch);

            // signalAll — лучший выбор при нескольких Consumer
            notEmpty.signalAll();

        } finally {
            lock.unlock();
        }
    }

    /**
     * Блокирующее извлечение элемента.
     * Если буфер пуст → поток ждёт.
     */
    public TeaBatch take() throws InterruptedException {
        lock.lock();
        try {
            while (deque.isEmpty()) {
                notEmpty.await();
            }

            TeaBatch batch = deque.removeFirst();

            // Будим всех, кто ждёт место
            notFull.signalAll();

            return batch;

        } finally {
            lock.unlock();
        }
    }

    /**
     * Потокобезопасный размер буфера.
     */
    public int size() {
        lock.lock();
        try {
            return deque.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Если в GUI нужна быстрая проверка (не блокирующая):
     */
    public int peekSizeUnsafe() {
        return deque.size();
    }

    /**
     * Вместимость буфера.
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Имя буфера (RAW / MID / READY)
     */
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "[" + name + ": " + deque.size() + "/" + capacity + "]";
    }
}
