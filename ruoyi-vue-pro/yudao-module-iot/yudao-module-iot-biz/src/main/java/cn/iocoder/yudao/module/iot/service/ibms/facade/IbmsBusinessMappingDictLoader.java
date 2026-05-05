package cn.iocoder.yudao.module.iot.service.ibms.facade;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 启动期从 {@code system_dict_data.ibms_group.remark.systems} 加载 IBMS 业务大类 ↔ 子系统码映射，
 * 通过 {@link IbmsBusinessMappingHelper#refresh(Map)} 替换内存兜底值。
 * <p>
 * 加载时机：{@link ApplicationReadyEvent}（确保 DataSource/MP/TX 全部就绪）。
 * 失败策略：仅打印 WARN/ERROR，Helper 自动保留 {@code buildDefault()} 兜底；不阻塞启动。
 * 幂等性：用 AtomicBoolean 防多 context 场景重复触发。
 *
 * @author 长辉信息科技有限公司
 */
@Slf4j
@Component
public class IbmsBusinessMappingDictLoader {

    private static final String DICT_TYPE = "ibms_group";
    private static final String REMARK_KEY_SYSTEMS = "systems";

    private final AtomicBoolean loaded = new AtomicBoolean(false);

    @Resource
    private IbmsBusinessMappingHelper helper;

    @Resource
    private DictDataService dictDataService;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (!loaded.compareAndSet(false, true)) {
            return;
        }
        try {
            Map<String, Set<String>> map = loadFromDict();
            if (map.isEmpty()) {
                log.warn("[IbmsBusinessMappingDictLoader] 字典 {} 为空或解析无有效映射，保留 buildDefault() 兜底", DICT_TYPE);
                return;
            }
            helper.refresh(map);
            log.info("[IbmsBusinessMappingDictLoader] 已从字典 {} 加载 IBMS 业务大类映射：{}", DICT_TYPE, summarize(map));
        } catch (Exception e) {
            log.error("[IbmsBusinessMappingDictLoader] 加载字典 {} 失败，保留 buildDefault() 兜底", DICT_TYPE, e);
        }
    }

    private Map<String, Set<String>> loadFromDict() {
        List<DictDataDO> rows = dictDataService.getDictDataListByDictType(DICT_TYPE);
        Map<String, Set<String>> map = new LinkedHashMap<>();
        if (CollUtil.isEmpty(rows)) {
            return map;
        }
        for (DictDataDO row : rows) {
            String group = row.getValue();
            String remark = row.getRemark();
            if (StrUtil.isBlank(group) || StrUtil.isBlank(remark)) {
                continue;
            }
            Set<String> systems = parseSystems(group, remark);
            if (!systems.isEmpty()) {
                map.merge(group.trim().toUpperCase(), systems, (a, b) -> {
                    a.addAll(b);
                    return a;
                });
            }
        }
        return map;
    }

    private Set<String> parseSystems(String group, String remark) {
        Set<String> out = new LinkedHashSet<>();
        try {
            JSONObject obj = JSONUtil.parseObj(remark);
            JSONArray arr = obj.getJSONArray(REMARK_KEY_SYSTEMS);
            if (arr == null) {
                return out;
            }
            for (Object item : arr) {
                String sys = StrUtil.toStringOrNull(item);
                if (StrUtil.isNotBlank(sys)) {
                    out.add(sys.trim().toUpperCase());
                }
            }
        } catch (Exception e) {
            log.warn("[IbmsBusinessMappingDictLoader] 字典 {} 的 value={} remark 非 JSON 或无 systems 数组：remark={}",
                    DICT_TYPE, group, remark);
        }
        return out;
    }

    private static String summarize(Map<String, Set<String>> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Set<String>> e : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append('=').append(e.getValue());
        }
        return sb.toString();
    }

}
