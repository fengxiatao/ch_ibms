package cn.iocoder.yudao.module.iot.enums.changhui;

import java.util.HashMap;
import java.util.Map;

/**
 * 长辉设备指标常量
 * 
 * <p>定义设备采集的各类指标类型及其对应的AFN码
 * <p>基于全渠道量测水设施数据传输规约V2.3（IP9500_OPC协议）
 *
 * @author 长辉信息科技有限公司
 */
public final class ChanghuiIndicatorConstants {

    // ==================== 指标类型常量 ====================

    /**
     * 水位（单位：m）
     */
    public static final String WATER_LEVEL = "waterLevel";

    /**
     * 瞬时流量（单位：L/s）
     */
    public static final String INSTANT_FLOW = "instantFlow";

    /**
     * 瞬时流速（单位：m/s）
     */
    public static final String INSTANT_VELOCITY = "instantVelocity";

    /**
     * 累计流量（单位：m³）
     */
    public static final String CUMULATIVE_FLOW = "cumulativeFlow";

    /**
     * 闸位/闸门开度（单位：mm）
     */
    public static final String GATE_POSITION = "gatePosition";

    /**
     * 温度（单位：°C）
     */
    public static final String TEMPERATURE = "temperature";

    /**
     * 渗压（单位：kPa）
     */
    public static final String SEEPAGE_PRESSURE = "seepagePressure";

    /**
     * 荷载（单位：kN）
     */
    public static final String LOAD = "load";

    // ==================== AFN码常量 ====================

    /**
     * AFN码：水位数据
     */
    public static final byte AFN_WATER_LEVEL = 0x06;

    /**
     * AFN码：瞬时流量数据
     */
    public static final byte AFN_INSTANT_FLOW = 0x07;

    /**
     * AFN码：瞬时流速数据
     */
    public static final byte AFN_INSTANT_VELOCITY = 0x08;

    /**
     * AFN码：累计流量数据
     */
    public static final byte AFN_CUMULATIVE_FLOW = 0x0A;

    /**
     * AFN码：闸位数据
     */
    public static final byte AFN_GATE_POSITION = 0x0B;

    /**
     * AFN码：渗压数据
     */
    public static final byte AFN_SEEPAGE_PRESSURE = 0x0C;

    /**
     * AFN码：温度数据
     */
    public static final byte AFN_TEMPERATURE = 0x0D;

    /**
     * AFN码：荷载数据
     */
    public static final byte AFN_LOAD = 0x0E;

    /**
     * AFN码：心跳
     */
    public static final byte AFN_HEARTBEAT = 0x3C;

    /**
     * AFN码：升级触发
     */
    public static final byte AFN_UPGRADE_TRIGGER = 0x02;

    /**
     * AFN码：升级URL
     */
    public static final byte AFN_UPGRADE_URL = 0x10;

    /**
     * AFN码：升级开始确认
     */
    public static final byte AFN_UPGRADE_START = 0x15;

    /**
     * AFN码：升级进度
     */
    public static final byte AFN_UPGRADE_PROGRESS = 0x66;

    /**
     * AFN码：升级完成
     */
    public static final byte AFN_UPGRADE_COMPLETE = 0x67;

    /**
     * AFN码：升级失败
     */
    public static final byte AFN_UPGRADE_FAILED = 0x68;

    /**
     * AFN码：多指标查询
     */
    public static final byte AFN_MULTI_QUERY = (byte) 0x83;

    // ==================== 指标单位常量 ====================

    /**
     * 水位单位：米
     */
    public static final String UNIT_WATER_LEVEL = "m";

    /**
     * 流量单位：升/秒
     */
    public static final String UNIT_FLOW = "L/s";

    /**
     * 流速单位：米/秒
     */
    public static final String UNIT_VELOCITY = "m/s";

    /**
     * 累计流量单位：立方米
     */
    public static final String UNIT_CUMULATIVE_FLOW = "m³";

    /**
     * 闸位单位：毫米
     */
    public static final String UNIT_GATE_POSITION = "mm";

    /**
     * 温度单位：摄氏度
     */
    public static final String UNIT_TEMPERATURE = "°C";

    /**
     * 渗压单位：千帕
     */
    public static final String UNIT_SEEPAGE_PRESSURE = "kPa";

    /**
     * 荷载单位：千牛
     */
    public static final String UNIT_LOAD = "kN";

    // ==================== AFN到指标类型映射 ====================

    /**
     * AFN码到指标类型的映射
     */
    private static final Map<Byte, String> AFN_TO_INDICATOR_MAP = new HashMap<>();

    /**
     * 指标类型到AFN码的映射
     */
    private static final Map<String, Byte> INDICATOR_TO_AFN_MAP = new HashMap<>();

    /**
     * 指标类型到单位的映射
     */
    private static final Map<String, String> INDICATOR_TO_UNIT_MAP = new HashMap<>();

    static {
        // 初始化AFN到指标类型映射
        AFN_TO_INDICATOR_MAP.put(AFN_WATER_LEVEL, WATER_LEVEL);
        AFN_TO_INDICATOR_MAP.put(AFN_INSTANT_FLOW, INSTANT_FLOW);
        AFN_TO_INDICATOR_MAP.put(AFN_INSTANT_VELOCITY, INSTANT_VELOCITY);
        AFN_TO_INDICATOR_MAP.put(AFN_CUMULATIVE_FLOW, CUMULATIVE_FLOW);
        AFN_TO_INDICATOR_MAP.put(AFN_GATE_POSITION, GATE_POSITION);
        AFN_TO_INDICATOR_MAP.put(AFN_TEMPERATURE, TEMPERATURE);
        AFN_TO_INDICATOR_MAP.put(AFN_SEEPAGE_PRESSURE, SEEPAGE_PRESSURE);
        AFN_TO_INDICATOR_MAP.put(AFN_LOAD, LOAD);

        // 初始化指标类型到AFN码映射
        INDICATOR_TO_AFN_MAP.put(WATER_LEVEL, AFN_WATER_LEVEL);
        INDICATOR_TO_AFN_MAP.put(INSTANT_FLOW, AFN_INSTANT_FLOW);
        INDICATOR_TO_AFN_MAP.put(INSTANT_VELOCITY, AFN_INSTANT_VELOCITY);
        INDICATOR_TO_AFN_MAP.put(CUMULATIVE_FLOW, AFN_CUMULATIVE_FLOW);
        INDICATOR_TO_AFN_MAP.put(GATE_POSITION, AFN_GATE_POSITION);
        INDICATOR_TO_AFN_MAP.put(TEMPERATURE, AFN_TEMPERATURE);
        INDICATOR_TO_AFN_MAP.put(SEEPAGE_PRESSURE, AFN_SEEPAGE_PRESSURE);
        INDICATOR_TO_AFN_MAP.put(LOAD, AFN_LOAD);

        // 初始化指标类型到单位映射
        INDICATOR_TO_UNIT_MAP.put(WATER_LEVEL, UNIT_WATER_LEVEL);
        INDICATOR_TO_UNIT_MAP.put(INSTANT_FLOW, UNIT_FLOW);
        INDICATOR_TO_UNIT_MAP.put(INSTANT_VELOCITY, UNIT_VELOCITY);
        INDICATOR_TO_UNIT_MAP.put(CUMULATIVE_FLOW, UNIT_CUMULATIVE_FLOW);
        INDICATOR_TO_UNIT_MAP.put(GATE_POSITION, UNIT_GATE_POSITION);
        INDICATOR_TO_UNIT_MAP.put(TEMPERATURE, UNIT_TEMPERATURE);
        INDICATOR_TO_UNIT_MAP.put(SEEPAGE_PRESSURE, UNIT_SEEPAGE_PRESSURE);
        INDICATOR_TO_UNIT_MAP.put(LOAD, UNIT_LOAD);
    }

    /**
     * 根据AFN码获取指标类型
     *
     * @param afn AFN码
     * @return 指标类型，如果未找到返回null
     */
    public static String getIndicatorByAfn(byte afn) {
        return AFN_TO_INDICATOR_MAP.get(afn);
    }

    /**
     * 根据指标类型获取AFN码
     *
     * @param indicator 指标类型
     * @return AFN码，如果未找到返回null
     */
    public static Byte getAfnByIndicator(String indicator) {
        return INDICATOR_TO_AFN_MAP.get(indicator);
    }

    /**
     * 根据指标类型获取单位
     *
     * @param indicator 指标类型
     * @return 单位，如果未找到返回null
     */
    public static String getUnitByIndicator(String indicator) {
        return INDICATOR_TO_UNIT_MAP.get(indicator);
    }

    /**
     * 判断AFN码是否为数据类型
     *
     * @param afn AFN码
     * @return 是否为数据类型
     */
    public static boolean isDataAfn(byte afn) {
        return AFN_TO_INDICATOR_MAP.containsKey(afn);
    }

    /**
     * 判断AFN码是否为升级相关
     *
     * @param afn AFN码
     * @return 是否为升级相关
     */
    public static boolean isUpgradeAfn(byte afn) {
        return afn == AFN_UPGRADE_TRIGGER || afn == AFN_UPGRADE_URL 
            || afn == AFN_UPGRADE_START || afn == AFN_UPGRADE_PROGRESS 
            || afn == AFN_UPGRADE_COMPLETE || afn == AFN_UPGRADE_FAILED;
    }

    private ChanghuiIndicatorConstants() {
        // 私有构造函数，防止实例化
    }
}
