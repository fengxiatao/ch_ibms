// Config\PTZPage.cpp : implementation file
//

#include "stdafx.h"
#include "PTZPage.h"


// CLS_PTZPage dialog

IMPLEMENT_DYNAMIC(CLS_PTZPage, CDialog)

CLS_PTZPage::CLS_PTZPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_PTZPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CLS_PTZPage::~CLS_PTZPage()
{
}

void CLS_PTZPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_PROTOCOL_NAME, m_cboProtocolName);
	DDX_Control(pDX, IDC_COMBO_COM_NO, m_cboComNo);
	DDX_Control(pDX, IDC_SPIN_ADDRESS, m_spinAddress);
	DDX_Control(pDX, IDC_COMBO_CHN_BAUD_RATE, m_cboBaudRate);
	DDX_Control(pDX, IDC_COMBO_CHN_PARITY_BIT, m_cboParityBit);
	DDX_Control(pDX, IDC_COMBO_CHN_DATA_BIT, m_cboDataBit);
	DDX_Control(pDX, IDC_COMBO_CHN_STOP_BIT, m_cboStopBit);
	DDX_Control(pDX, IDC_CHECK_ENABLE, m_chkEnable);
	DDX_Control(pDX, IDC_EDIT_PRESETINDEX, m_edtPresetIndex);
	DDX_Control(pDX, IDC_EDIT_IDLETIME, m_edtIdleTime);
	DDX_Control(pDX, IDC_BUTTON_AUTOBACK, m_btnAutoBack);
	DDX_Control(pDX, IDC_SPIN_PRESETINDEX, m_spinPreset);
	DDX_Control(pDX, IDC_SPIN_IDLETIME, m_spinIdleTime);
	DDX_Control(pDX, IDC_COMBO_SET_PTZ_TYPE, m_choSetPTZType);
	DDX_Control(pDX, IDC_COMBO_TEMPLEID_SET, m_choSetTempleID);
	DDX_Control(pDX, IDC_COMBO_TEMPLEID_GET, m_choGetTempleID);
	DDX_Control(pDX, IDC_COMBO_COMMAND_TYPE, m_choSetDevControl);
		
}


BEGIN_MESSAGE_MAP(CLS_PTZPage,CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_DEVICE_TYPE, &CLS_PTZPage::OnBnClickedButtonDeviceType)
	ON_BN_CLICKED(IDC_BUTTON_CHN_PTZ_FORMAT, &CLS_PTZPage::OnBnClickedButtonPtzFormat)
	ON_BN_CLICKED(IDC_BUTTON_AUTOBACK, &CLS_PTZPage::OnBnClickedButtonAutoback)
	ON_BN_CLICKED(IDC_CHECK_CHNPTZ, &CLS_PTZPage::OnBnClickedChnptzEnable)
	ON_BN_CLICKED(IDC_BUTTON_SET_PTZ_SET, &CLS_PTZPage::OnBnClickedButtonSetPtzSet)
	ON_BN_CLICKED(IDC_BUTTON_GET_PTZ_GET, &CLS_PTZPage::OnBnClickedButtonGetPtzGet)
	ON_BN_CLICKED(IDC_BUTTON_LENS_SET, &CLS_PTZPage::OnBnClickedButtonLensSet)
	ON_BN_CLICKED(IDC_BUTTON_LENS_GET, &CLS_PTZPage::OnBnClickedButtonLensGet)
	ON_BN_CLICKED(IDC_BUTTON_APERTURE_ON, &CLS_PTZPage::OnBnClickedButtonApertureOn)
	ON_BN_CLICKED(IDC_BUTTON_APERTURE_OFF, &CLS_PTZPage::OnBnClickedButtonApertureOff)
	ON_BN_CLICKED(IDC_BUTTON_APERTURE_GET, &CLS_PTZPage::OnBnClickedButtonApertureGet)
	ON_BN_CLICKED(IDC_BUTTON_DEV_SET, &CLS_PTZPage::OnBnClickedButtonDevSet)
END_MESSAGE_MAP()


// CLS_PTZPage message handlers

BOOL CLS_PTZPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();

	m_cboBaudRate.AddString(_T("50"));
	m_cboBaudRate.AddString(_T("75"));
	m_cboBaudRate.AddString(_T("110"));
	m_cboBaudRate.AddString(_T("150"));
	m_cboBaudRate.AddString(_T("300"));
	m_cboBaudRate.AddString(_T("600"));
	m_cboBaudRate.AddString(_T("1200"));
	m_cboBaudRate.AddString(_T("2400"));
	m_cboBaudRate.AddString(_T("4800"));
	m_cboBaudRate.AddString(_T("9600"));
	m_cboBaudRate.AddString(_T("19200"));
	m_cboBaudRate.AddString(_T("38400"));
	m_cboBaudRate.AddString(_T("57600"));
	m_cboBaudRate.AddString(_T("76800"));
	m_cboBaudRate.AddString(_T("115200"));
	m_cboBaudRate.SetCurSel(9);

	m_cboParityBit.SetItemData(0,'n');
	m_cboParityBit.SetItemData(1,'o');
	m_cboParityBit.SetItemData(2,'e');
	m_cboParityBit.SetCurSel(0);

	m_cboDataBit.ResetContent();
	for (int i = 4; i < 9; ++i)
	{
		m_cboDataBit.AddString(IntToString(i));
	}
	m_cboDataBit.SetCurSel(4);

	m_cboStopBit.AddString(IntToString(1));
	m_cboStopBit.AddString(IntToString(2));
	m_cboStopBit.SetCurSel(0);

	CEdit* pEdit = (CEdit*)GetDlgItem(IDC_EDIT_ADDRESS);
	pEdit->SetLimitText(3);
	m_spinAddress.SetRange(0,256);
	m_spinAddress.SetBuddy(pEdit);
	
	m_edtPresetIndex.SetLimitText(3);
	m_spinPreset.SetRange(1,256);
	m_spinPreset.SetBuddy(&m_edtPresetIndex);

	m_edtIdleTime.SetLimitText(5);
	m_spinIdleTime.SetRange32(10,65535);
	m_spinIdleTime.SetBuddy(&m_edtIdleTime);

	
	
	return TRUE;  
}

void CLS_PTZPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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
	
	UI_UpdateWindow();
}

void CLS_PTZPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
}

void CLS_PTZPage::UI_UpdateDialog()
{
	InsertString(m_cboParityBit,0,IDS_CFG_COM_PARITY_NONE);
	InsertString(m_cboParityBit,1,IDS_CFG_COM_PARITY_ODD);
	InsertString(m_cboParityBit,2,IDS_CFG_COM_PARITY_EVEN);

	SetDlgItemTextEx(IDC_STATIC_PROTOCOL_NAME,IDS_CFG_PTZ_PROTOCOL_NAME);
	SetDlgItemTextEx(IDC_STATIC_COM_NO,IDS_CFG_PTZ_COM_NO);
	SetDlgItemTextEx(IDC_STATIC_ADDRESS,IDS_CFG_PTZ_ADDRESS);
	SetDlgItemTextEx(IDC_BUTTON_DEVICE_TYPE,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_CHN_BAUD_RATE,IDS_CFG_COM_BAUD_RATE);
	SetDlgItemTextEx(IDC_STATIC_CHN_PARITY_BIT,IDS_CFG_COM_PARITY_BIT);
	SetDlgItemTextEx(IDC_STATIC_CHN_DATA_BIT,IDS_CFG_COM_DATA_BIT);
	SetDlgItemTextEx(IDC_STATIC_CHN_STOP_BIT,IDS_CFG_COM_STOP_BIT);
	SetDlgItemTextEx(IDC_STATIC_APERTURE_CONTROL,IDS_STATIC_APERTURE_CONTROL);
	SetDlgItemTextEx(IDC_BUTTON_CHN_PTZ_FORMAT,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_AUTOBACK,IDS_CONFIG_PTZ_AUTOBACK);
	SetDlgItemTextEx(IDC_CHECK_ENABLE,IDS_CONFIG_ECOP_RECOPARAM_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_PRESETINDEX,IDS_CONFIG_PTZ_PRESETINDEX);
	SetDlgItemTextEx(IDC_STATIC_IDLETIME,IDS_CONFIG_PTZ_IDLETIME);
	SetDlgItemTextEx(IDC_BUTTON_AUTOBACK,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_DEVICETYPE,IDS_CONFIG_PTZ_DEVICETYPE);
	SetDlgItemTextEx(IDC_STATIC_PTZ_FORMAT,IDS_CONFIG_PTZ_FORMAT);
	SetDlgItemTextEx(IDC_STATIC_LENS_CONTROL,IDS_STATIC_LENS_CONTROL);
	SetDlgItemTextEx(IDC_CHECK_CHNPTZ,IDS_CONFIG_ECOP_RECOPARAM_ENABLE);
	
	SetDlgItemText(IDC_STATIC_PTZ_ABS_COORDINATE, GetTextByLan(("PTZ绝对坐标"), ("PTZAbsCoordinate")));
	SetDlgItemText(IDC_STATIC_DEV_CONTROL, GetTextByLan(("命令类型"), ("Command Type")));
	SetDlgItemText(IDC_STATIC_COMMAND_TYPE, GetTextByLan(("设备控制"), ("Device Control")));
	SetDlgItemText(IDC_STATIC_PARAM1, GetTextByLan(("参数1"), ("Param1")));
	SetDlgItemText(IDC_STATIC_PARAM2, GetTextByLan(("参数2"), ("Param2")));
	SetDlgItemText(IDC_STATIC_CONTROL_TYPE, GetTextByLan(("控制类型"), ("Control Type")));
	SetDlgItemText(IDC_BUTTON_DEV_SET, GetTextByLan(("设置"), ("set")));

	m_choSetPTZType.ResetContent();
	m_choSetPTZType.InsertString(0, GetTextByLan(("速度控制"), ("ControlSpeed")));
	m_choSetPTZType.InsertString(1, GetTextByLan(("位置控制"), ("ControlPostion")));
	m_choSetPTZType.InsertString(2, GetTextByLan(("相对位置控制"), ("ControlRelativePostion")));
	m_choSetPTZType.SetCurSel(0);

	SetDlgItemText(IDC_BUTTON_SET_PTZ_SET, GetTextByLan(("设置"), ("Set")));											  
	SetDlgItemText(IDC_BUTTON_GET_PTZ_GET, GetTextByLan(("获取"), ("Get")));	
	SetDlgItemText(IDC_BUTTON_LENS_SET, GetTextByLan(("设置"), ("Set")));
	SetDlgItemText(IDC_BUTTON_LENS_GET, GetTextByLan(("获取"), ("Get")));
	SetDlgItemText(IDC_BUTTON_APERTURE_ON, GetTextByLan(("打开"), ("ON")));
	SetDlgItemText(IDC_BUTTON_APERTURE_OFF, GetTextByLan(("关闭"), ("OFF")));

	SetDlgItemText(IDC_BUTTON_APERTURE_GET, GetTextByLan(("获取"), ("Get")));
	SetDlgItemText(IDC_STATIC_APERTURE_STATE_GET, GetTextByLan(("获取手动光圈状态"), ("Get mannual Aperture State")));
	
	m_choSetTempleID.ResetContent();
	m_choSetTempleID.InsertString(0,GetTextByLan(("模板0"), ("Template0")));
	m_choSetTempleID.InsertString(1,GetTextByLan(("模板1"), ("Template1")));
	m_choSetTempleID.InsertString(2,GetTextByLan(("模板2"), ("Template2")));
	m_choSetTempleID.InsertString(3,GetTextByLan(("模板3"), ("Template3")));
	m_choSetTempleID.InsertString(4,GetTextByLan(("模板4"), ("Template4")));
	m_choSetTempleID.InsertString(5,GetTextByLan(("模板5"), ("Template5")));
	m_choSetTempleID.InsertString(6,GetTextByLan(("模板6"), ("Template6")));
	m_choSetTempleID.InsertString(7,GetTextByLan(("模板7"), ("Template7")));
	m_choSetTempleID.InsertString(8,GetTextByLan(("即时生效"), ("With immediate effect")));
	m_choSetTempleID.SetCurSel(0);

	m_choGetTempleID.ResetContent();
	m_choGetTempleID.InsertString(0,GetTextByLan(("模板0"), ("Template0")));
	m_choGetTempleID.InsertString(1,GetTextByLan(("模板1"), ("Template1")));
	m_choGetTempleID.InsertString(2,GetTextByLan(("模板2"), ("Template2")));
	m_choGetTempleID.InsertString(3,GetTextByLan(("模板3"), ("Template3")));
	m_choGetTempleID.InsertString(4,GetTextByLan(("模板4"), ("Template4")));
	m_choGetTempleID.InsertString(5,GetTextByLan(("模板5"), ("Template5")));
	m_choGetTempleID.InsertString(6,GetTextByLan(("模板6"), ("Template6")));
	m_choGetTempleID.InsertString(7,GetTextByLan(("模板7"), ("Template7")));
	m_choGetTempleID.InsertString(8,GetTextByLan(("即时生效"), ("With immediate effect")));
	m_choGetTempleID.SetCurSel(0);


	m_choSetDevControl.ResetContent();
	m_choSetDevControl.InsertString(0,GetTextByLan(("上"), ("Up")));
	m_choSetDevControl.InsertString(1,GetTextByLan(("下"), ("Down")));
	m_choSetDevControl.InsertString(2,GetTextByLan(("左"), ("Left")));
	m_choSetDevControl.InsertString(3,GetTextByLan(("右"), ("Right")));
	m_choSetDevControl.InsertString(4,GetTextByLan(("右上"), ("Upper Right")));
	m_choSetDevControl.InsertString(5,GetTextByLan(("左上"), ("Upper left")));
	m_choSetDevControl.InsertString(6,GetTextByLan(("右下"), ("Low Right")));
	m_choSetDevControl.InsertString(7,GetTextByLan(("左下"), ("Low Down")));
	m_choSetDevControl.InsertString(8,GetTextByLan(("停止"), ("Stop")));
	m_choSetDevControl.InsertString(9,GetTextByLan(("变倍 焦距拉近"), ("Zoom")));
	m_choSetDevControl.InsertString(10,GetTextByLan(("变倍 焦距拉远"), ("Zoom Out")));
	m_choSetDevControl.InsertString(11,GetTextByLan(("变化停止"), ("Change Stop")));
	m_choSetDevControl.InsertString(12,GetTextByLan(("焦点调近"), ("Focus Alignment")));
	m_choSetDevControl.InsertString(13,GetTextByLan(("焦点调远"), ("Focus Distancing")));
	m_choSetDevControl.InsertString(14,GetTextByLan(("焦点变化停止"), ("Focal Stop")));
	m_choSetDevControl.InsertString(15,GetTextByLan(("光圈自动调整"), ("Auto Aperture Adjust")));
	m_choSetDevControl.InsertString(16,GetTextByLan(("光圈增大"), ("Aperture Add")));
	m_choSetDevControl.InsertString(17,GetTextByLan(("光圈缩小"), ("Aperture Reduce")));
	m_choSetDevControl.SetCurSel(0);
}

void CLS_PTZPage::UI_UpdateWindow()
{
	if (m_iLogonID < 0)
	{
		return;
	}

	int iProductType = 0;
	int iRet = NetClient_GetProductType(m_iLogonID, &iProductType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProductType(%d,%d)"
			,m_iLogonID, iProductType);

		iProductType &= 0xFFFF;
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProductType(%d,%d)"
			,m_iLogonID, iProductType);
	}
	
	int iComPortCounts = 0;
	int iComPortStatus = 0;
	iRet = NetClient_GetComPortCounts(m_iLogonID, &iComPortCounts, &iComPortStatus);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetComPortCounts(%d,%d,%d)"
			,m_iLogonID, iComPortCounts, iComPortStatus);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetComPortCounts(%d,%d,%d)"
			,m_iLogonID, iComPortCounts, iComPortStatus);

		if (IsDVR(iProductType))
		{
			iComPortCounts = 4;
		}
		else
		{
			iComPortCounts = 2;
		}
	}

	int iComIndex = 0;
	CString strCom;
	m_cboComNo.ResetContent();

	if (0 == iComPortCounts)
	{
		iComIndex = m_cboComNo.AddString(_T("Com1"));
		m_cboComNo.SetItemData(iComIndex, 1);
		iComIndex = m_cboComNo.AddString(_T("Com2"));
		m_cboComNo.SetItemData(iComIndex, 2);
	}

	for (int i = 0; i < iComPortCounts; ++i)
	{
		if (0 == (iComPortStatus & (1<<i)))
		{
			continue;
		}

		strCom.Format(_T("COM%d"),i+1);
		iComIndex = m_cboComNo.AddString(strCom);
		m_cboComNo.SetItemData(iComIndex,i+1);
	}

	//Get a list of protocols
	st_NVSProtocol protocol = {0};
	m_cboProtocolName.ResetContent();
	iRet = NetClient_GetProtocolList(m_iLogonID,&protocol);
	if(0 == iRet && protocol.iCount > 0)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProtocolList(%d,%d)"
			,m_iLogonID,protocol.iCount);

		//int iProtocolIndex = -1;
		for (int i = 0; i < protocol.iCount; i++)
		{
// 			iProtocolIndex = m_cboProtocolName.FindStringExact(-1,protocol.cProtocol[i]);
// 			if (iProtocolIndex < 0)
			{
				m_cboProtocolName.AddString(protocol.cProtocol[i]);
			}
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProtocolList(%d,%#x)"
			,m_iLogonID,&protocol);

		m_cboProtocolName.AddString(_T("PTZ_PELCO_D"));
		m_cboProtocolName.AddString(_T("PTZ_PELCO_P"));
		m_cboProtocolName.AddString(_T("PTZ_TC615_P"));
		m_cboProtocolName.AddString(_T("DOME_PELCO_D"));
		m_cboProtocolName.AddString(_T("DOME_PELCO_P"));
		m_cboProtocolName.AddString(_T("DOME_PLUS"));
	}

	int iComPort = 0;
	int iDevAddress = 0;
	int iChannelNo = 0;
	char cDeviceType[64]= {0};
	if (IsDVR(iProductType)||iProductType==0x64)
	{
		iChannelNo = m_iChannelNo;
	}
	iRet = NetClient_GetDeviceType(m_iLogonID,iChannelNo,&iComPort,&iDevAddress,cDeviceType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDeviceType(%d,%d,%d,%d,%s)"
			,m_iLogonID,iChannelNo,iComPort,iDevAddress,cDeviceType);

		if (0 == iComPort)
		{
			m_cboComNo.SetCurSel(0);
		}
		else
		{
			CString strComPort;
			strComPort.Format(_T("COM%d"), iComPort);
			m_cboComNo.SelectString(-1, strComPort);
		}
		m_cboProtocolName.SelectString(-1, cDeviceType);
		SetDlgItemInt(IDC_EDIT_ADDRESS,iDevAddress);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDeviceType(%d,%d,%d,%d,%s)"
			,m_iLogonID,iChannelNo,iComPort,iDevAddress,cDeviceType);

		SetDlgItemText(IDC_EDIT_ADDRESS,_T(""));
		SetDlgItemText(IDC_EDIT_COM_FORMAT,_T(""));
	}

	char cComFormat[64] = {0};
	iRet = NetClient_GetCHNPTZFormat(m_iLogonID,m_iChannelNo,cComFormat);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetCHNPTZFormat(%d,%d,%s)"
			,m_iLogonID,m_iChannelNo,cComFormat);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetCHNPTZFormat(%d,%d,%s)"
			,m_iLogonID,m_iChannelNo,cComFormat);
	}

	if (strlen(cComFormat) <= 1)
	{
		CheckDlgButton(IDC_CHECK_CHNPTZ,FALSE);
	}
	else
	{
		int iBaudRate = 0;
		char cParityBit = 0;
		int iDataBit = 0;
		int iStopBit = 0;
		sscanf_s(cComFormat,"%d,%c,%d,%d",&iBaudRate,&cParityBit,sizeof(char),&iDataBit,&iStopBit);

		//m_cboBaudRate.SelectString(-1,IntToString(iBaudRate));
		SetDlgItemInt(IDC_COMBO_CHN_BAUD_RATE,iBaudRate);

		for (int i = 0; i < m_cboParityBit.GetCount(); ++i)
		{
			if(cParityBit == m_cboParityBit.GetItemData(i))
			{
				m_cboParityBit.SetCurSel(i);
				break;
			}
		}

		m_cboDataBit.SelectString(-1,IntToString(iDataBit));
		m_cboStopBit.SelectString(-1,IntToString(iStopBit));
		CheckDlgButton(IDC_CHECK_CHNPTZ,TRUE);
	}
	OnBnClickedChnptzEnable();
	
	int ipresetindex = 0;
	int iIdletime = 0;
	int iPTZEnable = 0;
	iRet = NetClient_GetPTZAutoBack(m_iLogonID,m_iChannelNo,&iPTZEnable,&ipresetindex,&iIdletime);
	if (0 == iRet)
	{
		m_chkEnable.SetCheck((iPTZEnable == 1)?BST_CHECKED:BST_UNCHECKED);
		SetDlgItemInt(IDC_EDIT_PRESETINDEX, ipresetindex);
		SetDlgItemInt(IDC_EDIT_IDLETIME, iIdletime);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetPTZAutoBack(%d,%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,iPTZEnable,ipresetindex,ipresetindex);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetPTZAutoBack(%d,%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,iPTZEnable,ipresetindex,ipresetindex);
	}
}

void CLS_PTZPage::OnBnClickedButtonDeviceType()
{
	if (m_iLogonID < 0)
	{
		return;
	}

	int iDevAddress = GetDlgItemInt(IDC_EDIT_ADDRESS);
	if (iDevAddress > 256 || iDevAddress < 0)
	{
		AddLog(LOG_TYPE_MSG,"","please select a correct address(%d)",iDevAddress);
		return;
	}

	int iComIndex = m_cboComNo.GetCurSel();
	if (iComIndex < 0)
	{
		AddLog(LOG_TYPE_MSG,"","please select a correct com num");
		return;
	}

	char cDeviceType[64]= {0}; 
	m_cboProtocolName.GetWindowText(cDeviceType,sizeof(cDeviceType));
	if (strlen(cDeviceType) <= 0)
	{
		AddLog(LOG_TYPE_MSG,"","please select a correct protocol name");
		return;
	}

	int iProductType = 0;
	int iRet = NetClient_GetProductType(m_iLogonID, &iProductType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetProductType(%d,%d)"
			,m_iLogonID, iProductType);

		iProductType &= 0xFFFF;
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetProductType(%d,%d)"
			,m_iLogonID, iProductType);
	}
	
	int iChannelNo = 0;
	if(IsDVR(iProductType)||iProductType==0x64||iProductType==0x66||iProductType==0x62)
	{
		iChannelNo = m_iChannelNo;
	}

	DWORD_PTR iComNo = m_cboComNo.GetItemData(iComIndex);
	iRet = NetClient_SetDeviceType(m_iLogonID, iChannelNo, iComNo,iDevAddress,cDeviceType);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDeviceType(%d,%d,%d,%d,%s)"
			,m_iLogonID,iChannelNo,iComNo,iDevAddress,cDeviceType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDeviceType(%d,%d,%d,%d,%s)"
			,m_iLogonID,iChannelNo,iComNo,iDevAddress,cDeviceType);
	}
}

void CLS_PTZPage::OnBnClickedButtonPtzFormat()
{
	char cComFormat[64] = {0};
	if(IsDlgButtonChecked(IDC_CHECK_CHNPTZ))
	{
		int iBaudRate = GetDlgItemInt(IDC_COMBO_CHN_BAUD_RATE);
		char cParityBit = (char)m_cboParityBit.GetItemData(m_cboParityBit.GetCurSel());
		int iDataBit = GetDlgItemInt(IDC_COMBO_CHN_DATA_BIT);
		int iStopBit = GetDlgItemInt(IDC_COMBO_CHN_STOP_BIT);
		sprintf_s(cComFormat,"%d,%c,%d,%d",iBaudRate,cParityBit,iDataBit,iStopBit);
	}
	else
	{
		sprintf_s(cComFormat,"0");
	}

	int iRet = NetClient_SetCHNPTZFormat(m_iLogonID,m_iChannelNo,cComFormat);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetCHNPTZFormat(%d,%d,%s)"
			,m_iLogonID,m_iChannelNo,cComFormat);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetCHNPTZFormat(%d,%d,%s)"
			,m_iLogonID,m_iChannelNo,cComFormat);
	}
}

void CLS_PTZPage::OnBnClickedButtonAutoback()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	int ipresetindex = GetDlgItemInt(IDC_EDIT_PRESETINDEX);
	if (ipresetindex < 1 || ipresetindex > 256)
	{
		AddLog(LOG_TYPE_MSG,"","please input a valid preset(%d),between 1 and 256",ipresetindex);
		return;
	}

	int iIdletime = GetDlgItemInt(IDC_EDIT_IDLETIME);
	if (iIdletime < 10 || ipresetindex > 65535)
	{
		AddLog(LOG_TYPE_MSG,"","please input a valid idle time(%d),between 10 and 65535",ipresetindex);
		return;
	}

	int iEnable = (m_chkEnable.GetCheck() == BST_CHECKED)?1:0;
	int iRet = NetClient_SetPTZAutoBack(m_iLogonID,m_iChannelNo,iEnable,ipresetindex,iIdletime);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetPTZAutoBack(%d,%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,iEnable,ipresetindex,iIdletime);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetPTZAutoBack(%d,%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,iEnable,ipresetindex,iIdletime);
	}
}

void CLS_PTZPage::OnBnClickedChnptzEnable()
{
	BOOL bEnable = IsDlgButtonChecked(IDC_CHECK_CHNPTZ);
	m_cboDataBit.EnableWindow(bEnable);
	m_cboParityBit.EnableWindow(bEnable);
	m_cboStopBit.EnableWindow(bEnable);
	m_cboBaudRate.EnableWindow(bEnable);
}

void CLS_PTZPage::OnBnClickedButtonSetPtzSet()
{
	//int iSetPtzType = (int)m_choSetPTZType.GetItemData(m_choSetPTZType.GetCurSel());
	int iSetPtzType = m_choSetPTZType.GetCurSel();
	int iSetP = GetDlgItemInt(IDC_EDIT_SET_PTZ_P);
	int iSetT = GetDlgItemInt(IDC_EDIT_SET_PTZ_T);
	int iSetZ = GetDlgItemInt(IDC_EDIT_SET_PTZ_Z);
	SetPtz tSetPtz = {0};
	tSetPtz.iSize = sizeof(SetPtz);
	tSetPtz.iType = iSetPtzType;
	tSetPtz.iPan = iSetP;
	tSetPtz.iTilt = iSetT;
	tSetPtz.iZoom = iSetZ;

	int iRet = NetClient_SendCommand(m_iLogonID,COMMAND_ID_SET_PTZ, m_iChannelNo, &tSetPtz, sizeof(SetPtz));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand(%d,%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,&tSetPtz,sizeof(SetPtz));
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand(%d,%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,&tSetPtz,sizeof(SetPtz));
	}
}

void CLS_PTZPage::OnBnClickedButtonGetPtzGet()
{
	int iRet = NetClient_SendCommand(m_iLogonID,COMMAND_ID_GET_PTZ, m_iChannelNo, NULL, 0);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand(%d,%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_GET_PTZ,m_iChannelNo,0,0);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand(%d,%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_GET_PTZ,m_iChannelNo,0,0);
	}
}


void  CLS_PTZPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (m_iLogonID < 0 || m_iLogonID != _iLogonID)
	{
		return;
	}

	if (_iChannelNo == m_iChannelNo)//Only refresh the channel whose parameter has changed
	{
		switch(_iParaType)
		{
		case PARA_SET_PTZ:
			{
				SetPtz tSetPtz = {0};
				tSetPtz.iSize = sizeof(SetPtz);
				int iRet = NetClient_RecvCommand(m_iLogonID,COMMAND_ID_SET_PTZ, m_iChannelNo, &tSetPtz, sizeof(SetPtz));
				if(0 == iRet)
				{
					AddLog(LOG_TYPE_SUCC,"","NetClient_RecvCommand(%d,%d,%d,%d,%d)"
						,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,&tSetPtz,sizeof(SetPtz));
				}
			}
			break;
		case PARA_GET_PTZ:
			{
				GetPtz tGetPtz = {0};
				tGetPtz.iSize = sizeof(GetPtz);
				int iRet = NetClient_RecvCommand(m_iLogonID,COMMAND_ID_GET_PTZ, m_iChannelNo, &tGetPtz, sizeof(GetPtz));
				if(0 == iRet)
				{
					AddLog(LOG_TYPE_SUCC,"","NetClient_RecvCommand(%d,%d,%d,%d,%d)"
						,m_iLogonID,COMMAND_ID_GET_PTZ,m_iChannelNo,&tGetPtz,sizeof(GetPtz));

					SetDlgItemInt(IDC_EDIT_GET_PTZ_P, tGetPtz.iPosP);
					SetDlgItemInt(IDC_EDIT_GET_PTZ_T, tGetPtz.iPosT);
					SetDlgItemInt(IDC_EDIT_GET_PTZ_Z, tGetPtz.iPosZ);
				}
			}
			break;
		default:
			break;
		}
	}
}

void CLS_PTZPage::OnBnClickedButtonLensSet()
{
	// TODO: Set Lens ZF
	int iSetZ = GetDlgItemInt(IDC_EDIT_LENS_ZOOM_SET);
	int iSetF = GetDlgItemInt(IDC_EDIT_LENS_FOCUS_SET);
	ElecPTZFPosInfo tElecPTZFPosInfo = {0};
	tElecPTZFPosInfo.iChannelNo = m_iChannelNo;
	tElecPTZFPosInfo.iPosP = 0;
	tElecPTZFPosInfo.iPosT = 0;
	tElecPTZFPosInfo.iPosZ = iSetZ;
	tElecPTZFPosInfo.iPosF = iSetF;
	tElecPTZFPosInfo.iType = 0;
	ElecPTZFPosResult tInfo = {0};
	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_SET_ELEC_PTZF_POS,m_iChannelNo, &tElecPTZFPosInfo, sizeof(tElecPTZFPosInfo),&tInfo, sizeof(tInfo));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig(%d,%d,%d,%d)"
			,m_iLogonID,CMD_SET_ELEC_PTZF_POS,m_iChannelNo,tInfo.iReturn);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig(%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,tInfo.iReturn);
	}

}

void CLS_PTZPage::OnBnClickedButtonLensGet()
{
	// TODO: Get Lens ZF
	ElecPTZFPosResult tInfo = {0};
	tInfo.iType = 0;
	ElecPTZFPosInfo tElecPTZFPosInfo = {0};
	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_GET_ELEC_PTZF_POS,m_iChannelNo, &tInfo, sizeof(ElecPTZFPosInfo),&tElecPTZFPosInfo, sizeof(ElecPTZFPosInfo));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig(%d,%d,%d,%d)"
			,m_iLogonID,CMD_SET_ELEC_PTZF_POS,m_iChannelNo,tInfo.iReturn);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig(%d,%d,%d,%d)"
			,m_iLogonID,COMMAND_ID_SET_PTZ,m_iChannelNo,tInfo.iReturn);
	}
	SetDlgItemInt(IDC_EDIT_LENS_ZOOM_GET, tElecPTZFPosInfo.iPosZ);
	SetDlgItemInt(IDC_EDIT_LENS_FOCUS_GET, tElecPTZFPosInfo.iPosF);
}

void CLS_PTZPage::OnBnClickedButtonApertureOn()
{
	// TODO: Set Aperture ON 
	int _iType = 0; //0--光圈调节
	int _iAutoEnable = 0;

	//开启手动光圈
	ITS_TTimeRangeParam tTimeRangeParam = { 0 };
	tTimeRangeParam.iType = _iType;
	tTimeRangeParam.iAutoEnable[_iType] = _iAutoEnable;
	tTimeRangeParam.iParam1[_iType] = 101;
	tTimeRangeParam.iParam2[_iType] = 0;
	tTimeRangeParam.iParam3[_iType] = 0;
	tTimeRangeParam.iParam4[_iType] = 0;
	int _iTempleID = m_choSetTempleID.GetCurSel();
	if (8 == _iTempleID)
	{
		_iTempleID = 255;
	}
	int iRet = NetClient_SetHDTimeRangeParam(m_iLogonID, m_iChannelNo, _iTempleID, &tTimeRangeParam, sizeof(tTimeRangeParam));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetHDTimeRangeParam(%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,_iTempleID,tTimeRangeParam.iType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetHDTimeRangeParam(%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,_iTempleID,tTimeRangeParam.iType);
	}
	
	
}

void CLS_PTZPage::OnBnClickedButtonApertureOff()
{
	// TODO: Set Aperture OFF
	int _iType = 0; //0--光圈调节
	int _iAutoEnable = 0;
	ITS_TTimeRangeParam tTimeRangeParam = { 0 };
	tTimeRangeParam.iType = _iType;
	tTimeRangeParam.iAutoEnable[_iType] = _iAutoEnable;
	tTimeRangeParam.iParam1[_iType] = 100;
	tTimeRangeParam.iParam2[_iType] = 0;
	tTimeRangeParam.iParam3[_iType] = 0;
	tTimeRangeParam.iParam4[_iType] = 0;
	int _iTempleID = m_choSetTempleID.GetCurSel();
	if (8 == _iTempleID)
	{
		_iTempleID = 255;
	}
		//发送协议
	int iRet = NetClient_SetHDTimeRangeParam(m_iLogonID, m_iChannelNo, _iTempleID, &tTimeRangeParam, sizeof(tTimeRangeParam));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetHDTimeRangeParam(%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,_iTempleID,tTimeRangeParam.iType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetHDTimeRangeParam(%d,%d,%d,%d)"
			,m_iLogonID,m_iChannelNo,_iTempleID,tTimeRangeParam.iType);
	}
	
	
}

void CLS_PTZPage::OnBnClickedButtonApertureGet()
{
	// TODO: Get Aperture Status
	DetailCameraParam pb = {0};
	pb.iSize = sizeof(pb);
	pb.iChanNo = m_iChannelNo;

	pb.iType = 0;
	pb.iAutoEnable = 0;
	int _iTempleID = m_choSetTempleID.GetCurSel();
	if (8 == _iTempleID)
	{
		_iTempleID = 255;
	}
	int iBytesReturned = -1;
	pb.iTemplateIndex = _iTempleID;
	pb.iParam1 = -1;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_HD_TIMERANGE_PARAMEX, m_iChannelNo, &pb, sizeof(pb), &iBytesReturned);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig NET_CLIENT_HD_TIMERANGE_PARAMEX(%d,%d)",m_iLogonID,m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig NET_CLIENT_HD_TIMERANGE_PARAMEX(%d,%d), error(%d)",m_iLogonID, m_iChannelNo,GetLastError());
	}
	if (101 == pb.iParam1)
	{
		SetDlgItemTextEx(IDC_EDIT_APERTURE_STATE_GET, IDS_STATIC_APERTURE_OPEN);
	}
	else if (100 == pb.iParam1 )
	{
		SetDlgItemTextEx(IDC_EDIT_APERTURE_STATE_GET, IDS_STATIC_APERTURE_CLOSE);
	}
	else
	{
		SetDlgItemTextEx(IDC_EDIT_APERTURE_STATE_GET, IDS_STATIC_APERTURE_ERROR);
	}
	
	
}

void CLS_PTZPage::OnBnClickedButtonDevSet()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	int iActionType = m_choSetDevControl.GetCurSel()+1;
	int iPrarm1 = GetDlgItemInt(IDC_EDIT_PARAM1);
	int iPrarm2 = GetDlgItemInt(IDC_EDIT_PARAM2);
	int	iControlType = GetDlgItemInt(IDC_EDIT_CONTROL_TYPE);

	int iRet = NetClient_DeviceCtrlEx(m_iLogonID, m_iChannelNo, iActionType, iPrarm1, iPrarm2, iControlType);	
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_DeviceCtrlEx(%d,%d,%d,%d,%d,%d)",m_iLogonID, m_iChannelNo, iActionType,iPrarm1,iPrarm2, iControlType);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_DeviceCtrlEx(%d,%d,%d,%d,%d,%d)",m_iLogonID, m_iChannelNo,iActionType,iPrarm1,iPrarm2, iControlType);
	}
}
