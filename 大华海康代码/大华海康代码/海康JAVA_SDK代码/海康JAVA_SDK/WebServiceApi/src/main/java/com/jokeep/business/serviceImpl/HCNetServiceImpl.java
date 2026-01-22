package com.jokeep.business.serviceImpl;

import com.alibaba.fastjson.JSONObject;

import com.jokeep.business.common.osSelect;
import com.jokeep.business.service.IHCNetService;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.ptr.IntByReference;
import io.swagger.models.auth.In;
import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.Pointer;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static com.jokeep.business.serviceImpl.AudioTest.*;
import static com.jokeep.business.serviceImpl.HCNetSDK.*;


@Service
public class HCNetServiceImpl implements IHCNetService {
    // 接口的实例，通过接口实例调用外部dll/so的函数
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    // 用户登录返回句柄
    static int lUserID;
    static int lPlay = -1;  //预览句柄
    static FRealDataCallBack fRealDataCallBack;//预览回调函数实现

    static PlayCtrl playControl = null;
    static IntByReference m_lPort= new IntByReference(-1);

    @Value("${hcnetipaddress}")
    private String HCNetIPAddress;

    @Value("${hcnetport}")
    private short HCNetPort;

    @Value("${hcnetldchannel}")
    private int HCNetlDChannel;
    /**
     * @param m_sDeviceIP 设备ip地址
     * @param wPort       端口号，设备网络SDK登录默认端口8000
     * @param m_sUsername 用户名
     * @param m_sPassword 密码
     */
    public void Login_V40(String m_sDeviceIP, short wPort, String m_sUsername, String m_sPassword) {
        /* 注册 */
        // 设备登录信息
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();

        // 设备信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
        m_strLoginInfo.wPort = wPort;
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());
        // 是否异步登录：false- 否，true- 是
        m_strLoginInfo.bUseAsynLogin = false;
        // write()调用后数据才写入到内存中
        m_strLoginInfo.write();

        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID == -1) {
            System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return;
        } else {
            System.out.println("登录成功！");

            // read()后，结构体中才有对应的数据
            m_strDeviceInfo.read();
            return;
        }
    }

    //设备注销 SDK释放
    public void Logout() {
        if (lUserID >= 0) {
            if (hCNetSDK.NET_DVR_Logout(lUserID) == false) {
                System.out.println("注销失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            }
            System.out.println("注销成功");
            hCNetSDK.NET_DVR_Cleanup();
            return;
        } else {
            System.out.println("设备未登录");
            hCNetSDK.NET_DVR_Cleanup();
            return;
        }
    }

    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    if (osSelect.isWindows())
                        //win系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "\\WebServiceApi\\lib\\HCNetSDK.dll";

                    else if (osSelect.isLinux())
                        //Linux系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "/lib/libhcnetsdk.so";
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 播放库加载
     *
     * @return
     */
    private static boolean CreatePlayInstance() {
        if (playControl == null) {
            synchronized (PlayCtrl.class) {
                String strPlayPath = "";
                try {
                    if (osSelect.isWindows())
                        //win系统加载库路径
                        strPlayPath = System.getProperty("user.dir") + "\\WebServiceApi\\lib\\PlayCtrl.dll";
                    else if (osSelect.isLinux())
                        //Linux系统加载库路径
                        strPlayPath = System.getProperty("user.dir") + "/lib/libPlayCtrl.so";
                    playControl=(PlayCtrl) Native.loadLibrary(strPlayPath, PlayCtrl.class);

                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strPlayPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    static class FRealDataCallBack implements HCNetSDK.FRealDataCallBack_V30 {
        //预览回调
        public void invoke(int lRealHandle, int dwDataType, ByteByReference pBuffer, int dwBufSize, Pointer pUser) {
            //System.out.println("码流数据回调, 数据类型: " + dwDataType + ", 数据长度:" + dwBufSize);
            //播放库解码
            switch (dwDataType) {
                case HCNetSDK.NET_DVR_SYSHEAD: //系统头
                    if (!playControl.PlayM4_GetPort(m_lPort)) //获取播放库未使用的通道号
                    {
                        break;
                    }
                    if (dwBufSize > 0) {
                        if (!playControl.PlayM4_SetStreamOpenMode(m_lPort.getValue(), PlayCtrl.STREAME_REALTIME))  //设置实时流播放模式
                        {
                            break;
                        }
                        if (!playControl.PlayM4_OpenStream(m_lPort.getValue(), pBuffer, dwBufSize, 1024 * 1024)) //打开流接口
                        {
                            break;
                        }
                        if (!playControl.PlayM4_Play(m_lPort.getValue(), null)) //播放开始
                        {
                            break;
                        }
                    }
                case HCNetSDK.NET_DVR_STREAMDATA:   //码流数据
                    if ((dwBufSize > 0) && (m_lPort.getValue() != -1)) {
                        if (!playControl.PlayM4_InputData(m_lPort.getValue(), pBuffer, dwBufSize))  //输入流数据
                        {
                            break;
                        }
                    }
            }
        }
    }

    @Override
    public Object panTiltControl(JSONObject map) {
        try {
            // 初始化
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            //Login_V40(HCNetIPAddress, HCNetPort, "admin", "jokeep2012");
            Login_V40(HCNetIPAddress, HCNetPort, map.getString("UserName"), map.getString("Password"));
            /*
             *实现SDK中其余功能模快
             */
            hCNetSDK.NET_DVR_PTZControl_Other(lUserID,HCNetlDChannel,Integer.valueOf(map.getString("Command")),0);
            System.out.println(hCNetSDK.NET_DVR_GetLastError());
            Thread.sleep(100);
            hCNetSDK.NET_DVR_PTZControl_Other(lUserID,HCNetlDChannel,Integer.valueOf(map.getString("Command")),1);
            System.out.println(hCNetSDK.NET_DVR_GetLastError());
            return 1;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        } finally {
            //用户注销，释放SDK
            return 1;
        }
    }

    @Override
    public Object GetDVRConfig(JSONObject map) {
//        List<Map<String, Object>> userList = new ArrayList<>();
//        try {
//            // 初始化
//            boolean initSuc = hCNetSDK.NET_DVR_Init();
//            if (initSuc != true) {
//                System.out.println("初始化失败");
//            }
//            // 打印SDK日志
//            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
//            // 用户登陆操作
//            Login_V40(HCNetIPAddress, HCNetPort, map.getString("UserName"), map.getString("Password"));
//            /*
//             *实现SDK中其余功能模快
//             */
//            int len=1024*1024;
//            int lens=4*4096;
//            HCNetSDK.NET_DVR_XML_CONFIG_INPUT struXMLInput = new HCNetSDK.NET_DVR_XML_CONFIG_INPUT();
//            struXMLInput.read();
//            struXMLInput.dwSize = struXMLInput.size();
//            String strURL = "GET /ISAPI/PTZCtrl/channels/1/presets";//透传接口获取预置点信息 GET /ISAPI/PTZCtrl/channels/<channelID>/presets
//            int iURLlen = strURL.length();
//            HCNetSDK.BYTE_ARRAY ptrUrl = new HCNetSDK.BYTE_ARRAY(iURLlen);
//            System.arraycopy(strURL.getBytes(), 0, ptrUrl.byValue, 0, strURL.length());
//            ptrUrl.write();
//            struXMLInput.lpRequestUrl = ptrUrl.getPointer();
//            struXMLInput.dwRequestUrlLen = iURLlen;
//            String strInbuffer ="";
//            int iInBufLen = strInbuffer.length();
//            if(iInBufLen==0)
//            {
//                struXMLInput.lpInBuffer=null;
//                struXMLInput.dwInBufferSize=0;
//                struXMLInput.write();
//            }
//            else
//            {
//                HCNetSDK.BYTE_ARRAY ptrInBuffer = new HCNetSDK.BYTE_ARRAY(iInBufLen);
//                ptrInBuffer.read();
//                ptrInBuffer.byValue = strInbuffer.getBytes();
//                ptrInBuffer.write();
//
//                struXMLInput.lpInBuffer = ptrInBuffer.getPointer();
//                struXMLInput.dwInBufferSize = iInBufLen;
//                struXMLInput.write();
//
//            }
//            HCNetSDK.BYTE_ARRAY ptrStatusByte = new HCNetSDK.BYTE_ARRAY(len);
//            ptrStatusByte.read();
//
//            HCNetSDK.BYTE_ARRAY ptrOutByte = new HCNetSDK.BYTE_ARRAY(lens);
//            ptrOutByte.read();
//
//            HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT struXMLOutput = new HCNetSDK.NET_DVR_XML_CONFIG_OUTPUT();
//            struXMLOutput.read();
//            struXMLOutput.dwSize = struXMLOutput.size();
//            struXMLOutput.lpOutBuffer = ptrOutByte.getPointer();
//            struXMLOutput.dwOutBufferSize = ptrOutByte.size();
//            struXMLOutput.lpStatusBuffer = ptrStatusByte.getPointer();
//            struXMLOutput.dwStatusSize  = ptrStatusByte.size();
//            struXMLOutput.write();
//
//            if(!hCNetSDK.NET_DVR_STDXMLConfig(lUserID, struXMLInput, struXMLOutput))
//            {
//                int iErr = hCNetSDK.NET_DVR_GetLastError();
//                JOptionPane.showMessageDialog(null, "NET_DVR_STDXMLConfig失败，错误号：" + iErr);
//            }
//            else
//            {
//                struXMLOutput.read();
//                ptrOutByte.read();
//                ptrStatusByte.read();
//                String xmlString = new String(ptrOutByte.byValue).trim();
//                /*<?xml version="1.0" encoding="UTF-8"?>
//                <PTZPresetList version="2.0" xmlns="http://www.hikvision.com/ver20/XMLSchema">
//                <PTZPreset>
//                <enabled>true</enabled>
//                <id>1</id>
//                <presetName>预置点 1</presetName>
//                </PTZPreset>
//                </PTZPresetList>*/
////                System.out.println(xmlString);
//                Document document = DocumentHelper.parseText(xmlString);
//                List bodyInfoList = document.selectNodes("//PTZPresetList");
//                Iterator iterator = bodyInfoList.iterator();
//                while (iterator.hasNext()) {
//                    Element bodyInfoElement = (Element) iterator.next();
//                    // 解析 user，针对集合
//                    Iterator userIterator = bodyInfoElement.elementIterator("PTZPreset");
//                    while (userIterator.hasNext()) {
//                        Map<String, Object> userMap = new HashMap<>();
//                        Element element = (Element) userIterator.next();
//                        userMap.put("id", getElementText(element, "id"));
//                        userMap.put("presetName", getElementText(element, "presetName"));
//                        userMap.put("enabled", getElementText(element, "enabled"));
//                        userList.add(userMap);
//                    }
//
//                }
////                System.out.println(userList);
//                return userList;
//            }
//
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            return -1;
//        } finally {
//            //用户注销，释放SDK
//            return userList;
//        }

        List<Map<String, Object>> userList = new ArrayList<>();
        String data="";
        try {
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            Login_V40(HCNetIPAddress, HCNetPort, map.getString("UserName"), map.getString("Password"));


            HCNetSDK.NET_DVR_PRESET_NAMES struPtTZPos = new HCNetSDK.NET_DVR_PRESET_NAMES();
            IntByReference pUsers = new IntByReference(1);
            Pointer pointer=struPtTZPos.getPointer();
            boolean b_GetPTZ = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_PRESET_NAME, HCNetlDChannel, pointer, struPtTZPos.size(), pUsers);
            if (b_GetPTZ == false) {
                System.out.println("获取PTZ坐标信息失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                struPtTZPos.read();
                for (int i = 0; i <struPtTZPos.presets.length; i++) {
                    if(struPtTZPos.presets[i].wPanPos!=0&&struPtTZPos.presets[i].wTiltPos!=0){
                        String YZDName=new String(struPtTZPos.presets[i].byName,"GBK");
                        YZDName=YZDName.replaceAll("[\u0000]", "");
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("id", struPtTZPos.presets[i].wPresetNum);
                        userMap.put("presetName",YZDName);
                        userMap.put("wTiltPos",struPtTZPos.presets[i].wTiltPos);
                        userMap.put("wZoomPos",struPtTZPos.presets[i].wZoomPos);
                        userMap.put("wPanPos",YZDName);
                        userList.add(userMap);
                    }
                }
            }
            System.out.println(hCNetSDK.NET_DVR_GetLastError());
        } catch (Exception e) {
            e.printStackTrace();
            return "开启失败";
        } finally {
            return userList;
        }

    }

    @Override
    public Object UpdateYZDName(JSONObject map) {
        String data="";
        try {
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            Login_V40(HCNetIPAddress, HCNetPort, map.getString("UserName"), map.getString("Password"));
            IntByReference pUsers = new IntByReference(1);

            HCNetSDK.NET_DVR_PRESET_NAME struPtTZPos = new HCNetSDK.NET_DVR_PRESET_NAME();
            struPtTZPos.read();
            struPtTZPos.dwSize = struPtTZPos.size();
            String names =String.valueOf(map.getString("name"));
            byte[] midbytes=names.getBytes("GBK");
            struPtTZPos.byName= new byte[NAME_LEN];
            struPtTZPos.byName=midbytes;
            struPtTZPos.wPresetNum= (short)Integer.parseInt(map.getString("yzdIDs"));

            struPtTZPos.write();
            boolean b_GetPTZs = hCNetSDK.NET_DVR_SetDVRConfig(lUserID, HCNetSDK.NET_DVR_SET_PRESET_NAME, HCNetlDChannel, struPtTZPos.getPointer(), struPtTZPos.size());
            if (b_GetPTZs == false) {
                System.out.println("获取PTZ坐标信息失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                struPtTZPos.read();
            }
            System.out.println(hCNetSDK.NET_DVR_GetLastError());

        } catch (Exception e) {
            e.printStackTrace();
            return "开启失败";
        } finally {
            //用户注销，释放SDK
            return "用户退出";
        }
    }

    @Override
    public Object moveYZD(JSONObject map) {

        try {
            // 初始化
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            int Commond=Integer.parseInt(map.getString("Command"));
            int  yzdID=Integer.parseInt(map.getString("yzdID").trim());

            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            //Login_V40(HCNetIPAddress, HCNetPort, "admin", "jokeep2012");
            Login_V40(HCNetIPAddress, HCNetPort, map.getString("UserName"), map.getString("Password"));
            /*
             *实现SDK中其余功能模快
             */
            hCNetSDK.NET_DVR_PTZPreset_Other(lUserID,HCNetlDChannel,Commond,yzdID);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            //用户注销，释放SDK
            return 1;
        }
    }

    @Override
    public String openOrCloseSpeek(JSONObject jsonObject) {
        String type=jsonObject.getString("type");
        String data="";
        try {
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            Login_V40(HCNetIPAddress, HCNetPort, jsonObject.getString("UserName"), jsonObject.getString("Password"));
            if(type.equals("0")){
                StartSpeek(lUserID);
            }
            else {
                StopSpees(lUserID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "开启失败";
        } finally {
            //用户注销，释放SDK
//            Logout();
            return "用户退出";
        }
    }


    @Override
    public String StartSpeekData(JSONObject jsonObject) {
        String type=jsonObject.getString("type");
        String data="";
        try {
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            Login_V40(HCNetIPAddress, HCNetPort, jsonObject.getString("UserName"), jsonObject.getString("Password"));
            TestVoiceG711Trans(lUserID);
        } catch (Exception e) {
            e.printStackTrace();
            return "语音转发成功";
        } finally {
            //用户注销，释放SDK
//            Logout();
            return "用户退出";
        }
    }

    @Override
    public String setAlarmLight(JSONObject jsonObject) {
        String type=jsonObject.getString("type");
        String data="";
        try {
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            Login_V40(HCNetIPAddress, HCNetPort, jsonObject.getString("UserName"), jsonObject.getString("Password"));

            if(type.equals("0")){
                hCNetSDK.NET_DVR_PTZControl_Other(lUserID,HCNetlDChannel,LIGHT_PWRON,0);
                System.out.println(hCNetSDK.NET_DVR_GetLastError());
                data="开启灯光";
            }
            else {
                hCNetSDK.NET_DVR_PTZControl_Other(lUserID,HCNetlDChannel,LIGHT_PWRON,1);
                System.out.println(hCNetSDK.NET_DVR_GetLastError());
                data="关闭灯光";
            }

            return data;
        } catch (Exception e) {
            e.printStackTrace();
            return "开启失败";
        } finally {
            //用户注销，释放SDK
            return "用户退出";
        }
    }

    @Override
    public String getToolPoint(JSONObject jsonObject) {
        String data="";
        try {
            boolean initSuc = hCNetSDK.NET_DVR_Init();
            if (initSuc != true) {
                System.out.println("初始化失败");
            }
            // 打印SDK日志
            hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
            // 用户登陆操作
            Login_V40(HCNetIPAddress, HCNetPort, jsonObject.getString("UserName"), jsonObject.getString("Password"));


            HCNetSDK.NET_DVR_PTZPOS struPtTZPos = new HCNetSDK.NET_DVR_PTZPOS();
            IntByReference pUsers = new IntByReference(1);
            struPtTZPos.write();
            boolean b_GetPTZ = hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_PTZPOS, HCNetlDChannel, struPtTZPos.getPointer(), struPtTZPos.size(), pUsers);
            if (b_GetPTZ == false) {
                System.out.println("获取PTZ坐标信息失败，错误码：" + hCNetSDK.NET_DVR_GetLastError());
            } else {
                struPtTZPos.read();
                int wPanPos = Integer.parseInt(Integer.toHexString(struPtTZPos.wPanPos).trim());
                float WPanPos = wPanPos * 0.1f;
                int wTiltPos = Integer.parseInt(Integer.toHexString(struPtTZPos.wTiltPos).trim());
                float WTiltPos = wTiltPos * 0.1f;
                int wZoomPos = Integer.parseInt(Integer.toHexString(struPtTZPos.wZoomPos).trim());
                float WZoomPos = wZoomPos * 0.1f;
                System.out.println("球机坐标：" + wPanPos +","+wTiltPos+","+wZoomPos+ "\n");
            }
            System.out.println(hCNetSDK.NET_DVR_GetLastError());
        } catch (Exception e) {
            e.printStackTrace();
            return "开启失败";
        } finally {
            //用户注销，释放SDK
            return "用户退出";
        }

    }




    /**
     * 功能描述：获取xml元素值
     *
     * @param element
     * @param elementIteratorName
     * @return
     */
    public static String getElementText(Element element, String elementIteratorName) {
        Iterator iterator = element.elementIterator(elementIteratorName);
        if (iterator.hasNext()) {
            Element subElement = (Element) iterator.next();
            return subElement.getText();
        }
        return null;
    }


}
