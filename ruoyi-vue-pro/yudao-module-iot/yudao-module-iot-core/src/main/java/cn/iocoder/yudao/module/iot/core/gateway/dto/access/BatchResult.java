package cn.iocoder.yudao.module.iot.core.gateway.dto.access;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量操作结果
 * 
 * @author 长辉信息科技有限公司
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResult {
    
    /**
     * 总数量
     */
    private int totalCount;
    
    /**
     * 成功数量
     */
    private int successCount;
    
    /**
     * 失败数量
     */
    private int failCount;
    
    /**
     * 详细结果列表
     */
    @Builder.Default
    private List<BatchResultItem> items = new ArrayList<>();
    
    /**
     * 是否全部成功
     */
    public boolean isAllSuccess() {
        return failCount == 0 && successCount > 0;
    }
    
    /**
     * 是否全部失败
     */
    public boolean isAllFailed() {
        return successCount == 0 && failCount > 0;
    }
    
    /**
     * 是否部分成功
     */
    public boolean isPartialSuccess() {
        return successCount > 0 && failCount > 0;
    }
    
    /**
     * 添加成功项
     */
    public void addSuccess(String id) {
        items.add(BatchResultItem.success(id));
        successCount++;
        totalCount++;
    }
    
    /**
     * 添加失败项
     */
    public void addFailure(String id, int errorCode, String errorMessage) {
        items.add(BatchResultItem.failure(id, errorCode, errorMessage));
        failCount++;
        totalCount++;
    }
    
    /**
     * 创建空结果
     */
    public static BatchResult empty() {
        return BatchResult.builder()
                .totalCount(0)
                .successCount(0)
                .failCount(0)
                .items(new ArrayList<>())
                .build();
    }
    
    /**
     * 创建单个成功结果
     */
    public static BatchResult singleSuccess(String id) {
        BatchResult result = empty();
        result.addSuccess(id);
        return result;
    }
    
    /**
     * 创建单个失败结果
     */
    public static BatchResult singleFailure(String id, int errorCode, String errorMessage) {
        BatchResult result = empty();
        result.addFailure(id, errorCode, errorMessage);
        return result;
    }
}
