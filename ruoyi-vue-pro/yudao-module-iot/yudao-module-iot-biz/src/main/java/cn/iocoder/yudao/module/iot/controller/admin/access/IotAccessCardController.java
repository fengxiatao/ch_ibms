package cn.iocoder.yudao.module.iot.controller.admin.access;

import cn.iocoder.yudao.framework.common.pojo.CommonResult;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.card.IotAccessCardAddReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.access.vo.card.IotAccessCardRespVO;
import cn.iocoder.yudao.module.iot.service.access.IotAccessCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static cn.iocoder.yudao.framework.common.pojo.CommonResult.success;

/**
 * 门禁卡信息管理 Controller
 *
 * @author 芋道源码
 */
@Tag(name = "管理后台 - 门禁卡信息管理")
@RestController
@RequestMapping("/iot/access/card")
@Validated
public class IotAccessCardController {

    @Resource
    private IotAccessCardService cardService;

    @PostMapping("/add")
    @Operation(summary = "添加卡信息到设备")
    @PreAuthorize("@ss.hasPermission('iot:access-card:add')")
    public CommonResult<Boolean> addCard(@Valid @RequestBody IotAccessCardAddReqVO reqVO) {
        return success(cardService.addCard(reqVO));
    }

    @PutMapping("/update")
    @Operation(summary = "更新卡信息")
    @PreAuthorize("@ss.hasPermission('iot:access-card:update')")
    public CommonResult<Boolean> updateCard(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("cardNo") String cardNo,
            @Valid @RequestBody IotAccessCardAddReqVO reqVO) {
        return success(cardService.updateCard(deviceId, cardNo, reqVO));
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除卡信息")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @Parameter(name = "cardNo", description = "卡号", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-card:delete')")
    public CommonResult<Boolean> deleteCard(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("cardNo") String cardNo) {
        return success(cardService.deleteCard(deviceId, cardNo));
    }

    @GetMapping("/list")
    @Operation(summary = "查询设备中的所有卡")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-card:query')")
    public CommonResult<List<IotAccessCardRespVO>> listCards(@RequestParam("deviceId") Long deviceId) {
        return success(cardService.listCards(deviceId));
    }

    @PostMapping("/clear")
    @Operation(summary = "清空设备中的所有卡")
    @Parameter(name = "deviceId", description = "设备ID", required = true)
    @PreAuthorize("@ss.hasPermission('iot:access-card:clear')")
    public CommonResult<Boolean> clearAllCards(@RequestParam("deviceId") Long deviceId) {
        return success(cardService.clearAllCards(deviceId));
    }

}
