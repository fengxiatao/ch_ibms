package cn.iocoder.yudao.module.system.convert.auth;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeSendReqDTO;
import cn.iocoder.yudao.module.system.api.sms.dto.code.SmsCodeUseReqDTO;
import cn.iocoder.yudao.module.system.api.social.dto.SocialUserBindReqDTO;
import cn.iocoder.yudao.module.system.controller.admin.auth.vo.*;
import cn.iocoder.yudao.module.system.dal.dataobject.oauth2.OAuth2AccessTokenDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO;
import cn.iocoder.yudao.module.system.dal.dataobject.permission.RoleDO;
import cn.iocoder.yudao.module.system.dal.dataobject.user.AdminUserDO;
import cn.iocoder.yudao.module.system.enums.permission.MenuTypeEnum;

import java.util.*;

import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.convertSet;
import static cn.iocoder.yudao.framework.common.util.collection.CollectionUtils.filterList;
import static cn.iocoder.yudao.module.system.dal.dataobject.permission.MenuDO.ID_ROOT;

/**
 * 手写版的 AuthConvert 实现，避免运行时 MapStruct 依赖缺失导致的 NoClassDefFoundError。
 */
public class AuthConvertImpl implements AuthConvert {

    @Override
    public AuthLoginRespVO convert(OAuth2AccessTokenDO bean) {
        if (bean == null) {
            return null;
        }
        return AuthLoginRespVO.builder()
                .userId(bean.getUserId())
                .accessToken(bean.getAccessToken())
                .refreshToken(bean.getRefreshToken())
                .expiresTime(bean.getExpiresTime())
                .build();
    }

    @Override
    public AuthPermissionInfoRespVO.MenuVO convertTreeNode(MenuDO menu) {
        if (menu == null) {
            return null;
        }
        AuthPermissionInfoRespVO.MenuVO vo = new AuthPermissionInfoRespVO.MenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setComponentName(menu.getComponentName());
        vo.setIcon(menu.getIcon());
        vo.setVisible(menu.getVisible());
        vo.setKeepAlive(menu.getKeepAlive());
        vo.setAlwaysShow(menu.getAlwaysShow());
        return vo;
    }

    @Override
    public AuthPermissionInfoRespVO convert(AdminUserDO user, List<RoleDO> roleList, List<MenuDO> menuList) {
        return AuthConvert.super.convert(user, roleList, menuList);
    }

    @Override
    public List<AuthPermissionInfoRespVO.MenuVO> buildMenuTree(List<MenuDO> menuList) {
        if (CollUtil.isEmpty(menuList)) {
            return Collections.emptyList();
        }
        menuList.removeIf(menu -> menu.getType().equals(MenuTypeEnum.BUTTON.getType()));
        menuList.sort(Comparator.comparing(MenuDO::getSort));

        Map<Long, AuthPermissionInfoRespVO.MenuVO> treeNodeMap = new LinkedHashMap<>();
        menuList.forEach(menu -> treeNodeMap.put(menu.getId(), convertTreeNode(menu)));
        treeNodeMap.values().stream().filter(node -> !node.getParentId().equals(ID_ROOT)).forEach(childNode -> {
            AuthPermissionInfoRespVO.MenuVO parentNode = treeNodeMap.get(childNode.getParentId());
            if (parentNode == null) {
                return;
            }
            if (parentNode.getChildren() == null) {
                parentNode.setChildren(new ArrayList<>());
            }
            parentNode.getChildren().add(childNode);
        });
        return filterList(treeNodeMap.values(), node -> ID_ROOT.equals(node.getParentId()));
    }

    @Override
    public SocialUserBindReqDTO convert(Long userId, Integer userType, AuthSocialLoginReqVO reqVO) {
        if (reqVO == null) {
            return null;
        }
        SocialUserBindReqDTO dto = new SocialUserBindReqDTO();
        dto.setUserId(userId);
        dto.setUserType(userType);
        dto.setSocialType(reqVO.getType());
        dto.setCode(reqVO.getCode());
        dto.setState(reqVO.getState());
        return dto;
    }

    @Override
    public SmsCodeSendReqDTO convert(AuthSmsSendReqVO reqVO) {
        if (reqVO == null) {
            return null;
        }
        return BeanUtils.toBean(reqVO, SmsCodeSendReqDTO.class);
    }

    @Override
    public SmsCodeUseReqDTO convert(AuthSmsLoginReqVO reqVO, Integer scene, String usedIp) {
        if (reqVO == null) {
            return null;
        }
        SmsCodeUseReqDTO dto = new SmsCodeUseReqDTO();
        dto.setMobile(reqVO.getMobile());
        dto.setCode(reqVO.getCode());
        dto.setScene(scene);
        dto.setUsedIp(usedIp);
        return dto;
    }
}

