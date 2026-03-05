// LS_DlgCPCArea.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgCPCArea.h"
#include "net_sdk_types.h"

// CLS_DlgCPCArea dialog

IMPLEMENT_DYNAMIC(CLS_DlgCPCArea, CDialog)

CLS_DlgCPCArea::CLS_DlgCPCArea(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_DlgCPCArea::IDD, pParent)
	, m_iCurAreaNo(0)
	, m_iEnable(0)
	, m_csAreaName(_T(""))
	, m_iCurPeople(0)
	, m_iMaxPeople(0)
	, m_iHour(0)
	, m_iMinutes(0)
	,m_pclsChanCheck(NULL)
	, m_csQueryAreaName(_T(""))
	, m_iQueryCurPeopleNum(0)
	, m_iQueryMaxPeople(0)
	, m_csQueryStartTime(_T(""))
{

}

CLS_DlgCPCArea::~CLS_DlgCPCArea()
{
}

void CLS_DlgCPCArea::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_DEVTYPE, m_cboDevType);
	DDX_Text(pDX, IDC_EDIT_SHOWAREA, m_iCurAreaNo);
	DDV_MinMaxInt(pDX, m_iCurAreaNo, 0, 19);
	DDX_Control(pDX, IDC_COMBO_SHOWTYPE, m_cboShowType);
	DDX_Control(pDX, IDC_COMBO_AREANO, m_cboAreaNo);
	DDX_Check(pDX, IDC_CHECK_AREA_ENABLE, m_iEnable);
	DDX_Text(pDX, IDC_EDIT_AREANAME, m_csAreaName);
	DDV_MaxChars(pDX, m_csAreaName, 63);
	DDX_Text(pDX, IDC_EDIT_AREA_CURPEOPLE, m_iCurPeople);
	DDX_Text(pDX, IDC_EDIT_AREA_MAXPEOPLE, m_iMaxPeople);
	DDX_Control(pDX, IDC_COMBO_AREACLEARMODE, m_cboClearMode);
	DDX_Text(pDX, IDC_EDIT_AREA_HOUR, m_iHour);
	DDV_MinMaxInt(pDX, m_iHour, 0, 23);
	DDX_Text(pDX, IDC_EDIT_AREA_MINUTE, m_iMinutes);
	DDV_MinMaxInt(pDX, m_iMinutes, 0, 59);
	DDX_Control(pDX, IDC_COMBO_AREANO2, m_cboAreaNoClear);
	DDX_Control(pDX, IDC_COMBO_AREANO3, m_cboQueryAreaNo);
	DDX_Text(pDX, IDC_EDIT_AREANAME2, m_csQueryAreaName);
	DDX_Text(pDX, IDC_EDIT_AREA_CURPEOPLE2, m_iQueryCurPeopleNum);
	DDX_Text(pDX, IDC_EDIT_AREA_MAXPEOPLE2, m_iQueryMaxPeople);
	DDX_Text(pDX, IDC_EDIT_AREA_CPCTIME, m_csQueryStartTime);
}


BEGIN_MESSAGE_MAP(CLS_DlgCPCArea, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_AREA_DISPLAY, &CLS_DlgCPCArea::OnBnClickedButtonAreaDisplay)
	ON_BN_CLICKED(IDC_BUTTON_AREA_CONFIG, &CLS_DlgCPCArea::OnBnClickedButtonAreaConfig)
	ON_BN_CLICKED(IDC_BUTTON_CLEAR_CPCAREA, &CLS_DlgCPCArea::OnBnClickedButtonClearCpcarea)
	ON_BN_CLICKED(IDC_BUTTON_AREA_QUERY, &CLS_DlgCPCArea::OnBnClickedButtonAreaQuery)
END_MESSAGE_MAP()


// CLS_DlgCPCArea message handlers


BOOL CLS_DlgCPCArea::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	// TODO:  Add extra initialization here

	m_cboDevType.AddString("IPC");
	m_cboDevType.AddString("NVR");
	m_cboDevType.SetCurSel(0);

	
	for (int i = 0; i < 20; i++)
	{
		CString strAreaNo;
		strAreaNo.Format("%d",i);
		m_cboAreaNo.AddString(strAreaNo);
		m_cboQueryAreaNo.AddString(strAreaNo);
		m_cboAreaNoClear.AddString(strAreaNo);
	}
	m_cboAreaNo.SetCurSel(0);
	m_cboQueryAreaNo.SetCurSel(0);
	m_cboAreaNoClear.SetCurSel(0);

	UI_UpdateChanCheck();
	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}
void CLS_DlgCPCArea::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	if (_iStreamNo < 0)
	{
		m_iStreamNO = 0;
	}
	else
	{
		m_iStreamNO = _iStreamNo;
	}

	UpdatePageUI();
}

void CLS_DlgCPCArea::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_DlgCPCArea::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	if (m_iLogonID < 0)
	{
		return;
	}

}

void CLS_DlgCPCArea::UpdateUIText()
{
	m_cboShowType.ResetContent();
	m_cboShowType.AddString(GetTextEx(IDS_VCA_CPC_AREA_SHOWINTERFACE));
	m_cboShowType.AddString(GetTextEx(IDS_VCA_CPC_AREA_EXITINTERFACE));
	m_cboShowType.SetCurSel(0);

	SetDlgItemTextEx(IDC_STATIC_DEVTYPE, IDS_VCA_AREADISPLAY_DEVTYPE);
	SetDlgItemTextEx(IDC_STATIC_SHOWCPCTYPE, IDS_VCA_AREADISPLAY_SHOWTYPE);
	SetDlgItemTextEx(IDC_STATIC_SHOWAREA, IDS_VCA_AREADISPLAY_CURSHOWAREA);
	SetDlgItemTextEx(IDC_BUTTON_AREA_DISPLAY, IDS_COMMON_SET);
	SetDlgItemTextEx(IDC_STATIC_AREANO, IDS_VCA_AREACONFIG_AREANO);
	SetDlgItemTextEx(IDC_CHECK_AREA_ENABLE, IDS_VCA_AREACONFIG_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_AREANAME, IDS_VCA_AREACONFIG_AREANAME);
	SetDlgItemTextEx(IDC_STATIC_AREA_CURPEOPLE, IDS_VCA_AREACONFIG_CURPEOPLE);
	SetDlgItemTextEx(IDC_STATIC_AREA_MAXPEOPLE, IDS_VCA_AREACONFIG_MAXPEOPLE);
	SetDlgItemTextEx(IDC_STATIC_CLEARMODE, IDS_VCA_AREACONFIG_CLEARMODE);
	SetDlgItemTextEx(IDC_STATIC_AREA_HOUR, IDS_VCA_AREACONFIG_HOUR);
	SetDlgItemTextEx(IDC_STATIC_AREA_MINUTE, IDS_VCA_AREACONFIG_MINUTE);
	SetDlgItemTextEx(IDC_BUTTON_AREA_CONFIG, IDS_COMMON_SET);
	SetDlgItemTextEx(IDC_STATIC_AREADISPLAY, IDS_VCA_AREADISPALY);
	SetDlgItemTextEx(IDC_STATIC_AREACONFIG, IDS_VCA_AREACONFIG);
	SetDlgItemTextEx(IDC_STATIC_AREANO3, IDS_VCA_AREACONFIG_AREANO);
	SetDlgItemTextEx(IDC_STATIC_AREANAME2, IDS_VCA_AREACONFIG_AREANAME);
	SetDlgItemTextEx(IDC_STATIC_AREA_CURPEOPLE2, IDS_VCA_AREACONFIG_CURPEOPLE);
	SetDlgItemTextEx(IDC_STATIC_AREA_MAXPEOPLE2, IDS_VCA_AREACONFIG_MAXPEOPLE);
	SetDlgItemText(IDC_STATIC_AREA_CPCTIME, GetTextByLan("统计开始时间","Start Time"));
	SetDlgItemText(IDC_BUTTON_AREA_QUERY, GetTextByLan("查询","Query"));

	m_cboClearMode.ResetContent();
	m_cboClearMode.AddString(GetTextEx(IDS_VCA_AREACONFIG_EVERYDAY));
	m_cboClearMode.AddString(GetTextEx(IDS_VCA_AREACONFIG_NERVER));
	m_cboClearMode.SetCurSel(0);

}

void CLS_DlgCPCArea::UpdatePageUI()
{
	UpdateAreaDisplay();
	UpdateAreaConfig();

}
void CLS_DlgCPCArea::UpdateAreaDisplay()
{
	CpcAreaDisplay tCpcAreaDisplay = {0};
	tCpcAreaDisplay.iSize = sizeof(tCpcAreaDisplay);


	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CPC_AREADISPALY, m_iChannelNO, &tCpcAreaDisplay, sizeof(tCpcAreaDisplay));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCAGetConfig[VCA_CMD_CPC_AREADISPALY] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCAGetConfig[VCA_CMD_CPC_AREADISPALY] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	m_cboDevType.SetCurSel(tCpcAreaDisplay.iDevType );
	m_cboShowType.SetCurSel(tCpcAreaDisplay.iDisplay-1);
	m_iCurAreaNo = tCpcAreaDisplay.iAreaNo;

	UpdateData(FALSE);

}
void CLS_DlgCPCArea::OnBnClickedButtonAreaDisplay()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);

	CpcAreaDisplay tCpcAreaDisplay = {0};
	tCpcAreaDisplay.iSize = sizeof(tCpcAreaDisplay);
	tCpcAreaDisplay.iDevType = m_cboDevType.GetCurSel();
	tCpcAreaDisplay.iDisplay = m_cboShowType.GetCurSel()+1;
	tCpcAreaDisplay.iAreaNo = m_iCurAreaNo;

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CPC_AREADISPALY, m_iChannelNO, &tCpcAreaDisplay, sizeof(tCpcAreaDisplay));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_CPC_AREADISPALY] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_CPC_AREADISPALY] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

}
void CLS_DlgCPCArea::UpdateAreaConfig()
{
	CpcAreaConfig tCpcAreaConfig = {0};
	tCpcAreaConfig.iSize = sizeof(tCpcAreaConfig);

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CPC_AREACONFIG, m_iChannelNO, &tCpcAreaConfig, sizeof(tCpcAreaConfig));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCAGetConfig[VCA_CMD_CPC_AREACONFIG] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCAGetConfig[VCA_CMD_CPC_AREACONFIG] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

	int iChannelNum = 0;	//Total number of channels
	NetClient_GetChannelNum(m_iLogonID, &iChannelNum);

	int iChanEnable[LEN_16] = {0};
	for (int i=0; i<LEN_16 && tCpcAreaConfig.iMaskCount; i++)
	{
		iChanEnable[i] = tCpcAreaConfig.iMask[i];
	}
	m_pclsChanCheck->InitData(iChannelNum,iChanEnable);


	UpdateData(FALSE);

}
void CLS_DlgCPCArea::OnBnClickedButtonAreaConfig()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	CpcAreaConfig tCpcAreaConfig = {0};
	tCpcAreaConfig.iSize = sizeof(tCpcAreaConfig);
	tCpcAreaConfig.iAreaNo = m_cboAreaNo.GetCurSel();
	tCpcAreaConfig.iEnable = m_iEnable;
	tCpcAreaConfig.iCurPeople = m_iCurPeople;
	tCpcAreaConfig.iMaxPeople = m_iMaxPeople;
	tCpcAreaConfig.iClearMode = m_cboClearMode.GetCurSel()+1;
	tCpcAreaConfig.iHour = m_iHour;
	tCpcAreaConfig.iMinute = m_iMinutes;
	strncpy_s(tCpcAreaConfig.cName,m_csAreaName.GetBuffer(0),sizeof(tCpcAreaConfig.cName));

	int iChanEnable[LEN_16];
	m_pclsChanCheck->GetChanValue(iChanEnable);

	int iMaskCount = 0;
	for (int i = LEN_16 - 1 ; i >= 0; i--)
	{
		if(0==iMaskCount&&iChanEnable!=0)
		{
			iMaskCount = i;
		}

		tCpcAreaConfig.iMask[i] = iChanEnable[i];
	}
	tCpcAreaConfig.iMaskCount = iMaskCount + 1;


	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CPC_AREACONFIG, m_iChannelNO, &tCpcAreaConfig, sizeof(tCpcAreaConfig));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_CPC_AREACONFIG] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAScanArea::NetClient_VCASetConfig[VCA_CMD_CPC_AREACONFIG] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

}
void CLS_DlgCPCArea::UI_UpdateChanCheck()
{
	if (NULL == m_pclsChanCheck )
	{
		m_pclsChanCheck = new CLS_ChanCheck();
		m_pclsChanCheck->Create(IDD_DLG_CFG_CHANNEL_CHECK, this);
	}

	if (NULL == m_pclsChanCheck  )
	{
		return;
	}

	RECT rc = {0};
	GetDlgItem(IDC_STATIC_MASK)->GetWindowRect(&rc);
	ScreenToClient(&rc);
	rc.top += 15;
	rc.bottom -= 10;
	rc.left += 5;
	rc.right -= 5;
	m_pclsChanCheck->MoveWindow(&rc);
	m_pclsChanCheck->ShowWindow(SW_SHOW);

}
void CLS_DlgCPCArea::OnBnClickedButtonClearCpcarea()
{
	// TODO: Add your control notification handler code here
	CpcAreaManualClear tInfo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iAreaNo = m_cboAreaNoClear.GetCurSel();

	CpcAreaManualClearResult tResultInfo = {0};
	tResultInfo.iSize = sizeof(tResultInfo);
	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_CPC_AREA_MANUALCLEAR,m_iChannelNO,&tInfo,tInfo.iSize,&tResultInfo,tResultInfo.iSize);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCPCArea::NetClient_CmdConfig[CMD_CPC_AREA_MANUALCLEAR] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCPCArea::NetClient_CmdConfig[CMD_CPC_AREA_MANUALCLEAR] (%d, %d) iResult = %d", m_iLogonID, m_iChannelNO,tResultInfo.iResult);

		if(0==tResultInfo.iResult)
		{
			MessageBox(GetTextByLan("清除成功!","Clear Success"));
		}
		else
		{
			MessageBox(GetTextByLan("清除失败!","Clear Faild"));
		}
	}
}

void CLS_DlgCPCArea::OnBnClickedButtonAreaQuery()
{
	// TODO: Add your control notification handler code here
	CpcAreaStatus tInfo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iAreaNo = m_cboQueryAreaNo.GetCurSel();

	CpcAreaStatusResult tResultInfo = {0};
	tResultInfo.iSize = sizeof(tResultInfo);
	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_QUERY_CPC_AREASTATUS,m_iChannelNO,&tInfo,tInfo.iSize,&tResultInfo,tResultInfo.iSize);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCPCArea::NetClient_CmdConfig[CMD_QUERY_CPC_AREASTATUS] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_csQueryAreaName = tResultInfo.cName;
		m_iQueryCurPeopleNum = tResultInfo.iCurPeople;
		m_iQueryMaxPeople = tResultInfo.iMaxPeople;
		m_csQueryStartTime.Format("%s",tResultInfo.iStartTime);

	
		UpdateData(FALSE);

		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCPCArea::NetClient_CmdConfig[CMD_QUERY_CPC_AREASTATUS] (%d, %d)", m_iLogonID, m_iChannelNO);
	}

}
