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
}
