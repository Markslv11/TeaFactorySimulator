package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Покупатель - работает в фазе 3 (CONSUME)
 * Несколько покупателей конкурируют за товар
 */
public class Buyer extends AbstractWorker {
    private final TeaBuffer readyBuffer;
    private int purchaseCount = 0;

    public Buyer(String workerName, TeaBuffer readyBuffer, Phaser phaser, Consumer<String> logger) {
        super(workerName, 3, phaser, logger);
        this.readyBuffer = readyBuffer;
    }

    @Override
    protected boolean performWork() throws InterruptedException {
        // Проверяем, есть ли товар
        if (readyBuffer.size() == 0) {
            log("ℹ️ Буфер готовой продукции пуст, завершаем фазу");
            return false;
        }

        // Пробуем купить
        TeaBatch batch = readyBuffer.take();

        log(String.format("🛒 Куплена партия: %s", batch));

        // Имитация времени покупки
        Thread.sleep(randomDelay());

        purchaseCount++;
        log(String.format("💰 Покупка завершена: %s (Всего покупок: %d)", batch, purchaseCount));

        // Продолжаем покупать, если есть ещё товар
        return readyBuffer.size() > 0;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }
}