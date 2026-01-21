package cn.iocoder.yudao.module.iot.core.alarm.parser;

import cn.iocoder.yudao.module.iot.core.alarm.dto.AlarmHostStatusDTO;
import lombok.extern.slf4j.Slf4j;

/**
 * 报警主机状态解析器（Core模块版本）
 * 
 * 根据协议文档解析主机状态响应
 * 
 * 协议格式示例：
 * 查询命令：C1234,10,0,9876,131
 * 响应格式：c1234,0,131ÉS0aaaaaaAB
 * 
 * 解析规则：
 * - 'S' 后第一个字符：系统状态（0=撤防，1=布防）
 * - 后续小写字母（a-z）：对应防区撤防
 * - 后续大写字母（A-Z）：对应防区布防
 * - 大写字母后跟数字：表示该防区正在报警
 * 
 * @author 芋道源码
 */
@Slf4j
public class AlarmHostStatusParser {

    /**
     * 解析主机状态响应
     * 
     * @param account 主机账号
     * @param response 响应数据（格式：c1234,0,131ÉS0aaaaaaAB）
     * @return 解析后的状态信息
     */
    public static AlarmHostStatusDTO parse(String account, String response) {
        AlarmHostStatusDTO status = new AlarmHostStatusDTO();
        status.setAccount(account);
        status.setRawData(response);

        try {
            // 0. 检查响应格式：c{account},{errorCode},{sequence}[状态数据]
            String[] parts = response.split(",", 3);
            if (parts.length < 2) {
                log.warn("[parse][响应格式错误] account={}, response={}", account, response);
                return status;
            }
            
            // 检查错误码（第二部分）
            String errorCode = parts[1];
            if (!"0".equals(errorCode)) {
                log.warn("[parse][主机返回错误] account={}, errorCode={}, response={}", account, errorCode, response);
                status.setErrorCode(Integer.parseInt(errorCode));
                status.setErrorMessage("主机返回错误码: " + errorCode + "，可能是密码错误或参数不正确");
                return status;
            }
            
            // 1. 查找 'S' 标记位置
            int sIndex = response.indexOf('S');
            if (sIndex == -1 || sIndex + 1 >= response.length()) {
                log.warn("[parse][未找到状态标记S] account={}, response={}", account, response);
                return status;
            }

            // 2. 解析系统状态和分区状态（'S' 后的字符）
            String statusStr = response.substring(sIndex + 1);
            int firstLetterIndex = findFirstLetterIndex(statusStr);
            
            if (firstLetterIndex > 0) {
                // 有分区状态（数字）
                String partitionStatusStr = statusStr.substring(0, firstLetterIndex);
                log.debug("[parse][解析分区状态] partitionStatusStr={}", partitionStatusStr);
                parsePartitionStatus(status, partitionStatusStr);
                
                // 系统状态取第一个分区的状态
                if (!status.getPartitions().isEmpty()) {
                    status.setSystemStatus(status.getPartitions().get(0).getStatus());
                    log.debug("[parse][设置系统状态] systemStatus={}, 来自第一个分区", status.getSystemStatus());
                } else {
                    log.warn("[parse][分区列表为空] 无法设置系统状态");
                }
                
                // 防区状态
                String zoneStatusStr = statusStr.substring(firstLetterIndex);
                parseZoneStatus(status, zoneStatusStr);
            } else {
                // 只有防区状态，没有分区
                log.debug("[parse][无分区状态] 设置系统状态为0");
                status.setSystemStatus(0);
                parseZoneStatus(status, statusStr);
            }

            log.info("[parse][状态解析成功] account={}, systemStatus={}, zoneCount={}", 
                    account, status.getSystemStatus(), status.getZones().size());

        } catch (Exception e) {
            log.error("[parse][状态解析异常] account={}, response={}", account, response, e);
        }

        return status;
    }

    /**
     * 查找字符串中第一个字母的索引
     */
    private static int findFirstLetterIndex(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 解析分区状态字符串
     */
    private static void parsePartitionStatus(AlarmHostStatusDTO status, String partitionStatusStr) {
        if (partitionStatusStr == null || partitionStatusStr.isEmpty()) {
            log.debug("[parsePartitionStatus][分区状态字符串为空]");
            return;
        }

        log.debug("[parsePartitionStatus][开始解析] partitionStatusStr={}, length={}", partitionStatusStr, partitionStatusStr.length());
        
        for (int i = 0; i < partitionStatusStr.length(); i++) {
            char ch = partitionStatusStr.charAt(i);
            if (Character.isDigit(ch)) {
                AlarmHostStatusDTO.PartitionStatus partition = new AlarmHostStatusDTO.PartitionStatus();
                partition.setPartitionNo(i + 1);
                partition.setStatus(Character.getNumericValue(ch));
                status.getPartitions().add(partition);
                log.debug("[parsePartitionStatus][解析分区] partitionNo={}, status={}", i + 1, Character.getNumericValue(ch));
            }
        }
        
        log.debug("[parsePartitionStatus][解析完成] 分区数={}", status.getPartitions().size());
    }

    /**
     * 解析防区状态字符串
     */
    private static void parseZoneStatus(AlarmHostStatusDTO status, String zoneStatusStr) {
        if (zoneStatusStr == null || zoneStatusStr.isEmpty()) {
            return;
        }

        int zoneNo = 1;
        int i = 0;

        while (i < zoneStatusStr.length()) {
            char ch = zoneStatusStr.charAt(i);

            if (!Character.isLetter(ch)) {
                i++;
                continue;
            }

            AlarmHostStatusDTO.ZoneStatus zone = new AlarmHostStatusDTO.ZoneStatus();
            zone.setZoneNo(zoneNo);

            if (Character.isLowerCase(ch)) {
                // 小写字母：防区撤防或旁路
                if (ch == 'a') {
                    zone.setStatus(0);
                    zone.setAlarmStatus(0);
                } else if (ch == 'b') {
                    zone.setStatus(2); // 旁路
                    zone.setAlarmStatus(0);
                } else {
                    zone.setStatus(0);
                    zone.setAlarmStatus(0);
                }
                i++;
            } else {
                // 大写字母：防区布防
                zone.setStatus(1);
                
                switch (ch) {
                    case 'A':
                        zone.setAlarmStatus(0);
                        break;
                    case 'B':
                        zone.setAlarmStatus(1);
                        break;
                    case 'C':
                        zone.setAlarmStatus(11);
                        break;
                    case 'D':
                        zone.setAlarmStatus(12);
                        break;
                    case 'E':
                        zone.setAlarmStatus(13);
                        break;
                    case 'F':
                        zone.setAlarmStatus(14);
                        break;
                    case 'G':
                        zone.setAlarmStatus(15);
                        break;
                    case 'H':
                        zone.setAlarmStatus(16);
                        break;
                    case 'I':
                        zone.setAlarmStatus(17);
                        break;
                    default:
                        zone.setAlarmStatus(0);
                        break;
                }
                i++;
            }

            status.getZones().add(zone);
            zoneNo++;
        }
    }
}
