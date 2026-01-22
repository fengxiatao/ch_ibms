package com.jokeep.business;

import com.jokeep.business.serviceImpl.HCNetSDK;

/**
 * @author test
 * @create 2021-02-07-15:53
 */
public class jna_test {
    // 接口的实例，通过接口实例调用外部dll/so的函数
    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;

    // 用户登录返回句柄
    static int lUserID;
    int iErr = 0;

    public static void main(String[] args) throws InterruptedException {
        jna_test test01 = new jna_test();
        // 初始化
        boolean initSuc = hCNetSDK.NET_DVR_Init();
        if (initSuc != true) {
            System.out.println("初始化失败");
        }
        // 打印SDK日志
        hCNetSDK.NET_DVR_SetLogToFile(3, ".\\SDKLog\\", false);
        // 用户登陆操作
        test01.Login_V40("192.168.60.103",(short)8000,"admin","jokeep2012");
        /*
        *实现SDK中其余功能模快
        */
        Thread.sleep(5000);
        //用户注销，释放SDK
        test01.Logout();    
    }
    /**
     *
     * @param m_sDeviceIP 设备ip地址
     * @param wPort       端口号，设备网络SDK登录默认端口8000
     * @param m_sUsername 用户名
     * @param m_sPassword 密码
     */
    public void Login_V40(String m_sDeviceIP,short wPort,String m_sUsername,String m_sPassword) {
        /* 注册 */
        // 设备登录信息
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();

        // 设备信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());
        m_strLoginInfo.wPort =wPort ;
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
        if (lUserID>=0)
        {
            if (hCNetSDK.NET_DVR_Logout(lUserID) == false) {
                System.out.println("注销失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            }
            System.out.println("注销成功");
            hCNetSDK.NET_DVR_Cleanup();
            return;
        }
        else{
            System.out.println("设备未登录");
            hCNetSDK.NET_DVR_Cleanup();
            return;
        }
    }
}

