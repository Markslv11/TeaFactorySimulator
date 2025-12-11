package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import com.teafactory.model.TeaType;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Поставщик сырья - работает только в фазе 0 (SUPPLY)
 * Поставляет партии сырого чая в rawBuffer
 */
public class RawSupplier implements Runnable {
    private final TeaBuffer rawBuffer;
    private final Phaser phaser;
    private final Consumer<String> logger;
    private volatile boolean running = true;
    
    public RawSupplier(TeaBuffer rawBuffer, Phaser phaser, 
Consumer<String> logger) {
        this.rawBuffer = rawBuffer;
        this.phaser = phaser;
        this.logger = logger;
        
        // Регистрируем этот поток в Phaser
        phaser.register();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName("RawSupplier");
        log("Поставщик запущен");
        
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                int phase = phaser.getPhase();
                
                // Работаем только в фазе 0
                if (phase == 0) {
                    supply();
                }
                
                // Ждём остальных участников фазы
                phaser.arriveAndAwaitAdvance();
            }
        } catch (InterruptedException e) {
            log("Поставщик прерван");
            Thread.currentThread().interrupt();
        } finally {
            phaser.arriveAndDeregister(); // Снимаемся с регистрации
            log("Поставщик остановлен");
        }
    }
    
    /**
     * Поставка сырья в буфер
     */
    private void supply() throws InterruptedException {
        // Создаём случайную партию чая
        TeaBatch batch = new TeaBatch(TeaType.random());
        batch.setStage("RAW");
        
        log("Поставка: " + batch);
        
        // Кладём в буфер (может заблокироваться, если буфер полон)
        rawBuffer.put(batch);
        
        log("Поставлено: " + batch + " → rawBuffer[" + rawBuffer.size() + 
"/" + rawBuffer.getCapacity() + "]");
        
        // Имитация времени поставки
        Thread.sleep(randomDelay());
    }
    
    public void stop() {
        running = false;
    }
    
    private void log(String message) {
        logger.accept("[ПОСТАВЩИК] " + message);
    }
    
    private long randomDelay() {
        return 300 + (long)(Math.random() * 600); // 300-900 мс
    }
}
