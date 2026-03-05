
#include "stdafx.h"
#include "OSDBackGroundPage.h"


IMPLEMENT_DYNAMIC(CLS_OSDBackgroundPage, CDialog)

CLS_OSDBackgroundPage::CLS_OSDBackgroundPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_OSDBackgroundPage::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iColor = 0;
}

CLS_OSDBackgroundPage::~CLS_OSDBackgroundPage()
{
}

void CLS_OSDBackgroundPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_BTN_COLOR_SHOW, m_btnOSDColor);
	DDX_Control(pDX, IDC_CBO_CH_TYPE, m_cboOsdCharType);
	DDX_Control(pDX, IDC_EDT_OSD_SORT, m_cboSort);
	DDX_Control(pDX, IDC_CBO_OSD_ENHANCE, m_cboEnhance);
	DDX_Control(pDX, IDC_COMBO_OSD_TYPE, m_cboOsdType);
}


BEGIN_MESSAGE_MAP(CLS_OSDBackgroundPage, CLS_BasePage)

	ON_BN_CLICKED(IDC_BTN_SELECT_COLOR, &CLS_OSDBackgroundPage::OnBnClickedBtnSelectColor)
	ON_BN_CLICKED(IDC_BTN_SET_OSD_BACK_GROUND, &CLS_OSDBackgroundPage::OnBnClickedBtnSetOsdBackGround)
	ON_CBN_SELCHANGE(IDC_CBO_CH_TYPE, &CLS_OSDBackgroundPage::OnCbnSelchangeCboChType)
	ON_BN_CLICKED(IDC_BUTTON_OSDBCLARITY_SEND, &CLS_OSDBackgroundPage::OnBnClickedButtonOsdbclaritySend)
	ON_CBN_SELCHANGE(IDC_COMBO_OSD_TYPE, &CLS_OSDBackgroundPage::OnCbnSelchangeComboOsdType)
END_MESSAGE_MAP()

BOOL CLS_OSDBackgroundPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_btnOSDColor.SetChildNum(0);
	m_btnOSDColor.SetColor(0,RGB(0,0,0));

	UI_UpdateDialog();
	
	return TRUE;
}

void CLS_OSDBackgroundPage::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = (_iChannelNo < 0) ? 0 : _iChannelNo;

	UI_UpdateOsdBackGroundParam();
	UI_UpdateOsdClarity();
}

void CLS_OSDBackgroundPage::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_OSDBackgroundPage::OnParamChangeNotify(int m_iLogonID, int m_iChannelNo, int _iParaType,void* _pPara,int _iUser)
{
	//Reserve
}

void CLS_OSDBackgroundPage::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_GPO_OSD_BAOCKGROUND, IDS_OSD_BACK_GROUND);
	SetDlgItemTextEx(IDC_STC_CH_TYPE, IDS_OSD_TYPE);
	SetDlgItemTextEx(IDC_BTN_SELECT_COLOR, IDS_CONFIG_OSD_CHOOSE_COLOR);
	SetDlgItemTextEx(IDC_STC_OSD_NUM, IDS_BACK_NUM);
	SetDlgItemTextEx(IDC_STC_OSD_SORT, IDS_SORT);
	SetDlgItemTextEx(IDC_STC_OSD_ENHANCE, IDS_CHAR_ENHANCE);
	SetDlgItemTextEx(IDC_BTN_SET_OSD_BACK_GROUND, IDS_SET);
	SetDlgItemText(IDC_STATIC_OSD_CLARITY,GetTextByLan(_T("字符叠加背景透明度"), _T("Character overlay background transparency ")));
	SetDlgItemText(IDC_STATIC_OSD_TYPE, GetTextByLan(_T("字符类型"), _T("Character type")));
	SetDlgItemText(IDC_STATIC_BCLARITY, GetTextByLan(_T("背景透明度"), _T("Background transparency")));
	SetDlgItemText(IDC_BUTTON_OSDBCLARITY_SEND, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_FCLARITY, GetTextByLan(_T("前景透明度"), _T("Foreground transparency")));


	int iTempIndex = m_cboOsdCharType.GetCurSel();
	iTempIndex = iTempIndex < 0 ? 0 : iTempIndex;
	m_cboOsdCharType.ResetContent();
	m_cboOsdCharType.SetItemData(m_cboOsdCharType.AddString(GetTextEx(IDS_ADV_CHANNEL_NAME)), 1);
	m_cboOsdCharType.SetItemData(m_cboOsdCharType.AddString(GetTextEx(IDS_CONFIG_OSD_DATETIME)), 2);
	m_cboOsdCharType.SetItemData(m_cboOsdCharType.AddString(GetTextEx(IDS_CONFIG_OSD_LOGO)), 3);
	m_cboOsdCharType.SetItemData(m_cboOsdCharType.AddString(GetTextEx(IDS_CONFIG_OSD_ADDITIONALCHAR)), 4);
	m_cboOsdCharType.SetItemData(m_cboOsdCharType.AddString(GetTextEx(IDS_CONFIG_OSD_ITS)), 5);
	m_cboOsdCharType.SetItemData(m_cboOsdCharType.AddString(GetTextEx(IDS_CONFIG_ITS) + GetTextEx(IDS_ITS_COMPO_PIC)), 6);
	iTempIndex = (iTempIndex < m_cboOsdCharType.GetCount()) ? iTempIndex : 0;
	m_cboOsdCharType.SetCurSel(iTempIndex);

	m_cboSort.ResetContent();
	m_cboSort.SetItemData(m_cboSort.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_DISABLE)), 0);
	m_cboSort.SetItemData(m_cboSort.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_ENABLE)), 1);
	m_cboSort.SetCurSel(0);

	m_cboEnhance.ResetContent();
	m_cboEnhance.SetItemData(m_cboEnhance.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_DISABLE)), 0);
	m_cboEnhance.SetItemData(m_cboEnhance.AddString(GetTextEx(IDS_CONFIG_FTP_SNAPSHOT_ENABLE)), 1);
	m_cboEnhance.SetCurSel(0);

	int iCurSel = m_cboOsdType.GetCurSel();
	m_cboOsdType.ResetContent();
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("通道名称"),_T("Channel Name"))), 0);
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("日期时间"),_T("Date Time "))), 1);
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("附加字符1"),_T("Additional character1"))), 2);
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("附加字符2"),_T("Additional character2"))), 3);
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("附加字符3"),_T("Additional character3"))), 4);
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("附加字符4"),_T("Additional character4"))), 5);
	m_cboOsdType.SetItemData(m_cboOsdType.AddString(GetTextByLan(_T("附加字符5"),_T("Additional character5"))), 6);
	m_cboOsdType.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
}

void CLS_OSDBackgroundPage::OnBnClickedBtnSelectColor()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	CColorDialog clsColorDlg;
	int iRet = (int)clsColorDlg.DoModal();
	if (IDOK == iRet)
	{
		m_iColor = clsColorDlg.GetColor();
		try
		{
			m_btnOSDColor.RemoveChild(0);
			m_btnOSDColor.AddChild(0);
			m_btnOSDColor.SetColor(0,RGB(GetRValue(m_iColor), GetGValue(m_iColor),GetBValue(m_iColor)));
		}
		catch (CException* e)
		{
			e->ReportError();
			e->Delete();
		}
	}
}

void CLS_OSDBackgroundPage::OnBnClickedBtnSetOsdBackGround()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if (m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid ChannelNo(%d)",m_iChannelNo);
		return;
	}

	OSDExtraInfo tOSDExtraInfo = {0};
	tOSDExtraInfo.iSize = sizeof(OSDExtraInfo);
	tOSDExtraInfo.iOsdType = (int)m_cboOsdCharType.GetItemData(m_cboOsdCharType.GetCurSel());
	tOSDExtraInfo.iBgColor = m_iColor;
	tOSDExtraInfo.iAutoLine = (int)m_cboSort.GetItemData(m_cboSort.GetCurSel());
	tOSDExtraInfo.iCharEnhance = (int)m_cboEnhance.GetItemData(m_cboEnhance.GetCurSel());
	tOSDExtraInfo.iDigitNum = GetDlgItemInt(IDC_EDT_OSD_NUM);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_OSD_EXTRA_INFO, m_iChannelNo, &tOSDExtraInfo, sizeof(OSDExtraInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig[_OSDEXTRAINFO](%d,%d)", m_iLogonID, m_iChannelNo);
		return;
	}

	AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[_OSDEXTRAINFO](%d,%d)", m_iLogonID, m_iChannelNo);
}

void CLS_OSDBackgroundPage::UI_UpdateOsdBackGroundParam()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if (m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid ChannelNo(%d)",m_iChannelNo);
		return;
	}

	OSDExtraInfo tOSDExtraInfo = {0};
	tOSDExtraInfo.iSize = sizeof(OSDExtraInfo);
	tOSDExtraInfo.iOsdType = (int)m_cboOsdCharType.GetItemData(m_cboOsdCharType.GetCurSel());
	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_OSD_EXTRA_INFO, m_iChannelNo, &tOSDExtraInfo, sizeof(OSDExtraInfo), &iByteReturn);
	
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig[_OSDEXTRAINFO](%d,%d)", m_iLogonID, m_iChannelNo);
		return;
	}

	AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[_OSDEXTRAINFO](%d,%d)", m_iLogonID, m_iChannelNo);

	m_iColor = tOSDExtraInfo.iBgColor;
	try
	{ 
		m_btnOSDColor.RemoveChild(0);
		m_btnOSDColor.AddChild(0);
		m_btnOSDColor.SetColor(0,RGB(GetRValue(m_iColor), GetGValue(m_iColor),GetBValue(m_iColor)));
	}
	catch (CException* e)
	{
		e->ReportError();
		e->Delete();
	}
	
	m_cboSort.SetCurSel(GetCboSel(&m_cboSort, tOSDExtraInfo.iAutoLine));
	m_cboEnhance.SetCurSel(GetCboSel(&m_cboEnhance, tOSDExtraInfo.iCharEnhance));
	SetDlgItemInt(IDC_EDT_OSD_NUM, tOSDExtraInfo.iDigitNum);
}

void CLS_OSDBackgroundPage::OnCbnSelchangeCboChType()
{
	UI_UpdateOsdBackGroundParam();
}

void CLS_OSDBackgroundPage::OnBnClickedButtonOsdbclaritySend()
{
	// TODO: Add control notification handler code here
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}
	
	if (m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid ChannelNo(%d)",m_iChannelNo);
		return;
	}

	OsdClarity tOsdClarity = {0};
	tOsdClarity.iSize = sizeof(OsdClarity);
	tOsdClarity.iOsdType = (int)m_cboOsdType.GetItemData(m_cboOsdType.GetCurSel());
	tOsdClarity.iBClarity = GetDlgItemInt(IDC_EDIT_BCLARITY);
	tOsdClarity.iFClarity = GetDlgItemInt(IDC_EDIT_FCLARITY);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_OSD_CLARITY, m_iChannelNo, &tOsdClarity, sizeof(OsdClarity));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig[OSDCLARITY](%d,%d)", m_iLogonID, m_iChannelNo);
		return;
	}

	AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[OSDCLARITY](%d,%d)", m_iLogonID, m_iChannelNo);
}

void CLS_OSDBackgroundPage::UI_UpdateOsdClarity()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid logon id(%d)",m_iLogonID);
		return;
	}

	if (m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","Invalid ChannelNo(%d)",m_iChannelNo);
		return;
	}

	OsdClarity tOsdClarity = {0};
	tOsdClarity.iOsdType = m_cboOsdType.GetCurSel();
	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_OSD_CLARITY, m_iChannelNo, &tOsdClarity, sizeof(tOsdClarity), &iByteReturn);

	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig[_OSDEXTRAINFO](%d,%d)", m_iLogonID, m_iChannelNo);
		return;
	}

	AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[_OSDEXTRAINFO](%d,%d)", m_iLogonID, m_iChannelNo);
	
	//m_cboOsdType.SetCurSel(tOsdClarity.iOsdType);
	SetDlgItemInt(IDC_EDIT_BCLARITY,tOsdClarity.iBClarity);
	SetDlgItemInt(IDC_EDIT_FCLARITY,tOsdClarity.iFClarity);
		
}
void CLS_OSDBackgroundPage::OnCbnSelchangeComboOsdType()
{
	// TODO: Add control notification handler code here
	UI_UpdateOsdClarity();
}
