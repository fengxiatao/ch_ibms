package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AccessControllerDetailVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AccessControllerTreeVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.AuthRecordPageReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.PersonDeviceAuthVO;

import java.util.List;

/**
 * 门禁管理 Service 接口
 * 
 * 提供门禁控制器+门通道的组合查询功能，用于门禁管理页面
 *
 * @author 芋道源码
 */
public interface AccessManagementService {

    /**
     * 获取门禁控制器树形结构（包含门通道）
     * 
     * 查询所有门禁控制器及其下属门通道，组装成树形结构
     * 用于门禁管理页面左侧的控制器列表展示
     *
     * @return 门禁控制器树形结构列表
     */
    List<AccessControllerTreeVO> getControllerTree();

    /**
     * 获取单个控制器详情（包含门通道列表）
     * 
     * 查询指定控制器的完整信息，包括：
     * - 设备基本信息
     * - 连接配置
     * - 设备能力集
     * - 门通道列表及状态
     *
     * @param deviceId 设备ID
     * @return 控制器详情
     */
    AccessControllerDetailVO getControllerDetail(Long deviceId);

    /**
     * 获取在线控制器树形结构
     * 
     * 只返回在线状态的门禁控制器及其门通道
     *
     * @return 在线门禁控制器树形结构列表
     */
    List<AccessControllerTreeVO> getOnlineControllerTree();

    /**
     * 刷新控制器状态
     * 
     * 从Gateway获取最新的设备状态并更新
     *
     * @param deviceId 设备ID
     */
    void refreshControllerStatus(Long deviceId);

    /**
     * 执行门控操作
     * 
     * 实现逻辑（Requirements 4.1, 4.4）：
     * 1. 检查设备在线状态
     * 2. 使用连接池句柄执行操作
     * 3. 无句柄时尝试即时登录
     *
     * @param reqVO 门控操作请求
     * @return 门控操作响应
     */
    cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.DoorControlRespVO doorControl(
            cn.iocoder.yudao.module.iot.controller.admin.access.vo.management.DoorControlReqVO reqVO);

    /**
     * 获取授权记录分页
     * 
     * 查询 iot_access_person_device_auth 表，关联查询人员、设备、通道名称
     * Requirements: 7.1, 7.2
     *
     * @param reqVO 分页查询请求
     * @return 授权记录分页结果
     */
    PageResult<PersonDeviceAuthVO> getAuthRecordPage(AuthRecordPageReqVO reqVO);

    /**
     * 重试失败的授权
     * 
     * 查询失败的授权记录，重新执行授权下发
     * Requirements: 7.3
     *
     * @param authId 授权记录ID
     */
    void retryFailedAuth(Long authId);

}
