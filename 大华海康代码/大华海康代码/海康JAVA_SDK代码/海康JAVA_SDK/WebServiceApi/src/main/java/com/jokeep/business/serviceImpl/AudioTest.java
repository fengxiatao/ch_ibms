package com.jokeep.business.serviceImpl;

import com.jokeep.business.common.osSelect;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.io.*;
import java.nio.ByteBuffer;

import static com.jokeep.business.serviceImpl.HCNetServiceImpl.hCNetSDK;

public class AudioTest {
    static int lUserID = -1;
//    static HCNetSDK hCNetSDK = null;
    static int lVoiceComHandle = -1; //语音对讲句柄
    static int lVoiceHandle = -1; //语音转发句柄
    static File Recvfile = null;
    static File fileDecodePcm = null;
    static File fileEncode = null;
    static FileOutputStream outputStream = null;
    static FileOutputStream outputStreamPcm = null;
    static FileOutputStream outputStreamG711 = null;
    
    static Pointer pDecHandle = null;
    static cbVoiceDataCallBack_MR_V30 cbVoiceDataCallBack = null;
    static VoiceDataCallBack voiceDatacallback = null;


    public static  void StartSpeek(int lUserID){
        //设置语音对讲回调函数
        if (voiceDatacallback==null)
        {
            voiceDatacallback=new VoiceDataCallBack();
        }
    
        //开启语音对讲
		AudioTest.StartVoiceCom(lUserID);
    }

    public static  void StopSpees(int lUserID){

        AudioTest.StopVoiceCom(lUserID);
        //退出程序时调用，每一台设备分别注销
        if (hCNetSDK.NET_DVR_Logout(lUserID)) {
            System.out.println("注销成功");
        }

        //SDK反初始化，释放资源，只需要退出时调用一次
        hCNetSDK.NET_DVR_Cleanup();
    }


    static class VoiceDataCallBack implements HCNetSDK.FVoiceDataCallBack_V30 {
        public void invoke(int lVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, byte byAudioFlag,  Pointer pUser) {
            //回调函数里保存语音对讲中双方通话语音数据
            System.out.println("语音对讲数据回调....");
        }
    }

    static class cbVoiceDataCallBack_MR_V30 implements HCNetSDK.FVoiceDataCallBack_MR_V30 {
        public void invoke(int lVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, byte byAudioFlag, Pointer pUser) {
            System.out.println("-----=cbVoiceDataCallBack_MR_V30 enter---------");
            if (byAudioFlag == 1) {
                System.out.println("设备发过来的语音");
                
                //设备发送过来的语音数据（建议另建线程或者消息事件方式将数据拷贝到回调函数外面处理，避免阻塞回调）
                try {
                    //将设备发送过来的语音数据写入文件
                	
                    //第一次先创建文件和句柄
                	if(outputStream == null)
                	{
                		Recvfile = new File(System.getProperty("user.dir") + "\\AudioFile\\DeviceSaveData.g7");
                        if (!Recvfile.exists()) {
                            try {
                            	Recvfile.createNewFile();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            outputStream = new FileOutputStream(Recvfile);
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                	}  
                	
                	//音频数据二进制写入文件
                	if(outputStream!= null)
                	{
                		long offset = 0;
                        ByteBuffer buffers = pRecvDataBuffer.getByteBuffer(offset, dwBufSize);
                        byte[] bytes = new byte[dwBufSize];
                        buffers.rewind();
                        buffers.get(bytes);
                        outputStream.write(bytes);
                	}                	
                    
                    
                    //将设备发送过来的G711数据解码成PCM数据
                    if (pDecHandle == null) {
                        pDecHandle = hCNetSDK.NET_DVR_InitG711Decoder();
                    }

                    HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM struAudioParam = new HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM();
                    struAudioParam.in_buf = pRecvDataBuffer;
                    struAudioParam.in_data_size = dwBufSize;

                    HCNetSDK.BYTE_ARRAY ptrVoiceData = new HCNetSDK.BYTE_ARRAY(320);
                    ptrVoiceData.write();

                    struAudioParam.out_buf = ptrVoiceData.getPointer();
                    struAudioParam.out_frame_size = 320;
                    struAudioParam.g711_type = 0; //G711编码类型：0- U law，1- A law
                    struAudioParam.write();

                    if (!hCNetSDK.NET_DVR_DecodeG711Frame(pDecHandle, struAudioParam)) {
                        System.out.println("NET_DVR_DecodeG711Frame failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                    }
                    struAudioParam.read();

                    //将解码之后PCM音频数据写入文件
                	if(fileDecodePcm == null)
                	{
                		fileDecodePcm = new File(System.getProperty("user.dir") + "\\AudioFile\\DecodeSaveData.pcm");  //保存回调函数的音频数据
                        if (!fileDecodePcm.exists()) {
                            try {
                            	fileDecodePcm.createNewFile();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        try {
                            outputStreamPcm = new FileOutputStream(fileDecodePcm);
                        } catch (FileNotFoundException e3) {
                            // TODO Auto-generated catch block
                            e3.printStackTrace();
                        }
                	}
                	if(fileDecodePcm != null)
                	{
                        long offsetPcm = 0;
                        ByteBuffer buffersPcm = struAudioParam.out_buf.getByteBuffer(offsetPcm, struAudioParam.out_frame_size);
                        byte[] bytesPcm = new byte[struAudioParam.out_frame_size];
                        buffersPcm.rewind();
                        buffersPcm.get(bytesPcm);
                        outputStreamPcm.write(bytesPcm);                		
                	}
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 开启语音对讲
     *
     * @param userID
     */
    public static boolean StartVoiceCom(int userID) {

        if (voiceDatacallback==null)
        {
            voiceDatacallback=new VoiceDataCallBack();
        }
        int voiceChannel = 1; //语音通道号。对于设备本身的语音对讲通道，从1开始；对于设备的IP通道，为登录返回的
        // 起始对讲通道号(byStartDTalkChan) + IP通道索引 - 1，例如客户端通过NVR跟其IP Channel02所接前端IPC进行对讲，则dwVoiceChan=byStartDTalkChan + 1
        boolean bret = true;  //需要回调的语音数据类型：0- 编码后的语音数据，1- 编码前的PCM原始数据
        lVoiceComHandle = hCNetSDK.NET_DVR_StartVoiceCom_V30(userID, voiceChannel, bret, voiceDatacallback, null);
        if (lVoiceComHandle <= -1) {
            System.out.println("语音对讲开启失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        System.out.println("语音对讲开始成功！");
        return true;

    }

    public static boolean StopVoiceCom(int userID) {
        if (!hCNetSDK.NET_DVR_StopVoiceCom(lVoiceComHandle)) {
            System.out.println("停止对讲失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }
        System.out.println("语音对讲停止成功！");
        return true;

    }


    /**
     * 开启语音转发
     * 设备音频编码格式G711u
     *
     * @return
     */
    public static void TestVoiceG711Trans(int userID) {
    	
        int voiceChannel = 1; //语音通道号。对于设备本身的语音对讲通道，从1开始；对于设备的IP通道，为登录返回的
        // 起始对讲通道号(byStartDTalkChan) + IP通道索引 - 1，例如客户端通过NVR跟其IP Channel02所接前端IPC进行对讲，则dwVoiceChan=byStartDTalkChan + 1
        
		//设置语音回调函数
        if (cbVoiceDataCallBack == null) {
            cbVoiceDataCallBack = new cbVoiceDataCallBack_MR_V30();
        }
		
        //开始语音转发
        lVoiceHandle = hCNetSDK.NET_DVR_StartVoiceCom_MR_V30(userID,voiceChannel, cbVoiceDataCallBack, null);
        if (lVoiceHandle == -1) {
            System.out.println("语音转发启动失败");
            return;
        }
        
        //以下代码是读取本地音频文件发送给设备
        //可以创建线程，在子线程里实现，这样可以不影响主线程其他功能操作
     
        //读取本地PCM文件中语音数据
        FileInputStream VoicePCMfile = null;    
        int dataLength = 0;
        try {
            //创建从文件读取数据的FileInputStream流
        	VoicePCMfile = new FileInputStream(new File(System.getProperty("user.dir") + "\\AudioFile\\DecodeSaveData.pcm"));
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }           
        try {

            //返回PCM文件的总字节数
            dataLength = VoicePCMfile.available();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        if (dataLength < 0) {
            System.out.println("input file dataSize < 0");
            return;
        }
        HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
        try {
        	VoicePCMfile.read(ptrVoiceByte.byValue);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        ptrVoiceByte.write();
        
        //保存PCM编码成G711后的音频编码数据
        fileEncode = new File(System.getProperty("user.dir") + "\\AudioFile\\EncodeData.g7");  
        if (!fileEncode.exists()) {
            try {
                fileEncode.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
        	//创建保存G711数据的文件
            outputStreamG711 = new FileOutputStream(fileEncode);
        } catch (FileNotFoundException e3) {
            // TODO Auto-generated catch block
            e3.printStackTrace();
        }        

        //PCM编码成G711
        int iEncodeSize = 0;
        HCNetSDK.NET_DVR_AUDIOENC_INFO enc_info = new HCNetSDK.NET_DVR_AUDIOENC_INFO();
        enc_info.write();
        Pointer encoder = hCNetSDK.NET_DVR_InitG711Encoder(enc_info.getPointer()); //创建G711编码库句柄
        
        while ((dataLength - iEncodeSize) > 640) {
            HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(640);
            System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, 640);
            ptrPcmData.write();

            HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
            ptrG711Data.write();

            HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM struEncParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
            struEncParam.in_buf = ptrPcmData.getPointer();
            struEncParam.out_buf = ptrG711Data.getPointer();
            struEncParam.out_frame_size = 320;
            struEncParam.g711_type = 0;//G711编码类型：0- U law，1- A law
            struEncParam.write();

            //每次读取640字节PCM数据，编程输出320字节G711数据
            if (!hCNetSDK.NET_DVR_EncodeG711Frame(encoder, struEncParam)) {
                System.out.println("NET_DVR_EncodeG711Frame failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                break;
            }
            struEncParam.read();
            ptrG711Data.read();

            //测试代码，将编码后发送给设备的语音数据保存到本地
            long offsetG711 = 0;
            ByteBuffer buffersG711 = struEncParam.out_buf.getByteBuffer(offsetG711, struEncParam.out_frame_size);
            byte[] bytesG711 = new byte[struEncParam.out_frame_size];
            buffersG711.rewind();
            buffersG711.get(bytesG711);
            try {
                outputStreamG711.write(bytesG711);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            
            iEncodeSize += 640;
            System.out.println("编码字节数：" + iEncodeSize);

            for (int i = 0; i < struEncParam.out_frame_size / 160; i++) {
                HCNetSDK.BYTE_ARRAY ptrG711Send = new HCNetSDK.BYTE_ARRAY(160);
                System.arraycopy(ptrG711Data.byValue, i * 160, ptrG711Send.byValue, 0, 160);
                ptrG711Send.write();
               
                //G711数据发送给设备，每次发送160字节，发送时间间隔20毫秒
                if (!hCNetSDK.NET_DVR_VoiceComSendData(lVoiceHandle, ptrG711Send.byValue, 160)) {
                    System.out.println("NET_DVR_VoiceComSendData failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                }

                //需要实时速率发送数据
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }
        
        //数据发送结束，关闭编码库资源
        hCNetSDK.NET_DVR_ReleaseG711Encoder(encoder);   
        
        //关闭文件资源
        if (VoicePCMfile != null) {
            try {
            	VoicePCMfile.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("VoicePCMfile.close");
        }
        
        if (outputStreamG711 != null) {
            try {
            	outputStreamG711.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("outputStreamG711.close");
        }     
        
        //停止语音对讲或者转发，释放资源
        if (lVoiceHandle > -1) {
            hCNetSDK.NET_DVR_StopVoiceCom(lVoiceHandle);
        }        
        
        //释放解码库资源
        if (pDecHandle != null) {
            hCNetSDK.NET_DVR_ReleaseG711Decoder(pDecHandle);
            System.out.println("NET_DVR_ReleaseG711Decoder");
        }
        
        //释放回调函数中读写文件的资源
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("outputStream.close");
        }   

        if (outputStreamPcm != null) {
            try {
                outputStreamPcm.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        return;
    }
    
    /**
     * 根据不同操作系统选择不同的库文件和库路径
     *
     * @return
     */
    private static boolean CreateSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    //System.setProperty("jna.debug_load", "true");
                    if (osSelect.isWindows())
                        //win系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll";

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
}




