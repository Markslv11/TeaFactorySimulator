package com.teafactory.workers;

import com.teafactory.buffer.TeaBuffer;
import com.teafactory.model.TeaBatch;
import java.util.concurrent.Phaser;
import java.util.function.Consumer;

/**
 * Покупатель - работает в фазе 3 (CONSUME)
 * Таких потоков 3 штуки, они конкурируют за готовый продукт
 */
public class Buyer extends AbstractWorker {
    private final TeaBuffer readyBuffer;
    private int purchaseCount = 0;

    public Buyer(String buyerName, TeaBuffer readyBuffer, Phaser phaser, Consumer<String> logger) {
        super(buyerName, 3, phaser, logger);
        this.readyBuffer = readyBuffer;
    }

    @Override
    protected void performWork() throws InterruptedException {
        // Проверяем наличие готового продукта
        int readySize = readyBuffer.size();
        if (readySize == 0) {
            log("⏳ Буфер готовой продукции пуст, ожидание...");
        }

        // Берём готовый продукт (конкуренция с другими покупателями!)
        TeaBatch batch = readyBuffer.take();
        purchaseCount++;

        log(String.format("🛒 Приобретена партия: %s [readyBuffer: %d/%d]",
                batch, readyBuffer.size(), readyBuffer.getCapacity()));

        log(String.format("☕ Наслаждаюсь чаем: %s", batch));

        // Имитация времени потребления
        Thread.sleep(randomDelay());

        log(String.format("✅ Партия %s успешно потреблена! (Всего куплено: %d)",
                batch, purchaseCount));
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }
}