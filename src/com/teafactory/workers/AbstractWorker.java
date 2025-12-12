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
    }

    @Override
    public void run() {
        Thread.currentThread().setName(workerName);
        phaser.register(); // Регистрируемся в фазере
        log("🟢 Поток запущен");

        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                int currentPhase = phaser.getPhase() % 4;

                // Работаем только в своей фазе
                if (currentPhase == workPhase) {
                    // Выполняем работу многократно в нашей фазе
                    boolean canContinue = true;
                    while (canContinue && running && !Thread.currentThread().isInterrupted()) {
                        try {
                            canContinue = performWork();
                        } catch (InterruptedException e) {
                            log("⚠️ Работа прервана");
                            throw e;
                        }
                    }

                    log("✅ Работа в фазе завершена");
                }

                // Сообщаем фазеру, что готовы к переходу
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
     *
     * @return true если можно продолжать работу,
     *         false если нужно завершить фазу (буфер пуст/полон, работа закончена)
     */
    protected abstract boolean performWork() throws InterruptedException;

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