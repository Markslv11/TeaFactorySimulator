package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Покупатель - работает только в фазе 3 (CONSUME)
 * Берёт готовый чай из readyBuffer
 * 
 * Таких потоков будет 3 штуки
 */
public class Buyer implements Runnable {
    private final TeaBuffer readyBuffer;
    private final Phaser phaser;
    private final Consumer<String> logger;
    private final String buyerName;
    private volatile boolean running = true;
    
    public Buyer(String name, TeaBuffer readyBuffer, Phaser phaser, 
Consumer<String> logger) {
        this.buyerName = name;
        this.readyBuffer = readyBuffer;
        this.phaser = phaser;
        this.logger = logger;
        
        phaser.register();
    }
    
    @Override
    public void run() {
        Thread.currentThread().setName(buyerName);
        log("Покупатель запущен");
        
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                int phase = phaser.getPhase();
                
                // Работаем только в фазе 3
                if (phase == 3) {
                    buy();
                }
                
                phaser.arriveAndAwaitAdvance();
            }
        } catch (InterruptedException e) {
            log("Покупатель прерван");
            Thread.currentThread().interrupt();
        } finally {
            phaser.arriveAndDeregister();
            log("Покупатель остановлен");
        }
    }
    
    /**
     * Покупка готового чая
     */
    private void buy() throws InterruptedException {
        // Берём готовый продукт
        TeaBatch batch = readyBuffer.take();
        
        log("Покупка: " + batch);
        log("readyBuffer[" + readyBuffer.size() + "/" + 
readyBuffer.getCapacity() + "]");
        
        // Имитация времени покупки
        Thread.sleep(randomDelay());
        
        log("Куплено: " + batch);
    }
    
    public void stop() {
        running = false;
    }
    
    private void log(String message) {
        logger.accept("[" + buyerName.toUpperCase() + "] " + message);
    }
    
    private long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }
}
