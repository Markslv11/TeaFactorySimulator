package com.teafactory.workers;

import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Абстрактный базовый класс для всех рабочих потоков.
 * Инкапсулирует общую логику работы с Phaser и логированием.
 */
public abstract class AbstractWorker implements Runnable {
    protected final Phaser phaser;
    protected final Consumer<String> logger;
    protected final String workerName;
    protected final int workPhase;
    protected volatile boolean running = true;

    public AbstractWorker(String workerName, int workPhase, Phaser phaser, Consumer<String> logger) {
        this.workerName = workerName;
        this.workPhase = workPhase;
        this.phaser = phaser;
        this.logger = logger;
        phaser.register();
    }

    @Override
    public void run() {
        Thread.currentThread().setName(workerName);
        log("🟢 Поток запущен");

        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                int currentPhase = phaser.getPhase();

                // Используем % 4 для циклического повторения фаз
                if (currentPhase % 4 == workPhase) {
                    performWork();
                }

                phaser.arriveAndAwaitAdvance();
            }
        } catch (InterruptedException e) {
            log("⚠️ Поток прерван");
            Thread.currentThread().interrupt();
        } finally {
            phaser.arriveAndDeregister();
            log("🔴 Поток остановлен");
        }
    }

    /**
     * Основная работа, которую выполняет worker в своей фазе.
     * Должна быть реализована в каждом конкретном worker.
     */
    protected abstract void performWork() throws InterruptedException;

    /**
     * Остановка worker
     */
    public void stop() {
        running = false;
    }

    /**
     * Логирование с префиксом имени worker
     */
    protected void log(String message) {
        logger.accept(String.format("[%s] %s", workerName, message));
    }

    /**
     * Случайная задержка для имитации работы
     */
    protected long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }
}