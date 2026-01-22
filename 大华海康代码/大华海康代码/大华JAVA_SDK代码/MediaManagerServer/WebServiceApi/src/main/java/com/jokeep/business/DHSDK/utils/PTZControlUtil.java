package com.jokeep.business.DHSDK.utils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.business.DHSDK.LastError;
import com.jokeep.business.DHSDK.NetSDKLib;
import com.jokeep.business.DHSDK.ToolKits;
import com.jokeep.business.DHSDK.common.Res;
import com.jokeep.business.DHSDK.demo.module.AutoRegisterModule;
import com.jokeep.business.DHSDK.demo.module.LoginModule;
import com.jokeep.business.DHSDK.demo.module.TalkModule;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import lombok.var;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.company.NetSDK.INetSDK.GetLastError;
import static com.jokeep.business.DHSDK.NetSDKLib.CFG_CMD_ALARMLAMP;

import static com.jokeep.business.DHSDK.NetSDKLib.CFG_CMD_LIGHT;
import static com.jokeep.business.DHSDK.ToolKits.getErrorCodePrint;
import static com.jokeep.business.DHSDK.demo.module.TalkModule.startTalk;
import static com.jokeep.business.DHSDK.demo.module.TalkModule.stopTalk;

public class PTZControlUtil {

    public static NetSDKLib.LLong m_hTalkHandle = new NetSDKLib.LLong(0); // 语音对讲句柄

    static NetSDKLib configapi = NetSDKLib.CONFIG_INSTANCE;

    // 初始化sdk
    public static NetSDKLib netsdk = NetSDKLib.NETSDK_INSTANCE;

    // 设备信息
    private static NetSDKLib.NET_DEVICEINFO_Ex m_stDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();

    // 登陆句柄
    private static NetSDKLib.LLong m_hLoginHandle = new NetSDKLib.LLong(0);

//    // 网络断线处理
    private static DisConnect disConnect =new DisConnect();
//
    // 设备连接恢复，实现设备连接恢复接口
    private static HaveReConnect haveReConnect = new HaveReConnect();

    public static void main(String[] args) {
        String m_strIp="192.168.60.101";
        int m_nPort=80;
        String m_strUser="admin";
        String m_strPassword="jokeep2012";
        int nChannelID=0;//默认通道为0//rtsp://admin:jokeep2012@192.168.60.101:80/cam/realmonitor?channel=1&subtype=0
        String command="RIGHT";//向左
        int lParam1=1;//只有有左上坐下操作才会有这个（1-8）
        int lParam2=8;//垂直水平移动速度(1-8)
        int yzdID=1;
        String type="1";
        upControlPtz(m_strIp,m_nPort,m_strUser,m_strPassword,nChannelID,command,lParam1,lParam2,type,yzdID);
    }
    /**
     * 云台控制
     * @param m_strIp       ip
     * @param m_nPort       端口
     * @param m_strUser     登录名
     * @param m_strPassword 密码
     * @param nChannelID    通道id 默认为0
     * @param command       命令
     * @param lParam1       默认 0，当有左上或左下等操作时才会传值 （1-8）
     * @param lParam2       垂直/水平 移动速度 （1-8）
     */
    public static void upControlPtz(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, int nChannelID, String command, int lParam1, int lParam2,String type,int yzdID) {
        // 初始化
        init(disConnect, haveReConnect);
//        // 若未登录，先登录
        if (m_hLoginHandle.longValue() == 0) {
            login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }
        // 开始移动，若超过角度则会变为左右移动
        if(m_hLoginHandle.longValue() != 0){
            if("1".equals(type)){
                control(nChannelID, command, lParam1, lParam2);
            }
            else if("2".equals(type)){
                controlYZD(nChannelID,command,yzdID);
            }


            getYZDList(nChannelID);
        }
//        // 退出
//        LoginModule.logout();
//        // 释放资源
//        LoginModule.cleanup();
    }

    /**
     *
     * @param nChannelID
     * @param command
     * @param lParam2
     */
    private static void controlYZD(int nChannelID, String command, int lParam2) {//lParam2预置点编号1-232
        //设置预置点
        if("YZD_SET".equals(command)){
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,  NetSDKLib.NET_PTZ_ControlType.NET_PTZ_POINT_SET_CONTROL, 0, lParam2, 0,0);
        }
        //删除预置点
        else if("YZD_DEL".equals(command)){
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,  NetSDKLib.NET_PTZ_ControlType.NET_PTZ_POINT_DEL_CONTROL, 0, lParam2, 0,0);
        }
        //移动预置点
        else if("YZD_MOVE".equals(command)){
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,  NetSDKLib.NET_PTZ_ControlType.NET_PTZ_POINT_MOVE_CONTROL, 0, lParam2, 0,0);
        }
    }

    private static void  getYZDList(int nChannelID){
//          NetSDKLib.PTZ_PRESET_INFO net_ptz_preset=new NetSDKLib.PTZ_PRESET_INFO();
//          IntByReference error = new IntByReference(0);
//          int nBufferLen = 100*1024;
//          byte[] strBuffer = new byte[nBufferLen];
//
//            var result=netsdk.CLIENT_GetNewDevConfig(m_hLoginHandle,NetSDKLib.CFG_CMD_PTZ_PRESET,-1,strBuffer,nBufferLen,error, 3000);
//            if (!result)
//            {
//                System.out.println(getErrorCodePrint());
//                return;
//            }else {
//            String s = new String(strBuffer);
//            System.out.println(s);
//            JSONObject jsonObject= JSON.parseObject(s);
//            System.out.println(jsonObject);
//            JSONObject Json=jsonObject.getJSONObject("params");
//            List<Map<String,Object>>list= (List<Map<String, Object>>) Json.get("table");
//            List<Map<String,Object>>listResult= (List<Map<String, Object>>) list.get(0);
//                System.out.println(listResult);
//            List<Map<String,Object>>listYZD=new ArrayList<>();
//                for (int i = 0; i <listResult.size() ; i++) {
//                  if("true" == String.valueOf(listResult.get(i).get("Enable"))&&!"".equals(listResult.get(i).get("Name"))){
//                      listResult.get(i).put("YZDID",i+1);
//                      listYZD.add(listResult.get(i));
//                  }
//                }
//                System.out.println(listYZD);
//             }


            NetSDKLib.CFG_ALARMLAMP_INFO CFG_ALARMLAMP_INFO=new NetSDKLib.CFG_ALARMLAMP_INFO();
            CFG_ALARMLAMP_INFO.emAlarmLamp=1;
            Structure strCmd=CFG_ALARMLAMP_INFO;
            int nBufferLen =100*1024;
            IntByReference error=new IntByReference(0);
            IntByReference restart=new IntByReference(0);
            byte[] bytes = new byte[nBufferLen];

            for(int i=0; i<nBufferLen; i++)bytes[i]=0;
        if (configapi.CLIENT_PacketData(CFG_CMD_ALARMLAMP, strCmd.getPointer(), strCmd.size(),
                bytes, nBufferLen)) {
            String s=new String(bytes);
            var result =netsdk.CLIENT_SetNewDevConfig(m_hLoginHandle,CFG_CMD_ALARMLAMP,-1,bytes,nBufferLen,error,restart,3000);
            if (!result)
            {
                System.out.println(getErrorCodePrint());
                return;
            }
        }


//        NetSDKLib.CFG_LIGHT_INFO CFG_LIGHT_INFO=new NetSDKLib.CFG_LIGHT_INFO();
//        CFG_LIGHT_INFO.nState=1;
//        CFG_LIGHT_INFO.emType=1;
//        Structure strCmd=CFG_LIGHT_INFO;
//        int nBufferLen =100*1024;
//        IntByReference error=new IntByReference(0);
//        IntByReference restart=new IntByReference(0);
//        byte[] bytes = new byte[nBufferLen];
//
//        for(int i=0; i<nBufferLen; i++)bytes[i]=0;
//        if (configapi.CLIENT_PacketData(CFG_CMD_LIGHT, strCmd.getPointer(), strCmd.size(),
//                bytes, nBufferLen)) {
//            String s=new String(bytes);
//            var result =netsdk.CLIENT_SetNewDevConfig(m_hLoginHandle,CFG_CMD_LIGHT,-1,bytes,nBufferLen,error,restart,3000);
//            if (!result)
//            {
//                System.out.println(getErrorCodePrint());
//                return;
//            }
//        }

    }



    /**
     * 云台控制
     * @param nChannelID
     * @param command
     * @param lParam1
     * @param lParam2
     */
    private static void control(int nChannelID, String command, int lParam1, int lParam2) {
        if ("UP".equals(command)) {
            //向上
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_UP_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_UP_CONTROL,
                    0, 0, 0, 1);
        } else if ("DOWN".equals(command)) {
            //向下
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_DOWN_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_DOWN_CONTROL,
                    0, 0, 0, 1);
        } else if ("LEFT".equals(command)) {
            //向左
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_LEFT_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_LEFT_CONTROL,
                    0, 0, 0, 1);
        } else if ("RIGHT".equals(command)) {
            //向右
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_RIGHT_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_RIGHT_CONTROL,
                    0, 0, 0, 1);
        } else if ("LEFT_UP".equals(command)) {
            //向左上
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTTOP,
                    lParam1, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTTOP,
                    0, 0, 0, 1);
        } else if ("LEFT_DOWN".equals(command)) {
            //向左下
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTDOWN,
                    lParam1, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTDOWN,
                    0, 0, 0, 1);
        } else if ("RIGHT_UP".equals(command)) {
            //向右上
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTTOP,
                    lParam1, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTTOP,
                    0, 0, 0, 1);
        } else if ("RIGHT_DOWN".equals(command)) {
            //向右下
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTDOWN,
                    lParam1, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTDOWN,
                    0, 0, 0, 1);
        } else if ("FOCUS_NEAR".equals(command)) {
            //变焦+
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_ADD_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_ADD_CONTROL,
                    0, 0, 0, 1);
        } else if ("FOCUS_FAR".equals(command)) {
            //变焦-
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_DEC_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_DEC_CONTROL,
                    0, 0, 0, 1);
        } else if ("ZOOM_IN".equals(command)) {
            //变倍+
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_ADD_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_ADD_CONTROL,
                    0, 0, 0, 1);
        } else if ("ZOOM_OUT".equals(command)) {
            //变倍-
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_DEC_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_DEC_CONTROL,
                    0, 0, 0, 1);
        } else if ("IRIS_ENLARGE".equals(command)) {
            //光圈+
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_ADD_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_ADD_CONTROL,
                    0, 0, 0, 1);
        } else if ("IRIS_REDUCE".equals(command)) {
            //光圈-
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_DEC_CONTROL,
                    0, lParam2, 0, 0);
            netsdk.CLIENT_DHPTZControlEx(m_hLoginHandle, nChannelID,
                    NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_DEC_CONTROL,
                    0, 0, 0, 1);
        }
    }

    /**
     * 初始化
     */
    private static boolean bInit    = false;
    private static boolean bLogopen = false;
    public static boolean init(NetSDKLib.fDisConnect disConnect, NetSDKLib.fHaveReConnect haveReConnect) {
        bInit = netsdk.CLIENT_Init(disConnect, null);
        if(!bInit) {
            System.out.println("Initialize SDK failed");
            return false;
        }

        //打开日志，可选
        NetSDKLib.LOG_SET_PRINT_INFO setLog = new NetSDKLib.LOG_SET_PRINT_INFO();
        File path = new File("./sdklog/");
        if (!path.exists()) {
            path.mkdir();
        }
        String logPath = path.getAbsoluteFile().getParent() + "\\sdklog\\" + ToolKits.getDate() + ".log";
        setLog.nPrintStrategy = 0;
        setLog.bSetFilePath = 1;
        System.arraycopy(logPath.getBytes(), 0, setLog.szLogFilePath, 0, logPath.getBytes().length);
        System.out.println(logPath);
        setLog.bSetPrintStrategy = 1;
        bLogopen = netsdk.CLIENT_LogOpen(setLog);
        if(!bLogopen ) {
            System.err.println("Failed to open NetSDK log");
        }

        // 设置断线重连回调接口，设置过断线重连成功回调函数后，当设备出现断线情况，SDK内部会自动进行重连操作
        // 此操作为可选操作，但建议用户进行设置
        netsdk.CLIENT_SetAutoReconnect(haveReConnect, null);

        //设置登录超时时间和尝试次数，可选
        int waitTime = 5000; //登录请求响应超时时间设置为5S
        int tryTimes = 1;    //登录时尝试建立链接1次
        netsdk.CLIENT_SetConnectTime(waitTime, tryTimes);


        // 设置更多网络参数，NET_PARAM的nWaittime，nConnectTryNum成员与CLIENT_SetConnectTime
        // 接口设置的登录设备超时时间和尝试次数意义相同,可选
        NetSDKLib.NET_PARAM netParam = new NetSDKLib.NET_PARAM();
        netParam.nConnectTime = 10000;      // 登录时尝试建立链接的超时时间
        netParam.nGetConnInfoTime = 3000;   // 设置子连接的超时时间
        netParam.nGetDevInfoTime = 3000;//获取设备信息超时时间，为0默认1000ms
        netsdk.CLIENT_SetNetworkParam(netParam);

        return true;
    }

    /**
     * 登录
     *
     * @param m_strIp       ip
     * @param m_nPort       端口号
     * @param m_strUser     账号
     * @param m_strPassword 密码
     * @return 成功则 true
     */
    public static boolean login(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) {
        //入参
        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstInParam.nPort = m_nPort;
        pstInParam.szIP = m_strIp.getBytes();
        pstInParam.szPassword = m_strPassword.getBytes();
        pstInParam.szUserName = m_strUser.getBytes();
        //出参
        NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstOutParam.stuDeviceInfo = m_stDeviceInfo;
        m_hLoginHandle = netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
        System.out.println(netsdk.getClass());
        if (m_hLoginHandle.longValue() == 0) {
            System.err.printf("登录失败！\n", m_strIp, m_nPort, getErrorCodePrint());
        } else {
            System.out.println("登录成功： [ " + m_strIp + " ]");
        }
        return m_hLoginHandle.longValue() == 0 ? false : true;
    }


    /**
     * 退出登录
     */
    private static boolean logout() {
        if (m_hLoginHandle.longValue() == 0) {
            return false;
        }

        boolean bRet = netsdk.CLIENT_Logout(m_hLoginHandle);
        if (bRet) {
            m_hLoginHandle.setValue(0);
        }

        return bRet;
    }

    /**
     * 释放资源
     */
    public static void cleanup() {
        if (bLogopen) {
            netsdk.CLIENT_LogClose();
        }
        if (bInit) {
            netsdk.CLIENT_Cleanup();
        }
    }
    /////////////////面板///////////////////
    // 设备断线回调: 通过 CLIENT_Init 设置该回调函数，当设备出现断线时，SDK会调用该函数
    private static class DisConnect implements NetSDKLib.fDisConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("Device[%s] Port[%d] DisConnect!\n", pchDVRIP, nDVRPort);
            // 断线提示
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.printf(Res.string().getPTZ() + " : " + Res.string().getDisConnectReconnecting());
                }
            });
        }
    }

    // 网络连接恢复，设备重连成功回调
    // 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
    private static class HaveReConnect implements NetSDKLib.fHaveReConnect {
        public void invoke(NetSDKLib.LLong m_hLoginHandle, String pchDVRIP, int nDVRPort, Pointer dwUser) {
            System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);

            // 重连提示
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    System.out.printf(Res.string().getPTZ() + " : " + Res.string().getOnline());
                }
            });
        }
    }

}
