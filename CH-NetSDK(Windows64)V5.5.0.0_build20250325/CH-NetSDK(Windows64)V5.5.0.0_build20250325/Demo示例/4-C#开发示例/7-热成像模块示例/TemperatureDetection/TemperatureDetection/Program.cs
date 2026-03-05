using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using System.Runtime.InteropServices;
using System.Threading;
using System.IO;

using COMMON_STRUCT;
using NVSSDK_INTERFACE;

namespace FacePicStreamDemo
{
    class Program
    {
        const uint CONST_INVALID_RECV_ID = 0xffffffff;
        private static int g_iLogonID = -1;
        private static uint g_uiRecvID = CONST_INVALID_RECV_ID;
        private static int g_iCount = 0;
        private static int g_iVcaStatus = 0;
        private const string g_strPicStreamDir = @".\PicStream";
        private static MAIN_NOTIFY_V4 MainNotify_V4 = null;
        private static NetPicPara g_tNetPicPara = new NetPicPara();


        private static void MyMAIN_NOTIFY_V4(UInt32 _ulLogonID, int _iWparam, IntPtr _iLParam, IntPtr _iUser)
        {
            int iMsgType = _iWparam & 0xffff;
            int iMsgValue = _iLParam.ToInt32();
            switch (iMsgType)
            {
                case SDKConstMsg.WCM_LOGON_NOTIFY:
                    {
                        Console.WriteLine("WCM_LOGON_NOTIFY!\n");
                        if (SDKConstMsg.LOGON_SUCCESS == iMsgValue)
                        {
                            Console.WriteLine("Logon Success!\n");
                            if (CONST_INVALID_RECV_ID == g_uiRecvID)
                            {
                                StartRecvPicture(); //Start receiving picture stream
                            }
                        }
                        else
                        {
                            Console.WriteLine("Logon Failed!\n");
                        }
                        break;
                    }
                case SDKConstMsg.WCM_VCA_SUSPEND:  //Intelligent analysis pause message
                    {
                        int iResult = 0;
                        VCASuspendResult tParam = new VCASuspendResult();
                        tParam.iBufSize = Marshal.SizeOf(tParam);
                        IntPtr ptParam = IntPtr.Zero;
                        ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
                        Marshal.StructureToPtr(tParam, ptParam, true);
                        NVSSDK.NetClient_GetDevConfig(g_iLogonID, NetClientTypes.VCA_SUSPEND, 0, ptParam, Marshal.SizeOf(tParam), ref iResult);
                        tParam = (VCASuspendResult)Marshal.PtrToStructure(ptParam, typeof(VCASuspendResult));
                        g_iVcaStatus = tParam.iResult;		//result
                        if (VCASuspendStatus.STATUS_PAUSE == tParam.iStatus)
                        {
                            if (VCASuspendStatus.RESULT_SUCCESS == tParam.iResult)
                            {
                                Console.WriteLine("[Notify_Main] pause vca success.\n");
                            }
                            else
                            {
                                Console.WriteLine("[Notify_Main] pause vca failed.\n");
                            }
                        }
                        break;
                    }
                default:
                    break;
            }
        }

        private static int MyNETPICSTREAM_NOTIFY(UInt32 _uiRecvID, int _lCommand, IntPtr _pvCallBackInfo, Int32 _BufLen, IntPtr _iUser)
        {
            if (null == _pvCallBackInfo)
            {
                return -1;
            }

            if (_uiRecvID != g_uiRecvID)
            {
                return -1;
            }

            if (g_iCount >= SDKTypes.MAX_SAVE_PCTURE_COUNT)
            {
                Console.WriteLine("save picture over 2000!\n");
                return -1;
            }

            if (SDKTypes.NET_PICSTREAM_CMD_FACE == _lCommand)
            {
                IntPtr ptVca = _pvCallBackInfo;
                FacePicStream tFacePicStream = (FacePicStream)Marshal.PtrToStructure(ptVca, typeof(FacePicStream));
                FileStream pfFullPic = null;
                PicData tFullPicData = (PicData)Marshal.PtrToStructure(tFacePicStream.tFullData, typeof(PicData));
                PicTime tTime = tFullPicData.tPicTime;
                DateTime tDataTime = new DateTime(int.Parse(DateTime.Now.Year.ToString()), int.Parse(DateTime.Now.Month.ToString()),
                    int.Parse(DateTime.Now.Day.ToString()), int.Parse(DateTime.Now.Hour.ToString()), int.Parse(DateTime.Now.Minute.ToString()), int.Parse(DateTime.Now.Second.ToString()), int.Parse(DateTime.Now.Millisecond.ToString()));
                try
                {
                    tDataTime = new DateTime((int)tTime.uiYear, (int)tTime.uiMonth, (int)tTime.uiDay,
                    (int)tTime.uiHour, (int)tTime.uiMinute, (int)tTime.uiSecondsr, (int)tTime.uiMilliseconds);
                }
                catch
                {
                    tTime.uiYear = uint.Parse(DateTime.Now.Year.ToString());
                    tTime.uiMonth = uint.Parse(DateTime.Now.Month.ToString());
                    tTime.uiDay = uint.Parse(DateTime.Now.Day.ToString());
                    tTime.uiHour = uint.Parse(DateTime.Now.Hour.ToString());
                    tTime.uiMinute = uint.Parse(DateTime.Now.Minute.ToString());
                    tTime.uiSecondsr = uint.Parse(DateTime.Now.Second.ToString());
                    tTime.uiSecondsr = uint.Parse(DateTime.Now.Millisecond.ToString());
                }
             
                try
                {
                    string strFullPicName = ".\\PicStream\\FullPic-No" + (g_iCount++) + "-Time" + tDataTime.ToString("yyyyMMddhhmmss") + ".jpg";
                    if (tFullPicData.iDataLen > 0)
                    {
                        Console.WriteLine(strFullPicName);
                        pfFullPic = new FileStream(strFullPicName, FileMode.Create);
                        if (null != pfFullPic)
                        {
                            byte[] btFullPicData = new byte[tFullPicData.iDataLen];
                            Marshal.Copy(tFullPicData.piPicData, btFullPicData, 0, tFullPicData.iDataLen);//Copy unmanaged memory to managed memory for use in C ා   
                            pfFullPic.Write(btFullPicData, 0, tFullPicData.iDataLen);
                        }
                    }
                }
                catch (IOException e)
                {
                    Console.WriteLine(e.Message);
                }
                finally
                {
                    if (null != pfFullPic)
                    {
                        pfFullPic.Close();
                    }
                }

                for (int i = 0; i < tFacePicStream.iFaceCount; ++i)
                {
                    FacePicData tFacePicData = (FacePicData)Marshal.PtrToStructure(tFacePicStream.tFaceData[i], typeof(FacePicData));
                    /*Face attribute information processing Start */
                    string strDebugInfo = "picture info:iWidth(" + tFacePicData.iWidth + "),iHeight(" + tFacePicData.iHeight + ")" + "\t";
                    for (int j = 0; j < tFacePicData.iFaceAttrCount; ++j)
                    {
                        FaceAttribute tFaceAttr = (FaceAttribute)Marshal.PtrToStructure(tFacePicData.ptFaceAttr[j], typeof(FaceAttribute));
                        if (Enum.IsDefined(typeof(EnumFaceAttrInfo), tFaceAttr.iType))
                        {
                            strDebugInfo += Enum.GetName(typeof(EnumFaceAttrName), tFaceAttr.iType) + ":";
                        }
                        else
                        {
                            strDebugInfo += "Type" + tFaceAttr.iType + ":";
                        }

                        switch (tFaceAttr.iType)
                        {
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Sex:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrSexName), tFaceAttr.iValue);
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Mask:
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Beard:
                            case (int)EnumFaceAttrInfo.FACE_ATTR_OpenEye:
                            case (int)EnumFaceAttrInfo.FACE_ATTR_OpenMouth:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrCommonName), tFaceAttr.iValue);
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Glasses:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrGlassesName), tFaceAttr.iValue);

                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Race:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrRaceName), tFaceAttr.iValue);
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Expression:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrExpressionName), tFaceAttr.iValue);
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Smile:
                                {
                                    if ((int)EnumAttrSmile.ATTR_SMILE_No == tFaceAttr.iValue)
                                    {
                                        strDebugInfo += Enum.GetName(typeof(EnumAttrSmileName), EnumAttrSmile.ATTR_SMILE_No);
                                    }
                                    else
                                    {
                                        strDebugInfo += Enum.GetName(typeof(EnumAttrSmileName), EnumAttrSmile.ATTR_SMILE_Yes);
                                    }
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_Nation:
                                {
                                    if ((int)EnumAttrNation.ATTR_NATION_Han > tFaceAttr.iValue)
                                    {
                                        strDebugInfo += Enum.GetName(typeof(EnumAttrNationName), EnumAttrNation.ATTR_NATION_Han);
                                    }
                                    else
                                    {
                                        strDebugInfo += Enum.GetName(typeof(EnumAttrNationName), EnumAttrNation.ATTR_NATION_Other);
                                        strDebugInfo += tFaceAttr.iValue;
                                    }
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_TEM_VALUE:
                                {
                                    strDebugInfo += (float)((tFaceAttr.iValue - 100000) / 100.0);
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_TEM_UNIT:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrTemUnitName), tFaceAttr.iValue);                      
                                }
                                break;
                            case (int)EnumFaceAttrInfo.FACE_ATTR_ABNORMAL_ALARM:
                                {
                                    strDebugInfo += Enum.GetName(typeof(EnumAttrAbnormalAlarmName), tFaceAttr.iValue);
                                }
                                break;
                            default:
                                {
                                    strDebugInfo += tFaceAttr.iValue;
                                }
                                break;
                        }

                        strDebugInfo += "\t";
                    }
                    /*Face attribute information processing End*/
                    Console.WriteLine(strDebugInfo);

                    string strFacePicName = ".\\PicStream\\FacePic-No" + (g_iCount++) + tDataTime.ToString("yyyyMMddhhmmss") + ".jpg";
                    byte[] btFacePicData = new byte[tFacePicData.iDataLen];
                    Marshal.Copy(tFacePicData.pPicData, btFacePicData, 0, tFacePicData.iDataLen);//Copy unmanaged memory to managed memory for use in C#


                    FileStream pfFaceFile = null;
                    try
                    {
                        if (tFacePicData.iDataLen > 0)
                        {
                            pfFaceFile = new FileStream(strFacePicName, FileMode.Create);
                            if (null != pfFullPic)
                            {
                                pfFaceFile.Write(btFacePicData, 0, tFacePicData.iDataLen);
                            }
                        }
                    }
                    catch (IOException e)
                    {
                        Console.WriteLine(e.Message);
                    }
                    finally
                    {
                        if (null != pfFaceFile)
                        {
                            pfFaceFile.Close();
                        }
                    }
                }
            }
            return 0;
        }

        private static void LogonDevice(int _iLogonType)
        {
            string strIP;
            string strUserName;
            string strPassword;
            string strProductID;

            Console.WriteLine("Please input user name: ");
            strUserName = Console.ReadLine();

            Console.WriteLine("Please input password: ");
            strPassword = Console.ReadLine();

            LogonPara tNormal;
            LogonActiveServer tActive;
            IntPtr pvPara = IntPtr.Zero;
            int iBufLen = 0;

            try
            {
                if (SDKTypes.SERVER_ACTIVE == _iLogonType)
                {
                    Console.WriteLine("Please input ProductID: ");
                    strProductID = Console.ReadLine();
                    tActive = new LogonActiveServer();
                    tActive.iSize = Marshal.SizeOf(tActive);
                    Array.Copy(Encoding.ASCII.GetBytes(strUserName), tActive.btUserName, strUserName.Length);
                    Array.Copy(Encoding.ASCII.GetBytes(strPassword), tActive.btUserPwd, strPassword.Length);
                    Array.Copy(Encoding.ASCII.GetBytes(strProductID), tActive.btProductID, strProductID.Length);
                    pvPara = Marshal.AllocHGlobal(Marshal.SizeOf(tActive));
                    Marshal.StructureToPtr(tActive, pvPara, true);
                    iBufLen = Marshal.SizeOf(tActive);

                    DsmOnline tOnline = new DsmOnline();
                    int length = Marshal.SizeOf(tOnline);
                    Array.Copy(Encoding.ASCII.GetBytes(strProductID), tOnline.btProductID, strProductID.Length);
                    IntPtr ptOnline = IntPtr.Zero;
                    try
                    {
                        ptOnline = Marshal.AllocHGlobal(length);
                        Marshal.StructureToPtr(tOnline, ptOnline, true);
                        NVSSDK.NetClient_GetDsmRegstierInfo(SDKConstMsg.DSM_CMD_GET_ONLINE_STATE, ptOnline, Marshal.SizeOf(tOnline));
                        int iOutTime = 0;
                        while (SDKTypes.DSM_STATE_ONLINE != tOnline.iOnline)
                        {
                            if (iOutTime >= 20)
                            {
                                Console.WriteLine("Device not register!\n");
                                break;
                            }
                            Thread.Sleep(1000);
                            NVSSDK.NetClient_GetDsmRegstierInfo(SDKConstMsg.DSM_CMD_GET_ONLINE_STATE, ptOnline, Marshal.SizeOf(tOnline));
                            iOutTime++;
                        }
                    }
                    catch (System.Exception ex)
                    {
                        Console.WriteLine(ex.Message);
                    }
                    finally
                    {
                        Marshal.FreeHGlobal(ptOnline);
                    }
                }
                else
                {
                    string strCharSet = "UTF-8";
                    Console.WriteLine("Please input server IP: ");
                    strIP = Console.ReadLine();
                    tNormal = new LogonPara();
                    tNormal.iSize = Marshal.SizeOf(tNormal);
                    tNormal.iNvsPort = 3000;
                    Array.Copy(Encoding.ASCII.GetBytes(strIP), tNormal.btNvsIP, strIP.Length);
                    Array.Copy(Encoding.ASCII.GetBytes(strUserName), tNormal.btUserName, strUserName.Length);
                    Array.Copy(Encoding.ASCII.GetBytes(strPassword), tNormal.btUserPwd, strPassword.Length);
                    Array.Copy(Encoding.ASCII.GetBytes(strCharSet), tNormal.btCharSet, strCharSet.Length);
                    iBufLen = Marshal.SizeOf(tNormal);
                    pvPara = Marshal.AllocHGlobal(Marshal.SizeOf(tNormal));
                    Marshal.StructureToPtr(tNormal, pvPara, true);
                }

                g_iLogonID = NVSSDK.NetClient_Logon_V4(_iLogonType, pvPara, iBufLen);
                if (g_iLogonID < 0)
                {
                    Console.WriteLine("[NetClient_Logon_V4] fail! " + g_iLogonID + "\n");
                }
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(pvPara);
            }
        }

        private static void StartRecvPicture()
        {
            IntPtr ptNetPicPara = IntPtr.Zero;
            try
            {
                g_tNetPicPara.iStructLen = Marshal.SizeOf(g_tNetPicPara);
                g_tNetPicPara.iChannelNo = 0;
                g_tNetPicPara.cbkPicStreamNotify = MyNETPICSTREAM_NOTIFY;
                g_tNetPicPara.pvUser = IntPtr.Zero;
                ptNetPicPara = Marshal.AllocHGlobal(Marshal.SizeOf(g_tNetPicPara));
                Marshal.StructureToPtr(g_tNetPicPara, ptNetPicPara, true);
                int iRet = NVSSDK.NetClient_StartRecvNetPicStream(g_iLogonID, ptNetPicPara, Marshal.SizeOf(g_tNetPicPara), ref g_uiRecvID);
                if (0 != iRet)
                {
                    Console.WriteLine("[NetClient_StartRecvNetPicStream] fail!");
                }
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(ptNetPicPara);
            }
        }

        private static void StopRecvPicture()
        {
            int iRet = NVSSDK.NetClient_StopRecvNetPicStream(g_uiRecvID);
            if (0 != iRet)
            {
                Console.WriteLine("[NetClient_StopRecvNetPicStream] fail!");
            }
            else
            {
                g_uiRecvID = CONST_INVALID_RECV_ID;
            }
        }

        private static void CreateStreamDirectory()
        {
            if (!Directory.Exists(g_strPicStreamDir))
            {
                Directory.CreateDirectory(g_strPicStreamDir);
            }
        }

        private static void FaceDetectionEnable()
        {
            IntPtr ptParam = IntPtr.Zero;
            AnyScene tParam = new AnyScene();
            tParam.iBufSize = Marshal.SizeOf(tParam);
            tParam.iSceneID = 0;	//Scene number 0-15
            tParam.iDevType = 1;	//0-IPC, 1-NVR
            int iBytesReturned = 0;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
            Marshal.StructureToPtr(tParam, ptParam, true);
            int iRet = NVSSDK.NetClient_GetDevConfig(g_iLogonID, NetClientTypes.ANYSCENE, 0, ptParam, Marshal.SizeOf(tParam), ref iBytesReturned);
            if (0 > iRet)
            {
                Console.WriteLine("[NetClient_GetDevConfig] NET_CLIENT_ANYSCENE fail!");
            }
            else
            {
                tParam = (AnyScene)Marshal.PtrToStructure(ptParam, typeof(AnyScene));
                tParam.iArithmetic = 1 << 2;//Face detection algorithm on
                tParam.iDevType = 1;	//0-IPC, 1-NVR
                Marshal.StructureToPtr(tParam, ptParam, true);
                iRet = NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.ANYSCENE, 0, ptParam, Marshal.SizeOf(tParam));
                if (iRet >= 0)
                {
                    Console.WriteLine("Successful opening of face detection algorithm.\n");
                }
                else
                {
                    Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_ANYSCENE failed.\n");
                }
            }
        }

        private static void SetVcaStatue(int _iStatus)
        {
            int iChanNo = 0;	//Channel number, 0 for the first channel
            VCASuspend tInfo = new VCASuspend();
            tInfo.iStatus = _iStatus;
            tInfo.iDevType = 1;
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ptParam, true);
            NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.VCA_SUSPEND, iChanNo, ptParam, Marshal.SizeOf(tInfo));
        }

        static void SetDetectParam()
        {
            FaceDetectArithmetic fda = new FaceDetectArithmetic();
            fda.iBufSize = Marshal.SizeOf(fda);
            fda.iDevType = 0;	//0-IPC, 1-NVR
            int iByteReturn = 0;
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(fda));
            Marshal.StructureToPtr(fda, ptParam, true);
            int iRet = NVSSDK.NetClient_GetDevConfig(g_iLogonID, NetClientTypes.FACE_DETECT_ARITHMETIC, 0, ptParam, Marshal.SizeOf(fda), ref iByteReturn);
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
                return;
            }

            fda = (FaceDetectArithmetic)Marshal.PtrToStructure(ptParam, typeof(FaceDetectArithmetic));
            if (fda.iMinSize >= fda.iMaxSize)
            {
                fda.iMaxSize = fda.iMinSize + 1;//Effectiveness of adjustment parameters (avoidance)
            }

            fda.iPushMode = 2;
            fda.iSnapTimes = 1;
            Marshal.StructureToPtr(fda, ptParam, true);
            iRet = NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.FACE_DETECT_ARITHMETIC, 0, ptParam, Marshal.SizeOf(fda));
            if (iRet >= 0)
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC success.\n");
            }
            else
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_FACE_DETECT_ARITHMETIC failed.\n");
            }
        }

        static void SetPicStreamUploadParam()
        {
            //Set background image quality and upload enable
            PicStreamUploadParam tInfo = new PicStreamUploadParam();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iSceneId = 0;
            tInfo.iPicType = 0;//0-Background map

            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ptParam, true);
            int iRet = NVSSDK.NetClient_VCAGetConfig(g_iLogonID, VcaCmd.PICSTREAM_UPLOADPARAM, 0, ptParam, Marshal.SizeOf(tInfo));

            if (iRet < 0)
            {
                Console.WriteLine("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
            }
            else
            {
                tInfo = (PicStreamUploadParam)Marshal.PtrToStructure(ptParam, typeof(PicStreamUploadParam));
                tInfo.iSnapEnable = 1;
                tInfo.iQpvalue = 80;
                tInfo.iIsOsd = 1;
                Marshal.StructureToPtr(tInfo, ptParam, true);
                iRet = NVSSDK.NetClient_VCASetConfig(g_iLogonID, VcaCmd.PICSTREAM_UPLOADPARAM, 0, ptParam, Marshal.SizeOf(tInfo));
                if (iRet >= 0)
                {
                    Console.WriteLine("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM success.\n");
                }
                else
                {
                    Console.WriteLine("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
                }
            }

            //Set up close-up image quality and upload enable
            PicStreamUploadParam tParam = new PicStreamUploadParam();
            tParam.iSize = Marshal.SizeOf(tParam);
            tParam.iSceneId = 0;
            tParam.iPicType = 1;//1-Close up

            IntPtr ptSpParam = IntPtr.Zero;
            ptSpParam = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
            Marshal.StructureToPtr(tParam, ptSpParam, true);
            iRet = NVSSDK.NetClient_VCAGetConfig(g_iLogonID, VcaCmd.PICSTREAM_UPLOADPARAM, 0, ptSpParam, Marshal.SizeOf(tParam));
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetDevConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
            }
            else
            {
                tParam = (PicStreamUploadParam)Marshal.PtrToStructure(ptSpParam, typeof(PicStreamUploadParam));
                tParam.iSnapEnable = 1;
                tParam.iQpvalue = 30;
                Marshal.StructureToPtr(tParam, ptSpParam, true);
                iRet = NVSSDK.NetClient_VCASetConfig(g_iLogonID, VcaCmd.PICSTREAM_UPLOADPARAM, 0, ptSpParam, Marshal.SizeOf(tParam));
                if (iRet < 0)
                {
                    Console.WriteLine("NetClient_VCASetConfig  VCA_CMD_PICSTREAM_UPLOADPARAM failed.\n");
                }
            }
        }

        //Enable / disable human body temperature measurement
        static void SetTemDetectEnable(int _iEnable)
        {
            int iRet = NVSSDK.NetClient_SetCommonEnable(g_iLogonID, CommonEnbaleID.TEMDETECT, 0, _iEnable); //Channel 0 is the visible light channel, and the thermal imaging equipment sets this parameter according to channel 0
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_SetCommonEnable  CI_COMMON_ID_TEMDETECT failed.\n");
            }
            else
            {
                Console.WriteLine("NetClient_SetCommonEnable  CI_COMMON_ID_TEMDETECT success.\n");
            }
        }

        //Set temperature scale type
        static void SetTemScaleType(int _iType)
        {
            if (1 != _iType && 2 != _iType)
            {
                Console.WriteLine("SetTemScaleType  inValid param!\n");
            }
            TemperatureScaleType tInfo = new TemperatureScaleType();
            tInfo.iChanNo = 0;
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iTempStandard = _iType;
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ptParam, true);
            int iRet = NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.TEMPERATURE_STANDARD, 0, ptParam, Marshal.SizeOf(tInfo));
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_TEMPERATURE_STANDARD failed.\n");
            }
            else
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_TEMPERATURE_STANDARD success.\n");
            }
        }
        //Set blackbody correction parameters
        static void SetBlackbodyCorrection(int _iChannelNo)
        {
            BlackbodyCorrection tInfo = new BlackbodyCorrection();
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ptParam, true);
            int iByteReturn = -1;
            int iRet = NVSSDK.NetClient_GetDevConfig(g_iLogonID, NetClientTypes.BLACKBODY_CORRECT, _iChannelNo, ptParam, Marshal.SizeOf(tInfo), ref iByteReturn);
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_GetDevConfig  NET_CLIENT_BLACKBODY_CORRECT failed.\n");
            }
            else
            {
                tInfo = (BlackbodyCorrection)Marshal.PtrToStructure(ptParam, typeof(BlackbodyCorrection));
                tInfo.iChanNo = 0;
                tInfo.iSize = Marshal.SizeOf(tInfo);
                tInfo.iBlackBodyCorrectEnable = 1;       //Blackbody correction enable, 0-off, 1-on, default on
                tInfo.iBlackBodyCorrectType = 2;         //Blackbody correction type, 1-single correction, 2-continuous correction, default continuous correction
				//temperature unit: 0-reserved, 1-celsius, 2-fahrenheit, 3-kelvin
				if(1 == tInfo.tParam[0].iBlackBodyTempUnit)
				{
					tInfo.tParam[0].iBlackBodyTemp = 3500;   //Temperature value * 100,  value 30-45  
				}
				else if(2 == tInfo.tParam[0].iBlackBodyTempUnit)
				{
					tInfo.tParam[0].iBlackBodyTemp = 9000;//Temperature value * 100 86 - 113
				}
                tInfo.tParam[0].iBlackBodyDistance = 100;//Distance in bold, CM
                tInfo.tParam[0].tRect.left = 2000;       //Left margin - the X coordinate of the upper left corner, which is the coordinate of ten thousandth ratio, 0-10000
                tInfo.tParam[0].tRect.top = 2000;        //Top margin - Y coordinate of the upper left corner
                tInfo.tParam[0].tRect.right = 8000;      //Right margin - bottom right X coordinate
                tInfo.tParam[0].tRect.bottom = 8000;     //Bottom margin - bottom right y coordinate
                ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
                Marshal.StructureToPtr(tInfo, ptParam, true);
                iRet = NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.BLACKBODY_CORRECT, _iChannelNo, ptParam, Marshal.SizeOf(tInfo));
                if (iRet < 0)
                {
                    Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_BLACKBODY_CORRECT failed.\n");
                }
                else
                {
                    Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_BLACKBODY_CORRECT success.\n");
                }
            }
        }

        //Set temperature conversion parameters
        static void SetBodyTemCompensation(int _iChannelNo)
        {
            BodyTempCorrect tInfo = new BodyTempCorrect();
            tInfo.iChanNo = _iChannelNo;
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iBodyTempCorrectEnable = 1;//Body temperature compensation enable, 0-not enabled, 1-enabled
            tInfo.iBodyTempCorrectSensitivity = 50;//Temperature compensation sensitivity, 0-100
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ptParam, true);
            int iRet = NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.BODYTEMP_CORRECT, _iChannelNo, ptParam, Marshal.SizeOf(tInfo));
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_BODYTEMP_CORRECT failed.\n");
            }
            else
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_BODYTEMP_CORRECT success.\n");
            }
        }

        //Set intelligent correction parameters
        static void SetIntelligentCorretct(int _iChannelNo)
        {
            IntelligentCorretct tInfo = new IntelligentCorretct();
            tInfo.iChanNo = _iChannelNo;
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iIntelligentCorrectEnable = 1;//Intelligent correction enable, 0-not enabled, 1-enabled
            tInfo.iIntelligentCorrectSensitivity = 50;//Intelligent correction sensitivity,0-100
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ptParam, true);
            int iRet = NVSSDK.NetClient_SetDevConfig(g_iLogonID, NetClientTypes.INTELLIGENT_CORRECT, _iChannelNo, ptParam, Marshal.SizeOf(tInfo));
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_INTELLIGENT_CORRECT failed.\n");
            }
            else
            {
                Console.WriteLine("NetClient_SetDevConfig  NET_CLIENT_INTELLIGENT_CORRECT success.\n");
            }
        }
        //Set temperature abnormal alarm parameters
        static void SetVcaTemDetect()
        {
            VCATemDetect vc = new VCATemDetect();
            vc.iSize = Marshal.SizeOf(vc);
            vc.iRuleID = 14;              //For temperature detection, when calling get interface, please bind 14
            vc.iSceneID = 0;              //Scene number, 0~32
            vc.iModelType = 1;
            IntPtr ptParam = IntPtr.Zero;
            ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(vc));
            Marshal.StructureToPtr(vc, ptParam, true);
            int iRet = NVSSDK.NetClient_VCAGetConfig(g_iLogonID, VcaCmd.TEMDETECT, 0, ptParam, Marshal.SizeOf(vc));
            if (iRet < 0)
            {
                Console.WriteLine("NetClient_VCAGetConfig  VCA_CMD_TEMDETECT failed.\n");
            }
            else
            {
                vc = (VCATemDetect)Marshal.PtrToStructure(ptParam, typeof(VCATemDetect));
                vc.iValid = 1;                //Whether this event detection is valid, 0-invalid, 1-valid
                vc.iTemThreshold = 3600;      //Temperature threshold, the value is the actual temperature * 100
                vc.iTempLoseEnable = 1;       //Temperature abnormal alarm, 0-not enabled, 1-enabled
                ptParam = Marshal.AllocHGlobal(Marshal.SizeOf(vc));
                Marshal.StructureToPtr(vc, ptParam, true);
                iRet = NVSSDK.NetClient_VCASetConfig(g_iLogonID, VcaCmd.TEMDETECT, 0, ptParam, Marshal.SizeOf(vc));
                Console.WriteLine("NetClient_VCASetConfig  VCA_CMD_TEMDETECT success.\n");
            }
        }

        static void Main(string[] args)
        {
            int iLogonType = SDKTypes.SERVER_NORMAL;
            string strTemp;
            Console.WriteLine("Please input LogonType: 0----Normal  1----Active\n");
            strTemp = Console.ReadLine();
            iLogonType = Convert.ToInt32(strTemp);

            //Initialize SDK
            if (SDKTypes.SERVER_ACTIVE == iLogonType)
            {
                Console.WriteLine("Please input listening port:");
                strTemp = Console.ReadLine();
                int iLlisteningPort = Convert.ToInt32(strTemp);
                NVSSDK.NetClient_Startup_V4(iLlisteningPort, 0, 0);
            }
            else
            {
                NVSSDK.NetClient_Startup_V4(0, 0, 0);
            }

            //Set callback function
            MainNotify_V4 = MyMAIN_NOTIFY_V4;
            NVSSDK.NetClient_SetNotifyFunction_V4(MainNotify_V4, null, null, null, null);

            //Login device
            LogonDevice(iLogonType);

            //Create picture stream receiving directory
            CreateStreamDirectory();

	        while (true)
	        {
		        Console.WriteLine("Please select: 0 exit, 1 enable face detection, 2 set face detection parameters, 3 enable body temperature measurement, 4 temperature scale selection, 5 blackbody correction,\n6 temperature conversion, 7 intelligent correction, 8 set temperature abnormal alarm parameters\n");
                strTemp = Console.ReadLine();
                if ("" == strTemp)
                {
                    continue;
                }

                int iOpt = Convert.ToInt32(strTemp);
		        if (0 == iOpt)
		        {
			        break;
		        }
		        else if (1 == iOpt)	//Turn on face detection
		        {
			        FaceDetectionEnable();
		        }
		        else if (2 == iOpt)//Set face detection parameters
		        {
			        SetVcaStatue(VCASuspendStatus.STATUS_PAUSE);	//Pause intelligent analysis
                    Console.ReadLine();
			        if (VCASuspendStatus.RESULT_SUCCESS != g_iVcaStatus) {
				        Console.WriteLine("[main] Intelligent analysis pause failed!\n");
                        Console.ReadLine();
				        continue;
			        }
			        SetDetectParam();//Parameters related to face detection (need to pause intelligent analysis)
			        SetVcaStatue(VCASuspendStatus.STATUS_RESUME);//Intelligent analysis needs to be restored after setting

			        SetPicStreamUploadParam();//Capture image quality, upload related parameters
		        }
		        else if (3 == iOpt)//Enable human body temperature measurement
		        {
                    SetTemDetectEnable(VCATemDetectStatus.TEM_DETECT_ENABLE);
		        }
		        else if (4 == iOpt)//Set temperature scale type
		        {
			        Console.WriteLine("1-celsius, 2-fahrenheit\n");
                    strTemp = Console.ReadLine();
                    int iType = Convert.ToInt32(strTemp);
			        SetTemScaleType(iType);
		        }
		        else if (5 == iOpt)//Blackbody correction
		        {
                    Console.WriteLine("Please Input Thermal imaging ChannelNo,(default:1)\n");
                    strTemp = Console.ReadLine();
                    int iChannelNo = 1;
                    if (strTemp.Length > 0)
                    {
                        iChannelNo = Convert.ToInt32(strTemp);
                    }

                    SetBlackbodyCorrection(iChannelNo);
		        }
		        else if (6 == iOpt)//Body temperature conversion
		        {
                    Console.WriteLine("Please Input Thermal imaging ChannelNo,(default:1)\n");
                    strTemp = Console.ReadLine();
                    int iChannelNo = 1;
                    if (strTemp.Length > 0)
                    {
                        iChannelNo = Convert.ToInt32(strTemp);
                    }
                    SetBodyTemCompensation(iChannelNo);
		        }
		        else if (7 == iOpt)//Intelligent correction
		        {
                    Console.WriteLine("Please Input Thermal imaging ChannelNo,(default:1)\n");
                    strTemp = Console.ReadLine();
                    int iChannelNo = 1;
                    if (strTemp.Length > 0)
                    {
                        iChannelNo = Convert.ToInt32(strTemp);
                    }
                    SetIntelligentCorretct(iChannelNo);
		        }
		        else if (8 == iOpt)//Set temperature abnormal alarm parameters
		        {
			        SetVcaTemDetect();
		        }
                else
                {
                    continue;
                }
	        }

            Console.ReadLine();

            //Stop receiving picture stream
            StopRecvPicture();

            //Log off user
            NVSSDK.NetClient_Logoff(g_iLogonID);

            //Release SDK resources
            NVSSDK.NetClient_Cleanup();

        }
    }


}
