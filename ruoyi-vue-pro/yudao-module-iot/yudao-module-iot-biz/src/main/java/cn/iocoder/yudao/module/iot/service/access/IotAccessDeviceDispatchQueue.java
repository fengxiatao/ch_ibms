package cn.iocoder.yudao.module.iot.service.access;

/**
 * 设备下发队列接口
 * 
 * 确保同一设备的下发任务串行执行，避免SDK冲突
 * 
 * Requirements: 10.2, 10.4
 *
 * @author Kiro
 */
public interface IotAccessDeviceDispatchQueue {

    /**
     * 提交下发任务到设备队列
     * 同一设备的任务会串行执行
     * 
     * @param deviceId 设备ID
     * @param task     下发任务
     */
    void submit(Long deviceId, Runnable task);

    /**
     * 获取设备队列状态
     * 
     * @param deviceId 设备ID
     * @return 队列状态
     */
    QueueStatus getQueueStatus(Long deviceId);

    /**
     * 取消设备的所有待执行任务
     * 
     * @param deviceId 设备ID
     * @return 取消的任务数量
     */
    int cancelPendingTasks(Long deviceId);

    /**
     * 获取全局队列统计信息
     * 
     * @return 统计信息
     */
    QueueStatistics getStatistics();

    /**
     * 关闭队列（优雅关闭）
     */
    void shutdown();

    /**
     * 队列状态
     */
    class QueueStatus {
        /**
         * 设备ID
         */
        private Long deviceId;
        
        /**
         * 待执行任务数
         */
        private int pendingCount;
        
        /**
         * 是否正在执行
         */
        private boolean executing;
        
        /**
         * 最后执行时间
         */
        private Long lastExecuteTime;

        public QueueStatus() {}

        public QueueStatus(Long deviceId, int pendingCount, boolean executing, Long lastExecuteTime) {
            this.deviceId = deviceId;
            this.pendingCount = pendingCount;
            this.executing = executing;
            this.lastExecuteTime = lastExecuteTime;
        }

        public Long getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(Long deviceId) {
            this.deviceId = deviceId;
        }

        public int getPendingCount() {
            return pendingCount;
        }

        public void setPendingCount(int pendingCount) {
            this.pendingCount = pendingCount;
        }

        public boolean isExecuting() {
            return executing;
        }

        public void setExecuting(boolean executing) {
            this.executing = executing;
        }

        public Long getLastExecuteTime() {
            return lastExecuteTime;
        }

        public void setLastExecuteTime(Long lastExecuteTime) {
            this.lastExecuteTime = lastExecuteTime;
        }
    }

    /**
     * 队列统计信息
     */
    class QueueStatistics {
        /**
         * 活跃设备队列数
         */
        private int activeDeviceCount;
        
        /**
         * 总待执行任务数
         */
        private int totalPendingCount;
        
        /**
         * 正在执行的设备数
         */
        private int executingDeviceCount;
        
        /**
         * 已完成任务总数
         */
        private long completedTaskCount;
        
        /**
         * 失败任务总数
         */
        private long failedTaskCount;

        public QueueStatistics() {}

        public QueueStatistics(int activeDeviceCount, int totalPendingCount, int executingDeviceCount,
                              long completedTaskCount, long failedTaskCount) {
            this.activeDeviceCount = activeDeviceCount;
            this.totalPendingCount = totalPendingCount;
            this.executingDeviceCount = executingDeviceCount;
            this.completedTaskCount = completedTaskCount;
            this.failedTaskCount = failedTaskCount;
        }

        public int getActiveDeviceCount() {
            return activeDeviceCount;
        }

        public void setActiveDeviceCount(int activeDeviceCount) {
            this.activeDeviceCount = activeDeviceCount;
        }

        public int getTotalPendingCount() {
            return totalPendingCount;
        }

        public void setTotalPendingCount(int totalPendingCount) {
            this.totalPendingCount = totalPendingCount;
        }

        public int getExecutingDeviceCount() {
            return executingDeviceCount;
        }

        public void setExecutingDeviceCount(int executingDeviceCount) {
            this.executingDeviceCount = executingDeviceCount;
        }

        public long getCompletedTaskCount() {
            return completedTaskCount;
        }

        public void setCompletedTaskCount(long completedTaskCount) {
            this.completedTaskCount = completedTaskCount;
        }

        public long getFailedTaskCount() {
            return failedTaskCount;
        }

        public void setFailedTaskCount(long failedTaskCount) {
            this.failedTaskCount = failedTaskCount;
        }
    }
}
