package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Фасовщик - работает только в фазе 2 (PACK)
 * Берёт обработанный чай из midBuffer, упаковывает и кладёт в readyBuffer
 */
public class Packer implements Runnable {
    private final TeaBuffer midBuffer;
    private final TeaBuffer readyBuffer;
    private final Phaser phaser;
    private final Consumer<String> logger;
    private volatile boolean running = true;
    
    public Packer(TeaBuffer midBuffer, TeaBuffer readyBuffer, Phaser 
phaser, Consumer<String> logger) {
        this.midBuffer = midBuffer;
        this.readyBuffer = readyBuffer;
        this.phaser = phaser;
        this.logger = logger;
        
        phaser.register();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName("Packer");
        log("Фасовщик запущен");
        
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                int phase = phaser.getPhase();
                
                // Работаем только в фазе 2
                if (phase == 2) {
                    pack();
                }
                
                phaser.arriveAndAwaitAdvance();
            }
        } catch (InterruptedException e) {
            log("Фасовщик прерван");
            Thread.currentThread().interrupt();
        } finally {
            phaser.arriveAndDeregister();
            log("Фасовщик остановлен");
        }
    }
    
    /**
     * Упаковка чая
     */
    private void pack() throws InterruptedException {
        // Берём из промежуточного буфера
        TeaBatch batch = midBuffer.take();
        
        log("Упаковка: " + batch);
        log("midBuffer[" + midBuffer.size() + "/" + 
midBuffer.getCapacity() + "]");
        
        // Имитация упаковки
        Thread.sleep(randomDelay());
        
        // Изменяем статус
        batch.setStage("PACKED");
        
        // Кладём в буфер готовой продукции
        readyBuffer.put(batch);
        
        log("Упаковано: " + batch + " → readyBuffer[" + 
readyBuffer.size() + "/" + readyBuffer.getCapacity() + "]");
    }
    
    public void stop() {
        running = false;
    }
    
    private void log(String message) {
        logger.accept("[ФАСОВЩИК] " + message);
    }
    
    private long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }
}
