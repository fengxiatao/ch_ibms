package com.jokeep.business.service;

import com.alibaba.fastjson.JSONObject;

public interface IHCNetService {

    /**
     * 云台控制
     * @param map
     */
    Object panTiltControl(JSONObject map);

    /**
     * 获取预置点名称
     * @param map
     */
    Object GetDVRConfig(JSONObject map);

    Object moveYZD(JSONObject map);

    String openOrCloseSpeek(JSONObject jsonObject);

    String setAlarmLight(JSONObject jsonObject);

    String getToolPoint(JSONObject jsonObject);

    String StartSpeekData(JSONObject jsonObject);

    Object UpdateYZDName(JSONObject map);
}
