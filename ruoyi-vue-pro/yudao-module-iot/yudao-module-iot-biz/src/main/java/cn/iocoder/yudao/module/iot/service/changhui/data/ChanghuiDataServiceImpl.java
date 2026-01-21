package cn.iocoder.yudao.module.iot.service.changhui.data;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataHistoryReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.data.ChanghuiDataSaveReqVO;
import cn.iocoder.yudao.module.iot.convert.changhui.ChanghuiDataConvert;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDataDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiDeviceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.changhui.ChanghuiDataMapper;
import cn.iocoder.yudao.module.iot.enums.changhui.ChanghuiIndicatorConstants;
import cn.iocoder.yudao.module.iot.service.changhui.device.ChanghuiDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 长辉设备数据采集 Service 实现类
 *
 * @author 长辉信息科技有限公司
 */
@Service
@Validated
@Slf4j
public class ChanghuiDataServiceImpl implements ChanghuiDataService {

    @Resource
    private ChanghuiDataMapper dataMapper;

    @Resource
    private ChanghuiDeviceService deviceService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveData(ChanghuiDataSaveReqVO reqVO) {
        ChanghuiDataDO data = ChanghuiDataConvert.INSTANCE.convert(reqVO);
        // 如果没有设备ID，尝试根据测站编码查找
        if (data.getDeviceId() == null && data.getStationCode() != null) {
            ChanghuiDeviceDO device = deviceService.getDeviceDOByStationCode(data.getStationCode());
            if (device != null) {
                data.setDeviceId(device.getId());
            }
        }
        // 如果没有时间戳，使用当前时间
        if (data.getTimestamp() == null) {
            data.setTimestamp(LocalDateTime.now());
        }
        dataMapper.insert(data);
        log.debug("[saveData] 数据保存成功: stationCode={}, indicator={}, value={}",
                data.getStationCode(), data.getIndicator(), data.getValue());
        return data.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveBatchData(List<ChanghuiDataSaveReqVO> reqVOList) {
        if (reqVOList == null || reqVOList.isEmpty()) {
            return;
        }
        for (ChanghuiDataSaveReqVO reqVO : reqVOList) {
            saveData(reqVO);
        }
        log.info("[saveBatchData] 批量保存数据成功: count={}", reqVOList.size());
    }

    @Override
    public ChanghuiDataRespVO queryData(String stationCode, String indicator) {
        ChanghuiDataDO data = dataMapper.selectLatestByStationCodeAndIndicator(stationCode, indicator);
        return enrichDataRespVO(ChanghuiDataConvert.INSTANCE.convert(data));
    }

    @Override
    public List<ChanghuiDataRespVO> queryMultipleData(String stationCode, List<String> indicators) {
        if (indicators == null || indicators.isEmpty()) {
            return Collections.emptyList();
        }
        // 获取每个指标的最新数据
        List<ChanghuiDataRespVO> result = new ArrayList<>();
        for (String indicator : indicators) {
            ChanghuiDataDO data = dataMapper.selectLatestByStationCodeAndIndicator(stationCode, indicator);
            if (data != null) {
                result.add(enrichDataRespVO(ChanghuiDataConvert.INSTANCE.convert(data)));
            }
        }
        return result;
    }

    @Override
    public Map<String, ChanghuiDataRespVO> getLatestData(String stationCode) {
        List<ChanghuiDataDO> dataList = dataMapper.selectLatestByStationCode(stationCode);
        return getLatestDataMap(dataList);
    }

    @Override
    public PageResult<ChanghuiDataRespVO> getHistoryData(ChanghuiDataHistoryReqVO reqVO) {
        PageResult<ChanghuiDataDO> pageResult = dataMapper.selectPage(
                reqVO.getStationCode(), reqVO.getDeviceId(), reqVO.getIndicator(),
                reqVO.getStartTime(), reqVO.getEndTime(),
                reqVO.getPageNo(), reqVO.getPageSize());
        PageResult<ChanghuiDataRespVO> result = ChanghuiDataConvert.INSTANCE.convertPage(pageResult);
        // 填充额外字段
        result.getList().forEach(this::enrichDataRespVO);
        return result;
    }

    @Override
    public byte[] exportHistoryData(ChanghuiDataHistoryReqVO reqVO) {
        // 查询所有数据（不分页）
        List<ChanghuiDataDO> dataList = dataMapper.selectList(
                reqVO.getStationCode(), reqVO.getDeviceId(), reqVO.getIndicator(),
                reqVO.getStartTime(), reqVO.getEndTime());
        
        // 转换为响应VO并填充额外字段
        List<ChanghuiDataRespVO> respList = ChanghuiDataConvert.INSTANCE.convertList(dataList);
        respList.forEach(this::enrichDataRespVO);
        
        // 生成CSV格式数据（简单实现，实际可使用EasyExcel）
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 写入CSV头
            String header = "ID,测站编码,指标类型,指标名称,数值,单位,采集时间,创建时间\n";
            baos.write(header.getBytes("UTF-8"));
            
            // 写入数据行
            for (ChanghuiDataRespVO data : respList) {
                String line = String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                        data.getId(),
                        data.getStationCode() != null ? data.getStationCode() : "",
                        data.getIndicator() != null ? data.getIndicator() : "",
                        data.getIndicatorName() != null ? data.getIndicatorName() : "",
                        data.getValue() != null ? data.getValue().toString() : "",
                        data.getUnit() != null ? data.getUnit() : "",
                        data.getTimestamp() != null ? data.getTimestamp().toString() : "",
                        data.getCreateTime() != null ? data.getCreateTime().toString() : "");
                baos.write(line.getBytes("UTF-8"));
            }
            
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("[exportHistoryData] 导出数据失败", e);
            return new byte[0];
        }
    }

    @Override
    public Map<String, ChanghuiDataRespVO> getLatestDataByDeviceId(Long deviceId) {
        List<ChanghuiDataDO> dataList = dataMapper.selectLatestByDeviceId(deviceId);
        return getLatestDataMap(dataList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataByDeviceId(Long deviceId) {
        int count = dataMapper.deleteByDeviceId(deviceId);
        log.info("[deleteDataByDeviceId] 删除设备数据: deviceId={}, count={}", deviceId, count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDataByStationCode(String stationCode) {
        int count = dataMapper.deleteByStationCode(stationCode);
        log.info("[deleteDataByStationCode] 删除设备数据: stationCode={}, count={}", stationCode, count);
    }

    /**
     * 从数据列表中提取每个指标的最新数据
     */
    private Map<String, ChanghuiDataRespVO> getLatestDataMap(List<ChanghuiDataDO> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return Collections.emptyMap();
        }
        // 按指标分组，取每组中时间最新的一条
        Map<String, ChanghuiDataDO> latestMap = dataList.stream()
                .collect(Collectors.toMap(
                        ChanghuiDataDO::getIndicator,
                        data -> data,
                        (existing, replacement) -> 
                            existing.getTimestamp().isAfter(replacement.getTimestamp()) ? existing : replacement
                ));
        // 转换为响应VO
        Map<String, ChanghuiDataRespVO> result = new HashMap<>();
        for (Map.Entry<String, ChanghuiDataDO> entry : latestMap.entrySet()) {
            result.put(entry.getKey(), enrichDataRespVO(ChanghuiDataConvert.INSTANCE.convert(entry.getValue())));
        }
        return result;
    }

    /**
     * 填充数据响应VO的额外字段
     */
    private ChanghuiDataRespVO enrichDataRespVO(ChanghuiDataRespVO respVO) {
        if (respVO == null) {
            return null;
        }
        // 填充指标名称和单位
        if (respVO.getIndicator() != null) {
            respVO.setIndicatorName(getIndicatorName(respVO.getIndicator()));
            respVO.setUnit(ChanghuiIndicatorConstants.getUnitByIndicator(respVO.getIndicator()));
        }
        return respVO;
    }

    /**
     * 获取指标名称
     */
    private String getIndicatorName(String indicator) {
        return switch (indicator) {
            case ChanghuiIndicatorConstants.WATER_LEVEL -> "水位";
            case ChanghuiIndicatorConstants.INSTANT_FLOW -> "瞬时流量";
            case ChanghuiIndicatorConstants.INSTANT_VELOCITY -> "瞬时流速";
            case ChanghuiIndicatorConstants.CUMULATIVE_FLOW -> "累计流量";
            case ChanghuiIndicatorConstants.GATE_POSITION -> "闸位";
            case ChanghuiIndicatorConstants.TEMPERATURE -> "温度";
            case ChanghuiIndicatorConstants.SEEPAGE_PRESSURE -> "渗压";
            case ChanghuiIndicatorConstants.LOAD -> "荷载";
            default -> indicator;
        };
    }

}
