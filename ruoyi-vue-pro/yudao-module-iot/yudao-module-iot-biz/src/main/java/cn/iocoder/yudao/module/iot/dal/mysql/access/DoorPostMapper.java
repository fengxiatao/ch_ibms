package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorpost.DoorPostPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorPostDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoorPostMapper extends BaseMapperX<DoorPostDO> {

    default PageResult<DoorPostDO> selectPage(DoorPostPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DoorPostDO>()
                .likeIfPresent(DoorPostDO::getPostName, reqVO.getPostName())
                .orderByDesc(DoorPostDO::getId));
    }

}


























