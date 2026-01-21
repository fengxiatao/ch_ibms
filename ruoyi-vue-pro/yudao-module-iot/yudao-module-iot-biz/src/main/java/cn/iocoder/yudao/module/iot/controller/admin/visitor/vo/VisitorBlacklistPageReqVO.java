package cn.iocoder.yudao.module.iot.controller.admin.visitor.vo;

import cn.iocoder.yudao.framework.common.pojo.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class VisitorBlacklistPageReqVO extends PageParam {
    private String visitorName;
    private Integer status;
}


























