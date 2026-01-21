package cn.iocoder.yudao.module.iot.service.access;

import cn.iocoder.yudao.module.iot.controller.admin.access.vo.card.IotAccessCardAddReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.card.IotAccessCardRespVO;

import java.util.List;

/**
 * 门禁卡信息管理 Service 接口
 *
 * @author 芋道源码
 */
public interface IotAccessCardService {

    /**
     * 添加卡信息到设备
     *
     * @param reqVO 卡信息
     * @return 是否成功
     */
    Boolean addCard(IotAccessCardAddReqVO reqVO);

    /**
     * 更新卡信息
     *
     * @param deviceId 设备ID
     * @param cardNo   卡号
     * @param reqVO    卡信息
     * @return 是否成功
     */
    Boolean updateCard(Long deviceId, String cardNo, IotAccessCardAddReqVO reqVO);

    /**
     * 删除卡信息
     *
     * @param deviceId 设备ID
     * @param cardNo   卡号
     * @return 是否成功
     */
    Boolean deleteCard(Long deviceId, String cardNo);

    /**
     * 查询设备中的所有卡
     *
     * @param deviceId 设备ID
     * @return 卡信息列表
     */
    List<IotAccessCardRespVO> listCards(Long deviceId);

    /**
     * 清空设备中的所有卡
     *
     * @param deviceId 设备ID
     * @return 是否成功
     */
    Boolean clearAllCards(Long deviceId);

}
