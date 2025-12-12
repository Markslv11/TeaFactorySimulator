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
    protected void performWork() throws InterruptedException {
        // Проверяем наличие обработанного чая
        int midSize = midBuffer.size();
        if (midSize == 0) {
            log("⏳ Промежуточный буфер пуст, ожидание...");
        }

        // Берём из промежуточного буфера
        TeaBatch batch = midBuffer.take();
        log(String.format("📥 Получена партия для упаковки: %s [midBuffer: %d/%d]",
                batch, midBuffer.size(), midBuffer.getCapacity()));

        log(String.format("📦 Начинаю упаковку: %s", batch));

        // Имитация упаковки
        Thread.sleep(randomDelay());

        // Изменяем статус
        batch.setStage("PACKED");

        log(String.format("✨ Упаковка завершена: %s", batch));

        // Проверяем место в буфере готовой продукции
        int readySize = readyBuffer.size();
        int readyCapacity = readyBuffer.getCapacity();

        if (readySize >= readyCapacity) {
            log(String.format("⏳ Буфер готовой продукции полон [%d/%d], ожидание...",
                    readySize, readyCapacity));
        }

        // Кладём в буфер готовой продукции
        readyBuffer.put(batch);

        int newReadySize = readyBuffer.size();
        log(String.format("✅ Партия %s → readyBuffer [%d/%d]",
                batch, newReadySize, readyCapacity));
    }
}