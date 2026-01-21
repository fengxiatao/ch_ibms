package cn.iocoder.yudao.module.iot.service.channel;

import lombok.Data;

/**
 * 通道同步结果
 *
 * @author IBMS Team
 */
@Data
public class SyncResult {

    /**
     * 同步的NVR设备数量
     */
    private Integer nvrCount = 0;

    /**
     * 成功同步的NVR数量
     */
    private Integer successCount = 0;

    /**
     * 失败的NVR数量
     */
    private Integer failCount = 0;

    /**
     * 新增的通道数量
     */
    private Integer addedChannels = 0;

    /**
     * 更新的通道数量
     */
    private Integer updatedChannels = 0;

    /**
     * 删除的通道数量
     */
    private Integer deletedChannels = 0;

    /**
     * 同步耗时（毫秒）
     */
    private Long duration = 0L;

    /**
     * 错误信息列表
     */
    private java.util.List<String> errors = new java.util.ArrayList<>();

    /**
     * 添加成功记录
     */
    public void addSuccess(int added, int updated, int deleted) {
        this.successCount++;
        this.addedChannels += added;
        this.updatedChannels += updated;
        this.deletedChannels += deleted;
    }

    /**
     * 添加失败记录
     */
    public void addFailure(String error) {
        this.failCount++;
        this.errors.add(error);
    }

    /**
     * 获取摘要信息
     */
    public String getSummary() {
        return String.format(
            "同步完成：NVR总数=%d, 成功=%d, 失败=%d, 新增通道=%d, 更新通道=%d, 删除通道=%d, 耗时=%dms",
            nvrCount, successCount, failCount, addedChannels, updatedChannels, deletedChannels, duration
        );
    }
}
