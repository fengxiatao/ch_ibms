// AdvSystemInfoPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "AdvSystemInfoPage.h"

#define TELNET_ENABLE	1
#define TELNET_DISABLE	0
#define SYSTEM_MODE24   0
#define SYSTEM_MODE12   1

#define SELFTEST_TYPE_FULL			1
#define SELFTEST_TYPE_HARDWARE		2
#define SELFTEST_TYPE_SOFTWARE		3

// CLS_AdvSystemInfoPage dialog

IMPLEMENT_DYNAMIC(CLS_AdvSystemInfoPage, CDialog)

CLS_AdvSystemInfoPage::CLS_AdvSystemInfoPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_AdvSystemInfoPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_blTelnetClose = TRUE;
	m_iChannelNo = -1;
}

CLS_AdvSystemInfoPage::~CLS_AdvSystemInfoPage()
{
}

void CLS_AdvSystemInfoPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO1, m_ComboBoxTimeFormat);
	DDX_Control(pDX, IDC_COMBO_CONNECT_INFO, m_ComboBoxConnInfo);
	DDX_Control(pDX, IDC_LIST1, m_lstCtConnectInfo);
	DDX_Control(pDX, IDC_COMBO3, m_ComboBoxSeparate);
	DDX_Control(pDX, IDC_COMBO_TIMEMODE, m_cboTimeMode);
	DDX_Control(pDX, IDC_CHECK_WEEKDIS, m_chkWeekDis);
	DDX_Radio(pDX, IDC_RADIO_ADV_SYS_TELNET_OPEN, m_blTelnetClose);
	DDX_Control(pDX, IDC_CBO_ALGORITHM_TYPE, m_cboAlgorithmType);
	DDX_Control(pDX, IDC_CHECK_SET_ALL, m_chkAll);
	DDX_Control(pDX, IDC_CHECK_SET_NET, m_chkNet);
	DDX_Control(pDX, IDC_CHECK_STORAGE, m_chkStorage);
	DDX_Control(pDX, IDC_CHECK_EVENT, m_chkEvent);
	DDX_Control(pDX, IDC_CHECK_SYSTEM, m_chkSystem);
	DDX_Control(pDX, IDC_CHECK_CHANNEL, m_chkChannel);
	DDX_Control(pDX, IDC_CHECK_PREVIEW, m_chkPreview);
	DDX_Control(pDX, IDC_BUTTON_ADV_SYS_DELETE, m_Button_Delete);
	DDX_Control(pDX, IDC_COMBO_COMMON_DEFAULT, m_cboCommonDefault);
	DDX_Control(pDX, IDC_CHECK_CAPIMG, m_chkCapImg);
	DDX_Control(pDX, IDC_CHECK_ANALYSSISIMG, m_chkAnalysisImg);
	DDX_Control(pDX, IDC_CHECK_INFRARIMG, m_chkInfrarImg);
	DDX_Control(pDX, IDC_CHECK_INFRARCAPIMG, m_chkInfrarCapImg);
}


BEGIN_MESSAGE_MAP(CLS_AdvSystemInfoPage, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_ADV_SYS_SET_TIME, &CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysSetTime)
	ON_BN_CLICKED(IDC_BUTTON_ADV_SYS_DEFAULT, &CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysDefault)
	ON_BN_CLICKED(IDC_BUTTON_ADV_SYS_REBOOT, &CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysReboot)
	ON_BN_CLICKED(IDC_BUTTON_ADV_SYS_SHUTDOWN, &CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysShutdown)
	ON_BN_CLICKED(IDC_BUTTON_TIME_FORMAT_SET, &CLS_AdvSystemInfoPage::OnBnClickedButtonTimeFormatSet)
	ON_CBN_SELCHANGE(IDC_COMBO_CONNECT_INFO, &CLS_AdvSystemInfoPage::OnCbnSelchangeComboConnectInfo)
	ON_BN_CLICKED(IDC_BUTTON_ADV_SYS_REFRESH, &CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysRefresh)
	ON_BN_CLICKED(IDC_RADIO_ADV_SYS_TELNET_OPEN, &CLS_AdvSystemInfoPage::OnBnClickedRadioAdvSysTelnetSet)
	ON_BN_CLICKED(IDC_RADIO_ADV_SYS_TELNET_CLOSE, &CLS_AdvSystemInfoPage::OnBnClickedRadioAdvSysTelnetSet)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_SET_ALG_TYPE, &CLS_AdvSystemInfoPage::OnBnClickedSetAlgType)
	ON_BN_CLICKED(IDC_BUTTON_CALIBRATE, &CLS_AdvSystemInfoPage::OnBnClickedButtonCalibrate)
	ON_BN_CLICKED(IDC_BUTTON_SELFTEST, &CLS_AdvSystemInfoPage::OnBnClickedButtonSelftest)
	ON_BN_CLICKED(IDC_BUTTON_SET_RESET, &CLS_AdvSystemInfoPage::OnBnClickedButtonSetReset)
	ON_BN_CLICKED(IDC_CHECK_SET_ALL, &CLS_AdvSystemInfoPage::OnBnClickedCheckSetAll)
	ON_BN_CLICKED(IDC_CHECK_SET_NET, &CLS_AdvSystemInfoPage::OnBnClickedCheckSetNet)
	ON_BN_CLICKED(IDC_CHECK_STORAGE, &CLS_AdvSystemInfoPage::OnBnClickedCheckStorage)
	ON_BN_CLICKED(IDC_CHECK_EVENT, &CLS_AdvSystemInfoPage::OnBnClickedCheckEvent)
	ON_BN_CLICKED(IDC_CHECK_SYSTEM, &CLS_AdvSystemInfoPage::OnBnClickedCheckSystem)
	ON_BN_CLICKED(IDC_CHECK_CHANNEL, &CLS_AdvSystemInfoPage::OnBnClickedCheckChannel)
	ON_BN_CLICKED(IDC_CHECK_PREVIEW, &CLS_AdvSystemInfoPage::OnBnClickedCheckPreview)
	ON_BN_CLICKED(IDC_BUTTON_ADV_SYS_DELETE, &CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysDelete)
	ON_BN_CLICKED(IDC_BUTTON_COMMON_DEFAULT, &CLS_AdvSystemInfoPage::OnBnClickedButtonCommonDefault)
	ON_BN_CLICKED(IDC_CHECK_ANALYSSISIMG, &CLS_AdvSystemInfoPage::OnBnClickedCheckAnalyssisimg)
	ON_BN_CLICKED(IDC_CHECK_INFRARIMG, &CLS_AdvSystemInfoPage::OnBnClickedCheckInfrarimg)
	ON_BN_CLICKED(IDC_CHECK_INFRARCAPIMG, &CLS_AdvSystemInfoPage::OnBnClickedCheckInfrarcapimg)
	ON_BN_CLICKED(IDC_CHECK_CAPIMG, &CLS_AdvSystemInfoPage::OnBnClickedCheckCapimg)
	ON_BN_CLICKED(IDC_BUTTON_GET_DEV_TIME, &CLS_AdvSystemInfoPage::OnBnClickedButtonGetDevTime)
END_MESSAGE_MAP()


// CLS_AdvSystemInfoPage message handlers



void CLS_AdvSystemInfoPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = (_iChannelNo < 0) ? 0 : _iChannelNo;
	UI_UpdateText();
	UI_UpdateTimeFormat();
	UI_UpdateAlgType();
}

void CLS_AdvSystemInfoPage::UI_UpdateText()
{
	m_lstCtConnectInfo.SetExtendedStyle(LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);
	SetDlgItemTextEx(IDC_BUTTON_ADV_SYS_SET_TIME, IDS_ADV_SYSTEM__TIME_SET);
	SetDlgItemTextEx(IDC_BUTTON_ADV_SYS_DEFAULT, IDS_ADV_SYSTEM_DEFAULT);
	SetDlgItemTextEx(IDC_BUTTON_ADV_SYS_REBOOT, IDS_ADV_SYSTEM_REBOOT);
	SetDlgItemTextEx(IDC_BUTTON_ADV_SYS_SHUTDOWN, IDS_ADV_SYSTEM_SHUTDOWN);
	SetDlgItemTextEx(IDC_BUTTON_CALIBRATE, IDS_CAMRA_CALIBRATE);	

	m_ComboBoxConnInfo.ResetContent();
	m_ComboBoxConnInfo.AddString(GetTextEx(IDS_ADV_SYSTEM_LOGON_INFO));
	m_ComboBoxConnInfo.AddString(GetTextEx(IDS_ADV_SYSTEM_CONNECT_INFO));
	m_ComboBoxConnInfo.SetCurSel(0);

	m_cboTimeMode.ResetContent();
	m_cboTimeMode.SetItemData(m_cboTimeMode.AddString(GetTextEx(IDS_ADV_SYSTEM_24MODE)), SYSTEM_MODE24);
	m_cboTimeMode.SetItemData(m_cboTimeMode.AddString(GetTextEx(IDS_ADV_SYSTEM_12MODE)), SYSTEM_MODE12);

	UI_UpdateTimeFormat();
	UI_UpdateConnInfoList();
	SetDlgItemTextEx(IDC_BUTTON_ADV_SYS_REFRESH, IDS_ADV_CONNECT_INFO_REFRESH);
	SetDlgItemTextEx(IDC_STATIC_ADV_CONNECT_INFO, IDS_ADV_CONNECT_INFO);
	SetDlgItemTextEx(IDC_BUTTON_TIME_FORMAT_SET, IDS_ADV_SYSTEM_TIME_FORMAT_SET);

	SetDlgItemTextEx(IDC_STATIC_TIMEMODE, IDS_ADV_SYSTEM_TIMEMODE);
 	SetDlgItemTextEx(IDC_STC_ADV_SYS_TELNET_SET, IDS_ADVANCE_SYSTEM_INFO_TELNET);
 	SetDlgItemTextEx(IDC_STATIC, IDS_HOLIDAY_PLAN_MODE);
	SetDlgItemTextEx(IDC_CHECK_WEEKDIS, IDS_ADV_SYSTEM_WEEKDIS);

	SetDlgItemTextEx(IDC_RADIO_ADV_SYS_TELNET_OPEN, IDS_CONFIG_OPEN);
	SetDlgItemTextEx(IDC_RADIO_ADV_SYS_TELNET_CLOSE, IDS_CONFIG_CLOSE);

	SetDlgItemTextEx(IDC_BUTTON_SELFTEST, IDS_CONFIG_SELFTEST);

	int iTempSel = m_cboAlgorithmType.GetCurSel();
	m_cboAlgorithmType.ResetContent();
	m_cboAlgorithmType.SetItemData(m_cboAlgorithmType.AddString(GetTextEx(IDS_MONITO)), ALGORITHM_TYPE_MONITOR);
	m_cboAlgorithmType.SetItemData(m_cboAlgorithmType.AddString(GetTextEx(IDS_TRAFFIC_ALG)), ALGORITHM_TYPE_TRAFFIC);
	iTempSel = (m_cboAlgorithmType.GetCount() > iTempSel) ? iTempSel : 0;
	m_cboAlgorithmType.SetCurSel(iTempSel);
	SetDlgItemTextEx(IDC_STC_ALGORITHM_TYPE, IDS_ITS_ALGTYPE_TYPE);
	SetDlgItemTextEx(IDC_SET_ALG_TYPE, IDS_SET);
	
	SetDlgItemText(IDC_BUTTON_GET_DEV_TIME, GetTextByLan("获取设备时间", "get dev time"));
	SetDlgItemText(IDC_BUTTON_ADV_SYS_DEFAULT, GetTextByLan("恢复出厂", "ReFactory"));
	SetDlgItemText(IDC_STATIC_RESSPACE, GetTextByLan("简单恢复", "SimpleRecovery"));
	SetDlgItemText(IDC_CHECK_SET_ALL, GetTextByLan("全选", "All"));
	SetDlgItemText(IDC_CHECK_CAPIMG, GetTextByLan("可见光抓拍图像", "CapImg"));
	SetDlgItemText(IDC_CHECK_ANALYSSISIMG, GetTextByLan("可见光分析图像", "AnalysisImg"));
	SetDlgItemText(IDC_CHECK_INFRARIMG, GetTextByLan("红外光监控图像", "InfrarImg"));
	SetDlgItemText(IDC_CHECK_INFRARCAPIMG, GetTextByLan("红外光抓拍图像", "InfrarCapImg"));
	SetDlgItemText(IDC_CHECK_SET_NET, GetTextByLan("网络", "NET"));
	SetDlgItemText(IDC_CHECK_STORAGE, GetTextByLan("存储", "Storage"));
	SetDlgItemText(IDC_CHECK_EVENT, GetTextByLan("事件", "Event"));
	SetDlgItemText(IDC_CHECK_SYSTEM, GetTextByLan("系统", "System"));
	SetDlgItemText(IDC_CHECK_CHANNEL, GetTextByLan("通道", "Channel"));
	SetDlgItemText(IDC_CHECK_PREVIEW, GetTextByLan("预览", "Preview"));
	SetDlgItemText(IDC_BUTTON_SET_RESET, GetTextByLan("设置", "Set"));
	SetDlgItemText(IDC_BUTTON_ADV_SYS_DELETE, GetTextByLan("断开", "Break off"));

	SetDlgItemText(IDC_STATIC_COMMON_DEFAULT, GetTextByLan("通用恢复", "Common default"));
	SetDlgItemTextEx(IDC_BUTTON_COMMON_DEFAULT, IDS_SET);

	m_cboCommonDefault.ResetContent();
	m_cboCommonDefault.SetItemData(m_cboCommonDefault.AddString(GetTextByLan("热成像模组", "Thermal module")), 1);
	m_cboCommonDefault.SetItemData(m_cboCommonDefault.AddString(GetTextByLan("FDD恢复出厂", "FDD Recover")), 2);
	m_cboCommonDefault.SetItemData(m_cboCommonDefault.AddString(GetTextByLan("TDD恢复出厂", "TDD Recover")), 3);
	m_cboCommonDefault.SetItemData(m_cboCommonDefault.AddString(GetTextByLan("FDD重启", "FDD Reboot")), 4);
	m_cboCommonDefault.SetItemData(m_cboCommonDefault.AddString(GetTextByLan("TDD重启", "TDD Reboot")), 5);
	m_cboCommonDefault.SetCurSel(0);
}

void CLS_AdvSystemInfoPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateText();
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysSetTime()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}

	int iRet = RET_FAILED;
	SYSTEMTIME tCurrent;
	GetLocalTime(&tCurrent);

#ifdef XML_PROTOCOL
	XmlDeviceSystemTime tXmlSysTime = {0};
	tXmlSysTime.iTimingMode = TIMING_MODE_MANUAL;
	tXmlSysTime.iTimeZoneDiffHour = 8;
	tXmlSysTime.tLocalTime.iYear = tCurrent.wYear;
	tXmlSysTime.tLocalTime.iMonth = tCurrent.wMonth;
	tXmlSysTime.tLocalTime.iDay = tCurrent.wDay;
	tXmlSysTime.tLocalTime.iHour = tCurrent.wHour;
	tXmlSysTime.tLocalTime.iMinute = tCurrent.wMinute;
	tXmlSysTime.tLocalTime.iSecond = tCurrent.wSecond;
	iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_DEVICE_SYSTIME, &tXmlSysTime, sizeof(XmlDeviceSystemTime), NULL, 0);
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_XmlSetDevConfig:NETXMLCFG_DEVICE_SYSTIME(%d).", m_iLogonID);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_XmlSetDevConfig:NETXMLCFG_DEVICE_SYSTIME(%d).", m_iLogonID);
	}
#else
	iRet = NetClient_SetTime(m_iLogonID, tCurrent.wYear, tCurrent.wMonth, tCurrent.wDay,
		tCurrent.wHour, tCurrent.wMinute, tCurrent.wSecond);
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice(m_iLogonID);	
		if (Device)
		{
			AddLog(LOG_TYPE_FAIL, "","NetClient_SetTime(%s, %d)", Device->cIP, m_iLogonID);
		}
	}
#endif
	

	
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysDefault()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}
	TDefaultPara stTDefaultPara = {0};
	stTDefaultPara.iSize = sizeof(TDefaultPara);
	stTDefaultPara.iType = DEFAULT_TYPE_ALL;   // full recovery

	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_DEFAULT_PARA, m_iChannelNO,	&stTDefaultPara, sizeof(TDefaultPara));
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice(m_iLogonID);
		if (Device)
		{
			AddLog(LOG_TYPE_FAIL, "","NetClient_DefaultPara(%s, %d)", Device->cIP, m_iLogonID);
		}
	}
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysReboot()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}
	int iRet = NetClient_Reboot(m_iLogonID);
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice(m_iLogonID);
		if (Device)
		{
			AddLog(LOG_TYPE_FAIL, "","NetClient_DefaultPara(%s, %d)", Device->cIP, m_iLogonID);
		}
	}
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysShutdown()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}
	int iRet = NetClient_ShutDownDev(m_iLogonID, 0);
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice(m_iLogonID);
		if (Device)
		{
			AddLog(LOG_TYPE_FAIL, "","NetClient_DefaultPara(%s, %d)", Device->cIP, m_iLogonID);
		}
	}
}

void CLS_AdvSystemInfoPage::UI_UpdateTimeFormat()
{
	if (m_iLogonID < 0)
	{
		return;
	}
	int iBytesReturned = 0;
	TDateFormat tDateFormat = {0};
	tDateFormat.iSize = sizeof(TDateFormat);
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DATE_FORMATE, 0x7FFFFFFF, &tDateFormat, sizeof(tDateFormat), &iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDevConfig faied! Logon id(%d)", m_iLogonID);
	}
	else
	{	
		m_ComboBoxTimeFormat.SetCurSel(tDateFormat.iFormatType);
		m_ComboBoxSeparate.SelectString(-1, &tDateFormat.cSeparate);
		m_cboTimeMode.SetCurSel((int)m_cboTimeMode.GetItemData(tDateFormat.iTimeMode));
		
		m_chkWeekDis.SetCheck(tDateFormat.iFlagWeek);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDevConfig success! Logon id(%d)", m_iLogonID);
	}

	XmlDeviceSystemTime tXmlSysTime = {0};
	iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_DEVICE_SYSTIME, NULL, 0, &tXmlSysTime, sizeof(XmlDeviceSystemTime));
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_XmlGetDevConfig:NETXMLCFG_DEVICE_SYSTIME(%d).", m_iLogonID);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_XmlGetDevConfig:NETXMLCFG_DEVICE_SYSTIME(%d).", m_iLogonID);
	}
}
BOOL CLS_AdvSystemInfoPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	m_ComboBoxTimeFormat.AddString("yyyy/mm/dd");
	m_ComboBoxTimeFormat.AddString("mm/dd/yyyy");
	m_ComboBoxTimeFormat.AddString("dd/mm/yyyy");
	m_ComboBoxTimeFormat.AddString("M. day yyyy");
	m_ComboBoxTimeFormat.AddString("day M. yyyy");
	m_ComboBoxTimeFormat.SetCurSel(0);

	m_ComboBoxSeparate.AddString("/");
	m_ComboBoxSeparate.AddString("-");
	m_ComboBoxSeparate.AddString(".");
	m_ComboBoxSeparate.SetCurSel(0);

	m_cboTimeMode.ResetContent();
	m_cboTimeMode.SetItemData(m_cboTimeMode.AddString(GetTextEx(IDS_ADV_SYSTEM_24MODE)), SYSTEM_MODE24);
	m_cboTimeMode.SetItemData(m_cboTimeMode.AddString(GetTextEx(IDS_ADV_SYSTEM_12MODE)), SYSTEM_MODE12);


	m_chkWeekDis.SetCheck(FALSE);

	m_lstCtConnectInfo.SetExtendedStyle(m_lstCtConnectInfo.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

	UI_UpdateTimeFormat();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonTimeFormatSet()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}
	CString szSeparate;
	m_ComboBoxSeparate.GetLBText(m_ComboBoxSeparate.GetCurSel(), szSeparate);
	TDateFormat tDateFormat = {0};
	tDateFormat.iSize = sizeof(TDateFormat);
	tDateFormat.iFormatType = m_ComboBoxTimeFormat.GetCurSel();
	memcpy(&tDateFormat.cSeparate, szSeparate.GetBuffer(), 1);
	tDateFormat.iTimeMode =  (int)m_cboTimeMode.GetItemData(m_cboTimeMode.GetCurSel());
	tDateFormat.iFlagWeek = m_chkWeekDis.GetCheck();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DATE_FORMATE, 0x7FFFFFFF, &tDateFormat, sizeof(tDateFormat));
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice((m_iLogonID));
		if (Device)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%s, %d)", Device->cIP, m_iLogonID);
		}
	}

}

BOOL CLS_AdvSystemInfoPage::DestroyWindow()
{
	// TODO: Add your specialized code here and/or call the base class
	
	return CLS_BasePage::DestroyWindow();
}

void CLS_AdvSystemInfoPage::UI_UpdateConnInfoList()
{
	m_lstCtConnectInfo.DeleteAllItems();
	while(m_lstCtConnectInfo.DeleteColumn(0)); 
	if (m_ComboBoxConnInfo.GetCurSel())
	{
		int iColumnIndex = 0;
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_ADV_CONNECT_INFO_CLIENT_IP, LVCFMT_LEFT, 150 );//insert column
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_ADV_CONNECT_INFO_USERNAME, LVCFMT_LEFT, 80 );
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_ADV_CONNECT_INFO_CHANNEL, LVCFMT_LEFT, 200 );
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_ADV_CONNECT_INFO_NETMODE, LVCFMT_LEFT, 70 );
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_LOG_STATUS, LVCFMT_LEFT, 70 );
		m_Button_Delete.EnableWindow(FALSE);
	}
	else
	{
		int iColumnIndex = 0;
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_ADV_CONNECT_INFO_CLIENT_IP, IDS_ADV_CONNECT_INFO_CLIENT_IP, 150 );//insert column
		InsertColumn( m_lstCtConnectInfo, iColumnIndex++, IDS_ADV_CONNECT_INFO_USERNAME, LVCFMT_LEFT, 80 );
		m_Button_Delete.EnableWindow(TRUE);
	}
	UpdateConnInfoList();
}


void CLS_AdvSystemInfoPage::OnCbnSelchangeComboConnectInfo()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}
	UI_UpdateConnInfoList();
}

void CLS_AdvSystemInfoPage::UpdateConnInfoList()
{
	ConnectInfoEx svc[MAX_CONNECT_COUNT] = {0};
	int i = 0;
	for(i = 0; i < MAX_CONNECT_COUNT; i++)
	{
		svc[i].iIndex = i;
	}
	int iChannelNum = 0;
	int iReturn = -1;
	int iRet = NetClient_GetChannelNum(m_iLogonID, &iChannelNum);
	if (iRet < 0)
	{
		PDEVICE_INFO Device = FindDevice(m_iLogonID);	
		if(Device)
		{
			AddLog(LOG_TYPE_FAIL, "","NetClient_GetChannelNum(%s, %d)", Device->cIP, m_iLogonID);	
		}
		return;
	}
	for (i = 0; i < MAX_CONNECT_COUNT; i++)
	{
		iRet = NetClient_GetDevConfig(m_iLogonID,NET_CLIENT_CONNECT_INFO,m_iChannelNo,&svc[i],sizeof(ConnectInfoEx),&iReturn);
		if (iRet < 0)
		{
			PDEVICE_INFO Device = FindDevice(m_iLogonID);	
			if (Device)
			{
				AddLog(LOG_TYPE_FAIL, "","NetClient_GetConnectInfo(%s, %d)", Device->cIP, m_iLogonID);	
			}
		}
		else
		{
			int iRow = 0;
			if (m_ComboBoxConnInfo.GetCurSel())
			{
				if ((strcmp(svc[i].tConnectInfo.cClientIP, "") == 0 && strcmp(svc[i].cServerIpv6, "") == 0) || svc[i].tConnectInfo.iChannelType == 0)
				{
					continue;
				}
				m_lstCtConnectInfo.InsertItem(iRow, "");
				int iColumn = 0;
				if (IsValidIPv6(svc[i].cServerIpv6) >= 1)
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, svc[i].cServerIpv6);
				}
				else
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, svc[i].tConnectInfo.cClientIP);
				}
				m_lstCtConnectInfo.SetItemText(iRow, iColumn++, svc[i].tConnectInfo.cUserName);
				CString szChannel;
				if (svc[i].tConnectInfo.iChannelID/iChannelNum == 0)
				{
					szChannel.Format("MainStream Channel%d", svc[i].tConnectInfo.iChannelID);
				}
				else if(svc[i].tConnectInfo.iChannelID/iChannelNum == 1)
				{
					szChannel.Format("SubStream Channel%d", svc[i].tConnectInfo.iChannelID%iChannelNum);
				}
				else if (svc[i].tConnectInfo.iChannelID/iChannelNum == 2)
				{
					szChannel.Format("InterTalk Channel");
				}
				else if(svc[i].tConnectInfo.iChannelID/iChannelNum == 4)
				{
					szChannel.Format("Picture Channel");
				}
				else
				{
					szChannel.Format("Download Channel");
				}

				m_lstCtConnectInfo.SetItemText(iRow, iColumn++, szChannel);
				if (svc[i].tConnectInfo.iNetMode == 4)
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, "Avtive");
				}
				else if (svc[i].tConnectInfo.iNetMode == 3)
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, "Multiple");
				}
				else if (svc[i].tConnectInfo.iNetMode == 2)
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, "UDP");
				}
				else
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, "TCP");
				}

				CString cstrConnectStatus;
				cstrConnectStatus = (0 == svc[i].tConnectInfo.iConnectState) ? GetTextEx(IDS_MENU_CONNECT) : GetTextEx(IDS_MENU_DISCONNECT);
				m_lstCtConnectInfo.SetItemText(iRow, iColumn++, cstrConnectStatus);
				iRow ++;
			}
			else
			{
				int iRow = 0;
				if ((strcmp(svc[i].tConnectInfo.cClientIP, "") == 0 && strcmp(svc[i].cServerIpv6, "") == 0) || svc[i].tConnectInfo.iChannelType == 1)
				{
					continue;
				}
				m_lstCtConnectInfo.InsertItem(iRow, "");
				int iColumn = 0;
				if (IsValidIPv6(svc[i].cServerIpv6) >= 1)
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, svc[i].cServerIpv6);
				}
				else
				{
					m_lstCtConnectInfo.SetItemText(iRow, iColumn++, svc[i].tConnectInfo.cClientIP);
				}
				m_lstCtConnectInfo.SetItemText(iRow, iColumn++, svc[i].tConnectInfo.cUserName);
				iRow ++;
			}
		}
	}
}
void CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysRefresh()
{
	// TODO: Add your control notification handler code here
	if (m_iLogonID < 0)
	{
		return;
	}
	UI_UpdateConnInfoList();
}

void CLS_AdvSystemInfoPage::OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iMsgType = _wParam & 0xFFFF;
	switch(iMsgType)
	{
	case WCM_CONNECT_INFO:
		{
			NVS_IPAndID* pIPID = (NVS_IPAndID*)_iLParam;
			int iLogonID = *pIPID->m_piLogonID;
			if (m_iLogonID == iLogonID)
			{
				UI_UpdateConnInfoList();
			}
		}
		break;
	case WCM_TO_DEFAULT_PARAM:
		{
			DefaultDevParam tInfo = {0};
			DefaultDevParam* ptInfo = (DefaultDevParam*)_iLParam;
			if (NULL != ptInfo)
			{
				int iCpySize = min(ptInfo->iSize, sizeof(DefaultDevParam));
				memcpy(&tInfo, ptInfo, iCpySize);

				if (1 == tInfo.iResult)
				{
					AddLog(LOG_TYPE_MSG,"","WCM_TO_DEFAULT_PARAM failed  iType=%d iResult=%d",tInfo.iType, tInfo.iResult);
				}
				else
				{
					AddLog(LOG_TYPE_MSG,"","WCM_TO_DEFAULT_PARAM success iType=%d iResult=%d",tInfo.iType, tInfo.iResult);
				}
			}
		}
		break;
	default:
		break;
	}
}

void CLS_AdvSystemInfoPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (PARA_DEV_TELNET == _iParaType)
	{
		BOOL blIsOpen = 0;
		NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_TELNET, 0x7FFFFFFF, &blIsOpen);
		m_blTelnetClose = !blIsOpen;
		UpdateData(FALSE);
	}	
}

void CLS_AdvSystemInfoPage::OnBnClickedRadioAdvSysTelnetSet()
{
	int iTelnetStatus = m_blTelnetClose;
	UpdateData();
	if (iTelnetStatus == m_blTelnetClose)
	{
		return;
	}

	NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_TELNET, 0x7FFFFFFF, !m_blTelnetClose);
}

void CLS_AdvSystemInfoPage::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		BOOL blIsOpen = 0;
		NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_TELNET, 0x7FFFFFFF, &blIsOpen);
		m_blTelnetClose = !blIsOpen;
		UpdateData(FALSE);
	}
}

void CLS_AdvSystemInfoPage::UI_UpdateAlgType()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Illeage LogonId(%d)",m_iLogonID);
		return;
	}
	AlgorithmType tAlgorithmType = {0};
	tAlgorithmType.iSize = sizeof(AlgorithmType);
	int iByteReturn = 0;

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ALGORITHM_TYPE, m_iChannelNo, &tAlgorithmType, sizeof(AlgorithmType), &iByteReturn);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","GetDevConfig AlgorithmType(%d,%d)",m_iLogonID, m_iChannelNo);
		goto EXIT_FUNC;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","GetDevConfig AlgorithmType(%d,%d)",m_iLogonID, m_iChannelNo);
	} 

	m_cboAlgorithmType.SetCurSel(GetCboSel(&m_cboAlgorithmType, tAlgorithmType.iAlgorithmType));

EXIT_FUNC:
	return;
}

void CLS_AdvSystemInfoPage::OnBnClickedSetAlgType()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Illeage LogonId(%d)",m_iLogonID);
		return;
	}
	AlgorithmType tAlgorithmType = {0};
	tAlgorithmType.iSize = sizeof(AlgorithmType);
	tAlgorithmType.iAlgorithmType = (int)m_cboAlgorithmType.GetItemData(m_cboAlgorithmType.GetCurSel());

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ALGORITHM_TYPE, m_iChannelNo, &tAlgorithmType, sizeof(AlgorithmType));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","SetDevConfig AlgorithmType(%d,%d)",m_iLogonID, m_iChannelNo);
		goto EXIT_FUNC;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","SetDevConfig AlgorithmType(%d,%d)",m_iLogonID, m_iChannelNo);
	} 

EXIT_FUNC:
	return;
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonCalibrate()
{
	int iTestType = 1;
	int iTestEnable = 1;

	int iRet = NetClient_CheckCamera(m_iLogonID, m_iChannelNo, iTestType, iTestEnable);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CheckCamera(%d,%d)",m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CheckCamera(%d,%d)",m_iLogonID, m_iChannelNo);
	}

}

void CLS_AdvSystemInfoPage::OnBnClickedButtonSelftest()
{
	SelfTest tParam = {0};
	tParam.iSize = sizeof(tParam);
	tParam.iType = SELFTEST_TYPE_FULL;
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_SELFTEST, m_iChannelNo, &tParam, sizeof(tParam));
	if(TD_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand COMMAND_ID_SELFTEST(%d,%d)",m_iLogonID, m_iChannelNo);
	}
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonSetReset()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Illeage LogonId(%d)",m_iLogonID);
		return;
	}

	int iResetLevel = 0;
	iResetLevel += m_chkCapImg.GetCheck()<<3;
	iResetLevel += m_chkAnalysisImg.GetCheck()<<4;
	iResetLevel += m_chkInfrarImg.GetCheck()<<5;
	iResetLevel += m_chkInfrarCapImg.GetCheck()<<6;
	iResetLevel += m_chkNet.GetCheck()<<16;
	iResetLevel += m_chkStorage.GetCheck()<<17;
	iResetLevel += m_chkSystem.GetCheck()<<18;
	iResetLevel += m_chkEvent.GetCheck()<<19;
	iResetLevel += m_chkChannel.GetCheck()<<20;
	iResetLevel += m_chkPreview.GetCheck()<<21;

	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_SYSTEM;
	stFuncAbilityLevel.iSubFuncType = 0;
	int iReturnByte = -1;
    int iResult = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNo,&stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iReturnByte);
	if (RET_SUCCESS == iRet && 0 < strlen(stFuncAbilityLevel.cParam))
	{
		iResult = _ttoi(stFuncAbilityLevel.cParam);		
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_AdvSystemInfoPage NET_CLIENT_GET_FUNC_ABILITY(%d,%d)",m_iLogonID, m_iChannelNo);

	}
	if(1 == iResult)
	{
		TDefaultPara stTDefaultPara = {0};
		stTDefaultPara.iSize = sizeof(TDefaultPara);
		stTDefaultPara.iType = iResetLevel;
		iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_DEFAULT_PARA, m_iChannelNO, &stTDefaultPara, sizeof(TDefaultPara));
		if(RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_SUCC,"","CLS_AdvSystemInfoPage COMMAND_ID_DEFAULT_PARA(%d,%d)",m_iLogonID, m_iChannelNo);
		}
	}
	else
	{
		iRet = NetClient_DefaultPara(m_iLogonID);
		if (iRet < 0)
		{
			PDEVICE_INFO Device = FindDevice(m_iLogonID);
			if (Device)
			{
				AddLog(LOG_TYPE_FAIL, "","NetClient_DefaultPara(%s, %d)", Device->cIP, m_iLogonID);
			}
		}
	}

}

void CLS_AdvSystemInfoPage::OnBnClickedCheckSetAll()
{
	int iCheck = m_chkAll.GetCheck();
	m_chkChannel.SetCheck(iCheck);
	m_chkNet.SetCheck(iCheck);
	m_chkEvent.SetCheck(iCheck);
	m_chkSystem.SetCheck(iCheck);
	m_chkStorage.SetCheck(iCheck);
	m_chkPreview.SetCheck(iCheck);
	m_chkCapImg.SetCheck(iCheck);
	m_chkAnalysisImg.SetCheck(iCheck);
	m_chkInfrarImg.SetCheck(iCheck);
	m_chkInfrarCapImg.SetCheck(iCheck);
}

void CLS_AdvSystemInfoPage::UpdateAllSelectBtn()
{
	if (BST_CHECKED == m_chkStorage.GetCheck() 
		&& BST_CHECKED == m_chkEvent.GetCheck() 
		&& BST_CHECKED == m_chkSystem.GetCheck() 
		&& BST_CHECKED == m_chkChannel.GetCheck()
		&& BST_CHECKED == m_chkNet.GetCheck()
		&& BST_CHECKED == m_chkPreview.GetCheck()
		&& BST_CHECKED == m_chkCapImg.GetCheck()
		&& BST_CHECKED == m_chkAnalysisImg.GetCheck()
		&& BST_CHECKED == m_chkInfrarImg.GetCheck()
		&& BST_CHECKED == m_chkInfrarCapImg.GetCheck())
	{
		m_chkAll.SetCheck(BST_CHECKED);
	} 
	else
	{
		m_chkAll.SetCheck(BST_UNCHECKED);
	}
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckSetNet()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckStorage()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckEvent()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckSystem()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckChannel()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckPreview()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonAdvSysDelete()
{
	// TODO: Add control notification handler code here

	int iIndex = 0;
	struct hostent *hst = NULL;
	unsigned char* pucAddr = NULL;
	char cIPArray[32][64] = {0};
	char cHostName[255] = {0};
	memset(cHostName,0,sizeof(cHostName));
	gethostname(cHostName, sizeof(cHostName));
	hst = gethostbyname(cHostName);
	if (hst)
	{
		for(iIndex=0; iIndex < 32; iIndex++)
		{
			pucAddr = (unsigned char*)hst->h_addr_list[iIndex];
			if (NULL == pucAddr)
			{
				break;
			}
			sprintf_s(cIPArray[iIndex],"%d.%d.%d.%d",*pucAddr,*(pucAddr+1),*(pucAddr+2),*(pucAddr+3));
		}
	}

	//disconnect online users
	TNetOffLine tNetOffLine = {0};
	int iLstCourt = m_lstCtConnectInfo.GetItemCount();
	bool bIsExist = false;
	for (int i=0; i<iLstCourt; i++)
	{		
		int iChecked  = m_lstCtConnectInfo.GetCheck(i);
		if (BST_CHECKED == iChecked)
		{
			CString cstrIp		= m_lstCtConnectInfo.GetItemText(i, 0);
			for(iIndex=0; iIndex < 32; iIndex++)
			{
				if (cstrIp == cIPArray[iIndex])
				{
					bIsExist = true;
					MessageBox("Unable to delete local ip","login information",0);
					break;
				}
				if (bIsExist)
				{
					continue;
				}
			}
			if (bIsExist)
			{
				continue;
			}
			if (IsValidIPv6(cstrIp) >= 1)
			{
				strcpy_s(tNetOffLine.pcIPAddrv6, cstrIp);
			}
			else
			{
				strcpy_s(tNetOffLine.cIPAddr, cstrIp);
			}
			tNetOffLine.iSize	= sizeof(TNetOffLine);
			tNetOffLine.iOffTime = 30;//The current device defaults to 30
			int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLINET_NETOFFLINE, m_iChannelNO,&tNetOffLine,sizeof(TNetOffLine));
			if (iRet == 0)
			{
				AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLINET_NETOFFLINE] successfully delete");
			}
			else
			{
				AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig[NET_CLINET_NETOFFLINE] delete failed" );
			}
		}
	}
	OnBnClickedButtonAdvSysRefresh();
}

void CLS_AdvSystemInfoPage::OnBnClickedButtonCommonDefault()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Illeage LogonId(%d)",m_iLogonID);
		return;
	}

	DefaultDevParam tPara = {0};
	tPara.iSize = sizeof(tPara);
	tPara.iType = m_cboCommonDefault.GetItemData(m_cboCommonDefault.GetCurSel());
	int iRet = NetClient_SendCommand(m_iLogonID, COMMAND_ID_TO_DEFAULT_PARA, m_iChannelNO, &tPara, sizeof(tPara));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SendCommand COMMAND_ID_TO_DEFAULT_PARA(%d,%d)",m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SendCommand COMMAND_ID_TO_DEFAULT_PARA(%d,%d)",m_iLogonID, m_iChannelNo);
	}
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckCapimg()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckAnalyssisimg()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckInfrarimg()
{
	UpdateAllSelectBtn();
}

void CLS_AdvSystemInfoPage::OnBnClickedCheckInfrarcapimg()
{
	UpdateAllSelectBtn();
}



void CLS_AdvSystemInfoPage::OnBnClickedButtonGetDevTime()
{
	// TODO: Get Dev Time
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Illeage LogonId(%d)",m_iLogonID);
		return;
	}
	NVS_FILE_TIME_V1 tPara = {0};
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_GET_DEVICE_TIME, m_iChannelNO, NULL, 0 ,&tPara, sizeof(tPara));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig CMD_GET_DEVICE_TIME(%d,%d)",m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig CMD_GET_DEVICE_TIME(%d,%d)",m_iLogonID, m_iChannelNo);
	}

	CString strTime;
	CTime tNow = CTime::GetCurrentTime();   
	strTime.Format("%04d-%02d-%02d  %02d:%02d:%02d"
		,tPara.iYear,tPara.iMonth,tPara.iDay
		,tPara.iHour,tPara.iMinute,tPara.iSecond);
	SetDlgItemText(IDC_EDIT_GET_DEV_TIME, strTime);
}


