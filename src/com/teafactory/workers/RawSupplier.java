package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import com.teafactory.model.TeaType;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Поставщик сырья - работает в фазе 0 (SUPPLY)
 */
public class RawSupplier extends AbstractWorker {
    private final TeaBuffer rawBuffer;

    public RawSupplier(TeaBuffer rawBuffer, Phaser phaser, Consumer<String> logger) {
        super("ПОСТАВЩИК", 0, phaser, logger);
        this.rawBuffer = rawBuffer;
    }

    @Override
    protected boolean performWork() throws InterruptedException {
        // Проверяем, есть ли место в буфере
        if (rawBuffer.size() >= rawBuffer.getCapacity()) {
            log("ℹ️ Буфер сырья полон, завершаем фазу");
            return false; // Буфер полон, завершаем фазу
        }

        log("📦 Подготовка новой партии сырья...");

        // Создаём партию
        TeaBatch batch = new TeaBatch(TeaType.random());
        batch.setStage("RAW");

        log(String.format("✨ Создана партия: %s", batch));

        // Имитация времени подготовки
        Thread.sleep(randomDelay());

        // Добавляем в буфер
        rawBuffer.put(batch);

        int newSize = rawBuffer.size();
        log(String.format("✅ Партия %s добавлена в буфер [%d/%d]", batch, newSize, rawBuffer.getCapacity()));

        // Продолжаем работу, если буфер не полон
        return rawBuffer.size() < rawBuffer.getCapacity();
    }
}