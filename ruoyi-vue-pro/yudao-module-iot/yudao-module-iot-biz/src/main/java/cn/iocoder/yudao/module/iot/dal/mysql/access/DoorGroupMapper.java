package cn.iocoder.yudao.module.iot.dal.mysql.access;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.doorgroup.DoorGroupPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.access.DoorGroupDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoorGroupMapper extends BaseMapperX<DoorGroupDO> {

    default PageResult<DoorGroupDO> selectPage(DoorGroupPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DoorGroupDO>()
                .likeIfPresent(DoorGroupDO::getGroupName, reqVO.getGroupName())
                .orderByDesc(DoorGroupDO::getId));
    }

}


























