package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import com.teafactory.model.TeaType;

import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Поставщик сырья — работает в фазе 0 (SUPPLY).
 * Создаёт RAW партии и помещает их в rawBuffer.
 */
public class RawSupplier extends AbstractWorker {

    private final TeaBuffer rawBuffer;

    public RawSupplier(TeaBuffer rawBuffer, Phaser phaser, Consumer<String> logger) {
        super(phaser, logger);
        this.rawBuffer = rawBuffer;
    }

    @Override
    protected void doPhaseWork(int phase) throws InterruptedException {

        // Фаза 0 (SUPPLY)
        if (phase % 4 != 0) {
            return;
        }

        log("Вошёл в фазу SUPPLY. rawBuffer="
                + rawBuffer.size() + "/" + rawBuffer.getCapacity());

        // Создаём новую партию сырья
        TeaBatch batch = new TeaBatch(TeaType.random());
        batch.setStage("RAW");

        log("Создал сырьё: " + batch);

        // Пытаемся положить партию
        rawBuffer.put(batch);

        log("Партия добавлена: " + batch
                + " | rawBuffer=" + rawBuffer.size()
                + "/" + rawBuffer.getCapacity());

        Thread.sleep(randomDelay());
    }

    private long randomDelay() {
        return 300 + (long)(Math.random() * 600); // 300–900 ms
    }

    @Override
    protected void log(String msg) {
        logger.accept("[SUPPLIER] " + msg);
    }
}
