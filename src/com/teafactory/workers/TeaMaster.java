package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Мастер обработки - работает в фазе 1 (PROCESS)
 */
public class TeaMaster extends AbstractWorker {
    private final TeaBuffer rawBuffer;
    private final TeaBuffer midBuffer;

    public TeaMaster(TeaBuffer rawBuffer, TeaBuffer midBuffer, Phaser phaser, Consumer<String> logger) {
        super("МАСТЕР", 1, phaser, logger);
        this.rawBuffer = rawBuffer;
        this.midBuffer = midBuffer;
    }

    @Override
    protected void performWork() throws InterruptedException {
        // Проверяем наличие сырья
        int rawSize = rawBuffer.size();
        if (rawSize == 0) {
            log("⏳ Буфер сырья пуст, ожидание...");
        }

        // Берём сырьё (может заблокироваться)
        TeaBatch batch = rawBuffer.take();
        log(String.format("📥 Получено сырьё: %s [rawBuffer: %d/%d]",
                batch, rawBuffer.size(), rawBuffer.getCapacity()));

        log(String.format("🔧 Начинаю обработку: %s", batch));

        // Имитация обработки
        Thread.sleep(randomDelay());

        // Изменяем статус
        batch.setStage("PROCESSED");

        log(String.format("✨ Обработка завершена: %s", batch));

        // Проверяем место в промежуточном буфере
        int midSize = midBuffer.size();
        int midCapacity = midBuffer.getCapacity();

        if (midSize >= midCapacity) {
            log(String.format("⏳ Промежуточный буфер полон [%d/%d], ожидание...",
                    midSize, midCapacity));
        }

        // Кладём в промежуточный буфер
        midBuffer.put(batch);

        int newMidSize = midBuffer.size();
        log(String.format("✅ Партия %s → midBuffer [%d/%d]",
                batch, newMidSize, midCapacity));
    }

    // Метод randomDelay() наследуется от AbstractWorker
}