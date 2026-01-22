package com.jokeep.business.controller;

import com.alibaba.fastjson.JSONObject;
import com.jokeep.annotations.NoAuthentication;
import com.jokeep.annotations.RequestDecrypt;
import com.jokeep.annotations.ResponseEncrypt;
import com.jokeep.business.DHSDK.utils.PTZControlUtil;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/DHController")
public class InitDataController {

    @NoAuthentication
    @RequestDecrypt(false)
    @ResponseEncrypt(false)
    @RequestMapping(value = "/panTiltControl" , method = RequestMethod.POST, produces = "application/json;charset=utf-8")
    public void panTiltControl(HttpServletRequest request,@RequestBody JSONObject jsonObject){
            String m_strUser=jsonObject.getString("UserName");//登录用户名
            String m_strPassword=jsonObject.getString("Password");//登录密码
            String command=jsonObject.getString("Command");//指令
            String type=jsonObject.getString("type");//"1"控制方位，"2"设置预置点
            String m_strIp="192.168.60.101";
            int yzdID=1;
            if(!jsonObject.getString("yzdID").equals("")){
                yzdID=Integer.parseInt(jsonObject.getString("yzdID").trim());
            }

            int m_nPort=80;
            int lParam1=1;//只有有左上坐下操作才会有这个（1-8）
            int lParam2=8;//垂直水平移动速度(1-8)
            int nChannelID=0;//默认通道为0
            PTZControlUtil.upControlPtz(m_strIp,m_nPort,m_strUser,m_strPassword,nChannelID,command,lParam1,lParam2,type,yzdID);
    }
}
