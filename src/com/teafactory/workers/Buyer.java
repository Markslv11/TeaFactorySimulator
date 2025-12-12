package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;

import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Покупатель — работает в фазе 3 (CONSUME)
 * Забирает упакованные партии из readyBuffer.
 */
public class Buyer extends AbstractWorker {

    private final TeaBuffer readyBuffer;
    private final String buyerName;

    public Buyer(String buyerName, TeaBuffer readyBuffer, Phaser phaser, Consumer<String> logger) {
        super(phaser, logger);
        this.buyerName = buyerName;
        this.readyBuffer = readyBuffer;
    }

    @Override
    protected void doPhaseWork(int phase) throws InterruptedException {

        // Фаза 3 (CONSUME)
        if (phase % 4 != 3) {
            return;
        }

        log("Вошёл в фазу CONSUME. readyBuffer="
                + readyBuffer.size() + "/" + readyBuffer.getCapacity());

        TeaBatch batch = readyBuffer.take();

        log("Получил чай: " + batch
                + " | readyBuffer=" + readyBuffer.size()
                + "/" + readyBuffer.getCapacity());

        Thread.sleep(randomDelay());

        log("Выпил чай: " + batch);
    }

    private long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }

    @Override
    protected void log(String msg) {
        logger.accept("[" + buyerName + "] " + msg);
    }
}
