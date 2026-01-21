package cn.iocoder.yudao.module.infra.dal.mysql.file;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.infra.controller.admin.file.vo.config.FileConfigPageReqVO;
import cn.iocoder.yudao.module.infra.dal.dataobject.file.FileConfigDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileConfigMapper extends BaseMapperX<FileConfigDO> {

    default PageResult<FileConfigDO> selectPage(FileConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FileConfigDO>()
                .likeIfPresent(FileConfigDO::getName, reqVO.getName())
                .eqIfPresent(FileConfigDO::getStorage, reqVO.getStorage())
                .betweenIfPresent(FileConfigDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(FileConfigDO::getId));
    }

    default FileConfigDO selectByMaster() {
        // 使用 selectList + 取第一条，避免多条主配置时报错
        // 正常情况下应该只有一条主配置，这里做容错处理
        // 按更新时间降序，选择最近更新的主配置
        // 注意：使用 apply 直接写 SQL，避免 bit(1) 类型的 Boolean 转换问题
        return selectOne(new LambdaQueryWrapperX<FileConfigDO>()
                .apply("master = 1")  // 直接使用数值比较，避免类型转换问题
                .orderByDesc(FileConfigDO::getUpdateTime)
                .last("LIMIT 1"));
    }

}
