package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Мастер обработки - работает только в фазе 1 (PROCESS)
 * Берёт сырьё из rawBuffer, обрабатывает и кладёт в midBuffer
 */
public class TeaMaster implements Runnable {
    private final TeaBuffer rawBuffer;
    private final TeaBuffer midBuffer;
    private final Phaser phaser;
    private final Consumer<String> logger;
    private volatile boolean running = true;
    
    public TeaMaster(TeaBuffer rawBuffer, TeaBuffer midBuffer, Phaser 
phaser, Consumer<String> logger) {
        this.rawBuffer = rawBuffer;
        this.midBuffer = midBuffer;
        this.phaser = phaser;
        this.logger = logger;
        
        phaser.register();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName("TeaMaster");
        log("Мастер запущен");
        
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                int phase = phaser.getPhase();
                
                // Работаем только в фазе 1
                if (phase == 1) {
                    process();
                }
                
                phaser.arriveAndAwaitAdvance();
            }
        } catch (InterruptedException e) {
            log("Мастер прерван");
            Thread.currentThread().interrupt();
        } finally {
            phaser.arriveAndDeregister();
            log("Мастер остановлен");
        }
    }
    
    /**
     * Обработка сырья
     */
    private void process() throws InterruptedException {
        // Берём сырьё из rawBuffer (блокируется, если пусто)
        TeaBatch batch = rawBuffer.take();
        
        log("Обработка: " + batch);
        log("rawBuffer[" + rawBuffer.size() + "/" + 
rawBuffer.getCapacity() + "]");
        
        // Имитация процесса обработки
        Thread.sleep(randomDelay());
        
        // Изменяем статус
        batch.setStage("PROCESSED");
        
        // Кладём в промежуточный буфер (блокируется, если полон)
        midBuffer.put(batch);
        
        log("Обработано: " + batch + " → midBuffer[" + midBuffer.size() + 
"/" + midBuffer.getCapacity() + "]");
    }
    
    public void stop() {
        running = false;
    }
    
    private void log(String message) {
        logger.accept("[МАСТЕР] " + message);
    }
    
    private long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }
}
