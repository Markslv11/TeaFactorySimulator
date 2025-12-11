package com.teafactory.buffer;

import com.teafactory.model.TeaBatch;
import java.util.ArrayDeque;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Потокобезопасный буфер с ограниченной ёмкостью.
 * Использует ReentrantLock и Condition для синхронизации.
 * 
 * ВАЖНО: Не использует synchronized, только явные Lock и Condition!
 */
public class TeaBuffer {
    private final ArrayDeque<TeaBatch> deque;
    private final ReentrantLock lock;
    private final Condition notEmpty;  // Сигнализирует, что буфер не 
    private final Condition notFull;   // Сигнализирует, что буфер не

    private final int capacity;
    private final String name;
    
    public TeaBuffer(int capacity, String name) {
        this.capacity = capacity;
        this.name = name;
        this.deque = new ArrayDeque<>(capacity);
        this.lock = new ReentrantLock(true); // fair lock для честной
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }
    
    /**
     * Положить элемент в буфер.
     * Блокирует поток, если буфер полон, до освобождения места.
     */
    public void put(TeaBatch batch) throws InterruptedException {
        lock.lock(); // Захватываем эксклюзивный доступ
        try {
            // Ждём, пока не освободится место (если буфер полон)
            while (deque.size() >= capacity) {
                notFull.await(); // Освобождаем lock и ждём сигнала
            }
            
            deque.addLast(batch);
            
            // Сигнализируем ждущим потокам, что буфер больше не пустой
            notEmpty.signal();
            
        } finally {
            lock.unlock(); // ОБЯЗАТЕЛЬНО освобождаем lock в finally
        }
    }
    
    /**
     * Взять элемент из буфера.
     * Блокирует поток, если буфер пуст, до появления элемента.
     */
    public TeaBatch take() throws InterruptedException {
        lock.lock();
        try {
            // Ждём, пока не появится элемент (если буфер пуст)
            while (deque.isEmpty()) {
                notEmpty.await();
            }
            
            TeaBatch batch = deque.removeFirst();
            
            // Сигнализируем ждущим потокам, что буфер больше не полный
            notFull.signal();
            
            return batch;
            
        } finally {
            lock.unlock();
        }
    }
    
    /**
     * Получить текущий размер буфера (потокобезопасно)
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
     * Получить вместимость буфера
     */
    public int getCapacity() {
        return capacity;
    }
    
    /**
     * Получить имя буфера
     */
    public String getName() {
        return name;
    }
}
