package cn.iocoder.yudao.module.iot.service.changhui.alarm;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.alarm.ChanghuiAlarmSaveReqVO;

/**
 * 长辉设备报警管理 Service 接口
 * 
 * <p>提供报警的保存、查询、确认等功能
 * <p>支持过力矩、过电流、过电压、低电压、水位超限、闸位超限等报警类型
 *
 * @author 长辉信息科技有限公司
 */
public interface ChanghuiAlarmService {

    /**
     * 保存报警
     *
     * @param reqVO 报警保存请求
     * @return 报警ID
     */
    Long saveAlarm(ChanghuiAlarmSaveReqVO reqVO);

    /**
     * 分页查询报警
     *
     * @param reqVO 分页查询请求
     * @return 分页结果
     */
    PageResult<ChanghuiAlarmRespVO> getAlarmPage(ChanghuiAlarmPageReqVO reqVO);

    /**
     * 确认报警
     *
     * @param alarmId 报警ID
     * @param ackUser 确认人
     */
    void acknowledgeAlarm(Long alarmId, String ackUser);

    /**
     * 获取未确认报警数量
     *
     * @return 未确认报警数量
     */
    Long getUnacknowledgedCount();

    /**
     * 获取指定设备的未确认报警数量
     *
     * @param stationCode 测站编码
     * @return 未确认报警数量
     */
    Long getUnacknowledgedCountByStationCode(String stationCode);

    /**
     * 获取报警详情
     *
     * @param alarmId 报警ID
     * @return 报警详情
     */
    ChanghuiAlarmRespVO getAlarm(Long alarmId);

    /**
     * 删除设备的所有报警
     *
     * @param deviceId 设备ID
     */
    void deleteAlarmByDeviceId(Long deviceId);

    /**
     * 删除设备的所有报警（根据测站编码）
     *
     * @param stationCode 测站编码
     */
    void deleteAlarmByStationCode(String stationCode);

}
