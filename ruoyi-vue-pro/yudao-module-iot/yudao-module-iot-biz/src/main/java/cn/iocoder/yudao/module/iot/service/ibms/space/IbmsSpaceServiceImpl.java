package cn.iocoder.yudao.module.iot.service.ibms.space;

import cn.hutool.core.collection.CollUtil;
import cn.iocoder.yudao.framework.common.exception.util.ServiceExceptionUtil;
import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.common.util.object.BeanUtils;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceSaveReqVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsSpaceTreeNodeRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsUnassignedItemRespVO;
import cn.iocoder.yudao.module.iot.controller.admin.ibms.space.vo.IbmsUnassignedPageReqVO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsChannelDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsDeviceDO;
import cn.iocoder.yudao.module.iot.dal.dataobject.ibms.IbmsSpaceDO;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsChannelMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsDeviceMapper;
import cn.iocoder.yudao.module.iot.dal.mysql.ibms.IbmsSpaceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.*;

/**
 * IBMS 空间管理 Service 实现
 */
@Service
@Validated
@RequiredArgsConstructor
public class IbmsSpaceServiceImpl implements IbmsSpaceService {

    private final IbmsSpaceMapper spaceMapper;
    private final IbmsDeviceMapper deviceMapper;
    private final IbmsChannelMapper channelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSpace(IbmsSpaceSaveReqVO reqVO) {
        IbmsSpaceDO space = BeanUtils.toBean(reqVO, IbmsSpaceDO.class);
        space.setId(null);
        spaceMapper.insert(space);
        return space.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSpace(IbmsSpaceSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw ServiceExceptionUtil.exception0(400, "空间 ID 不能为空");
        }
        IbmsSpaceDO exist = spaceMapper.selectById(reqVO.getId());
        if (exist == null) {
            throw ServiceExceptionUtil.exception0(404, "空间不存在");
        }
        IbmsSpaceDO update = BeanUtils.toBean(reqVO, IbmsSpaceDO.class);
        spaceMapper.updateById(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSpace(Long id) {
        IbmsSpaceDO exist = spaceMapper.selectById(id);
        if (exist == null) {
            return;
        }
        long childCount = spaceMapper.selectCount(new LambdaQueryWrapperX<IbmsSpaceDO>()
                .eq(IbmsSpaceDO::getParentId, id));
        if (childCount > 0) {
            throw ServiceExceptionUtil.exception0(400, "该空间存在子空间，请先删除子空间");
        }

        String spaceText = String.format("%s %s", exist.getSpaceCode(), exist.getName());
        long bindDeviceCount = deviceMapper.selectCount(new LambdaQueryWrapperX<IbmsDeviceDO>()
                .eq(IbmsDeviceDO::getSpace, spaceText));
        if (bindDeviceCount > 0) {
            throw ServiceExceptionUtil.exception0(400, "该空间已绑定设备，请先解除设备绑定");
        }

        long bindChannelCount = channelMapper.selectCount(new LambdaQueryWrapperX<IbmsChannelDO>()
                .eq(IbmsChannelDO::getSpaceId, id));
        if (bindChannelCount > 0) {
            throw ServiceExceptionUtil.exception0(400, "该空间已绑定通道，请先解除通道绑定");
        }

        spaceMapper.deleteById(id);
    }

    @Override
    public IbmsSpaceRespVO getSpace(Long id) {
        IbmsSpaceDO space = spaceMapper.selectById(id);
        if (space == null) {
            return null;
        }
        return BeanUtils.toBean(space, IbmsSpaceRespVO.class);
    }

    @Override
    public List<IbmsSpaceTreeNodeRespVO> getSpaceTree() {
        List<IbmsSpaceDO> list = spaceMapper.selectList(new LambdaQueryWrapperX<IbmsSpaceDO>()
                .orderByAsc(IbmsSpaceDO::getSort)
                .orderByAsc(IbmsSpaceDO::getId));
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }

        Map<Long, IbmsSpaceTreeNodeRespVO> nodeMap = new LinkedHashMap<>();
        list.forEach(item -> {
            IbmsSpaceTreeNodeRespVO node = new IbmsSpaceTreeNodeRespVO();
            node.setId(item.getId());
            node.setParentId(item.getParentId());
            node.setName(item.getName());
            node.setSpaceCode(item.getSpaceCode());
            node.setType(item.getType());
            node.setChildren(new ArrayList<>());
            nodeMap.put(node.getId(), node);
        });

        List<IbmsSpaceTreeNodeRespVO> roots = new ArrayList<>();
        nodeMap.values().forEach(node -> {
            Long parentId = node.getParentId() != null ? node.getParentId() : 0L;
            if (parentId == 0L) {
                roots.add(node);
                return;
            }
            IbmsSpaceTreeNodeRespVO parent = nodeMap.get(parentId);
            if (parent == null) {
                roots.add(node);
                return;
            }
            parent.getChildren().add(node);
        });
        return roots;
    }

    @Override
    public PageResult<IbmsUnassignedItemRespVO> getUnassignedPage(IbmsUnassignedPageReqVO reqVO) {
        List<IbmsUnassignedItemRespVO> resultList = new ArrayList<>();

        String type = reqVO.getType();
        boolean includeDevice = type == null || type.isEmpty() || "device".equalsIgnoreCase(type);
        boolean includePoint = type == null || type.isEmpty() || "point".equalsIgnoreCase(type);

        // 未分配的设备（空间为空）
        if (includeDevice) {
            List<IbmsDeviceDO> devices = deviceMapper.selectList(new LambdaQueryWrapperX<IbmsDeviceDO>()
                    .likeIfPresent(IbmsDeviceDO::getName, reqVO.getKeyword())
                    .likeIfPresent(IbmsDeviceDO::getDeviceCode, reqVO.getKeyword())
                    .eqIfPresent(IbmsDeviceDO::getGroupCode, reqVO.getGroupCode())
                    .and(w -> w.isNull(IbmsDeviceDO::getSpace).or().eq(IbmsDeviceDO::getSpace, "")));
            for (IbmsDeviceDO device : devices) {
                IbmsUnassignedItemRespVO item = new IbmsUnassignedItemRespVO();
                item.setId(device.getId());
                item.setType("device");
                item.setCode(device.getDeviceCode());
                item.setName(device.getName());
                item.setGroup(device.getGroupCode());
                item.setSystem(device.getSystemCode());
                resultList.add(item);
            }
        }

        // 未分配的通道（spaceId 为空）
        if (includePoint) {
            List<IbmsChannelDO> channels = channelMapper.selectList(new LambdaQueryWrapperX<IbmsChannelDO>()
                    .likeIfPresent(IbmsChannelDO::getName, reqVO.getKeyword())
                    .likeIfPresent(IbmsChannelDO::getCode, reqVO.getKeyword())
                    .isNull(IbmsChannelDO::getSpaceId));
            for (IbmsChannelDO channel : channels) {
                IbmsUnassignedItemRespVO item = new IbmsUnassignedItemRespVO();
                item.setId(channel.getId());
                item.setType("point");
                item.setCode(channel.getCode());
                item.setName(channel.getName());
                item.setGroup("-");
                item.setSystem(channel.getSystemType());
                resultList.add(item);
            }
        }

        // 按 ID 倒序，保证稳定性
        resultList.sort((a, b) -> Long.compare(
                b.getId() != null ? b.getId() : 0L,
                a.getId() != null ? a.getId() : 0L
        ));

        // 内存分页
        long total = resultList.size();
        int pageNo = (int) reqVO.getPageNo();
        int pageSize = (int) reqVO.getPageSize();
        int fromIndex = (pageNo - 1) * pageSize;
        if (fromIndex >= total) {
            return new PageResult<>(Collections.emptyList(), total);
        }
        int toIndex = Math.min(fromIndex + pageSize, (int) total);
        List<IbmsUnassignedItemRespVO> pageList = resultList.subList(fromIndex, toIndex);
        return new PageResult<>(pageList, total);
    }
}

