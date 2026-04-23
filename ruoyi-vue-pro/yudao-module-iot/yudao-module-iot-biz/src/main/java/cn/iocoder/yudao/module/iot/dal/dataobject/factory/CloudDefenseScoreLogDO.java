package cn.iocoder.yudao.module.iot.dal.dataobject.factory;

import cn.iocoder.yudao.framework.tenant.core.db.TenantBaseDO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@TableName(value = "iot_cloud_defense_score_log", autoResultMap = true)
@KeySequence("iot_cloud_defense_score_log_seq")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CloudDefenseScoreLogDO extends TenantBaseDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer score;

    private String scoreLevel;

    private LocalDateTime scoreTime;

    private String remark;
}
