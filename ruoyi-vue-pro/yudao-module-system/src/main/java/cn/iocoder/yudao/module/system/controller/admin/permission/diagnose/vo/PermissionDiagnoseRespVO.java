package cn.iocoder.yudao.module.system.controller.admin.permission.diagnose.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 权限「拒绝访问」诊断响应 VO
 *
 * @author 长辉信息科技有限公司
 */
@Schema(description = "管理后台 - 权限拒绝访问诊断 Response VO")
@Data
public class PermissionDiagnoseRespVO {

    @Schema(description = "HTTP 方法", example = "GET")
    private String httpMethod;

    @Schema(description = "请求 URL", example = "/admin-api/iot/parking/lane/list-by-lot")
    private String url;

    @Schema(description = "处理该请求的 Controller 全限定名", example = "cn...ParkingLaneController")
    private String handlerClass;

    @Schema(description = "处理该请求的方法名", example = "getParkingLaneListByLot")
    private String handlerMethod;

    @Schema(description = "缺失的权限标识", example = "iot:parking:lane:query-btn")
    private String requiredPermission;

    @Schema(description = "permission 对应的所有菜单（一般是 type=3 按钮）")
    private List<MenuSimpleVO> permissionMenus = new ArrayList<>();

    @Schema(description = "祖先目录链；从根目录到按钮，依次列出。可用于修复指引。")
    private List<MenuSimpleVO> ancestorChain = new ArrayList<>();

    @Schema(description = "套餐 ID，传入则计算该套餐是否包含上述菜单", example = "113")
    private Long tenantPackageId;

    @Schema(description = "套餐缺失的菜单 ID 列表（按钮 + 祖先链中未在套餐里的菜单）")
    private List<Long> missingInPackage = new ArrayList<>();

    @Schema(description = "建议运营操作的菜单 ID（一般是按钮的直接父级 type=2 菜单页）", example = "6344")
    private Long recommendCheckMenuId;

    @Schema(description = "可读修复建议", example = "套餐请勾选「6344 配置车道」")
    private String suggestion;

    @Schema(description = "诊断/修复过程产生的提示信息")
    private List<String> messages = new ArrayList<>();

    @Schema(description = "管理后台 - 菜单精简信息")
    @Data
    public static class MenuSimpleVO {

        @Schema(description = "菜单编号", example = "72551")
        private Long id;

        @Schema(description = "菜单名称", example = "车道查询")
        private String name;

        @Schema(description = "类型：1-目录；2-菜单；3-按钮", example = "3")
        private Integer type;

        @Schema(description = "权限标识", example = "iot:parking:lane:query-btn")
        private String permission;

        @Schema(description = "父菜单编号", example = "6344")
        private Long parentId;

    }

}
