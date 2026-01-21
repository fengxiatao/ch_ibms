package cn.iocoder.yudao.module.iot.service.access;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 批量事务辅助类
 * 
 * 用于将大批量数据分批提交，每批使用独立事务，
 * 避免长事务导致的锁竞争问题。
 *
 * @author 芋道源码
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchTransactionHelper {

    /**
     * 默认批次大小
     */
    private static final int DEFAULT_BATCH_SIZE = 50;

    /**
     * 分批执行操作
     *
     * @param items 要处理的数据列表
     * @param batchProcessor 批次处理器
     * @param <T> 数据类型
     * @return 处理结果（成功批次数，失败批次数）
     */
    public <T> BatchResult executeBatch(List<T> items, Consumer<List<T>> batchProcessor) {
        return executeBatch(items, batchProcessor, DEFAULT_BATCH_SIZE);
    }

    /**
     * 分批执行操作（自定义批次大小）
     *
     * @param items 要处理的数据列表
     * @param batchProcessor 批次处理器
     * @param batchSize 批次大小
     * @param <T> 数据类型
     * @return 处理结果
     */
    public <T> BatchResult executeBatch(List<T> items, Consumer<List<T>> batchProcessor, int batchSize) {
        if (items == null || items.isEmpty()) {
            return new BatchResult(0, 0, new ArrayList<>());
        }

        int successCount = 0;
        int failCount = 0;
        List<BatchError> errors = new ArrayList<>();

        // 分批处理
        for (int i = 0; i < items.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, items.size());
            List<T> batch = items.subList(i, endIndex);
            int batchIndex = i / batchSize;

            try {
                executeBatchInNewTransaction(batch, batchProcessor);
                successCount++;
                log.debug("[executeBatch] 批次 {} 处理成功，数量: {}", batchIndex, batch.size());
            } catch (Exception e) {
                failCount++;
                errors.add(new BatchError(batchIndex, i, endIndex, e.getMessage()));
                log.error("[executeBatch] 批次 {} 处理失败: {}", batchIndex, e.getMessage());
                // 继续处理下一批，不中断
            }
        }

        log.info("[executeBatch] 批量处理完成: 总数={}, 成功批次={}, 失败批次={}", 
                items.size(), successCount, failCount);
        
        return new BatchResult(successCount, failCount, errors);
    }

    /**
     * 在新事务中执行批次操作
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public <T> void executeBatchInNewTransaction(List<T> batch, Consumer<List<T>> batchProcessor) {
        batchProcessor.accept(batch);
    }

    /**
     * 批量处理结果
     */
    public static class BatchResult {
        private final int successBatchCount;
        private final int failBatchCount;
        private final List<BatchError> errors;

        public BatchResult(int successBatchCount, int failBatchCount, List<BatchError> errors) {
            this.successBatchCount = successBatchCount;
            this.failBatchCount = failBatchCount;
            this.errors = errors;
        }

        public int getSuccessBatchCount() {
            return successBatchCount;
        }

        public int getFailBatchCount() {
            return failBatchCount;
        }

        public List<BatchError> getErrors() {
            return errors;
        }

        public boolean hasErrors() {
            return failBatchCount > 0;
        }
    }

    /**
     * 批次错误信息
     */
    public static class BatchError {
        private final int batchIndex;
        private final int startIndex;
        private final int endIndex;
        private final String errorMessage;

        public BatchError(int batchIndex, int startIndex, int endIndex, String errorMessage) {
            this.batchIndex = batchIndex;
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.errorMessage = errorMessage;
        }

        public int getBatchIndex() {
            return batchIndex;
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
