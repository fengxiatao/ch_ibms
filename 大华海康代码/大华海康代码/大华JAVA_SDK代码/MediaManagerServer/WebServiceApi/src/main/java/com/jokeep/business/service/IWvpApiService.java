package com.jokeep.business.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

public interface IWvpApiService {
    Object presetQuery(String deviceId, String channelId);

    Object homePosition(String deviceId, String channelId,String enabled,String presetIndex,String resetTime);

    Object ptzControl(String deviceId, String channelId,String command);

    Object controlRecord(String deviceId, String channelId,String recordCmdStr);

    Object queryRecordList(int page, int count);

    Object frontEndCommand(String channelId,String deviceId, int cmdCode, int parameter1, int parameter2, int combindCode2);

    Object recordDateList(String app,String stream);

    Object recordFileList(int pageNo,int pageSize,String app,String stream,String startTime ,String endTime);
}
