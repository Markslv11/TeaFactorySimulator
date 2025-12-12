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
    protected void performWork() throws InterruptedException {
        log("📦 Подготовка новой партии сырья...");

        // Создаём партию
        TeaBatch batch = new TeaBatch(TeaType.random());
        batch.setStage("RAW");

        log(String.format("✨ Создана партия: %s", batch));

        // Проверяем место в буфере
        int currentSize = rawBuffer.size();
        int capacity = rawBuffer.getCapacity();

        if (currentSize >= capacity) {
            log(String.format("⏳ Буфер сырья полон [%d/%d], ожидание...", currentSize, capacity));
        }

        // Имитация времени подготовки
        Thread.sleep(randomDelay());

        // Добавляем в буфер (может заблокироваться)
        rawBuffer.put(batch);

        int newSize = rawBuffer.size();
        log(String.format("✅ Партия %s добавлена в буфер [%d/%d]", batch, newSize, capacity));
    }

    // Метод randomDelay() наследуется от AbstractWorker
}