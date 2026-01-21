package cn.iocoder.yudao.module.iot.service.changhui.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataHistoryReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataSaveReqVO;

import java.util.List;
import java.util.Map;

/**
 * 长辉设备数据采集 Service 接口
 * 
 * <p>提供设备数据的保存、查询、历史数据查询和导出功能
 * <p>支持水位、流量、流速、闸位、温度、渗压、荷载等多种指标
 *
 * @author 长辉信息科技有限公司
 */
public interface ChanghuiDataService {

    /**
     * 保存数据
     *
     * @param reqVO 数据保存请求
     * @return 数据ID
     */
    Long saveData(ChanghuiDataSaveReqVO reqVO);

    /**
     * 批量保存数据
     *
     * @param reqVOList 数据保存请求列表
     */
    void saveBatchData(List<ChanghuiDataSaveReqVO> reqVOList);

    /**
     * 查询单个指标数据（最新）
     *
     * @param stationCode 测站编码
     * @param indicator   指标类型
     * @return 数据响应
     */
    ChanghuiDataRespVO queryData(String stationCode, String indicator);

    /**
     * 查询多个指标数据（最新）
     *
     * @param stationCode 测站编码
     * @param indicators  指标类型列表
     * @return 数据响应列表
     */
    List<ChanghuiDataRespVO> queryMultipleData(String stationCode, List<String> indicators);

    /**
     * 获取设备最新数据（所有指标）
     *
     * @param stationCode 测站编码
     * @return 指标类型到数据的映射
     */
    Map<String, ChanghuiDataRespVO> getLatestData(String stationCode);

    /**
     * 获取历史数据（分页）
     *
     * @param reqVO 历史数据查询请求
     * @return 分页结果
     */
    PageResult<ChanghuiDataRespVO> getHistoryData(ChanghuiDataHistoryReqVO reqVO);

    /**
     * 导出历史数据
     *
     * @param reqVO 历史数据查询请求
     * @return Excel文件字节数组
     */
    byte[] exportHistoryData(ChanghuiDataHistoryReqVO reqVO);

    /**
     * 根据设备ID获取最新数据
     *
     * @param deviceId 设备ID
     * @return 指标类型到数据的映射
     */
    Map<String, ChanghuiDataRespVO> getLatestDataByDeviceId(Long deviceId);

    /**
     * 删除设备的所有数据
     *
     * @param deviceId 设备ID
     */
    void deleteDataByDeviceId(Long deviceId);

    /**
     * 删除设备的所有数据（根据测站编码）
     *
     * @param stationCode 测站编码
     */
    void deleteDataByStationCode(String stationCode);

}
