package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;

import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Фасовщик — работает в фазе 2 (PACK)
 * Забирает обработанные партии из midBuffer,
 * упаковывает и складывает в readyBuffer.
 */
public class Packer extends AbstractWorker {

    private final TeaBuffer midBuffer;
    private final TeaBuffer readyBuffer;

    public Packer(TeaBuffer midBuffer, TeaBuffer readyBuffer, Phaser phaser, Consumer<String> logger) {
        super(phaser, logger);
        this.midBuffer = midBuffer;
        this.readyBuffer = readyBuffer;
    }

    @Override
    protected void doPhaseWork(int phase) throws InterruptedException {

        // Фаза PACK — фаза №2
        if (phase % 4 != 2) {
            return;
        }

        log("Вошёл в фазу PACK. midBuffer="
                + midBuffer.size() + "/" + midBuffer.getCapacity());

        // Берём обработанную партию
        TeaBatch batch = midBuffer.take();
        log("Упаковка: " + batch + " | midBuffer="
                + midBuffer.size() + "/" + midBuffer.getCapacity());

        Thread.sleep(randomDelay());

        // Устанавливаем статус
        batch.setStage("PACKED");

        // Кладём в буфер готовой продукции
        readyBuffer.put(batch);

        log("Упаковано: " + batch + " | readyBuffer="
                + readyBuffer.size() + "/" + readyBuffer.getCapacity());
    }

    private long randomDelay() {
        return 300 + (long)(Math.random() * 600);
    }

    @Override
    protected void log(String msg) {
        logger.accept("[PACKER] " + msg);
    }
}
