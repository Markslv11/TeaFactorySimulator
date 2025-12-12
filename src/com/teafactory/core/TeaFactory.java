package com.teafactory.core;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.workers.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Главный координатор фабрики с поддержкой перезапуска и статистикой
 */
public class TeaFactory {
    private final TeaBuffer rawBuffer;
    private final TeaBuffer midBuffer;
    private final TeaBuffer readyBuffer;

    private Phaser phaser;
    private List<Thread> threads;
    private List<AbstractWorker> workers;

    private final Consumer<String> logger;

    // Статистика
    private long startTime;
    private long totalRuntime;
    private int cyclesCompleted;

    public TeaFactory(Consumer<String> logger) {
        this.logger = logger;

        this.rawBuffer = new TeaBuffer(5, "RawBuffer");
        this.midBuffer = new TeaBuffer(3, "MidBuffer");
        this.readyBuffer = new TeaBuffer(4, "ReadyBuffer");

        this.threads = new ArrayList<>();
        this.workers = new ArrayList<>();

        log("🏭 Фабрика инициализирована");
    }

    /**
     * Запуск или перезапуск фабрики
     */
    public void start() {
        log("🚀 Запуск фабрики...");

        // Создаём новый Phaser
        phaser = new Phaser(0) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {
                cyclesCompleted++;
                String phaseName = getPhaseName(phase);
                logger.accept(String.format("━━━━━━━━ ФАЗА %d (%s) ЗАВЕРШЕНА ━━━━━━━━", phase, phaseName));
                return false; // продолжаем работу
            }
        };

        // Очищаем старые данные
        threads.clear();
        workers.clear();

        // Создаём workers
        RawSupplier supplier = new RawSupplier(rawBuffer, phaser, logger);
        TeaMaster master = new TeaMaster(rawBuffer, midBuffer, phaser, logger);
        Packer packer = new Packer(midBuffer, readyBuffer, phaser, logger);

        Buyer buyer1 = new Buyer("ПОКУПАТЕЛЬ-1", readyBuffer, phaser, logger);
        Buyer buyer2 = new Buyer("ПОКУПАТЕЛЬ-2", readyBuffer, phaser, logger);
        Buyer buyer3 = new Buyer("ПОКУПАТЕЛЬ-3", readyBuffer, phaser, logger);

        workers.add(supplier);
        workers.add(master);
        workers.add(packer);
        workers.add(buyer1);
        workers.add(buyer2);
        workers.add(buyer3);

        // Создаём потоки
        for (AbstractWorker worker : workers) {
            Thread thread = new Thread(worker);
            threads.add(thread);
        }

        // Запускаем потоки
        startTime = System.currentTimeMillis();
        threads.forEach(Thread::start);

        log(String.format("✅ Фабрика запущена! Активных потоков: %d", threads.size()));
    }

    /**
     * Остановка фабрики
     */
    public void stop() {
        log("🛑 Остановка фабрики...");

        // Останавливаем всех workers
        workers.forEach(AbstractWorker::stop);

        // Прерываем потоки
        threads.forEach(Thread::interrupt);

        // Ждём завершения
        for (Thread thread : threads) {
            try {
                thread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Обновляем статистику
        totalRuntime += (System.currentTimeMillis() - startTime);

        log("✅ Фабрика остановлена");
        logStatistics();
    }

    /**
     * Вывод статистики
     */
    private void logStatistics() {
        int totalPurchases = workers.stream()
                .filter(w -> w instanceof Buyer)
                .mapToInt(w -> ((Buyer) w).getPurchaseCount())
                .sum();

        double runtimeSeconds = totalRuntime / 1000.0;

        log("📊 ═══════════ СТАТИСТИКА ═══════════");
        log(String.format("   ⏱️  Время работы: %.1f сек", runtimeSeconds));
        log(String.format("   🔄 Циклов завершено: %d", cyclesCompleted));
        log(String.format("   📦 Всего куплено партий: %d", totalPurchases));

        if (runtimeSeconds > 0) {
            double throughput = totalPurchases / runtimeSeconds;
            log(String.format("   ⚡ Производительность: %.2f партий/сек", throughput));
        }

        log("   📈 Статус буферов:");
        log(String.format("      • Raw Buffer: %d/%d", rawBuffer.size(), rawBuffer.getCapacity()));
        log(String.format("      • Mid Buffer: %d/%d", midBuffer.size(), midBuffer.getCapacity()));
        log(String.format("      • Ready Buffer: %d/%d", readyBuffer.size(), readyBuffer.getCapacity()));
        log("═════════════════════════════════════");
    }

    /**
     * Текущая статистика для отображения в GUI
     */
    public String getCurrentStatistics() {
        int totalPurchases = workers.stream()
                .filter(w -> w instanceof Buyer)
                .mapToInt(w -> ((Buyer) w).getPurchaseCount())
                .sum();

        long currentRuntime = System.currentTimeMillis() - startTime;
        double runtimeSeconds = currentRuntime / 1000.0;

        return String.format("⏱️ %.1fs | 🔄 %d циклов | 📦 %d партий",
                runtimeSeconds, cyclesCompleted, totalPurchases);
    }

    public int getCurrentPhase() {
        return phaser != null ? phaser.getPhase() % 4 : 0;
    }

    public String getCurrentPhaseName() {
        return getPhaseName(getCurrentPhase());
    }

    private String getPhaseName(int phase) {
        switch (phase % 4) {
            case 0: return "SUPPLY";
            case 1: return "PROCESS";
            case 2: return "PACK";
            case 3: return "CONSUME";
            default: return "UNKNOWN";
        }
    }

    public TeaBuffer getRawBuffer() { return rawBuffer; }
    public TeaBuffer getMidBuffer() { return midBuffer; }
    public TeaBuffer getReadyBuffer() { return readyBuffer; }

    private void log(String message) {
        logger.accept("[ФАБРИКА] " + message);
    }
}