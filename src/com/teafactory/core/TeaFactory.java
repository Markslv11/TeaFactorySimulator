package com.teafactory.core;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.workers.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Главный координатор фабрики.
 * Создаёт буферы, потоки и управляет Phaser.
 */
public class TeaFactory {
    // Буферы
    private final TeaBuffer rawBuffer;
    private final TeaBuffer midBuffer;
    private final TeaBuffer readyBuffer;
    
    // Phaser для синхронизации фаз
    private final Phaser phaser;
    
    // Рабочие потоки
    private final List<Thread> threads;
    private final List<Runnable> workers;
    
    // Логгер для передачи в GUI
    private final Consumer<String> logger;
    
    public TeaFactory(Consumer<String> logger) {
        this.logger = logger;
        
        // Создаём буферы с разными ёмкостями
        this.rawBuffer = new TeaBuffer(5, "RawBuffer");
        this.midBuffer = new TeaBuffer(3, "MidBuffer");
        this.readyBuffer = new TeaBuffer(4, "ReadyBuffer");
        
        // Создаём Phaser без участников (они зарегистрируются сами)
        this.phaser = new Phaser(0) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) 
{
                // Логируем переход между фазами
                String phaseName = getPhaseName(phase);
                logger.accept("═══ Фаза " + phase + " (" + phaseName + 
") завершена ═══");
                
                // false = продолжаем работу (не завершаем Phaser)
                return false;
            }
        };
        
        this.threads = new ArrayList<>();
        this.workers = new ArrayList<>();
        
        log("Фабрика инициализирована");
    }
    
    /**
     * Запуск всех рабочих потоков
     */
    public void start() {
        log("Запуск фабрики...");
        
        // Создаём рабочих
        RawSupplier supplier = new RawSupplier(rawBuffer, phaser, logger);
        TeaMaster master = new TeaMaster(rawBuffer, midBuffer, phaser, 
logger);
        Packer packer = new Packer(midBuffer, readyBuffer, phaser, 
logger);
        
        Buyer buyer1 = new Buyer("Buyer-1", readyBuffer, phaser, logger);
        Buyer buyer2 = new Buyer("Buyer-2", readyBuffer, phaser, logger);
        Buyer buyer3 = new Buyer("Buyer-3", readyBuffer, phaser, logger);
        
        // Сохраняем ссылки на рабочих для последующей остановки
        workers.add(supplier);
        workers.add(master);
        workers.add(packer);
        workers.add(buyer1);
        workers.add(buyer2);
        workers.add(buyer3);
        
        // Создаём и запускаем потоки
        Thread t1 = new Thread(supplier);
        Thread t2 = new Thread(master);
        Thread t3 = new Thread(packer);
        Thread t4 = new Thread(buyer1);
        Thread t5 = new Thread(buyer2);
        Thread t6 = new Thread(buyer3);
        
        threads.add(t1);
        threads.add(t2);
        threads.add(t3);
        threads.add(t4);
        threads.add(t5);
        threads.add(t6);
        
        // Запускаем все потоки
        threads.forEach(Thread::start);
        
        log("Фабрика запущена! Потоков: " + threads.size());
    }
    
    /**
     * Остановка всех потоков
     */
    public void stop() {
        log("Остановка фабрики...");
        
        // Останавливаем рабочих (устанавливаем флаг running = false)
        workers.forEach(worker -> {
            if (worker instanceof RawSupplier) ((RawSupplier) 
worker).stop();
            else if (worker instanceof TeaMaster) ((TeaMaster) 
worker).stop();
            else if (worker instanceof Packer) ((Packer) worker).stop();
            else if (worker instanceof Buyer) ((Buyer) worker).stop();
        });
        
        // Прерываем потоки
        threads.forEach(Thread::interrupt);
        
        // Ждём завершения всех потоков
        for (Thread thread : threads) {
            try {
                thread.join(2000); // Ждём максимум 2 секунды
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        log("Фабрика остановлена");
    }
    
    /**
     * Получить текущую фазу
     */
    public int getCurrentPhase() {
        return phaser.getPhase();
    }
    
    /**
     * Получить название текущей фазы
     */
    public String getCurrentPhaseName() {
        return getPhaseName(phaser.getPhase());
    }
    
    /**
     * Получить название фазы по номеру
     */
    private String getPhaseName(int phase) {
        switch (phase % 4) {
            case 0: return "SUPPLY";
            case 1: return "PROCESS";
            case 2: return "PACK";
            case 3: return "CONSUME";
            default: return "UNKNOWN";
        }
    }
    
    // Геттеры для буферов (для отображения в GUI)
    public TeaBuffer getRawBuffer() { return rawBuffer; }
    public TeaBuffer getMidBuffer() { return midBuffer; }
    public TeaBuffer getReadyBuffer() { return readyBuffer; }
    
    private void log(String message) {
        logger.accept("[ФАБРИКА] " + message);
    }
}
