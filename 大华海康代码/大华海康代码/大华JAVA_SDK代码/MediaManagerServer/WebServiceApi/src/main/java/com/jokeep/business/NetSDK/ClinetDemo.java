package com.jokeep.business.NetSDK;

import com.jokeep.business.common.osSelect;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import java.util.Timer;

/**
 * @create 2020-12-24-17:55
 */
public class ClinetDemo {

    int iErr = 0;
    static HCNetSDK hCNetSDK = null;
    static PlayCtrl playControl = null;
    static int lUserID = 0;//用户句柄
    static int lAlarmHandle = -1;//报警布防句柄
    static int lListenHandle = -1;//报警监听句柄

    static boolean bSaveHandle = false;
    Timer Playbacktimer;//回放用定时器

    static FExceptionCallBack_Imp fExceptionCallBack;
    static int FlowHandle;

    static class FExceptionCallBack_Imp implements HCNetSDK.FExceptionCallBack {
        public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {
            System.out.println("异常事件类型:"+dwType);
            return;
        }
    }

    public static void main(String[] args) throws InterruptedException {

        if (hCNetSDK == null&&playControl==null) {
            if (!CreateSDKInstance()) {
                System.out.println("Load SDK fail");
                return;
            }
            if (!CreatePlayInstance()) {
                System.out.println("Load PlayCtrl fail");
                return;
            }
        }
        //linux系统建议调用以下接口加载组件库
        if (osSelect.isLinux()) {
            HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
            HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
            //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
            String strPath1 = System.getProperty("user.dir") + "/lib/libcrypto.so.1.1";
            String strPath2 = System.getProperty("user.dir") + "/lib/libssl.so.1.1";

            System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
            ptrByteArray1.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

            System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
            ptrByteArray2.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

            String strPathCom = System.getProperty("user.dir") + "/lib";
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
        }

        //SDK初始化，一个程序只需要调用一次
        boolean initSuc = hCNetSDK.NET_DVR_Init();
        
        if (initSuc != true) {
            System.out.println("初始化失败");
        }
        
        //异常消息回调
        if(fExceptionCallBack == null)
        {
            fExceptionCallBack = new FExceptionCallBack_Imp();
        }
        
        Pointer pUser = null;
        if (!hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, fExceptionCallBack, pUser)) {
            return ;
        }
        System.out.println("设置异常消息回调成功");

        //启动SDK写日志
        hCNetSDK.NET_DVR_SetLogToFile(3, "..\\sdkLog\\", false);

        //登录设备，每一台设备分别登录; 登录句柄是唯一的，可以区分设备
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        String m_sDeviceIP = "192.168.60.105";//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = "admin";//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = "jokeep2012";//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = 8000; //SDK端口
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.write();

        lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        if (lUserID == -1) {
            System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
        } else {
            System.out.println(m_sDeviceIP + ":设备登录成功! " + "设备序列号:" + new String(m_strDeviceInfo.struDeviceV30.sSerialNumber).trim());
            m_strDeviceInfo.read();
        }
        
        int lChannel = 2;
        //byStartDChan为IP通道起始通道号, 预览回放NVR的IP通道时需要根据起始通道号进行取值
        int lDChannel = (int)m_strDeviceInfo.struDeviceV30.byStartDChan + lChannel -1;
        System.out.println("预览通道号: " + lDChannel);
        
        //注释掉的代码也可以参考，去掉注释可以运行
        //VideoDemo.getIPChannelInfo(lUserID); //获取IP通道             
        
        VideoDemo.RealPlay(lUserID, lDChannel);   //预览

        //按时间回放和下载
        new VideoDemo().PlayBackBytime(lUserID, lDChannel);
        new VideoDemo().DowmloadRecordByTime(lUserID, lDChannel); //按时间下载录像
        
        //按时间回放和下载录像，需要等待回放和下载完成后调用注销和释放接口

        //按录像文件回放和下载
        VideoDemo.DownloadRecordByFile(lUserID, lDChannel);//录像文件查找下载
        VideoDemo.playBackByfile(lUserID, lDChannel);
        Thread.sleep(3000);

        //退出程序时调用，每一台设备分别注销
        if (hCNetSDK.NET_DVR_Logout(lUserID)) {
            System.out.println("注销成功");
        }
        
        //SDK反初始化，释放资源，只需要退出时调用一次
        hCNetSDK.NET_DVR_Cleanup();
        return;
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
                    playControl=(PlayCtrl) Native.loadLibrary(strPlayPath,PlayCtrl.class);

                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strPlayPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }
}



