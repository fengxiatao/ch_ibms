package com.jokeep.business.service;

import com.alibaba.fastjson.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

public interface DHNetService {

    void ChangeNormalControle(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, int nChannelID, String command, int lParam1, int lParam2, String type, int yzdID);

    void ChangeYZDControle(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, int nChannelID, String command, int lParam1, int lParam2, String type, int yzdID);

    List<Map<String, Object>> getYZDList(String m_strIp, int m_nPort, String m_strUser, String m_strPassword);

    String openOrCloseSpeek(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, String type);

    String setAlarmLight(String m_strIp, int m_nPort,int nChannelID, String m_strUser, String m_strPassword, String type);


    String getToolPoint(String m_strIp, int m_nPort, String m_strUser, String m_strPassword);

    String StartSpeekData(String m_strIp, int m_nPort, int nChannelID, String m_strUser, String m_strPassword,String type,String F_FileUrl);

    List<Map<String, Object>> getFileList(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) throws UnsupportedEncodingException;

    String UpdateYZDName(String m_strIp, int m_nPort, int nChannelID, String m_strUser, String m_strPassword, String name,String yzdIDs);

    /**
     * 查询录像文件列表（使用大华SDK CLIENT_QueryRecordFile）
     * @param m_strIp 设备IP
     * @param m_nPort 设备端口
     * @param m_strUser 用户名
     * @param m_strPassword 密码
     * @param nChannelID 通道号
     * @param startTime 开始时间 yyyy-MM-dd HH:mm:ss
     * @param endTime 结束时间 yyyy-MM-dd HH:mm:ss
     * @return 录像文件列表
     */
    List<Map<String, Object>> queryRecordFiles(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, 
                                                int nChannelID, String startTime, String endTime);
}
