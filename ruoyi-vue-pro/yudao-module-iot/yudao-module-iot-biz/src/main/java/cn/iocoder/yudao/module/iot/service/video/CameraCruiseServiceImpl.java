package cn.iocoder.yudao.module.iot.service.video;

import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraCruisePointSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.video.vo.CameraCruiseSaveReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.channel.IotDeviceChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruiseDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraCruisePointDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.video.CameraPresetDO;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraCruiseMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraCruisePointMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.video.CameraPresetMapper;
import cn.iocoder.yudao.module.iot.service.channel.IotDeviceChannelService;
import cn.iocoder.yudao.module.iot.service.video.nvr.NvrCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil.exception;
import static cn.iocoder.yudao.module.iot.enums.ErrorCodeConstants.*;

/**
 * 摄像头巡航路线 Service 实现类
 *
 * @author 芋道源码
 */
@Service
@Slf4j
public class CameraCruiseServiceImpl implements CameraCruiseService {

    private static final String LOG_PREFIX = "[CameraCruise]";

    @Resource
    private CameraCruiseMapper cruiseMapper;

    @Resource
    private CameraCruisePointMapper cruisePointMapper;

    @Resource
    private CameraPresetMapper presetMapper;

    @Resource
    private NvrCommandService nvrCommandService;

    @Resource
    private IotDeviceChannelService channelService;

    /**
     * 巡航任务管理器
     * key: 巡航路线ID
     * value: 巡航任务Future
     */
    private final Map<Long, Future<?>> cruiseTasks = new ConcurrentHashMap<>();

    /**
     * 巡航任务执行线程池
     */
    private final ScheduledExecutorService cruiseExecutor = Executors.newScheduledThreadPool(
            4, r -> {
                Thread t = new Thread(r, "cruise-executor");
                t.setDaemon(true);
                return t;
            }
    );

    @PreDestroy
    public void destroy() {
        log.info("{} 关闭巡航任务线程池...", LOG_PREFIX);
        cruiseTasks.values().forEach(future -> future.cancel(true));
        cruiseTasks.clear();
        cruiseExecutor.shutdownNow();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCruise(CameraCruiseSaveReqVO createReqVO) {
        // 校验巡航路线名称是否已存在
        CameraCruiseDO existCruise = cruiseMapper.selectByChannelIdAndName(
                createReqVO.getChannelId(), createReqVO.getCruiseName());
        if (existCruise != null) {
            throw exception(CAMERA_CRUISE_NAME_EXISTS);
        }

        // 插入巡航路线
        CameraCruiseDO cruise = BeanUtils.toBean(createReqVO, CameraCruiseDO.class);
        cruise.setStatus(0); // 默认停止状态
        cruiseMapper.insert(cruise);

        // 插入巡航点
        saveCruisePoints(cruise.getId(), createReqVO.getPoints());

        log.info("[createCruise] 创建巡航路线成功: channelId={}, cruiseName={}, pointCount={}",
                cruise.getChannelId(), cruise.getCruiseName(), createReqVO.getPoints().size());

        // 异步同步到设备（不阻塞主流程，失败不影响创建）
        final Long cruiseId = cruise.getId();
        cruiseExecutor.submit(() -> {
            try {
                Thread.sleep(500); // 等待事务提交
                syncCruiseToDeviceSilently(cruiseId, 1);
            } catch (Exception e) {
                log.warn("{} 创建后自动同步到设备失败（不影响创建）: id={}, error={}", 
                        LOG_PREFIX, cruiseId, e.getMessage());
            }
        });

        return cruise.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCruise(CameraCruiseSaveReqVO updateReqVO) {
        // 校验存在
        validateCruiseExists(updateReqVO.getId());

        // 校验名称是否重复
        CameraCruiseDO existCruise = cruiseMapper.selectByChannelIdAndName(
                updateReqVO.getChannelId(), updateReqVO.getCruiseName());
        if (existCruise != null && !existCruise.getId().equals(updateReqVO.getId())) {
            throw exception(CAMERA_CRUISE_NAME_EXISTS);
        }

        // 更新巡航路线
        CameraCruiseDO updateObj = BeanUtils.toBean(updateReqVO, CameraCruiseDO.class);
        cruiseMapper.updateById(updateObj);

        // 删除旧的巡航点
        cruisePointMapper.deleteByCruiseId(updateReqVO.getId());

        // 插入新的巡航点
        saveCruisePoints(updateReqVO.getId(), updateReqVO.getPoints());

        log.info("[updateCruise] 更新巡航路线成功: id={}, cruiseName={}, pointCount={}",
                updateObj.getId(), updateObj.getCruiseName(), updateReqVO.getPoints().size());

        // 异步同步到设备（不阻塞主流程，失败不影响更新）
        final Long cruiseId = updateReqVO.getId();
        cruiseExecutor.submit(() -> {
            try {
                Thread.sleep(500); // 等待事务提交
                syncCruiseToDeviceSilently(cruiseId, 1);
            } catch (Exception e) {
                log.warn("{} 更新后自动同步到设备失败（不影响更新）: id={}, error={}", 
                        LOG_PREFIX, cruiseId, e.getMessage());
            }
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCruise(Long id) {
        // 校验存在
        validateCruiseExists(id);

        // 删除巡航路线
        cruiseMapper.deleteById(id);

        // 删除巡航点
        cruisePointMapper.deleteByCruiseId(id);

        log.info("[deleteCruise] 删除巡航路线成功: id={}", id);
    }

    @Override
    public CameraCruiseDO getCruise(Long id) {
        return cruiseMapper.selectById(id);
    }

    @Override
    public List<CameraCruiseDO> getCruiseListByChannelId(Long channelId) {
        return cruiseMapper.selectListByChannelId(channelId);
    }

    @Override
    public void startCruise(Long id) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 如果已经在运行，先停止
        if (cruiseTasks.containsKey(id)) {
            log.info("{} 巡航已在运行，先停止: id={}", LOG_PREFIX, id);
            doStopCruise(id);
        }

        // 获取巡航点列表（至少需要2个预设点才能启动巡航）
        List<CameraCruisePointDO> cruisePoints = cruisePointMapper.selectListByCruiseId(id);
        if (cruisePoints.size() < 2) {
            throw exception(CAMERA_CRUISE_POINTS_TOO_FEW);
        }

        // 获取通道信息
        IotDeviceChannelDO channel = channelService.getChannel(cruise.getChannelId());
        if (channel == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 获取设备连接信息
        Long deviceId = channel.getDeviceId();
        String targetIp = channel.getTargetIp();
        String username = channel.getUsername() != null ? channel.getUsername() : "admin";
        String password = channel.getPassword() != null ? channel.getPassword() : "admin123";
        Integer targetChannelNo = channel.getTargetChannelNo() != null ? channel.getTargetChannelNo() : channel.getChannelNo();

        log.info("{} 启动巡航: id={}, cruiseName={}, channelId={}, deviceId={}, targetIp={}, pointCount={}", 
                LOG_PREFIX, id, cruise.getCruiseName(), cruise.getChannelId(), deviceId, targetIp, cruisePoints.size());

        // 启动巡航任务
        CruiseTask task = new CruiseTask(
                id,
                cruise,
                cruisePoints,
                deviceId,
                targetChannelNo,
                targetIp,
                username,
                password
        );

        Future<?> future = cruiseExecutor.submit(task);
        cruiseTasks.put(id, future);

        // 更新状态为运行中
        CameraCruiseDO updateObj = new CameraCruiseDO();
        updateObj.setId(id);
        updateObj.setStatus(1);
        cruiseMapper.updateById(updateObj);

        log.info("{} 巡航任务已启动: id={}, cruiseName={}", LOG_PREFIX, id, cruise.getCruiseName());
    }

    @Override
    public void stopCruise(Long id) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 停止巡航任务
        doStopCruise(id);

        // 更新状态为停止
        CameraCruiseDO updateObj = new CameraCruiseDO();
        updateObj.setId(id);
        updateObj.setStatus(0);
        cruiseMapper.updateById(updateObj);

        log.info("{} 停止巡航: id={}, cruiseName={}", LOG_PREFIX, id, cruise.getCruiseName());
    }

    /**
     * 停止巡航任务
     */
    private void doStopCruise(Long id) {
        Future<?> future = cruiseTasks.remove(id);
        if (future != null) {
            future.cancel(true);
            log.info("{} 巡航任务已取消: id={}", LOG_PREFIX, id);
        }
    }

    /**
     * 巡航任务
     */
    private class CruiseTask implements Runnable {
        private final Long cruiseId;
        private final CameraCruiseDO cruise;
        private final List<CameraCruisePointDO> cruisePoints;
        private final Long deviceId;
        private final Integer channelNo;
        private final String targetIp;
        private final String username;
        private final String password;

        CruiseTask(Long cruiseId, CameraCruiseDO cruise, List<CameraCruisePointDO> cruisePoints,
                   Long deviceId, Integer channelNo, String targetIp, String username, String password) {
            this.cruiseId = cruiseId;
            this.cruise = cruise;
            this.cruisePoints = cruisePoints;
            this.deviceId = deviceId;
            this.channelNo = channelNo;
            this.targetIp = targetIp;
            this.username = username;
            this.password = password;
        }

        @Override
        public void run() {
            log.info("{} 巡航任务开始执行: cruiseId={}, cruiseName={}, pointCount={}", 
                    LOG_PREFIX, cruiseId, cruise.getCruiseName(), cruisePoints.size());

            int defaultDwellTime = cruise.getDwellTime() != null ? cruise.getDwellTime() : 10;
            boolean loopEnabled = Boolean.TRUE.equals(cruise.getLoopEnabled());

            try {
                do {
                    for (int i = 0; i < cruisePoints.size(); i++) {
                        // 检查是否被中断
                        if (Thread.currentThread().isInterrupted()) {
                            log.info("{} 巡航任务被中断: cruiseId={}", LOG_PREFIX, cruiseId);
                            return;
                        }

                        CameraCruisePointDO point = cruisePoints.get(i);
                        
                        // 获取预设点信息
                        CameraPresetDO preset = presetMapper.selectById(point.getPresetId());
                        if (preset == null) {
                            log.warn("{} 预设点不存在，跳过: cruiseId={}, presetId={}", 
                                    LOG_PREFIX, cruiseId, point.getPresetId());
                            continue;
                        }

                        log.info("{} 巡航转到预设点: cruiseId={}, point={}/{}, presetNo={}, presetName={}", 
                                LOG_PREFIX, cruiseId, i + 1, cruisePoints.size(), 
                                preset.getPresetNo(), preset.getPresetName());

                        // 发送 GOTO 预设点命令
                        try {
                            nvrCommandService.presetControl(
                                    deviceId,
                                    channelNo,
                                    preset.getPresetNo(),
                                    "GOTO",
                                    targetIp,
                                    username,
                                    password,
                                    null
                            );
                        } catch (Exception e) {
                            log.error("{} GOTO预设点命令发送失败: cruiseId={}, presetNo={}, error={}", 
                                    LOG_PREFIX, cruiseId, preset.getPresetNo(), e.getMessage());
                        }

                        // 停留时间
                        int dwellTime = point.getDwellTime() != null ? point.getDwellTime() : defaultDwellTime;
                        
                        log.debug("{} 预设点停留: cruiseId={}, presetNo={}, dwellTime={}s", 
                                LOG_PREFIX, cruiseId, preset.getPresetNo(), dwellTime);

                        try {
                            Thread.sleep(dwellTime * 1000L);
                        } catch (InterruptedException e) {
                            log.info("{} 巡航任务被中断(停留期间): cruiseId={}", LOG_PREFIX, cruiseId);
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }

                    if (loopEnabled) {
                        log.info("{} 巡航完成一轮，继续循环: cruiseId={}", LOG_PREFIX, cruiseId);
                    }

                } while (loopEnabled && !Thread.currentThread().isInterrupted());

                log.info("{} 巡航任务完成: cruiseId={}", LOG_PREFIX, cruiseId);

                // 巡航完成后更新状态
                CameraCruiseDO updateObj = new CameraCruiseDO();
                updateObj.setId(cruiseId);
                updateObj.setStatus(0);
                cruiseMapper.updateById(updateObj);
                cruiseTasks.remove(cruiseId);

            } catch (Exception e) {
                log.error("{} 巡航任务异常: cruiseId={}, error={}", LOG_PREFIX, cruiseId, e.getMessage(), e);
                // 异常时更新状态为停止
                try {
                    CameraCruiseDO updateObj = new CameraCruiseDO();
                    updateObj.setId(cruiseId);
                    updateObj.setStatus(0);
                    cruiseMapper.updateById(updateObj);
                } catch (Exception ex) {
                    log.error("{} 更新巡航状态失败: cruiseId={}", LOG_PREFIX, cruiseId);
                }
                cruiseTasks.remove(cruiseId);
            }
        }
    }

    /**
     * 校验巡航路线是否存在
     */
    private CameraCruiseDO validateCruiseExists(Long id) {
        CameraCruiseDO cruise = cruiseMapper.selectById(id);
        if (cruise == null) {
            throw exception(CAMERA_CRUISE_NOT_EXISTS);
        }
        return cruise;
    }

    /**
     * 保存巡航点列表
     */
    private void saveCruisePoints(Long cruiseId, List<?> points) {
        for (Object point : points) {
            CameraCruisePointDO cruisePoint = BeanUtils.toBean(point, CameraCruisePointDO.class);
            cruisePoint.setCruiseId(cruiseId);
            cruisePointMapper.insert(cruisePoint);
        }
    }

    // ==================== 设备巡航功能 ====================

    @Override
    public void syncCruiseToDevice(Long id, Integer tourNo) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 获取巡航点列表（至少需要2个预设点才能同步）
        List<CameraCruisePointDO> cruisePoints = cruisePointMapper.selectListByCruiseId(id);
        if (cruisePoints.size() < 2) {
            throw exception(CAMERA_CRUISE_POINTS_TOO_FEW);
        }

        // 获取通道信息
        IotDeviceChannelDO channel = channelService.getChannel(cruise.getChannelId());
        if (channel == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 获取预设点编号列表和停留时间
        List<Integer> presetNos = new ArrayList<>();
        List<Integer> dwellTimes = new ArrayList<>();
        for (CameraCruisePointDO point : cruisePoints) {
            CameraPresetDO preset = presetMapper.selectById(point.getPresetId());
            if (preset != null) {
                presetNos.add(preset.getPresetNo());
                // 优先使用巡航点的停留时间，否则使用巡航路线的默认停留时间
                int dwellTime = (point.getDwellTime() != null && point.getDwellTime() > 0) 
                        ? point.getDwellTime() 
                        : (cruise.getDwellTime() != null ? cruise.getDwellTime() : 15);
                dwellTimes.add(dwellTime);
            }
        }

        if (presetNos.isEmpty()) {
            throw exception(CAMERA_CRUISE_NO_POINTS);
        }

        // 确定巡航组编号
        int actualTourNo = tourNo != null ? tourNo : (int) ((id % 8) + 1);

        // 获取设备连接信息
        Long deviceId = channel.getDeviceId();
        String targetIp = channel.getTargetIp();
        String username = channel.getUsername() != null ? channel.getUsername() : "admin";
        String password = channel.getPassword() != null ? channel.getPassword() : "admin123";
        Integer targetChannelNo = channel.getTargetChannelNo() != null ? channel.getTargetChannelNo() : channel.getChannelNo();

        // 巡航组名称
        String tourName = cruise.getCruiseName();

        log.info("{} 同步巡航到设备: id={}, cruiseName={}, tourNo={}, presetCount={}, dwellTimes={}", 
                LOG_PREFIX, id, tourName, actualTourNo, presetNos.size(), dwellTimes);

        // 发送同步命令
        nvrCommandService.syncTourToDevice(
                deviceId,
                targetChannelNo,
                actualTourNo,
                tourName,
                presetNos,
                dwellTimes,
                targetIp,
                username,
                password
        );
    }

    /**
     * 静默同步巡航到设备（不抛异常，用于自动同步场景）
     *
     * @param id     巡航路线ID
     * @param tourNo 巡航组编号
     */
    private void syncCruiseToDeviceSilently(Long id, Integer tourNo) {
        try {
            // 获取巡航路线
            CameraCruiseDO cruise = cruiseMapper.selectById(id);
            if (cruise == null) {
                log.warn("{} 静默同步失败：巡航路线不存在 id={}", LOG_PREFIX, id);
                return;
            }

            // 获取巡航点列表
            List<CameraCruisePointDO> cruisePoints = cruisePointMapper.selectListByCruiseId(id);
            if (cruisePoints.isEmpty()) {
                log.warn("{} 静默同步失败：巡航点为空 id={}", LOG_PREFIX, id);
                return;
            }

            // 获取通道信息
            IotDeviceChannelDO channel = channelService.getChannel(cruise.getChannelId());
            if (channel == null) {
                log.warn("{} 静默同步失败：通道不存在 channelId={}", LOG_PREFIX, cruise.getChannelId());
                return;
            }

            // 获取预设点编号列表和停留时间
            List<Integer> presetNos = new ArrayList<>();
            List<Integer> dwellTimes = new ArrayList<>();
            for (CameraCruisePointDO point : cruisePoints) {
                CameraPresetDO preset = presetMapper.selectById(point.getPresetId());
                if (preset != null) {
                    presetNos.add(preset.getPresetNo());
                    // 优先使用巡航点的停留时间，否则使用巡航路线的默认停留时间
                    int dwellTime = (point.getDwellTime() != null && point.getDwellTime() > 0) 
                            ? point.getDwellTime() 
                            : (cruise.getDwellTime() != null ? cruise.getDwellTime() : 15);
                    dwellTimes.add(dwellTime);
                }
            }

            if (presetNos.isEmpty()) {
                log.warn("{} 静默同步失败：无有效预设点 id={}", LOG_PREFIX, id);
                return;
            }

            // 确定巡航组编号
            int actualTourNo = tourNo != null ? tourNo : (int) ((id % 8) + 1);

            // 获取设备连接信息
            Long deviceId = channel.getDeviceId();
            String targetIp = channel.getTargetIp();
            String username = channel.getUsername() != null ? channel.getUsername() : "admin";
            String password = channel.getPassword() != null ? channel.getPassword() : "admin123";
            Integer targetChannelNo = channel.getTargetChannelNo() != null ? channel.getTargetChannelNo() : channel.getChannelNo();

            // 巡航组名称
            String tourName = cruise.getCruiseName();

            log.info("{} 静默同步巡航到设备: id={}, cruiseName={}, tourNo={}, presetCount={}, dwellTimes={}", 
                    LOG_PREFIX, id, tourName, actualTourNo, presetNos.size(), dwellTimes);

            // 发送同步命令
            nvrCommandService.syncTourToDevice(
                    deviceId,
                    targetChannelNo,
                    actualTourNo,
                    tourName,
                    presetNos,
                    dwellTimes,
                    targetIp,
                    username,
                    password
            );

            log.info("{} ✅ 静默同步巡航到设备成功: id={}", LOG_PREFIX, id);

        } catch (Exception e) {
            log.warn("{} 静默同步巡航到设备异常: id={}, error={}", LOG_PREFIX, id, e.getMessage());
        }
    }

    @Override
    public void startDeviceCruise(Long id, Integer tourNo) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 获取通道信息
        IotDeviceChannelDO channel = channelService.getChannel(cruise.getChannelId());
        if (channel == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 确定巡航组编号
        int actualTourNo = tourNo != null ? tourNo : (int) ((id % 8) + 1);

        // 获取设备连接信息
        Long deviceId = channel.getDeviceId();
        String targetIp = channel.getTargetIp();
        String username = channel.getUsername() != null ? channel.getUsername() : "admin";
        String password = channel.getPassword() != null ? channel.getPassword() : "admin123";
        Integer targetChannelNo = channel.getTargetChannelNo() != null ? channel.getTargetChannelNo() : channel.getChannelNo();

        log.info("{} 启动设备巡航: id={}, cruiseName={}, tourNo={}", 
                LOG_PREFIX, id, cruise.getCruiseName(), actualTourNo);

        // 发送启动命令
        nvrCommandService.startDeviceTour(
                deviceId,
                targetChannelNo,
                actualTourNo,
                targetIp,
                username,
                password
        );

        // 更新状态为运行中
        CameraCruiseDO updateObj = new CameraCruiseDO();
        updateObj.setId(id);
        updateObj.setStatus(1);
        cruiseMapper.updateById(updateObj);
    }

    @Override
    public void stopDeviceCruise(Long id, Integer tourNo) {
        // 校验存在
        CameraCruiseDO cruise = validateCruiseExists(id);

        // 获取通道信息
        IotDeviceChannelDO channel = channelService.getChannel(cruise.getChannelId());
        if (channel == null) {
            throw exception(DEVICE_NOT_EXISTS);
        }

        // 确定巡航组编号
        int actualTourNo = tourNo != null ? tourNo : (int) ((id % 8) + 1);

        // 获取设备连接信息
        Long deviceId = channel.getDeviceId();
        String targetIp = channel.getTargetIp();
        String username = channel.getUsername() != null ? channel.getUsername() : "admin";
        String password = channel.getPassword() != null ? channel.getPassword() : "admin123";
        Integer targetChannelNo = channel.getTargetChannelNo() != null ? channel.getTargetChannelNo() : channel.getChannelNo();

        log.info("{} 停止设备巡航: id={}, cruiseName={}, tourNo={}", 
                LOG_PREFIX, id, cruise.getCruiseName(), actualTourNo);

        // 发送停止命令
        nvrCommandService.stopDeviceTour(
                deviceId,
                targetChannelNo,
                actualTourNo,
                targetIp,
                username,
                password
        );

        // 更新状态为停止
        CameraCruiseDO updateObj = new CameraCruiseDO();
        updateObj.setId(id);
        updateObj.setStatus(0);
        cruiseMapper.updateById(updateObj);
    }

    // ==================== 巡航点管理 ====================

    @Override
    public Long addCruisePoint(CameraCruisePointSaveReqVO reqVO) {
        // 校验巡航线路存在
        validateCruiseExists(reqVO.getCruiseId());
        
        // 校验预设点存在
        CameraPresetDO preset = presetMapper.selectById(reqVO.getPresetId());
        if (preset == null) {
            throw exception(CAMERA_PRESET_NOT_EXISTS);
        }
        
        // 插入巡航点
        CameraCruisePointDO point = BeanUtils.toBean(reqVO, CameraCruisePointDO.class);
        cruisePointMapper.insert(point);
        
        log.info("{} 添加巡航点成功: cruiseId={}, presetId={}, sortOrder={}", 
                LOG_PREFIX, reqVO.getCruiseId(), reqVO.getPresetId(), reqVO.getSortOrder());
        
        return point.getId();
    }

    @Override
    public void updateCruisePoint(CameraCruisePointSaveReqVO reqVO) {
        // 校验巡航点存在
        CameraCruisePointDO existPoint = cruisePointMapper.selectById(reqVO.getId());
        if (existPoint == null) {
            throw exception(CAMERA_CRUISE_POINT_NOT_EXISTS);
        }
        
        // 校验预设点存在
        if (reqVO.getPresetId() != null) {
            CameraPresetDO preset = presetMapper.selectById(reqVO.getPresetId());
            if (preset == null) {
                throw exception(CAMERA_PRESET_NOT_EXISTS);
            }
        }
        
        // 更新巡航点
        CameraCruisePointDO updateObj = BeanUtils.toBean(reqVO, CameraCruisePointDO.class);
        cruisePointMapper.updateById(updateObj);
        
        log.info("{} 更新巡航点成功: id={}", LOG_PREFIX, reqVO.getId());
    }

    @Override
    public void deleteCruisePoint(Long id) {
        // 校验巡航点存在
        CameraCruisePointDO existPoint = cruisePointMapper.selectById(id);
        if (existPoint == null) {
            throw exception(CAMERA_CRUISE_POINT_NOT_EXISTS);
        }
        
        // 删除巡航点
        cruisePointMapper.deleteById(id);
        
        log.info("{} 删除巡航点成功: id={}", LOG_PREFIX, id);
    }

    @Override
    public List<CameraCruisePointDO> getCruisePointListByCruiseId(Long cruiseId) {
        return cruisePointMapper.selectListByCruiseId(cruiseId);
    }

}
