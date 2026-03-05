using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

using System.Runtime.InteropServices;
using System.Collections;

namespace NetClient
{
    public partial class RightsManagementForm : Form
    {
        //local authority
        public const int HAND_CLEAR_ALARM_LOCAL     = 7;	    //local clear alarm
        public const int RECORD_PLAYBACK_LOCAL      = 8;		//local playback
        public const int LOG_SEARCH_LOCAL           = 9;		//local search log
        public const int ALARM_SET_LOCAL            = 10;		//local alarm set
        public const int CHANNEL_MANAGEMENT_LOCAL   = 11;		//local channel management
        public const int PARAM_SET_LOCAL            = 12;		//local parameter set
        public const int SYSTEM_SET_LOCAL           = 13;		//local system set
        public const int USER_MANAGEMENT_LOCAL      = 14;		//local user management
        public const int RESTART_LOCAL              = 25;		//local restart

        //remote authority
        public const int HAND_CLEAR_ALARM_REMOTE	= 15;		//remote clear alarm
        public const int POWEROFF_RESTART_REMOTE	= 16;		//remote restart
        public const int RECORD_REMOTE				= 17;		//remote record
        public const int LOG_SEARCH_REMOTE			= 18;		//remote search log
        public const int ALARM_SET_REMOTE			= 19;		//remote alarm set
        public const int CHANNEL_MANAGEMENT_REMOTE	= 20;		//remote channel management
        public const int PARAM_SET_REMOTE			= 21;		//remote parameter set
        public const int SYSTEM_SET_REMOTE			= 22;		//remote system set
        public const int USER_MANAGEMENT_REMOTE		= 23;		//remote user management
        public const int TALKBACK_REMOTE            = 24;		//remote talk

        //channel authority
        public const int LOCAL_PTZ_CHANNEL			= 0;		//local ptz
        public const int LOCAL_PLAYBACK_CHANNEL		= 1;		//local playback
        public const int REMOTE_PTZ_CHANNEL			= 2;		//remote ptz
        public const int REMOTE_PLAYBACK_CHANNEL	= 3;	    //remote playback
        public const int REMOTE_PREVIEW_CHANNEL		= 4;		//remote preview
        public const int LOCAL_HAND_OPERATE_CHANNEL	= 5;		//local manual
        public const int REMOTE_HAND_OPERATE_CHANNEL= 6;		//remote manual
        public const int LOCAL_PREVIEW_CHANNEL      = 26;		//local preview

        public int m_iLogonID = -1;
        private bool m_bIsAdmin = false;

        private Dictionary<int, CheckBox> m_dicAuthorityIndexMapControl = new Dictionary<int, CheckBox>();  // key: index of authority; value: control in UI  
        // using by local authority and remote authority

        int m_iCurPageNo = -1;     // index of page, start by 0
        int m_iMaxPage = 0;        // 20 channels per page now, number of max page, start by 0
        int m_iChannelNum = 1;     // support number of channel
        private int[] m_iRightChannel = new int[16];      // rights info of channel
        private List<CheckBox> m_listChannelCheckBox = new List<CheckBox>();   // list of CheckBox control for channel

       ArrayList m_lsAuthority = new ArrayList();   // the datasource of cboRights control
        
        public RightsManagementForm()
        {
            InitializeComponent();

            // Initialize the list of CheckBox control for channel
            m_listChannelCheckBox.Clear();
            m_listChannelCheckBox.Add(chkChannel1);
            m_listChannelCheckBox.Add(chkChannel2);
            m_listChannelCheckBox.Add(chkChannel3);
            m_listChannelCheckBox.Add(chkChannel4);
            m_listChannelCheckBox.Add(chkChannel5);
            m_listChannelCheckBox.Add(chkChannel6);
            m_listChannelCheckBox.Add(chkChannel7);
            m_listChannelCheckBox.Add(chkChannel8);
            m_listChannelCheckBox.Add(chkChannel9);
            m_listChannelCheckBox.Add(chkChannel10);
            m_listChannelCheckBox.Add(chkChannel11);
            m_listChannelCheckBox.Add(chkChannel12);
            m_listChannelCheckBox.Add(chkChannel13);
            m_listChannelCheckBox.Add(chkChannel14);
            m_listChannelCheckBox.Add(chkChannel15);
            m_listChannelCheckBox.Add(chkChannel16);
            m_listChannelCheckBox.Add(chkChannel17);
            m_listChannelCheckBox.Add(chkChannel18);
            m_listChannelCheckBox.Add(chkChannel19);
            m_listChannelCheckBox.Add(chkChannel20);

            // Initialize the Dictionary using the index of authority and CheckBox control 
            m_dicAuthorityIndexMapControl.Clear();
            m_dicAuthorityIndexMapControl.Add(HAND_CLEAR_ALARM_LOCAL, chkManual);
            m_dicAuthorityIndexMapControl.Add(RESTART_LOCAL, chkReboot);
            m_dicAuthorityIndexMapControl.Add(LOG_SEARCH_LOCAL, chkLogSearch);
            m_dicAuthorityIndexMapControl.Add(ALARM_SET_LOCAL, chkAlarm);
            m_dicAuthorityIndexMapControl.Add(CHANNEL_MANAGEMENT_LOCAL, chkManageChannels);
            m_dicAuthorityIndexMapControl.Add(PARAM_SET_LOCAL, chkParaSet);
            m_dicAuthorityIndexMapControl.Add(SYSTEM_SET_LOCAL, chkSystemSet);
            m_dicAuthorityIndexMapControl.Add(USER_MANAGEMENT_LOCAL, chkUsrManage);
            m_dicAuthorityIndexMapControl.Add(HAND_CLEAR_ALARM_REMOTE, chkRemoteManual);
            m_dicAuthorityIndexMapControl.Add(POWEROFF_RESTART_REMOTE, chkRemoteReboot);
            m_dicAuthorityIndexMapControl.Add(LOG_SEARCH_REMOTE, chkRemoteLogSearch);
            m_dicAuthorityIndexMapControl.Add(ALARM_SET_REMOTE, chkRemoteAlarm);
            m_dicAuthorityIndexMapControl.Add(CHANNEL_MANAGEMENT_REMOTE, chkRemoteChannel);
            m_dicAuthorityIndexMapControl.Add(PARAM_SET_REMOTE, chkRemoteParaSet);
            m_dicAuthorityIndexMapControl.Add(SYSTEM_SET_REMOTE, chkRemoteSystemSet);
            m_dicAuthorityIndexMapControl.Add(USER_MANAGEMENT_REMOTE, chkRemoteUsrManage);
            m_dicAuthorityIndexMapControl.Add(TALKBACK_REMOTE, chkRemoteVoice);

        }
        private void RightsManagementForm_Shown(object sender, EventArgs e)
        {

            // Initialize the channel info, calculate the number of page, 20 channels per page now, because Form have 20 CheckBox controls of channel 
            int iAllChannelNum = 1;
            int iForbidChannel = 0;
            if (NVSSDK.NetClient_GetChannelNum(m_iLogonID, ref iAllChannelNum) >= 0 &&
                NVSSDK.NetClient_GetCommonEnable(m_iLogonID, SDKCOMMON_ID.CI_COMMON_ID_FORBIDCHN, 0x7fffffff, ref iForbidChannel) >= 0)
            {
                m_iChannelNum = iAllChannelNum - iForbidChannel;
            }

            if (m_listChannelCheckBox.Count > 0)
            {
                m_iMaxPage = (m_iChannelNum-1) / m_listChannelCheckBox.Count;
            }

            cboPage.Items.Clear();
            for (int i = 0; i <= m_iMaxPage; i++)
            {
                cboPage.Items.Add((i + 1).ToString());
            }

            // update the user's list
            UpdateUserList();
          
        }
        private void UpdateUserList()
        {
	        cboUserList.Items.Clear();

	        // Get User Number
            int iUserNum = 0;
            int iRet = NVSSDK.NetClient_GetUserNum(m_iLogonID, ref iUserNum);

            if (iRet < 0)
            {
                m_bIsAdmin = false;
                return;
            }
	        
 	        int iAuthority = 0;
            byte[] btUserName = new byte[128];
            byte[] btPassword = new byte[128];
            

	        for (int i=0; i<iUserNum; i++)
	        {
                iRet = NVSSDK.NetClient_GetUserInfo(m_iLogonID, i, btUserName, btPassword, ref iAuthority);
                if (iRet >= 0)
		        {
                    //convert byte[] to string
                    string strUsername = Encoding.ASCII.GetString(btUserName);
                    cboUserList.Items.Insert(i, strUsername);
		        }
	        }

            if (cboUserList.Items.Count > 0)
	        {
		        m_bIsAdmin = true;
                cboUserList.SelectedIndex = 0 ;
	        }
	        else
	        {
		        m_bIsAdmin = false;
	        }
        }

        private void cboUserList_SelectedIndexChanged(object sender, EventArgs e)
        {
            UpdateLocalAndRemoteAuthority();
            UpdateAuthorityOfChannel();
        }

        private void UpdateLocalAndRemoteAuthority()
        {
	        if (!m_bIsAdmin)
	        {
		        return;
	        }

	        UpdateEnablelistLocalAndRemote();

            foreach (KeyValuePair<int, CheckBox> pair in m_dicAuthorityIndexMapControl)
            {
                pair.Value.Checked = false;
            }

	        string strName;
            strName = cboUserList.Text;
            IntPtr ptUserAut = IntPtr.Zero;
            try
            {
                USER_AUTHORITY strUserAut = new USER_AUTHORITY();
                strUserAut.structAutInfo = new AUTHORITY_INFO[27];
                for (int i = 0; i < 27; i++)
                {
                    strUserAut.structAutInfo[i] = new AUTHORITY_INFO();
                    strUserAut.structAutInfo[i].uiList = new Int32[4];
                }
                strUserAut.iNeedSize = Marshal.SizeOf(strUserAut);
                ptUserAut = Marshal.AllocHGlobal(Marshal.SizeOf(strUserAut));
                Marshal.StructureToPtr(strUserAut, ptUserAut, true);
                int iRet = NVSSDK.NetClient_GetUserAuthority(m_iLogonID, strName, ptUserAut, Marshal.SizeOf(strUserAut));
                strUserAut = (USER_AUTHORITY)Marshal.PtrToStructure(ptUserAut, typeof(USER_AUTHORITY));
                if (iRet < 0)
                {
                    return;
                }

                for (int i = 0; i < 27; i++)
                {
                    if (1 == strUserAut.structAutInfo[i].uiList[0])
                    {
                        if (m_dicAuthorityIndexMapControl.ContainsKey(i))
                        {
                            m_dicAuthorityIndexMapControl[i].Checked = true;
                        }
                    }  // if (1 == strUserAut.structAutInfo[i].uiList[0])
                   
                }  // end of for (int i = 0; i < 27; i++)
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(ptUserAut);
            }


            // Set Check all Control state of local and remote
            //CheckSelectedAllLocal();
            //CheckSelectedAllRemote();
        }

        private void UpdateEnablelistLocalAndRemote()
        {
            foreach (KeyValuePair<int, CheckBox> pair in m_dicAuthorityIndexMapControl)
            {
                pair.Value.Enabled = false;
            }
            
	        chkLocalAll.Enabled  = true;
            chkRemoteAll.Enabled = true;

	        int iCurUser = cboUserList.SelectedIndex;

            if (0 == iCurUser)
            {
                chkLocalAll.Enabled = false;
                chkRemoteAll.Enabled = false;
                return;
            }

	        int iAuthority = 0;
            byte[] btUserName = new byte[128];
            byte[] btPassword = new byte[128];
            int iRet = NVSSDK.NetClient_GetUserInfo(m_iLogonID, iCurUser, btUserName, btPassword, ref iAuthority);

            IntPtr ptGroupAut = IntPtr.Zero;
            try
            {
                GROUP_AUTHORITY strGroupAut = new GROUP_AUTHORITY();
                strGroupAut.iSize = Marshal.SizeOf(strGroupAut);
                strGroupAut.uiList = new Int32[2];
                strGroupAut.btList = new byte[256];
                strGroupAut.iGroupNO = iAuthority;
                int iBufSize = Marshal.SizeOf(strGroupAut);
                int isize = Marshal.SizeOf(typeof(GROUP_AUTHORITY));
                ptGroupAut = Marshal.AllocHGlobal(Marshal.SizeOf(strGroupAut));
                Marshal.StructureToPtr(strGroupAut, ptGroupAut, true);
                //get group of Authority .  Gets the items that is allowed to be set by a Authority
                iRet = NVSSDK.NetClient_GetGroupAuthority(m_iLogonID, ptGroupAut, iBufSize);
                strGroupAut = (GROUP_AUTHORITY)Marshal.PtrToStructure(ptGroupAut, typeof(GROUP_AUTHORITY));
                if (iRet >= 0)
                {
                    for (int i = 0; i < 27; i++)
                    {
                        if (1 == (strGroupAut.uiList[0] >> i & 0x01))
                        {
                            if (m_dicAuthorityIndexMapControl.ContainsKey(i))
                            {
                                m_dicAuthorityIndexMapControl[i].Enabled = true;
                            }
                        } // end of 
                    } // end of for (int i=0; i< 27; i++)
                } // end of if (iRet >= 0)
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(ptGroupAut);
            }
        }

        private void UpdateAuthorityOfChannel()
        {
            int iCurUser = cboUserList.SelectedIndex;
            int iAuthority = 0;
            byte[] btUserName = new byte[128];
            byte[] btPassword = new byte[128];
            int iRet = NVSSDK.NetClient_GetUserInfo(m_iLogonID, iCurUser, btUserName, btPassword, ref iAuthority);
            
            m_lsAuthority.Clear();
            IntPtr ptGroupAut = IntPtr.Zero;
            try
            {
                GROUP_AUTHORITY strGroupAut = new GROUP_AUTHORITY();
                strGroupAut.iSize = Marshal.SizeOf(strGroupAut);
                strGroupAut.uiList = new Int32[2];
                strGroupAut.btList = new byte[256];
                strGroupAut.iGroupNO = iAuthority;
                int iBufSize = Marshal.SizeOf(strGroupAut);
                int isize = Marshal.SizeOf(typeof(GROUP_AUTHORITY));
                ptGroupAut = Marshal.AllocHGlobal(Marshal.SizeOf(strGroupAut));
                Marshal.StructureToPtr(strGroupAut, ptGroupAut, true);
                //Get Group authority
                iRet = NVSSDK.NetClient_GetGroupAuthority(m_iLogonID, ptGroupAut, iBufSize);
                strGroupAut = (GROUP_AUTHORITY)Marshal.PtrToStructure(ptGroupAut, typeof(GROUP_AUTHORITY));
                if (iRet >= 0)
                {
                    for (int i = 0; i < 27; i++)
                    {
                        if (1 == (strGroupAut.uiList[0] >> i & 0x01))
                        {
                            switch (i)
                            {
                                // channel
                                case LOCAL_PTZ_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("L.tri. Camera control", LOCAL_PTZ_CHANNEL));
                                        break;
                                    }
                                case REMOTE_PTZ_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("R.tri. Camera control", REMOTE_PTZ_CHANNEL));
                                        break;
                                    }
                                case LOCAL_PLAYBACK_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("Local Replay", LOCAL_PLAYBACK_CHANNEL));
                                        break;
                                    }
                                case REMOTE_PLAYBACK_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("Remote Replay", REMOTE_PLAYBACK_CHANNEL));
                                        break;
                                    }
                                case REMOTE_PREVIEW_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("Remote Viewing", REMOTE_PREVIEW_CHANNEL));
                                        break;
                                    }
                                case LOCAL_HAND_OPERATE_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("Local Manual Operation", LOCAL_HAND_OPERATE_CHANNEL));
                                        break;
                                    }
                                case REMOTE_HAND_OPERATE_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("Remote Manual Operation", REMOTE_HAND_OPERATE_CHANNEL));
                                        break;
                                    }
                                case LOCAL_PREVIEW_CHANNEL:
                                    {
                                        m_lsAuthority.Add(new TextAndValue("Local preview", LOCAL_PREVIEW_CHANNEL));
                                        break;
                                    }
                                default:
                                    break;
                            }
                        }  // end of 
                    } // end of for (int i=0; i< 27; i++)

                }  // end of if (iRet >= 0)
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(ptGroupAut);
            }
            if (m_lsAuthority.Count > 0)
            {
                cboRights.DataSource = null;
                cboRights.DataSource = m_lsAuthority;
                cboRights.DisplayMember = "DisplayText";
                cboRights.ValueMember = "RealValue";
                cboRights.SelectedIndex = 0;
            }

            if (cboRights.Items.Count <= 0)
            {
                return;
            }

            m_iCurPageNo = -1;
            
            LoadRightsInfoOfChannel();

            if (cboPage.Items.Count > 0)
            {
                cboPage.SelectedIndex = 0;
                cboPage_SelectedIndexChanged(null,null);
            }
        }
        private void cboRights_SelectedIndexChanged(object sender, EventArgs e)
        {
            //if (cboRights.Items.Count <= 0)
            //{
            //    return;
            //}
            //LoadRightsInfoOfChannel();

            //if (cboPage.Items.Count > 0)
            //{
            //    cboPage.SelectedIndex = 0;
            //}
        }
        private void LoadRightsInfoOfChannel()
        {
            if (!m_bIsAdmin)
            {
                return;
            }

            for (int i = 0; i < 16; i++)
            {
                m_iRightChannel[i] = 0;
            }

            string strValue = cboRights.SelectedValue.ToString();
            int iValue = Convert.ToInt32(strValue);
            if (iValue < 0)
            {
                return;
            }

            string strName;
            strName = cboUserList.Text;

            AUTHORITY_INFO_V1 stAUTHORITY_INFO_V1 = new AUTHORITY_INFO_V1();
            stAUTHORITY_INFO_V1.iSize = Marshal.SizeOf(stAUTHORITY_INFO_V1);
            stAUTHORITY_INFO_V1.iLevel = iValue;
            stAUTHORITY_INFO_V1.tbUsername = new byte[32];
            stAUTHORITY_INFO_V1.iList = new Int32[16];
            CommonFunction.ByteCopy(Encoding.ASCII.GetBytes(strName), stAUTHORITY_INFO_V1.tbUsername);
            int iReturn = -1;
            IntPtr intptr = IntPtr.Zero;
            try
            {
                intptr = Marshal.AllocHGlobal(Marshal.SizeOf(stAUTHORITY_INFO_V1));
                Marshal.StructureToPtr(stAUTHORITY_INFO_V1, intptr, true);
                int iRet = NVSSDK.NetClient_GetDevConfig(m_iLogonID, NetSDKCmd.NET_CLIENT_MODIFYAUTHORITY, 0x7FFFFFFF, intptr, Marshal.SizeOf(stAUTHORITY_INFO_V1), ref iReturn);
                stAUTHORITY_INFO_V1 = (AUTHORITY_INFO_V1)Marshal.PtrToStructure(intptr, typeof(AUTHORITY_INFO_V1));

                if (0 == iRet)
                {
                    for (int i = 0; i < 16; i++)
                    {
                        m_iRightChannel[i] = stAUTHORITY_INFO_V1.iList[i];
                    }
                }
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(intptr);
            }
        }
        
        private void cboPage_SelectedIndexChanged(object sender, EventArgs e)
        {
            // Save Current Info from UI To Memory
            SaveUIToMemory(m_iCurPageNo);

            // Show info from Memory To UI
            m_iCurPageNo = cboPage.SelectedIndex;
            ShowMemoryToUI(m_iCurPageNo);
            
        }
        private void btnPrePage_Click(object sender, EventArgs e)
        {
            if (m_iCurPageNo - 1 >= 0)
            {
                cboPage.SelectedIndex = cboPage.SelectedIndex -1;
            }
        }

        private void btnNextPage_Click(object sender, EventArgs e)
        {
            if (m_iCurPageNo + 1 <= m_iMaxPage)
            {
                cboPage.SelectedIndex = cboPage.SelectedIndex + 1;
            }
        }
        private void SaveUIToMemory(int _iPageNo)
        {
            if (_iPageNo < 0 || _iPageNo > m_iMaxPage)
            {
                return;
            }

            for (int iCheckIndex = 0; iCheckIndex < m_listChannelCheckBox.Count; iCheckIndex++)
            {
                int iChannelNo = m_listChannelCheckBox.Count * _iPageNo + iCheckIndex;
                
                if (iChannelNo >= m_iChannelNum)
                {
                    break;
                }

                int iIndex = iChannelNo / 32;
                int iBite = iChannelNo % 32;
                if (m_listChannelCheckBox[iCheckIndex].Checked)
                {
                    m_iRightChannel[iIndex] |= 0x01 << iBite;
                }
                else
                {
                    m_iRightChannel[iIndex] &= ~(0x01 << iBite);
                }
            }
        }
        private void ShowMemoryToUI(int _iPageNo)
        {
            if (_iPageNo < 0 || _iPageNo > m_iMaxPage)
            {
                return;
            }

            int iCurUser = cboUserList.SelectedIndex;

            for (int iCheckIndex = 0; iCheckIndex < m_listChannelCheckBox.Count; iCheckIndex++)
            {
                int iChannelNo = m_listChannelCheckBox.Count * _iPageNo + iCheckIndex;
                m_listChannelCheckBox[iCheckIndex].Text = (iChannelNo + 1).ToString();
               
                //if it not support the channel, put it enabled = false
                if (iChannelNo >= m_iChannelNum)
                {
                    m_listChannelCheckBox[iCheckIndex].Checked = false;
                    m_listChannelCheckBox[iCheckIndex].Enabled = false;
                    continue;
                }
                
                // admin can not modify channel authority
                if (0 == iCurUser)
                {
                    m_listChannelCheckBox[iCheckIndex].Enabled = false;
                }
                else
                {
                    m_listChannelCheckBox[iCheckIndex].Enabled = true;
                }
                

                int iIndex = iChannelNo / 32;
                int iBite = iChannelNo % 32;
                int iCheckedValue = (m_iRightChannel[iIndex] >> iBite) & 0x01;
                if (iCheckedValue > 0)
                {
                    m_listChannelCheckBox[iCheckIndex].Checked = true;
                }
                else
                {
                    m_listChannelCheckBox[iCheckIndex].Checked = false;
                }
            }
        }
        private void btnSave_Click(object sender, EventArgs e)
        {
            SaveLocalAndRemoteAuthority();
            SaveChannelAuthority();
        }

        private void SaveLocalAndRemoteAuthority()
        {
            int iModifyNum = 0;
            int iAllSaveNum = 0;
            
            string strName;
            strName = cboUserList.Text;
            IntPtr ptUserAut = IntPtr.Zero;
            USER_AUTHORITY strUserAut = new USER_AUTHORITY();
            strUserAut.structAutInfo = new AUTHORITY_INFO[27];
            for (int i = 0; i < 27; i++)
            {
                strUserAut.structAutInfo[i] = new AUTHORITY_INFO();
                strUserAut.structAutInfo[i].uiList = new Int32[4];
            }
                
            foreach (KeyValuePair<int, CheckBox> pair in m_dicAuthorityIndexMapControl)
            {
                // protect
                if (iModifyNum < 0 || iModifyNum >= 27)
                {
                    Console.WriteLine("structAutInfo[iModifyNum]  iModifyNum error %d", iModifyNum);
                    continue;
                }
                
                if (pair.Value.Checked)
                {
                    strUserAut.structAutInfo[iModifyNum].uiList[0] = 1;
                }
                else
                {
                    strUserAut.structAutInfo[iModifyNum].uiList[0] = 0;
                }
                strUserAut.structAutInfo[iModifyNum].iLevel = pair.Key;

                iModifyNum++;
                iAllSaveNum++;
                
                // per 10 authoritis save once, or the last time to save less than 10
                if (10 == iModifyNum || iAllSaveNum == m_dicAuthorityIndexMapControl.Count)
                {
                    try
                    {
                        strUserAut.iNeedSize = Marshal.SizeOf(typeof(AUTHORITY_INFO)) * iModifyNum + 4;
                        ptUserAut = Marshal.AllocHGlobal(Marshal.SizeOf(strUserAut));
                        Marshal.StructureToPtr(strUserAut, ptUserAut, true);
                        int iRet = NVSSDK.NetClient_ModifyUserAuthority(m_iLogonID, strName, ptUserAut, Marshal.SizeOf(strUserAut));
                        if (0 != iRet)
                        {
                            Console.WriteLine("NetClient_ModifyUserAuthority error");
                        }
                        
                    }
                    catch (System.Exception ex)
                    {
                        Console.WriteLine(ex.Message);	
                    }
                    finally
                    {
                        Marshal.FreeHGlobal(ptUserAut);
                    }

                    // recount
                    iModifyNum = 0;
                }
            }
        }
        private void SaveChannelAuthority()
        {
            if (!m_bIsAdmin)
            {
                return;
            }
            SaveUIToMemory(m_iCurPageNo);

            string strValue = cboRights.SelectedValue.ToString();
            int iValue = Convert.ToInt32(strValue);
            if (iValue < 0)
            {
                return;
            }

            string strName;
            strName = cboUserList.Text;

            AUTHORITY_INFO_V1 stAUTHORITY_INFO_V1 = new AUTHORITY_INFO_V1();
            stAUTHORITY_INFO_V1.iSize = Marshal.SizeOf(stAUTHORITY_INFO_V1);
            stAUTHORITY_INFO_V1.iLevel = iValue;
            stAUTHORITY_INFO_V1.tbUsername = new byte[32];
            stAUTHORITY_INFO_V1.iList = new Int32[16];
            CommonFunction.ByteCopy(Encoding.ASCII.GetBytes(strName), stAUTHORITY_INFO_V1.tbUsername);
            for (int i = 0; i < 16; i++)
            {
                stAUTHORITY_INFO_V1.iList[i] = m_iRightChannel[i];
            }

            IntPtr intptr = IntPtr.Zero;
            try
            {
                intptr = Marshal.AllocHGlobal(Marshal.SizeOf(stAUTHORITY_INFO_V1));
                Marshal.StructureToPtr(stAUTHORITY_INFO_V1, intptr, true);
                int iRet = NVSSDK.NetClient_SetDevConfig(m_iLogonID, NetSDKCmd.NET_CLIENT_MODIFYAUTHORITY, 0x7FFFFFFF, intptr, Marshal.SizeOf(stAUTHORITY_INFO_V1));
                if (0 != iRet)
                {
                    Console.WriteLine("NetClient_SetDevConfig return error");
                }
            }
            catch (System.Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                Marshal.FreeHGlobal(intptr);
            }
        }

        private void btnClose_Click(object sender, EventArgs e)
        {
            this.Close();
        }
        private void chkLocalAll_CheckedChanged(object sender, EventArgs e)
        {
            chkManual.Checked = chkManual.Enabled ? chkLocalAll.Checked : chkManual.Checked;
            chkReboot.Checked = chkReboot.Enabled ? chkLocalAll.Checked : chkReboot.Checked;
            chkLogSearch.Checked = chkLogSearch.Enabled ? chkLocalAll.Checked : chkLogSearch.Checked;
            chkAlarm.Checked = chkAlarm.Enabled ? chkLocalAll.Checked : chkAlarm.Checked;
            chkManageChannels.Checked = chkManageChannels.Enabled ? chkLocalAll.Checked : chkManageChannels.Checked;
            chkParaSet.Checked = chkParaSet.Enabled ? chkLocalAll.Checked : chkParaSet.Checked;
            chkSystemSet.Checked = chkSystemSet.Enabled ? chkLocalAll.Checked : chkSystemSet.Checked;
            chkUsrManage.Checked = chkUsrManage.Enabled ? chkLocalAll.Checked : chkUsrManage.Checked;
        }
        private void chkRemoteAll_CheckedChanged(object sender, EventArgs e)
        {
            chkRemoteManual.Checked = chkRemoteManual.Enabled ? chkRemoteAll.Checked : chkRemoteManual.Checked;
            chkRemoteReboot.Checked = chkRemoteReboot.Enabled ? chkRemoteAll.Checked : chkRemoteReboot.Checked;
            chkRemoteLogSearch.Checked = chkRemoteLogSearch.Enabled ? chkRemoteAll.Checked : chkRemoteLogSearch.Checked;
            chkRemoteAlarm.Checked = chkRemoteAlarm.Enabled ? chkRemoteAll.Checked : chkRemoteAlarm.Checked;
            chkRemoteChannel.Checked = chkRemoteChannel.Enabled ? chkRemoteAll.Checked : chkRemoteChannel.Checked;
            chkRemoteParaSet.Checked = chkRemoteParaSet.Enabled ? chkRemoteAll.Checked : chkRemoteParaSet.Checked;
            chkRemoteSystemSet.Checked = chkRemoteSystemSet.Enabled ? chkRemoteAll.Checked : chkRemoteSystemSet.Checked;
            chkRemoteUsrManage.Checked = chkRemoteUsrManage.Enabled ? chkRemoteAll.Checked : chkRemoteUsrManage.Checked;
            chkRemoteVoice.Checked = chkRemoteVoice.Enabled ? chkRemoteAll.Checked : chkRemoteVoice.Checked;
        }
        private void CheckSelectedAllLocal()
        {
            if ((chkManual.Checked || !chkManual.Enabled) &&
                (chkReboot.Checked || !chkReboot.Enabled) &&
                (chkLogSearch.Checked || !chkLogSearch.Enabled) &&
                (chkAlarm.Checked || !chkAlarm.Enabled) &&
                (chkManageChannels.Checked || !chkManageChannels.Enabled) &&
                (chkParaSet.Checked || !chkParaSet.Enabled) &&
                (chkSystemSet.Checked || !chkSystemSet.Enabled) &&
                (chkUsrManage.Checked || !chkUsrManage.Enabled))
            {
                chkLocalAll.Checked = true;
            }
            else
            {
                chkLocalAll.Checked = false;
            }
        }
        private void CheckSelectedAllRemote()
        {
            if ((chkRemoteManual.Checked || !chkRemoteManual.Enabled) &&
               (chkRemoteReboot.Checked || !chkRemoteReboot.Enabled) &&
               (chkRemoteLogSearch.Checked || !chkRemoteLogSearch.Enabled) &&
               (chkRemoteAlarm.Checked || !chkRemoteAlarm.Enabled) &&
               (chkRemoteChannel.Checked || !chkRemoteChannel.Enabled) &&
               (chkRemoteParaSet.Checked || !chkRemoteParaSet.Enabled) &&
               (chkRemoteSystemSet.Checked || !chkRemoteSystemSet.Enabled) &&
               (chkRemoteUsrManage.Checked || !chkRemoteUsrManage.Enabled) &&
               (chkRemoteVoice.Checked || !chkRemoteVoice.Enabled))
            {
                chkRemoteAll.Checked = true;
            }
            else
            {
                chkRemoteAll.Checked = false;
            }
        }

        private void cboRights_SelectionChangeCommitted(object sender, EventArgs e)
        {
            if (cboRights.Items.Count <= 0)
            {
                return;
            }

            m_iCurPageNo = -1;
            LoadRightsInfoOfChannel();

            if (cboPage.Items.Count > 0)
            {
                cboPage.SelectedIndex = 0;
                cboPage_SelectedIndexChanged(sender, e);
            }
        }

    }

    class TextAndValue
    {
        private int _RealValue = 0;
        private string _DisplayText = "";

        public string DisplayText
        {
            get
            {
                return _DisplayText;
            }
        }

        public string RealValue
        {
            get
            {
                return _RealValue.ToString();
            }
        }

        public TextAndValue(string ShowText, int RealVal)
        {
            _DisplayText = ShowText;
            _RealValue = RealVal;
        }
    }
}
