using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;
using System.Threading;

using System.IO;
using System.Runtime.InteropServices;

namespace FaceDemo
{
    delegate void SetProcessBarValueCallBack(int iValue);
    delegate void SetButtonCallBack();
    public partial class Client : Form
    {
        private MAIN_NOTIFY_V4 MainNotify_V40 = null;//Main callback

        public int m_iLogonId;//login ID
        public int m_iChannelNo;//Channel number
        public int m_iTaskID;//Face recognition needs this variable
        public List<string> m_listInportFacePic;
        public List<FaceInfo> m_listExportFacePic;
        public int m_iImportPicCount;
        public int m_iChannelCount;
        bool g_isDownloadFinished = false;
        public UInt32 m_uDLId;

        public string m_strPicPath = "";

        public int m_iLogonMode = NVSSDK.SERVER_NORMAL;


        private delegate void DispMSGDelegate(int index, string MSG);//Define an agent

        public Client()
        {
            InitializeComponent();
            m_iLogonId = -1;
            StartUp();
            //CheckForIllegalCrossThreadCalls = false;//Allows cross-thread access to the control at false
            tabPage4.Parent = null;//Temporarily hide the face layout page:tabPage4(TO DO,The page can be released after it implemented)
            //tabPageSearchByEvent.Parent = null;//Temporarily hide the retrieve face page by event
            //tabPageSearchByFeature.Parent = null;//Temporarily hide the retrieve face page by feature
        }

        //Start and initialize the SDK
        private void StartUp()
        {
            //Set the default network ports used by the client and the master controller

            NVSSDK.NetClient_SetPort(3000, 6000);

            //Set the ID of message notification 
            //NVSSDK.NetClient_SetMSGHandle(SDKConstMsg.WM_MAIN_MESSAGE, this.Handle, SDKConstMsg.MSG_PARACHG, SDKConstMsg.MSG_ALARM);

            //Set the working mode of the SDK
            NVSSDK.NetClient_SetSDKWorkMode(NVSSDK.EASYX_LIGHT_MODE);

            //Start up the SDK
            NVSSDK.NetClient_Startup_V4(0, 0, 0);

            //Set the main callback
            MainNotify_V40 = MyMAIN_NOTIFY_V4;
            NVSSDK.NetClient_SetNotifyFunction_V4(MainNotify_V40, null, null, null, null);

            //Initialize UI interface
            UI_Init();

        }

        private void MyMAIN_NOTIFY_V4(UInt32 _ulLogonID, IntPtr _iWparam, IntPtr _iLParam, IntPtr _iUser)
        {
            switch (_iWparam.ToInt32())
            {
                //The message of the login status 
                //param1 login IP
                //param2 login ID
                //param3 login status
                case SDKConstMsg.WCM_LOGON_NOTIFY:
                    {
                        switch (_iLParam.ToInt32())
                        {
                            case SDKConstMsg.LOGON_SUCCESS:
                                //MessageBox.Show("Login successfully! notify_v4");
                                break;
                            case SDKConstMsg.LOGON_TIMEOUT:
                                MessageBox.Show("Login timeout! notify_v4");
                                break;
                            case SDKConstMsg.LOGON_FAILED:
                                MessageBox.Show("Login failed! notify_v4");
                                break;
                            default:
                                break;
                        }
                        break;
                    }
                case SDKConstMsg.WCM_FACE_MODEING:
                    {
                        FaceModeResult tRet = new FaceModeResult();
                        tRet = (FaceModeResult)Marshal.PtrToStructure(_iLParam, typeof(FaceModeResult));

                        if (tRet.iTotal > 0)
                        {
                            Win32API.PostMessage(_iUser, ClientControlMsg.WM_CLIENT_MODEING, tRet.iIndex, tRet.iTotal);
                        }
                        break;
                    }
                case SDKConstMsg.WCM_VCA_SUSPEND:
                    {
                        Win32API.PostMessage(_iUser, ClientControlMsg.WM_CLIENT_SUSPEND, 0, 0);
                        break;
                    }
                case SDKConstMsg.WCM_DWONLOAD_FINISHED:
                    {
                        UInt32 uDownloadID = (UInt32)_iLParam.ToInt32();
		                if (m_uDLId != uDownloadID) {
                            return;
		                }
                        NVSSDK.NetClient_NetFileStopDownloadFile(m_uDLId);
                        if (m_strPicPath != "")
                        {
                            PictureBox pictureBox1 = new PictureBox();
                            this.pictureBox1.Load(m_strPicPath);
                            m_strPicPath = "";
                        }
                        g_isDownloadFinished = true;
                        break;
                    }
                case SDKConstMsg.WCM_DOWNLOAD_INTERRUPT:
                case SDKConstMsg.WCM_DWONLOAD_FAULT:
                    {
                        //MessageBox.Show("Download file failed");
                        UInt32 uDownloadID = (UInt32)_iLParam.ToInt32();
                        if (m_uDLId != uDownloadID)
                        {
                            return;
                        }
                        NVSSDK.NetClient_NetFileStopDownloadFile(m_uDLId);
                        break;
                    }
                default:
                    break;
            }
        }

        //Overrides the message-handing function to handle custom messages
        protected override void DefWndProc(ref System.Windows.Forms.Message m)
        {
            switch (m.Msg)
            {
                case SDKConstMsg.WM_MAIN_MESSAGE://WM_MAIN_MESSAGE is a custom message
                    {
                        //The custom message-handing function 
                        OnMessagePro(m.WParam, m.LParam);
                        break;
                    }
                case ClientControlMsg.WM_CLIENT_MODEING:
                    {
                        labelPicModelProcess.Text = m.WParam.ToInt32().ToString() + "/" + m.LParam.ToInt32().ToString();
                        progressBarPicModel.Value = m.WParam.ToInt32() * 100 / m.LParam.ToInt32();

                        if (m.WParam.ToInt32() == m.LParam.ToInt32())
                        {
                            //the modeling is completed 
                            UI_ShowPicPage(m_iCurPage, false);
                        }

                        break;
                    }
                case ClientControlMsg.WM_CLIENT_SUSPEND:
                    {
                        VCASuspendResult tParam = new VCASuspendResult();
                        tParam.iBufSize = Marshal.SizeOf(typeof(VCASuspendResult));

                        IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
                        Marshal.StructureToPtr(tParam, ipParam, true);//false is prone to memory leaks

                        int iRet = NVSSDK.NetClient_GetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_VCA_SUSPEND, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(VCASuspendResult)), IntPtr.Zero);

                        tParam = (VCASuspendResult)Marshal.PtrToStructure(ipParam, typeof(VCASuspendResult));

                        if (NVSSDK.VCA_SUSPEND_STATUS_PAUSE == tParam.iStatus && NVSSDK.VCA_SUSPEND_RESULT_CONFIGING == tParam.iResult)
                        {
                            //The pause fails. Operations such as adding, modifying, deleting, and modeling are not allowed on the interface
                        }
                        else if (NVSSDK.VCA_SUSPEND_STATUS_PAUSE == tParam.iStatus && NVSSDK.VCA_SUSPEND_RESULT_SUCCESS == tParam.iResult)
                        {
                            UI_EnbaleTabPic(true);//Operations such as adding, modifying, deleting, and modeling are allowed on the page only after the pause succeeds
                        }

                        Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

                        break;
                    }
                case ClientControlMsg.WM_CLIENT_RECVPICNUM:
                    {
                        labelStreamRecvPicNub.Text = g_iCount.ToString();

                        break;
                    }
                default:
                    break;
            }


            //The default message-handing function
            base.DefWndProc(ref m);
        }

        public void OnMessagePro(IntPtr wParam, IntPtr lParam)
        {
            //The low 16 bits of wParam are the type of message
            int iMsgType = wParam.ToInt32() & 0xFFFF;
            //lParam. Information structure NVS_IPAndID address of the network video server NVS
            //Marshal.The PtrToStructure function converts the Intptr address into a structure
            //NVS_IPAndID  ipAndID = (NVS_IPAndID)Marshal.PtrToStructure(lParam, typeof(NVS_IPAndID));

            switch (iMsgType)
            {
                //The message of the login status 
                //param1 login IP
                //param2 login ID
                //param3 login status
                case SDKConstMsg.WCM_LOGON_NOTIFY:
                    {
                        NVS_IPAndID ipAndID = (NVS_IPAndID)Marshal.PtrToStructure(lParam, typeof(NVS_IPAndID));
                        int i = wParam.ToInt32();
                        LogonNotify(ipAndID.m_pIP.ToCharArray(), ipAndID.m_pID, wParam.ToInt32() >> 16);
                        break;
                    }
                default:
                    break;
            }
        }


        //WCM_LOGON_NOTIFY, the message-handing function
        private void LogonNotify(char[] _cIP, string _strID, int iLogonState)
        {
            //iLogonState the login status
            switch (iLogonState)
            {
                case SDKConstMsg.LOGON_SUCCESS://Display device ID after the login succeeds
                    {
                        //MessageBox.Show("Login successfully! notify_v4");
                        break;
                    }
                case SDKConstMsg.LOGON_FAILED:
                case SDKConstMsg.LOGON_ING:
                case SDKConstMsg.LOGON_RETRY:
                case SDKConstMsg.NOT_LOGON:
                case SDKConstMsg.LOGON_TIMEOUT://The login is failed
                    {
                        MessageBox.Show("Logon failed!");
                        break;
                    }
                default:
                    break;
            }
        }


        private void UI_Init()
        {
            UI_Init_TabPic();//the base of images of human face 
            UI_Init_TabSchedule();//The protection of human face
            UI_Init_TabStream();//The picture stream of human face
            UI_Init_Search_dialog();//To capture pictures by searching for pictures
            UI_Init_CapByEvent();//Retrieve human faces by event
            UI_Init_CapByFeature();//Retrieve human faces by the feature value
        }

        private void buttonLogon_Click(object sender, EventArgs e)
        {
            int iRet = -1;
            if (NVSSDK.SERVER_ACTIVE == m_iLogonMode)
            {
	            //Login logic through active mode
	            int iLocalListenPort = Int32.Parse(textBoxPort.Text);
	            iRet = NVSSDK.NetClient_SetPort(iLocalListenPort, 0);	//Start up the local listening service
	            if(0 != iRet ) {
		            MessageBox.Show("Set local lan port fail!");
		            return;
	            }

	            ActiveNetWanInfo tLocalWanInfo = new ActiveNetWanInfo();
                tLocalWanInfo.cWanIP = new char[32];
	            tLocalWanInfo.iSize = Marshal.SizeOf(tLocalWanInfo);
	            tLocalWanInfo.iWanPort = Int32.Parse(textBoxWanPort.Text);
                CommonFunction.CharsCopy(textBoxWanIP.Text.ToCharArray(), tLocalWanInfo.cWanIP);
	            //Enable a port on the local public network(Router Mapping Port)
                IntPtr ptrWanInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tLocalWanInfo));
                Marshal.StructureToPtr(tLocalWanInfo, ptrWanInfo, true);
                iRet = NVSSDK.NetClient_SetDsmConfig(NVSSDK.DSM_CMD_SET_NET_WAN_INFO, ptrWanInfo, Marshal.SizeOf(tLocalWanInfo));
	            if(0 != iRet) {
		            MessageBox.Show("Set local wan port fail!");
		            return;
	            }

	            DsmOnline tOnline = new DsmOnline();
	            tOnline.iSize = Marshal.SizeOf(tOnline);
                tOnline.cProductID = new char[32];
                CommonFunction.CharsCopy(textBoxIP.Text.ToCharArray(), tOnline.cProductID );
	            int iOutTime = 0;
	            while (true)
	            {
		            //Obtain the online registration status
                    IntPtr ptrOnLine = Marshal.AllocHGlobal(Marshal.SizeOf(tOnline));
                    Marshal.StructureToPtr(tOnline, ptrOnLine, true);
                    NVSSDK.NetClient_GetDsmRegstierInfo(NVSSDK.DSM_CMD_GET_ONLINE_STATE, ptrOnLine, Marshal.SizeOf(tOnline));
                    tOnline = (DsmOnline)Marshal.PtrToStructure(ptrOnLine, typeof(DsmOnline));
                    if (NVSSDK.DSM_STATE_ONLINE == tOnline.iOnline)
                    {
			            break;
		            }

		            if (iOutTime >= 30) {
                        MessageBox.Show("Device not register!");
			            return;
		            }

                    Thread.Sleep(1000);
		            iOutTime++;
	            }

	            LogonActiveServer tActive = new LogonActiveServer();
                tActive.iSize = Marshal.SizeOf(tActive);
                tActive.cProductID = new char[32];
                tActive.cUserName = new char[16];
                tActive.cUserPwd = new char[16];
                CommonFunction.CharsCopy(textBoxIP.Text.ToCharArray(), tActive.cProductID);
                CommonFunction.CharsCopy(textBoxAdmin.Text.ToCharArray(), tActive.cUserName);
                CommonFunction.CharsCopy(textBoxPassWord.Text.ToCharArray(), tActive.cUserPwd);
                IntPtr ptrActive = Marshal.AllocHGlobal(Marshal.SizeOf(tActive));
                Marshal.StructureToPtr(tActive, ptrActive, true);
                m_iLogonId = NVSSDK.NetClient_Logon_V4(NVSSDK.SERVER_ACTIVE, ptrActive, Marshal.SizeOf(tActive));
            }
            else
            {
                LogonPara tNormal = new LogonPara();
                tNormal.cNvsIP = new char[32];
                tNormal.cUserName = new char[16];
                tNormal.cUserPwd = new char[16];
                tNormal.iSize = Marshal.SizeOf(tNormal);
                CommonFunction.CharsCopy(textBoxIP.Text.ToCharArray(), tNormal.cNvsIP);
                CommonFunction.CharsCopy(textBoxAdmin.Text.ToCharArray(), tNormal.cUserName);
                CommonFunction.CharsCopy(textBoxPassWord.Text.ToCharArray(), tNormal.cUserPwd);
                tNormal.iNvsPort = Int32.Parse(textBoxPort.Text);
                IntPtr ptrNormal = Marshal.AllocHGlobal(Marshal.SizeOf(tNormal));
                Marshal.StructureToPtr(tNormal, ptrNormal, true);
                m_iLogonId = NVSSDK.NetClient_Logon_V4(NVSSDK.SERVER_NORMAL, ptrNormal, Marshal.SizeOf(tNormal));
            }

            if (m_iLogonId < 0)
            {
                m_iLogonId = -1;
                MessageBox.Show("Logon failed!");
                return;
            }

            NVSSDK.NetClient_SetNotifyUserData_V4(m_iLogonId, this.Handle);

            int iTimes = 0;
            while (0 != NVSSDK.NetClient_GetLogonStatus(m_iLogonId))
            {
                if (iTimes++ > 250)
                {
                    return;
                }
                Thread.Sleep(20);
            }

            int iChanNum = 0;
            NVSSDK.NetClient_GetChannelNum(m_iLogonId, ref iChanNum);
            comboBoxChanelNo.Items.Clear();

            for (int i = 0; i < iChanNum; ++i)
            {
                comboBoxChanelNo.Items.Insert(i, (i + 1).ToString());
            }
            comboBoxChanelNo.SelectedIndex = 0;
            m_iChannelNo = comboBoxChanelNo.Items.IndexOf(comboBoxChanelNo.Text);
            
            tabControl_SearchLib.Enabled = true;
            UI_Init_SearchPic();
            QueryLibkey(ref comboBox_SearchLib);
            UI_Init_FaceDetect();//The interface of face detection
            UpdateFaceDetect();//The parameter of face detection
        }

        private void buttonLogOff_Click(object sender, EventArgs e)
        {
            if (m_iLogonId >= 0)
            {
                NVSSDK.NetClient_Logoff(m_iLogonId);
                m_iLogonId = -1;
                tabControl_SearchLib.Enabled = false;
                buttonStreamStartRecv.Enabled = true;
                g_uiRecvID = CONST_INVALID_RECV_ID;
            }
        }

        private void comboBoxChanelNo_SelectedIndexChanged(object sender, EventArgs e)
        {
            m_iChannelNo = comboBoxChanelNo.Items.IndexOf(comboBoxChanelNo.Text);
            UpdateFaceDetect();
        }

        //Tab1: Face database module------------

        //The column of face library list(listViewLib)corresponding to the index number
        public class LIB_LINE_INDEX
        {
            public const int ITEM_LIB_LIBKEY = 0;                //LibKey Hidden columns
            public const int ITEM_LIB_INDEX = 1;				//serials
            public const int ITEM_LIB_NAME = 2;				//The library name
            public const int ITEM_LIB_VALUE = 3;				//Recognition threshold
            public const int ITEM_LIB_UPLOAD = 4;				//Recognition information
            public const int ITEM_LIB_DESCRIP = 5;				//Describe
            public const int ITEM_LIB_UUID = 6;				//Platform UUID
            public const int ITEM_LIB_VERSION = 7;			    //Platform version
        }

        private void buttonLibQuery_Click(object sender, EventArgs e)
        {
            UI_UpdataList();
        }

        private void buttonLibAdd_Click(object sender, EventArgs e)
        {
            FaceLibEdit tEdit = new FaceLibEdit();
            tEdit.tFaceLib = new FaceLibInfo();
            tEdit.tFaceLib.cName = new byte[CommonLen.LEN_64];
            tEdit.tFaceLib.cExtrInfo = new byte[CommonLen.LEN_64];
            tEdit.tFaceLib.cLibUUID = new byte[CommonLen.LEN_UUID];
            tEdit.tFaceLib.cLibVersion = new byte[CommonLen.LEN_64];

            //Fields that are necessarily required
            tEdit.iSize = Marshal.SizeOf(tEdit);
            tEdit.iChanNo = m_iChannelNo;
            tEdit.tFaceLib.iSize = Marshal.SizeOf(tEdit.tFaceLib);
            tEdit.tFaceLib.iThreshold = trackBarLibThreshold.Value;
            tEdit.tFaceLib.iLibKey = 0;
            tEdit.tFaceLib.iAlarmType = comboBoxLibUpload.SelectedIndex;
            CommonFunction.BytesCopy(textBoxLibName.Text, tEdit.tFaceLib.cName);
            //end 

            //Fields that are not necessarily required
            CommonFunction.BytesCopy(textBoxLibDescrip.Text, tEdit.tFaceLib.cExtrInfo);
            //end

            //The normal devices do not need these fields
            CommonFunction.BytesCopy(textBoxLibUUID.Text, tEdit.tFaceLib.cLibUUID);
            CommonFunction.BytesCopy(textBoxLibVersion.Text, tEdit.tFaceLib.cLibVersion);
            tEdit.tFaceLib.iOptType = 1;	//1 Add,2 Modification
            //end

            IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceLibEdit)));//AllocCoTaskMem
            Marshal.StructureToPtr(tEdit, ipQueryInfo, true);//It is prone to memory leaks at false

            FaceReply tReply = new FaceReply();
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
            Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_LIB_EDIT, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tEdit), ipReply, Marshal.SizeOf(tReply));

            tReply = (FaceReply)Marshal.PtrToStructure(ipReply, typeof(FaceReply));

            if (0 == iRet && 0 == tReply.iResult)
            {
                UI_UpdataList();
            }
            else
            {
                MessageBox.Show("Face library added failed, return value:" + iRet.ToString() + ",Reply:" + tReply.iResult.ToString());
            }

            Marshal.FreeHGlobal(ipQueryInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipReply);//Free the allocated unmanaged memory
        }

        private void buttonLibModify_Click(object sender, EventArgs e)
        {
            if (listViewLib.SelectedItems.Count <= 0)
            {
                MessageBox.Show("Please select a record in the form first!");
                return;
            }
            int iIndex = listViewLib.SelectedItems[0].Index;

            FaceLibEdit tEdit = new FaceLibEdit();
            tEdit.tFaceLib = new FaceLibInfo();
            tEdit.tFaceLib.cName = new byte[CommonLen.LEN_64];
            tEdit.tFaceLib.cExtrInfo = new byte[CommonLen.LEN_64];
            tEdit.tFaceLib.cLibUUID = new byte[CommonLen.LEN_UUID];
            tEdit.tFaceLib.cLibVersion = new byte[CommonLen.LEN_64];

            tEdit.iSize = Marshal.SizeOf(tEdit);
            tEdit.iChanNo = m_iChannelNo;
            tEdit.tFaceLib.iSize = Marshal.SizeOf(tEdit.tFaceLib);
            tEdit.tFaceLib.iThreshold = trackBarLibThreshold.Value;
            tEdit.tFaceLib.iAlarmType = comboBoxLibUpload.SelectedIndex;
            tEdit.tFaceLib.iLibKey = int.Parse(listViewLib.FocusedItem.Text); //LibKey is greater than 0, imply modification
            CommonFunction.BytesCopy(textBoxLibName.Text, tEdit.tFaceLib.cName);

            //Fields that are not necessarily required
            CommonFunction.BytesCopy(textBoxLibDescrip.Text, tEdit.tFaceLib.cExtrInfo);
            //end

            //The normal devices do not need these fields
            CommonFunction.BytesCopy(textBoxLibUUID.Text, tEdit.tFaceLib.cLibUUID);
            CommonFunction.BytesCopy(textBoxLibVersion.Text, tEdit.tFaceLib.cLibVersion);
            tEdit.tFaceLib.iOptType = 2;	//1 Add,2 Modification
            //end

            IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tEdit));
            Marshal.StructureToPtr(tEdit, ipQueryInfo, true);//It is prone to memory leaks at false

            FaceReply tReply = new FaceReply();
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
            Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_LIB_EDIT, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tEdit), ipReply, Marshal.SizeOf(tReply));
            if (0 == iRet && 0 == tReply.iResult)
            {
                UI_UpdataList();
            }
            else
            {
                MessageBox.Show("Face library modification failed, return value:" + iRet.ToString() + ",Reply:" + tReply.iResult.ToString());
            }

            Marshal.FreeHGlobal(ipQueryInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipReply);//Free the allocated unmanaged memory
        }

        private void buttonLibDelete_Click(object sender, EventArgs e)
        {
            if (listViewLib.SelectedItems.Count <= 0)
            {
                MessageBox.Show("Please select a record in the form first!");
                return;
            }
            int iIndex = listViewLib.SelectedItems[0].Index;

            FaceLibDelete tInfo = new FaceLibDelete();
            tInfo.cLibUUID = new byte[CommonLen.LEN_UUID];

            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iChanNo = m_iChannelNo;
            tInfo.iLibKey = int.Parse(listViewLib.FocusedItem.Text);
            //The normal devices do not need these fields
            CommonFunction.BytesCopy(textBoxLibUUID.Text, tInfo.cLibUUID);
            //end

            IntPtr ipDeleteInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipDeleteInfo, true);//It is prone to memory leaks at false

            FaceReply tReply = new FaceReply();
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
            Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

            int iRet = -1;
            //Synchronous interface, face base map in the library more, the interface needs to wait a long time to return
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_LIB_DELETE, m_iChannelNo, ipDeleteInfo, Marshal.SizeOf(tInfo), ipReply, Marshal.SizeOf(tReply));

            if (0 == iRet && 0 == tReply.iResult)
            {
                UI_UpdataList();
            }
            else
            {
                MessageBox.Show("Face library deletion failed, return value:" + iRet.ToString() + ",Reply:" + tReply.iResult.ToString());
            }

            Marshal.FreeHGlobal(ipDeleteInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipReply);//Free the allocated unmanaged memory
        }

        private void UI_UpdataList()
        {
            listViewLib.Items.Clear();
            Thread.Sleep(20);//Delay is added to prevent query immediately after the operation

            FaceLibQuery tQuery = new FaceLibQuery();
            tQuery.iSize = Marshal.SizeOf(tQuery);
            tQuery.iChanNo = m_iChannelNo;
            tQuery.iPageNo = 0;
            tQuery.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT;

            IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tQuery));
            Marshal.StructureToPtr(tQuery, ipQueryInfo, true);//It is prone to memory leaks at false

            //Array of marshaling structures
            FaceLibQueryResult[] tResult = new FaceLibQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
            for (int i = 0; i < tResult.Length; i++)
            {
                tResult[i] = new FaceLibQueryResult();
            }
            IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceLibQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_LIB_QUERY, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(FaceLibQueryResult)));

            //Restore the structure array  
            for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            {
                IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceLibQueryResult)));
                tResult[i] = (FaceLibQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceLibQueryResult));
            }

            if (0 == iRet)
            {
                for (int iIdx = 0; iIdx < NVSSDK.FACE_MAX_PAGE_COUNT && iIdx < tResult[0].iPageCount; ++iIdx)
                {
                    if (tResult[iIdx].tFaceLib.iSize > 0)
                    {
                        UI_UpdateListItem(ref tResult[iIdx].tFaceLib, -1);
                    }
                }
            }
            else
            {
                MessageBox.Show("Face library query failed, return value:" + iRet.ToString());
            }

            Marshal.FreeHGlobal(ipQueryInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipResult);//Free the allocated unmanaged memory
        }

        private void UI_UpdateListItem(ref FaceLibInfo sLibInfo, int iLibIndex)
        {
            int iIndex = iLibIndex;
            if (-1 == iLibIndex)
            {
                iIndex = listViewLib.Items.Count;
            }

            ListViewItem one = new ListViewItem();
            one.Text = sLibInfo.iLibKey.ToString();//iLibKey
            one.SubItems.Add((iIndex + 1).ToString());//Serials
            one.SubItems.Add(CommonFunction.ByteToStr(sLibInfo.cName));//The library name
            one.SubItems.Add(sLibInfo.iThreshold.ToString());//Recognition threshold

            string strAlarmType = "Not upload";
            if (1 == sLibInfo.iAlarmType)
            {
                strAlarmType = "Upload";
            }
            one.SubItems.Add(strAlarmType);//Recognition information

            one.SubItems.Add(CommonFunction.ByteToStr(sLibInfo.cExtrInfo));//Describe 
            //The normal devices do not need these fields
            one.SubItems.Add(CommonFunction.ByteToStr(sLibInfo.cLibUUID));//Platform UUID
            one.SubItems.Add(CommonFunction.ByteToStr(sLibInfo.cLibVersion));//Platform version
            //end

            listViewLib.Items.Insert(iIndex, one);
        }

        private void listViewLib_MouseUp(object sender, MouseEventArgs e)
        {
            int iIndex = -1;
            if (listViewLib.SelectedItems.Count <= 0)
            {
                textBoxLibName.Text = "";
                trackBarLibThreshold.Value = 0;
                labelLibThresholdSize.Text = trackBarLibThreshold.Value.ToString();
                comboBoxLibUpload.SelectedIndex = 0;
                textBoxLibDescrip.Text = "";
                textBoxLibUUID.Text = "";
                textBoxLibVersion.Text = "";
            }
            else
            {
                iIndex = ((ListView)sender).SelectedIndices[0];

                ListViewItem one = new ListViewItem();
                one = listViewLib.Items[iIndex];
                textBoxLibName.Text = one.SubItems[LIB_LINE_INDEX.ITEM_LIB_NAME].Text;
                trackBarLibThreshold.Value = int.Parse(one.SubItems[LIB_LINE_INDEX.ITEM_LIB_VALUE].Text);
                labelLibThresholdSize.Text = trackBarLibThreshold.Value.ToString();
                if ("Upload" == one.SubItems[LIB_LINE_INDEX.ITEM_LIB_UPLOAD].Text)
                {
                    comboBoxLibUpload.SelectedIndex = 1;
                }
                else if ("Not upload" == one.SubItems[LIB_LINE_INDEX.ITEM_LIB_UPLOAD].Text)
                {
                    comboBoxLibUpload.SelectedIndex = 0;
                }
                textBoxLibDescrip.Text = one.SubItems[LIB_LINE_INDEX.ITEM_LIB_DESCRIP].Text;
                //The normal devices do not need these fields
                textBoxLibUUID.Text = one.SubItems[LIB_LINE_INDEX.ITEM_LIB_UUID].Text;
                textBoxLibVersion.Text = one.SubItems[LIB_LINE_INDEX.ITEM_LIB_VERSION].Text;
            }
        }

        private void trackBarLibThreshold_ValueChanged(object sender, EventArgs e)
        {
            labelLibThresholdSize.Text = ((TrackBar)sender).Value.ToString();
        }





        //Tab2: Face database module------------

        //private FaceQueryResult[] m_tFacePicInfo = new FaceQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
        //private FaceLibQueryResult[] m_tFaceLibInfo = new FaceLibQueryResult[2*NVSSDK.FACE_MAX_PAGE_COUNT];
        //private FaceQuery m_tQueryInfo;
        private FaceModeling m_tModelInfo = new FaceModeling();//The modeling information
        private int m_iCurPage;
        private int m_iTolalPage;

        //The column of face library list(listViewLib)corresponding to the index number
        public class PIC_LINE_INDEX
        {
            public const int ITEM_LIB_FACEKEY = 0;                //FaceKey-Hidden columns
            public const int ITEM_PIC_INDEX = 1;				//Serials
            public const int ITEM_PIC_NAME = 2;				//The face name
            public const int ITEM_PIC_SEX = 3;				//gender
            public const int ITEM_PIC_NATION = 4;				//nation
            public const int ITEM_PIC_BIRTH = 5;				//date of birth
            public const int ITEM_PIC_PLACE = 6;				//native place
            public const int ITEM_PIC_CARDTYPE = 7;				//Document type
            public const int ITEM_PIC_CARDNO = 8;				//ID number
            public const int ITEM_PIC_MODESTATE = 9;				//modeling status
            public const int ITEM_PIC_UUID = 10;			    //picture UUID
            public const int ITEM_LIB_LIBKEY = 11;			    //LibKey-Hidden column
            public const int ITEM_LIB_UUID = 12;			    //LibUUID-Hidden column
        }

        public string[] CONST_CSTR_MODE = new string[] { "Unknown", "Modeling Successful", "Modeling Failure", "Unmodeled" };
        public string[] CONST_CSTR_SEX = new string[] { "Unknown", "Man", "Woman" };
        public string[] CONST_CSTR_CARD = new string[] { "Unknown", "ID card", "Military ID card" };

        [System.Flags]
        enum EnumModeingType
        {
            MODING_TYPE_SINGLE = 0,//A single modeling
            MODING_TYPE_BATCH_UNMODELED = 1,//Unmodeled image modeling
            MODING_TYPE_BATCH_ALL,//All of picture modeling
            MODING_TYPE_CANCEL
        };

        private void UI_Init_TabPic()
        {
            comboBoxLibUpload.SelectedIndex = 0;
            comboBoxPicSex.SelectedIndex = 0;//gender
            comboBoxPicNation.SelectedIndex = 0;//nation
            comboBoxPicProvince.SelectedIndex = 0;//Provinces and Cites
            comboBoxPicCity.SelectedIndex = 0;//City
            comboBoxPicCardType.SelectedIndex = 0;//Document type
            comboBoxPicModel.SelectedIndex = 0;//modeling status
            comboBoxPicModelType.SelectedIndex = 0;//modeling type
        }

        private void tabControl1_MouseClick(object sender, MouseEventArgs e)
        {
            if (tabPage2.Name == ((TabControl)sender).SelectedTab.Name)//Tab selects the face Base map TAB to turn off the face detection algorithm
            {
                UI_EnbaleTabPic(false);
                if (DialogResult.OK == MessageBox.Show("Face map operation needs to pause the face recognition algorithm!", "Tips", MessageBoxButtons.OKCancel, MessageBoxIcon.Question))
                {
                    //Add faces, modeling need to pause intelligent analysis, otherwise it will fail
                    int iStatus = NVSSDK.VCA_SUSPEND_STATUS_PAUSE;
                    //IntPtr ipStatus = new IntPtr(iStatus);
                    IntPtr ipStatus = Marshal.AllocCoTaskMem(Marshal.SizeOf(typeof(int)));
                    Marshal.StructureToPtr(iStatus, ipStatus, true);//It is prone to memory leaks at false

                    NVSSDK.NetClient_SetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_VCA_SUSPEND, m_iChannelNo, ipStatus, Marshal.SizeOf(typeof(int)));

                    Marshal.FreeHGlobal(ipStatus);
                }
            }
            else//Resume face detection algorithm when Tab exits the face base map TAB
            {
                int iStatus = NVSSDK.VCA_SUSPEND_STATUS_RESUME;
                //IntPtr ipStatus = new IntPtr(iStatus);
                IntPtr ipStatus = Marshal.AllocCoTaskMem(Marshal.SizeOf(typeof(int)));
                Marshal.StructureToPtr(iStatus, ipStatus, true);//It is prone to memory leaks at false

                NVSSDK.NetClient_SetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_VCA_SUSPEND, m_iChannelNo, ipStatus, Marshal.SizeOf(typeof(int)));

                Marshal.FreeHGlobal(ipStatus);
            }
        }

        private void UI_EnbaleTabPic(bool _blEnbale)
        {
            buttonPicAdd.Enabled = _blEnbale;
            buttonPicModify.Enabled = _blEnbale;
            buttonPicDelete.Enabled = _blEnbale;
            comboBoxPicModelType.Enabled = _blEnbale;
            buttonPicModelStart.Enabled = _blEnbale;
            buttonPicModelStop.Enabled = _blEnbale;
            button1.Enabled = _blEnbale;//daoru
            button2.Enabled = _blEnbale;//daochu
        }

        //In addition to the query face base map, add, delete, change after the interface will be empty
        private void UI_Reset_TabPic()
        {
            comboBoxPicSex.SelectedIndex = 0;//gender
            comboBoxPicNation.SelectedIndex = 0;//nation
            comboBoxPicProvince.SelectedIndex = 0;//Provinces and Cites
            comboBoxPicCity.SelectedIndex = 0;//City
            comboBoxPicCardType.SelectedIndex = 0;//Document type
            comboBoxPicModel.SelectedIndex = 0;//modeling status

            textBoxPicCardNum.Text = "";
            textBoxPicName.Text = "";
        }

        private void buttonPicLibKeyQuery_Click(object sender, EventArgs e)
        {
            QueryLibkey(ref comboBoxPicLib);
        }

        private void QueryLibkey(ref ComboBox _cbo)
        {
            _cbo.DataSource = null;
            _cbo.Items.Clear();
            DataTable dt = new DataTable();
            dt.Columns.Add("Text", Type.GetType("System.String"));
            dt.Columns.Add("Value", Type.GetType("System.String"));
            _cbo.DataSource = dt;
            _cbo.DisplayMember = "Text";   // Text, that is, explicit text
            _cbo.ValueMember = "Value";    // Value, that is, real value

            FaceLibQuery tQuery = new FaceLibQuery();
            tQuery.iSize = Marshal.SizeOf(tQuery);
            tQuery.iChanNo = m_iChannelNo;
            tQuery.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT;

            int iRet = -1;

            int iPageNo = 0;
            while (true)
            {
                tQuery.iPageNo = iPageNo;

                IntPtr ipQuery = Marshal.AllocCoTaskMem(Marshal.SizeOf(tQuery));
                Marshal.StructureToPtr(tQuery, ipQuery, true);//It is prone to memory leaks at false

                //Array of marshaling structures
                FaceLibQueryResult[] tResult = new FaceLibQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
                for (int i = 0; i < tResult.Length; i++)
                {
                    tResult[i] = new FaceLibQueryResult();
                }
                IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceLibQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);
                //Marshal.StructureToPtr(tResult, ipResult, true);//It is prone to memory leaks at false

                iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_LIB_QUERY, m_iChannelNo, ipQuery, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(FaceLibQueryResult)));

                //Restore the structure array  
                for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
                {
                    IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceLibQueryResult)));
                    tResult[i] = (FaceLibQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceLibQueryResult));
                }

                //tResult = (FaceLibQueryResult)Marshal.PtrToStructure(ipResult, typeof(FaceLibQueryResult));

                if (0 == iRet)
                {
                    int iIdx;
                    for (iIdx = 0; iIdx < NVSSDK.FACE_MAX_PAGE_COUNT && iIdx < tResult[0].iPageCount; ++iIdx)
                    {
                        if (tResult[iIdx].tFaceLib.iSize > 0)
                        {
                            string strTemp = (tResult[iIdx].tFaceLib.iLibKey).ToString() + "," + CommonFunction.ByteToStr(tResult[iIdx].tFaceLib.cLibUUID);//stitching format: LibKey,LibUUID (separate with commas)
                            dt.Rows.Add(CommonFunction.ByteToStr(tResult[iIdx].tFaceLib.cName), strTemp);
                        }
                    }

                    if (iIdx > 0)
                    {
                        _cbo.SelectedIndex = 0;
                    }

                }
                else
                {
                    iRet = -1;
                    break;
                }

                //FaceLibQueryResult* pResult = m_tFaceLibInfo;
                //memcpy(pResult+iPageNo*FACE_MAX_PAGE_COUNT, &tResult, sizeof(FaceLibQueryResult)*tResult[0].iPageCount);

                int iPageCount = tResult[0].iTotal / NVSSDK.FACE_MAX_PAGE_COUNT;
                if (tResult[0].iTotal % NVSSDK.FACE_MAX_PAGE_COUNT > 0)
                {
                    iPageCount = iPageCount + 1;
                }

                iPageNo++;
                if (iPageNo >= iPageCount || iPageNo > 1)
                {
                    break;
                }
                Marshal.FreeHGlobal(ipQuery);//Free the allocated unmanaged memory
                Marshal.FreeHGlobal(ipResult);//Free the allocated unmanaged memory
            }

        }

        private void buttonPicPath_Click(object sender, EventArgs e)
        {
            OpenPicPath(ref textBoxPicPath);
        }

        private void OpenPicPath(ref TextBox _tBox)
        {
            OpenFileDialog openFileDialogPic = new OpenFileDialog();
            openFileDialogPic.InitialDirectory = "";//Initial directory, no value can also be assigned
            openFileDialogPic.Filter = "Picture file(*.jpg;*.jpeg;*.png)|*.jpg;*.jpeg;*.png";//file type

            if (openFileDialogPic.ShowDialog() == DialogResult.OK)//Pop-up selection box
            {
                _tBox.Text = openFileDialogPic.FileName;
            }
        }


        //_iIndex=0 return LibKey's value; _iIndex=1 return UUID's value
        private string SplitLibKeyAndUUID(string _strSource, int _iIndex)
        {
            string[] strArray = _strSource.Split(',');
            int iLenth = strArray.Length;
            if (iLenth <= _iIndex) //In exceptional cases, the empty string is returned to avoid crashes
            {
                return "";
            }
            return strArray[_iIndex];
        }

        private void buttonPicQuery_Click(object sender, EventArgs e)
        {
            int iLibKeySel = comboBoxPicLib.SelectedIndex;
            if (iLibKeySel < 0)
            {
                MessageBox.Show("Please check or select the face library first!");
                return;
            }

            m_iCurPage = 0;
            m_iTolalPage = 0;
            labelPicPage.Text = "";

            UI_ShowPicPage(m_iCurPage, true);
        }

        private void buttonPicAdd_Click(object sender, EventArgs e)
        {
            int iLibKeySel = comboBoxPicLib.SelectedIndex;
            if (iLibKeySel < 0)
            {
                MessageBox.Show("Please check or select the face library first!");
                return;
            }

            FaceEdit tInfo = new FaceEdit();
            tInfo.tFace = new FaceInfo();
            tInfo.tFace.cName = new byte[CommonLen.LEN_64];
            tInfo.cFacePic = new byte[CommonLen.LEN_256];
            tInfo.tFace.cBirthTime = new byte[CommonLen.LEN_16];
            tInfo.tFace.cCertNum = new byte[CommonLen.LEN_64];
            tInfo.tFace.cLibUUID = new byte[CommonLen.LEN_UUID];
            tInfo.tFace.cFaceUUID = new byte[CommonLen.LEN_UUID];
            tInfo.tFace.cLibVersion = new byte[CommonLen.LEN_64];
            tInfo.tFace.cVerifyCode = new byte[CommonLen.LEN_64];
            tInfo.tFace.cFileName = new byte[CommonLen.LEN_256];

            //necessary field
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iChanNo = m_iChannelNo;
            string strTemp = comboBoxPicLib.SelectedValue.ToString();
            tInfo.tFace.iLibKey = int.Parse(SplitLibKeyAndUUID(strTemp, 0));
            tInfo.tFace.iModeling = 1;		//This is temporarily dead and defaults to modeling. Field description: Add: 0 not modeled, 1 modeled, query: 0- unknown, 1- Modeled successful, 2- Modeled failed, 3- Not modeled
            tInfo.tFace.iFaceKey = 0;		//0 imply to add
            CommonFunction.BytesCopy(textBoxPicName.Text, tInfo.tFace.cName);
            CommonFunction.BytesCopy(textBoxPicPath.Text, tInfo.cFacePic);
            CommonFunction.BytesCopy(dateTimePickerPicEnd.Text, tInfo.tFace.cBirthTime);
            //end

            //unnecessary field
            tInfo.tFace.iSex = comboBoxPicSex.SelectedIndex;
            tInfo.tFace.iNation = comboBoxPicNation.SelectedIndex;//0 is unknown. Only one value is "unknown". In practice, add the value as required
            tInfo.tFace.iPlace = (comboBoxPicCity.SelectedIndex & 0xffff) | ((comboBoxPicProvince.SelectedIndex & 0xffff) << 16);//City and province need joint by bit, there is only one value "unknown" for the time being. The actual use is to add the value as required
            tInfo.tFace.iCertType = comboBoxPicCardType.SelectedIndex;
            CommonFunction.BytesCopy(textBoxPicCardNum.Text, tInfo.tFace.cCertNum);
            //end

            //The normal devices do not need these fields
            CommonFunction.BytesCopy((SplitLibKeyAndUUID(strTemp, 1)), tInfo.tFace.cLibUUID);
            CommonFunction.BytesCopy(textBoxPicUUID.Text, tInfo.tFace.cFaceUUID);
            tInfo.tFace.iOptType = 1;	//1 Add 2 Modification
            //end

            if ("" == textBoxPicName.Text)
            {
                //Face name is empty, return directly
                MessageBox.Show("Face basemap addition failed, face name can not be empty!");
                textBoxPicName.Focus();
                return;
            }

            if ("" == textBoxPicPath.Text)
            {
                //Face picture is empty, return directly
                MessageBox.Show("Face basemap addition failed, please select a face picture!");
                textBoxPicPath.Focus();
                return;
            }

            IntPtr ipEditInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));//AllocCoTaskMem
            Marshal.StructureToPtr(tInfo, ipEditInfo, true);//It is prone to memory leaks at false

            FaceReply tReply = new FaceReply();
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
            Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_EDIT, m_iChannelNo, ipEditInfo, Marshal.SizeOf(tInfo), ipReply, Marshal.SizeOf(tReply));

            tReply = (FaceReply)Marshal.PtrToStructure(ipReply, typeof(FaceReply));

            if (0 == iRet && 0 == tReply.iResult)
            {
                UI_ShowPicPage(m_iCurPage, false);
            }
            else
            {
                MessageBox.Show("Face basemap addition failed, return value:" + iRet.ToString() + ",Reply:" + (tReply.iResult).ToString());
            }

            Marshal.FreeHGlobal(ipEditInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipReply);//Free the allocated unmanaged memory
        }

        private void buttonPicModify_Click(object sender, EventArgs e)
        {
            if (listViewPicInfo.SelectedItems.Count <= 0)
            {
                MessageBox.Show("Please select a record in the form first!");
                return;
            }

            int iIndex = listViewPicInfo.SelectedItems[0].Index;
            ListViewItem one = new ListViewItem();
            one = listViewPicInfo.Items[iIndex];

            FaceEdit tInfo = new FaceEdit();
            tInfo.tFace = new FaceInfo();
            tInfo.tFace.cName = new byte[CommonLen.LEN_64];
            tInfo.cFacePic = new byte[CommonLen.LEN_256];
            tInfo.tFace.cBirthTime = new byte[CommonLen.LEN_16];
            tInfo.tFace.cCertNum = new byte[CommonLen.LEN_64];
            tInfo.tFace.cLibUUID = new byte[CommonLen.LEN_UUID];
            tInfo.tFace.cFaceUUID = new byte[CommonLen.LEN_UUID];
            tInfo.tFace.cLibVersion = new byte[CommonLen.LEN_64];
            tInfo.tFace.cVerifyCode = new byte[CommonLen.LEN_64];
            tInfo.tFace.cFileName = new byte[CommonLen.LEN_256];

            //necessary field
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iChanNo = m_iChannelNo; //Channel, 0 indicates the first cahannel, IPC has only one channel
            tInfo.tFace.iLibKey = int.Parse(one.SubItems[PIC_LINE_INDEX.ITEM_LIB_LIBKEY].Text);	//face database key value, gain from listViewPicInfo's hidden column
            tInfo.tFace.iFaceKey = int.Parse(one.Text);//face base map's key value, gain from listViewPicInfo's hidden column
            CommonFunction.BytesCopy(textBoxPicName.Text, tInfo.tFace.cName);
            CommonFunction.BytesCopy(dateTimePickerPicEnd.Text, tInfo.tFace.cBirthTime);
            //end

            //unnecessary field
            tInfo.tFace.iSex = comboBoxPicSex.SelectedIndex;			//gender, 0 unkow, 1 male , 2 female
            tInfo.tFace.iNation = comboBoxPicNation.SelectedIndex;		//nation, 0 unkow, There is only one value "unknown" for the time being. The actual use is to add the value as required	
            tInfo.tFace.iPlace = (comboBoxPicCity.SelectedIndex & 0xffff) | ((comboBoxPicProvince.SelectedIndex & 0xffff) << 16);//There is only one value "unknown" for the time being. The actual use is to add the value as required	
            tInfo.tFace.iCertType = comboBoxPicCardType.SelectedIndex;		//Document type, 0 unkonw, 1 2nd-generation ID card, 2 certificate of officers
            CommonFunction.BytesCopy(textBoxPicCardNum.Text, tInfo.tFace.cCertNum);//ID number
            //end

            //The normal devices do not need these fields
            tInfo.tFace.iOptType = 2;		//1 Add 2 Modification
            //end

            if ("" == textBoxPicName.Text)
            {
                //Face name is empty, return directly
                MessageBox.Show("Face map modification failed, face name can not be empty!");
                textBoxPicName.Focus();
                return;
            }

            IntPtr ipEditInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipEditInfo, true);//It is prone to memory leaks at false

            FaceReply tReply = new FaceReply();
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
            Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_EDIT, m_iChannelNo, ipEditInfo, Marshal.SizeOf(tInfo), ipReply, Marshal.SizeOf(tReply));

            tReply = (FaceReply)Marshal.PtrToStructure(ipReply, typeof(FaceReply));

            if (0 == iRet && 0 == tReply.iResult)
            {
                UI_ShowPicPage(m_iCurPage, false);
            }
            else
            {
                MessageBox.Show("Face basemap modification failed, return value:" + iRet.ToString() + ",Reply:" + (tReply.iResult).ToString());
            }

            Marshal.FreeHGlobal(ipEditInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipReply);//Free the allocated unmanaged memory
        }

        private void buttonPicDelete_Click(object sender, EventArgs e)
        {
            if (listViewPicInfo.SelectedItems.Count <= 0)
            {
                MessageBox.Show("Please select a record in the form first!");
                return;
            }

            int iIndex = listViewPicInfo.SelectedItems[0].Index;
            ListViewItem one = new ListViewItem();
            one = listViewPicInfo.Items[iIndex];

            FaceDelete tInfo = new FaceDelete();
            tInfo.cLibUUID = new byte[CommonLen.LEN_UUID];
            tInfo.cFaceUUID = new byte[CommonLen.LEN_UUID];

            //necessary field
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iChanNo = m_iChannelNo;
            tInfo.iLibKey = int.Parse(one.SubItems[PIC_LINE_INDEX.ITEM_LIB_LIBKEY].Text);	//face database's key value, gain from listViewPicInfo's hidden column
            tInfo.iFaceKey = int.Parse(one.Text);//face base map's key value, gain from listViewPicInfo's hidden column
            //end

            //The normal devices do not need these fields
            CommonFunction.BytesCopy((one.SubItems[PIC_LINE_INDEX.ITEM_LIB_UUID].Text), tInfo.cLibUUID);//gain from listViewPicInfo's hidden column
            CommonFunction.BytesCopy((one.SubItems[PIC_LINE_INDEX.ITEM_PIC_UUID].Text), tInfo.cFaceUUID);
            //end

            IntPtr ipDeleteInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipDeleteInfo, true);//It is prone to memory leaks at false

            FaceReply tReply = new FaceReply();
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
            Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_DELETE, m_iChannelNo, ipDeleteInfo, Marshal.SizeOf(tInfo), ipReply, Marshal.SizeOf(tReply));

            tReply = (FaceReply)Marshal.PtrToStructure(ipReply, typeof(FaceReply));

            if (0 == iRet && 0 == tReply.iResult)
            {
                UI_ShowPicPage(m_iCurPage, false);
            }
            else
            {
                MessageBox.Show("Face basemap deletion failed, return value:" + iRet.ToString() + ",Reply:" + (tReply.iResult).ToString());
            }

            Marshal.FreeHGlobal(ipDeleteInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipReply);//Free the allocated unmanaged memory
        }


        private void UI_ShowPicPage(int _iPageNo, bool _blSerach)
        {
            if ((m_iTolalPage > 0 && _iPageNo >= m_iTolalPage) || _iPageNo < 0)
            {
                return;
            }

            if (!_blSerach)
            {
                //Except the operation of query, other operation need clean up content of interface's controller
                UI_Reset_TabPic();
            }

            FaceQuery tQuery = new FaceQuery();
            tQuery.cBirthStart = new byte[CommonLen.LEN_16];
            tQuery.cBirthEnd = new byte[CommonLen.LEN_16];
            tQuery.cName = new byte[CommonLen.LEN_64];
            tQuery.cCertNum = new byte[CommonLen.LEN_64];
            tQuery.cLibUUID = new byte[CommonLen.LEN_UUID];

            //necessary field
            tQuery.iSize = Marshal.SizeOf(tQuery);
            tQuery.iChanNo = m_iChannelNo;
            string strTemp = comboBoxPicLib.SelectedValue.ToString();
            tQuery.iLibKey = int.Parse(SplitLibKeyAndUUID(strTemp, 0));
            tQuery.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT; ;//
            CommonFunction.BytesCopy(dateTimePickerPicBegin.Text, tQuery.cBirthStart);
            CommonFunction.BytesCopy(dateTimePickerPicEnd.Text, tQuery.cBirthEnd);
            //end

            //unnecessary field
            tQuery.iSex = comboBoxPicSex.SelectedIndex;
            tQuery.iNation = comboBoxPicNation.SelectedIndex;//nation, 0 unkonw, There is only one value "unknown" for the time being. The actual use is to add the value as required	
            tQuery.iPlace = (comboBoxPicCity.SelectedIndex & 0xffff) | ((comboBoxPicProvince.SelectedIndex & 0xffff) << 16);//City and province need joint by bit, there is only one value "unknown" for the time being. The actual use is to add the value as required	
            tQuery.iCertType = comboBoxPicCardType.SelectedIndex;
            tQuery.iModeling = comboBoxPicModel.SelectedIndex;
            CommonFunction.BytesCopy(textBoxPicName.Text, tQuery.cName);
            CommonFunction.BytesCopy(textBoxPicCardNum.Text, tQuery.cCertNum);
            //end

            //The normal devices do not need these fields
            CommonFunction.BytesCopy((SplitLibKeyAndUUID(strTemp, 1)), tQuery.cLibUUID);
            //end

            listViewPicInfo.Items.Clear();

            tQuery.iPageNo = _iPageNo;

            IntPtr ipQueryInfo = Marshal.AllocCoTaskMem(Marshal.SizeOf(tQuery));
            Marshal.StructureToPtr(tQuery, ipQueryInfo, true);//It is prone to memory leaks at false

            IntPtr ipResult = IntPtr.Zero;
            //Array of marshaling structures
            FaceQueryResult[] tFacePicInfo = new FaceQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
            for (int i = 0; i < tFacePicInfo.Length; i++)
            {
                tFacePicInfo[i] = new FaceQueryResult();
            }
            ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);
            //Marshal.StructureToPtr(tFacePicInfo, ipResult, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_QUERY, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(FaceQueryResult)));

            //Restore the structure array  
            for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            {
                IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceQueryResult)));
                tFacePicInfo[i] = (FaceQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceQueryResult));
            }

            //tFacePicInfo = (FaceQueryResult)Marshal.PtrToStructure(ipFacePicInfo, typeof(FaceQueryResult));

            if (0 != iRet)
            {
                MessageBox.Show("The face bwasemap query failed, return value:" + iRet.ToString());
                return;
            }

            //page process
            int iTotalPage = tFacePicInfo[0].iTotal / NVSSDK.FACE_MAX_PAGE_COUNT;
            if (tFacePicInfo[0].iTotal % NVSSDK.FACE_MAX_PAGE_COUNT > 0 && tFacePicInfo[0].iTotal > 0)
            {
                iTotalPage++;
            }

            if (iTotalPage != m_iTolalPage)
            {
                m_iTolalPage = iTotalPage;
                comboBoxPicPage.Items.Clear();
                for (int i = 0; i < m_iTolalPage; ++i)
                {
                    comboBoxPicPage.Items.Insert(i, (i + 1).ToString());
                }
            }
            m_iCurPage = _iPageNo;

            if (comboBoxPicPage.Items.Count > 0)
            {
                comboBoxPicPage.SelectedIndex = m_iCurPage;
            }

            if (tFacePicInfo[0].iTotal > 0)
            {
                _iPageNo++;
            }
            labelPicPage.Text = _iPageNo.ToString() + "/" + m_iTolalPage.ToString();

            for (int i = 0; i < tFacePicInfo[0].iPageCount; ++i)
            {
                UI_UpdateFaceList(ref listViewPicInfo, ref tFacePicInfo[i].tFace, -1);
            }

            Marshal.FreeHGlobal(ipQueryInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipResult);//Free the allocated unmanaged memory

        }

        private void UI_UpdateFaceList(ref ListView _lst, ref FaceInfo _tInfo, int _iLibIndex)
        {
            int iIndex = _iLibIndex;
            if (-1 == iIndex)
            {
                iIndex = _lst.Items.Count;
            }

            ListViewItem one = new ListViewItem();
            one.Text = _tInfo.iFaceKey.ToString();//iFaceKey  Inorder to gain data, this column hide in the interface
            one.SubItems.Add((iIndex + 1).ToString());//Serials
            one.SubItems.Add(CommonFunction.ByteToStr(_tInfo.cName));//face name
            one.SubItems.Add(CONST_CSTR_SEX[_tInfo.iSex]);//gender
            one.SubItems.Add("Unknown");//nation
            one.SubItems.Add(CommonFunction.ByteToStr(_tInfo.cBirthTime));//date of birth
            one.SubItems.Add("Unknown");//native place
            one.SubItems.Add(CONST_CSTR_CARD[_tInfo.iCertType]);//Document type
            one.SubItems.Add(CommonFunction.ByteToStr(_tInfo.cCertNum));//ID card
            one.SubItems.Add(CONST_CSTR_MODE[_tInfo.iModeling + 1]);//modeling status
            //normal device don't have this
            one.SubItems.Add(CommonFunction.ByteToStr(_tInfo.cFaceUUID));//picture UUID
            //end

            one.SubItems.Add((_tInfo.iLibKey).ToString());//iLibKey  Inorder to gain data, this column hide in the interface
            one.SubItems.Add(CommonFunction.ByteToStr(_tInfo.cLibUUID));//cLibUUID Inorder to gain data, this column hide in the interface

            _lst.Items.Insert(iIndex, one);

        }

        private void listViewPicInfo_MouseUp(object sender, MouseEventArgs e)
        {
            int iIndex = -1;
            if (listViewPicInfo.SelectedItems.Count <= 0)
            {
                textBoxPicName.Text = "";
                comboBoxPicSex.SelectedIndex = 0;
                comboBoxPicNation.SelectedIndex = 0;
                comboBoxPicCardType.SelectedIndex = 0;
                textBoxPicCardNum.Text = "";
                textBoxPicUUID.Text = "";
                comboBoxPicModel.SelectedIndex = 0;
                comboBoxPicProvince.SelectedIndex = 0;
                comboBoxPicCity.SelectedIndex = 0;
            }
            else
            {
                iIndex = ((ListView)sender).SelectedIndices[0];

                ListViewItem one = new ListViewItem();
                one = listViewPicInfo.Items[iIndex];
                textBoxPicName.Text = one.SubItems[PIC_LINE_INDEX.ITEM_PIC_NAME].Text;
                comboBoxPicSex.SelectedIndex = Array.IndexOf(CONST_CSTR_SEX, one.SubItems[PIC_LINE_INDEX.ITEM_PIC_SEX].Text); ;
                comboBoxPicNation.SelectedIndex = 0;//Temporarily write death to 0, the actual use of the actual value to update
                comboBoxPicCardType.SelectedIndex = Array.IndexOf(CONST_CSTR_CARD, one.SubItems[PIC_LINE_INDEX.ITEM_PIC_CARDTYPE].Text);
                textBoxPicCardNum.Text = one.SubItems[PIC_LINE_INDEX.ITEM_PIC_CARDNO].Text;
                textBoxPicUUID.Text = one.SubItems[PIC_LINE_INDEX.ITEM_PIC_UUID].Text;
                int iPointer = Array.IndexOf(CONST_CSTR_MODE, one.SubItems[PIC_LINE_INDEX.ITEM_PIC_MODESTATE].Text);
                comboBoxPicModel.SelectedIndex = iPointer;
                comboBoxPicProvince.SelectedIndex = 0;//Temporarily write death to 0, the actual use of the actual value to update
                comboBoxPicCity.SelectedIndex = 0;//Temporarily write death to 0, the actual use of the actual value to update        
            }
        }

        //home page   previous page   next page   end page   jump page 
        private void buttonPicHomePage_Click(object sender, EventArgs e)
        {
            if (listViewPicInfo.Items.Count <= 0)
            {
                return;
            }

            UI_ShowPicPage(0, false);
        }

        private void buttonPicLastPage_Click(object sender, EventArgs e)
        {
            if (listViewPicInfo.Items.Count <= 0)
            {
                return;
            }

            UI_ShowPicPage(m_iCurPage - 1, false);
        }

        private void buttonPicNextPage_Click(object sender, EventArgs e)
        {
            if (listViewPicInfo.Items.Count <= 0)
            {
                return;
            }

            UI_ShowPicPage(m_iCurPage + 1, false);
        }

        private void buttonPicEndPage_Click(object sender, EventArgs e)
        {
            if (listViewPicInfo.Items.Count <= 0)
            {
                return;
            }

            UI_ShowPicPage(m_iTolalPage - 1, false);
        }

        private void comboBoxPicPage_SelectionChangeCommitted(object sender, EventArgs e)
        {
            if (listViewPicInfo.Items.Count <= 0)
            {
                return;
            }

            UI_ShowPicPage(comboBoxPicPage.SelectedIndex, false);
        }

        private void buttonPicModelStart_Click(object sender, EventArgs e)
        {
            labelPicModelProcess.Text = "";

            FaceModeling tInfo = new FaceModeling();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iChanNo = m_iChannelNo;
            tInfo.iType = comboBoxPicModelType.SelectedIndex;

            if ((int)EnumModeingType.MODING_TYPE_SINGLE == tInfo.iType)//Single picture modeling
            {
                //get the selected line number
                if (listViewPicInfo.SelectedItems.Count <= 0)
                {
                    MessageBox.Show("Please select a record in the form first!");
                    return;
                }

                int iIndex = listViewPicInfo.SelectedItems[0].Index;
                ListViewItem one = new ListViewItem();
                one = listViewPicInfo.Items[iIndex];

                tInfo.iLibKey = int.Parse(one.SubItems[PIC_LINE_INDEX.ITEM_LIB_LIBKEY].Text);	//face database's key value, gain from listViewPicInfo's hidden column              
                //Face key can have a maximum of 20, this table for the single selection can not be selected, so only 1 processing
                tInfo.iFaceNum = 1;
                tInfo.iFaceKey[0] = int.Parse(one.Text);//FaceKey store at the first column of ListView
            }
            else
            {
                //Batch modeling 
                if (listViewPicInfo.Items.Count > 0)
                {
                    ListViewItem one = new ListViewItem();
                    one = listViewPicInfo.Items[0];
                    tInfo.iLibKey = int.Parse(one.SubItems[PIC_LINE_INDEX.ITEM_LIB_LIBKEY].Text);	//face database's key value, gain from listViewPicInfo's hidden column
                }
                else
                {
                    MessageBox.Show("Please check the face map first!");
                }
            }

            IntPtr ipModeling = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipModeling, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_MODEL, m_iChannelNo, ipModeling, Marshal.SizeOf(tInfo), IntPtr.Zero, 0);

            tInfo = (FaceModeling)Marshal.PtrToStructure(ipModeling, typeof(FaceModeling));

            if (0 == iRet)
            {
                //Need save the modeling information after stopping
                m_tModelInfo.iSize = tInfo.iSize;
                m_tModelInfo.iChanNo = tInfo.iChanNo;
                m_tModelInfo.iType = tInfo.iType;
                m_tModelInfo.iLibKey = tInfo.iLibKey;
                m_tModelInfo.iFaceNum = tInfo.iFaceNum;

                Array.Copy(tInfo.iFaceKey, m_tModelInfo.iFaceKey, tInfo.iFaceKey.Length);
            }

            Marshal.FreeHGlobal(ipModeling);//Free the allocated unmanaged memory
        }

        private void buttonPicModelStop_Click(object sender, EventArgs e)
        {
            //Pull out the information you're modeling, and then empty it 
            FaceModeling tInfo = new FaceModeling();
            tInfo.iFaceKey = new int[NVSSDK.FACE_MAX_PAGE_COUNT];

            tInfo.iSize = m_tModelInfo.iSize;
            tInfo.iChanNo = m_tModelInfo.iChanNo;
            tInfo.iType = m_tModelInfo.iType;
            tInfo.iLibKey = m_tModelInfo.iLibKey;
            tInfo.iFaceNum = m_tModelInfo.iFaceNum;
            Array.Copy(m_tModelInfo.iFaceKey, tInfo.iFaceKey, m_tModelInfo.iFaceKey.Length);

            m_tModelInfo.iSize = 0;
            m_tModelInfo.iChanNo = 0;
            m_tModelInfo.iType = 0;
            m_tModelInfo.iLibKey = 0;
            m_tModelInfo.iFaceNum = 0;
            Array.Clear(m_tModelInfo.iFaceKey, 0, (m_tModelInfo.iFaceKey).Length);

            //No modeling, no stopping 
            if (tInfo.iSize <= 0)
            {
                return;
            }

            tInfo.iType = (int)EnumModeingType.MODING_TYPE_CANCEL;

            IntPtr ipModeling = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipModeling, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_MODEL, m_iChannelNo, ipModeling, Marshal.SizeOf(tInfo), IntPtr.Zero, 0);

            Marshal.FreeHGlobal(ipModeling);//Free the allocated unmanaged memory       
        }





        //Tab3: face picture stream module------------
        const uint CONST_INVALID_RECV_ID = 0xffffffff;
        private static uint g_uiRecvID = CONST_INVALID_RECV_ID;//Connection ID of receiving the stream of picture
        private static NetPicPara g_tNetPicPara = new NetPicPara();
        private static int g_iCount = 0;//Total number of images received

        private void UI_Init_TabStream()
        {
            string strPath = Application.StartupPath;
            textBoxStreamPath.Text = strPath + "\\FacePicStream";
        }

        private void buttonStreamPath_Click(object sender, EventArgs e)
        {
            FolderBrowserDialog folderBrowserDialogStreamPath = new FolderBrowserDialog();
            folderBrowserDialogStreamPath.Description = "Please select a folder";

            //Display picture and save dialog box
            if (folderBrowserDialogStreamPath.ShowDialog() == DialogResult.OK || folderBrowserDialogStreamPath.ShowDialog() == DialogResult.Yes)
            {
                string strNewPath = folderBrowserDialogStreamPath.SelectedPath;
                textBoxStreamPath.Text = strNewPath;
            }
        }

        private void buttonStreamStartRecv_Click(object sender, EventArgs e)
        {
            if (CONST_INVALID_RECV_ID != g_uiRecvID)
            {
                //Image stream has been enabled, cannot be enabled again
                MessageBox.Show("The received picture stream has been turned on and cannot be turned on again.");
                return;
            }

            buttonStreamStartRecv.Enabled = false;

            if (!Directory.Exists(textBoxStreamPath.Text))
            {
                Directory.CreateDirectory(textBoxStreamPath.Text);
            }

            IntPtr ptNetPicPara = IntPtr.Zero;
            try
            {
                //Enabled image stream
                g_tNetPicPara.iStructLen = Marshal.SizeOf(g_tNetPicPara);
                g_tNetPicPara.iChannelNo = 0;
                g_tNetPicPara.cbkPicStreamNotify = MyNetPicStreamNotify;
                g_tNetPicPara.pvUser = this.Handle;
                g_tNetPicPara.iPicType = 0;

                ptNetPicPara = Marshal.AllocHGlobal(Marshal.SizeOf(g_tNetPicPara));
                Marshal.StructureToPtr(g_tNetPicPara, ptNetPicPara, true);

                int iRet = NVSSDK.NetClient_StartRecvNetPicStream(m_iLogonId, ptNetPicPara, Marshal.SizeOf(g_tNetPicPara), ref g_uiRecvID);
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

        //Image stream processing callback
        private static int MyNetPicStreamNotify(UInt32 _uiRecvID, int _lCommand, IntPtr _pvCallBackInfo, Int32 _BufLen, IntPtr _iUser)
        {
            if (null == _pvCallBackInfo)
            {
                return -1;
            }

            if (_uiRecvID != g_uiRecvID)
            {
                return -1;
            }

            if (NVSSDK.NET_PICSTREAM_CMD_FACE == _lCommand)
            {
                IntPtr ptVca = _pvCallBackInfo;
                FacePicStream tFacePicStream = (FacePicStream)Marshal.PtrToStructure(ptVca, typeof(FacePicStream));
                FileStream pfFullPic = null;
                PicData tFullPicData = (PicData)Marshal.PtrToStructure(tFacePicStream.tFullData, typeof(PicData));
                PicTime tTime = tFullPicData.tPicTime;
                DateTime tDataTime = DateTime.Now;//Let's initialize it to the current time so it doesn't crash when the time doesn't make sense
                if (tFullPicData.iDataLen > 0)
                {
                    tDataTime = new DateTime((int)tTime.uiYear, (int)tTime.uiMonth, (int)tTime.uiDay,
    (int)tTime.uiHour, (int)tTime.uiMinute, (int)tTime.uiSecondsr, (int)tTime.uiMilliseconds);
                }

                //paorama
                try
                {
                    if (tFullPicData.iDataLen > 0)
                    {
                        string strFullPicName = ".\\FacePicStream\\FullPic-No" + (g_iCount++) + "-Time" + tDataTime.ToString("20yyMMddhhmmss") + ".jpg";
                        Console.WriteLine(strFullPicName);
                        pfFullPic = new FileStream(strFullPicName, FileMode.Create);
                        if (null != pfFullPic)
                        {
                            byte[] btFullPicData = new byte[tFullPicData.iDataLen];
                            Marshal.Copy(tFullPicData.piPicData, btFullPicData, 0, tFullPicData.iDataLen);//it can be used in c# after copy unmanaged memory to managed memory    
                            pfFullPic.Write(btFullPicData, 0, tFullPicData.iDataLen);
                        };
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

                //Face small map and face base map
                for (int i = 0; i < tFacePicStream.iFaceCount; ++i)
                {
                    FacePicData tFacePicData = (FacePicData)Marshal.PtrToStructure(tFacePicStream.tFaceData[i], typeof(FacePicData));

                    FileStream pfFaceFile = null;
                    try
                    {
                        if (tFacePicData.iDataLen > 0)
                        {
                            //Face small map
                            string strFacePicName = ".\\FacePicStream\\FacePic-No" + (g_iCount++) + "-Time" + tDataTime.ToString("20yyMMddhhmmss") + ".jpg";
                            pfFaceFile = new FileStream(strFacePicName, FileMode.Create);
                            if (null != pfFaceFile)
                            {
                                byte[] btFacePicData = new byte[tFacePicData.iDataLen];
                                Marshal.Copy(tFacePicData.pPicData, btFacePicData, 0, tFacePicData.iDataLen);//it can be used in c# after copy unmanaged memory to managed memory 
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

                    //face base map
                    if (1 == tFacePicData.iAlramType)	//face base map is exist
                    {
                        FileStream pfNegFile = null;
                        try
                        {
                            if (tFacePicData.iNegPicLen > 0)
                            {
                                string strNegPicName = ".\\FacePicStream\\NegPic-No" + (g_iCount++) + "-Time" + tDataTime.ToString("20yyMMddhhmmss") + "Similarity-" + (tFacePicData.iSimilatity).ToString() + ".jpg";
                                pfNegFile = new FileStream(strNegPicName, FileMode.Create);
                                if (null != pfNegFile)
                                {
                                    byte[] btNegPicData = new byte[tFacePicData.iNegPicLen];
                                    Marshal.Copy(tFacePicData.pcNegPicData, btNegPicData, 0, tFacePicData.iNegPicLen);//it can be used in c# after copy unmanaged memory to managed memory
                                    pfNegFile.Write(btNegPicData, 0, tFacePicData.iNegPicLen);
                                }

                            }
                        }
                        catch (IOException e)
                        {
                            Console.WriteLine(e.Message);
                        }
                        finally
                        {
                            if (null != pfNegFile)
                            {
                                pfNegFile.Close();
                            }
                        }
                    }
                }

                Win32API.PostMessage(_iUser, ClientControlMsg.WM_CLIENT_RECVPICNUM, 0, 0);
            }

            return 1;
        }

        private void buttonStreamStopRecv_Click(object sender, EventArgs e)
        {
            if (CONST_INVALID_RECV_ID != g_uiRecvID)
            {
                //stop picture stream
                int iRet = NVSSDK.NetClient_StopRecvNetPicStream(g_uiRecvID);
                g_uiRecvID = CONST_INVALID_RECV_ID;
                buttonStreamStartRecv.Enabled = true;
            }
        }






        //Tab4: face defense module ------------

        private void UI_Init_TabSchedule()
        {
            comboBoxScheduleArith.DataSource = null;
            comboBoxScheduleArith.Items.Clear();
            DataTable dt = new DataTable();
            dt.Columns.Add("Text", Type.GetType("System.String"));
            dt.Columns.Add("Value", Type.GetType("System.Int32"));
            comboBoxScheduleArith.DataSource = dt;
            comboBoxScheduleArith.DisplayMember = "Text";   // Text, that is, dislpayed text
            comboBoxScheduleArith.ValueMember = "Value";    // Value, that is, real value 
            dt.Rows.Add("IPC Face recognition", NVSSDK.ALARM_TYPE_FACE_IDENT);
            dt.Rows.Add("NVR Face recognition", NVSSDK.ALARM_TYPE_NVR_VCA);
            comboBoxScheduleArith.SelectedIndex = 0;

            comboBoxSubType.DataSource = null;
            comboBoxSubType.Items.Clear();
            DataTable dt1 = new DataTable();
            dt1.Columns.Add("Text", Type.GetType("System.String"));
            dt1.Columns.Add("Value", Type.GetType("System.Int32"));
            comboBoxSubType.DataSource = dt1;
            comboBoxSubType.DisplayMember = "Text";   // Text, that is, dislpayed text
            comboBoxSubType.ValueMember = "Value";    // Valuethat is, real value 
            dt1.Rows.Add("Black List", 0);
            dt1.Rows.Add("White List", 1);
            comboBoxSubType.SelectedIndex = 0;

            comboBoxScheduleSet.SelectedIndex = 0;
        }

        private void buttonScheduleSearch_Click(object sender, EventArgs e)
        {
            QueryLibkey(ref comboBoxScheduleLib);
        }

        private void checkBoxSchedule1_CheckedChanged(object sender, EventArgs e)
        {
            dateTimePickerScheduleBegin1.Enabled = checkBoxSchedule1.Checked;
            dateTimePickerScheduleEnd1.Enabled = checkBoxSchedule1.Checked;
        }

        private void checkBoxSchedule2_CheckedChanged(object sender, EventArgs e)
        {
            dateTimePickerScheduleBegin2.Enabled = checkBoxSchedule2.Checked;
            dateTimePickerScheduleEnd2.Enabled = checkBoxSchedule2.Checked;
        }

        private void checkBoxSchedule3_CheckedChanged(object sender, EventArgs e)
        {
            dateTimePickerScheduleBegin3.Enabled = checkBoxSchedule3.Checked;
            dateTimePickerScheduleEnd3.Enabled = checkBoxSchedule3.Checked;
        }

        private void checkBoxSchedule4_CheckedChanged(object sender, EventArgs e)
        {
            dateTimePickerScheduleBegin4.Enabled = checkBoxSchedule4.Checked;
            dateTimePickerScheduleEnd4.Enabled = checkBoxSchedule4.Checked;
        }

        private void buttonScheduleSet_Click(object sender, EventArgs e)
        {
            //TO DO
            int iRet = -1;
            int iLibKeySel = 0;
            iLibKeySel = comboBoxScheduleArith.SelectedIndex;
            if (iLibKeySel < 0)
            {
                MessageBox.Show("Please search or select the face library first!");
                return;
            }

            //Enabled defense
            string strLibAnduuid = comboBoxScheduleLib.SelectedValue.ToString();
            string strLibKey = SplitLibKeyAndUUID(strLibAnduuid, 0);
            string strLibUUid = SplitLibKeyAndUUID(strLibAnduuid, 1);
            TAlarmScheEnableParam tSchEnabel = new TAlarmScheEnableParam();
            tSchEnabel.cLibUUID = new byte[CommonLen.LEN_UUID];
            tSchEnabel.iBuffSize = Marshal.SizeOf(tSchEnabel); ;
            tSchEnabel.iSceneID = 0;
            tSchEnabel.iParam1 = Convert.ToInt32(strLibKey);//Face database ID, when at type=21, subtype=1 or type= 20, just need this libkey
            tSchEnabel.iParam2 = Convert.ToInt32(comboBoxSubType.SelectedValue.ToString());	//when algorithm type = 20, 0-blacklist, 1-whitelist; when iType=21, indicate intelligent analysis of NVR

            tSchEnabel.iParam1 = Convert.ToInt32(strLibKey);

            //Before enabling the defense function, you need to query the uuid through libkey. This field is required by the sending protocol
            //FaceLibQuery tQuery = new FaceLibQuery();
            //tQuery.iSize = Marshal.SizeOf(tQuery);
            //tQuery.iChanNo = m_iChannelNo;
            //tQuery.iPageNo = 0;
            //tQuery.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT;

            //IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tQuery));
            //Marshal.StructureToPtr(tQuery, ipQueryInfo, true);//It is prone to memory leaks at false

            ////Array of marshaling structures
            //FaceLibQueryResult[] tResult = new FaceLibQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
            //for (int i = 0; i < tResult.Length; i++)
            //{
            //    tResult[i] = new FaceLibQueryResult();
            //}
            //IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceLibQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);

            //iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_LIB_QUERY, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(FaceLibQueryResult)));

            //for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            //{
            //    IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceLibQueryResult)));
            //    tResult[i] = (FaceLibQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceLibQueryResult));
            //}
            //Querying the uuid code is complete
            //Array.Copy(tSchEnabel.cLibUUID, tResult[comboBoxScheduleLib.SelectedIndex].tFaceLib.cLibUUID, CommonLen.LEN_UUID);
            CommonFunction.BytesCopy(strLibUUid, tSchEnabel.cLibUUID);
            tSchEnabel.iEnable = (int)checkBoxScheduleEnabel.CheckState;
            IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tSchEnabel));
            Marshal.StructureToPtr(tSchEnabel, ipQueryInfo, true);//It is prone to memory leaks at false
            iRet = NVSSDK.NetClient_SetAlarmConfig(m_iLogonId, m_iChannelNo, Convert.ToInt32(comboBoxScheduleArith.SelectedValue.ToString()), NVSSDK.CMD_ALARMSCH_ENABLE_EX, ipQueryInfo);

            //set the deployment time template
            //deployment template
            TAlarmScheduleParam tSchParam = new TAlarmScheduleParam();
            tSchParam.timeSeg = new NVS_SCHEDTIME[CommonLen.MAX_DAYS, CommonLen.MAX_TIMESEGMENT];
            tSchParam.cLibUUID = new byte[CommonLen.LEN_UUID];
            tSchParam.iBuffSize = Marshal.SizeOf(tSchParam);
            tSchParam.iWeekday = comboBoxScheduleSet.SelectedIndex;
            tSchParam.iSceneID = 0;	//scenario
            tSchParam.iParam1 = Convert.ToInt32(strLibKey);//face database ID
            tSchParam.iParam2 = Convert.ToInt32(comboBoxSubType.SelectedValue.ToString());	//when algorithm type = 20, 0-blacklist, 1-whitelist; when iType=21, indicate intelligent analysis of NVR
            //Starts to configure four deployment time ranges
            GetScheTimeToStruct(ref tSchParam.timeSeg[tSchParam.iWeekday, 0], checkBoxSchedule1.Checked, dateTimePickerScheduleBegin1.Value, dateTimePickerScheduleEnd1.Value);
            GetScheTimeToStruct(ref tSchParam.timeSeg[tSchParam.iWeekday, 1], checkBoxSchedule2.Checked, dateTimePickerScheduleBegin2.Value, dateTimePickerScheduleEnd2.Value);
            GetScheTimeToStruct(ref tSchParam.timeSeg[tSchParam.iWeekday, 2], checkBoxSchedule3.Checked, dateTimePickerScheduleBegin3.Value, dateTimePickerScheduleEnd3.Value);
            GetScheTimeToStruct(ref tSchParam.timeSeg[tSchParam.iWeekday, 3], checkBoxSchedule4.Checked, dateTimePickerScheduleBegin4.Value, dateTimePickerScheduleEnd4.Value);
            CommonFunction.BytesCopy(strLibUUid, tSchParam.cLibUUID);
            ipQueryInfo = Marshal.AllocHGlobal(tSchParam.iBuffSize);
            Marshal.StructureToPtr(tSchParam, ipQueryInfo, true);//It is prone to memory leaks at false
            iRet = NVSSDK.NetClient_SetAlarmConfig(m_iLogonId, m_iChannelNo, Convert.ToInt32(comboBoxScheduleArith.SelectedValue.ToString()), NVSSDK.CMD_SET_ALARMSCHEDULE, ipQueryInfo);
        }

        private void GetScheTimeToStruct(ref NVS_SCHEDTIME tScheTime, bool _bEnable, DateTime dtBegin, DateTime dtEnd)
        {
            tScheTime.m_ustRecordMode = 0;
            if (_bEnable)
            {
                tScheTime.m_ustRecordMode = 1;
            }
            if (tScheTime.m_ustRecordMode != 0)
            {
                tScheTime.m_ustStartHour = (ushort)dtBegin.Hour;
                tScheTime.m_usStartMin = (ushort)dtBegin.Minute;
                tScheTime.m_ustStopHour = (ushort)dtEnd.Hour;
                tScheTime.m_ustStopMin = (ushort)dtEnd.Minute;
            }
        }


        private void comboBoxScheduleSet_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

        private void comboBoxScheduleArith_SelectedIndexChanged(object sender, EventArgs e)
        {
            //when the alarmtype is ipc face recongnition, alarm subtype is blacklist or whitelist
            string s_AlarmType = comboBoxScheduleArith.SelectedValue.ToString();
            int _iAlarmType = Convert.ToInt32(s_AlarmType);
            if (NVSSDK.ALARM_TYPE_FACE_IDENT == _iAlarmType)
            {
                comboBoxSubType.DataSource = null;
                comboBoxSubType.Items.Clear();
                DataTable dt = new DataTable();
                dt.Columns.Add("Text", Type.GetType("System.String"));
                dt.Columns.Add("Value", Type.GetType("System.Int32"));
                comboBoxSubType.DataSource = dt;
                comboBoxSubType.DisplayMember = "Text";   // Text, that is, dislpayed text
                comboBoxSubType.ValueMember = "Value";    // Value, that is, real value 

                dt.Rows.Add("Black List", 0);
                dt.Rows.Add("White List", 1);
            }
            else if (NVSSDK.ALARM_TYPE_NVR_VCA == _iAlarmType)
            {
                comboBoxSubType.DataSource = null;
                comboBoxSubType.Items.Clear();
                DataTable dt = new DataTable();
                dt.Columns.Add("Text", Type.GetType("System.String"));
                dt.Columns.Add("Value", Type.GetType("System.Int32"));
                comboBoxSubType.DataSource = dt;
                comboBoxSubType.DisplayMember = "Text";   // Text, that is, dislpayed text
                comboBoxSubType.ValueMember = "Value";    // Value, that is, real value 

                dt.Rows.Add("Face Detection", 0);
                dt.Rows.Add("Face recognition-comparison", 1);
                dt.Rows.Add("Face recognition-stranger", 2);
                dt.Rows.Add("Face recognition-frequency", 3);
                dt.Rows.Add("Face recognition-detention", 4);
            }

        }

        private void button1_Click(object sender, EventArgs e)
        {
            button1.Enabled = false;
            button2.Enabled = false;
            progressBar1.Value = 0;
            labelPicCommitProcess.Text = "0";
            if (comboBoxPicLib.SelectedIndex < 0)
            {
                MessageBox.Show("Please search or select the face library first!");
                return;
            }
            m_listInportFacePic = new List<string>();


            //Select the folder to import
            string cstrFilePath = BrowseFolder();
            if (cstrFilePath == "")
            {
                return;
            }

            ////Read all the images in the folder
            DirectoryInfo theFolder = new DirectoryInfo(cstrFilePath);
            FileInfo[] dirInfo = theFolder.GetFiles();
            //Traversing the folder
            foreach (FileInfo NextFile in dirInfo)
            {
                string strFilePathIndex = cstrFilePath;
                strFilePathIndex += "\\" + NextFile.Name;
                m_listInportFacePic.Add(strFilePathIndex);
            }


            ////The total number of import
            m_iImportPicCount = (int)m_listInportFacePic.Count;
            if (m_iImportPicCount <= 0)
            {
                MessageBox.Show("No Pic File!");
                return;
            }

            Thread thread = new Thread(new ParameterizedThreadStart(Import));
            thread.IsBackground = true;
            thread.Start(SplitLibKeyAndUUID(comboBoxPicLib.SelectedValue.ToString(), 0));//Start a new thread, the first face database name to the child thread, to avoid the control in the child thread operation caused by crash
        }
        private void SetLabelValue(int value)
        {
            // InvokeRequired required compares the thread ID of the
            // calling thread to the thread ID of the creating thread.
            // If these threads are different, it returns true.
            if (this.labelPicCommitProcess.InvokeRequired)
            {
                SetProcessBarValueCallBack dt = new SetProcessBarValueCallBack(SetLabelValue);
                this.Invoke(dt, new object[] { value });
            }
            else
            {
                this.labelPicCommitProcess.Text = value.ToString() + '%';
            }
        }
        private void SetProcessBarValue(int value)
        {
            // InvokeRequired required compares the thread ID of the
            // calling thread to the thread ID of the creating thread.
            // If these threads are different, it returns true.
            if (this.progressBar1.InvokeRequired)
            {
                SetProcessBarValueCallBack dt = new SetProcessBarValueCallBack(SetProcessBarValue);
               this.Invoke(dt, new object[] { value });
            }
            else
            {
                this.progressBar1.Value = value;
            }
        }
        private void SetLabelResultValue(string value)
        {
            //if (this.labelPicCommitProcess.InvokeRequired)
            //{
            //    SetProcessBarValueCallBack dt = new SetProcessBarValueCallBack(SetLabelResultValue);
            //    this.Invoke(dt, new object[] { value });
            //}
            //else
            //{
            //    this.labelPicCommitProcess.Text = value;
            //}
        }

        private void SetButton1State()
        {
            if (this.button1.InvokeRequired)
            {
                SetButtonCallBack dt = new SetButtonCallBack(SetButton1State);
                this.Invoke(dt);
            }
            else
            {
                this.button1.Enabled = true;
            }
        }

        private void SetButton2State()
        {
            if (this.button2.InvokeRequired)
            {
                SetButtonCallBack dt = new SetButtonCallBack(SetButton2State);
                this.Invoke(dt);
            }
            else
            {
                this.button2.Enabled = true;
            }
        }

        private void ExportPic(object obj1)
        {
            int iCount = 0;
            int _iSuccessPic = 0, _iFailPic = 0; ;
            while (iCount != (int)m_listExportFacePic.Count)
            {
                g_isDownloadFinished = false;
                string strLocalPath = obj1.ToString();
                DOWNLOAD_FILE tdl = new DOWNLOAD_FILE();
                tdl.m_iSize = Marshal.SizeOf(tdl);
                tdl.m_cRemoteFilename = new byte[255];
                tdl.m_cLocalFilename = new byte[255];
                tdl.m_iSpeed = 32;//Download at maximum speed
                m_uDLId = 0xffffffff;
                strLocalPath = strLocalPath + "\\" + CommonFunction.AddString(m_listExportFacePic.ElementAt(iCount).cName) + "_FACE_" + m_listExportFacePic.ElementAt(iCount).iLibKey.ToString() + "_" + m_listExportFacePic.ElementAt(iCount).iFaceKey.ToString() + ((m_listExportFacePic.ElementAt(iCount).iFileType != 1) ? ".jpg" : ".png");
                CommonFunction.BytesCopy(strLocalPath, tdl.m_cLocalFilename);
                string strPathName = "FACE:0:" + m_listExportFacePic.ElementAt(iCount).iLibKey.ToString() + ":" + m_listExportFacePic.ElementAt(iCount).iFaceKey.ToString();
                CommonFunction.BytesCopy(strPathName, tdl.m_cRemoteFilename);
                //sprintf_s(tDL.m_cRemoteFilename, sizeof(tDL.m_cRemoteFilename), "FACE:%d:%d:%d", 0, _tInfo.iLibKey, _tInfo.iFaceKey);
                IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tdl));
                Marshal.StructureToPtr(tdl, ipReply, true);//It is prone to memory leaks at false
                byte[] byte_tdl = new byte[tdl.m_iSize];
                Marshal.Copy(ipReply, byte_tdl, 0, tdl.m_iSize);
                int iRet = NVSSDK.NetClient_NetFileDownload(ref m_uDLId, m_iLogonId, NVSSDK.DOWNLOAD_CMD_FILE, byte_tdl, Marshal.SizeOf(tdl));
                int iTimes = 0;
                while (!g_isDownloadFinished)
                {
                    iTimes++;
                    Thread.Sleep(1);
                    if (iTimes >= 10 * 100 * 10)
                    {
                        _iFailPic++;
                        MessageBox.Show("download pic failed");
                        break;
                    }
                }
                int iPercent = (iCount+1) * 100 / m_listExportFacePic.Count;
                SetLabelValue(iPercent);
                SetProcessBarValue(iPercent);
                iCount++;
            }
            _iSuccessPic = m_listExportFacePic.Count - _iFailPic;
            string strValue = "Success Pic:" + _iSuccessPic.ToString() + " Fail Pic:" + _iFailPic.ToString();
            MessageBox.Show(strValue);
            SetButton1State();
            SetButton2State();
        }
        private void Import(object obj)
        {
            Int32 _iDealPicCount = 0;
            Int32 _iSuccessPic = 0;
            Int32 _iFailed = 0;
            FaceEdit tEdit = new FaceEdit();
            tEdit.tFace = new FaceInfo();
            tEdit.tFace.cBirthTime = new byte[CommonLen.LEN_16];
            tEdit.iSize = Marshal.SizeOf(tEdit);
            tEdit.iChanNo = 0;
            tEdit.tFace.iSize = Marshal.SizeOf(tEdit.tFace);
            tEdit.tFace.iModeling = 1;
            tEdit.tFace.iLibKey = Convert.ToInt32(obj.ToString());
            tEdit.tFace.iOptType = 1;	//1 Add 2 Modification
            string strDate = CommonFunction.GetCurTimeStr();
            int iPos = strDate.IndexOf(" ", 0);
            if (iPos > 0)
            {
                strDate = strDate.Substring(0, iPos);
            }
            CommonFunction.BytesCopy(strDate, tEdit.tFace.cBirthTime);
            //progressBar1 = new ProgressBar();

            for (int i = 0; i < m_iImportPicCount; i++)
            {
                string strFileName = m_listInportFacePic.ElementAt(i);
                string cstrName = System.IO.Path.GetFileName(strFileName);

                //Start importing	
                tEdit.tFace.iFileType = CommonFunction.GetFaceFileType(cstrName);
                iPos = cstrName.IndexOf("_FACE_", 0);

                if (iPos > 0)
                {
                    cstrName = cstrName.Substring(0, iPos); ;
                }
                if (cstrName.Length >= CommonLen.LEN_64)
                {
                    cstrName = cstrName.Substring(CommonLen.LEN_64 - 1);
                }
                iPos = cstrName.IndexOf(".", 0);
                if (iPos > 0)
                {
                    cstrName = cstrName.Substring(0, iPos);
                }

                tEdit.cFacePic = new byte[CommonLen.LEN_256];
                tEdit.tFace.cName = new byte[CommonLen.LEN_64];
                CommonFunction.BytesCopy(strFileName, tEdit.cFacePic);
                CommonFunction.BytesCopy(cstrName, tEdit.tFace.cName);

                IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceEdit)));//AllocCoTaskMem
                Marshal.StructureToPtr(tEdit, ipQueryInfo, true);//It is prone to memory leaks at false

                FaceReply tReply = new FaceReply();
                tReply.iResult = -1;
                IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tReply));
                Marshal.StructureToPtr(tReply, ipReply, true);//It is prone to memory leaks at false

                int iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_EDIT, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tEdit), ipReply, Marshal.SizeOf(tReply));
                tReply = (FaceReply)Marshal.PtrToStructure(ipReply, typeof(FaceReply));
                Console.Write(iRet.ToString(), tReply.iResult.ToString());
                _iDealPicCount++;
                if (0 == tReply.iResult)
                {
                    _iSuccessPic++;
                }
                else
                {
                    _iFailed++;
                }
                int iPercent = _iDealPicCount * 100 / m_iImportPicCount;
                SetLabelValue(iPercent);
                SetProcessBarValue(iPercent);
            }
            string strValue = "Success Pic:" + _iSuccessPic.ToString() + " Fail Pic:" + _iFailed.ToString();
            MessageBox.Show(strValue);
            SetButton1State();
            SetButton2State();
        }
        private string BrowseFolder()
        {
            System.Windows.Forms.FolderBrowserDialog dialog = new System.Windows.Forms.FolderBrowserDialog();
            dialog.Description = "choose the pictures path";
            if (dialog.ShowDialog() == System.Windows.Forms.DialogResult.OK)
            {
                if (string.IsNullOrEmpty(dialog.SelectedPath))
                {
                    MessageBox.Show(this, "the path of Picture is NULL", "hint");
                    return "";
                }
            }
            return dialog.SelectedPath;
        }

        //tabpage5 A snapshot of the face search
        private void button3_Click(object sender, EventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.InitialDirectory = @"C:\\";
            openFileDialog.Filter = "PicFile(*.jpg;*.jpeg;*.png)|*.jpg;*.jpeg;*.png||";
            openFileDialog.Multiselect = false;
            if (openFileDialog.ShowDialog() == DialogResult.OK)
            {
                string localFileName = openFileDialog.FileName;
                textBox1.Text = localFileName;
            }
        }

        private void UI_Init_SearchPic()
        {

            NVSSDK.NetClient_GetChannelNum(m_iLogonId, ref m_iChannelCount);
            if (m_iChannelCount <= 0)
            {
                MessageBox.Show("GetChannel Error");
                return;
            }

            for (int i = 1; i <= NVSSDK.MAX_QUERY_LIST_COUNT; i++)
            {
                ((CheckBox)this.ChannelListByPic.Controls.Find("checkChan" + i.ToString(), true)[0]).Enabled = false;
                ((CheckBox)this.groupBoxofSearchbyEve.Controls.Find("checkBox" + i.ToString(), true)[0]).Enabled = false;
            }

            for (int i = 1; i <= m_iChannelCount && i <= NVSSDK.MAX_QUERY_LIST_COUNT; i++)
            {
                ((CheckBox)this.ChannelListByPic.Controls.Find("checkChan" + i.ToString(), true)[0]).Enabled = true;
                ((CheckBox)this.groupBoxofSearchbyEve.Controls.Find("checkBox" + i.ToString(), true)[0]).Enabled = true;
            }

        }

        private void UI_Init_Search_dialog()
        {
            comboBox_sortBy.DataSource = null;
            comboBox_sortBy.Items.Clear();
            DataTable dt = new DataTable();
            dt.Columns.Add("Text", Type.GetType("System.String"));
            dt.Columns.Add("Value", Type.GetType("System.Int32"));
            comboBox_sortBy.DataSource = dt;
            comboBox_sortBy.DisplayMember = "Text";   // Text, that is, dislpayed text
            comboBox_sortBy.ValueMember = "Value";    // Value, that is, real value
            dt.Rows.Add("Capture time", 0);
            dt.Rows.Add("Similarity", 1);

            int iIndex = 10;
            label_similarity.Text = iIndex.ToString();
        }

        private void UI_Init_CapByEvent()
        {
            comboBox2.DataSource = null;
            comboBox2.Items.Clear();
            DataTable dt = new DataTable();
            dt.Columns.Add("Text", Type.GetType("System.String"));
            dt.Columns.Add("Value", Type.GetType("System.Int32"));
            comboBox2.DataSource = dt;
            comboBox2.DisplayMember = "Text";   // Text, that is, dislpayed text
            comboBox2.ValueMember = "Value";    // Value, that is, real value
            dt.Rows.Add("All", 0x7FFFFFFF);
            dt.Rows.Add("Face Detection", 1);
            dt.Rows.Add("Face recognition-comparison", 2);
            dt.Rows.Add("Face recognition-stranger", 3);
            dt.Rows.Add("Face recognition-frequency", 4);
            dt.Rows.Add("Face recognition-detension", 5);
        }

        private void UI_Init_CapByFeature()
        { 
            comboBoxbyFeatureAge.SelectedIndex = 0;
            comboBoxByFeatureSex.SelectedIndex = 0;
            comboBoxByFeatureNation.SelectedIndex = 0;
            comboBoxByFeatureGlasses.SelectedIndex = 0;
            comboBoxByFeatureMasks.SelectedIndex = 0;
        }

        private void button4_Click(object sender, EventArgs e)
        {
            //first, clean up combox
            comboBoxCutResult.DataSource = null;
            comboBoxCutResult.Items.Clear();
            DataTable dt = new DataTable();
            dt.Columns.Add("Text", Type.GetType("System.String"));
            dt.Columns.Add("Value", Type.GetType("System.String"));
            comboBoxCutResult.DataSource = dt;
            comboBoxCutResult.DisplayMember = "Text";   // Text, that is, dislpayed text
            comboBoxCutResult.ValueMember = "Value";    // Value, that is, real value

            m_iTaskID = -1;

            //Buckle figure
            FaceCutEx tInfo = new FaceCutEx();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.cPicPath = new byte[CommonLen.LEN_256];
            tInfo.iPicType = 0;	//0-jpg, 1-png
            tInfo.iChanNo = m_iChannelNo;
            tInfo.iPageNo = 0;
            tInfo.iPageCount = CommonLen.FACE_MAX_PAGE_COUNT;
            CommonFunction.BytesCopy(textBox1.Text, tInfo.cPicPath);

            IntPtr ipQueryInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipQueryInfo, true);//It is prone to memory leaks at false

            //Array of marshaling structures
            FaceCutQueryResult[] tResult = new FaceCutQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
            for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            {
                tResult[i] = new FaceCutQueryResult();
            }
            IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceLibQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);

            //Traversing the checkbox
            
            int iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_CUT_EX, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tInfo), ipResult,  Marshal.SizeOf(typeof(FaceCutQueryResult)));
            if (0 != iRet)
            {
                MessageBox.Show("Cut Pic Failed");
                return;
            }
            //Restore structure
            for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            {
                IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceCutQueryResult)));
                tResult[i] = (FaceCutQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceCutQueryResult));
            }
            //Process the result of Buckle figure
            for(int i = 0; i < tResult[0].iPageCount && i < CommonLen.FACE_MAX_PAGE_COUNT; ++i)
            {
                dt.Rows.Add(CommonFunction.ByteToStr(tResult[i].cFileName), CommonFunction.ByteToStr(tResult[i].cFileName));
            }
            if (tResult[0].iPageCount > 0)
            {
                m_iTaskID = tResult[0].iTaskId;
                comboBoxCutResult.SelectedItem = 0;
                download_showImage();
            }
        }

        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            download_showImage();
        }

        private void download_showImage()
        {
            if (comboBoxCutResult.Items.Count < 0)
            {
                return;
            }
            string csPicPath = comboBoxCutResult.SelectedValue.ToString();
            csPicPath = csPicPath.Replace("/", "_");
            string csModulePath = System.IO.Directory.GetCurrentDirectory();
            csPicPath = csModulePath + "\\" + csPicPath;
            int iLen = 0;
            for (int i = 0; i < csPicPath.Length; i++)
            {
                if (csPicPath[i] != '\0')
                    iLen++;
                else
                    break;
            }
            csPicPath = csPicPath.Substring(0, iLen);
            if (File.Exists(csPicPath)) // Judge whether the image after matting exists in the local. If it exists, it will be displayed directly
            {
                pictureBox1.Load(csPicPath);
            }
            else                        //If no, download the file from the device to the local computer
            {
                DOWNLOAD_FILE tdl = new DOWNLOAD_FILE();
                tdl.m_iSize = Marshal.SizeOf(tdl);
                tdl.m_cRemoteFilename = new byte[255];
                tdl.m_cLocalFilename = new byte[255];
                CommonFunction.BytesCopy(comboBoxCutResult.SelectedValue.ToString(), tdl.m_cRemoteFilename);
                CommonFunction.BytesCopy(csPicPath, tdl.m_cLocalFilename);
                tdl.m_iReqMode = 0; // You must download in stream mode here
                tdl.m_iSpeed = 32;//Download at maximum speed
                m_uDLId = 0xffffffff;
                IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tdl));
                Marshal.StructureToPtr(tdl, ipReply, true);//It is prone to memory leaks at false
                byte[] byte_tdl = new byte[tdl.m_iSize];
                Marshal.Copy(ipReply, byte_tdl, 0, tdl.m_iSize);
                int iRet = NVSSDK.NetClient_NetFileDownload(ref m_uDLId, m_iLogonId, NVSSDK.DOWNLOAD_CMD_FILE, byte_tdl, Marshal.SizeOf(tdl));
                if (0 == iRet)
                {
                    //PictureBox pictureBox1 = new PictureBox();
                    m_strPicPath =  CommonFunction.ByteToStr(tdl.m_cLocalFilename);
                    int iLen1 = 0;
                    for (int i = 0; i < m_strPicPath.Length; i++)
                    {
                        if (m_strPicPath[i] != '\0')
                            iLen1++;
                        else
                            break;
                    }
                    m_strPicPath = m_strPicPath.Substring(0, iLen1);

                }
                else
                {
                    MessageBox.Show("download file failed");
                }
            }
        }

        private void check_All_CheckedChanged(object sender, EventArgs e)
        {
            foreach (Control c in ChannelListByPic.Controls)//Traverse all the controls in ChannelListByPic
            {
                if (c is CheckBox)//Only CheckBox control is traversed
                {
                    if (((CheckBox)c).CheckState != check_All.CheckState)
                    {
                        if (((CheckBox)c).Enabled == false)
                        {
                            ((CheckBox)c).CheckState = CheckState.Unchecked;
                        }
                        else
                            ((CheckBox)c).CheckState = check_All.CheckState;
                        
                        if (check_All.CheckState == CheckState.Checked)
                            ((CheckBox)c).Enabled = false;
                        else
                            ((CheckBox)c).Enabled = true;
                    }
                    else
                    {
                        if (check_All.CheckState == CheckState.Checked)
                        {
                            ((CheckBox)c).Enabled = false;
                        }
                    
                    }
                }
            }
        }

        private void trackBar1_Scroll(object sender, EventArgs e)
        {
            label_similarity.Text = trackBar_similarity.Value.ToString();
        }

        private void GetNvsFileTime(ref DateTimePicker dt, ref NVS_FILE_TIME tInfo)
        {
            DateTime date = DateTime.Parse(dt.Text);
            tInfo.m_iYear = (ushort)System.Int32.Parse(date.Year.ToString());
            tInfo.m_iMonth = (ushort)System.Int32.Parse(date.Month.ToString());
            tInfo.m_iDay = (ushort)System.Int32.Parse(date.Day.ToString());
            tInfo.m_iHour = (ushort)System.Int32.Parse(date.Hour.ToString());
            tInfo.m_iMinute = (ushort)System.Int32.Parse(date.Minute.ToString());
            tInfo.m_iSecond = (ushort)System.Int32.Parse(date.Second.ToString());
        }

        private void GetNvsFileTime1(ref DateTimePicker dt, ref NetFileQueryVca tInfo)
        {
            DateTime date = DateTime.Parse(dt.Text);
            tInfo.tBegTime.m_iYear = (ushort)System.Int32.Parse(date.Year.ToString());
            tInfo.tBegTime.m_iMonth = (ushort)System.Int32.Parse(date.Month.ToString());
            tInfo.tBegTime.m_iDay = (ushort)System.Int32.Parse(date.Day.ToString());
            tInfo.tBegTime.m_iHour = (ushort)System.Int32.Parse(date.Hour.ToString());
            tInfo.tBegTime.m_iMinute = (ushort)System.Int32.Parse(date.Minute.ToString());
            tInfo.tBegTime.m_iSecond = (ushort)System.Int32.Parse(date.Second.ToString());
        }

        private void buttonStartSearchByPic_Click(object sender, EventArgs e)
        {
            listView_SearchByPic.Items.Clear();
            textBox_Process.Text = "0";

            if (m_iTaskID <= 0)
            {
                MessageBox.Show("Please cutout first!");
                return;
            }
            //query conditions
            FaceSearchSnap tQuery = new FaceSearchSnap();
            tQuery.iSize = Marshal.SizeOf(tQuery);
            tQuery.iChanSize = Marshal.SizeOf(typeof(QueryChanNo));
            tQuery.cPicturePath = new byte[CommonLen.LEN_256];
            //channel list
            QueryChanNo[] m_tQueryChan = new QueryChanNo[NVSSDK.MAX_QUERY_LIST_COUNT];
            for (int i = 0; i < NVSSDK.MAX_QUERY_LIST_COUNT; i++)
            {
                m_tQueryChan[i] = new QueryChanNo();
            }

            if (check_All.CheckState == CheckState.Checked)
            {
                tQuery.iChanCount = 1;
                m_tQueryChan[0].iChanNo = 0x7FFFFFFF;
            }
            else
            {
                for (int i = 1; i <= m_iChannelCount && i <= NVSSDK.MAX_QUERY_LIST_COUNT; i++)
                {
                    if (((CheckBox)this.ChannelListByPic.Controls.Find("checkChan" + i.ToString(), true)[0]).CheckState == CheckState.Checked)
                    {
                        m_tQueryChan[tQuery.iChanCount].iChanNo = i - 1;
                        tQuery.iChanCount++;
                    }
                }
            }

            IntPtr ipChanList = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(QueryChanNo)) * NVSSDK.MAX_QUERY_LIST_COUNT);

            for (int i = 0; i < NVSSDK.MAX_QUERY_LIST_COUNT; i++)
            {
                Marshal.StructureToPtr(m_tQueryChan[i], (IntPtr)((UInt32)ipChanList + i * Marshal.SizeOf(typeof(QueryChanNo))), true);//It is prone to memory leaks at false
            }
            
            tQuery.pChanList = ipChanList;

            //start and end times
            GetNvsFileTime(ref dateBeginTimePickerByPic,ref tQuery.tBegTime);
            GetNvsFileTime(ref dateEndTimePickerbyPic, ref tQuery.tEndTime);
            tQuery.iSimilarity = trackBar_similarity.Value;
            tQuery.iSortMode = System.Int32.Parse(comboBox_sortBy.SelectedValue.ToString());
            tQuery.iTaskId = m_iTaskID;
            CommonFunction.BytesCopy(comboBoxCutResult.SelectedValue.ToString(), tQuery.cPicturePath);

            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tQuery));
            Marshal.StructureToPtr(tQuery, ipReply, true);//It is prone to memory leaks at false
            int iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_SEARCH_SNAP, m_iChannelNo, ipReply, Marshal.SizeOf(tQuery), IntPtr.Zero, 0);
            if (0 != iRet)
            {
                MessageBox.Show("Start search failed!");
                return;
            }
        }

        private void buttonSearchByPicProgress_Click(object sender, EventArgs e)
        {
            if (m_iTaskID <= 0)
            {
                MessageBox.Show("Please cutout first!");
                return;
            }
            FaceReply tOutInfo = new FaceReply();
            FaceSearchSnapProcess tInfo = new FaceSearchSnapProcess();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iTaskId = m_iTaskID;
            IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipReply, true);//It is prone to memory leaks at false

            IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(tOutInfo));
             Marshal.StructureToPtr(tOutInfo, ipResult, true);//It is prone to memory leaks at false
            int iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_SEARCH_SNAP_PROCESS, m_iChannelNo, ipReply, Marshal.SizeOf(tInfo), ipResult,Marshal.SizeOf(tOutInfo));
            
            //Resore structure
            tOutInfo  = (FaceReply)Marshal.PtrToStructure(ipResult, typeof(FaceReply));
            if (0 != iRet)
            {
                MessageBox.Show("Progress query failed!");
                return;
            }
            if (6 == tOutInfo.iResult)
            {
                textBox_Process.Text = tOutInfo.iDelLibProgress.ToString();
            }
        }

        private void buttonSearchByPicResult_Click(object sender, EventArgs e)
        {
            	listView_SearchByPic.Items.Clear();
            	if (m_iTaskID <= 0)
               {
                    MessageBox.Show("Please cutout first!");
                    return;
                }
            	if("100" != textBox_Process.Text)
                {
                    MessageBox.Show("Search is not complete!");
                    return;
                }
	
	            FaceSearchSnapQuery tInfo = new FaceSearchSnapQuery();
	            tInfo.iSize = Marshal.SizeOf(tInfo);
	            tInfo.iTaskId = m_iTaskID;
	            tInfo.iPageSize = NVSSDK.FACE_MAX_PAGE_COUNT;
	            tInfo.iPageNo = 0;

                IntPtr ipReply = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
                Marshal.StructureToPtr(tInfo, ipReply, true);//It is prone to memory leaks at false

	            FaceSearchSnapResult[] tOutInfo = new FaceSearchSnapResult[NVSSDK.FACE_MAX_PAGE_COUNT];
                for(int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
                {
                    tOutInfo[i] = new FaceSearchSnapResult();
                    tOutInfo[i].tPicSnap = new VcaFileAttr();
                    tOutInfo[i].tPicSnap.cFileName = new byte[CommonLen.LEN_64];
                    tOutInfo[i].tPicNeg = new VcaFileAttr();
                    tOutInfo[i].tPicNeg.cFileName = new byte[CommonLen.LEN_64];
                }

                 IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceSearchSnapResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);
                 int iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_SEARCH_SNAP_RESULT, m_iChannelNo, ipReply, Marshal.SizeOf(tInfo), ipResult, Marshal.SizeOf(typeof(FaceSearchSnapResult)));
	             if (0 != iRet) {
                     MessageBox.Show("The result query failed!");
                     return;
                 }
            //Resore structure
                 for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
                 {
                     IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceSearchSnapResult)));
                     tOutInfo[i] = (FaceSearchSnapResult)Marshal.PtrToStructure(ptr, typeof(FaceSearchSnapResult));
                 }
                 for (int i = 0; i < tOutInfo[0].iCurPageCount && i < NVSSDK.FACE_MAX_PAGE_COUNT; ++i)
                 {
                     int iIndex = listView_SearchByPic.Items.Count;

                     ListViewItem one = new ListViewItem();
                     one.Text = (i+1).ToString();
                     one.SubItems.Add(tOutInfo[i].iChanNo.ToString());//channel
                     string  strTimeBegin = tOutInfo[i].tBegTime.m_iYear.ToString() + "-" + tOutInfo[i].tBegTime.m_iMonth.ToString() + "-" + tOutInfo[i].tBegTime.m_iDay.ToString() + " " + tOutInfo[i].tBegTime.m_iHour.ToString() + ":" + tOutInfo[i].tBegTime.m_iMinute.ToString() + ":" + tOutInfo[i].tBegTime.m_iSecond.ToString();
                     one.SubItems.Add(strTimeBegin);
                     string strTimeEnd = tOutInfo[i].tEndTime.m_iYear.ToString() + "-" + tOutInfo[i].tEndTime.m_iMonth.ToString() + "-" + tOutInfo[i].tEndTime.m_iDay.ToString() + " " + tOutInfo[i].tEndTime.m_iHour.ToString() + ":" + tOutInfo[i].tEndTime.m_iMinute.ToString() + ":" + tOutInfo[i].tEndTime.m_iSecond.ToString();//endtime
                     one.SubItems.Add(strTimeEnd);
                     one.SubItems.Add(tOutInfo[i].iAge.ToString());
                     one.SubItems.Add(tOutInfo[i].iSex.ToString());
                     one.SubItems.Add(tOutInfo[i].iNation.ToString());
                     one.SubItems.Add(tOutInfo[i].iWearGlasses.ToString());
                     one.SubItems.Add(tOutInfo[i].iWearMask.ToString());
                     one.SubItems.Add(tOutInfo[i].iSimilarity.ToString());
                     string strBigPic = CommonFunction.ByteToStr(tOutInfo[i].tPicSnap.cFileName);
                     one.SubItems.Add(strBigPic);
                     string strSmallPic = CommonFunction.ByteToStr(tOutInfo[i].tPicNeg.cFileName);
                     one.SubItems.Add(strSmallPic);
                     //end

                     listView_SearchByPic.Items.Insert(iIndex, one);	
                 }	
        }

        private void button_View_Click(object sender, EventArgs e)
        {
            OpenFileDialog openFileDialog = new OpenFileDialog();
            openFileDialog.InitialDirectory = @"C:\\";
            openFileDialog.Filter = "PicFile(*.jpg;*.jpeg;*.png)|*.jpg;*.jpeg;*.png||";
            openFileDialog.Multiselect = false;
            if (openFileDialog.ShowDialog() == DialogResult.OK)
            {
                string localFileName = openFileDialog.FileName;
                textBox2.Text = localFileName;
                this.pictureBox2.Load(localFileName);
            }
        }

        private void trackBar1_Scroll_1(object sender, EventArgs e)
        {
            label_similarityoflib.Text = trackBar_LibSearch.Value.ToString();
        }

        private void button_searchLib_Click(object sender, EventArgs e)
        {
            int iLibKeySel = 0;
            string strLib = comboBox_SearchLib.SelectedValue.ToString();
            int i = strLib.IndexOf(",");
            strLib = strLib.Substring(0, i);
            iLibKeySel = System.Int32.Parse(strLib);
            
            //after confirming the face database
            //first,  buckle figure
            FaceCutEx tCut = new FaceCutEx();
            tCut.iSize = Marshal.SizeOf(tCut);
            tCut.iPicType = 0;		//0-jpg, 1-png
            tCut.iChanNo = 0;
            tCut.iPageNo = 0;
            tCut.iPageCount = 1;	//Only pick 1 face for retrieval
            tCut.cPicPath = new byte[CommonLen.LEN_256];
            CommonFunction.BytesCopy(textBox2.Text, tCut.cPicPath);
            if (0 == (tCut.cPicPath).Length)
            {
                MessageBox.Show("Please select a picture first!");
                return;
            }

            listView_SearchLibPic.Items.Clear();

            IntPtr ipEditInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tCut));
            Marshal.StructureToPtr(tCut, ipEditInfo, true);//It is prone to memory leaks at false
            FaceCutQueryResult tCutRet = new FaceCutQueryResult();

            IntPtr ipResultInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tCutRet));
            int iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_CUT_EX, m_iChannelNo, ipEditInfo, Marshal.SizeOf(tCut), ipResultInfo, Marshal.SizeOf(tCutRet));

            tCutRet = (FaceCutQueryResult)Marshal.PtrToStructure(ipResultInfo, typeof(FaceCutQueryResult));

            if (0 != iRet || 0 == (tCutRet.cFileName).Length)
            {
                MessageBox.Show("Face matching failure!");
                return;
            }

            //retrieve the result of buckle figure
            //retrieve the result of buckle figure
	         FaceSearch  tInfo = new FaceSearch();
             tInfo.cLibKey = new byte[CommonLen.LEN_64];
             tInfo.cPicName = new byte[CommonLen.LEN_256];
             tInfo.iSize = Marshal.SizeOf(tInfo);
	         tInfo.iTaskId = tCutRet.iTaskId;
	         tInfo.iSimilar = System.Int32.Parse(label_similarityoflib.Text);
	         tInfo.iLibKey = iLibKeySel;
             Array.Copy(tCutRet.cFileName, tInfo.cPicName, CommonLen.LEN_256);
	         tInfo.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT;
             IntPtr ipSearchInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
             Marshal.StructureToPtr(tInfo, ipSearchInfo, true);//It is prone to memory leaks at false
              while(true)
              {
           
                  FaceQueryResult[] tSearchRet = new FaceQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
                  for (int k = 0; k < NVSSDK.FACE_MAX_PAGE_COUNT; k++)
                  {
                      tSearchRet[k] = new FaceQueryResult();
                      tSearchRet[k].tFace.cName = new byte[CommonLen.LEN_64];
                      tSearchRet[k].tFace.cBirthTime = new byte[CommonLen.LEN_16];
                      tSearchRet[k].tFace.cCertNum = new byte[CommonLen.LEN_64];
                      tSearchRet[k].tFace.cLibUUID = new byte[CommonLen.LEN_UUID];
                      tSearchRet[k].tFace.cFaceUUID = new byte[CommonLen.LEN_UUID];
                      tSearchRet[k].tFace.cLibVersion = new byte[CommonLen.LEN_64];
                      tSearchRet[k].tFace.cVerifyCode = new byte[CommonLen.LEN_64];
                      tSearchRet[k].tFace.cFileName = new byte[CommonLen.LEN_256];
                      tSearchRet[k].iSize = Marshal.SizeOf(typeof(FaceQueryResult));
                  }
                  IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);

                  iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_SEARCH, m_iChannelNo, ipSearchInfo, Marshal.SizeOf(tInfo), ipResult, Marshal.SizeOf(typeof(FaceQueryResult)));
                 if (0 != iRet)
                {
                     MessageBox.Show("Face searching failure!");
                     return;
                 }
                 //Restore the structure array  
                 for (int iIndex = 0; iIndex < NVSSDK.FACE_MAX_PAGE_COUNT; iIndex++)
                 {
                     IntPtr ptr = (IntPtr)((UInt32)ipResult + iIndex * Marshal.SizeOf(typeof(FaceQueryResult)));
                     tSearchRet[iIndex] = (FaceQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceQueryResult));
                 }
		         int iTotalCount = tSearchRet[0].iTotal;
		         int iTolalPage = iTotalCount / NVSSDK.FACE_MAX_PAGE_COUNT;
		         if (iTotalCount % NVSSDK.FACE_MAX_PAGE_COUNT > 0)
                 {
                     iTolalPage++;
                 }
                  tInfo.iPageNo++;
	
                  for (int j = 0; j < tSearchRet[0].iPageCount; ++j)
                  {
                      if (tSearchRet[j].iSize <= 0)
                      {
                          break;
                      }

                      int iIndex = listView_SearchLibPic.Items.Count;

                      ListViewItem one = new ListViewItem();
                      one.Text = (tSearchRet[j].tFace.iLibKey).ToString();
                      one.SubItems.Add((iIndex+1).ToString());
                      string strName = CommonFunction.ByteToStr(tSearchRet[j].tFace.cName);
                      one.SubItems.Add(strName);
                      one.SubItems.Add((tSearchRet[j].tFace.iSex).ToString());
                      string strBirthTime = CommonFunction.ByteToStr(tSearchRet[j].tFace.cBirthTime);
                      one.SubItems.Add(strBirthTime);
                      one.SubItems.Add((tSearchRet[j].tFace.iNation).ToString());
                      one.SubItems.Add((tSearchRet[j].tFace.iPlace).ToString());
                      one.SubItems.Add((tSearchRet[j].tFace.iCertType).ToString());
                      string strCertNum = CommonFunction.ByteToStr(tSearchRet[j].tFace.cCertNum);
                      one.SubItems.Add(strCertNum);
                      listView_SearchLibPic.Items.Insert(iIndex, one);	
                  }
                  if (tInfo.iPageNo >= iTolalPage)
                  { 
                      break;
                  }
              }


        }

        private void checkBox_AllchnInEvent_CheckedChanged(object sender, EventArgs e)
        {
            foreach (Control c in groupBoxofSearchbyEve.Controls)//Traverse all the controls in ChannelListByPic
            {
                if (c is CheckBox)//Only CheckBox control is traversed
                {
                    if (((CheckBox)c).CheckState != checkBox_AllchnInEvent.CheckState)
                    {
                        if (((CheckBox)c).Enabled == false)
                        {
                            ((CheckBox)c).CheckState = CheckState.Unchecked;
                        }
                        else
                            ((CheckBox)c).CheckState = checkBox_AllchnInEvent.CheckState;

                        if (checkBox_AllchnInEvent.CheckState == CheckState.Checked)
                            ((CheckBox)c).Enabled = false;
                        else
                            ((CheckBox)c).Enabled = true;
                    }
                    else
                    {
                        if (checkBox_AllchnInEvent.CheckState == CheckState.Checked)
                        {
                            ((CheckBox)c).Enabled = false;
                        }

                    }
                }
            }
        }

        private void buttonSearchByEvent_Click(object sender, EventArgs e)
        {
            	QueryChanNo[]	tQueryChan = new QueryChanNo[NVSSDK.MAX_QUERY_LIST_COUNT];
                int iIndex = 0;
                for(iIndex = 0; iIndex < NVSSDK.MAX_QUERY_LIST_COUNT; iIndex++)
                {
                    tQueryChan[iIndex] = new QueryChanNo();
                }
                NetFileQueryVca tQuery = new NetFileQueryVca();
                tQuery.cQueryCondition = new char[NVSSDK.MAX_QUERY_LIST_COUNT, CommonLen.LEN_256];
                tQuery.iVcaList = new int[NVSSDK.MAX_QUERY_LIST_COUNT];
                tQuery.iSize = Marshal.SizeOf(tQuery);
                tQuery.iChanSize =Marshal.SizeOf(typeof(QueryChanNo));
	            tQuery.iChanCount = 0;
              
	            
            //channel list

                if (checkBox_AllchnInEvent.CheckState == CheckState.Checked)
                {
                    tQuery.iChanCount = 1;
                    tQueryChan[0].iChanNo = 0x7FFFFFFF;
                }
                else
                {
                    for (int i = 1; i <= m_iChannelCount && i <= NVSSDK.MAX_QUERY_LIST_COUNT; i++)
                    {
                        if (((CheckBox)this.groupBoxofSearchbyEve.Controls.Find("checkBox" + i.ToString(), true)[0]).CheckState == CheckState.Checked)
                        {
                            tQueryChan[tQuery.iChanCount].iChanNo = i - 1;
                            tQuery.iChanCount++;
                        }
                    }
                }

                IntPtr ipChanList = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(QueryChanNo)) * NVSSDK.MAX_QUERY_LIST_COUNT);

                for (int i = 0; i < NVSSDK.MAX_QUERY_LIST_COUNT; i++)
                {
                    Marshal.StructureToPtr(tQueryChan[i], (IntPtr)((UInt32)ipChanList + i * Marshal.SizeOf(typeof(QueryChanNo))), true);//It is prone to memory leaks at false
                }

                tQuery.pChanList = ipChanList;

               //start and end times
                GetNvsFileTime(ref dateTimePicker1, ref tQuery.tBegTime);
                GetNvsFileTime(ref dateTimePicker2, ref tQuery.tEndTime);

	            tQuery.iVcaCount = 1;//This demo queries only one intelligent analysis type. You can select one based on actual requirements
	            tQuery.iVcaList[0] = 9;   //9:face recognition
                tQuery.iPageCount = 20;
	            tQuery.iPageNo = 0;
	            tQuery.iFileType = 2;   //querying the file type  2-picture
	            tQuery.iConditionCount = 2; //query Number of conditions

                int iEventType = (1 << 16) + 7; //Retrieve by Event
                string strIndex = iEventType.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[0, iIndex] = strIndex[iIndex];
                }
               
                strIndex = comboBox2.SelectedValue.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[1, iIndex] = strIndex[iIndex];
                }
                NetFileQueryVcaResult[] tResult = new NetFileQueryVcaResult[CommonLen.FACE_MAX_PAGE_COUNT];
                for (iIndex = 0; iIndex < CommonLen.FACE_MAX_PAGE_COUNT; iIndex++)
                {
                    tResult[iIndex] = new NetFileQueryVcaResult();
                    tResult[iIndex].tFileAttr = new VcaFileAttr[NVSSDK.MAX_VCA_FILE_COUNT];
                    for (int j = 0; j < NVSSDK.MAX_VCA_FILE_COUNT;j++ )
                    {
                        tResult[iIndex].tFileAttr[j] = new VcaFileAttr();
                        tResult[iIndex].tFileAttr[j].cFileName = new byte[CommonLen.LEN_64];
                        tResult[iIndex].tFileAttr[j].cReserve = new byte[CommonLen.LEN_64];
                    }

                    tResult[iIndex].tBegTime = new NVS_FILE_TIME();
                    tResult[iIndex].tEndTime = new NVS_FILE_TIME();

                    tResult[iIndex].cExAttr = new byte[NVSSDK.MAX_VCA_ATTR_COunt*CommonLen.LEN_256];
                }
                IntPtr ipInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tQuery));
                Marshal.StructureToPtr(tQuery, ipInfo, true);//It is prone to memory leaks at false

                IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(NetFileQueryVcaResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);
                //Marshal.StructureToPtr(tResult, ipResult, true);//It is prone to memory leaks at false

                int iRet = NVSSDK.NetClient_Query_V5(m_iLogonId, NVSSDK.CMD_NETFILE_QUERY_VCA, m_iChannelNo, ipInfo, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(NetFileQueryVcaResult)));

                if (iRet != 0)
                {
                    MessageBox.Show("Get Failed");
                    return;
                }
                //Restore the structure array 
                for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
                {
                    IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(NetFileQueryVcaResult)));
                    tResult[i] = (NetFileQueryVcaResult)Marshal.PtrToStructure(ptr, typeof(NetFileQueryVcaResult));
                }
                listViewByEvent.Items.Clear();
                for (int i = 0; i < tResult[0].iCurPageCount; ++i)
                {

                    int iCount = listViewByEvent.Items.Count;

                    ListViewItem one = new ListViewItem();
                    one.Text = (i + 1).ToString();
                    one.SubItems.Add((i+1).ToString());
                    string strTimeBegin = tResult[i].tBegTime.m_iYear.ToString() + "-" + tResult[i].tBegTime.m_iMonth.ToString() + "-" + tResult[i].tBegTime.m_iDay.ToString() + " " + tResult[i].tBegTime.m_iHour.ToString() + ":" + tResult[i].tBegTime.m_iMinute.ToString() + ":" + tResult[i].tBegTime.m_iSecond.ToString();
                    one.SubItems.Add(strTimeBegin);
                    string strTimeEnd = tResult[i].tEndTime.m_iYear.ToString() + "-" + tResult[i].tEndTime.m_iMonth.ToString() + "-" + tResult[i].tEndTime.m_iDay.ToString() + " " + tResult[i].tEndTime.m_iHour.ToString() + ":" + tResult[i].tEndTime.m_iMinute.ToString() + ":" + tResult[i].tEndTime.m_iSecond.ToString();//endtime
                    one.SubItems.Add(strTimeEnd);
                    string strSmallPic = CommonFunction.ByteToStr(tResult[i].tFileAttr[0].cFileName);
                    one.SubItems.Add(strSmallPic);
                    string strBigPic = CommonFunction.ByteToStr(tResult[i].tFileAttr[1].cFileName);
                    one.SubItems.Add(strBigPic);
                    
                    listViewByEvent.Items.Insert(iCount, one);
                }
        }

//tabSearchByCapture:tab SearchByFeature------------
        
        private void buttonSeachByFeature_Click(object sender, EventArgs e)
        {
            QueryChanNo[] tQueryChan = new QueryChanNo[NVSSDK.MAX_QUERY_LIST_COUNT];
            int iIndex = 0;
            for (iIndex = 0; iIndex < NVSSDK.MAX_QUERY_LIST_COUNT; iIndex++)
            {
                tQueryChan[iIndex] = new QueryChanNo();
            }

            NetFileQueryVca tQuery = new NetFileQueryVca();
            tQuery.cQueryCondition = new char[NVSSDK.MAX_QUERY_LIST_COUNT, CommonLen.LEN_256];
            tQuery.iVcaList = new int[NVSSDK.MAX_QUERY_LIST_COUNT];
            tQuery.iSize = Marshal.SizeOf(tQuery);
            tQuery.iChanSize = Marshal.SizeOf(typeof(QueryChanNo));
            tQuery.iChanCount = 0;


            //channel list
            
            if (checkBoxByFeatureAllChn.CheckState == CheckState.Checked)
            {
                tQuery.iChanCount = 1;
                tQueryChan[0].iChanNo = 0x7FFFFFFF;
            }
            else
            {
                for (int i = 1; i <= m_iChannelCount && i <= NVSSDK.MAX_QUERY_LIST_COUNT; i++)
                {
                    if (((CheckBox)this.groupBoxofSearchbyFeature.Controls.Find("checkBox" + (i + 33).ToString(), true)[0]).CheckState == CheckState.Checked)
                    {
                        tQueryChan[tQuery.iChanCount].iChanNo = i - 1;
                        tQuery.iChanCount++;
                    }
                }
            }

            IntPtr ipChanList = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(QueryChanNo)) * NVSSDK.MAX_QUERY_LIST_COUNT);

            for (int i = 0; i < NVSSDK.MAX_QUERY_LIST_COUNT; i++)
            {
                Marshal.StructureToPtr(tQueryChan[i], (IntPtr)((UInt32)ipChanList + i * Marshal.SizeOf(typeof(QueryChanNo))), true);//It is prone to memory leaks at false
            }

            tQuery.pChanList = ipChanList;

           
            //start and end times
            GetNvsFileTime(ref dateTimePickerByFeatureStartTime, ref tQuery.tBegTime);
            GetNvsFileTime(ref dateTimePickerByFeatureEndTime, ref tQuery.tEndTime);

            tQuery.iVcaCount = 1;       //This demo queries only one intelligent analysis type. You can select one based on actual requirements
            tQuery.iVcaList[0] = 9;     //9:face recognition
            tQuery.iPageCount = 20;
            tQuery.iPageNo = 0;
            tQuery.iFileType = 2;       //querying the file type  2-picture
            tQuery.iConditionCount = 7; //query Number of conditions

            int iEventType = (0 << 16) + 7; //Search type 0 - Search by characteristics
            string strIndex = iEventType.ToString();
            for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
            {
                tQuery.cQueryCondition[0, iIndex] = strIndex[iIndex];
            }

            int iAll = 0x7FFFFFFF;
            if (0 != comboBoxbyFeatureAge.SelectedIndex)//In All, it is not necessary to assign a value to the query conditions of the age group
            {
                strIndex = comboBoxbyFeatureAge.SelectedIndex.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[1, iIndex] = strIndex[iIndex];
                }
            }
            else
            {
                strIndex = iAll.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[1, iIndex] = strIndex[iIndex];
                }
            }

            if (0 != comboBoxByFeatureSex.SelectedIndex)//In All, it is not necessary to assign a value to gender's query conditions
            {
                strIndex = comboBoxByFeatureSex.SelectedIndex.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[2, iIndex] = strIndex[iIndex];
                }
            }
            else
            {
                strIndex = iAll.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[2, iIndex] = strIndex[iIndex];
                }
            }

            if (0 != comboBoxByFeatureNation.SelectedIndex)//It is not necessary to assign a value to the query conditions of the nation when all
            {
                strIndex = comboBoxByFeatureNation.SelectedIndex.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[3, iIndex] = strIndex[iIndex];
                }
            }
            else
            {
                strIndex = iAll.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[3, iIndex] = strIndex[iIndex];
                }
            }


            strIndex = textBoxByFeatureName.Text.ToString();
            for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
            {
                tQuery.cQueryCondition[4, iIndex] = strIndex[iIndex];
            }

            if (0 != comboBoxByFeatureGlasses.SelectedIndex)//In All, it is not necessary to assign a value to the query conditions for wearing glasses
            {
                strIndex = comboBoxByFeatureGlasses.SelectedIndex.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[5, iIndex] = strIndex[iIndex];
                }
            }
            else
            {
                strIndex = iAll.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[5, iIndex] = strIndex[iIndex];
                }
            }

            if (0 != comboBoxByFeatureMasks.SelectedIndex) //In All, it is not necessary to assign a value to the query conditions whether or not to wear a mask
            {
                strIndex = comboBoxByFeatureMasks.SelectedIndex.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[6, iIndex] = strIndex[iIndex];
                }
            }
            else
            {
                strIndex = iAll.ToString();
                for (iIndex = 0; iIndex < strIndex.Length; iIndex++)
                {
                    tQuery.cQueryCondition[6, iIndex] = strIndex[iIndex];
                }
            }

            NetFileQueryVcaResult tVcaResult = new NetFileQueryVcaResult();
            int iiiii = Marshal.SizeOf(tVcaResult);


            NetFileQueryVcaResult[] tResult = new NetFileQueryVcaResult[CommonLen.FACE_MAX_PAGE_COUNT];
            for (iIndex = 0; iIndex < CommonLen.FACE_MAX_PAGE_COUNT; iIndex++)
            {
                tResult[iIndex] = new NetFileQueryVcaResult();
                tResult[iIndex].tFileAttr = new VcaFileAttr[NVSSDK.MAX_VCA_FILE_COUNT];
                for (int j = 0; j < NVSSDK.MAX_VCA_FILE_COUNT; j++)
                {
                    tResult[iIndex].tFileAttr[j] = new VcaFileAttr();
                    tResult[iIndex].tFileAttr[j].cFileName = new byte[CommonLen.LEN_64];
                    tResult[iIndex].tFileAttr[j].cReserve = new byte[CommonLen.LEN_64];
                }

                tResult[iIndex].tBegTime = new NVS_FILE_TIME();
                tResult[iIndex].tEndTime = new NVS_FILE_TIME();

                tResult[iIndex].cExAttr = new byte[NVSSDK.MAX_VCA_ATTR_COunt*CommonLen.LEN_256];
            }

            IntPtr ipInfo = Marshal.AllocHGlobal(Marshal.SizeOf(tQuery));
            Marshal.StructureToPtr(tQuery, ipInfo, true);//It is prone to memory leaks at false

            IntPtr ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(NetFileQueryVcaResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);

            int iRet = NVSSDK.NetClient_Query_V5(m_iLogonId, NVSSDK.CMD_NETFILE_QUERY_VCA, m_iChannelNo, ipInfo, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(NetFileQueryVcaResult)));

            if (iRet != 0)
            {
                MessageBox.Show("Search Failed");
                return;
            }

            //Restore the structure array 
            for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            {
                IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(NetFileQueryVcaResult)));
                tResult[i] = (NetFileQueryVcaResult)Marshal.PtrToStructure(ptr, typeof(NetFileQueryVcaResult));
            }

            listViewSearchByFeature.Items.Clear();
            
            for (int i = 0; i < tResult[0].iCurPageCount; ++i)
            {
                int iCount = listViewSearchByFeature.Items.Count;

                ListViewItem one = new ListViewItem();
                one.Text = (i + 1).ToString();
                one.SubItems.Add((tResult[i].iChanNo).ToString());
                string strTimeBegin = tResult[i].tBegTime.m_iYear.ToString() + "-" + tResult[i].tBegTime.m_iMonth.ToString() + "-" + tResult[i].tBegTime.m_iDay.ToString() + " " + tResult[i].tBegTime.m_iHour.ToString() + ":" + tResult[i].tBegTime.m_iMinute.ToString() + ":" + tResult[i].tBegTime.m_iSecond.ToString();
                one.SubItems.Add(strTimeBegin);
                string strTimeEnd = tResult[i].tEndTime.m_iYear.ToString() + "-" + tResult[i].tEndTime.m_iMonth.ToString() + "-" + tResult[i].tEndTime.m_iDay.ToString() + " " + tResult[i].tEndTime.m_iHour.ToString() + ":" + tResult[i].tEndTime.m_iMinute.ToString() + ":" + tResult[i].tEndTime.m_iSecond.ToString();//endtime
                one.SubItems.Add(strTimeEnd);

                //Result attribute 1, age range (age value)
                byte[] cExAttrAge = new byte[CommonLen.LEN_256];
                Array.Copy(tResult[i].cExAttr, 0, cExAttrAge, 0, CommonLen.LEN_256);
                string strAge = CommonFunction.ByteToStr(cExAttrAge);
                one.SubItems.Add(strAge);

                //Result attribute 2, gender (1-male, 2-female, 3-unknown)
                byte[] cExAttrSex = new byte[CommonLen.LEN_256];
                Array.Copy(tResult[i].cExAttr, CommonLen.LEN_256, cExAttrSex, 0, CommonLen.LEN_256);
                string strSex = CommonFunction.ByteToStr(cExAttrSex);
                one.SubItems.Add(strSex);

                //Result attribute 3, nation (1-Han nationality, 2-minority nationality)
                byte[] cExAttrNation = new byte[CommonLen.LEN_256];
                Array.Copy(tResult[i].cExAttr, CommonLen.LEN_256*2, cExAttrNation, 0, CommonLen.LEN_256);
                string strNation = CommonFunction.ByteToStr(cExAttrNation);
                one.SubItems.Add(strNation);

                //Result attribute 4, wearing glasses (1-wearing, 2-not wearing)
                byte[] cExAttrGlasses = new byte[CommonLen.LEN_256];
                Array.Copy(tResult[i].cExAttr, CommonLen.LEN_256 * 3, cExAttrGlasses, 0, CommonLen.LEN_256);
                string strGlasses = CommonFunction.ByteToStr(cExAttrGlasses);
                one.SubItems.Add(strGlasses);

                //Result attribute 5, wearing a mask (1-wearing, 2-not wearing)
                byte[] cExAttrMarks = new byte[CommonLen.LEN_256];
                Array.Copy(tResult[i].cExAttr, CommonLen.LEN_256 * 4, cExAttrMarks, 0, CommonLen.LEN_256);
                string strMarks = CommonFunction.ByteToStr(cExAttrMarks);
                one.SubItems.Add(strMarks);

                string strSmallPic = CommonFunction.ByteToStr(tResult[i].tFileAttr[0].cFileName);
                one.SubItems.Add(strSmallPic);

                string strBigPic = CommonFunction.ByteToStr(tResult[i].tFileAttr[1].cFileName);
                one.SubItems.Add(strBigPic);

                listViewSearchByFeature.Items.Insert(iCount, one);
            }
        }

        private void checkBoxByFeatureAllChn_CheckedChanged(object sender, EventArgs e)
        {
            foreach (Control c in groupBoxofSearchbyFeature.Controls)//Traverse all controls in ChannelListByPic
            {
                if (c is CheckBox)//Traverse CheckBox controls only
                {
                    if (((CheckBox)c).CheckState != checkBoxByFeatureAllChn.CheckState)
                    {
                        if (((CheckBox)c).Enabled == false)
                        {
                            ((CheckBox)c).CheckState = CheckState.Unchecked;
                        }
                        else
                            ((CheckBox)c).CheckState = checkBoxByFeatureAllChn.CheckState;

                        if (checkBoxByFeatureAllChn.CheckState == CheckState.Checked)
                            ((CheckBox)c).Enabled = false;
                        else
                            ((CheckBox)c).Enabled = true;
                    }
                    else
                    {
                        if (checkBoxByFeatureAllChn.CheckState == CheckState.Checked)
                        {
                            ((CheckBox)c).Enabled = false;
                        }

                    }
                }
            }
        }


        private void comboBoxSubType_SelectedIndexChanged(object sender, EventArgs e)
        {

        }

//tabPageFaceDetect:Face detection module------------

        private void UI_Init_FaceDetect()
        {
            comboBoxFaceDetectState.Items.Clear();
            comboBoxFaceDetectState.Items.Insert(0, "IPC");
            if (comboBoxChanelNo.Items.Count > 1)
            {
                comboBoxFaceDetectState.Items.Insert(1, "NVR");
            }
            comboBoxFaceDetectState.SelectedIndex = 0;


            comboBoxFaceDetectPushMode.DataSource = null;
            comboBoxFaceDetectPushMode.Items.Clear();
            DataTable dtPushMode = new DataTable();
            dtPushMode.Columns.Add("Text", Type.GetType("System.String"));
            dtPushMode.Columns.Add("Value", Type.GetType("System.Int32"));
            comboBoxFaceDetectPushMode.DataSource = dtPushMode;
            comboBoxFaceDetectPushMode.DisplayMember = "Text";   // Text, that is, explicit text
            comboBoxFaceDetectPushMode.ValueMember = "Value";    // Value, the actual value
            dtPushMode.Rows.Add("Fastest", 1);
            dtPushMode.Rows.Add("Quality best", 2);
            dtPushMode.Rows.Add("Timing", 4);
            dtPushMode.Rows.Add("Consecutively", 6);
            comboBoxFaceDetectPushMode.SelectedIndex = 0;


            comboBoxFaceDetectSnapMode.DataSource = null;
            comboBoxFaceDetectSnapMode.Items.Clear();
            DataTable dtSnapMode = new DataTable();
            dtSnapMode.Columns.Add("Text", Type.GetType("System.String"));
            dtSnapMode.Columns.Add("Value", Type.GetType("System.Int32"));
            comboBoxFaceDetectSnapMode.DataSource = dtSnapMode;
            comboBoxFaceDetectSnapMode.DisplayMember = "Text";   // Text, that is, explicit text
            comboBoxFaceDetectSnapMode.ValueMember = "Value";    // Value, the actual value
            dtSnapMode.Rows.Add("All snap", 1);
            dtSnapMode.Rows.Add("High quality", 2);
            dtSnapMode.Rows.Add("Custom", 3);
            comboBoxFaceDetectSnapMode.SelectedIndex = 0;

        }

        private void UpdateFaceDetect()
        {
            if (m_iLogonId < 0)
            {
                return;
            }
            
            GetAnyScene();
            GetFaceDetect();
            GetBigPicUploadParam();
            GetSmallPicUploadParam();
        }

        private void buttonFaceDetecSet_Click(object sender, EventArgs e)
        {
            int iMinFaceSize = Convert.ToInt32(textBoxFaceDetectMInSize.Text.ToString());
            if (iMinFaceSize > 10000 || iMinFaceSize < 1)
            {
                MessageBox.Show("Minimum face size can be entered:1~10000!");
                return;
            }

            if (0 != SetAnyScene())
            {
                return;
            }
            if (0 != SetFaceDetect())
            {
                return;
            }

            SetBigPicUploadParam();
            SetSmallPicUploadParam();

        }

        private void buttonFaceDetectGet_Click(object sender, EventArgs e)
        {
            UpdateFaceDetect();
        }
        
        private int GetAnyScene()
        {
	        int iRet = -1;

	        AnyScene stAnyScene =  new AnyScene();
            stAnyScene.cSceneName = new char[CommonLen.LEN_32];
            stAnyScene.iBufSize = Marshal.SizeOf(stAnyScene);
	        stAnyScene.iSceneID = 0;
	        stAnyScene.iDevType = comboBoxFaceDetectState.SelectedIndex;
	       
            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(stAnyScene));
            Marshal.StructureToPtr(stAnyScene, ipParam, true);//It is prone to memory leaks at false

            iRet = NVSSDK.NetClient_GetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_ANYSCENE, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(AnyScene)), IntPtr.Zero);
	        
            stAnyScene = (AnyScene)Marshal.PtrToStructure(ipParam, typeof(AnyScene));
            
            if (iRet == 0)
	        {
                int iEnableFaceDetect = (stAnyScene.iArithmetic>>2) & 0x01;
                checkBoxFaceDetectState.Checked = (1 == iEnableFaceDetect)? true:false ;
	        }

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }

        private int SetAnyScene()
        {
	        int iRet = -1;

            AnyScene stAnyScene = new AnyScene();
            stAnyScene.cSceneName = new char[CommonLen.LEN_32];
            stAnyScene.iBufSize = Marshal.SizeOf(stAnyScene);
            stAnyScene.iSceneID = 0;
            stAnyScene.iDevType = comboBoxFaceDetectState.SelectedIndex;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(stAnyScene));
            Marshal.StructureToPtr(stAnyScene, ipParam, true);//It is prone to memory leaks at false

            iRet = NVSSDK.NetClient_GetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_ANYSCENE, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(AnyScene)), IntPtr.Zero);

            stAnyScene = (AnyScene)Marshal.PtrToStructure(ipParam, typeof(AnyScene));

	        if (iRet != 0)
	        {
                Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory
                return iRet;
	        }

            stAnyScene.iDevType = comboBoxFaceDetectState.SelectedIndex;
            int iFace = (true == checkBoxFaceDetectState.Checked)? 1:0;
            if (1 == iFace)
	        {
		        stAnyScene.iArithmetic |= (iFace<<2);
	        }
	        else
	        {
		        stAnyScene.iArithmetic = stAnyScene.iArithmetic&~(1<<2);
	        }

            Marshal.StructureToPtr(stAnyScene, ipParam, true);//It is prone to memory leaks at false
            iRet = NVSSDK.NetClient_SetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_ANYSCENE, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(AnyScene)));
            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }

        private int GetFaceDetect()
        {
	        int iRet = -1;

            FaceDetectArithmetic tParam = new FaceDetectArithmetic();
            tParam.ptArea = new POINT[CommonLen.MAX_FACE_DETECT_AREA_COUNT];
            tParam.iBufSize = Marshal.SizeOf(tParam);
	        tParam.iSceneID = 0;
            tParam.iDevType = comboBoxFaceDetectState.SelectedIndex;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
            Marshal.StructureToPtr(tParam, ipParam, true);//It is prone to memory leaks at false
            
            iRet = NVSSDK.NetClient_GetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(FaceDetectArithmetic)), IntPtr.Zero);
            tParam = (FaceDetectArithmetic)Marshal.PtrToStructure(ipParam, typeof(FaceDetectArithmetic));
            if (0 == iRet)
	        {

                for (int i = 0; i < comboBoxFaceDetectPushMode.Items.Count; ++i)
                {
                    comboBoxFaceDetectPushMode.SelectedIndex = i;
                    if ((int)comboBoxFaceDetectPushMode.SelectedValue == tParam.iPushMode)
                    {
                        break;
                    }
                    if (comboBoxFaceDetectPushMode.Items.Count -1 == i)//The first option is selected by default if it does not meet the conditions
                    {
                        comboBoxFaceDetectPushMode.SelectedIndex = 0;
                    }
                }
                for (int i = 0; i < comboBoxFaceDetectSnapNum.Items.Count; ++i)
		        {
                    if (i+1 == tParam.iSnapTimes)
                    {
                        comboBoxFaceDetectSnapNum.SelectedIndex = i;
                        break;
                    }
                    if (comboBoxFaceDetectSnapNum.Items.Count - 1 == i)//The first option is selected by default if it does not meet the conditions
                    {
                        comboBoxFaceDetectSnapNum.SelectedIndex = 0;
                    }
		        }
                for (int i = 0; i < comboBoxFaceDetectSnapMode.Items.Count; ++i)
		        {
                    comboBoxFaceDetectSnapMode.SelectedIndex = i;
                    if ((int)comboBoxFaceDetectSnapMode.SelectedValue == tParam.iSnapMode)
                    {
                        break;
                    }
                    if (comboBoxFaceDetectSnapMode.Items.Count - 1 == i)//The first option is selected by default if it does not meet the conditions
                    {
                        comboBoxFaceDetectSnapMode.SelectedIndex = 0;
                    }
		        }
                

                trackBarFaceDetectSnapLevel.Value = tParam.iSnapLevel;
                labelFaceDetectSnapLevelValue.Text = tParam.iSnapLevel.ToString();
                
                textBoxFaceDetectMInSize.Text = tParam.iMinSizeEx.ToString();

                trackBarFaceDetectSnapSpace.Value = tParam.iSnapSpace;
                labelFaceDetectSnapSpaceValue.Text = tParam.iSnapSpace.ToString();

                trackBarFaceDetectBright.Value = tParam.iExposureBright;
                labelFaceDetectBrightValue.Text = tParam.iExposureBright.ToString();


                checkBoxFaceDetectShowRule.Checked = (1 == tParam.iDisplayRule ? true : false);
                checkBoxFaceDetectShowTarget.Checked = (1 == tParam.iDisplayTarget ? true : false);

	        }

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }

        private int SetFaceDetect()
        {
	        int iRet = -1;

            FaceDetectArithmetic tParam = new FaceDetectArithmetic();
            tParam.ptArea = new POINT[CommonLen.MAX_FACE_DETECT_AREA_COUNT];
            tParam.iBufSize = Marshal.SizeOf(tParam);
            tParam.iSceneID = 0;
            tParam.iDevType = comboBoxFaceDetectState.SelectedIndex;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
            Marshal.StructureToPtr(tParam, ipParam, true);//It is prone to memory leaks at false

            iRet = NVSSDK.NetClient_GetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(FaceDetectArithmetic)), IntPtr.Zero);
            tParam = (FaceDetectArithmetic)Marshal.PtrToStructure(ipParam, typeof(FaceDetectArithmetic));
	        if (0 != iRet)
	        {
		        return iRet;
	        }

            tParam.iDevType = comboBoxFaceDetectState.SelectedIndex;

            tParam.iPushMode = (int)comboBoxFaceDetectPushMode.SelectedValue;
        	
	        if (1 == tParam.iPushMode || 2 == tParam.iPushMode)
	        {
                tParam.iSnapTimes = (int)comboBoxFaceDetectSnapNum.SelectedIndex+1;//When the push map strategy is the fastest and best, the number of snapshots iSnapTimes takes effect
	        }
	        else if (4 == tParam.iPushMode)
	        {
                tParam.iSnapTimes = (int)comboBoxFaceDetectSnapNum.SelectedIndex + 1;//When the push map policy is timed, the number of snapshots iSnapTimes and snapshot interval iSnapSpace take effect
                tParam.iSnapSpace = (int)trackBarFaceDetectSnapSpace.Value;
	        }
	        else if(6 == tParam.iPushMode)
	        {
		        //When the push map strategy is continuous, the number of snapshots and snapshot interval do not take effect
	        }

            tParam.iSnapMode = (int)comboBoxFaceDetectSnapMode.SelectedValue;
	        if (tParam.iSnapMode == 3)
	        {
                tParam.iSnapLevel = (int)trackBarFaceDetectSnapLevel.Value;//When the capture mode is user-defined, the iSnapLevel comprehensive quality takes effect
	        }

            tParam.iMinSizeEx = Convert.ToInt32(textBoxFaceDetectMInSize.Text.ToString());
            tParam.iExposureBright = (int)trackBarFaceDetectBright.Value;
            tParam.iDisplayRule = (int)checkBoxFaceDetectShowRule.CheckState;
            tParam.iDisplayTarget = (int)checkBoxFaceDetectShowTarget.CheckState;

            Marshal.StructureToPtr(tParam, ipParam, true);//It is prone to memory leaks at false
            iRet = NVSSDK.NetClient_SetDevConfig(m_iLogonId, NVSSDK.NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(FaceDetectArithmetic)));

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }

        private int GetBigPicUploadParam()
        {
            PicStreamUploadParam tInfo = new PicStreamUploadParam();
            tInfo.iSize = Marshal.SizeOf(tInfo);
	        tInfo.iSceneId	= 0;
	        tInfo.iPicType	= 0;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipParam, true);//It is prone to memory leaks at false

            int iRet = NVSSDK.NetClient_VCAGetConfig(m_iLogonId, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(PicStreamUploadParam)));
            tInfo = (PicStreamUploadParam)Marshal.PtrToStructure(ipParam, typeof(PicStreamUploadParam));
            
            if (0 == iRet)
	        {
                trackBarFaceDetectBigPicQuality.Value = tInfo.iQpvalue;
                labelFaceDetectBigPicQualityValue.Text = tInfo.iQpvalue.ToString();

                checkBoxFaceDetectSnapBigPic.Checked = (1 == tInfo.iSnapEnable ? true : false);
                checkBoxFaceDetectBigPicOsd.Checked = (1 == tInfo.iIsOsd ? true : false);
	        }

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }

        private int SetBigPicUploadParam()
        {
            PicStreamUploadParam tInfo = new PicStreamUploadParam();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iSceneId = 0;
            tInfo.iPicType = 0;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipParam, true);//It is prone to memory leaks at false

            int iRet = NVSSDK.NetClient_VCAGetConfig(m_iLogonId, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(PicStreamUploadParam)));
            tInfo = (PicStreamUploadParam)Marshal.PtrToStructure(ipParam, typeof(PicStreamUploadParam));
            
            if (0 != iRet)
	        {
                Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory
                return iRet;
	        }

            tInfo.iSize = Marshal.SizeOf(tInfo);
	        tInfo.iSceneId		= 0;
	        tInfo.iRuleNo		= 0;
	        tInfo.iPicType		= 0;

            tInfo.iSnapEnable = (int)checkBoxFaceDetectSnapBigPic.CheckState;
            tInfo.iIsOsd = (int)checkBoxFaceDetectBigPicOsd.CheckState;
            tInfo.iQpvalue = (int)trackBarFaceDetectBigPicQuality.Value;

            Marshal.StructureToPtr(tInfo, ipParam, true);//It is prone to memory leaks at false
            iRet = NVSSDK.NetClient_VCASetConfig(m_iLogonId, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(PicStreamUploadParam)));

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }


        private int GetSmallPicUploadParam()
        {
            PicStreamUploadParam tInfo = new PicStreamUploadParam();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iSceneId = 0;
            tInfo.iPicType = 1;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipParam, true);//It is prone to memory leaks at false

            int iRet = NVSSDK.NetClient_VCAGetConfig(m_iLogonId, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(PicStreamUploadParam)));
            tInfo = (PicStreamUploadParam)Marshal.PtrToStructure(ipParam, typeof(PicStreamUploadParam));

            if (0 == iRet)
            {
                trackBarFaceDetectSmallPicQuality.Value = tInfo.iQpvalue;
                labelFaceDetectSmallPicQualityValue.Text = tInfo.iQpvalue.ToString();
            }

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

            return iRet;
        }

        private int SetSmallPicUploadParam()
        {
            PicStreamUploadParam tInfo = new PicStreamUploadParam();
            tInfo.iSize = Marshal.SizeOf(tInfo);
            tInfo.iSceneId = 0;
            tInfo.iPicType = 1;

            IntPtr ipParam = Marshal.AllocHGlobal(Marshal.SizeOf(tInfo));
            Marshal.StructureToPtr(tInfo, ipParam, true);//It is prone to memory leaks at false

            int iRet = NVSSDK.NetClient_VCAGetConfig(m_iLogonId, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(PicStreamUploadParam)));
            tInfo = (PicStreamUploadParam)Marshal.PtrToStructure(ipParam, typeof(PicStreamUploadParam));

	        if (0 != iRet)
	        {
                Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory
                return iRet;
	        }

            tInfo.iSize     = Marshal.SizeOf(tInfo);
	        tInfo.iSceneId	= 0;
	        tInfo.iRuleNo	= 0;
	        tInfo.iPicType	= 1;
            tInfo.iQpvalue  = (int)trackBarFaceDetectSmallPicQuality.Value;

            Marshal.StructureToPtr(tInfo, ipParam, true);//It is prone to memory leaks at false
            iRet = NVSSDK.NetClient_VCASetConfig(m_iLogonId, NVSSDK.VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, ipParam, Marshal.SizeOf(typeof(PicStreamUploadParam)));

            Marshal.FreeHGlobal(ipParam);//Free the allocated unmanaged memory

	        return iRet;
        }

        private void comboBoxFaceDetectState_SelectedIndexChanged(object sender, EventArgs e)
        {
            UpdateFaceDetect();
        }

        private void trackBarFaceDetectSnapSpace_Scroll(object sender, EventArgs e)
        {
            labelFaceDetectSnapSpaceValue.Text = trackBarFaceDetectSnapSpace.Value.ToString();
        }

        private void trackBarFaceDetectSnapLevel_Scroll(object sender, EventArgs e)
        {
            labelFaceDetectSnapLevelValue.Text = trackBarFaceDetectSnapLevel.Value.ToString();
        }

        private void trackBarFaceDetectBright_Scroll(object sender, EventArgs e)
        {
            labelFaceDetectBrightValue.Text = trackBarFaceDetectBright.Value.ToString();
        }

        private void trackBarFaceDetectBigPicQuality_Scroll(object sender, EventArgs e)
        {
            labelFaceDetectBigPicQualityValue.Text = trackBarFaceDetectBigPicQuality.Value.ToString();
        }

        private void trackBarFaceDetectSmallPicQuality_Scroll(object sender, EventArgs e)
        {
            labelFaceDetectSmallPicQualityValue.Text = trackBarFaceDetectSmallPicQuality.Value.ToString();
        }

        private void NormalMode_CheckedChanged(object sender, EventArgs e)
        {
            if (radioNormalMode.Checked)
            {
                textBoxIP.Text = "192.168.1.2";
                labelIP.Text = "IPAddr";
                labelPort.Text = "DevPort";
                textBoxPort.Text = "3000";
                labelWanPort.Visible = false;
                textBoxWanPort.Visible = false;
                m_iLogonMode = NVSSDK.SERVER_NORMAL;
                labelWanIP.Visible = false;
                textBoxWanIP.Visible = false;
            }

            if (radioActiveMode.Checked)
            {
                textBoxIP.Text = "ID0000801940400160610391";
                labelIP.Text = "FactoryID";
                labelPort.Text = "LanPort";
                textBoxPort.Text = "6004";
                labelWanPort.Visible = true;
                textBoxWanPort.Visible = true;
                m_iLogonMode = NVSSDK.SERVER_ACTIVE;
                labelWanIP.Visible = true;
                textBoxWanIP.Visible = true;
            }
        }

        private void button2_Click(object sender, EventArgs e)
        {
            button1.Enabled = false;
            button2.Enabled = false;
            progressBar1.Value = 0;
            labelPicCommitProcess.Text = "0";
            //You must query the face database first 
            if (comboBoxPicLib.SelectedIndex < 0)
            {
                MessageBox.Show("Please search or select the face library first!");
                return;
            }
            //Select the folder to export
            int _iPageNo = 0;
            string cstrFilePath = BrowseFolder();
            if (cstrFilePath == "")
            {
                return;
            }
            m_listExportFacePic = new List<FaceInfo>();
            m_listExportFacePic.Clear();

            FaceQuery tQuery = new FaceQuery();
            tQuery.cBirthStart = new byte[CommonLen.LEN_16];
            tQuery.cBirthEnd = new byte[CommonLen.LEN_16];
            tQuery.cName = new byte[CommonLen.LEN_64];
            tQuery.cCertNum = new byte[CommonLen.LEN_64];
            tQuery.cLibUUID = new byte[CommonLen.LEN_UUID];

            //necessary field
            tQuery.iSize = Marshal.SizeOf(tQuery);
            tQuery.iChanNo = m_iChannelNo;
            string strTemp = comboBoxPicLib.SelectedValue.ToString();
            tQuery.iLibKey = int.Parse(SplitLibKeyAndUUID(strTemp, 0));
            tQuery.iPageCount = NVSSDK.FACE_MAX_PAGE_COUNT; ;//
            CommonFunction.BytesCopy(dateTimePickerPicBegin.Text, tQuery.cBirthStart);
            CommonFunction.BytesCopy(dateTimePickerPicEnd.Text, tQuery.cBirthEnd);
            //end

            //unnecessary field
            tQuery.iSex = comboBoxPicSex.SelectedIndex;
            tQuery.iNation = comboBoxPicNation.SelectedIndex;//nation, 0 unknown , There is only one value "unknown" for the time being. The actual use is to add the value as required	
            tQuery.iPlace = (comboBoxPicCity.SelectedIndex & 0xffff) | ((comboBoxPicProvince.SelectedIndex & 0xffff) << 16);//City and province need to be spliced bit by bit,//There is only one value "unknown" for the time being. The actual use is to add the value as required	
            tQuery.iCertType = comboBoxPicCardType.SelectedIndex;
            tQuery.iModeling = comboBoxPicModel.SelectedIndex;
            CommonFunction.BytesCopy(textBoxPicName.Text, tQuery.cName);
            CommonFunction.BytesCopy(textBoxPicCardNum.Text, tQuery.cCertNum);
            //end

            //The normal devices do not need these fields
            CommonFunction.BytesCopy((SplitLibKeyAndUUID(strTemp, 1)), tQuery.cLibUUID);
            //end

            listViewPicInfo.Items.Clear();

            tQuery.iPageNo = _iPageNo;

            IntPtr ipQueryInfo = Marshal.AllocCoTaskMem(Marshal.SizeOf(tQuery));
            Marshal.StructureToPtr(tQuery, ipQueryInfo, true);//It is prone to memory leaks at false

            IntPtr ipResult = IntPtr.Zero;
            //Array of marshaling structures
            FaceQueryResult[] tFacePicInfo = new FaceQueryResult[NVSSDK.FACE_MAX_PAGE_COUNT];
            for (int i = 0; i < tFacePicInfo.Length; i++)
            {
                tFacePicInfo[i] = new FaceQueryResult();
            }
            ipResult = Marshal.AllocHGlobal(Marshal.SizeOf(typeof(FaceQueryResult)) * NVSSDK.FACE_MAX_PAGE_COUNT);
            //Marshal.StructureToPtr(tFacePicInfo, ipResult, true);//It is prone to memory leaks at false

            int iRet = -1;
            iRet = NVSSDK.NetClient_FaceConfig(m_iLogonId, NVSSDK.FACE_CMD_QUERY, m_iChannelNo, ipQueryInfo, Marshal.SizeOf(tQuery), ipResult, Marshal.SizeOf(typeof(FaceQueryResult)));


            //tFacePicInfo = (FaceQueryResult)Marshal.PtrToStructure(ipFacePicInfo, typeof(FaceQueryResult));

            if (0 != iRet)
            {
                MessageBox.Show("The face bwasemap query failed, return value:" + iRet.ToString());
                return;
            }

            //Restore the structure array  
            for (int i = 0; i < NVSSDK.FACE_MAX_PAGE_COUNT; i++)
            {
                IntPtr ptr = (IntPtr)((UInt32)ipResult + i * Marshal.SizeOf(typeof(FaceQueryResult)));
                tFacePicInfo[i] = (FaceQueryResult)Marshal.PtrToStructure(ptr, typeof(FaceQueryResult));
                if (i < tFacePicInfo[0].iTotal)
                {
                    m_listExportFacePic.Add(tFacePicInfo[i].tFace);
                }
            }

            //Page number processing
            int iTotalPage = tFacePicInfo[0].iTotal / NVSSDK.FACE_MAX_PAGE_COUNT;
            if (tFacePicInfo[0].iTotal % NVSSDK.FACE_MAX_PAGE_COUNT > 0 && tFacePicInfo[0].iTotal > 0)
            {
                iTotalPage++;
            }
            if (iTotalPage != m_iTolalPage)
            {
                m_iTolalPage = iTotalPage;
                comboBoxPicPage.Items.Clear();
                for (int i = 0; i < m_iTolalPage; ++i)
                {
                    comboBoxPicPage.Items.Insert(i, (i + 1).ToString());
                }
            }
            m_iCurPage = _iPageNo;

            if (comboBoxPicPage.Items.Count > 0)
            {
                comboBoxPicPage.SelectedIndex = m_iCurPage;
            }

            if (tFacePicInfo[0].iTotal > 0)
            {
                _iPageNo++;
            }
            labelPicPage.Text = _iPageNo.ToString() + "/" + m_iTolalPage.ToString();

            for (int i = 0; i < tFacePicInfo[0].iPageCount; ++i)
            {
                UI_UpdateFaceList(ref listViewPicInfo, ref tFacePicInfo[i].tFace, -1);
            }
            //After the query, start to export the face base map

            Thread thread = new Thread(new ParameterizedThreadStart(ExportPic));
            thread.IsBackground = true;
            thread.Start(cstrFilePath);//Start a new thread, and first pass the name of the face database to the sub thread to avoid the control crash caused by the sub thread operation

            Marshal.FreeHGlobal(ipQueryInfo);//Free the allocated unmanaged memory
            Marshal.FreeHGlobal(ipResult);//Free the allocated unmanaged memory

        }

        private void Client_FormClosing(object sender, FormClosingEventArgs e)
        {
            System.Environment.Exit(System.Environment.ExitCode);
            this.Dispose();
            this.Close();
        }
    }
}