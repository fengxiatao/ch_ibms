package com.jokeep.business.controller;

import com.alibaba.fastjson.JSONObject;
import com.jokeep.annotations.NoAuthentication;
import com.jokeep.annotations.RequestDecrypt;
import com.jokeep.annotations.ResponseEncrypt;
import com.jokeep.business.service.IHCNetService;
import com.jokeep.framework.controller.BaseController;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/HCNet")
public class HCNetController extends BaseController {

    @Autowired
    IHCNetService ihcNetService;

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value = "云台控制", notes = "云台控制")
    @RequestMapping(value = "/panTiltControl", method = RequestMethod.POST)
    public Object panTiltControl(@RequestBody JSONObject map) {
        return ihcNetService.panTiltControl(map);
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value = "获取预置点名称", notes = "获取预置点名称")
    @RequestMapping(value = "/GetDVRConfig", method = RequestMethod.POST)
    public Object GetDVRConfig(@RequestBody JSONObject map) {
        return ihcNetService.GetDVRConfig(map);
    }


    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value = "预置点设置", notes = "预置点设置")
    @RequestMapping(value = "/moveYZD", method = RequestMethod.POST)
    public Object moveYZD(@RequestBody JSONObject map) {
        return ihcNetService.moveYZD(map);
    }


    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @ApiOperation(value = "预置点名称设置", notes = "预置点设置")
    @RequestMapping(value = "/UpdateYZDName", method = RequestMethod.POST)
    public Object UpdateYZDName(@RequestBody JSONObject map) {
        return ihcNetService.UpdateYZDName(map);
    }


    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/openOrCloseSpeek" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> openOrCloseSpeek(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String data=ihcNetService.openOrCloseSpeek(jsonObject);
        map.put("data",data);
        return map;
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/StartSpeekData" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> StartSpeekData(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String data=ihcNetService.StartSpeekData(jsonObject);
        map.put("data",data);
        return map;
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/setAlarmLight" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> setAlarmLight(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String data=ihcNetService.setAlarmLight(jsonObject);
        map.put("data",data);
        return map;
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/getToolPoint" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> getToolPoint(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String data=ihcNetService.getToolPoint(jsonObject);
        map.put("data",data);
        return map;
    }





}