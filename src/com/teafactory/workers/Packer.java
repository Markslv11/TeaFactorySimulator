package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Фасовщик - работает в фазе 2 (PACK)
 */
public class Packer extends AbstractWorker {
    private final TeaBuffer midBuffer;
    private final TeaBuffer readyBuffer;

    public Packer(TeaBuffer midBuffer, TeaBuffer readyBuffer, Phaser phaser, Consumer<String> logger) {
        super("ФАСОВЩИК", 2, phaser, logger);
        this.midBuffer = midBuffer;
        this.readyBuffer = readyBuffer;
    }

    @Override
    protected boolean performWork() throws InterruptedException {
        // Проверяем, есть ли что упаковывать
        if (midBuffer.size() == 0) {
            log("ℹ️ Промежуточный буфер пуст, завершаем фазу");
            return false;
        }

        // Проверяем, есть ли место куда положить
        if (readyBuffer.size() >= readyBuffer.getCapacity()) {
            log("ℹ️ Буфер готовой продукции полон, завершаем фазу");
            return false;
        }

        // Берём из промежуточного буфера
        TeaBatch batch = midBuffer.take();
        log(String.format("📦 Взята партия на упаковку: %s", batch));

        // Упаковка
        Thread.sleep(randomDelay());
        batch.setStage("PACKED");

        log(String.format("🎁 Упаковка завершена: %s", batch));

        // Кладём в буфер готовой продукции
        readyBuffer.put(batch);

        int newSize = readyBuffer.size();
        log(String.format("✅ Партия %s готова к продаже [%d/%d]", batch, newSize, readyBuffer.getCapacity()));

        // Продолжаем, если есть ещё товар для упаковки И есть место в выходном буфере
        return midBuffer.size() > 0 && readyBuffer.size() < readyBuffer.getCapacity();
    }
}