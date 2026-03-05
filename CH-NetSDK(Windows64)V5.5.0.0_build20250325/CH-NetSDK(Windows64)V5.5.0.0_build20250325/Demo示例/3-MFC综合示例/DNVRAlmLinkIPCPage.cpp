// DNVRAlmLinkIPCPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DNVRAlmLinkIPCPage.h"

#define LINK_SOUND				0
#define LINK_OUTPORT			1


// CLS_DNVRAlmLinkIPCPage dialog

IMPLEMENT_DYNAMIC(CLS_DNVRAlmLinkIPCPage, CDialog)

CLS_DNVRAlmLinkIPCPage::CLS_DNVRAlmLinkIPCPage(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_DNVRAlmLinkIPCPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_pclsChanCheck = NULL;
	m_bFrontChangeCPWindow = false;
	m_bFrontChangeAVWindow = false;
}

CLS_DNVRAlmLinkIPCPage::~CLS_DNVRAlmLinkIPCPage()
{
	if (NULL != m_pclsChanCheck)
	{
		delete m_pclsChanCheck;
		m_pclsChanCheck = NULL; 
	}
}

// CLS_DNVRAlmLinkIPCPage message handlers

void CLS_DNVRAlmLinkIPCPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_DISPLAYENABLE, m_chkDisplayEnable);
	DDX_Control(pDX, IDC_CHECK_SOUNDENABLE, m_chkSoundEnable);
	DDX_Control(pDX, IDC_COMBO_SOUNDNO, m_iSerialNo);
	DDX_Control(pDX, IDC_COMBO_FRONTEND, m_cboFrontEndLinkType);
	DDX_Control(pDX, IDC_COMBO_NO, m_cboNo);
	DDX_Control(pDX, IDC_COMBO_AREANO, m_cboAreaNo);
	DDX_Control(pDX, IDC_COMBO_LINKIPC_CHANNEL, m_cboLinkChannel);
}


BEGIN_MESSAGE_MAP(CLS_DNVRAlmLinkIPCPage, CLS_BasePage)
	ON_CBN_SELCHANGE(IDC_COMBO_FRONTEND, &CLS_DNVRAlmLinkIPCPage::OnCbnSelchangeComboFrontend)
	ON_BN_CLICKED(IDC_BTN_ALARM_LINK_FRONTEND, &CLS_DNVRAlmLinkIPCPage::OnBnClickedBtnAlarmLinkFrontend)
	ON_CBN_SELCHANGE(IDC_COMBO_NO, &CLS_DNVRAlmLinkIPCPage::OnCbnSelchangeComboNo)
	ON_CBN_SELCHANGE(IDC_COMBO_AREANO, &CLS_DNVRAlmLinkIPCPage::OnCbnSelchangeComboAreano)
	ON_BN_CLICKED(IDC_BUTTON_SETAREALINK, &CLS_DNVRAlmLinkIPCPage::OnBnClickedButtonSetarealink)
END_MESSAGE_MAP()


// CLS_DNVRAlmLinkIPCPage message handlers
BOOL CLS_DNVRAlmLinkIPCPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();
	UI_UpdateChanCheck();
	

	int iOffset = 0;
	RECT rcPC = {0};
	RECT rcTemp = {0};
	GetDlgItem(IDC_STATIC_LINKSET)->GetWindowRect(&rcPC);
	ScreenToClient(&rcPC);

	GetDlgItem(IDC_STATIC_LINKSETAVIDEO)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	iOffset = rcPC.top - rcTemp.top;
	OffsetWindow(IDC_STATIC_LINKSETAVIDEO,0,iOffset);
	OffsetWindow(IDC_CHECK_DISPLAYENABLE,0,iOffset);
	OffsetWindow(IDC_CHECK_SOUNDENABLE,0,iOffset);
	OffsetWindow(IDC_STATIC_SOUNDNUM, 0, iOffset);
	OffsetWindow(IDC_COMBO_SOUNDNO, 0, iOffset);

	return TRUE;
}

void CLS_DNVRAlmLinkIPCPage::MoveAudioWindow(int _iOffset)
{
	if (100 == _iOffset)
	{
		GetDlgItem(IDC_BUTTON_AVIDEO)->ShowWindow(SW_HIDE);
		if(m_bFrontChangeAVWindow)
			return;
		m_bFrontChangeAVWindow = true;
		

	}else{
		if(!m_bFrontChangeAVWindow)
			return;
		m_bFrontChangeAVWindow = false;
	}
	OffsetWindow(IDC_STATIC_LINKSETAVIDEO,0,_iOffset);
	OffsetWindow(IDC_CHECK_DISPLAYENABLE,0,_iOffset);
	OffsetWindow(IDC_CHECK_SOUNDENABLE,0,_iOffset);
	OffsetWindow(IDC_STATIC_SOUNDNUM, 0, _iOffset);
	OffsetWindow(IDC_COMBO_SOUNDNO, 0, _iOffset);
}

void CLS_DNVRAlmLinkIPCPage::MoveCPWindow(int _iOffset)
{
	if (100 == _iOffset)
	{
		if(m_bFrontChangeCPWindow)
			return;
		m_bFrontChangeCPWindow = true;
		
	}else{
		if(!m_bFrontChangeCPWindow)
			return;
		m_bFrontChangeCPWindow = false;
	}

	OffsetWindow(IDC_STATIC_LINKSET,0,_iOffset);
	


	RECT rc = {0};
	GetDlgItem(IDC_STATIC_LINKSET)->GetWindowRect(&rc);
	ScreenToClient(&rc);
	rc.top += 15;
	rc.bottom -= 10;
	rc.left += 5;
	rc.right -= 5;
	m_pclsChanCheck->MoveWindow(&rc);
	m_pclsChanCheck->ShowWindow(SW_SHOW);

}

void CLS_DNVRAlmLinkIPCPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}

	UI_ShowLinkType();
	UI_UpdateChannel();
	
}

void CLS_DNVRAlmLinkIPCPage::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_DNVRAlmLinkIPCPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_CHANNELENABLE);
	SetDlgItemTextEx(IDC_CHECK_DISPLAYENABLE,IDS_CONFIG_DNVR_ALMLINK_DISPLAYENABLE);
	SetDlgItemTextEx(IDC_CHECK_SOUNDENABLE,IDS_CONFIG_DNVR_ALMLINK_SOUNDENABLE);
	SetDlgItemTextEx(IDC_BTN_ALARM_LINK_FRONTEND,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_SOUNDNUM, IDS_SOUNDNO);
	SetDlgItemTextEx(IDC_BUTTON_SETAREALINK,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_CHANNELENABLE,IDS_CONFIG_DNVR_ALMLINK_OUTPORT);
	SetDlgItemTextEx(IDC_STATIC_AREANO,IDS_ITS_AREAID);
	SetDlgItemTextEx(IDC_STATIC_NO,IDS_CONFIG_LINK_DEV_NUM);
	SetDlgItemTextEx(IDC_STATIC_LINKIPCCHANNEL,IDS_ITS_CHANNELID);
	SetDlgItemTextEx(IDC_STATIC_FRONTLINKTYPE,IDS_ALARM_LINK_INTERVAL_LINK_TYPE);
	
	for (int i = 0; i < MAX_NVR_LINK_IPC_NUMBER; i++)
	{
		m_cboNo.AddString(IntToString(i));
	}
	m_cboNo.SetCurSel(0);

	for (int i = 0; i < MAX_CPC_AREA_NUM; i++)
	{
		m_cboAreaNo.AddString(IntToString(i));
	}
	m_cboAreaNo.SetCurSel(0);
	
}

void CLS_DNVRAlmLinkIPCPage::UI_UpdateChannel()
{
	if (m_iLogonID < 0)
		return ;

	int iChannelNum = 0;
	m_cboLinkChannel.ResetContent();
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	for (int i=0; i<iChannelNum; i++)
	{
		CString strChannelNum;
		strChannelNum.Format("%d",i);
		m_cboLinkChannel.AddString(strChannelNum);
	}
	m_cboLinkChannel.SetCurSel(0);
}


void CLS_DNVRAlmLinkIPCPage::UI_ShowLinkCP(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_LINKSET)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_CHANNELENABLE)->ShowWindow(FALSE);
	m_pclsChanCheck->ShowWindow(_iCmdShow?SW_SHOW:SW_HIDE);

	int iCurIndex = m_cboNo.GetCurSel();
	if(iCurIndex != -1)
	{
		AlarmNVRLinkIPC &tAlarmNVRLinkIPC = m_tAlarmNVRLinkIPCParam.tAlarmNVRLinkIPC[iCurIndex];
		m_pclsChanCheck->InitData(MAX_BITSET_COUNT,tAlarmNVRLinkIPC.tULinkIPCParam.iCommonSet);
	}

}

void CLS_DNVRAlmLinkIPCPage::UI_ShowLinkAV(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_LINKSETAVIDEO)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_CHECK_DISPLAYENABLE)->ShowWindow(SW_HIDE);
	GetDlgItem(IDC_CHECK_SOUNDENABLE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_STATIC_SOUNDNUM)->ShowWindow(_iCmdShow);
	m_iSerialNo.ShowWindow(_iCmdShow);
}

void CLS_DNVRAlmLinkIPCPage::UI_ShowLinkType()
{
	UI_ShowLinkCP(SW_HIDE);
	UI_ShowLinkAV(SW_HIDE);
	
	UI_ShowLinkFrontEnd(SW_SHOW);
}

void CLS_DNVRAlmLinkIPCPage::OffsetWindow(int iID,int dx,int dy)
{
	RECT rcTemp = {0};
	GetDlgItem(iID)->GetWindowRect(&rcTemp);
	ScreenToClient(&rcTemp);
	OffsetRect(&rcTemp,dx,dy);
	GetDlgItem(iID)->MoveWindow(&rcTemp);
}

void CLS_DNVRAlmLinkIPCPage::UI_ShowLinkFrontEnd(int _iCmdShow)
{
	GetDlgItem(IDC_STATIC_FRONTLINKTYPE)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_COMBO_FRONTEND)->ShowWindow(_iCmdShow);
	GetDlgItem(IDC_BTN_ALARM_LINK_FRONTEND)->ShowWindow(_iCmdShow);

	if(SW_SHOW==_iCmdShow)
	{
		m_cboFrontEndLinkType.ResetContent();
		m_cboFrontEndLinkType.AddString(GetTextEx(IDS_CONFIG_DNVR_LINKAUDIO));
		m_cboFrontEndLinkType.SetItemData(0,ALARMLINKTYPE_LINKSOUND);
		m_cboFrontEndLinkType.AddString(GetTextEx(IDS_CONFIG_DNVR_LINKOUTPORT));
		m_cboFrontEndLinkType.SetItemData(1,ALARMLINKTYPE_LINKOUTPORT);
		m_cboFrontEndLinkType.SetCurSel(0);

		OnCbnSelchangeComboFrontend();
	}

}

//add by zhy end

void CLS_DNVRAlmLinkIPCPage::UI_UpdateChanCheck()
{
	if (NULL == m_pclsChanCheck)
	{
		m_pclsChanCheck = new CLS_ChanCheck();
		m_pclsChanCheck->Create(IDD_DLG_CFG_CHANNEL_CHECK, this);
	}

	if (NULL == m_pclsChanCheck)
	{
		return;
	}

	RECT rc = {0};
	GetDlgItem(IDC_STATIC_LINKSET)->GetWindowRect(&rc);
	ScreenToClient(&rc);
	rc.top += 15;
	rc.bottom -= 10;
	rc.left += 5;
	rc.right -= 5;
	m_pclsChanCheck->MoveWindow(&rc);
	m_pclsChanCheck->ShowWindow(SW_HIDE);
}

void CLS_DNVRAlmLinkIPCPage::UpdateVoice()
{
	m_iSerialNo.ResetContent();
	AudioSampleFileCount tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	int iReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_AUDIO_SAMPLE_FILE_COUNT, m_iChannelNO, &tInfo, sizeof(tInfo), &iReturn);
	if(RET_SUCCESS == iRet)
	{
		if(tInfo.iTotalCount > 100 || tInfo.iTotalCount <= 0)
		{
			tInfo.iTotalCount = 3;
		}

		CString cstrTemp;
		int iItem = 0;
		for(int i = 0; i < tInfo.iSampleCount; i++)
		{
			cstrTemp.Format("%d", i+1);
			m_iSerialNo.InsertString(iItem++,GetText(IDS_LINK_GUARD_SOUND) + cstrTemp);
		}

		for(int j = 0; j < tInfo.iCustomCount; j++)
		{
			cstrTemp.Format("%d", j+1);
			m_iSerialNo.InsertString(iItem++,GetText(IDS_HD_MODE_CUSTOMIZED) + cstrTemp);
		}
		m_iSerialNo.SetCurSel(0);
	}
}

void CLS_DNVRAlmLinkIPCPage::UI_UpdateCurIndex(int _iCurIndex)
{
	if(_iCurIndex < 0 || _iCurIndex > MAX_NVR_LINK_IPC_NUMBER)
		return;

	AlarmNVRLinkIPC &tAlarmNVRLinkIPC = m_tAlarmNVRLinkIPCParam.tAlarmNVRLinkIPC[_iCurIndex];

	m_cboLinkChannel.SetCurSel(tAlarmNVRLinkIPC.iLinkChannelNo);

	switch(tAlarmNVRLinkIPC.iLinkType)
	{
	case ALARMLINKTYPE_LINKSOUND:
		m_cboFrontEndLinkType.SetCurSel(0);
		m_chkSoundEnable.SetCheck(tAlarmNVRLinkIPC.tULinkIPCParam.tLinkSoundParam.iEnable);
		m_iSerialNo.SetCurSel(tAlarmNVRLinkIPC.tULinkIPCParam.tLinkSoundParam.iSerialNo);
		break;
	case ALARMLINKTYPE_LINKOUTPORT:
		m_cboFrontEndLinkType.SetCurSel(1);
		m_pclsChanCheck->InitData(MAX_BITSET_COUNT,tAlarmNVRLinkIPC.tULinkIPCParam.iCommonSet);
		break;
	default:
		break;
	}
	OnCbnSelchangeComboFrontend();
	UpdateData(FALSE);
}

void CLS_DNVRAlmLinkIPCPage::UpdateNVRLinkIPC(int _iAreaNo)
{
	if(_iAreaNo < 0 || _iAreaNo > MAX_CPC_AREA_NUM)
		return;

	memset(&m_tAlarmNVRLinkIPCParam,0x00,sizeof(AlarmNVRLinkIPCParam));
	m_tAlarmNVRLinkIPCParam.iSize = sizeof(AlarmNVRLinkIPCParam);

	int iRet = NetClient_GetAlarmConfig(m_iLogonID,_iAreaNo,0,CMD_GET_ALARM_NVRLINKIPC,&m_tAlarmNVRLinkIPCParam);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmConfig(%d,%d,CMD_GET_ALARM_NVRLINKIPC)",m_iLogonID,_iAreaNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,CMD_GET_ALARM_NVRLINKIPC)",m_iLogonID,_iAreaNo);
	}

	UI_UpdateCurIndex(m_cboNo.GetCurSel());

	UpdateData(FALSE);

}


void CLS_DNVRAlmLinkIPCPage::OnCbnSelchangeComboFrontend()
{
	// TODO: Add your control notification handler code here
	//Linked sound and port output
    int iCurIndex = m_cboFrontEndLinkType.GetCurSel();
	switch(iCurIndex)
	{
	case LINK_SOUND:
		UI_ShowLinkAV(SW_SHOW);
		UI_ShowLinkCP(SW_HIDE);
		MoveAudioWindow(50);
		break;
	case LINK_OUTPORT:
		UI_ShowLinkAV(SW_HIDE);
		UI_ShowLinkCP(SW_SHOW);
		MoveCPWindow(50);
		break;
	default:
		break;
	}

}

void CLS_DNVRAlmLinkIPCPage::SetNVRLinkIPC()
{
	UpdateData(TRUE);

	int iLinkNo = m_cboNo.GetCurSel();

	if(iLinkNo < 0 || iLinkNo >= MAX_NVR_LINK_IPC_NUMBER)
	{
		return;
	}
	AlarmNVRLinkIPC &tAlarmNVRLinkIPC = m_tAlarmNVRLinkIPCParam.tAlarmNVRLinkIPC[iLinkNo];

	tAlarmNVRLinkIPC.iTrigChannelNo = m_cboAreaNo.GetCurSel();
	tAlarmNVRLinkIPC.iLinkChannelNo = m_cboLinkChannel.GetCurSel();
	tAlarmNVRLinkIPC.iMajorType = ALARM_TYPE_NVR_VCA;
	tAlarmNVRLinkIPC.iMinorType = VCA_NVR_EVENT_CPC_AREA_INFINITY;

	tAlarmNVRLinkIPC.iAlarmTypeParam = 0;
	int iCurIndex = m_cboFrontEndLinkType.GetCurSel();
	//Link front-end sound
	if (LINK_SOUND == iCurIndex)
	{
		tAlarmNVRLinkIPC.iLinkType = ALARMLINKTYPE_LINKSOUND;
		tAlarmNVRLinkIPC.tULinkIPCParam.tLinkSoundParam.iEnable = (BST_CHECKED == m_chkSoundEnable.GetCheck()) ? 1 : 0;
		tAlarmNVRLinkIPC.tULinkIPCParam.tLinkSoundParam.iSerialNo = m_iSerialNo.GetCurSel();

	}
	else if (LINK_OUTPORT == iCurIndex)//Linkage front-end output port
	{
		tAlarmNVRLinkIPC.iLinkType = ALARMLINKTYPE_LINKOUTPORT;
		m_pclsChanCheck->GetChanValue(tAlarmNVRLinkIPC.tULinkIPCParam.iCommonSet);
	}

}

void CLS_DNVRAlmLinkIPCPage::OnBnClickedBtnAlarmLinkFrontend()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	if(m_tAlarmNVRLinkIPCParam.iCount < m_cboNo.GetCurSel())
	{
		m_tAlarmNVRLinkIPCParam.iCount = m_cboNo.GetCurSel();
	}

	SetNVRLinkIPC();
}

void CLS_DNVRAlmLinkIPCPage::OnCbnSelchangeComboNo()
{
	// TODO: Add your control notification handler code here
	UI_UpdateCurIndex(m_cboNo.GetCurSel());
	UpdateData(FALSE);
}

void CLS_DNVRAlmLinkIPCPage::OnCbnSelchangeComboAreano()
{
	// TODO: Add your control notification handler code here
	UpdateNVRLinkIPC(m_cboAreaNo.GetCurSel());
}

void CLS_DNVRAlmLinkIPCPage::OnBnClickedButtonSetarealink()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iRet = NetClient_SetAlarmConfig(m_iLogonID, 0, 0, CMD_SET_ALARM_NVRLINKIPC, &m_tAlarmNVRLinkIPCParam);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig(%d,%d,CMD_SET_ALARM_NVRLINKIPC)",m_iLogonID,m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig(%d,%d,CMD_SET_ALARM_NVRLINKIPC)",m_iLogonID,m_iChannelNo);
	}
}

