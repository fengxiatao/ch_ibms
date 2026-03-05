package cn.iocoder.yudao.module.iot.newgateway.plugins.accessgen1;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 门禁命令确认服务
 * 
 * <p>基于设备事件回调的命令确认机制，解决 SDK 返回值不可靠的问题。</p>
 * 
 * <h2>设计原理</h2>
 * <p>门禁设备执行命令后，会通过报警回调上报事件：</p>
 * <ul>
 *     <li>command=12673 → 远程开门事件（门真的开了）</li>
 *     <li>command=12658 → 关门事件</li>
 *     <li>command=12677 → 门模式变化事件</li>
 * </ul>
 * 
 * <p>SDK 的同步返回值只能确认"命令已发送"，不能确认"命令已执行"。
 * 设备的事件回调才是"真相来源"。</p>
 * 
 * <h2>使用流程</h2>
 * <ol>
 *     <li>发送命令前：调用 {@link #registerPendingCommand} 注册待确认命令</li>
 *     <li>发送命令：调用 SDK 发送命令</li>
 *     <li>收到事件回调：调用 {@link #confirmCommand} 确认命令执行</li>
 *     <li>等待确认：调用 {@link #waitForConfirmation} 等待确认结果</li>
 * </ol>
 * 
 * <h2>已知限制</h2>
 * <ul>
 *     <li>某些设备回调的 channelNo 总是为 0，会使用模糊匹配</li>
 *     <li>同设备同命令类型的并发命令可能导致匹配不准确</li>
 *     <li>建议避免对同一设备快速连续发送相同类型的命令</li>
 * </ul>
 * 
 * @author IoT Gateway Team
 */
@Slf4j
@Service
public class DoorCommandConfirmationService {

    private static final String LOG_PREFIX = "[DoorCommandConfirmation]";

    /**
     * 默认确认超时时间（秒）
     * <p>根据实测，设备上报开门事件通常在 1-2 秒内</p>
     */
    private static final int DEFAULT_CONFIRMATION_TIMEOUT_SECONDS = 5;

    /**
     * 命令过期时间（毫秒）
     * <p>防止内存泄漏，超过此时间的未完成命令将被清理</p>
     */
    private static final long COMMAND_EXPIRE_MS = 60_000; // 60秒

    /**
     * 待确认的命令 Map
     * Key: confirmationKey (deviceId:channelNo:commandType)
     * Value: PendingCommand 包含 future 和注册时间
     */
    private final Map<String, PendingCommand> pendingCommands = new ConcurrentHashMap<>();

    /**
     * 命令类型：开门
     */
    public static final String CMD_OPEN_DOOR = "OPEN_DOOR";

    /**
     * 命令类型：关门
     */
    public static final String CMD_CLOSE_DOOR = "CLOSE_DOOR";

    /**
     * 报警类型：远程开门事件
     */
    public static final int ALARM_REMOTE_OPEN_DOOR = 12673;

    /**
     * 报警类型：关门事件
     */
    public static final int ALARM_DOOR_CLOSED = 12658;

    /**
     * 注册待确认的命令
     *
     * @param deviceId    设备ID
     * @param channelNo   通道号
     * @param commandType 命令类型 (OPEN_DOOR, CLOSE_DOOR)
     * @return 确认 Key，用于后续操作
     */
    public String registerPendingCommand(Long deviceId, Integer channelNo, String commandType) {
        String key = buildKey(deviceId, channelNo, commandType);
        
        // 如果已存在相同的待确认命令，先取消它
        PendingCommand existing = pendingCommands.remove(key);
        if (existing != null && !existing.future.isDone()) {
            existing.future.complete(ConfirmationResult.cancelled("被新命令取代"));
            log.debug("{} 取消已存在的待确认命令: {}", LOG_PREFIX, key);
        }
        
        PendingCommand pendingCommand = new PendingCommand(new CompletableFuture<>(), System.currentTimeMillis(), channelNo);
        pendingCommands.put(key, pendingCommand);
        
        log.debug("{} 注册待确认命令: key={}, pendingCount={}", LOG_PREFIX, key, pendingCommands.size());
        return key;
    }

    /**
     * 确认命令已执行（由报警回调调用）
     * <p>
     * 注意：此方法只 complete future，不从 Map 中移除。
     * 因为设备事件可能先于 SDK 返回到达，如果此时移除，
     * 后续的 waitForConfirmation 会找不到命令。
     * </p>
     * <p>
     * 匹配策略：
     * 1. 首先尝试精确匹配 (deviceId:channelNo:commandType)
     * 2. 如果事件 channelNo=0（设备不区分通道），则模糊匹配同设备同命令类型的最早注册的命令
     * </p>
     *
     * @param deviceId  设备ID
     * @param channelNo 通道号（报警事件中的通道号，可能为 0 表示主通道）
     * @param alarmType 报警类型
     * @return 命令匹配结果，包含是否匹配成功和原始的 channelNo
     */
    public CommandMatchResult confirmCommand(Long deviceId, Integer channelNo, int alarmType) {
        String commandType = mapAlarmToCommandType(alarmType);
        if (commandType == null) {
            return CommandMatchResult.notMatched();
        }

        // 尝试精确匹配 (deviceId:channelNo:commandType)
        String exactKey = buildKey(deviceId, channelNo, commandType);
        PendingCommand pendingCommand = pendingCommands.get(exactKey);
        String matchedKey = exactKey;
        Integer matchedChannelNo = channelNo;
        
        // 如果精确匹配失败，尝试匹配所有通道（因为某些设备的报警事件 channelNo 可能为 0）
        if (pendingCommand == null && channelNo != null && channelNo == 0) {
            // 遍历查找同设备同命令类型的待确认命令，选择最早注册的（FIFO）
            String prefix = deviceId + ":";
            String suffix = ":" + commandType;
            long earliestTime = Long.MAX_VALUE;
            
            for (Map.Entry<String, PendingCommand> entry : pendingCommands.entrySet()) {
                if (entry.getKey().startsWith(prefix) && entry.getKey().endsWith(suffix)) {
                    PendingCommand candidate = entry.getValue();
                    // 选择未完成且注册时间最早的命令
                    if (!candidate.future.isDone() && candidate.registerTime < earliestTime) {
                        pendingCommand = candidate;
                        matchedKey = entry.getKey();
                        matchedChannelNo = candidate.originalChannelNo;  // 使用原始注册的 channelNo
                        earliestTime = candidate.registerTime;
                    }
                }
            }
            
            if (pendingCommand != null) {
                log.debug("{} 模糊匹配到待确认命令（FIFO）: alarmChannelNo=0, matchedKey={}, originalChannel={}", 
                        LOG_PREFIX, matchedKey, pendingCommand.originalChannelNo);
            }
        }
        
        if (pendingCommand != null && !pendingCommand.future.isDone()) {
            pendingCommand.future.complete(ConfirmationResult.success(alarmType));
            log.info("{} ✅ 命令已确认: deviceId={}, eventChannelNo={}, alarmType={}, matchedKey={}, matchedChannelNo={}", 
                    LOG_PREFIX, deviceId, channelNo, alarmType, matchedKey, matchedChannelNo);
            return CommandMatchResult.matched(matchedChannelNo);
        }
        
        return CommandMatchResult.notMatched();
    }
    
    /**
     * 命令匹配结果
     * <p>用于返回 confirmCommand 的结果，包含是否匹配成功和匹配的 channelNo</p>
     */
    public static class CommandMatchResult {
        private final boolean matched;
        private final Integer channelNo;
        
        private CommandMatchResult(boolean matched, Integer channelNo) {
            this.matched = matched;
            this.channelNo = channelNo;
        }
        
        public static CommandMatchResult matched(Integer channelNo) {
            return new CommandMatchResult(true, channelNo);
        }
        
        public static CommandMatchResult notMatched() {
            return new CommandMatchResult(false, null);
        }
        
        public boolean isMatched() {
            return matched;
        }
        
        /** 获取匹配的 channelNo（用户实际操作的门） */
        public Integer getChannelNo() {
            return channelNo;
        }
    }

    /**
     * 等待命令确认
     * <p>
     * 注意：由于设备事件可能先于 SDK 返回到达，confirmCommand 可能已经 complete 了 future。
     * 此时 future.get() 会立即返回，无需等待。
     * </p>
     *
     * @param confirmationKey 确认 Key（由 registerPendingCommand 返回）
     * @param timeoutSeconds  超时时间（秒），null 使用默认值
     * @return 确认结果
     */
    public ConfirmationResult waitForConfirmation(String confirmationKey, Integer timeoutSeconds) {
        PendingCommand pendingCommand = pendingCommands.get(confirmationKey);
        if (pendingCommand == null) {
            log.warn("{} 未找到待确认命令: key={}", LOG_PREFIX, confirmationKey);
            return ConfirmationResult.notFound();
        }

        int timeout = timeoutSeconds != null ? timeoutSeconds : DEFAULT_CONFIRMATION_TIMEOUT_SECONDS;
        
        try {
            // 如果 confirmCommand 已经 complete 了 future，这里会立即返回
            ConfirmationResult result = pendingCommand.future.get(timeout, TimeUnit.SECONDS);
            log.debug("{} 等待确认完成: key={}, result={}", LOG_PREFIX, confirmationKey, result);
            return result;
        } catch (java.util.concurrent.TimeoutException e) {
            // 超时未收到确认
            log.warn("{} 命令确认超时: key={}, timeout={}s", LOG_PREFIX, confirmationKey, timeout);
            return ConfirmationResult.timeout(timeout);
        } catch (Exception e) {
            log.error("{} 等待确认异常: key={}", LOG_PREFIX, confirmationKey, e);
            return ConfirmationResult.error(e.getMessage());
        } finally {
            // 无论成功、超时还是异常，都移除命令
            pendingCommands.remove(confirmationKey);
        }
    }

    /**
     * 取消待确认命令
     *
     * @param confirmationKey 确认 Key
     */
    public void cancelPendingCommand(String confirmationKey) {
        PendingCommand pendingCommand = pendingCommands.remove(confirmationKey);
        if (pendingCommand != null && !pendingCommand.future.isDone()) {
            pendingCommand.future.complete(ConfirmationResult.cancelled("手动取消"));
            log.debug("{} 取消待确认命令: key={}", LOG_PREFIX, confirmationKey);
        }
    }

    /**
     * 构建确认 Key
     */
    private String buildKey(Long deviceId, Integer channelNo, String commandType) {
        return deviceId + ":" + channelNo + ":" + commandType;
    }

    /**
     * 将报警类型映射为命令类型
     */
    private String mapAlarmToCommandType(int alarmType) {
        return switch (alarmType) {
            case ALARM_REMOTE_OPEN_DOOR -> CMD_OPEN_DOOR;
            case ALARM_DOOR_CLOSED -> CMD_CLOSE_DOOR;
            default -> null;
        };
    }

    /**
     * 获取待确认命令数量（用于监控）
     */
    public int getPendingCount() {
        return pendingCommands.size();
    }

    /**
     * 定时清理过期的待确认命令（防止内存泄漏）
     * <p>每30秒执行一次，清理超过 COMMAND_EXPIRE_MS 未完成的命令</p>
     */
    @Scheduled(fixedRate = 30000)
    public void cleanupExpiredCommands() {
        if (pendingCommands.isEmpty()) {
            return;
        }
        
        long now = System.currentTimeMillis();
        int cleanedCount = 0;
        
        Iterator<Map.Entry<String, PendingCommand>> iterator = pendingCommands.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PendingCommand> entry = iterator.next();
            PendingCommand cmd = entry.getValue();
            
            // 清理已完成的或已过期的命令
            if (cmd.future.isDone()) {
                iterator.remove();
                cleanedCount++;
            } else if (now - cmd.registerTime > COMMAND_EXPIRE_MS) {
                // 过期未完成的命令，complete 为超时
                cmd.future.complete(ConfirmationResult.timeout((int)(COMMAND_EXPIRE_MS / 1000)));
                iterator.remove();
                cleanedCount++;
                log.warn("{} 清理过期命令: key={}, age={}ms", LOG_PREFIX, entry.getKey(), now - cmd.registerTime);
            }
        }
        
        if (cleanedCount > 0) {
            log.debug("{} 清理完成: cleaned={}, remaining={}", LOG_PREFIX, cleanedCount, pendingCommands.size());
        }
    }

    // ==================== 内部类 ====================
    
    /**
     * 待确认命令封装
     */
    private static class PendingCommand {
        final CompletableFuture<ConfirmationResult> future;
        final long registerTime;
        final Integer originalChannelNo;
        
        PendingCommand(CompletableFuture<ConfirmationResult> future, long registerTime, Integer originalChannelNo) {
            this.future = future;
            this.registerTime = registerTime;
            this.originalChannelNo = originalChannelNo;
        }
    }

    /**
     * 确认结果
     */
    public static class ConfirmationResult {
        private final Status status;
        private final Integer alarmType;
        private final String message;

        private ConfirmationResult(Status status, Integer alarmType, String message) {
            this.status = status;
            this.alarmType = alarmType;
            this.message = message;
        }

        public static ConfirmationResult success(int alarmType) {
            return new ConfirmationResult(Status.CONFIRMED, alarmType, "设备已确认执行");
        }

        public static ConfirmationResult timeout(int timeoutSeconds) {
            return new ConfirmationResult(Status.TIMEOUT, null, 
                    "等待设备确认超时(" + timeoutSeconds + "秒)，命令可能未执行");
        }

        public static ConfirmationResult cancelled(String reason) {
            return new ConfirmationResult(Status.CANCELLED, null, reason);
        }

        public static ConfirmationResult notFound() {
            return new ConfirmationResult(Status.NOT_FOUND, null, "未找到待确认命令");
        }

        public static ConfirmationResult error(String message) {
            return new ConfirmationResult(Status.ERROR, null, message);
        }

        public boolean isConfirmed() {
            return status == Status.CONFIRMED;
        }

        public boolean isTimeout() {
            return status == Status.TIMEOUT;
        }

        public Status getStatus() {
            return status;
        }

        public Integer getAlarmType() {
            return alarmType;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "ConfirmationResult{status=" + status + ", alarmType=" + alarmType + ", message='" + message + "'}";
        }

        public enum Status {
            /** 设备已确认执行 */
            CONFIRMED,
            /** 等待确认超时 */
            TIMEOUT,
            /** 命令被取消 */
            CANCELLED,
            /** 未找到待确认命令 */
            NOT_FOUND,
            /** 发生错误 */
            ERROR
        }
    }
}
