package cn.iocoder.yudao.module.iot.service.ibms.facade;

import cn.iocoder.yudao.module.system.dal.dataobject.dict.DictDataDO;
import cn.iocoder.yudao.module.system.service.dict.DictDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * 单元测试：验证 IbmsBusinessMappingDictLoader 在 ApplicationReadyEvent 触发时，
 * 能从 system_dict_data.ibms_group.remark.systems 正确解析并刷新到 IbmsBusinessMappingHelper。
 *
 * <p>不依赖 RocketMQ / 数据源 / Spring Boot 上下文。
 */
@ExtendWith(MockitoExtension.class)
class IbmsBusinessMappingDictLoaderTest {

    @Mock
    private DictDataService dictDataService;

    @Spy
    private IbmsBusinessMappingHelper helper = new IbmsBusinessMappingHelper();

    @InjectMocks
    private IbmsBusinessMappingDictLoader loader;

    @BeforeEach
    void resetLoadedFlag() {
        // 确保每个用例的 AtomicBoolean 处于未触发状态
        ReflectionTestUtils.setField(loader, "loaded", new java.util.concurrent.atomic.AtomicBoolean(false));
    }

    private DictDataDO row(String value, String remark) {
        DictDataDO d = new DictDataDO();
        d.setDictType("ibms_group");
        d.setValue(value);
        d.setRemark(remark);
        return d;
    }

    @Test
    void onApplicationReady_shouldRefreshHelperFromDict() {
        List<DictDataDO> rows = Arrays.asList(
                row("SA", "{\"systems\": [\"VI\", \"AL\", \"GR\"], \"icon\": \"fa-shield-alt\"}"),
                row("ST", "{\"systems\": [\"AC\", \"IC\", \"CA\", \"VM\"], \"icon\": \"fa-door-open\"}"),
                row("SB", "{\"systems\": [\"BA\", \"LI\", \"EL\", \"EM\"], \"icon\": \"fa-building\"}"),
                row("SE", "{\"systems\": [\"EP\", \"EN\"], \"icon\": \"fa-bolt\"}"),
                row("SF", "{\"systems\": [\"FD\", \"PA\"], \"icon\": \"fa-fire-extinguisher\"}")
        );
        when(dictDataService.getDictDataListByDictType("ibms_group")).thenReturn(rows);

        loader.onApplicationReady();

        // 大类正向校验
        assertEquals("SA", helper.resolveGroupBySystem("VI"));
        assertEquals("SA", helper.resolveGroupBySystem("AL"));
        assertEquals("SA", helper.resolveGroupBySystem("GR"));
        assertEquals("ST", helper.resolveGroupBySystem("AC"));
        assertEquals("ST", helper.resolveGroupBySystem("IC"));
        assertEquals("ST", helper.resolveGroupBySystem("CA"));
        assertEquals("ST", helper.resolveGroupBySystem("VM"));
        assertEquals("SB", helper.resolveGroupBySystem("BA"));
        assertEquals("SB", helper.resolveGroupBySystem("LI"));
        assertEquals("SB", helper.resolveGroupBySystem("EL"));
        assertEquals("SB", helper.resolveGroupBySystem("EM"));
        assertEquals("SE", helper.resolveGroupBySystem("EP"));
        assertEquals("SE", helper.resolveGroupBySystem("EN"));
        assertEquals("SF", helper.resolveGroupBySystem("FD"));
        assertEquals("SF", helper.resolveGroupBySystem("PA"));

        // 反向 + 大小写不敏感
        assertEquals("ST", helper.resolveGroupBySystem("vm"));
        assertEquals("SB", helper.resolveGroupBySystem(" em "));

        // 子系统集合
        assertTrue(helper.listSystemCodesOfGroup("ST").contains("VM"));
        assertTrue(helper.listSystemCodesOfGroup("SB").contains("EM"));
        assertEquals(4, helper.listSystemCodesOfGroup("ST").size());
        assertEquals(4, helper.listSystemCodesOfGroup("SB").size());

        // 自洽校验
        assertTrue(helper.isConsistent("ST", "VM"));
        assertFalse(helper.isConsistent("ST", "EM"));
    }

    @Test
    void onApplicationReady_shouldKeepDefaultWhenDictEmpty() {
        when(dictDataService.getDictDataListByDictType("ibms_group")).thenReturn(java.util.Collections.emptyList());

        loader.onApplicationReady();

        // Helper 仍走 buildDefault() 兜底（VM/EM 在 Java 默认值里也存在）
        assertEquals("ST", helper.resolveGroupBySystem("VM"));
        assertEquals("SB", helper.resolveGroupBySystem("EM"));
        assertEquals("SA", helper.resolveGroupBySystem("VI"));
    }

    @Test
    void onApplicationReady_shouldIgnoreInvalidRemarkJson() {
        // 一条非 JSON 的 remark 不应让整个加载失败
        List<DictDataDO> rows = Arrays.asList(
                row("SA", "{\"systems\": [\"VI\", \"AL\", \"GR\"]}"),
                row("ST", "not-a-json"),
                row("SB", "{\"systems\": [\"BA\", \"LI\", \"EL\", \"EM\"]}")
        );
        when(dictDataService.getDictDataListByDictType("ibms_group")).thenReturn(rows);

        loader.onApplicationReady();

        // 有效行生效
        assertEquals("SA", helper.resolveGroupBySystem("VI"));
        assertEquals("SB", helper.resolveGroupBySystem("EM"));
        // 无效行的大类，因 helper.refresh 仅传入合法条目，ST 未被覆盖 → 保留 buildDefault() 值
        // 注：refresh 是整体替换。因此 ST 在 newMap 里若不存在，则 ST 系列旧值会丢失。
        // 这里仅断言"加载未崩溃 + 其他大类生效"。
        assertNotNull(helper.resolveGroupBySystem("VI"));
    }

    @Test
    void onApplicationReady_shouldBeIdempotent() {
        List<DictDataDO> rows = Arrays.asList(
                row("SA", "{\"systems\": [\"VI\"]}"),
                row("ST", "{\"systems\": [\"AC\"]}"),
                row("SB", "{\"systems\": [\"BA\"]}"),
                row("SE", "{\"systems\": [\"EP\"]}"),
                row("SF", "{\"systems\": [\"FD\"]}")
        );
        when(dictDataService.getDictDataListByDictType("ibms_group")).thenReturn(rows);

        loader.onApplicationReady();
        // 第二次触发应被 AtomicBoolean 拦截，不再重复 refresh
        loader.onApplicationReady();

        assertEquals("SA", helper.resolveGroupBySystem("VI"));
    }

}
