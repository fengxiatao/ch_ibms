package cn.iocoder.yudao.module.iot.service.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设备人员对账结果 DTO
 * 
 * 对比系统权限组人员与设备实际存储的用户后生成的差异报告
 *
 * @author 长辉信息科技
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceSyncCheckResult {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备IP
     */
    private String deviceIp;

    /**
     * 是否对账成功
     */
    private boolean success;

    /**
     * 错误信息（对账失败时）
     */
    private String errorMessage;

    /**
     * 系统应有人员列表（权限组关联的人员）
     */
    private List<PersonInfo> systemUsers;

    /**
     * 设备实际人员列表
     */
    private List<DeviceUserInfo> deviceUsers;

    /**
     * 已同步人员（系统和设备都有）
     */
    private List<SyncedUser> syncedUsers;

    /**
     * 系统多余人员（系统有、设备无，需要下发）
     */
    private List<PersonInfo> systemOnlyUsers;

    /**
     * 设备多余人员（设备有、系统无，野生用户）
     */
    private List<DeviceUserInfo> deviceOnlyUsers;

    /**
     * 统计信息
     */
    private SyncStatistics statistics;

    /**
     * 人员信息（系统侧）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PersonInfo {
        /**
         * 人员ID
         */
        private Long personId;

        /**
         * 人员编码（用于设备匹配）
         */
        private String personCode;

        /**
         * 人员姓名
         */
        private String personName;

        /**
         * 凭证类型列表（CARD, FACE, FINGERPRINT等）
         */
        private List<String> credentialTypes;

        /**
         * 卡号（如有）
         */
        private String cardNo;

        /**
         * 所属权限组名称
         */
        private String groupName;
    }

    /**
     * 设备用户信息（设备侧）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeviceUserInfo {
        /**
         * 设备上的用户ID
         */
        private String userId;

        /**
         * 用户名称
         */
        private String userName;

        /**
         * 卡号
         */
        private String cardNo;

        /**
         * 设备记录号（用于删除）
         */
        private Integer recNo;

        /**
         * 有效期开始
         */
        private String validStart;

        /**
         * 有效期结束
         */
        private String validEnd;

        /**
         * 是否有人脸
         */
        private Boolean hasFace;

        /**
         * 是否有指纹
         */
        private Boolean hasFingerprint;
    }

    /**
     * 已同步用户（匹配成功的）
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncedUser {
        /**
         * 系统人员信息
         */
        private PersonInfo systemUser;

        /**
         * 设备用户信息
         */
        private DeviceUserInfo deviceUser;

        /**
         * 是否完全一致
         */
        private boolean consistent;

        /**
         * 差异描述（如凭证不一致等）
         */
        private String difference;
    }

    /**
     * 同步统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncStatistics {
        /**
         * 系统人员总数
         */
        private int systemTotal;

        /**
         * 设备人员总数
         */
        private int deviceTotal;

        /**
         * 已同步数量
         */
        private int syncedCount;

        /**
         * 系统多余数量
         */
        private int systemOnlyCount;

        /**
         * 设备多余数量
         */
        private int deviceOnlyCount;

        /**
         * 同步率（已同步/系统总数）
         */
        private double syncRate;
    }

    /**
     * 创建失败结果
     */
    public static DeviceSyncCheckResult failure(Long deviceId, String deviceName, String error) {
        return DeviceSyncCheckResult.builder()
                .deviceId(deviceId)
                .deviceName(deviceName)
                .success(false)
                .errorMessage(error)
                .build();
    }

}
