package com.jokeep.business.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.annotations.NoAuthentication;
import com.jokeep.annotations.RequestDecrypt;
import com.jokeep.annotations.ResponseEncrypt;
import com.jokeep.business.service.IWvpApiService;
import com.jokeep.framework.controller.BaseController;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("/wvp")
public class WvpApiController extends BaseController {
    @Autowired
    IWvpApiService iWvpApiService;

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="查询预置位",notes="查询预置位")
    @RequestMapping(value = "/presetQuery", method = RequestMethod.POST)
    public Object presetQuery(@RequestBody JSONObject map) {
        String channelId=map.getString("channelId");
        String deviceId=map.getString("deviceId");
        return iWvpApiService.presetQuery(deviceId,channelId);
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="看守位控制",notes="看守位控制")
    @RequestMapping(value = "/homePosition", method = RequestMethod.POST)
    public Object homePosition(@RequestBody JSONObject map) {
        String channelId=map.getString("channelId");
        String deviceId=map.getString("deviceId");
        String enabled=map.getString("enabled");
        String presetIndex=map.getString("presetIndex");
        String resetTime=map.getString("resetTime");
        return iWvpApiService.homePosition(deviceId,channelId,enabled,presetIndex,resetTime);
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="云台控制",notes="控制指令,允许值: left, right, up, down, upleft, upright, downleft, downright, zoomin, zoomout, stop")
    @RequestMapping(value = "/ptzControl", method = RequestMethod.POST)
    public Object ptzControl(@RequestBody JSONObject map){
        String channelId=map.getString("channelId");
        String deviceId=map.getString("deviceId");
        String command=map.getString("command");
        return iWvpApiService.ptzControl(deviceId,channelId,command);
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="手动录像",notes="命令， 可选值：Record（手动录像），StopRecord（停止手动录像")
    @RequestMapping(value = "/controlRecord", method = RequestMethod.POST)
    public Object controlRecord(@RequestBody JSONObject map){
        String channelId=map.getString("channelId");
        String deviceId=map.getString("deviceId");
        String command=map.getString("recordCmdStr");
        return iWvpApiService.controlRecord(deviceId,channelId,command);
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="云端录像",notes="查询云端录像")
    @RequestMapping(value = "/queryRecordList", method = RequestMethod.POST)
    public Object queryRecordList(@RequestBody JSONObject map) {
        int page=map.getInteger("page");
        int count=map.getInteger("count");
        return iWvpApiService.queryRecordList(page,count);
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="前端控制命令",notes="前端控制命令")
    @RequestMapping(value = "/frontEndCommand", method = RequestMethod.POST)
    public Object frontEndCommand(@RequestBody JSONObject map) {
        String channelId=map.getString("channelId");
        String deviceId=map.getString("deviceId");
        int cmdCode=map.getInteger("cmdCode");
        int parameter1=map.getInteger("parameter1");
        int parameter2=map.getInteger("parameter2");
        int combindCode2=map.getInteger("combindCode2");
        return iWvpApiService.frontEndCommand(channelId,deviceId,cmdCode,parameter1,parameter2,combindCode2);
    }
    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="录像时间列表",notes="查询录像列表")
    @RequestMapping(value = "/recordDateList", method = RequestMethod.POST)
    public Object recordDateList(@RequestBody JSONObject map) {
        String app=map.getString("app");
        String stream=map.getString("stream");
        return iWvpApiService.recordDateList(app,stream);
    }
    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value="录像文件列表",notes="查询录像列表")
    @RequestMapping(value = "/recordFileList", method = RequestMethod.POST)
    public Object recordFileList(@RequestBody JSONObject map) {
        int pageNo=map.getInteger("pageNo");
        int pageSize=map.getInteger("pageSize");
        String app=map.getString("app");
        String stream=map.getString("stream");
        String startTime=map.getString("startTime");
        String endTime=map.getString("endTime");
        return iWvpApiService.recordFileList(pageNo,pageSize,app,stream,startTime,endTime);
    }
}
