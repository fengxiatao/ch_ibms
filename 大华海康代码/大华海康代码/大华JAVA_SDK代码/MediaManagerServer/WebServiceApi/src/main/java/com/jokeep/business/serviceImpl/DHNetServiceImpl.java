package com.jokeep.business.serviceImpl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jokeep.business.DHSDK.NativeString;
import com.jokeep.business.DHSDK.NetSDKLib;
import com.jokeep.business.DHSDK.ToolKits;
import com.jokeep.business.DHSDK.common.Res;
import com.jokeep.business.DHSDK.demo.module.LoginModule;
import com.jokeep.business.DHSDK.structure.NET_IN_PRE_UPLOAD_REMOTE_FILE;
import com.jokeep.business.DHSDK.structure.NET_IN_PTZBASE_MOVEABSOLUTELY_INFO;
import com.jokeep.business.DHSDK.structure.NET_OUT_PRE_UPLOAD_REMOTE_FILE;
import com.jokeep.business.DHSDK.structure.NET_REMOTE_FILE_INFO;
import com.jokeep.business.service.DHNetService;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.ByReference;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import lombok.var;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.io.*;
import java.util.*;

import static com.jokeep.business.DHSDK.NetSDKLib.*;
import static com.jokeep.business.DHSDK.NetSDKLib.CtrlType.*;
import static com.jokeep.business.DHSDK.NetSDKLib.NET_PTZ_ControlType.NET_PTZ_POINT_SET_CONTROL;
import static com.jokeep.business.DHSDK.ToolKits.getErrorCodePrint;
import static com.jokeep.business.DHSDK.demo.module.TalkModule.startTalk;
import static com.jokeep.business.DHSDK.demo.module.TalkModule.stopTalk;

@Service
public class DHNetServiceImpl  implements DHNetService {
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

    @Override
    public void ChangeNormalControle(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, int nChannelID, String command, int lParam1, int lParam2, String type, int yzdID) {
        LoginModule.init(disConnect, haveReConnect);
//        // 若未登录，先登录
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }

        if(LoginModule.m_hLoginHandle.longValue() != 0){
            if ("UP".equals(command)) {
                //向上
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_UP_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_UP_CONTROL,
                        0, 0, 0, 1);
            } else if ("DOWN".equals(command)) {
                //向下
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_DOWN_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_DOWN_CONTROL,
                        0, 0, 0, 1);
            } else if ("LEFT".equals(command)) {
                //向左
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_LEFT_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_LEFT_CONTROL,
                        0, 0, 0, 1);
            } else if ("RIGHT".equals(command)) {
                //向右
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_RIGHT_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_RIGHT_CONTROL,
                        0, 0, 0, 1);
            } else if ("LEFT_UP".equals(command)) {
                //向左上
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTTOP,
                        lParam1, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTTOP,
                        0, 0, 0, 1);
            } else if ("LEFT_DOWN".equals(command)) {
                //向左下
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTDOWN,
                        lParam1, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_LEFTDOWN,
                        0, 0, 0, 1);
            } else if ("RIGHT_UP".equals(command)) {
                //向右上
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTTOP,
                        lParam1, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTTOP,
                        0, 0, 0, 1);
            } else if ("RIGHT_DOWN".equals(command)) {
                //向右下
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTDOWN,
                        lParam1, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_EXTPTZ_ControlType.NET_EXTPTZ_RIGHTDOWN,
                        0, 0, 0, 1);
            } else if ("FOCUS_NEAR".equals(command)) {
                //变焦+
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_ADD_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_ADD_CONTROL,
                        0, 0, 0, 1);
            } else if ("FOCUS_FAR".equals(command)) {
                //变焦-
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_DEC_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_FOCUS_DEC_CONTROL,
                        0, 0, 0, 1);
            } else if ("ZOOM_IN".equals(command)) {
                //变倍+
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_ADD_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_ADD_CONTROL,
                        0, 0, 0, 1);
            } else if ("ZOOM_OUT".equals(command)) {
                //变倍-
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_DEC_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_ZOOM_DEC_CONTROL,
                        0, 0, 0, 1);
            } else if ("IRIS_ENLARGE".equals(command)) {
                //光圈+
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_ADD_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_ADD_CONTROL,
                        0, 0, 0, 1);
            } else if ("IRIS_REDUCE".equals(command)) {
                //光圈-
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_DEC_CONTROL,
                        0, lParam2, 0, 0);
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,
                        NetSDKLib.NET_PTZ_ControlType.NET_PTZ_APERTURE_DEC_CONTROL,
                        0, 0, 0, 1);
            }

        }
    }

    @Override
    public void ChangeYZDControle(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, int nChannelID, String command, int lParam1, int lParam2, String type, int yzdID) {
        LoginModule.init(disConnect, haveReConnect);
//        // 若未登录，先登录
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }

        if(LoginModule.m_hLoginHandle.longValue() != 0){
            //设置预置点
            if("YZD_SET".equals(command)){
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,  NET_PTZ_POINT_SET_CONTROL, 0, yzdID, 0,0);
            }
            //删除预置点
            else if("YZD_DEL".equals(command)){
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,  NetSDKLib.NET_PTZ_ControlType.NET_PTZ_POINT_DEL_CONTROL, 0, yzdID, 0,0);
            }
            //移动预置点
            else if("YZD_MOVE".equals(command)){
                netsdk.CLIENT_DHPTZControlEx(LoginModule.m_hLoginHandle, nChannelID,  NetSDKLib.NET_PTZ_ControlType.NET_PTZ_POINT_MOVE_CONTROL, 0, yzdID, 0,0);
            }
        }
//        // 退出
//        LoginModule.logout();
//        // 释放资源
//        LoginModule.cleanup();
    }


    /**
     * 修改预置点名称
     * @param m_strIp
     * @param m_nPort
     * @param nChannelID
     * @param m_strUser
     * @param m_strPassword
     * @param name
     * @return
     */
    @Override
    public String UpdateYZDName(String m_strIp, int m_nPort, int nChannelID, String m_strUser, String m_strPassword, String name,String yzdIDs) {
        LoginModule.init(disConnect, haveReConnect);
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }
        if(LoginModule.m_hLoginHandle.longValue() != 0){
            int yzdId=Integer.parseInt(yzdIDs);
            NET_IN_PTZBASE_MOVEABSOLUTELY_INFO putPoints =new NET_IN_PTZBASE_MOVEABSOLUTELY_INFO();
            try {
//                byte[] midbytes = name.getBytes("GBK");
//                NativeString nativeString=new NativeString(name);
//                new NativeString(names).getPointer()
//                putPoints.byReserved=midbytes;
                var result=false;
                result=netsdk.CLIENT_DHPTZControlEx2(LoginModule.m_hLoginHandle, nChannelID,NET_PTZ_POINT_SET_CONTROL,0,yzdId,0,0,ToolKits.GetGBKStringToPointer(name));
                if(!result){
                    System.out.println(getErrorCodePrint());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            }
        return "修改成功";
    }

    @Override
    public List<Map<String, Object>> getYZDList(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) {
        List<Map<String,Object>>listYZD=new ArrayList<>();
        LoginModule.init(disConnect, haveReConnect);
//        // 若未登录，先登录
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }

        if(LoginModule.m_hLoginHandle.longValue() != 0){
            IntByReference error = new IntByReference(0);
            int nBufferLen = 100*1024;
            byte[] strBuffer = new byte[nBufferLen];

            var result=netsdk.CLIENT_GetNewDevConfig(LoginModule.m_hLoginHandle,NetSDKLib.CFG_CMD_PTZ_PRESET,-1,strBuffer,nBufferLen,error, 3000);
            if (!result)
            {
                System.out.println(getErrorCodePrint());
                return null;
            }else {
                String s = new String(strBuffer);
//                System.out.println(s);
                JSONObject jsonObject= JSON.parseObject(s);
//                System.out.println(jsonObject);
                JSONObject Json=jsonObject.getJSONObject("params");
                List<Map<String,Object>> list= (List<Map<String, Object>>) Json.get("table");
                List<Map<String,Object>>listResult= (List<Map<String, Object>>) list.get(0);
//                System.out.println(listResult);
                listResult.removeIf(Objects::isNull);
                for (int i = 0; i <listResult.size() ; i++) {
                    if(null!=listResult.get(i).get("Enable")){
                        if("true" == String.valueOf(listResult.get(i).get("Enable"))&&!"".equals(listResult.get(i).get("Name"))){
                            listResult.get(i).put("YZDID",i+1);
                            listYZD.add(listResult.get(i));
                        }
                    }
                }
                System.out.println(listYZD);
            }
        }

        return listYZD;
    }

    @Override
    public String openOrCloseSpeek(String m_strIp, int m_nPort, String m_strUser, String m_strPassword, String type) {
        LoginModule.init(disConnect, haveReConnect);
        String data="";
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }

        if(LoginModule.m_hLoginHandle.longValue() != 0){
            if(type.equals("0")){
                startTalk(0,-1);
                data="开启通话";
            }
            else {
                stopTalk();
                data="关闭通话";
            }
        }
        return data;
    }

@Override
public String setAlarmLight(String m_strIp, int m_nPort, int nChannelID,String m_strUser, String m_strPassword, String type) {
    LoginModule.init(disConnect, haveReConnect);
    String data="";
    if (LoginModule.m_hLoginHandle.longValue() == 0) {
        LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
    }

    if(LoginModule.m_hLoginHandle.longValue() != 0){
        var result=true;
        if(type.equals("0")){
            NET_COAXIAL_CONTROL_IO_INFO[] stp=new NET_COAXIAL_CONTROL_IO_INFO[2];

            NET_COAXIAL_CONTROL_IO_INFO net_coaxial_control_io_info=new NET_COAXIAL_CONTROL_IO_INFO();
            net_coaxial_control_io_info.emType=1;
            net_coaxial_control_io_info.emSwicth=1;
            net_coaxial_control_io_info.emMode=2;
            stp[0]=net_coaxial_control_io_info;

            NET_IN_CONTROL_COAXIAL_CONTROL_IO pInBuf=new NET_IN_CONTROL_COAXIAL_CONTROL_IO();
            pInBuf.nChannel=nChannelID;
            pInBuf.nInfoCount=1;
            pInBuf.stInfo=stp;


            NET_OUT_CONTROL_COAXIAL_CONTROL_IO pOutBuf=new NET_OUT_CONTROL_COAXIAL_CONTROL_IO();
            pInBuf.write();
            pOutBuf.write();
            result=netsdk.CLIENT_ControlDeviceEx(LoginModule.m_hLoginHandle,CTRLTYPE_CTRL_COAXIAL_CONTROL_IO,pInBuf.getPointer(),pOutBuf.getPointer(),3000);
        }
        else {
            NET_COAXIAL_CONTROL_IO_INFO[] stp=new NET_COAXIAL_CONTROL_IO_INFO[2];

            NET_COAXIAL_CONTROL_IO_INFO net_coaxial_control_io_info=new NET_COAXIAL_CONTROL_IO_INFO();
            net_coaxial_control_io_info.emType=1;
            net_coaxial_control_io_info.emSwicth=2;
            net_coaxial_control_io_info.emMode=2;
            stp[0]=net_coaxial_control_io_info;

            NET_IN_CONTROL_COAXIAL_CONTROL_IO pInBuf=new NET_IN_CONTROL_COAXIAL_CONTROL_IO();
            pInBuf.nChannel=nChannelID;
            pInBuf.nInfoCount=1;
            pInBuf.stInfo=stp;

            NET_OUT_CONTROL_COAXIAL_CONTROL_IO pOutBuf=new NET_OUT_CONTROL_COAXIAL_CONTROL_IO();

            pInBuf.write();
            pOutBuf.write();
            result=netsdk.CLIENT_ControlDeviceEx(LoginModule.m_hLoginHandle,CTRLTYPE_CTRL_COAXIAL_CONTROL_IO,pInBuf.getPointer(),pOutBuf.getPointer(),3000);
        }

        if (!result)
        {
            System.out.println(getErrorCodePrint());
        }else {
            data="设置成功";
        }
    }
    return data;
}

    @Override
    public String getToolPoint(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) {
        LoginModule.init(disConnect, haveReConnect);
        String data="";
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }

        if(LoginModule.m_hLoginHandle.longValue() != 0){
            var result=true;
            NetSDKLib.NET_PTZ_LOCATION_INFO net_ptz_location_info=new NetSDKLib.NET_PTZ_LOCATION_INFO();
            net_ptz_location_info.nChannelID=0;
            IntByReference intRetLen = new IntByReference(0);
            Pointer pointer=net_ptz_location_info.getPointer();
            net_ptz_location_info.write();
            result=netsdk.CLIENT_QueryDevState(LoginModule.m_hLoginHandle,NET_DEVSTATE_PTZ_LOCATION,pointer,net_ptz_location_info.size(),intRetLen,50000);
            net_ptz_location_info.read();

//            System.out.println(net_ptz_location_info);
            System.out.println("聚焦点位："+net_ptz_location_info.fFocusPosition);
            System.out.println("position:"+net_ptz_location_info.nPTZPan+","+net_ptz_location_info.nPTZTilt+","+net_ptz_location_info.nPTZZoom);
            System.out.println("positionNew:"+net_ptz_location_info.stuAbsPosition.nPosX+","+net_ptz_location_info.stuAbsPosition.nPosY+","+net_ptz_location_info.stuAbsPosition.nZoom);
            System.out.println("预置点编号:"+net_ptz_location_info.dwPresetID);
            if (!result)
            {
                System.out.println(getErrorCodePrint());
            }else {
                data="获取数据成功";
            }
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> getFileList(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) throws UnsupportedEncodingException {
        LoginModule.init(disConnect, haveReConnect);
        List<Map<String,Object>>list=new ArrayList<>();
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }
        if(LoginModule.m_hLoginHandle.longValue() != 0){
            List list1=new ArrayList();
            list1.add("/mnt/mtd/audiofiles/");
            list1.add("/usr/data/audiofiles/");
            for (int i = 0; i <list1.size() ; i++) {
                NET_IN_LIST_REMOTE_FILE stIn = new NET_IN_LIST_REMOTE_FILE();
                stIn.pszPath = String.valueOf(list1.get(i));
                stIn.bFileNameOnly = 1;  // 只获取文件名称, 不返回文件夹信息, 文件信息中只有文件名有效
                stIn.emCondition = NET_REMOTE_FILE_COND.NET_REMOTE_FILE_COND_NONE;
                NET_OUT_LIST_REMOTE_FILE stOut=new NET_OUT_LIST_REMOTE_FILE();//输出结构体
                int maxFileCount = 50;  // 每次查询的文件个数
                SDK_REMOTE_FILE_INFO[] remoteFileArr;
                while (true) {
                    remoteFileArr = new SDK_REMOTE_FILE_INFO[maxFileCount];
                    for (int k = 0; k < maxFileCount; k++) {
                        remoteFileArr[k] = new SDK_REMOTE_FILE_INFO();
                    }

                    stOut.nMaxFileCount = maxFileCount;
                    stOut.pstuFiles = new Memory(remoteFileArr[0].size() * maxFileCount);   // Pointer初始化
                    stOut.pstuFiles.clear(remoteFileArr[0].size() * maxFileCount);

                    ToolKits.SetStructArrToPointerData(remoteFileArr, stOut.pstuFiles);    // 将数组内存拷贝给Pointer

                    if (netsdk.CLIENT_ListRemoteFile(LoginModule.m_hLoginHandle, stIn, stOut, 3000)) {
                        if (maxFileCount > stOut.nRetFileCount) {
                            ToolKits.GetPointerDataToStructArr(stOut.pstuFiles, remoteFileArr);    // 将Pointer的信息输出到结构体
                            break;
                        } else {
                            maxFileCount += 50;
                        }
                    } else {
                        return null;
                    }
                }
                System.out.println(remoteFileArr);
                for (int j = 0; j <remoteFileArr.length ; j++) {
                    String path=new String(remoteFileArr[j].szPath,"GBK");
                    if(!"".equals(path.trim())&&(path.trim().contains(".wav")||path.trim().contains(".pcm")||path.trim().contains(".aac")) ){
                        System.out.println(path);
                        Map<String,Object>map=new HashMap<>();
                        String newPath=path.replaceAll("[\u0000]", "");
                        map.put("FileName",newPath.substring(newPath.lastIndexOf("/")+1));
                        map.put("FilePath",newPath);
                        list.add(map);
                    }
                }

            }
        }
        return list;
    }


//    @Override
//    public String StartSpeekData(String m_strIp, int m_nPort, int nChannelID, String m_strUser, String m_strPassword,String type,String F_FileUrl) {
//        LoginModule.init(disConnect, haveReConnect);
//        String data="";
//        if (LoginModule.m_hLoginHandle.longValue() == 0) {
//            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
//        }
//        NET_CTRL_START_PLAYAUDIO net_ctrl_start_playaudio=new NET_CTRL_START_PLAYAUDIO();
//        String Path=F_FileUrl;
//        byte[] midbytes= new byte[0];
//        midbytes = Path.getBytes();
//        net_ctrl_start_playaudio.szAudioPath=midbytes;
//            net_ctrl_start_playaudio.write();
//        if(LoginModule.m_hLoginHandle.longValue() != 0){
//            var result=true;
//            if(type.equals("0")){
//                result=netsdk.CLIENT_ControlDevice(LoginModule.m_hLoginHandle,CTRLTYPE_CTRL_START_PLAYAUDIO,net_ctrl_start_playaudio.getPointer(),3000);
//            }
//            else {
//                result=netsdk.CLIENT_ControlDevice(LoginModule.m_hLoginHandle,CTRLTYPE_CTRL_STOP_PLAYAUDIO,net_ctrl_start_playaudio.getPointer(),3000);
//            }
//            if (!result)
//            {
//                System.out.println(getErrorCodePrint());
//            }else {
//                net_ctrl_start_playaudio.read();
//                data="获取数据成功";
//            }
//        }
//        return data;
//    }



    /**
     * 上传音频文件
     * @param m_strIp
     * @param m_nPort
     * @param nChannelID
     * @param m_strUser
     * @param m_strPassword
     * @return
     */
    @Override
    public String StartSpeekData(String m_strIp, int m_nPort, int nChannelID, String m_strUser, String m_strPassword,String type,String F_FileUrl) {
        LoginModule.init(disConnect, haveReConnect);
        String data="";
        if (LoginModule.m_hLoginHandle.longValue() == 0) {
            LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
        }

        if(LoginModule.m_hLoginHandle.longValue() != 0){
            var result=true;
            var results=true;
            var resultes=true;
            int nBufferLen =100*1024;
            IntByReference error=new IntByReference(0);
            IntByReference restart=new IntByReference(0);
            byte[] bytes = new byte[nBufferLen];
             result=netsdk.CLIENT_QueryNewSystemInfo(LoginModule.m_hLoginHandle,CFG_CAP_CMD_SPEAK,nChannelID,bytes,nBufferLen,error,3000);//获取文件路径

            String s=new String(bytes);
            System.out.println(s);
            JSONObject jsonObject=JSONObject.parseObject(s);
            JSONArray pszFileDst=jsonObject.getJSONObject("params").getJSONObject("caps").getJSONArray("AudioPlayPath");
            JSONObject jsonObject1= (JSONObject) pszFileDst.get(1);//上传
            String UploadPath=jsonObject1.getString("Path");

            JSONObject jsonObject2= (JSONObject) pszFileDst.get(0);//预上传
            String UploadPath2=jsonObject2.getString("Path");

            NET_IN_PRE_UPLOAD_REMOTE_FILE net_in_upload_remote_file=new NET_IN_PRE_UPLOAD_REMOTE_FILE();//文件预上传输入结构体
            String FilePath="D:/SVN/Speak.wav";
            net_in_upload_remote_file.pszFileSrc= FilePath;//System.getProperty("user.dir")+ "\\AudioFile\\Speak.wav";
            net_in_upload_remote_file.pszFileDst="/mnt/mtd/audiofiles/Speak.wav";

            NET_OUT_PRE_UPLOAD_REMOTE_FILE net_out_pre_upload_remote_file =new NET_OUT_PRE_UPLOAD_REMOTE_FILE();
            net_out_pre_upload_remote_file.bContinue2Upload=true;

            results= netsdk.CLIENT_PreUploadRemoteFile(LoginModule.m_hLoginHandle,net_in_upload_remote_file,net_out_pre_upload_remote_file,0);//预上传


            NET_IN_UPLOAD_REMOTE_FILE net_in_upload_remote_file1=new NET_IN_UPLOAD_REMOTE_FILE();//文件输入结构体
            net_in_upload_remote_file1.pszFileSrc=ToolKits.GetGBKStringToPointer(FilePath);
            net_in_upload_remote_file1.pszFileDst=ToolKits.GetGBKStringToPointer("/mnt/mtd/audiofiles/Speak.wav");
            net_in_upload_remote_file1.nPacketLen=0;
            net_in_upload_remote_file1.pszFolderDst=ToolKits.GetGBKStringToPointer("/mnt/mtd/audiofiles");
            NET_OUT_UPLOAD_REMOTE_FILE net_out_upload_remote_file=new NET_OUT_UPLOAD_REMOTE_FILE();//文件输出结构体

            net_in_upload_remote_file1.write();
            resultes=netsdk.CLIENT_UploadRemoteFile(LoginModule.m_hLoginHandle,net_in_upload_remote_file1,net_out_upload_remote_file,0);

            if(!resultes){
                System.out.println(getErrorCodePrint());
            }

            if (!results){
                System.out.println(getErrorCodePrint());
            }

            if (!result)
            {
                System.out.println(getErrorCodePrint());
            }else {
                data="获取数据成功";
            }
            LOG_SET_PRINT_INFO log_set_print_info=new LOG_SET_PRINT_INFO();
            log_set_print_info.bSetFilePath=0;
            var resultess=netsdk.CLIENT_LogOpen(log_set_print_info);
            if(!resultess){
                System.out.println(getErrorCodePrint());
            }
        }
        return data;
    }


    public static byte[] getBytesFromFile(File file) {
        if (file == null) {
            return null;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = stream.read(b)) != -1) {
                out.write(b, 0, n);
            }
            stream.close();
            out.close();
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }




    /**
     * 查询录像文件列表（使用大华SDK CLIENT_QueryRecordFile）
     */
    @Override
    public List<Map<String, Object>> queryRecordFiles(String m_strIp, int m_nPort, String m_strUser, 
                                                       String m_strPassword, int nChannelID, 
                                                       String startTime, String endTime) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            // 初始化SDK
            LoginModule.init(disConnect, haveReConnect);
            
            // 登录设备
            if (LoginModule.m_hLoginHandle.longValue() == 0) {
                LoginModule.login(m_strIp, m_nPort, m_strUser, m_strPassword);
            }
            
            if (LoginModule.m_hLoginHandle.longValue() == 0) {
                System.err.println("[录像查询] 设备登录失败");
                return result;
            }
            
            // 转换时间格式
            NetSDKLib.NET_TIME stTimeStart = convertStringToNetTime(startTime);
            NetSDKLib.NET_TIME stTimeEnd = convertStringToNetTime(endTime);
            
            // 创建录像文件信息数组（最多查询2000条）
            int maxRecordCount = 2000;
            NetSDKLib.NET_RECORDFILE_INFO[] stFileInfo = 
                (NetSDKLib.NET_RECORDFILE_INFO[]) new NetSDKLib.NET_RECORDFILE_INFO().toArray(maxRecordCount);
            IntByReference nFindCount = new IntByReference(0);
            
            // 调用大华SDK查询录像
            // RecordFileType: 0-所有录像, 1-外部报警, 2-动态监测报警
            int nRecordFileType = 0;
            boolean bRet = netsdk.CLIENT_QueryRecordFile(
                LoginModule.m_hLoginHandle,
                nChannelID,
                nRecordFileType,
                stTimeStart,
                stTimeEnd,
                null,
                stFileInfo,
                maxRecordCount * stFileInfo[0].size(),
                nFindCount,
                5000,  // 超时时间5秒
                false
            );
            
            if (bRet) {
                int count = nFindCount.getValue();
                System.out.println("[录像查询] 查询成功，找到 " + count + " 个录像文件");
                
                // 转换为Map格式返回
                for (int i = 0; i < count; i++) {
                    NetSDKLib.NET_RECORDFILE_INFO fileInfo = stFileInfo[i];
                    
                    Map<String, Object> record = new HashMap<>();
                    record.put("channelId", fileInfo.ch);
                    record.put("fileName", new String(fileInfo.filename).trim());
                    record.put("fileSize", fileInfo.size);
                    record.put("startTime", formatNetTime(fileInfo.starttime));
                    record.put("endTime", formatNetTime(fileInfo.endtime));
                    record.put("recordType", fileInfo.driveno); // 录像类型
                    record.put("diskNo", fileInfo.diskno);      // 磁盘号
                    
                    result.add(record);
                }
            } else {
                System.err.println("[录像查询] 查询失败: " + ToolKits.getErrorCodePrint());
            }
            
        } catch (Exception e) {
            System.err.println("[录像查询] 异常: " + e.getMessage());
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 将字符串时间转换为 NET_TIME 格构
     * @param timeStr 格式: yyyy-MM-dd HH:mm:ss
     */
    private NetSDKLib.NET_TIME convertStringToNetTime(String timeStr) {
        NetSDKLib.NET_TIME netTime = new NetSDKLib.NET_TIME();
        try {
            String[] parts = timeStr.split(" ");
            String[] dateParts = parts[0].split("-");
            String[] timeParts = parts[1].split(":");
            
            netTime.dwYear = Integer.parseInt(dateParts[0]);
            netTime.dwMonth = Integer.parseInt(dateParts[1]);
            netTime.dwDay = Integer.parseInt(dateParts[2]);
            netTime.dwHour = Integer.parseInt(timeParts[0]);
            netTime.dwMinute = Integer.parseInt(timeParts[1]);
            netTime.dwSecond = Integer.parseInt(timeParts[2]);
        } catch (Exception e) {
            System.err.println("[时间转换] 失败: " + e.getMessage());
        }
        return netTime;
    }
    
    /**
     * 将 NET_TIME 格式转换为字符串
     * @param netTime NET_TIME对象
     * @return yyyy-MM-dd HH:mm:ss 格式的字符串
     */
    private String formatNetTime(NetSDKLib.NET_TIME netTime) {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d",
            netTime.dwYear, netTime.dwMonth, netTime.dwDay,
            netTime.dwHour, netTime.dwMinute, netTime.dwSecond);
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
