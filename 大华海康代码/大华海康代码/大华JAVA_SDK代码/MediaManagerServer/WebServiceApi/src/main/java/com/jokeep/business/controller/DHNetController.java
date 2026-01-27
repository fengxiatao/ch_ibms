package com.jokeep.business.controller;


import com.alibaba.fastjson.JSONObject;
import com.jokeep.annotations.NoAuthentication;
import com.jokeep.annotations.RequestDecrypt;
import com.jokeep.annotations.ResponseEncrypt;
import com.jokeep.business.DHSDK.utils.PTZControlUtil;
import com.jokeep.business.service.DHNetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/DHNetController")
public class DHNetController {
    @Resource
    DHNetService dhNetService;

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/panTiltControl" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public void panTiltControl(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        String command=jsonObject.getString("Command");//指令
        String type=jsonObject.getString("type");//"1"控制方位，"2"设置预置点
        String m_strIp="192.168.60.100";
        int yzdID=1;
        if(!jsonObject.getString("yzdID").equals("")){
            yzdID=Integer.parseInt(jsonObject.getString("yzdID").trim());
        }

        int m_nPort=80;
        int lParam1=1;//只有有左上坐下操作才会有这个（1-8）
        int lParam2=8;//垂直水平移动速度(1-8)
        int nChannelID=0;//默认通道为0
        if("1".equals(type)){
            dhNetService.ChangeNormalControle(m_strIp,m_nPort,m_strUser,m_strPassword,nChannelID,command,lParam1,lParam2,type,yzdID);
        }
        else if("2".equals(type)){
            dhNetService.ChangeYZDControle(m_strIp,m_nPort,m_strUser,m_strPassword,nChannelID,command,lParam1,lParam2,type,yzdID);
        }
    }


    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/getYZDList" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> getYZDList(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        List<Map<String,Object>> list=dhNetService.getYZDList(m_strIp,m_nPort,m_strUser,m_strPassword);
        map.put("ListYZD",list);
        return map;
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/getFileList" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> getFileList(HttpServletRequest request, @RequestBody JSONObject jsonObject) throws UnsupportedEncodingException {
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        List<Map<String,Object>> list=dhNetService.getFileList(m_strIp,m_nPort,m_strUser,m_strPassword);
        map.put("LisFile",list);
        return map;
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/openOrCloseSpeek" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> openOrCloseSpeek(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        String type=jsonObject.getString("type");//获取类型 0：开启 1 关闭
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        String data=dhNetService.openOrCloseSpeek(m_strIp,m_nPort,m_strUser,m_strPassword,type);
        map.put("data",data);
        return map;
    }



    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/StartSpeekData" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> StartSpeekData(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        String type=jsonObject.getString("types");//获取类型 0：开启 1 关闭
        String F_FileUrl=jsonObject.getString("F_FileUrl");
        int nChannelID=0;//默认通道为0
        String data=dhNetService.StartSpeekData(m_strIp,m_nPort,nChannelID,m_strUser,m_strPassword,type,F_FileUrl);
        map.put("data",data);
        return map;
    }


    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/setAlarmLight" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> setAlarmLight(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        String type=jsonObject.getString("type");//获取类型 0：开启 1 关闭
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        int nChannelID=0;//默认通道为0
        String data=dhNetService.setAlarmLight(m_strIp,m_nPort,nChannelID,m_strUser,m_strPassword,type);
        map.put("data",data);
        return map;
    }

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/UpdateYZDName" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> UpdateYZDName(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        String name=jsonObject.getString("name");//获取需要修改的名称
        String yzdIDs=jsonObject.getString("yzdIDs");//修改的预置点id'
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        int nChannelID=0;//默认通道为0
        String data=dhNetService.UpdateYZDName(m_strIp,m_nPort,nChannelID,m_strUser,m_strPassword,name,yzdIDs);
        map.put("data",data);
        return map;
    }



    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/getToolPoint" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String,Object> getToolPoint(HttpServletRequest request, @RequestBody JSONObject jsonObject){
        Map<String,Object>map=new HashMap<>();
        String m_strUser=jsonObject.getString("UserName");//登录用户名
        String m_strPassword=jsonObject.getString("Password");//登录密码
        int m_nPort=80;
        String m_strIp="192.168.60.100";
        String data=dhNetService.getToolPoint(m_strIp,m_nPort,m_strUser,m_strPassword);
        map.put("data",data);
        return map;
    }

    /**
     * 查询录像文件列表（使用大华SDK）
     * @param jsonObject 包含设备信息、通道号、时间范围
     * @return 录像文件列表
     */
    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/queryRecordFiles", method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public Map<String, Object> queryRecordFiles(HttpServletRequest request, @RequestBody JSONObject jsonObject) {
        Map<String, Object> result = new HashMap<>();
        try {
            String m_strUser = jsonObject.getString("userName");       // 登录用户名
            String m_strPassword = jsonObject.getString("password");   // 登录密码
            String m_strIp = jsonObject.getString("ip");               // 设备IP
            int m_nPort = jsonObject.getInteger("port");               // 设备端口
            int nChannelID = jsonObject.getInteger("channelId");       // 通道号
            String startTime = jsonObject.getString("startTime");      // 开始时间 yyyy-MM-dd HH:mm:ss
            String endTime = jsonObject.getString("endTime");          // 结束时间 yyyy-MM-dd HH:mm:ss
            
            List<Map<String, Object>> recordFiles = dhNetService.queryRecordFiles(
                m_strIp, m_nPort, m_strUser, m_strPassword, 
                nChannelID, startTime, endTime
            );
            
            result.put("success", true);
            result.put("data", recordFiles);
            result.put("total", recordFiles.size());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询录像失败：" + e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
}
