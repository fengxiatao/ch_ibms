package cn.iocoder.yudao.module.iot.dal.dataobject.patrolplan;

import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IoT 轮巡执行记录 DO
 *
 * @author 长辉信息
 */
@TableName(value = "iot_video_patrol_record", autoResultMap = true)
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IotVideoPatrolRecordDO extends BaseDO {

    /**
     * ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 计划ID
     */
    private Long planId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 场景ID
     */
    private Long sceneId;

    /**
     * 场景名称
     */
    private String sceneName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 实际停留时长(秒)
     */
    private Integer duration;

    /**
     * AI分析结果（JSON格式）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> aiAnalysisResult;

    /**
     * 是否异常
     * 0: 正常
     * 1: 异常
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean isAbnormal;

    /**
     * 异常类型
     */
    private String abnormalType;

    /**
     * 异常描述
     */
    private String abnormalDesc;

    /**
     * 抓拍图片URL列表（JSON数组）
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> snapshotUrls;

    /**
     * 录像文件URL
     */
    private String recordingUrl;

    /**
     * 是否已处理
     * 0: 未处理
     * 1: 已处理
     */
    @TableField(typeHandler = cn.iocoder.yudao.framework.mybatis.core.type.BooleanToIntTypeHandler.class)
    private Boolean handled;

    /**
     * 处理人
     */
    private String handler;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

}
