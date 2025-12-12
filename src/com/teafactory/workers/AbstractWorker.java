package com.teafactory.workers;

import java.util.concurrent.Phaser;
import java.util.function.Consumer;

public abstract class AbstractWorker implements Runnable {

    protected final Phaser phaser;
    protected final Consumer<String> logger;
    protected volatile boolean running = true;

    protected AbstractWorker(Phaser phaser, Consumer<String> logger) {
        this.phaser = phaser;
        this.logger = logger;
        this.phaser.register();
    }

    public void stop() {
        running = false;
    }

    @Override
    public final void run() {
        log("Thread started");

        try {
            while (running) {
                int phase = phaser.getPhase();

                // выполняем работу фазы
                doPhaseWork(phase);

                // ждём остальных
                phaser.arriveAndAwaitAdvance();
            }
        } catch (InterruptedException ignored) {
            log("Thread interrupted");
        } finally {
            log("Thread finished");
            phaser.arriveAndDeregister();
        }
    }

    protected abstract void doPhaseWork(int phase) throws InterruptedException;

    protected void log(String msg) {
        logger.accept("[" + getClass().getSimpleName() + "] " + msg);
    }
}
