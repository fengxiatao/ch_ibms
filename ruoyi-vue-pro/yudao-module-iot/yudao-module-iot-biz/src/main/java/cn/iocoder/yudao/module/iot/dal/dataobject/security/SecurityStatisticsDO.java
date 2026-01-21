package cn.iocoder.yudao.module.iot.dal.dataobject.security;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.time.LocalDate;

/**
 * 安防统计数据 DO
 *
 * @author 长辉信息
 */
@TableName("security_statistics")
@KeySequence("security_statistics_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityStatisticsDO extends BaseDO {

    /**
     * ID
     */
    @TableId
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 统计小时（0-23）
     */
    private Integer statHour;

    /**
     * 统计类型（daily/hourly）
     */
    private String statType;

    // ========== 设备统计 ==========
    /**
     * 摄像头总数
     */
    private Integer totalCameras;

    /**
     * 在线摄像头数
     */
    private Integer onlineCameras;

    /**
     * 离线摄像头数
     */
    private Integer offlineCameras;

    // ========== 人脸统计 ==========
    /**
     * 人脸抓拍数
     */
    private Integer faceCaptureCount;

    /**
     * 人脸识别数
     */
    private Integer faceRecognitionCount;

    /**
     * 陌生人数
     */
    private Integer strangerCount;

    // ========== 车辆统计 ==========
    /**
     * 车辆抓拍数
     */
    private Integer vehicleCaptureCount;

    /**
     * 车辆进入数
     */
    private Integer vehicleInCount;

    /**
     * 车辆离开数
     */
    private Integer vehicleOutCount;

    // ========== 告警统计 ==========
    /**
     * 总告警数
     */
    private Integer totalAlarms;

    /**
     * 已处理告警数
     */
    private Integer handledAlarms;

    /**
     * 未处理告警数
     */
    private Integer unhandledAlarms;

    /**
     * 按级别统计告警（JSON）
     */
    private String alarmByLevel;

    /**
     * 按类型统计告警（JSON）
     */
    private String alarmByType;

    // ========== 其他统计 ==========
    /**
     * 分析任务数
     */
    private Integer analysisTaskCount;

    /**
     * 布控人员数
     */
    private Integer controlledPersonCount;

}



















