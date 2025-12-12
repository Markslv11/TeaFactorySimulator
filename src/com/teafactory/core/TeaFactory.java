package com.teafactory.core;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.workers.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Центральный координатор фабрики.
 *
 * ✔ Создаёт и управляет потоками
 * ✔ Управляет фазами через Phaser
 * ✔ Логирует этапы
 * ✔ Предоставляет данные для GUI
 */
public class TeaFactory {

    // Буферы производства
    private final TeaBuffer rawBuffer;
    private final TeaBuffer midBuffer;
    private final TeaBuffer readyBuffer;

    // Фазовый синхронизатор
    private final Phaser phaser;

    // Все рабочие (Runnable)
    private List<Runnable> workers = new ArrayList<>();

    // Потоки
    private List<Thread> threads = new ArrayList<>();

    // Логгер → передаётся в GUI
    private final Consumer<String> logger;

    // Флаг состояния фабрики
    private volatile boolean isRunning = false;

    public TeaFactory(Consumer<String> logger) {
        this.logger = logger;

        // Создаём буферы
        rawBuffer = new TeaBuffer(5, "RAW");
        midBuffer = new TeaBuffer(3, "MID");
        readyBuffer = new TeaBuffer(4, "READY");

        // Phaser: 0 parties — рабочие зарегистрируются сами
        phaser = new Phaser(0) {
            @Override
            protected boolean onAdvance(int phase, int registeredParties) {

                logger.accept(
                        String.format("────────── ФАЗА %d (%s) завершена ──────────",
                                phase, phaseName(phase))
                );

                return false; // продолжать работу
            }
        };

        log("Фабрика инициализирована");
    }

    // ======================================================
    //                    ЗАПУСК ФАБРИКИ
    // ======================================================
    public void start() {

        if (isRunning) {
            log("Фабрика уже работает");
            return;
        }

        log("Запуск фабрики...");

        threads.clear();
        workers.clear();

        // +++++++++++++ СОЗДАЁМ РАБОЧИХ +++++++++++++
        RawSupplier supplier = new RawSupplier(rawBuffer, phaser, logger);
        TeaMaster master = new TeaMaster(rawBuffer, midBuffer, phaser, logger);
        Packer packer = new Packer(midBuffer, readyBuffer, phaser, logger);

        Buyer buyer1 = new Buyer("Buyer-1", readyBuffer, phaser, logger);
        Buyer buyer2 = new Buyer("Buyer-2", readyBuffer, phaser, logger);
        Buyer buyer3 = new Buyer("Buyer-3", readyBuffer, phaser, logger);

        workers.add(supplier);
        workers.add(master);
        workers.add(packer);
        workers.add(buyer1);
        workers.add(buyer2);
        workers.add(buyer3);

        // +++++++++++++ СОЗДАЁМ ПОТОКИ +++++++++++++
        threads.add(new Thread(supplier, "Supplier"));
        threads.add(new Thread(master,   "Master"));
        threads.add(new Thread(packer,   "Packer"));
        threads.add(new Thread(buyer1,   "Buyer-1"));
        threads.add(new Thread(buyer2,   "Buyer-2"));
        threads.add(new Thread(buyer3,   "Buyer-3"));

        isRunning = true;

        // +++++++++++++ ЗАПУСК ПОТОКОВ +++++++++++++
        for (Thread t : threads) {
            t.start();
        }

        log("Фабрика запущена! Активных потоков: " + threads.size());
    }

    // ======================================================
    //                     ОСТАНОВКА ФАБРИКИ
    // ======================================================
    public void stop() {

        if (!isRunning) {
            log("Фабрика уже остановлена");
            return;
        }

        log("Остановка фабрики...");

        isRunning = false;

        // Останавливаем всех рабочих
        for (Runnable worker : workers) {
            if (worker instanceof AbstractWorker w) {
                w.stop();
            }
        }

        // Прерываем потоки
        for (Thread t : threads) {
            t.interrupt();
        }

        // Ждём завершения
        for (Thread t : threads) {
            try {
                t.join(2000);
            } catch (InterruptedException ignored) {}
        }

        workers.clear();
        threads.clear();

        log("Фабрика остановлена");
    }

    // ======================================================
    //                   ФАЗОВАЯ ЛОГИКА
    // ======================================================
    public int getPhase() {
        return phaser.getPhase();
    }

    public String getPhaseName() {
        return phaseName(phaser.getPhase());
    }

    // === Методы, которые нужны GUI ===
    public int getCurrentPhase() {
        return getPhase();
    }

    public String getCurrentPhaseName() {
        return getPhaseName();
    }

    private String phaseName(int phase) {
        return switch (phase % 4) {
            case 0 -> "SUPPLY";
            case 1 -> "PROCESS";
            case 2 -> "PACK";
            case 3 -> "CONSUME";
            default -> "UNKNOWN";
        };
    }

    // ======================================================
    //                   ГЕТТЕРЫ ДЛЯ GUI
    // ======================================================
    public TeaBuffer getRawBuffer() { return rawBuffer; }
    public TeaBuffer getMidBuffer() { return midBuffer; }
    public TeaBuffer getReadyBuffer() { return readyBuffer; }

    public boolean isRunning() { return isRunning; }

    // ======================================================
    //                          ЛОГ
    // ======================================================
    private void log(String msg) {
        logger.accept("[ФАБРИКА] " + msg);
    }
}
