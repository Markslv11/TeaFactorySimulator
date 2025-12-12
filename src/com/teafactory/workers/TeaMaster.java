package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Мастер чайного производства - работает в фазе 1 (PROCESS)
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
    protected boolean performWork() throws InterruptedException {
        // Проверяем, есть ли что обрабатывать
        if (rawBuffer.size() == 0) {
            log("ℹ️ Буфер сырья пуст, завершаем фазу");
            return false;
        }

        // Проверяем, есть ли место куда положить
        if (midBuffer.size() >= midBuffer.getCapacity()) {
            log("ℹ️ Промежуточный буфер полон, завершаем фазу");
            return false;
        }

        // Берём из буфера сырья
        TeaBatch batch = rawBuffer.take();
        log(String.format("🔧 Взята партия на обработку: %s", batch));

        // Обработка
        Thread.sleep(randomDelay());
        batch.setStage("PROCESSED");

        log(String.format("⚙️ Обработка завершена: %s", batch));

        // Кладём в промежуточный буфер
        midBuffer.put(batch);

        int newSize = midBuffer.size();
        log(String.format("✅ Партия %s помещена в промежуточный буфер [%d/%d]", batch, newSize, midBuffer.getCapacity()));

        // Продолжаем, если есть ещё сырьё И есть место в выходном буфере
        return rawBuffer.size() > 0 && midBuffer.size() < midBuffer.getCapacity();
    }
}