package cn.iocoder.yudao.module.iot.convert.changhui;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.module.iot.controller.admin.changhui.vo.control.ChanghuiControlLogRespVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.changhui.ChanghuiControlLogDO;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChanghuiControlLogConvertImpl implements ChanghuiControlLogConvert {

    @Override
    public ChanghuiControlLogRespVO convert(ChanghuiControlLogDO log) {
        if (log == null) {
            return null;
        }
        ChanghuiControlLogRespVO vo = new ChanghuiControlLogRespVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }

    @Override
    public List<ChanghuiControlLogRespVO> convertList(List<ChanghuiControlLogDO> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        return list.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ChanghuiControlLogRespVO> convertPage(PageResult<ChanghuiControlLogDO> page) {
        if (page == null) {
            return null;
        }
        List<ChanghuiControlLogRespVO> list = convertList(page.getList());
        return new PageResult<>(list, page.getTotal());
    }
}

