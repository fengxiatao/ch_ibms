// ITSOtherPage.cpp : implementation file
//

#include "stdafx.h"
#include "ITSOtherPage.h"
#include <math.h>

unsigned int P_ulPicID[1] = {-1}; // Picture stream connection ID
unsigned int P_ulCapID[8]= {-1,-1,-1,-1,-1,-1,-1,-1}; //Simulated capture stream connection ID

// CLS_ITSOtherPage dialog

IMPLEMENT_DYNAMIC(CLS_ITSOtherPage, CDialog)

CLS_ITSOtherPage::CLS_ITSOtherPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ITSOtherPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNo = 0;

}

CLS_ITSOtherPage::~CLS_ITSOtherPage()
{
}

void CLS_ITSOtherPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_DEVICECODE, m_edtDeviceCode);
	DDX_Control(pDX, IDC_EDIT_ROADNAME, m_edtRoadName);
	DDX_Control(pDX, IDC_BUTTON_CAMLOCATION, m_btnCamLocation);
	DDX_Control(pDX, IDC_EDIT__WORKMODE, m_edtWokeMode);
	DDX_Control(pDX, IDC_EDIT_INTERVAL, m_edtInterval);
	DDX_Control(pDX, IDC_BUTTON_WORKMODE, m_btnWokeMode);
	DDX_Control(pDX, IDC_COMBO_PICCHAN, m_cboPicChan);
	DDX_Control(pDX, IDC_CHECK_PICCHAN, m_chkConnPic);
	DDX_Control(pDX, IDC_COMBO_CAPCHAN, m_cboCapChan);
	DDX_Control(pDX, IDC_COMBO_ITSNETMODE, m_cboNetMode);
	DDX_Control(pDX, IDC_BUTTON_CONNECT, m_btnConnect);
	DDX_Control(pDX, IDC_BUTTON_DISCONNECT, m_btnDisconnect);
	DDX_Control(pDX, IDC_COMBO_ITS_ENABLE_MODE, m_cboEnableMode);
	DDX_Control(pDX, IDC_COMBO_ITS_ENABLE2, m_cboEnable);
	DDX_Control(pDX, IDC_COMBO_ITS_DEVSTA, m_cboDevSta);
	DDX_Control(pDX, IDC_CBO_LINE_NO, m_cboLineNo);
	DDX_Control(pDX, IDC_CHK_LINK_ENABLE, m_chkLinkEnable);
}


BEGIN_MESSAGE_MAP(CLS_ITSOtherPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_CAMLOCATION, &CLS_ITSOtherPage::OnBnClickedButtonCamlocation)
	ON_BN_CLICKED(IDC_BUTTON_WORKMODE, &CLS_ITSOtherPage::OnBnClickedButtonWorkmode)
	ON_BN_CLICKED(IDC_BUTTON_CONNECT, &CLS_ITSOtherPage::OnBnClickedButtonConnect)
	ON_BN_CLICKED(IDC_BUTTON_DISCONNECT, &CLS_ITSOtherPage::OnBnClickedButtonDisconnect)
	ON_BN_CLICKED(IDC_BUTTON_ITS_ENABLE_SET, &CLS_ITSOtherPage::OnBnClickedButtonItsEnableSet)
	ON_CBN_SELCHANGE(IDC_COMBO_ITS_ENABLE_MODE, &CLS_ITSOtherPage::OnCbnSelchangeComboItsEnableMode)
	ON_BN_CLICKED(IDC_BUTTON_ITS_JPEGSIZE_SET, &CLS_ITSOtherPage::OnBnClickedButtonItsJpegsizeSet)
	ON_BN_CLICKED(IDC_BUTTON_ITS_JPEGQUALITY_SET, &CLS_ITSOtherPage::OnBnClickedButtonItsJpegqualitySet)
	ON_CBN_SELCHANGE(IDC_COMBO_ITS_DEVSTA, &CLS_ITSOtherPage::OnCbnSelchangeComboItsDevsta)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_LINK_SET, &CLS_ITSOtherPage::OnBnClickedBtnLinkSet)
	ON_CBN_SELCHANGE(IDC_CBO_LINE_NO, &CLS_ITSOtherPage::OnCbnSelchangeCboLineNo)
END_MESSAGE_MAP()


// CLS_ITSOtherPage message handlers
BOOL CLS_ITSOtherPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_edtDeviceCode.SetLimitText(64);
	m_edtRoadName.SetLimitText(64);

	m_cboPicChan.AddString("0");
	m_cboPicChan.SetCurSel(0);

	for (int i=0; i<4; i++)
	{
		m_cboCapChan.AddString(IntToCString(i));	
	}
	m_cboCapChan.SetCurSel(0);

	m_cboNetMode.AddString("TCP");
	m_cboNetMode.AddString("UDP");
	m_cboNetMode.AddString("MUC");
	m_cboNetMode.SetCurSel(0);

	for (int i=0; i<4; i++)
	{
		m_cboLineNo.AddString(IntToCString(i));	
	}
	m_cboLineNo.SetCurSel(0);

	UI_UpdateDialog();

	return TRUE;
}

void CLS_ITSOtherPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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
	if (_iStreamNo < 0)
	{
		m_iStreamNo = 0;
	}
	else
	{
		m_iStreamNo = _iStreamNo;
	}
	UI_UpdateOther();
	UpdateEnable();
	UI_UpdateWorkmode();
}

void CLS_ITSOtherPage::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_ITSOtherPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_STATIC_CAMLOCATION,IDS_CONFIG_ITS_CAMLOCATION);
	SetDlgItemTextEx(IDC_STATIC_DEVICECODE,IDS_CONFIG_ITS_DEVICECODE);
	SetDlgItemTextEx(IDC_STATIC_ROADNAME,IDS_CONFIG_ITS_ROADNAME);
	SetDlgItemTextEx(IDC_BUTTON_CAMLOCATION,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_WORKMODE,IDS_CONFIG_LAN_WOKEMODE);
	SetDlgItemTextEx(IDC_STATIC_WORKMODE1,IDS_CONFIG_LAN_WOKEMODE);
	SetDlgItemTextEx(IDC_STATIC_INTERVAL,IDS_CONFIG_ECOP_INTERVAL);
	SetDlgItemTextEx(IDC_BUTTON_WORKMODE,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_STREAM,IDS_CONFIG_ITS_STREAM);
	SetDlgItemTextEx(IDC_STATIC_PICCHAN,IDS_CONFIG_ITS_PICCHAN);
	SetDlgItemTextEx(IDC_STATIC_CAPCHAN,IDS_CONFIG_ITS_CAPCHAN);
	SetDlgItemTextEx(IDC_STATIC_ITSNETMODE,IDS_CONFIG_DNVR_DIGIT_NETMODE);
	SetDlgItemTextEx(IDC_CHECK_PICCHAN,IDS_CONFIG_ITS_CONNPIC);
	SetDlgItemTextEx(IDC_BUTTON_CONNECT,IDS_CONFIG_ITS_CONNECT);
	SetDlgItemTextEx(IDC_BUTTON_DISCONNECT,IDS_CONFIG_ITS_DISCONNECT);
	SetDlgItemTextEx(IDC_STATIC_ILLEGALPARK,IDS_CONFIG_ITS_ILLEGALPARK);
	SetDlgItemTextEx(IDC_STATIC_SET_ITS_ENABLE, IDS_CONFIG_ITS_ENABLE_SETTING);
	SetDlgItemTextEx(IDC_STATIC_SETITS_ENABLE_MODE, IDS_CONFIG_ITS_ENABLE_SETTING);
    SetDlgItemTextEx(IDC_STATIC_SETITS_ENABLE, IDS_CONFIG_ITS_ENABLE);
    SetDlgItemTextEx(IDC_BUTTON_ITS_ENABLE_SET, IDS_SET);

	SetDlgItemTextEx(IDC_STATIC_JPEG, IDS_CONFIG_ITS_OTHER_JPEG);
	SetDlgItemTextEx(IDC_STATIC_SETITS_JPEGWIDTH, IDS_CONFIG_ITS_OTHER_JPEGWIDTH);
	SetDlgItemTextEx(IDC_STATIC_SETITS_JPEGHEIGHT, IDS_CONFIG_ITS_OTHER_JPEGHEIGH);
	SetDlgItemTextEx(IDC_STATIC_SETITS_JPEGQUALITY, IDS_CONFIG_ITS_OTHER_JPEGQUALITY);
	SetDlgItemTextEx(IDC_BUTTON_ITS_JPEGSIZE_SET, IDS_SET);
    SetDlgItemTextEx(IDC_BUTTON_ITS_JPEGQUALITY_SET, IDS_SET);

	SetDlgItemTextEx(IDC_STATIC_DEVSTA, IDS_CONFIG_ITS_OTHER_DEVSTATUS);
	SetDlgItemTextEx(IDC_STATIC_DEVSTA_STA, IDS_CONFIG_ITS_OTHER_DEVSTATUS);
	SetDlgItemTextEx(IDC_STATIC_DEVSTA_TYPE, IDS_CONFIG_ITS_OTHER_STATUSTYPE);

	SetDlgItemTextEx(IDC_CHK_LINK_ENABLE, IDS_ITS_ENABLED);
	SetDlgItemTextEx(IDC_STC_LIAN_NO, IDS_ITS_ROADWAY_ID);
	SetDlgItemTextEx(IDC_STC_CAMERA_IP, IDS_CONFIG_IP_FILTER_IP);
	SetDlgItemTextEx(IDC_STC_CAMERA_PORT, IDS_CONFIG_IP_PORT_WEBPORT);
	SetDlgItemTextEx(IDC_BTN_LINK_SET, IDS_SET);

	UpdateEnable();
}

void CLS_ITSOtherPage::UpdateEnable()
{
	m_cboEnableMode.ResetContent();
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_POWERSYNC)), ITS_UNIVERSAL_EABLE_TYPE_POWER_SYN);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_MANUAL_MODE)), ITS_UNIVERSAL_EABLE_TYPE_MANUAL);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_CRCVERIFY)), ITS_UNIVERSAL_EABLE_TYPE_CRC);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_VIDEOSPEED)), ITS_UNIVERSAL_EABLE_TYPE_VIDEO_SPEED);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_REDLTPLUS)), ITS_UNIVERSAL_EABLE_TYPE_RED_LIGHT_ENHANCE);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_FTP)), ITS_UNIVERSAL_EABLE_TYPE_FTP);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_VIDEOCOMPOUND)), ITS_UNIVERSAL_EABLE_TYPE_COMBINE);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_FACEVERIFY)), ITS_UNIVERSAL_EABLE_TYPE_FACE);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_CONFIG_ITS_CARFLAG)), ITS_UNIVERSAL_EABLE_TYPE_LICENSE_PLATE);
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_BELTCHECK)), ITS_UNIVERSAL_EABLE_TYPE_SAFETY_BELT);																	
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_TYPECHECK)), ITS_UNIVERSAL_EABLE_TYPE_CAR_TYPE);																	
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_COFACE_CHECK)), ITS_UNIVERSAL_EABLE_TYPE_COPILOT_FACE);																
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_PIC_EXPORT)), ITS_UNIVERSAL_EABLE_TYPE_OUTPUT_PICTURE);					
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_CAR_PIC_EXPORT)), ITS_UNIVERSAL_EABLE_TYPE_LICENSE_PLATE_OUTPUT_PICTURE);		
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_MAIN_PIC_EXPORT)), ITS_UNIVERSAL_EABLE_TYPE_DRIVER_FACE_OUTPUT_PICTURE);	
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_COPILOT_PIC_EXPORT)), ITS_UNIVERSAL_EABLE_TYPE_COPILOT_FACE_OUTPUT_PICTURE);	
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_SNAP_SHOT)), ITS_UNIVERSAL_EABLE_TYPE_QUICK_CAPTURE);				
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_CONTINUEATION_CAPTURE)), ITS_UNIVERSAL_EABLE_TYPE_CONTINUEATION_CAPTURE);					
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextEx(IDS_ITS_CALLING)), ITS_UNIVERSAL_EABLE_TYPE_CALLING);							
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextByLan("停车事件","Parking events")), ITS_UNIVERSAL_EABLE_TYPE_PARKING_EVENT);							
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextByLan("逆行事件","Retrograde event")), ITS_UNIVERSAL_EABLE_TYPE_RETROGRANDE_EVENT);							
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextByLan("变道事件","Lane changing events")), ITS_UNIVERSAL_EABLE_TYPE_LANECHANGE_EVENT);							
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextByLan("蛇形事件","Serpentine event")), ITS_UNIVERSAL_EABLE_TYPE_SERPENTINE_EVENT);							
	m_cboEnableMode.SetItemData(m_cboEnableMode.AddString(GetTextByLan("拥堵事件","Congestion events")), ITS_UNIVERSAL_EABLE_TYPE_CONGESTIONEVENT);							

	m_cboEnableMode.SetCurSel(0);

	m_cboEnable.ResetContent();
	m_cboEnable.AddString(GetTextEx(IDS_CONFIG_ITS_NO_ENABLE));
	m_cboEnable.AddString(GetTextEx(IDS_CONFIG_ITS_ENABLE));

	//add string
	m_cboDevSta.ResetContent();
	m_cboDevSta.AddString(GetTextEx(IDS_CONFIG_ITS_OTHER_RIGHTLIGHT));
	m_cboDevSta.SetCurSel(0);

	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	} 
	ITS_EnalbeParam pEnableParam = {0};
	pEnableParam.iType = 0;

	if (0 == NetClient_GetITSExtraInfo( m_iLogonID, ITS_ENABLE_CMD_GET, NULL, &pEnableParam, sizeof(ITS_EnalbeParam)))
	{
		m_cboEnable.SetCurSel(pEnableParam.iEnable);
	}
	else
		m_cboEnable.SetCurSel(-1);
}

BOOL CLS_ITSOtherPage::UI_UpdateOther()
{
	ITS_CamLocation pb = {0};
	int iRet = NetClient_GetITSCamLocation(m_iLogonID, &pb, sizeof(pb));
	if (0 == iRet)
	{
		SetDlgItemText(IDC_EDIT_DEVICECODE, pb.cDeviceVode);
		SetDlgItemText(IDC_EDIT_ROADNAME, pb.cRoadName);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSDayNight (%d,%d)",m_iLogonID,sizeof(pb));
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetITSDayNight (%d,%d)",m_iLogonID,sizeof(pb));
	}
	ITS_CamWorkMode pc = {0};
	iRet = NetClient_GetITSWorkMode(m_iLogonID, &pc, sizeof(pc));
	if (0 == iRet)
	{
		SetDlgItemInt(IDC_EDIT__WORKMODE, pc.iWorkMode);
		SetDlgItemInt(IDC_EDIT_INTERVAL, pc.iInterval);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSWorkMode (%d,%d)",m_iLogonID,sizeof(pc));
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetITSWorkMode (%d,%d)",m_iLogonID,sizeof(pc));
	}

	int iJpegWidth = 0;
	int iJpegHeigh = 0;
	iRet = NetClient_GetJEPGSize(m_iLogonID, m_iChannelNo, &iJpegWidth, &iJpegHeigh);
	if (0 == iRet)
	{
		SetDlgItemInt(IDC_EDIT_JPEGWIDTH, iJpegWidth);
		SetDlgItemInt(IDC_EDIT_JPEGHEIGH, iJpegHeigh);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetJEPGSize (%d,%d,%d)",m_iLogonID,iJpegWidth, iJpegHeigh);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetJEPGSize (%d,%d,%d)",m_iLogonID,iJpegWidth, iJpegHeigh);
	}
	int iJpegQuality = 0;
	iRet = NetClient_GetJPEGQuality(m_iLogonID, &iJpegQuality);
	if (0 == iRet)
	{
		SetDlgItemInt(IDC_EDIT_JPEGQUALITY, iJpegQuality);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetJPEGQuality (%d,%d)",m_iLogonID,iJpegQuality);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetJPEGQuality (%d,%d)",m_iLogonID,iJpegQuality);
	}
	STR_DevStatus tDevSta = {0};
	int iStrSize = sizeof(STR_DevStatus);
	iRet = NetClient_GetDevStatus(m_iLogonID, &tDevSta, iStrSize);
	if (0 == iRet)
	{
		m_cboDevSta.SetCurSel(tDevSta.iType);
		char cStrTem[8][20] = {"", "", "", ""};
		char cChinese[4][20] = {"Green", "Red", "Yellow", "Unknown"};
		for (int i = 0; i < 32; i += 4)
		{
			int iSer = 0;
			int iIndex = (tDevSta.iStatus[i]>>i)&0xF;
			if (iIndex < 0 || iIndex >=8)
			{
				continue;
			}
			strcpy(cStrTem[iSer], cChinese[iIndex]);	
			iSer += 1; 
		}
		CString cstrSta;
		cstrSta.Format("1:%s,2:%s,3:%s,4:%s,5:%s,6:%s,7:%s,8:%s", cStrTem[0], cStrTem[1], cStrTem[2], cStrTem[3], cStrTem[4], cStrTem[5], cStrTem[6], cStrTem[7]);
		SetDlgItemText(IDC_EDIT_DEVSTA, cstrSta);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_QueryDevStatus (%d,%d)",m_iLogonID,tDevSta.iType);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_QueryDevStatus (%d,%d)",m_iLogonID,tDevSta.iType);
	}
	return TRUE;
}

void CLS_ITSOtherPage::OnBnClickedButtonCamlocation()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	ITS_CamLocation pa = {0};
	GetDlgItemText(IDC_EDIT_DEVICECODE, pa.cDeviceVode, 64);
	GetDlgItemText(IDC_EDIT_ROADNAME, pa.cRoadName, 64);
	int iRet = NetClient_SetITSCamLocation(m_iLogonID, &pa, sizeof(pa));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSCamLocation (%d,%d)",m_iLogonID,sizeof(pa));
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSCamLocation (%d,%d)",m_iLogonID,sizeof(pa));
	}
}

void CLS_ITSOtherPage::OnBnClickedButtonWorkmode()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	ITS_CamWorkMode pa = {0};
	pa.iWorkMode = GetDlgItemInt(IDC_EDIT__WORKMODE);
	pa.iInterval = GetDlgItemInt(IDC_EDIT_INTERVAL);
	int iRet = NetClient_SetITSWorkMode(m_iLogonID, &pa, sizeof(pa));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSWorkMode (%d,%d)",m_iLogonID,sizeof(pa));
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSWorkMode (%d,%d)",m_iLogonID,sizeof(pa));
	}
}

void CLS_ITSOtherPage::UI_UpdateWorkmode()
{
	if(m_iLogonID < 0)
		return ;

	ITS_CamWorkMode pa = {0};
	int iRet = NetClient_GetITSWorkMode(m_iLogonID, &pa, sizeof(pa));
	if(iRet == 0)
	{
		SetDlgItemInt(IDC_EDIT__WORKMODE, pa.iWorkMode);
		SetDlgItemInt(IDC_EDIT_INTERVAL, pa.iInterval);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSWorkMode(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSWorkMode(%d), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_ITSOtherPage::UI_UpdateLinkCamera()
{
	if(m_iLogonID < 0)
		return ;

	ITS_LinkPanoPamaCap tParam = {0};
	tParam.iSize = sizeof(tParam);
	tParam.iChannelNo = m_cboLineNo.GetCurSel();
	int iRet = NetClient_GetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_LINKPANORAMACAP, tParam.iChannelNo, &tParam, sizeof(tParam));
	if(iRet == 0)
	{
		m_chkLinkEnable.SetCheck(tParam.iEnable);
		SetDlgItemText(IDC_EDT_CAMERA_IP, tParam.cLinkCameraIP);
		SetDlgItemInt(IDC_EDT_CAMERA_PORT, tParam.iLinkCameraPort);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSExtraInfo(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSExtraInfo(%d), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_ITSOtherPage::OnBnClickedButtonConnect()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int iChannelIndex = -1;
	int iRet = -1;
// 	int iChannelNum;
// 	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);        //@120530 new connect protocol
	bool bChannelInUse = (P_ulPicID[m_cboPicChan.GetCurSel()] != (unsigned int)-1);
	if(bChannelInUse)
	{
		AddLog(LOG_TYPE_MSG,"","Current Pic stream(%d) is in used",m_cboPicChan.GetCurSel());
		return ;
	}
	if (m_chkConnPic.GetCheck() == BST_CHECKED)
	{
		CreateDirectory("D:\\Demo_PicStream\\", NULL); //Create a storage folder
		iChannelIndex = m_cboPicChan.GetCurSel(); //Stream No = 4, chan No = 16 is the PIC channel
		CLIENTINFO cltInfo = {0};
		cltInfo.m_iChannelNo = iChannelIndex;
		memcpy(cltInfo.m_cRemoteIP, FindDevice(m_iLogonID)->cIP, 16);
		int iModeIndex = m_cboNetMode.GetCurSel()+1;
		cltInfo.m_iTimeout = 2000;
		cltInfo.m_iTTL = 8;
		cltInfo.m_iBufferCount = 0;
		cltInfo.m_iDelayNum = 0;
		int iVideoTypeIndex = CHANNEL_PICTURE; //cb1205 picture stream mode
		cltInfo.m_iServerID = m_iLogonID;
		//cltInfo.m_iChannelNo = m_iChannelNo;
		cltInfo.m_iStreamNO = iVideoTypeIndex;
		iRet = NetClient_StartRecvEx((unsigned int*)&P_ulPicID[m_cboPicChan.GetCurSel()], &cltInfo, cbkRecvDataEx, (void*)0xF000);
		if (0 == iRet)
		{
			unsigned int uiConnID = P_ulPicID[m_cboPicChan.GetCurSel()];
			iRet = NetClient_StartCaptureData(uiConnID);
			if (0 == iRet)
			{
				AddLog(LOG_TYPE_SUCC,"","NetClient_StartCaptureData (%d)",uiConnID);
			} 
			else
			{
				AddLog(LOG_TYPE_FAIL,"","NetClient_StartCaptureData (%d)",uiConnID);
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_StartRecvEx (0xF000)");
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StartRecvEx (0xF000)");
		}
	} 
	else
	{
		iChannelIndex = m_cboCapChan.GetCurSel(); //Stream No = 3, chan No = [24,32) is the analog capture channel
		CLIENTINFO cltInfo = {0};
		cltInfo.m_iChannelNo = iChannelIndex;
		memcpy(cltInfo.m_cRemoteIP, FindDevice(m_iLogonID)->cIP, 16);
		int iModeIndex = m_cboNetMode.GetCurSel()+1;
		cltInfo.m_iTimeout = 2000;
		cltInfo.m_iTTL = 8;
		cltInfo.m_iBufferCount = 0;
		cltInfo.m_iDelayNum = 0;
		int iVideoTypeIndex = 5;              //simulate grab
		cltInfo.m_iServerID = m_iLogonID;
		//cltInfo.m_iChannelNo = m_iChannelNo;
		cltInfo.m_iStreamNO = iVideoTypeIndex;
		iRet = NetClient_StartRecvEx((unsigned int*)&P_ulCapID[m_cboCapChan.GetCurSel()], &cltInfo, cbkRecvDataEx, (void*)0xF001);
		if (0 == iRet)
		{
			unsigned int uiConnID = P_ulCapID[m_cboCapChan.GetCurSel()];
			iRet = NetClient_StartCaptureData(uiConnID);
			if (0 == iRet)
			{
				AddLog(LOG_TYPE_SUCC,"","NetClient_StartCaptureData (%d)",uiConnID);
			} 
			else
			{
				AddLog(LOG_TYPE_FAIL,"","NetClient_StartCaptureData (%d)",uiConnID);
			}
			AddLog(LOG_TYPE_SUCC,"","NetClient_StartRecvEx (0xF001)");
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StartRecvEx (0xF001)");
		}
	}
}
void CLS_ITSOtherPage::OnBnClickedButtonDisconnect()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if (m_chkConnPic.GetCheck() == BST_CHECKED)
	{
		unsigned int uiConnID = P_ulPicID[m_cboPicChan.GetCurSel()];
		int iRet = NetClient_StopCaptureData(uiConnID);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_StopCaptureData (%d)", uiConnID);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StopCaptureData (%d)", uiConnID);
		}
		iRet = NetClient_StopRecv(uiConnID);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecv (%d)", uiConnID);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StopRecv (%d)", uiConnID);
		}
		P_ulPicID[m_cboPicChan.GetCurSel()] = (unsigned int)-1;
	}
	else
	{
		unsigned int uiConnID = P_ulCapID[m_cboCapChan.GetCurSel()];
		int iRet = NetClient_StopCaptureData(uiConnID);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_StopCaptureData (%d)", uiConnID);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StopCaptureData (%d)", uiConnID);
		}
		iRet = NetClient_StopRecv(uiConnID);
		if (0 == iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_StopRecv (%d)", uiConnID);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_StopRecv (%d)", uiConnID);
		}
		P_ulPicID[m_cboCapChan.GetCurSel()] = (unsigned int)-1;
	}
}

static void __stdcall cbkRecvDataEx(unsigned long _ulID,unsigned char* _ucData,int _iLen, int _iFlag, void* _lpUserData)
{
	//Now each picture is a structure at the beginning, and now FEOF is sent at the end of the picture, and will not be separated, and sent uniformly through the video channel. The last package will not be divided into how many are left over
	//Enter this callback function once, indicating that a data packet with a length of _iLen is received
	//Enter this callback function one or more times before receiving a complete frame of data (including: structure size bytes + 32 bytes data frame header + (picture 1 + picture 2...) + (4 bytes) =FEOF) + another 4 bytes);
	//Therefore, it is necessary to do a good job of combining the data sent each time and extracting the required information.
	static bool blNewPicStream = true;
	static int g_iPicCount = 0;       //Indicates the serial number of the picture captured each time
	static int g_iCaptureNum = 0;     //represents the number of snapshots
	static int g_iPicNum  = 0;        //The number of pictures contained in the corresponding data in this picture header
	static int g_iCarSerialNum = 0;
	static int g_iPictureNum = 0;
	static unsigned char ucPacketHeader[2 * sizeof(STRCT_Record)] = {0};
	static FILE* fpPicStream = NULL;
	static STRCT_Record* pHeader = NULL;
	static int iNeedDataLen = sizeof(STRCT_Record) + 32;
	static char *pcVehicleColor[33][5] = {"white","orange-red","orange","light yellow","light blue","light green","light cyan","light purple","pink","black","red","yellow-green","yellow","grey","gold","blue-grey",
		"Blue","Green Grey","Green","Beige","Cyan","Cyan","Flesh","Crimson","Dark Blue","Sky Blue","Silver Grey","Magenta" ,"purple","brown red","brown yellow","brown"};
	static char *pcLPRPlateColor[7] = {"PC_UNKNOWN", "PC_BLUE", "PC_YELLOW", "PC_WHITE", "PC_BLACK", "PC_GREEN"};
	static char *pcLPRPlateType[16] = 
	{
		"PT_UNKOWN"		    //= 0,	// unknown
		"PT_CAR_NORMAL"	    //= 1,	// ordinary car, private car, blue, black
		"PT_BIG_NORMAL"	    //= 2,	// general cart, truck, yellow
		"PT_POLICE"		    //= 4,  // Police car (separator between first and second character), white
		"PT_WJ"		        //= 8,	// Armed Police Vehicles
		"PT_HK"			    //= 16,	// Hong Kong and Macau vehicles
		"PT_DOUBLELINE"	    //= 32,	// Double yellow card
		"PT_MILITARY"	    //= 64,	// military card
		"PT_AMBASSADOR"	    //= 128,	// Embassy vehicle
		"PT_POLICE_TYPE2"	//= 512,    // Second class police car (delimiter between second and third character), white
		"PT_XUE"			//= 1024,	// "Learn" license plate
		"PT_MOTOR"		    //= 2048,	// motorcycle
		"PT_LIAOTEMP"		//= 4096,   // Lin character license plate
		"PT_GREEN"		    //= 8192,	// double deck green card, agricultural vehicle
		"PT_GUA"			//= 16384	// "hang" license plate
	};

	if(0xF000 == (int)_lpUserData)
	{
		//This if implementation copies the _ucData passed by the callback into ucPacketHeader
		//Then convert it into the image information STR_PacketHeader structure
		if(blNewPicStream)
		{
			//Read the data of the structure part
			if(_iLen >= iNeedDataLen)
			{
				//Copy the data belonging to the structure in the _ucData data to the ucPacketHeader array
				memcpy(ucPacketHeader + (sizeof(STRCT_Record)+32 - iNeedDataLen), _ucData, iNeedDataLen);
				pHeader = (STRCT_Record*)(ucPacketHeader + 32);

				//get the number of pictures
				g_iPicNum = pHeader->m_iCaptureInfoNum;
				g_iCarSerialNum = pHeader->m_iCarSerialNum;
				g_iPictureNum = pHeader->m_iPictureNum;
				//display license plate color body color video file name
				int  iVehicleColorNumber = pHeader->m_iCarColor;
				CString strVehicleColor = pcVehicleColor[iVehicleColorNumber][0];
				int  iPlateColorNumber = pHeader->m_iPlateColor;
				CString strPlateColor = pcLPRPlateColor[iPlateColorNumber];
				int  iPlateTypeIndex = pHeader->m_iPlateColor;
				if(iPlateTypeIndex != 0)
				{
					iPlateTypeIndex = (int)log10((double)iPlateTypeIndex);
				}
				CString strPlateType = pcLPRPlateType[iPlateTypeIndex];
				AddLog(LOG_TYPE_MSG,"","VehicleColor:%s, iPlateColor:%s, iPlateType:%s",(LPSTR)(LPCSTR)strVehicleColor,(LPSTR)(LPCSTR)strPlateColor,(LPSTR)(LPCSTR)strPlateType);
				// Extract the data of the structure part to complete
				blNewPicStream = false;
				iNeedDataLen = iNeedDataLen - _iLen; //This should be 0 or a negative number, indicating the excess data beyond the structure data
			}
			else
			{
				//Copy all data of _ucData data to ucPacketHeader array
				memcpy(ucPacketHeader + (sizeof(STRCT_Record)+32 - iNeedDataLen), _ucData, _iLen);
				iNeedDataLen = iNeedDataLen - _iLen;
			}
			AddLog(LOG_TYPE_MSG,"","Pic Header, NeedLen = %d, recv len = %d",iNeedDataLen,_iLen);
		}

		//Save the image according to the structure information
		if(false == blNewPicStream)
		{
			// newly received data
			if(iNeedDataLen >= 0)
			{
				// Determine whether it is the last packet of a frame of data
				char csEnd[5] = {0};
				memcpy(csEnd, _ucData+(_iLen-8), 4);
				if(strcmp(csEnd,"FEOF") == 0)
				{
 					if(NULL != fpPicStream && _iLen >= 8)
					{
						fwrite(_ucData, _iLen - 8, 1, fpPicStream);
					}
					g_iPicCount = 0;
					g_iCaptureNum++;
					g_iPicNum = 0;

					blNewPicStream = true;
					iNeedDataLen = sizeof(STRCT_Record) + 32;
					memset(ucPacketHeader, 0 , 2 * sizeof(STRCT_Record));
					AddLog(LOG_TYPE_MSG,"","fpPicStream111 is %p",fpPicStream);
					if (fpPicStream != NULL)
					{
						fflush(fpPicStream);
						fclose(fpPicStream);
						fpPicStream = NULL;
					}
					AddLog(LOG_TYPE_MSG,"","Recv FEOF, recv len = %d",_iLen);

					return;
				}
				//Write to file for data that is not the last packet of data
				if(iNeedDataLen >= _iLen)               //add "=" cb@120530
				{
					if(NULL != fpPicStream && _iLen >= 0)
					{
						fwrite(_ucData, _iLen, 1, fpPicStream);
					}
				}
				else //This should be deleted cb@120531 will not be subcontracted now
				{
					//No FEOF was found at this time, but it is already the end of the second picture, maybe FEOF(int) has been subcontracted
					if(NULL != fpPicStream && iNeedDataLen >= 0)
					{
						fwrite(_ucData, iNeedDataLen, 1, fpPicStream);
					}
				}

				//The following iNeedDataLen is a positive number, which means that the iNeedDataLen long data is needed to get a complete picture
				//The iNeedDataLen below is 0, which means that the data of a complete picture is just obtained, and a new picture name is created in the following while
				//The iNeedDataLen below is a negative number, which means that a complete picture has been completed, and the data of -iNeedDataLen is left. Create a new picture name in the following while, and save the remaining data
				iNeedDataLen = iNeedDataLen - _iLen;
			}
			//trace_r("Pic Data, NeedLen = %d, recv len = %d\n", iNeedDataLen, _iLen);

			// Process the previously unprocessed image data
			while(iNeedDataLen <= 0) //cb <=0 to == 120602 -1116 0
			{
				AddLog(LOG_TYPE_MSG,"","fpPicStream222 is %p",fpPicStream);
				if(fpPicStream)
				{
					AddLog(LOG_TYPE_MSG,"","fpPicStream333 is %p",fpPicStream);
					fflush(fpPicStream);
					fclose(fpPicStream);
					fpPicStream = NULL;
				}

				if(g_iPicCount < g_iPicNum)
				{
					if(NULL == fpPicStream)
					{
						char cDirStore[256] = {0};
						CString strDirStore;
						strDirStore.Format("D:\\Demo_PicStream\\PicStream_ITS%d_%d.jpg",g_iCarSerialNum, g_iPictureNum);
						fpPicStream = fopen((LPSTR)(LPCSTR)strDirStore, "wb");
						if(NULL == fpPicStream)
							return;
					}

					if((0 - iNeedDataLen) > pHeader->m_iCaptureLen[g_iPicCount])
					{
						//Normally should not execute here
						if(NULL != fpPicStream && pHeader->m_iCaptureLen[g_iPicCount] >= 0)
						{
							//fwrite(_ucData + _iLen + iNeedDataLen , pHeader->m_iCaptureLen[g_iPicCount], 1, fpPicStream);
						}
					}
					else
					{
						if(NULL != fpPicStream && iNeedDataLen <= 0)
						{
							fwrite(_ucData + _iLen + iNeedDataLen , (0-iNeedDataLen), 1, fpPicStream);
						}
					}
					//new protocol once only send one picture, iNeedDataLen now retur a Plus quantity .
					iNeedDataLen = pHeader->m_iCaptureLen[g_iPicCount] + iNeedDataLen;
					g_iPicCount++;
				}
				else
					break;
			}
		}
	}
	else if(0xF001 == (int)_lpUserData)
		AddLog(LOG_TYPE_MSG,"","fCap channel data...len(%d)");
#ifndef PLAY_CALLBACK_STREAM
	return;
#else
	if (g_iRealStreamPlayer >= 0)
	{
		TFmPlayer::TC_PutStreamToPlayer(g_iRealStreamPlayer, _ucData, _iLen);
	}
#endif
}


void CLS_ITSOtherPage::OnBnClickedButtonItsEnableSet()
{
	ITS_EnalbeParam pEnableParam = {0};
	pEnableParam.iType = (int)m_cboEnableMode.GetItemData(m_cboEnableMode.GetCurSel());
	pEnableParam.iEnable = m_cboEnable.GetCurSel();
	if (0 == NetClient_SetITSExtraInfo( m_iLogonID, ITS_ENABLE_CMD_SET, NULL, &pEnableParam, sizeof(ITS_EnalbeParam)))
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetITSExtraInfo(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetITSExtraInfo(%d),error no:(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_ITSOtherPage::OnCbnSelchangeComboItsEnableMode()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	} 
	ITS_EnalbeParam pEnableParam = {0};
	//pEnableParam.iType = MAX_ITS_ITSMANUALENABLE_NUM;
	pEnableParam.iType = (int)m_cboEnableMode.GetItemData(m_cboEnableMode.GetCurSel());

	if (0 == NetClient_GetITSExtraInfo( m_iLogonID, ITS_ENABLE_CMD_SET, NULL, &pEnableParam, sizeof(ITS_EnalbeParam)))
	{
		m_cboEnable.SetCurSel(pEnableParam.iEnable);
	}
	else
		m_cboEnable.SetCurSel(-1);
}

void CLS_ITSOtherPage::OnBnClickedButtonItsJpegsizeSet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid logon id(%d)", m_iLogonID);
	}

	int iJpegWidth = GetDlgItemInt(IDC_EDIT_JPEGWIDTH);
	int iJpegHeigh = GetDlgItemInt(IDC_EDIT_JPEGHEIGH);
	int iRet = NetClient_SetJEPGSize(m_iLogonID, m_iChannelNo, iJpegWidth, iJpegHeigh);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetJEPGSize (%d,%d,%d)",m_iLogonID,iJpegWidth, iJpegHeigh);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetJEPGSize (%d,%d,%d)",m_iLogonID,iJpegWidth, iJpegHeigh);
	}

}

void CLS_ITSOtherPage::OnBnClickedButtonItsJpegqualitySet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid logon id(%d)", m_iLogonID);
	}

	int iJpegQuality = GetDlgItemInt(IDC_EDIT_JPEGQUALITY);
	int iRet = NetClient_SetJPEGQuality(m_iLogonID, iJpegQuality);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetJPEGQuality (%d,%d)",m_iLogonID,iJpegQuality);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetJPEGQuality (%d,%d)",m_iLogonID,iJpegQuality);
	}
}

void CLS_ITSOtherPage::OnCbnSelchangeComboItsDevsta()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid logon id(%d)", m_iLogonID);
	}

	STR_DevStatus tDevSta = {0};
	int iStrSize = sizeof(STR_DevStatus);
	int iRet = NetClient_GetDevStatus(m_iLogonID, &tDevSta, iStrSize);
	if (0 == iRet)
	{
		m_cboDevSta.SetCurSel(tDevSta.iType);
		char cStrTem[8][5] = {"", "", "", ""};
		char cChinese[4][5] = {"GN", "RD", "YE", "UNK"};
		for (int i = 0; i < 32; i += 4)
		{
			int iSer = 0;
			int iIndex = (tDevSta.iStatus[i]>>i)&0xF;
			if (iIndex < 0 || iIndex >=8)
			{
				continue;
			}
			strcpy(cStrTem[iSer], cChinese[iIndex]);	
			iSer += 1; 
		}
		CString cstrSta;
		cstrSta.Format("1:%s,2:%s,3:%s,4:%s,5:%s,6:%s,7:%s,8:%s", cStrTem[0], cStrTem[1], cStrTem[2], cStrTem[3], cStrTem[4], cStrTem[5], cStrTem[6], cStrTem[7]);
		SetDlgItemText(IDC_EDIT_DEVSTA, cstrSta);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_QueryDevStatus (%d,%d)",m_iLogonID,tDevSta.iType);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_QueryDevStatus (%d,%d)",m_iLogonID,tDevSta.iType);
	}
}

void CLS_ITSOtherPage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UI_UpdateLinkCamera();
	}
}

void CLS_ITSOtherPage::OnBnClickedBtnLinkSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	} 

	ITS_LinkPanoPamaCap tParam = {0};
	tParam.iSize = sizeof(tParam);
	tParam.iChannelNo = m_cboLineNo.GetCurSel();
	tParam.iEnable = m_chkLinkEnable.GetCheck();
	CString cstrIP;
	GetDlgItemText(IDC_EDT_CAMERA_IP, cstrIP);
	strcpy_s(tParam.cLinkCameraIP, LEN_16, cstrIP);
	tParam.iLinkCameraPort = GetDlgItemInt(IDC_EDT_CAMERA_PORT);	
	int iRet = NetClient_SetITSExtraInfo(m_iLogonID, ITS_EXTRAINFO_CMD_LINKPANORAMACAP,tParam.iChannelNo,  &tParam, sizeof(tParam));
	if(iRet == 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSExtraInfo(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSExtraInfo(%d)",m_iLogonID);
	}
}

void CLS_ITSOtherPage::OnCbnSelchangeCboLineNo()
{
	UI_UpdateLinkCamera();
}
