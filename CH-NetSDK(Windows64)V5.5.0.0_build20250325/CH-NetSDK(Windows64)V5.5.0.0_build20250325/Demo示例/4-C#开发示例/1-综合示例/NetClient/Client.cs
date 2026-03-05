using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Windows.Forms;
using System.Runtime.InteropServices;
using System.Threading;
using System.IO;

namespace NetClient
{
    public partial class Client : BaseForm
    {
        //public IntPtr hwndReplayfrm = IntPtr.Zero; 
        //public ReplayForm Replayfrm = null;

        private const int T_AUDIO8 = 0;
        private const int T_YUV420 = 1;
        private const int T_YUV422 = 2;

        private const int REC_FILE_TYPE_NORMAL = 0;
        private const int REC_FILE_TYPE_MP4 = 10;

        private const int MAX_DEVICE_NUM = 16;

        private RECVDATA_NOTIFY RecvDataNotify = null;
        private COMRECV_NOTIFY ComRecvNotify = null;
        private DECYUV_NOTIFY DecYuvNotify = null;

        private MAIN_NOTIFY_V4 MainNotify_V40 = null;

        private ALARM_NOTIFY_V4 AlarmNotify_V40 = null;
        private PARACHANGE_NOTIFY_V4 ParamChangeNotify_V40 = null;

        private static FileStream fsSdv = null;
        private static FileStream fsYuv = null;
        private static FileStream fsPcm = null;

        private const int FTP_CMD_SET_SNAPSHOT = 0;
        private const int FTP_CMD_GET_SNAPSHOT = 4;

        private string strContinuousSnapPath;
        private int m_iSnapCount = 0;
        private System.Timers.Timer tTimer;

        private bool m_bTalkEnd = true;
        String m_strTalkFileName = ""; //filename of the intercom file
     
        //video window array
        VideoWindow[] m_video;

        //Array of connection state structures corresponding to the video window
        CONNECT_STATE[] m_conState;

        //List of all logged in devices
        CLIENTINFO [] m_cltInfo;

        //Login Device ID List
        string[] m_sID;

        //current video window tag, starting from 0
        int m_iCurrentFrame = 0;

        //Maximum number of video windows that can be displayed
        const int CONST_iFrameNum = 16;

        //Double-click times, for switching between multi-screen and single-screen, single-screen and full-screen, and returning to the original state
        //Multi-window0 Single-window1 Fullscreen2
        int m_iDBClick = 0;

        //Double-click DSM Network Video Server
        bool m_blNSClick = false;

        //Declare the DSMInfo form
        FormDSM formDSM = null;
        public Client()
        {
            InitializeComponent();
            CheckForIllegalCrossThreadCalls = false;
            StartUp();            
        }


        //Start the SDK and initialize
        private void StartUp()
        {
            //Sets the default network port used by clients and controls
            NVSSDK.NetClient_SetPort(3000, 6000);

            //Start the SDK
            NVSSDK.NetClient_Startup();

            //Initialize the NSLook library
            NVSSDK.NSLook_Startup();

            // Set login success callback
            MainNotify_V40 = MyMAIN_NOTIFY_V4;
            AlarmNotify_V40 = MyAlarm_NOTIFY_V4;
            ParamChangeNotify_V40 = MyParaChange_NOTIFY_V4;
            NVSSDK.NetClient_SetNotifyFunction_V4(MainNotify_V40, AlarmNotify_V40, ParamChangeNotify_V40, null, null);

            //Notice:
            //NetClient_SetNotifyFunction_V4 and NetClient_SetMSGHandle can only take effect after setting one
            //Set message notification ID
            //NVSSDK.NetClient_SetMSGHandle(SDKConstMsg.WM_MAIN_MESSAGE, this.Handle, SDKConstMsg.MSG_PARACHG, SDKConstMsg.MSG_ALARM);

            //Create a video window object
            m_conState = new CONNECT_STATE[16];
            m_video = new VideoWindow[16];
            for (int i = 0; i < 16; i++)
            {
                //Initialize the connection state structure
                m_conState[i].m_iChannelNO = -1;
                m_conState[i].m_iLogonID = -1;
                m_conState[i].m_uiConID = UInt32.MaxValue;

                //Modify video window properties and register click and double-click events
                m_video[i] = new VideoWindow();                
                m_video[i].Hide();
                m_video[i].pnlVideo.TabIndex = i;
                m_video[i].pnlVideo.Click += new EventHandler(Video_Click);
                m_video[i].pnlVideo.DoubleClick += new EventHandler(Video_DBClick);
            }

            m_cltInfo = new CLIENTINFO[MAX_DEVICE_NUM];
            m_sID = new string[MAX_DEVICE_NUM];
            for (int i = 0; i < MAX_DEVICE_NUM; i++)
            {
                //Connect up to 16 devices
                m_cltInfo[i].m_iServerID = -1;
            }
            //Add the video window to the main form
            this.Controls.AddRange(m_video);
            cboChannel.SelectedIndex = 0;
            cboMode.SelectedIndex = 0;
            cboScreen.SelectedIndex = 1;
            cboStream.SelectedIndex = 0;
            comboBoxScene.SelectedIndex = 0;
            comboBoxEventID.SelectedIndex = 0;
            comboBoxRule.SelectedIndex = 0;

            //4 screens are displayed by default
            DisplayWindows(2);

            //Set the initial ID to connect to the registry
            btnDSMLogon.Tag = -1;
        }

        //Show _iRows row video window
        private void DisplayWindows(int _iRows)
        {
            //height of the client area of ??the main form
            int iHeight =( ClientSize.Height - 150)/ _iRows;

            //The height of four to three
            int iWidth = iHeight*4/3;

            //Hide each video window
            for (int i = _iRows * _iRows; i < 16; i++)
            {
                m_video[i].Hide();
            }

            //Display and adjust the first _iRows*_iRows video windows
            for (int i = 0; i < _iRows; i++)
            {
                for (int j = 0; j < _iRows; j++)
                {
                    m_video[i * _iRows + j].Left = j * iWidth;
                    m_video[i * _iRows + j].Top = i * iHeight;
                    m_video[i * _iRows + j].Width = iWidth;
                    m_video[i * _iRows + j].Height = iHeight;
                    m_video[i * _iRows + j].Show();
                }
            }
            //If only one screen is displayed, modify the number of double-clicks
            m_iDBClick = _iRows == 1 ? 1 : 0;
        }

        //Rewrite the message handler to handle custom messages
        protected override void DefWndProc(ref System.Windows.Forms.Message m)
        {
            //WM_MAIN_MESSAGE is a custom system message
            if (m.Msg == SDKConstMsg.WM_MAIN_MESSAGE)
            {
                //custom message handler
                this.Notify(m.WParam, m.LParam);
            }

            //default message handler
            base.DefWndProc(ref m);
        }
        public override void OnMessagePro(IntPtr wParam, IntPtr lParam)
        {
            //The lower 16 bits of wParam are the type of the message;
            int iMsgType = wParam.ToInt32() & 0xFFFF;
            //lParam, the NVS_IPAndID address of the information structure of the network video server NVS
            //Marshal.PtrToStructure function converts the Intptr address into a structure
            //NVS_IPAndID  ipAndID = (NVS_IPAndID)Marshal.PtrToStructure(lParam, typeof(NVS_IPAndID));

            switch (iMsgType)
            {
                //login status message
                //param1 login IP
                //param2 login ID
                //param3 login status
                case 29:
                    {
                        MessageBox.Show(" Download interrupt");
                        break;
                    }
                case SDKConstMsg.WCM_LOGON_NOTIFY:
                    {
                        NVS_IPAndID ipAndID = (NVS_IPAndID)Marshal.PtrToStructure(lParam, typeof(NVS_IPAndID));
                        int i = wParam.ToInt32();
                        LogonNotify(ipAndID.m_pIP.ToCharArray(), ipAndID.m_pID, wParam.ToInt32() >> 16);
                        break;
                    }


                //Video header message, generated when video header is received.
                //lParam, the NVS_IPAndID address of the information structure of the network video server NVS;
                //wParamHi lower 8 bits indicate the channel number;
                //wParamHi high 8 bits indicate stream type;
                case SDKConstMsg.WCM_VIDEO_HEAD:
                    VideoArrive();
                    break;

                //The video is forcibly disconnected message, which is generated after the current video connection is forcibly disconnected by the agent.
                //param1, video connection ID number
                case SDKConstMsg.WCM_VIDEO_DISCONNECT:
                    VideoDisconnect((UInt32)lParam.ToInt32());
                    break;

                //Network command disconnect message, which is generated when the network connection is disconnected unexpectedly.
                //param1, the IP address of the network video server;
                case SDKConstMsg.WCM_ERR_ORDER:
                    {
                        NVS_IPAndID ipAndID = (NVS_IPAndID)Marshal.PtrToStructure(lParam, typeof(NVS_IPAndID));

                        NetDisconnect(ipAndID.m_pIP);
                        break;
                    }


                //Network data error, this message will be generated when the connection exceeds the maximum number.
                //param1, the IP address of the network video server;
                case SDKConstMsg.WCM_ERR_DATANET:
                    {
                        //VideoDisconnect((UInt32)lParam.ToInt32());
                        MessageBox.Show("Net Service has error, Connect ID:" + lParam.ToInt32().ToString());
                        break;
                    }

                //Recording error message, which is generated when there is an error in the video recording.
                //param1, video connection ID number
                case SDKConstMsg.WCM_RECORD_ERR:
                    RecordError((UInt32)lParam.ToInt32());
                    break;

                case SDKConstMsg.WCM_TALK:
                    Int32 iLParam = wParam.ToInt32() >> 16;
                    if (0 == iLParam)
                    {
                        // TALK_BEGIN_OK, open the intercom successfully
                        openTalkFileDialog.InitialDirectory = "";//Initial directory, no assignment can be
                        openTalkFileDialog.Filter = "All files (*.*)|*.*";//File type

                        if (DialogResult.OK == openTalkFileDialog.ShowDialog())//Pop up the selection box
                        {
                            m_strTalkFileName = openTalkFileDialog.FileName;//Fully qualified name of the opened file
                            Thread t2 = new Thread(new ThreadStart(TalkMethod));
                            t2.Start();
                        }
                        else 
                        {
                            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
                            NVSSDK.NetClient_TalkEnd(iLogonID);
                        }
                    }
                    else if (3 == iLParam)
                    {
                       
                        // TALK_END_OK, close the intercom successfully
                        MessageBox.Show("Intercom completed!");
                    }
                    else if (1 == iLParam || 2 == iLParam)
                    {
                        // TALK_END_OK, close the intercom successfully
                        MessageBox.Show("Intercom error!");
                    }
                    break;

                default:
                    break;
            }
        }

        //WCM_LOGON_NOTIFY message processing function
        private void LogonNotify(char[] _cIP,string _strID,int iLogonState)
        {
            //iLogonState login status
            switch (iLogonState)
            {
                case SDKConstMsg.LOGON_SUCCESS://Login successfully displays the device ID number
                    {
                        textID.Text = _strID;
                        btnLogon.Text = "Logoff";

                        string tmp = new string(_cIP);
                        for (int i = 0; i < MAX_DEVICE_NUM; i++)
                        {
                            string sIP = new string(m_cltInfo[i].m_cRemoteIP);
                            if (0 == sIP.CompareTo(tmp))
                            {
                                m_sID[i] = _strID;
                            }
                        }
                        cboIP.Items.Add(tmp);
                        //After double-clicking the network video server in DSM, connect the video directly
                        if (m_blNSClick)
                        {
                            UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;
                            if (m_conState[m_iCurrentFrame].m_uiConID == UInt32.MaxValue)
                            {
                                cboChannel.SelectedIndex = 0;
                                cboStream.SelectedIndex = 0;
                                btnConnect_Click(btnConnect, EventArgs.Empty);
                            }
                            m_blNSClick = false;
                        }
                        break;
                    }
                case SDKConstMsg.LOGON_FAILED:
                case SDKConstMsg.LOGON_ING:
                case SDKConstMsg.LOGON_RETRY:
                case SDKConstMsg.NOT_LOGON:
                case SDKConstMsg.LOGON_TIMEOUT://Login failed
                    {
                        string tmp = new string(_cIP);
                        for (int i = 0; i < MAX_DEVICE_NUM; i++)
                        {
                            string sIP = new string(m_cltInfo[i].m_cRemoteIP);
                            if (0 == sIP.CompareTo(tmp))
                            {
                                m_cltInfo[i].m_iServerID = -1;
                            }
                        }

                        textID.Text = "";
                        MessageBox.Show("Logon failed!");
                        btnLogon.Text = "Logon";
                        break;
                    }
            }
        }

        //WCM_VIDEO_HEAD message processing function
        private void VideoArrive()
        {
            RECT rect = new RECT();
            
            // start playing when the video arrives
	        NVSSDK.NetClient_StartPlay( m_conState[m_iCurrentFrame].m_uiConID,m_video[m_iCurrentFrame].pnlVideo.Handle, rect, 0);
            btnPlay.Text = "Stop";

            //Modify video status information
            GetWindowStates();
        }

        //WCM_VIDEO_DISCONNECT message processing function
        private void VideoDisconnect(UInt32 _uiConID)
        {
            bool isCurrentFrame = false;
            for (int i = 0; i < CONST_iFrameNum; i++)
            {
                //After the video is forcibly disconnected, refresh the corresponding window display
                if (m_conState[i].m_uiConID == _uiConID)
                {
                    //Stop all video reception
                    NVSSDK.NetClient_StopRecv(_uiConID);
                    m_conState[m_iCurrentFrame].m_iChannelNO = -1;
                    m_conState[m_iCurrentFrame].m_uiConID = UInt32.MaxValue;                    
                    m_video[m_iCurrentFrame].Invalidate(true);
                    if (i == m_iCurrentFrame)
                    {
                        isCurrentFrame = true;
                    }
                }
            }

            //If it is the currently selected window, update its status display information
            if (isCurrentFrame == true)
            {
                GetWindowStates();
            }	
        }

        //WCM_ERR_ORDER message handler
        private void NetDisconnect(string _strIP)
        {
            string strMSG = "The network connected to the network video server ";
            strMSG += _strIP;
            strMSG += " is accidentally disconnected!";
            MessageBox.Show(strMSG);
        }

        //WCM_ERR_DATANET message processing function
        private void NetDataError(string _strIP)
        {
            string strMSG = "The network video server ";
            strMSG += _strIP;
            strMSG += "has the largest number of connections!";
            MessageBox.Show(strMSG);
        }

        //WCM_RECORD_ERR message handler
        private void RecordError(UInt32 _uiConID)
        {
            bool isCurrentFrame = false;
            //The window whose connection ID is _uiCon stops recording
            for (int i = 0; i < CONST_iFrameNum; i++)
            {
                if (m_conState[i].m_uiConID == _uiConID)
                {
                    //Stop writing the received data to the file
                    NVSSDK.NetClient_StopCaptureFile(_uiConID);
                    if (i == m_iCurrentFrame)
                    {
                        isCurrentFrame = true;
                    }
                }
            }
            //If the current window has an error in recording, then update the Caption of the recording button to Record
            if (isCurrentFrame == true)
            {
                btnRecord.Text = "Record";
            }
            MessageBox.Show("Record error !");	
        }

        //Clear the state information corresponding to the window
        private void InitWindowStates()
        {
            btnLogon.Text = "Logon";
            btnConnect.Text = "Connect";
            btnPlay.Text = "Play";
            btnRecord.Text = "Record";
            btnMoveAuto.Text = "Auto";
            cboOSDEnable.SelectedIndex = -1;
            cboOSDType.SelectedIndex = -1;
            cboOSDX.SelectedIndex = -1;
            cboOSDY.SelectedIndex = -1;

            //Set the default number of serial ports to 2
            if (cboComNo.Items.Count > 2)
            {
                for (int i = cboComNo.Items.Count-1; i> 1; i--)
                {
                    cboComNo.Items.RemoveAt(i);
                    cboComSend.Items.RemoveAt(i);
                }                
            }
            textOSD.Text = "";
            cboDeviceType.SelectedIndex = -1;
            cboWorkMode.SelectedIndex = -1;
            cboComNo.SelectedIndex = -1;
            textComFormat.Text = "";
            textAddress.Text = "";

            //The current video window is not logged in, clear the video parameters
            if (m_conState[m_iCurrentFrame].m_iLogonID < 0)
            {
                trckBrightness.Value = 0;
                trckContrast.Value = 0;
                trckHue.Value = 0;
                trckSaturation.Value = 0;
            }
        }

        //Set the state information corresponding to the window
        private void GetWindowStates()
        {

            for (int i = 0; i < MAX_DEVICE_NUM; i++)
            {
                if (m_cltInfo[i].m_iServerID == m_conState[m_iCurrentFrame].m_iLogonID)
                {
                    cboIP.Text = new string(m_cltInfo[i].m_cRemoteIP);
                    textID.Text = m_sID[i];
                }
            }

            btnLogon.Text = "Logoff";
            btnConnect.Text = "Disconnect";
            if (NVSSDK.NetClient_GetPlayingStatus(m_conState[m_iCurrentFrame].m_uiConID) == SDKConstMsg.PLAYER_PLAYING)
            {
                btnPlay.Text = "Stop";
            }
            else
            {
                btnPlay.Text = "Play";
            }
            UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;

            // is recording
            if (NVSSDK.NetClient_GetCaptureStatus(uiConID) == 1)
            {
                btnRecord.Text = "Stop";
            }
            Int32 iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            Int32 iComPortCounts = 2;
            Int32 iComPortEnabledStatus = 0;

            //Get the number of serial port numbers of the front-end device
            NVSSDK.NetClient_GetComPortCounts(iLogonID, ref iComPortCounts,ref iComPortEnabledStatus);

            //Add a serial port to reach the number of serial ports supported by the front-end device
            if (cboComNo.Items.Count < iComPortCounts)
            {
                for(int i = cboComNo.Items.Count; i < iComPortCounts; i++)
                {
                    cboComNo.Items.Add("COM" + (i + 1));
                    cboComSend.Items.Add("COM" + (i + 1));
                }
            }
            //Modify the status of character overlay string, character overlay type, device type, and video parameters
            GetOSD();
            GetOSDType();
            GetDeviceType();
            GetVideoParam();
            GetVCAParam();
            //GetFTPUploadConfig();//new add
        }

        //Update the state information corresponding to the window
        private void SetWindowStates()
        {
            //When playing, set the state information corresponding to the window; otherwise, clear the state information corresponding to the window
            if (NVSSDK.NetClient_GetLogonStatus(m_conState[m_iCurrentFrame].m_iLogonID) == SDKConstMsg.LOGON_SUCCESS)
            {
                GetWindowStates();
            }
            else
            {
                InitWindowStates();
            }
        }

        //Click the video display window
        private void Video_Click(object sender, EventArgs e)
        {
            Panel pane = (Panel)sender;

            //Modify the border of the previous video window
            m_video[m_iCurrentFrame].picVideo.BackColor = SystemColors.Control; 
           
            //Modify the current video window mark
            m_iCurrentFrame = pane.TabIndex;

            //modify the channel number
            cboChannel.SelectedIndex = m_conState[m_iCurrentFrame].m_iChannelNO >= 0 ? m_conState[m_iCurrentFrame].m_iChannelNO : m_iCurrentFrame;
            
            //Add a red border to the current video window
            m_video[m_iCurrentFrame].picVideo.BackColor = Color.Red;

            //Update the state of the current video window
            SetWindowStates();
        }

        //Double click on the video display window
        private void Video_DBClick(object sender, EventArgs e)
        {
            //If there is no video playing in the current window, exit
            if (NVSSDK.NetClient_GetPlayingStatus(m_conState[m_iCurrentFrame].m_uiConID) != SDKConstMsg.PLAYER_PLAYING)
            {
                return;
            }
            
            if (m_iDBClick == 0)//Multi-screen to single-screen
            {
                //Hide the non-current video window
                for (int i = 0; i <= cboScreen.SelectedIndex; i++)
                {
                    if (i != m_iCurrentFrame)
                    {
                        m_video[i].Hide();
                    }                    
                }

                // Adjust the position and size of the current video window
                m_video[m_iCurrentFrame].Left = ClientRectangle.Left;
                m_video[m_iCurrentFrame].Top = ClientRectangle.Top;                
                m_video[m_iCurrentFrame].Height = ClientRectangle.Height;
                m_video[m_iCurrentFrame].Width = m_video[m_iCurrentFrame].Height*4/3 -170;

                //Modify the number of double clicks
                m_iDBClick = 1;
            }
            else if (m_iDBClick == 1)//Single screen to full screen
            {
                // get the resolution of the display
                Rectangle rect = Screen.PrimaryScreen.Bounds;
                this.SetVisibleCore(false);

                //Remove the border, maximize the window, and ensure the order to achieve full screen
                this.FormBorderStyle = FormBorderStyle.None;
                this.WindowState = FormWindowState.Maximized; 

                //Enlarge the screen, hide the red border
                m_video[m_iCurrentFrame].Left = -3;
                m_video[m_iCurrentFrame].Top = -3;
                m_video[m_iCurrentFrame].Height = rect.Height+6;
                m_video[m_iCurrentFrame].Width = rect.Width+6;

                //Set the Z order of the current video window to 0
                m_video[m_iCurrentFrame].BringToFront();
                this.SetVisibleCore(true);

                //Modify the number of double clicks
                m_iDBClick = 2;                
            }
            else//Full screen to multi-screen
            {
                //First restore the window to normal, and then remove the border; otherwise, the window will gradually become larger or smaller.
                this.WindowState = FormWindowState.Normal;
                this.FormBorderStyle = FormBorderStyle.Sizable;

                //Display cboScreen.SelectedIndex + 1 screen, restore the window state when double-clicked for the first time
                DisplayWindows(cboScreen.SelectedIndex + 1);
            }
        }

        private void UpdateDeviceList()
        {
            cboIP.Items.Clear();
            for (int i = 0; i < MAX_DEVICE_NUM; i++)
            {
                if (m_cltInfo[i].m_iServerID > -1)
                {
                    string tmp = new string(m_cltInfo[i].m_cRemoteIP);
                    cboIP.Items.Add(tmp);
                }
            }
        }

        //login and logout
        private void btnLogon_Click(object sender, EventArgs e)
        {
            if (btnLogon.Text == "Logon")//Login
            {
                string strProxy = "";
                string strIP = cboIP.Text;
                string strUser = textUser.Text;
                string strPwd = textPwd.Text;
                string strProxyID = "";
                int iPort = 3000;
                int iRet;

                // Determine if the device is already logged in
                //All in the list are logged in
                for (int i = 0; i < MAX_DEVICE_NUM; i++)
                {
                    string tmp = new string(m_cltInfo[i].m_cRemoteIP);
                    if (0 == tmp.CompareTo(strIP) && m_cltInfo[i].m_iServerID > -1)
                    {
                        MessageBox.Show("The device is logged in!");
                        return;
                    }
                }
               
                //Log in to the specified network video server
                iRet = NVSSDK.NetClient_Logon(strProxy, strIP, strUser, strPwd, strProxyID, iPort);
                if (iRet < 0)
                {
                    MessageBox.Show("Logon failed !");
                    return;
                }
                bool bFind = false;
                for (int i = 0; i < MAX_DEVICE_NUM; i++)
                {
                    if (m_cltInfo[i].m_iServerID < 0)
                    {
                        m_cltInfo[i].m_iServerID = iRet;
                        m_cltInfo[i].m_cRemoteIP = strIP.ToCharArray();
                        bFind = true;
                        break;
                    }
                }
                if (!bFind)
                {
                    MessageBox.Show("Logon Device is Full!");
                }

                btnLogon.Text = "Logoff";
            }
            else //logout
            {
                btnLogon.Text = "Logon";
                int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
                if (iLogonID < 0)//If the current window is not logged in, do not operate
                {
                    return;
                }

                //Log out the user login corresponding to the current window
                NVSSDK.NetClient_Logoff(iLogonID);

                for (int i = 0; i < MAX_DEVICE_NUM; i++)
                {
                    if (m_cltInfo[i].m_iServerID == iLogonID)
                    {
                        m_cltInfo[i].m_iServerID = -1;
                        m_sID[i] = "";
                    }
                }

                UpdateDeviceList();

                //Update the corresponding window information
                for (int i = 0; i < CONST_iFrameNum; i++)
                {
                    if (m_conState[i].m_iLogonID == iLogonID)
                    {
                        m_conState[i].m_iLogonID = -1;
                        m_conState[i].m_iChannelNO = -1;
                        m_conState[i].m_uiConID = UInt32.MaxValue;
                        m_video[i].Invalidate(true);
                    }
                }
                //Clear the status information of the current video window
                InitWindowStates();                
            }
        }

        // connect and disconnect
        private void btnConnect_Click(object sender, EventArgs e)
        {
            if (btnConnect.Text == "Connect")//connection operation
            {
                int index = GetCurDeviceIndex();
                if (index < 0)
                {
                    return;
                }


                m_cltInfo[index].m_iChannelNo = cboChannel.SelectedIndex;
                switch (cboMode.SelectedIndex)
                {
                    //1-private tcp connect, 2-private udp connect, 3-private multicast connect, 6-rtsp stream via RTP-over-TCP,
                    //7-rtsp stream via RTP-over-UDP, 8-rtsp stream via RTP-over-Multicast, 9-rtsps stream via SRTP-over-UDP, 10-rtsps stream via SRTP-over-Multicast
                    case 0: m_cltInfo[index].m_iNetMode = ConnectNetMode.NETMODE_TCP; break;
                    case 1: m_cltInfo[index].m_iNetMode = ConnectNetMode.NETMODE_UDP; break;
                    case 2: m_cltInfo[index].m_iNetMode = ConnectNetMode.NETMODE_MC; break;
                    case 3: m_cltInfo[index].m_iNetMode = ConnectNetMode.RTP_OVER_TCP; break;
                    case 4: m_cltInfo[index].m_iNetMode = ConnectNetMode.RTP_OVER_UDP; break;
                    case 5: m_cltInfo[index].m_iNetMode = ConnectNetMode.RTP_OVER_MCAST; break;
                    case 6: m_cltInfo[index].m_iNetMode = ConnectNetMode.SRTP_OVER_UDP; break;
                    case 7: m_cltInfo[index].m_iNetMode = ConnectNetMode.SRTP_OVER_MCAST; break;
                    default: m_cltInfo[index].m_iNetMode = ConnectNetMode.NETMODE_TCP; break; 	
                }
                m_cltInfo[index].m_iStreamNO = cboStream.SelectedIndex;
                m_cltInfo[index].m_cNetFile = new char[255];
                m_cltInfo[index].m_cRemoteIP = new char[16];
                Array.Copy(cboIP.Text.ToCharArray(), m_cltInfo[index].m_cRemoteIP, cboIP.Text.Length);
                UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;

                //Get the video playback state corresponding to the current window
                int iRet = NVSSDK.NetClient_GetPlayingStatus(uiConID);

                //If the video is playing, do not connect
                if (iRet != SDKConstMsg.PLAYER_PLAYING)
                {
                    int iChannelNum = 0;

                    //Get the maximum number of channels of the network video server connected to the current window
                    NVSSDK.NetClient_GetChannelNum(m_cltInfo[index].m_iServerID, ref iChannelNum);

                    // Determine whether the maximum channel number is exceeded
                    if (m_cltInfo[index].m_iChannelNo >= iChannelNum)
                    {
                        MessageBox.Show("Max Channel is " + iChannelNum);
                        cboChannel.SelectedIndex = iChannelNum - 1;
                        return;
                    }
                    //Start receiving all the video data
                    iRet = NVSSDK.NetClient_StartRecv(ref uiConID, ref m_cltInfo[index], null);

                    //The operation fails, clear the information of the structure m_conState
                    if (iRet < 0)
                    {
                        m_conState[m_iCurrentFrame].m_iLogonID = -1;
                        m_conState[m_iCurrentFrame].m_uiConID = UInt32.MaxValue;
                        m_conState[m_iCurrentFrame].m_iChannelNO = -1;
                        MessageBox.Show("Connect failed !");                        
                        return;
                    }
                    //The operation is successful, update the information of the structure m_conState
                    m_conState[m_iCurrentFrame].m_iLogonID = m_cltInfo[index].m_iServerID;
                    m_conState[m_iCurrentFrame].m_iChannelNO = m_cltInfo[index].m_iChannelNo;
                    m_conState[m_iCurrentFrame].m_uiConID = uiConID;
                    m_conState[m_iCurrentFrame].m_iStreamNO = m_cltInfo[index].m_iStreamNO;

                    //Start exporting the received data
                    NVSSDK.NetClient_StartCaptureData(uiConID);
                    if (iRet == 1)
                    {
                        RECT rect = new RECT();

                        //Start playing a certain video
                        NVSSDK.NetClient_StartPlay(uiConID, m_video[m_iCurrentFrame].pnlVideo.Handle, rect, 0);
                        btnPlay.Text = "Stop";
                        GetWindowStates();
                    }
                    btnConnect.Text = "Disconnect";
                }
            }
            else // disconnect operation
            {
                NVSSDK.NetClient_StopRecv(m_conState[m_iCurrentFrame].m_uiConID);//Stop video reception
                m_conState[m_iCurrentFrame].m_iChannelNO = -1;//Modify the channel number and connection ID of the current window
                m_conState[m_iCurrentFrame].m_uiConID = UInt32.MaxValue;
                m_video[m_iCurrentFrame].Invalidate(true);//Refresh the current window and update its status information
                btnConnect.Text = "Connect";

                //Clear the status information of the current video window
                InitWindowStates();
                btnLogon.Text = "Logoff";
            }
               
        }

        // show video and stop playing
        private void btnPlay_Click(object sender, EventArgs e)
        {
            //The current window is not connected, exit
            if (m_conState[m_iCurrentFrame].m_uiConID == UInt32.MaxValue)
            {
                return;
            }
            string strCaption = btnPlay.Text;
            int iRet;
            if (strCaption == "Play") //Display video
            {
                RECT rect = new RECT();

                //Start playing the video
                iRet = NVSSDK.NetClient_StartPlay
                (
                    m_conState[m_iCurrentFrame].m_uiConID,
                    m_video[m_iCurrentFrame].pnlVideo.Handle,
                    rect,
                    0
                );
                if (iRet == 0)
                {
                    btnPlay.Text = "Stop";
                }
            }
            else //stop playing
            {
                //Stop accepting video data
                iRet = NVSSDK.NetClient_StopCaptureData(m_conState[m_iCurrentFrame].m_uiConID);

                //stop playing a certain video
                iRet = NVSSDK.NetClient_StopPlay(m_conState[m_iCurrentFrame].m_uiConID);
                m_video[m_iCurrentFrame].Invalidate(true);
                btnPlay.Text = "Play";
            }
        }

        //Change the number of video display screens
        private void cboScreen_SelectedIndexChanged(object sender, EventArgs e)
        {            
            DisplayWindows(cboScreen.SelectedIndex + 1);
        }        

        //record
        private void btnRecord_Click(object sender, EventArgs e)
        {
            UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;

            //Only record the connected window
            if (uiConID == UInt32.MaxValue)
            {
                return;
            }
            string strCaption = btnRecord.Text;
            if (strCaption == "Record")//No recording, start recording
            {
                dlgSaveFile.Filter = "(*.sdv)|*.sdv|(*.mp4)|*.mp4";

                //Display the file save dialog
                if(dlgSaveFile.ShowDialog() == DialogResult.OK)
                {
                    string strFileName = dlgSaveFile.FileName;
                    int iRecordType = REC_FILE_TYPE_NORMAL;
                    if(Path.GetExtension(strFileName)==".mp4")
                    {
                        iRecordType = REC_FILE_TYPE_MP4;
                    }

                    //Start writing the received data to the file
                    int iRet = NVSSDK.NetClient_StartCaptureFile(uiConID, strFileName,iRecordType);
                    if (iRet == 0)
                    {
                        btnRecord.Text = "Stop";
                    }
                }
            }
            else // is recording
            {
                //Stop writing the received data to the file
                NVSSDK.NetClient_StopCaptureFile(uiConID);
                btnRecord.Text = "Record";
            }
        }

        // capture operation
        private void btnCapPic_Click(object sender, EventArgs e)
        {
            UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;

            //Only record the connected window
            if (uiConID == UInt32.MaxValue)
            {
                return;
            }
            dlgSaveFile.Filter = "(*.bmp)|*.bmp";

            //Display the file save dialog
            if (dlgSaveFile.ShowDialog() == DialogResult.OK)
            {
                string strFileName = dlgSaveFile.FileName;

                //Start writing the received data to the file
                NVSSDK.NetClient_CaptureBmpPic(uiConID, strFileName);           
            }
        }

        //Get and display character overlay parameters
        private void GetOSD()
        {
            byte[] btOSD = new byte[32];
            UInt32 uiColor = 0;

            //Get the string superimposed on the video source
            NVSSDK.NetClient_GetOsdText
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                btOSD,
                ref uiColor
            );

            //Convert byte array to string
            textOSD.Text =Encoding.ASCII.GetString(btOSD);
        }

        //Set character overlay parameters
        private void SetOSD()
        {
            string strOSD = textOSD.Text;

            // Determine if it is an empty string
            strOSD = strOSD == "" ? " " : strOSD;
            UInt32 uiColor = 0;

            // superimpose a string on the video source
            NVSSDK.NetClient_SetOsdText
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                Encoding.Default.GetBytes(strOSD),//Convert the string into a byte array, here use Default to convert
                uiColor
            );
            textOSD.Text = strOSD;
        }

        //Get character overlay status
        private void GetOSDType()
        {
            Int32 iType = cboOSDType.SelectedIndex;

            //Convert to character overlay type code, 0x01 overlay time, 0x02 overlay string, 0x04 overlay LOGO logo
            switch(iType)
            {
                case 0:
                    iType = 0x01;
                    break;
                case 1:
                    iType = 0x02;
                    break;
                case 2:
                    iType = 0x04;
                    break;
                default :
                    iType = 0x02;
                    cboOSDType.SelectedIndex = 1;
                    break;
            }
            int iX = 0;
            int iY = 0;
            int iEnable = 0;


            //Get the character overlay status of a certain channel of the network video server
            NVSSDK.NetClient_GetOsdType
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                iType,
                ref iX,
                ref iY,
                ref iEnable
            );
            cboOSDX.Text = iX.ToString();
            cboOSDY.Text = iY.ToString();
            cboOSDEnable.SelectedIndex = iEnable;           
        }

        //Set the character overlay state
        private void SetOSDType()
        {
            Int32 iType = cboOSDType.SelectedIndex;

            //Convert to character overlay type code, 0x01 overlay time, 0x02 overlay string, 0x04 overlay LOGO logo
            switch (iType)
            {
                case 0:
                    iType = 0x01;
                    break;
                case 2:
                    iType = 0x04;
                    break;
                default:
                    iType = 0x02;
                    break;
            }
            int iX = 0;
            int iY = 0;
            int iEnable = 0;
            try
            {
                iX = Int32.Parse(cboOSDX.Text);
                iY = Int32.Parse(cboOSDY.Text);
                iEnable = cboOSDEnable.SelectedIndex;
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            //Start or stop the character overlay operation, and specify the position of the character overlay.
            int iRet = NVSSDK.NetClient_SetOsdType
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,               
                iX,
                iY,
                iType,
                iEnable
            );
            if (iRet < 0)
            {
                MessageBox.Show("NetClient_SetOsdType Failed! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }
        }
        //Get the current device
        private int GetCurDeviceIndex()
        {
            
           string curIP = cboIP.Text;
           int index = -1;
           for (int i = 0; i < MAX_DEVICE_NUM; i++)
           {
              string tmp = new string(m_cltInfo[i].m_cRemoteIP);
              if (0 == tmp.CompareTo(curIP))
              {
                  index = i;
                  break;
              }
           }
           return index;
        }

        //Modify character overlay information
        private void btnOSDSet_Click(object sender, EventArgs e)
        {
            SetOSDType();
            SetOSD();
        }

        //Get and display device type information
        private void GetDeviceType()
        {
            int iCom = 0;
            int iDevAddress = 0;
            StringBuilder strDevType = new StringBuilder();
            StringBuilder strComFormat = new StringBuilder();
            int iWorkMode = 0;
            int iRet;

            //Get the control device type and serial port property settings
            iRet = NVSSDK.NetClient_GetDeviceType
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                ref iCom,
                ref iDevAddress,
                strDevType
            );
            if (iRet < 0)
            {
                MessageBox.Show("NetClient_GetDeviceType Failed!" + (Marshal.GetLastWin32Error() - 0x10000000).ToString());
            }

            //The serial port number starts from 1
            if (iCom < 1)
            {
                iCom = 1;
            }
            //Get serial port properties
            iRet = NVSSDK.NetClient_GetComFormat
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                iCom,
                strComFormat,
                ref iWorkMode
            );
            if (iRet < 0)
            {
                MessageBox.Show("NetClient_GetComFormat Failed! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }

            int iPresetSpeed = 0;
            DOMEPTZ tDomePtz = new DOMEPTZ();
            tDomePtz.iSize = Marshal.SizeOf(tDomePtz);
            tDomePtz.iType = DOME_PTZ.DOME_PTZ_TYPE_PRESET_SPEED_LEVE;
            tDomePtz.iAutoEnable = 1;
            tDomePtz.iParam1 = comboBoxPresetSpeed.SelectedIndex;
            tDomePtz.iParam2 = 0;

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tDomePtz));
            Marshal.StructureToPtr(tDomePtz, intptr, true);

            iRet = NVSSDK.NetClient_GetDomePTZ(m_conState[m_iCurrentFrame].m_iLogonID, m_conState[m_iCurrentFrame].m_iChannelNO, intptr, Marshal.SizeOf(tDomePtz));
            tDomePtz = (DOMEPTZ)Marshal.PtrToStructure(intptr, typeof(DOMEPTZ));
            
            if (0 == iRet)
            {
                iPresetSpeed = tDomePtz.iParam1;
            }

            Marshal.FreeHGlobal(intptr);

            // Serial port working mode 1: Protocol 2: Transparent channel
            if (iWorkMode < 1)
            {
                iWorkMode = 1;
            }		
            cboComNo.SelectedIndex = iCom - 1;
            cboComSend.SelectedIndex = iCom - 1;
            textAddress.Text = iDevAddress.ToString();
            cboDeviceType.Text = strDevType.ToString();
            textComFormat.Text = strComFormat.ToString();
            cboWorkMode.SelectedIndex = iWorkMode - 1;
            comboBoxPresetSpeed.SelectedIndex = iPresetSpeed;
        }

        //Modify device information
        private void SetDeviceType()
        {
            int iCom = cboComNo.SelectedIndex+1;
            int iDevAddress = 0;
            byte[] btDevType = Encoding.ASCII.GetBytes(cboDeviceType.Text);
            byte[] btComFormat = Encoding.ASCII.GetBytes(textComFormat.Text);
            int iWorkMode = cboWorkMode.SelectedIndex+1;
            try
            {
                //do type conversion
                iDevAddress = Int32.Parse(textAddress.Text);
            }
            catch (System.Exception ex)
            {
                MessageBox.Show("The parameter is incorrect!" + ex.Message);
            }
            int iRet;

            //Set the control device type and serial port properties
            iRet = NVSSDK.NetClient_SetDeviceType
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                iCom,
                iDevAddress,
                btDevType
            );
            if (iRet < 0)
            {
                MessageBox.Show("NetClient_SetDeviceType Failed! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }

            //Set serial port properties
            iRet = NVSSDK.NetClient_SetComFormat
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                iCom,
                btDevType,
                btComFormat,
                iWorkMode
            );
            if (iRet < 0)
            {
                MessageBox.Show("NetClient_SetComFormat Failed! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }
        }

        //Modify device type information
        private void btnDevTypeSet_Click(object sender, EventArgs e)
        {
            SetDeviceType();
        }

        //Get and display video parameters
        private void GetVideoParam()
        {
            //Create a video parameter structure
            STR_VideoParam structVideoParam = new STR_VideoParam();

            //Get the video display parameters of a certain channel of the network video server
            NVSSDK.NetClient_GetVideoParam
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                ref structVideoParam
            );

            //Update the state of each video parameter display control
            trckBrightness.Value = structVideoParam.m_ustBrightness;
            trckContrast.Value = structVideoParam.m_ustContrast;
            trckHue.Value = structVideoParam.m_usHue;
            trckSaturation.Value = structVideoParam.m_ustSaturation;
        }

        //modify video parameters
        private void SetVideoParam()
        {
            if (m_conState[m_iCurrentFrame].m_iLogonID < 0)
            {
                return;
            }
            //Create a video parameter structure
            STR_VideoParam structVideoParam1 = new STR_VideoParam();
            structVideoParam1.m_usHue = (UInt16)trckHue.Value;
            structVideoParam1.m_ustBrightness = (UInt16)trckBrightness.Value;
            structVideoParam1.m_ustContrast = (UInt16)trckContrast.Value;
            structVideoParam1.m_ustSaturation = (UInt16)trckSaturation.Value;

            //Set the video display parameters of a certain channel of the network video server
            int iRet = NVSSDK.NetClient_SetVideoParam
            (
                m_conState[m_iCurrentFrame].m_iLogonID,
                m_conState[m_iCurrentFrame].m_iChannelNO,
                ref structVideoParam1
            );
            if (iRet < 0)
            {
                MessageBox.Show("NetClient_SetVideoParam Failed! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }
        }

        //modify the contrast
        private void trckContrast_ValueChanged(object sender, EventArgs e)
        {
            //Modify the value of the corresponding label
            lblContrast.Text = ((TrackBar)sender).Value.ToString();
        }

        //modify brightness
        private void trckBrightness_ValueChanged(object sender, EventArgs e)
        {
            //Modify the value of the corresponding label
            lblBrightness.Text = ((TrackBar)sender).Value.ToString();
        }
       
        private void trckSaturation_ValueChanged(object sender, EventArgs e)
        {
            lblSaturation.Text = ((TrackBar)sender).Value.ToString();
        }

        private void trckHue_ValueChanged(object sender, EventArgs e)
        {
            lblHue.Text = ((TrackBar)sender).Value.ToString();
        }

        //Automatically set video parameters after dragging the slider
        private void trckVideoParam_MouseUp(object sender, MouseEventArgs e)
        {
            SetVideoParam();
        }

        //Set the video parameters after clicking the set button
        private void btnVideoParamSet_Click(object sender, EventArgs e)
        {
            SetVideoParam();
        }

        private void trckSpeed_ValueChanged(object sender, EventArgs e)
        {
            lblSpeed.Text = ((TrackBar)sender).Value.ToString();
        }

        //Control the device through the protocol
        //_iAction action code
        //_iParam1 horizontal speed or preset number
        //_iParam2 vertical speed
        private int ProtocalControl(int _iAction,int _iParam1,int _iParam2)
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;           
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;            
            int iRet = -1;

            //The parameters of the electronic PTZ, control type (Normal, e_PTZ)
            int iControlType = chkPTZ.Checked == true ? 1 : 0;

            //Control the action of the device connected to a channel of the network video server
            iRet = NVSSDK.NetClient_DeviceCtrlEx
            (
                iLogonID,
                iChannelNo,
                _iAction,
                _iParam1,
                _iParam2,
                iControlType
            );
            if (iRet < 0)//Device control failed
            {
                MessageBox.Show("NetClient_DeviceCtrlEx Failed ! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }
            return iRet;
        }

        //Control the device through the transparent channel
        //_iAction action code
        //_iParam1 horizontal speed or preset number
        //_iParam2 vertical speed
        private int ChannelControl(int _iAction,int _iParam1,int _iParam2)
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            string strDevType = cboDeviceType.Text;
            if (strDevType.Length < 4 || strDevType.Substring(0, 4) != "DOME")
            {
                return -1;
            }
            int iComNo = cboComSend.SelectedIndex + 1;
            int iRet = -1;

            //Create and set the control parameter structure
            CONTROL_PARAM cParam = new CONTROL_PARAM();
            cParam.m_ptMove.x = _iParam1;//horizontal speed
            cParam.m_ptMove.y = _iParam2;//Vertical speed

            //CALL_VIEW : 62 call preset
            //SET_VIEW : 63 Set preset position
            if (_iAction == 62 || _iAction == 63)
            {
                cParam.m_iPreset = _iParam1; //Preset position number
            }
            
            cParam.m_btBuf = new byte[256];//Data to be processed
            try
            {
                //device address
                cParam.m_iAddress = Int32.Parse(textAddress.Text);
            }
            catch (System.Exception ex)
            {
                //Conversion failed
                MessageBox.Show(ex.Message);
                return -1;
            }

            //Call the control code processing function in the dll file corresponding to the device type
            iRet = NVSSDK.NetClient_GetControlCode(strDevType, _iAction, ref cParam);
            if (iRet != 1)//Control code processing failed
            {                
                MessageBox.Show("NetClient_GetControlCode Failed ! ");
                return -1;
            }

            //Send data from serial port through transparent channel
            iRet = NVSSDK.NetClient_ComSend
            (
                iLogonID,
                cParam.m_btBuf,
                cParam.m_iCount,
                iComNo
            );
            if (iRet < 0) //data sending failed
            {
                MessageBox.Show("NetClient_ComSend Failed ! USER_ERROR+" + (Marshal.GetLastWin32Error() - SDKConstMsg.USER_ERROR));
            }
            return iRet;
        }

        // control the device
        //_iAction action code
        //_iParam1 horizontal speed or preset number
        //_iParam2 vertical speed
        private int DevControl(int _iAction)
        {
            UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;

            //The current video window is not playing
            if (uiConID == UInt32.MaxValue)
            {
                return -1;
            }
            int iWorkMode = cboWorkMode.SelectedIndex;
            int iParam1 = 0;
            int iParam2 = 0;
            int iSpeed = trckSpeed.Value;
            int iPreset = 0;
            switch (_iAction)
            {
                case ActionControlMsg.MOVE_UP://move up
                    iParam1 = 0;
                    iParam2 = iSpeed;

                    //MOVE transparent channel move code
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_DOWN://move down
                    iParam1 = 0;
                    iParam2 = iWorkMode == 0 ? iSpeed : -iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_LEFT://move left
                    iParam1 = iWorkMode == 0 ? iSpeed : -iSpeed;
                    iParam2 = 0;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_RIGHT://move right
                    iParam1 = iSpeed;
                    iParam2 = 0;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_UP_LEFT://move up and left
                    iParam1 = iWorkMode == 0 ? iSpeed : -iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_UP_RIGHT://move up and right
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_DOWN_LEFT://move down left
                    iParam1 = iWorkMode == 0 ? iSpeed : -iSpeed;
                    iParam2 = iWorkMode == 0 ? iSpeed : -iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.MOVE_DOWN_RIGHT://move down right
                    iParam1 = iSpeed;
                    iParam2 = iWorkMode == 0 ? iSpeed : -iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : ActionControlMsg.MOVE;
                    break;
                case ActionControlMsg.ZOOM_BIG://Zoom
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 31;
                    break;
                case ActionControlMsg.ZOOM_SMALL://Zoom small
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 33;
                    break;
                case ActionControlMsg.FOCUS_NEAR://Focus Near
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 37;
                    break;
                case ActionControlMsg.FOCUS_FAR://Focus far
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 35;
                    break;
                case ActionControlMsg.IRIS_OPEN://Iris open
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 39;
                    break;
                case ActionControlMsg.IRIS_CLOSE://Iris close
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 41;
                    break;
                case ActionControlMsg.RAIN_ON://wiper on
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 47;
                    break;
                case ActionControlMsg.RAIN_OFF://wiper off
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 48;
                    break;
                case ActionControlMsg.LIGHT_ON://backlight on
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 43;
                    break;
                case ActionControlMsg.LIGHT_OFF://backlight off
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 44;
                    break;
                case ActionControlMsg.HOR_AUTO://Auto cruise
                    iParam1 = 0;
                    iParam2 = 0;
                    _iAction = iWorkMode == 0 ? _iAction : 21;
                    break;
                case ActionControlMsg.HOR_AUTO_STOP://stop auto cruise
                    iParam1 = 0;
                    iParam2 = 0;
                    _iAction = iWorkMode == 0 ? _iAction : 22;
                    break;
                case ActionControlMsg.CALL_VIEW://call preset
                    try
                    {
                        iPreset = Int32.Parse(textPreset.Text);
                    }
                    catch (System.Exception ex)
                    {
                        MessageBox.Show(ex.Message);
                        return -1;
                    }
                    iParam1 = iPreset;
                    iParam2 = 0;
                    _iAction = iWorkMode == 0 ? _iAction : 62;
                    break;
                case ActionControlMsg.SET_VIEW://Set preset position
                    try
                    {
                        iPreset = Int32.Parse(textPreset.Text);
                    }
                    catch (System.Exception ex)
                    {
                        MessageBox.Show(ex.Message);
                        return -1;
                    }
                    iParam1 = iPreset;
                    iParam2 = 0;
                    _iAction = iWorkMode == 0 ? _iAction : 63;
                    break;
                case ActionControlMsg.POWER_ON://Power on
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 45;
                    break;
                case ActionControlMsg.POWER_OFF://Power off
                    iParam1 = iSpeed;
                    iParam2 = iSpeed;
                    _iAction = iWorkMode == 0 ? _iAction : 46;
                    break;                
                default:                
                    iParam1 = 0;
                    iParam2 = 0;

                    //9 Protocol control stop code
                    _iAction = iWorkMode == 0 ? 9 : _iAction;
                    break;
            }

            if (iWorkMode == 0)// serial port working mode is protocol
            { 
                //Control the device through the protocol
                return ProtocalControl(_iAction, iParam1, iParam2);
            }
            else if (iWorkMode == 1)// serial port working mode is transparent channel
            {  
                //Control the device through the transparent channel
                return ChannelControl(_iAction, iParam1, iParam2);
            }
            return -1;
        }

        //move control handler
        private void MOVE_MouseDown(object sender, MouseEventArgs e)
        {
            Button btnControl = (Button)sender;
            btnMoveAuto.Text = "Auto";

            //Get the control code in the Tag property
            int iAction = Int32.Parse(btnControl.Tag.ToString());
           
            //call the move handler
            DevControl(iAction);
        }

        //stop move handler
        private void MOVE_STOP_MouseUp(object sender, MouseEventArgs e)
        {
            //stop the move operation
            DevControl(ActionControlMsg.MOVE_STOP);
        }

        //Auto key in control
        private void btnMoveAuto_Click(object sender, EventArgs e)
        {
            int iRet;

            //Auto cruise operation
            if (btnMoveAuto.Text == "Auto") //Auto state
            {
                iRet = DevControl(ActionControlMsg.HOR_AUTO);
                if (iRet == 0)
                {
                    btnMoveAuto.Text = "Stop";
                }
            }
            else // is the Stop state
            {
                iRet = DevControl(ActionControlMsg.HOR_AUTO_STOP);
                if (iRet == 0)
                {
                    btnMoveAuto.Text = "Auto";
                }
            }            
        }

        private void chkPower_CheckedChanged(object sender, EventArgs e)
        {
            if (chkPower.Checked) //checked state
            {
                //Call the function DevControl to turn on the power
                DevControl(ActionControlMsg.POWER_ON);
            }
            else //unselected state
            {
                //Call the function DevControl to turn off the power
                DevControl(ActionControlMsg.POWER_OFF);
            }
             
        }

        private void chkLight_CheckedChanged(object sender, EventArgs e)
        {
            if (chkLight.Checked) //Selected state
            {
                //Call the function DevControl to turn on the backlight
                DevControl(ActionControlMsg.LIGHT_ON);
            }
            else //unselected state
            {
                //Call the function DevControl to turn on and off the backlight
                DevControl(ActionControlMsg.LIGHT_OFF);
            }
        }

        private void chkRain_CheckedChanged(object sender, EventArgs e)
        {
            if (chkRain.Checked) //checked state
            {
                //Call the function DevControl to open the wiper operation
                DevControl(ActionControlMsg.RAIN_ON);
            }
            else //unselected state
            {
                //Call the function DevControl to close the wiper operation
                DevControl(ActionControlMsg.RAIN_OFF);
            }
        }

        private void btnZoomBig_MouseUp(object sender, MouseEventArgs e)
        {
            //Call the function DevControl to stop the zoom operation
            DevControl(ActionControlMsg.ZOOM_BIG_STOP);
        }

        private void btnZoomSmall_MouseUp(object sender, MouseEventArgs e)
        {
            //Call the function DevControl to stop the zoom operation
            DevControl(ActionControlMsg.ZOOM_SMALL_STOP);
        }

        private void btnIrisOpen_MouseUp(object sender, MouseEventArgs e)
        {
            //Call the function DevControl to stop the aperture opening operation
            DevControl(ActionControlMsg.IRIS_OPEN_STOP);
        }

        private void btnIrisClose_MouseUp(object sender, MouseEventArgs e)
        {
            //Call the function DevControl to stop the aperture closing operation
            DevControl(ActionControlMsg.IRIS_CLOSE_STOP);
        }

        private void btnFocusNear_MouseUp(object sender, MouseEventArgs e)
        {
            //Call the function DevControl to stop the focus near operation
            DevControl(ActionControlMsg.FOCUS_NEAR_STOP);
        }

        private void btnFocusFar_MouseUp(object sender, MouseEventArgs e)
        {
            //Call the function DevControl to stop the focus far operation
            DevControl(ActionControlMsg.FOCUS_FAR_STOP);
        }

        // call the preset operation
        private void btnGotoPreset_Click(object sender, EventArgs e)
        {
            int iPreset = 0;
            try
            {
                // Perform preset type conversion operation
                iPreset = Int32.Parse(textPreset.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }
            if (iPreset < 1 || iPreset > 255)
            {
                MessageBox.Show("The preset is a number between (1--255)");
                return;
            }

            //Call the function DevControl to call the preset operation
            DevControl(ActionControlMsg.CALL_VIEW);
        }

        // set preset operation
        private void btnSetPreset_Click(object sender, EventArgs e)
        {
            int iPreset = 0;
            try
            {
                // Perform preset type conversion operation
                iPreset = Int32.Parse(textPreset.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }
            if (iPreset < 1 || iPreset > 255)
            {
                MessageBox.Show("The preset is a number between (1--255)");
                return;
            }

            //Call the function DevControl to call the preset operation
            DevControl(ActionControlMsg.SET_VIEW);
        }

        private void btnAssistantON_Click(object sender, EventArgs e)
        {
            int iAssistantNo = 0;
            try
            {
                iAssistantNo = Int32.Parse(textAssistant.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            //Only accept input values ??between 1--8
            if (iAssistantNo < 1 || iAssistantNo > 8)
            {
                MessageBox.Show("Only accept numbers between 1--8");
                return;
            }
            int iWorkMode = cboWorkMode.SelectedIndex;
            int iSpeed = trckSpeed.Value;

            //Only operate when the serial port working mode is protocol
            if (iWorkMode == 0)
            {
                ProtocalControl(33,iAssistantNo,iSpeed);
            }	
        }

        private void btnAssistantOFF_Click(object sender, EventArgs e)
        {
            int iAssistantNo = 0;
            try
            {
                iAssistantNo = Int32.Parse(textAssistant.Text.Trim());
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            //Only accept input values ??between 1--8
            if (iAssistantNo < 1 || iAssistantNo > 8)
            {
                MessageBox.Show("Only accept numbers between 1--8");
                return;
            }
            int iWorkMode = cboWorkMode.SelectedIndex;
            int iSpeed = trckSpeed.Value;

            //Only operate when the serial port working mode is protocol
            if (iWorkMode == 0)
            {
                ProtocalControl(34, iAssistantNo, iSpeed);
            }	
        }

        //Send data from serial port through transparent channel
        private void btnComSend_Click(object sender, EventArgs e)
        {
            // Split the input string by ','
            string[] strValue = textComSend.Text.Trim().Split(new char[] { ',' });
            byte[] btBuffer = new byte[1024];
            int i = 0;
            try
            {
                for ( ; i < strValue.Length; i++)
                {
                    //Convert hex string to integer
                    btBuffer[i] = (byte)Int32.Parse(strValue[i],System.Globalization.NumberStyles.HexNumber);
                }
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iComNo = cboComSend.SelectedIndex + 1;
            int iRet;

            //Send data btBuffer
            iRet = NVSSDK.NetClient_ComSend(iLogonID, btBuffer, i, iComNo);

            //operation failed
            if (iRet < 0)
            {
                MessageBox.Show("Send data failed !");
            }
        }

        private void Client_FormClosed(object sender, FormClosedEventArgs e)
        {
            //Clear the NetClient library
            NVSSDK.NetClient_Cleanup();

            //Clear the NSLook library
            NVSSDK.NSLook_Cleanup();
        }

        //Connect to a registry server
        private void btnDSMLogon_Click(object sender, EventArgs e)
        {            
            string strUser = textServerUser.Text;
            string strPwd = textServerPwd.Text;
            string strIP = textServerIP.Text;

            // remove the middle space
            strIP = strIP.Replace(" ", "");
            byte[] btIP = Encoding.ASCII.GetBytes(strIP);
            string strPort = textServerPort.Text;
            int iPort = 0;
            try
            {
                //Server port type conversion
                iPort = Int32.Parse(strPort);
            }
            catch (System.Exception ex)
            {
                MessageBox.Show(ex.Message);
                return;
            }

            // first log out, then log in
            int iID = (Int32)btnDSMLogon.Tag;
            if (iID >= 0)
            {
                //Disconnect from a registry
                NVSSDK.NSLook_LogoffServer(iID);
                btnDSMLogon.Tag = -1;
            }

            //Connect to the specified server, reconnection is not allowed by default
            int iRet = NVSSDK.NSLook_LogonServer(btIP, iPort, false);

            //Connection failed, pop up connection failure dialog
            if (iRet < 0)
            {
                MessageBox.Show("Connect to the registry server " + strIP + " Failed!");
                return;
            }

            //The connection is successful, save the connection number in the Tag of the button btnDSMLogon
            btnDSMLogon.Tag = iRet;

            //Create DSM subform
            if (formDSM == null)
            {
                formDSM = new FormDSM();
            } 

            //Modify the properties of the subform
            formDSM.ServerIP = strIP;
            formDSM.ServerID = iRet;
            formDSM.UserName = textServerUser.Text;
            formDSM.Password = textServerPwd.Text;

            // Determine whether there is an inactive FormDSM form, avoid creating multiple FormDSM forms
            foreach (Form form in Application.OpenForms)
            {
                if (form.GetType() == typeof(FormDSM))
                {
                    form.Activate();
                    return;
                }
            }

            // show the subform
            formDSM.Show(this); 
        }

        //Subform call to log in to the network video server
        public void Logon(string _strIP)
        {
            m_blNSClick = true;
            cboIP.Text = _strIP;
            btnLogon.Text = "Logon";         
            btnLogon_Click(btnLogon, EventArgs.Empty);           
        }

        //When exiting the registry server, the subform calls
        public void LogofServer()
        {
            // Empty the formDSM field
            formDSM = null;
        }

        private void btnReplay_Click(object sender, EventArgs e)
        {

            int index = GetCurDeviceIndex();
            if (index < 0)
            {
                return;
            }

            ReplayForm frm = new ReplayForm(m_cltInfo[index], m_cltInfo[index].m_iServerID,this);

            frm.ShowDialog();
            //frm.Show(this); 
        }

        //thread function
        public void TalkMethod()
        {
            Thread.Sleep(100);

            byte[] btInput = new byte[640];
            FileStream fn = new FileStream(m_strTalkFileName, FileMode.Open);
            if (fn != null)
            {
                int iTimes = (fn.Length % 640) != 0 ? ((int)fn.Length / 640 + 1) : (int)fn.Length / 640;
                for (int i = 0; i < iTimes; i++)
                {
                    int iReadLen = fn.Read(btInput, 0, 640);
                    NVSSDK.NetClient_InputTalkingdata(btInput, iReadLen);
                    Thread.Sleep(40);
                }
                Thread.Sleep(1000);
                int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
                NVSSDK.NetClient_TalkEnd(iLogonID);
                fn.Close();
            }
        }

        private void MyMAIN_NOTIFY_V4(UInt32 _ulLogonID, IntPtr _iWparam, IntPtr _iLParam, Int32 _iUser)
        {
            switch (_iWparam.ToInt32())
            {
                //login status message
                //param1 login IP
                //param2 login ID
                //param3 login status
                case SDKConstMsg.WCM_LOGON_NOTIFY:
                    {
                        m_conState[m_iCurrentFrame].m_iLogonID = (int)_ulLogonID;
                        switch(_iLParam.ToInt32())
                        {
                            case SDKConstMsg.LOGON_SUCCESS:
                                MessageBox.Show("Logon success!");
                                break;
                            case SDKConstMsg.LOGON_TIMEOUT:
                                MessageBox.Show("Logon time out!");
                                break;
                            default:
                                break;
                        }
                        
                        break;
                    }
                case SDKConstMsg.WCM_TALK:
                    {
                        // Start the thread to send audio data NVSSDK.NetClient_InputTalkingdata()
                        if (0 == (int)_iLParam)
                        {
                            // TALK_BEGIN_OK, open the intercom successfully
              openTalkFileDialog.InitialDirectory = "";//Initial directory, no assignment can be
                            openTalkFileDialog.Filter = "All files (*.*)|*.*";//File type
                            openTalkFileDialog.ShowDialog();//Pop up the selection box
                            //String strOpenFileFolder = openTalkFileDialog.FileName.Substring(0,openTalkFileDialog.FileName.LastIndexOf('\\'));//The directory of the opened file
                            m_strTalkFileName = openTalkFileDialog.FileName;//Fully qualified name of the opened file
                     
                            Thread t2 = new Thread(new ThreadStart(TalkMethod));
                            t2.Start();
                        }
                        else if (3 == (int)_iLParam)
                        {
                            // TALK_END_OK, close the intercom successfully
                            MessageBox.Show("Intercom completed!");
                        }
                        else if (1 == (int)_iLParam || 2 == (int)_iLParam)
                        {
                            // TALK_END_OK, close the intercom successfully
                            MessageBox.Show("Intercom error!");
                        }

                        break;
                    }
                case SDKConstMsg.WCM_VCA_SUSPEND:
                    {
                        int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
                        int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;

                        VCASuspendResult tParam = new VCASuspendResult();
                        tParam.iBufSize = Marshal.SizeOf(tParam);
                      
                        IntPtr intptr = IntPtr.Zero;
                        intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
                        Marshal.StructureToPtr(tParam, intptr, true);
                        
                        Int32 iRetBytes = 0;
                        int iRet = NVSSDK.NetClient_GetDevConfig(iLogonID, NetSDKCmd.NET_CLIENT_VCA_SUSPEND, iChannelNo, intptr, Marshal.SizeOf(tParam), ref iRetBytes);

                        tParam = (VCASuspendResult)Marshal.PtrToStructure(intptr, typeof(VCASuspendResult));
                    
                        if (VCA_DEFINE.VCA_SUSPEND == tParam.iStatus && VCA_DEFINE.VCA_SUSPEND_RESULT_CONFIGING == tParam.iResult)
                        {
                            MessageBox.Show("Intelligent analysis parameters is modifying, will enter a read-only mode.");
                        }

                        Marshal.FreeHGlobal(intptr);

                        break;
                    }
               default:
                    break;
            }
        }

        private void MyAlarm_NOTIFY_V4(Int32 _ulLogonID, Int32 _iChan, Int32 _iAlarmState, Int32 _iAlarmType, Int32 _iUser)
        {
            StringBuilder sbAlarmMsg = new StringBuilder("AlarmMsg-", 128);

            sbAlarmMsg.Append(DateTime.Now.ToLocalTime().ToString());

            switch (_iAlarmType)
            {
                case AlarmConstMsgType.ALARM_VDO_MOTION:
                    sbAlarmMsg.Append("- MOTION");
                    break;
                case AlarmConstMsgType.ALARM_VDO_REC:
                    sbAlarmMsg.Append("- REC");
                    break;
                case AlarmConstMsgType.ALARM_VDO_LOST:
                    sbAlarmMsg.Append("- LOST");
                    break;
                case AlarmConstMsgType.ALARM_VDO_INPORT:
                    sbAlarmMsg.Append("- INPORT");
                    break;
                case AlarmConstMsgType.ALARM_VDO_OUTPORT:
                    sbAlarmMsg.Append("- OUTPORT");
                    break;
                case AlarmConstMsgType.ALARM_VDO_COVER:
                    sbAlarmMsg.Append("- COVER");
                    break;
                case AlarmConstMsgType.ALARM_VCA_INFO:
                    {
                        VCA_TAlarmInfo info = new VCA_TAlarmInfo();
                        Int32 iRet = NVSSDK.NetClient_VCAGetAlarmInfo(_ulLogonID, _iAlarmState, ref info, Marshal.SizeOf(info));
                        sbAlarmMsg.Append("- VCA");
                        sbAlarmMsg.Append(" RuleID: " + info.iRuleID);
                        Int32 iVCAAlarmState = info.iState;
                        switch (iVCAAlarmState)
                        {
                            case 0:
                                sbAlarmMsg.Append("- OFF");
                                break;
                            case 1:
                                sbAlarmMsg.Append("- ON");
                                break;
                            default:
                                sbAlarmMsg.Append("-" + iVCAAlarmState.ToString());
                                break;
                        }
                    }
                    break;
                default:
                    sbAlarmMsg.Append("-" + _iAlarmType.ToString());
                    break;
            }

            if (AlarmConstMsgType.ALARM_VCA_INFO == _iAlarmType)
            {
                // nothing
            }
            else
            {
                switch (_iAlarmState)
                {
                    case 0:
                        sbAlarmMsg.Append("- OFF");
                        break;
                    case 1:
                        sbAlarmMsg.Append("- ON");
                        break;
                    default:
                        sbAlarmMsg.Append("-" + _iAlarmState.ToString());
                        break;
                }
            }
            
            lbAlarmlistBox.Items.Insert(0, sbAlarmMsg.ToString());
        }

        private void MyParaChange_NOTIFY_V4(Int32 _ulLogonID, Int32 _iChan, Int32 _iParaType, IntPtr _strPara, Int32 _iUser)
        {
            switch (_iParaType)
            {
                case ParamChangeConstMsgType.PARA_IRRIGATION_NOTIFY:
                    {
                        // first convert the struct
                        STR_Para param = (STR_Para)Marshal.PtrToStructure(_strPara, typeof(STR_Para));
                        // pointer in the conversion struct
                        IrrigationPara vp = (IrrigationPara)Marshal.PtrToStructure(param.iPara[0], typeof(IrrigationPara));
                        int size = vp.iSize;
                        int value = vp.iValue;
                        int type = vp.iType;
                    }
                    break;
                case ParamChangeConstMsgType.PARA_VCA_ALARMSTAT:
                    {
                        // first convert the struct
                        STR_Para param = (STR_Para)Marshal.PtrToStructure(_strPara, typeof(STR_Para));
                        //get people statistics
                        int iIn = (int)(long)param.iPara[0];
                        int iOut = (int)(long)param.iPara[1];
                        int iInDiff = (int)(long)param.iPara[2];
                        int iOutDiff = (int)(long)param.iPara[3];
                        int iPass = (int)(long)param.iPara[4];
                        int iRegion = (int)(long)param.iPara[5];
                        int iStay = (int)(long)param.iPara[6];
                        int iAlarmCount = (int)(long)param.iPara[7];
                    }
                    break;
                default:
                    break;
            }
        }

        private void InputTalk_Click(object sender, EventArgs e)
        {
            
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;           
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;
            NVSSDK.NetClient_TalkStart(iLogonID, 1);//1 represents input audio data
        }
		
		private void btnRights_Click(object sender, EventArgs e)
        {
            int index = GetCurDeviceIndex();
            if (index >= 0)
            {
                RightsManagementForm frm = new RightsManagementForm();
                frm.m_iLogonID = m_cltInfo[index].m_iServerID;
                frm.ShowDialog();
            }

        }

        private void btnUserManagement_Click(object sender, EventArgs e)
        {
            int index = GetCurDeviceIndex();
            if (index >= 0)
            {
                UserManagementForm frm = new UserManagementForm();
                frm.m_iLogonID = m_cltInfo[index].m_iServerID;
                frm.ShowDialog();
            }
        }

        private void buttonPTZAbsSet_Click(object sender, EventArgs e)
        {
            int iSetPtzType = comboBoxPTZAbs.SelectedIndex;

            int iSetP = Int32.Parse(textCoordinateP.Text);
            int iSetT = Int32.Parse(textCoordinateT.Text);
            int iSetZ = Int32.Parse(textCoordinateZ.Text);

            SetPtz tSetPtz = new SetPtz();
            tSetPtz.iSize = Marshal.SizeOf(tSetPtz);
            tSetPtz.iType = iSetPtzType;
            tSetPtz.iPan  = iSetP;
            tSetPtz.iTilt = iSetT;
            tSetPtz.iZoom = iSetZ;

            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tSetPtz));
            Marshal.StructureToPtr(tSetPtz, intptr, true);

            int iRet = NVSSDK.NetClient_SendCommand(iLogonID, COMMAND_ID.COMMAND_ID_SET_PTZ, iChannelNo, intptr, Marshal.SizeOf(tSetPtz));

            Marshal.FreeHGlobal(intptr);
        }

        private void buttonPTZAbsGet_Click(object sender, EventArgs e)
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;
            

            int iRet = NVSSDK.NetClient_SendCommand(iLogonID, COMMAND_ID.COMMAND_ID_GET_PTZ, iChannelNo, IntPtr.Zero, 0);

            Thread.Sleep(1000);

            GetPtz tGetPtz = new GetPtz();
            tGetPtz.iSize = Marshal.SizeOf(tGetPtz);

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tGetPtz));
            Marshal.StructureToPtr(tGetPtz, intptr, true);

            int iRetResult = NVSSDK.NetClient_RecvCommand(iLogonID, COMMAND_ID.COMMAND_ID_GET_PTZ, iChannelNo, intptr, Marshal.SizeOf(tGetPtz));

            tGetPtz = (GetPtz)Marshal.PtrToStructure(intptr, typeof(GetPtz));

            if (0 == iRet)
            {
                textCoordinateP.Text = Convert.ToString(tGetPtz.iPosP);
                textCoordinateT.Text = Convert.ToString(tGetPtz.iPosT);
                textCoordinateZ.Text = Convert.ToString(tGetPtz.iPosZ);
            }

            Marshal.FreeHGlobal(intptr);
        }


        private void comboBoxPresetSpeed_SelectedValueChanged(object sender, EventArgs e)
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;

            if (iLogonID < 0)
            {
                return;
            }

            DOMEPTZ tDomePtz = new DOMEPTZ();
            tDomePtz.iSize = Marshal.SizeOf(tDomePtz);
            tDomePtz.iType = DOME_PTZ.DOME_PTZ_TYPE_PRESET_SPEED_LEVE;
            tDomePtz.iAutoEnable = 1;
            tDomePtz.iParam1 = comboBoxPresetSpeed.SelectedIndex;
            tDomePtz.iParam2 = 0;

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tDomePtz));
            Marshal.StructureToPtr(tDomePtz, intptr, true);

            int iRet = NVSSDK.NetClient_SetDomePTZ(iLogonID, iChannelNo, intptr, Marshal.SizeOf(tDomePtz));

            Marshal.FreeHGlobal(intptr);
        }

        private void tabVideo_SelectedIndexChanged(object sender, EventArgs e)
        {
            if (pageVCA.Name == ((TabControl)sender).SelectedTab.Name)//tabVideo: During the VCA scene configuration, the VCA will be suspended!
            {
                if (DialogResult.OK == MessageBox.Show("During the VCA scene configuration, the VCA will be suspended!", "Tip", MessageBoxButtons.OKCancel, MessageBoxIcon.Question))
                {
                    int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
                    int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;

                    int iStatus = VCA_DEFINE.VCA_SUSPEND;
                    IntPtr intptr = IntPtr.Zero;
                    intptr = Marshal.AllocHGlobal(Marshal.SizeOf(iStatus));
                    Marshal.StructureToPtr(iStatus, intptr, true);

                    int iRet = NVSSDK.NetClient_SetDevConfig(iLogonID, NetSDKCmd.NET_CLIENT_VCA_SUSPEND, iChannelNo, intptr, Marshal.SizeOf(iStatus));

                    Marshal.FreeHGlobal(intptr);
                }

            }
            else
            {
                int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
                int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;

                int iStatus = VCA_DEFINE.VCA_RESUME;
                IntPtr intptr = IntPtr.Zero;
                intptr = Marshal.AllocHGlobal(Marshal.SizeOf(iStatus));
                Marshal.StructureToPtr(iStatus, intptr, true);

                int iRet = NVSSDK.NetClient_SetDevConfig(iLogonID, NetSDKCmd.NET_CLIENT_VCA_SUSPEND, iChannelNo, intptr, Marshal.SizeOf(iStatus));

                Marshal.FreeHGlobal(intptr);
            }

        }

        private void comboBoxVCASwitch_SelectedIndexChanged(object sender, EventArgs e)
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;
            if (iChannelNo < 0)
            {
                return;
            }

            int iEnable = comboBoxVCASwitch.SelectedIndex;
            vca_TVCAParam vp = new vca_TVCAParam();
            vp.chnParam = new vca_TVCAChannelParam[128];
            for (int i = 0; i < 128;i++ )
            {
                vp.chnParam[i].rule = new vca_TRuleParam[8];
                for (int j = 0; j < 8; j++)
                {
                    vp.chnParam[i].rule[j].cRuleName = new byte[17];
                    vp.chnParam[i].rule[j].alarmSchedule.timeSeg = new vca_TAlarmTimeSegment[7,4];
                    vp.chnParam[i].rule[j].alarmLink.iLinkSet = new int[6];
                    vp.chnParam[i].rule[j].alarmLink.ptz = new vca_TLinkPTZ[128];
                }
            }
           
            vp.iChannelID = iChannelNo;
            vp.iEnable = iEnable;
            vp.chnParam[iChannelNo].iEnable = iEnable;

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(vp));
            Marshal.StructureToPtr(vp, intptr, true);
            int iRet = NVSSDK.NetClient_VCASetConfig(iLogonID, NetSDKCmd.VCA_CMD_SET_CHANNEL_ENABLE, iChannelNo, intptr, Marshal.SizeOf(vp));

            Marshal.FreeHGlobal(intptr);
        }

        private void buttonSaveScene_Click(object sender, EventArgs e)
        {
            int iSceneID = 0;
            int iArithmetic = 0;
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;
            if (iChannelNo < 0)
            {
                return;
            }

            if (0 < comboBoxScene.SelectedIndex && comboBoxScene.SelectedIndex < 16)
            {
                iSceneID = comboBoxScene.SelectedIndex;
            }

            iArithmetic |= (checkBoxBehavior.Checked == true ? 1 : 0) << 0;
            iArithmetic |= (checkBoxTracking.Checked == true ? 1 : 0) << 1;

            AnyScene tParam = new AnyScene();
            tParam.cSceneName = new byte[32];
            tParam.iBufSize = Marshal.SizeOf(tParam);
            tParam.iSceneID = iSceneID;
            tParam.iArithmetic = iArithmetic;

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
            Marshal.StructureToPtr(tParam, intptr, true);
            int iRet = NVSSDK.NetClient_SetDevConfig(iLogonID, NetSDKCmd.NET_CLIENT_ANYSCENE, iChannelNo, intptr, Marshal.SizeOf(tParam));

            Marshal.FreeHGlobal(intptr);
        }

        private void GetVCAParam()
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;
            if (iChannelNo < 0)
            {
                return;
            }
            
            //switch
            vca_TVCAParam vp = new vca_TVCAParam();
            vp.chnParam = new vca_TVCAChannelParam[128];
            for (int i = 0; i < 128; i++)
            {
                vp.chnParam[i].rule = new vca_TRuleParam[8];
                for (int j = 0; j < 8; j++)
                {
                    vp.chnParam[i].rule[j].cRuleName = new byte[17];
                    vp.chnParam[i].rule[j].alarmSchedule.timeSeg = new vca_TAlarmTimeSegment[7, 4];
                    vp.chnParam[i].rule[j].alarmLink.iLinkSet = new int[6];
                    vp.chnParam[i].rule[j].alarmLink.ptz = new vca_TLinkPTZ[128];
                }
            }

            vp.iChannelID = iChannelNo;

            IntPtr intptrVp = IntPtr.Zero;
            intptrVp = Marshal.AllocHGlobal(Marshal.SizeOf(vp));
            Marshal.StructureToPtr(vp, intptrVp, true);

            int iRet = NVSSDK.NetClient_VCAGetConfig(iLogonID, NetSDKCmd.VCA_CMD_GET_CHANNEL_ENABLE, iChannelNo, intptrVp, Marshal.SizeOf(vp));
            if (iRet >= 0)
            {
                vp = (vca_TVCAParam)Marshal.PtrToStructure(intptrVp, typeof(vca_TVCAParam));
                if(vp.chnParam[iChannelNo].iEnable <0 || vp.chnParam[iChannelNo].iEnable>=2)
                {
                    comboBoxVCASwitch.SelectedIndex = 0;
                }
                else
                {
                    comboBoxVCASwitch.SelectedIndex =  vp.chnParam[iChannelNo].iEnable;
                }
            }
            else
            {
                comboBoxVCASwitch.SelectedIndex = 0;
            }
            Marshal.FreeHGlobal(intptrVp);

            //scene
            int iSceneID = 0;
            if (0 <= comboBoxScene.SelectedIndex && comboBoxScene.SelectedIndex < 16)
            {
                iSceneID = comboBoxScene.SelectedIndex;
            }
            
            AnyScene tParam = new AnyScene();
            tParam.cSceneName = new byte[32];
            tParam.iBufSize = Marshal.SizeOf(tParam);
            tParam.iSceneID = iSceneID;
            int iBytesReturned = 0;
            
            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(tParam));
            Marshal.StructureToPtr(tParam, intptr, true);

            iRet = NVSSDK.NetClient_GetDevConfig(iLogonID, NetSDKCmd.NET_CLIENT_ANYSCENE, iChannelNo, intptr, Marshal.SizeOf(tParam), ref iBytesReturned);
            if (iRet >= 0)
            {
                tParam = (AnyScene)Marshal.PtrToStructure(intptr, typeof(AnyScene));
                int iStatus = 0;
                iStatus = (tParam.iArithmetic >> 0) & 0x01;
                checkBoxBehavior.Checked = (iStatus == 1 ? true : false);
                iStatus = (tParam.iArithmetic >> 1) & 0x01;
                checkBoxTracking.Checked = (iStatus == 1 ? true : false);
            }
            else
            {
                checkBoxBehavior.Checked = false;
                checkBoxTracking.Checked = false;
            }
            Marshal.FreeHGlobal(intptr);

            //rule Tripwire

            VCA_TRuleParam_Tripwire vcaTrp = new VCA_TRuleParam_Tripwire();
            vcaTrp.stRegion1.stPoints = new vca_TPoint[32];

            IntPtr intptrVcaTrp = IntPtr.Zero;
            intptrVcaTrp = Marshal.AllocHGlobal(Marshal.SizeOf(vcaTrp));
            Marshal.StructureToPtr(vcaTrp, intptrVcaTrp, true);

            iRet = NVSSDK.NetClient_VCAGetConfig(iLogonID, NetSDKCmd.VCA_CMD_TRIPWIRE_EX, iChannelNo, intptrVcaTrp, Marshal.SizeOf(vcaTrp));
            if (iRet >= 0)
            {
                vcaTrp = (VCA_TRuleParam_Tripwire)Marshal.PtrToStructure(intptrVcaTrp, typeof(VCA_TRuleParam_Tripwire));
                if (vcaTrp.iBufSize <= 0)
                {
                    return;
                }

                comboBoxTargetType.SelectedIndex = vcaTrp.iTargetTypeCheck;
                textBoxPointNum.Text = vcaTrp.stRegion1.iPointNum.ToString();
                
                string strPointBuf = "";
                for (int i = 0; i < vcaTrp.stRegion1.iPointNum; i++)
                {
                    string tmpStr;
                    tmpStr = "(" + vcaTrp.stRegion1.stPoints[i].iX.ToString() + "," + vcaTrp.stRegion1.stPoints[i].iY.ToString() + ")";
                    strPointBuf += tmpStr;
                }
                textBoxPoints.Text = strPointBuf;
            }

            Marshal.FreeHGlobal(intptrVcaTrp);
        }

        private void btnVCASave_Click(object sender, EventArgs e)
        {
            int iLogonID = m_conState[m_iCurrentFrame].m_iLogonID;
            int iChannelNo = m_conState[m_iCurrentFrame].m_iChannelNO;
            if (iChannelNo < 0)
            {
                return;
            }

            int iSceneID = 0;
            if (0 <= comboBoxScene.SelectedIndex && comboBoxScene.SelectedIndex < 16)
            {
                iSceneID = comboBoxScene.SelectedIndex;
            }
            int iRuleID = 0;
            if (0 <= comboBoxRule.SelectedIndex && comboBoxRule.SelectedIndex<8)
            {
                iRuleID = comboBoxRule.SelectedIndex;
            }


            vca_TVCAParam vp = new vca_TVCAParam();
            vp.chnParam = new vca_TVCAChannelParam[128];
            for (int i = 0; i < 128; i++)
            {
                vp.chnParam[i].rule = new vca_TRuleParam[8];
                for (int j = 0; j < 8; j++)
                {
                    vp.chnParam[i].rule[j].cRuleName = new byte[17];
                    vp.chnParam[i].rule[j].alarmSchedule.timeSeg = new vca_TAlarmTimeSegment[7, 4];
                    vp.chnParam[i].rule[j].alarmLink.iLinkSet = new int[6];
                    vp.chnParam[i].rule[j].alarmLink.ptz = new vca_TLinkPTZ[128];
                }
            }
            vp.iChannelID = iChannelNo;

            IntPtr intptr = IntPtr.Zero;
            intptr = Marshal.AllocHGlobal(Marshal.SizeOf(vp));
            Marshal.StructureToPtr(vp, intptr, true);

            int iRet = NVSSDK.NetClient_VCAGetConfig(iLogonID, NetSDKCmd.VCA_CMD_GET_CHANNEL, iChannelNo, intptr, Marshal.SizeOf(vp));
            vp = (vca_TVCAParam)Marshal.PtrToStructure(intptr, typeof(vca_TVCAParam));
            if (0 == vp.chnParam[vp.iChannelID].iEnable)
            {
                Marshal.FreeHGlobal(intptr);
                return;
            }

            VCA_TRuleParam_Tripwire vcaTrp = new VCA_TRuleParam_Tripwire();
            vcaTrp.stRegion1.stPoints = new vca_TPoint[32];

            vcaTrp.stRule.iRuleID = iRuleID;
            vcaTrp.stRule.iSceneID = iSceneID;

            vcaTrp.stRule.iValid = 1;
            vcaTrp.iBufSize = Marshal.SizeOf(vcaTrp);
            vcaTrp.stDisplayParam.iColor = 2;
            vcaTrp.stDisplayParam.iAlarmColor = 1;
            vcaTrp.iTargetTypeCheck = comboBoxTargetType.SelectedIndex;
            vcaTrp.stDisplayParam.iDisplayRule = 1;
            vcaTrp.stDisplayParam.iDisplayStat = 1;
            vcaTrp.iDisplayTarget = 1;
            vcaTrp.iTripwireType = 1;
            vcaTrp.iTripwireDirection = 0;
            vcaTrp.stRegion1.iPointNum = 2;
            vcaTrp.stRegion1.stPoints[0].iX = 500;
            vcaTrp.stRegion1.stPoints[0].iY = 200;
            vcaTrp.stRegion1.stPoints[1].iX = 500;
            vcaTrp.stRegion1.stPoints[1].iY = 800;

            IntPtr intptrVcaTrp = IntPtr.Zero;
            intptrVcaTrp = Marshal.AllocHGlobal(Marshal.SizeOf(vcaTrp));
            Marshal.StructureToPtr(vcaTrp, intptrVcaTrp, true);

            iRet = NVSSDK.NetClient_VCASetConfig(iLogonID, NetSDKCmd.VCA_CMD_TRIPWIRE_EX, iChannelNo, intptrVcaTrp, Marshal.SizeOf(vcaTrp));

            Marshal.FreeHGlobal(intptrVcaTrp);
            Marshal.FreeHGlobal(intptr);
        }

        private void btn_CapturePic_Click(object sender, EventArgs e)
        {
            UInt32 uiConID = m_conState[m_iCurrentFrame].m_uiConID;
            Int32 iPicType = PICTYPE_DEF.PIC_TYPE_JPG;
            byte[] btBuffer = new byte[10*1024*1024];
            Int32 iPicData = 10 * 1024 * 1024;
            //Only record the connected window
            if (uiConID == UInt32.MaxValue)
            {
                return;
            }
            Int32 iRet = NVSSDK.NetClient_CapturePicData(uiConID, iPicType, btBuffer, ref iPicData);
            FileStream pfFullPic = null;
            if (iRet > 0)
            {
                try
                {
                   //Display date format Friday,July, 01,2009
                    string strFullPicName = ".\\" + DateTime.Now.ToString("HHmmssddddMMMMddyyyy", new System.Globalization.DateTimeFormatInfo()) +".jpg";
                    Console.WriteLine(strFullPicName);
                    pfFullPic = new FileStream(strFullPicName, FileMode.Create);
                    if (null != pfFullPic && iPicData > 0)
                    {
                        pfFullPic.Write(btBuffer, 0, iPicData);
                    }
                }
                finally
                {
                    if (null != pfFullPic)
                    {
                        pfFullPic.Close();
                    }
                }
            }
        }
        
    }
}
