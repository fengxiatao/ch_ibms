package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.record.AccessRecordPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.AccessRecordDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AccessRecordMapper extends BaseMapperX<AccessRecordDO> {

    default PageResult<AccessRecordDO> selectPage(AccessRecordPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AccessRecordDO>()
                .eqIfPresent(AccessRecordDO::getPersonId, reqVO.getPersonId())
                .eqIfPresent(AccessRecordDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(AccessRecordDO::getOpenType, reqVO.getOpenType())
                .eqIfPresent(AccessRecordDO::getOpenResult, reqVO.getOpenResult())
                .betweenIfPresent(AccessRecordDO::getAccessTime, reqVO.getAccessTime())
                .orderByDesc(AccessRecordDO::getId));
    }

    /**
     * 查询通行方式统计（按开门类型分组）
     */
    @Select("<script>" +
            "SELECT " +
            "  open_type as openType, " +
            "  COUNT(*) as count " +
            "FROM iot_access_record " +
            "WHERE deleted = 0 " +
            "  <if test='startTime != null'> AND access_time &gt;= #{startTime} </if>" +
            "  <if test='endTime != null'> AND access_time &lt;= #{endTime} </if>" +
            "GROUP BY open_type " +
            "ORDER BY count DESC" +
            "</script>")
    List<Map<String, Object>> selectAccessMethodStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 查询24小时人员流量统计（按小时分组）
     */
    @Select("<script>" +
            "SELECT " +
            "  HOUR(access_time) as hour, " +
            "  SUM(CASE WHEN direction = 1 THEN 1 ELSE 0 END) as inCount, " +
            "  SUM(CASE WHEN direction = 0 THEN 1 ELSE 0 END) as outCount " +
            "FROM iot_access_record " +
            "WHERE deleted = 0 " +
            "  <if test='startTime != null'> AND access_time &gt;= #{startTime} </if>" +
            "  <if test='endTime != null'> AND access_time &lt; #{endTime} </if>" +
            "GROUP BY HOUR(access_time) " +
            "ORDER BY hour ASC" +
            "</script>")
    List<Map<String, Object>> selectHourlyTrafficStatistics(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

}

