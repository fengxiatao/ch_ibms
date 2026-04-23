package cn.iocoder.yudao.module.iot.dal.mysql.ibms;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.channel.vo.IbmsChannelPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IBMS 通道 Mapper
 */
@Mapper
public interface IbmsChannelMapper extends BaseMapperX<IbmsChannelDO> {

    default PageResult<IbmsChannelDO> selectPage(IbmsChannelPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IbmsChannelDO>()
                .likeIfPresent(IbmsChannelDO::getCode, reqVO.getKeyword())
                .likeIfPresent(IbmsChannelDO::getName, reqVO.getKeyword())
                .eqIfPresent(IbmsChannelDO::getBusiness, reqVO.getBusiness())
                .eqIfPresent(IbmsChannelDO::getSpaceId, reqVO.getSpaceId())
                .eqIfPresent(IbmsChannelDO::getDeviceId, reqVO.getDeviceId())
                .eqIfPresent(IbmsChannelDO::getTypeCode, reqVO.getTypeCode())
                .eqIfPresent(IbmsChannelDO::getSystemType, reqVO.getSystemType())
                .eqIfPresent(IbmsChannelDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IbmsChannelDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IbmsChannelDO::getId));
    }

    default List<IbmsChannelDO> selectListByDeviceId(Long deviceId) {
        return selectList(new LambdaQueryWrapperX<IbmsChannelDO>()
                .eq(IbmsChannelDO::getDeviceId, deviceId)
                .orderByAsc(IbmsChannelDO::getId));
    }

    /**
     * 视频类通道候选集：系统 VI、类型码 VT*、或历史 VIDEO 类型码（与 {@code iot_device_channel.channel_type=VIDEO} 语义对齐）。
     */
    default List<IbmsChannelDO> selectListVideoOrientedChannels() {
        return selectList(new LambdaQueryWrapperX<IbmsChannelDO>()
                .and(w -> w.eq(IbmsChannelDO::getSystemType, "VI")
                        .or()
                        .likeRight(IbmsChannelDO::getTypeCode, "VT")
                        .or()
                        .eq(IbmsChannelDO::getTypeCode, "VIDEO"))
                .orderByAsc(IbmsChannelDO::getId));
    }
}

