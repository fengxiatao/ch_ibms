package cn.iocoder.yudao.module.iot.service.ibms.facade;

import cn.hutool.core.util.StrUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * IBMS 五大业务大类 ↔ 子系统码 映射 Helper。
 * <p>
 * 权威来源：{@code system_dict_data.ibms_group.remark.systems}（由系统管理员维护的字典）。
 * <br>本类为只读内存缓存，若字典运行期有变动，调用 {@link #refresh(Map)} 主动替换；
 * 本期（M0）先以下列硬编码做兜底，与字典保持一致。
 * </p>
 *
 * <ul>
 *   <li>SA 智慧安防 ← VI(视频监控) / AL(入侵报警) / GR(巡更系统)</li>
 *   <li>ST 智慧通行 ← AC(门禁) / IC(对讲) / CA(停车场) / VM(访客)</li>
 *   <li>SB 智慧建筑 ← BA(楼宇自控) / LI(照明) / EL(电梯) / EM(环境监测)</li>
 *   <li>SE 智慧能源 ← EP(变配电) / EN(能源管理)</li>
 *   <li>SF 智慧消防 ← FD(火灾报警) / PA(公共广播)</li>
 * </ul>
 */
@Component
public class IbmsBusinessMappingHelper {

    /** 大类码 → 子系统码集合（不可变） */
    private volatile Map<String, Set<String>> groupToSystems = buildDefault();

    /** 子系统码 → 大类码（反向查表；volatile 保证内存可见性） */
    private volatile Map<String, String> systemToGroup = invertMap(groupToSystems);

    private static Map<String, Set<String>> buildDefault() {
        Map<String, Set<String>> m = new LinkedHashMap<>();
        m.put("SA", setOf("VI", "AL", "GR"));
        m.put("ST", setOf("AC", "IC", "CA", "VM"));
        m.put("SB", setOf("BA", "LI", "EL", "EM"));
        m.put("SE", setOf("EP", "EN"));
        m.put("SF", setOf("FD", "PA"));
        return Collections.unmodifiableMap(m);
    }

    private static Set<String> setOf(String... items) {
        Set<String> s = new LinkedHashSet<>();
        Collections.addAll(s, items);
        return Collections.unmodifiableSet(s);
    }

    private static Map<String, String> invertMap(Map<String, Set<String>> groupToSystems) {
        Map<String, String> inv = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : groupToSystems.entrySet()) {
            for (String sys : e.getValue()) {
                inv.put(sys, e.getKey());
            }
        }
        return Collections.unmodifiableMap(inv);
    }

    /**
     * 由 system_code 解析业务大类（group_code）。
     *
     * @param systemCode 子系统码（大小写不敏感）
     * @return 对应的 group_code；若未匹配返回 {@code null}
     */
    public String resolveGroupBySystem(String systemCode) {
        if (StrUtil.isBlank(systemCode)) {
            return null;
        }
        return systemToGroup.get(systemCode.trim().toUpperCase());
    }

    /**
     * 获取某个业务大类下所有子系统码。
     *
     * @param groupCode 大类码（大小写不敏感）
     * @return 子系统码集合；若未匹配返回空集合
     */
    public Set<String> listSystemCodesOfGroup(String groupCode) {
        if (StrUtil.isBlank(groupCode)) {
            return Collections.emptySet();
        }
        Set<String> s = groupToSystems.get(groupCode.trim().toUpperCase());
        return s != null ? s : Collections.emptySet();
    }

    /**
     * 判断 system_code 与 group_code 是否自洽（一致）。
     *
     * @param groupCode 业务大类码
     * @param systemCode 子系统码
     * @return 一致返回 true；二者任一为空视为不约束，返回 true
     */
    public boolean isConsistent(String groupCode, String systemCode) {
        if (StrUtil.isBlank(groupCode) || StrUtil.isBlank(systemCode)) {
            return true;
        }
        Set<String> systems = listSystemCodesOfGroup(groupCode);
        return systems.contains(systemCode.trim().toUpperCase());
    }

    /**
     * 运行期替换映射表（来自最新字典快照）。线程安全。
     */
    public synchronized void refresh(Map<String, Set<String>> newMap) {
        if (newMap == null || newMap.isEmpty()) {
            return;
        }
        Map<String, Set<String>> copy = new LinkedHashMap<>();
        for (Map.Entry<String, Set<String>> e : newMap.entrySet()) {
            if (StrUtil.isBlank(e.getKey()) || e.getValue() == null) {
                continue;
            }
            Set<String> normalized = new LinkedHashSet<>();
            for (String v : e.getValue()) {
                if (StrUtil.isNotBlank(v)) {
                    normalized.add(v.trim().toUpperCase());
                }
            }
            copy.put(e.getKey().trim().toUpperCase(), Collections.unmodifiableSet(normalized));
        }
        this.groupToSystems = Collections.unmodifiableMap(copy);
        this.systemToGroup = invertMap(this.groupToSystems);
    }

}
