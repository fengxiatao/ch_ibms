package com.jokeep.business.serviceImpl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.business.common.WvpHttpRequest;
import com.jokeep.business.service.IWvpApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WvpApiServiceImpl implements IWvpApiService {
    private final String PREST_QUERY = "/api/ptz/preset/query/{deviceId}/{channelId}";
    private final String HOME_POSITION = "/api/device/control/home_position/{deviceId}/{enabled}";
    private final String PTZ_CONTROL = "/api/ptz/control/{deviceId}/{channelId}";
    private final String CONTROL_RECORD = "/api/device/control/record/{deviceId}/{recordCmdStr}";
    private final String RECORD_LIST="/record_proxy/FQ3TF8yT83wh5Wvz/api/record/list";
    private final String FRONT_END_COMMAND="/api/ptz/front_end_command/{deviceId}/{channelId}";
    private final String RECORD_DATE_LIST="/record_proxy/FQ3TF8yT83wh5Wvz/api/record/date/list";
    private final String RECORD_FILE_LIST="/record_proxy/FQ3TF8yT83wh5Wvz/api/record/file/list";

    @Autowired
    WvpHttpRequest wvpHttpRequest;

    @Override
    public Object presetQuery(String deviceId, String channelId) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deviceId", deviceId);
        uriVariables.put("channelId", channelId);
        Object result = wvpHttpRequest.get(PREST_QUERY, null, uriVariables, Object.class);
        return result;
    }

    @Override
    public Object homePosition(String deviceId, String channelId, String enabled, String presetIndex, String resetTime) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deviceId", deviceId);
        uriVariables.put("enabled", enabled);
        Map<String, String> params = new HashMap<>();
        params.put("channelId", channelId);
        params.put("presetIndex", presetIndex);
        params.put("resetTime", resetTime);
        Object result = wvpHttpRequest.get(HOME_POSITION, params, uriVariables, Object.class);
        return result;
    }

    @Override
    public Object ptzControl(String deviceId, String channelId, String command) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deviceId", deviceId);
        uriVariables.put("channelId", channelId);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("command", command);
        params.add("horizonSpeed", 100);
        params.add("verticalSpeed", 100);
        params.add("zoomSpeed", 100);
        Object result = wvpHttpRequest.post(PTZ_CONTROL, params, null, uriVariables, String.class);
        return result;
    }

    @Override
    public Object controlRecord(String deviceId, String channelId, String recordCmdStr) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deviceId", deviceId);
        uriVariables.put("recordCmdStr", recordCmdStr);
        Map<String, String> params = new HashMap<>();
        params.put("channelId", channelId);
        Object result = wvpHttpRequest.get(CONTROL_RECORD, params, uriVariables, Object.class);
        return result;
    }
    @Override
    public Object queryRecordList(int page, int count) {
        Map<String,String> params=new HashMap<>();
        params.put("page",page+"");
        params.put("count",count+"");
        Object result=wvpHttpRequest.get(RECORD_LIST,params,null,Object.class);
        return result;
    }

    @Override
    public Object frontEndCommand(String channelId, String deviceId, int cmdCode, int parameter1, int parameter2, int combindCode2) {
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("deviceId", deviceId);
        uriVariables.put("channelId", channelId);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        params.add("cmdCode", cmdCode);
        params.add("parameter1", parameter1);
        params.add("parameter2", parameter2);
        params.add("combindCode2", combindCode2);
        Object result = wvpHttpRequest.post(FRONT_END_COMMAND, params, null, uriVariables, String.class);
        return result;
    }

    @Override
    public Object recordDateList(String app, String stream) {
        Map<String,String> params=new HashMap<>();
        params.put("app",app);
        params.put("stream",stream);
        Object result=wvpHttpRequest.get(RECORD_DATE_LIST,params,null,Object.class);
        return result;
    }

    @Override
    public Object recordFileList(int pageNo, int pageSize, String app, String stream, String startTime, String endTime) {
        Map<String,String> params=new HashMap<>();
        params.put("pageNo",pageNo+"");
        params.put("pageSize",pageSize+"");
        params.put("app",app);
        params.put("stream",stream);
        params.put("startTime",startTime);
        params.put("endTime",endTime);
        Object result=wvpHttpRequest.get(RECORD_FILE_LIST,params,null,Object.class);
        return result;
    }
}
