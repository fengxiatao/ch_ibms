package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.upgrade.ChanghuiUpgradeTaskRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiUpgradeTaskDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 长辉升级任务 Convert
 *
 * @author 长辉信息科技有限公司
 */
@Mapper
public interface ChanghuiUpgradeTaskConvert {

    ChanghuiUpgradeTaskConvert INSTANCE = Mappers.getMapper(ChanghuiUpgradeTaskConvert.class);

    /**
     * DO转响应VO
     */
    ChanghuiUpgradeTaskRespVO convert(ChanghuiUpgradeTaskDO task);

    /**
     * DO列表转响应VO列表
     */
    List<ChanghuiUpgradeTaskRespVO> convertList(List<ChanghuiUpgradeTaskDO> list);

    /**
     * 分页结果转换
     */
    PageResult<ChanghuiUpgradeTaskRespVO> convertPage(PageResult<ChanghuiUpgradeTaskDO> page);

}
