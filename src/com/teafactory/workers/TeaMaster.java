package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;

import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Мастер обработки — работает в фазе 1 (PROCESS).
 * Берёт сырьё из rawBuffer, обрабатывает и кладёт в midBuffer.
 */
public class TeaMaster extends AbstractWorker {

    private final TeaBuffer rawBuffer;
    private final TeaBuffer midBuffer;

    public TeaMaster(TeaBuffer rawBuffer, TeaBuffer midBuffer, Phaser phaser, Consumer<String> logger) {
        super(phaser, logger);
        this.rawBuffer = rawBuffer;
        this.midBuffer = midBuffer;
    }

    @Override
    protected void doPhaseWork(int phase) throws InterruptedException {

        // Фаза PROCESS — фаза №1
        if (phase % 4 != 1) {
            return;
        }

        log("Вошёл в фазу PROCESS. rawBuffer="
                + rawBuffer.size() + "/" + rawBuffer.getCapacity());

        // Берём партию сырья (блокируется если пусто)
        TeaBatch batch = rawBuffer.take();

        log("Обрабатываю: " + batch
                + " | rawBuffer=" + rawBuffer.size()
                + "/" + rawBuffer.getCapacity());

        Thread.sleep(randomDelay());

        batch.setStage("PROCESSED");

        // Кладём в промежуточный буфер
        midBuffer.put(batch);

        log("Готово: " + batch
                + " → midBuffer=" + midBuffer.size()
                + "/" + midBuffer.getCapacity());
    }

    private long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }

    @Override
    protected void log(String msg) {
        logger.accept("[МАСТЕР] " + msg);
    }
}
