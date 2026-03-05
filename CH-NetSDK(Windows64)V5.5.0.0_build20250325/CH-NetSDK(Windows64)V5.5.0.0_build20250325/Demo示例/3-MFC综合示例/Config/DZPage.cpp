// Config/DZPage.cpp : implementation file
//

#include "stdafx.h"
#include "DZPage.h"


// CLS_DZPage dialog

IMPLEMENT_DYNAMIC(CLS_DZPage, CDialog)

CLS_DZPage::CLS_DZPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DZPage::IDD, pParent)
{
	m_iLogonID = -1;
}

CLS_DZPage::~CLS_DZPage()
{
}

void CLS_DZPage::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_PARAM1, m_edtParam1);
	DDX_Control(pDX, IDC_EDIT_PARAM2, m_edtParam2);
	DDX_Control(pDX, IDC_EDIT_PARAM3, m_edtParam3);
	DDX_Control(pDX, IDC_EDIT_PARAM4, m_edtParam4);
	DDX_Control(pDX, IDC_EDIT_PARAM5, m_edtParam5);
	DDX_Control(pDX, IDC_EDIT_PARAM6, m_edtParam6);
	DDX_Control(pDX, IDC_EDIT_PARAM7, m_edtParam7);
	DDX_Control(pDX, IDC_EDIT_PARAM8, m_edtParam8);
	DDX_Control(pDX, IDC_EDIT_PARAM9, m_edtParam9);
	DDX_Control(pDX, IDC_EDIT_PARAM10, m_edtParam10);
	DDX_Control(pDX, IDC_EDIT_PARAM11, m_edtParam11);
	DDX_Control(pDX, IDC_EDIT_PARAM12, m_edtParam12);
	DDX_Control(pDX, IDC_EDIT_PARAM13, m_edtParam13);
	DDX_Control(pDX, IDC_EDIT_PARAM14, m_edtParam14);
	DDX_Control(pDX, IDC_EDIT_PARAM15, m_edtParam15);
	DDX_Control(pDX, IDC_EDIT_PARAM16, m_edtParam16);
	DDX_Control(pDX, IDC_EDIT_PARAM17, m_edtParam17);
	DDX_Control(pDX, IDC_EDIT_PARAM18, m_edtParam18);
	DDX_Control(pDX, IDC_EDIT_PARAM19, m_edtParam19);
	DDX_Control(pDX, IDC_EDIT_PARAM20, m_edtParam20);
	DDX_Control(pDX, IDC_EDIT_TYPE, m_edtParamType);
	DDX_Control(pDX, IDC_EDIT_COUNT, m_edtParamCount);
	DDX_Control(pDX, IDC_CHECK_DZTRANS_SYNC, m_chkDzTransSync);
	DDX_Control(pDX, IDC_CHECK_DZTRANS_CODING, m_chkDzTranscoding);
	DDX_Control(pDX, IDC_CHECK_DZTRANS_NVR_LOCAL, m_chkDzTransParaNvrLocal);
}


BEGIN_MESSAGE_MAP(CLS_DZPage, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_DZSET, &CLS_DZPage::OnBnClickedButtonDzset)
	ON_BN_CLICKED(IDC_BUTTON_DZSET_DZTRANS, &CLS_DZPage::OnBnClickedButtonDzsetDztrans)
	ON_BN_CLICKED(IDC_BUTTON_GET_DZTRANSPARA, &CLS_DZPage::OnBnClickedButtonGetDztranspara)
END_MESSAGE_MAP()


// CLS_DZPage message handlers

BOOL CLS_DZPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_iLogonID = -1;

	m_edtParam1.SetLimitText(63);
	m_edtParam2.SetLimitText(63);
	m_edtParam3.SetLimitText(63);
	m_edtParam4.SetLimitText(63);
	m_edtParam5.SetLimitText(63);
	m_edtParam6.SetLimitText(63);
	m_edtParam7.SetLimitText(63);
	m_edtParam8.SetLimitText(63);
	m_edtParam9.SetLimitText(63);
	m_edtParam10.SetLimitText(63);
	m_edtParam11.SetLimitText(63);
	m_edtParam12.SetLimitText(63);
	m_edtParam13.SetLimitText(63);
	m_edtParam14.SetLimitText(63);
	m_edtParam15.SetLimitText(63);
	m_edtParam16.SetLimitText(63);
	m_edtParam17.SetLimitText(63);
	m_edtParam18.SetLimitText(63);
	m_edtParam19.SetLimitText(63);
	m_edtParam20.SetLimitText(63);

	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS1))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS2))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS3))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS4))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS5))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS6))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS7))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS8))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS9))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS10))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS11))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS12))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS13))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS14))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS15))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS16))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS17))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS18))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS19))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS20))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS21))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS22))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS23))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS24))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS25))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS26))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS27))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS28))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS29))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS30))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS31))->SetLimitText(LEN_128 - 1);
	((CEdit*)GetDlgItem(IDC_EDIT_PARAM_DZTRANS32))->SetLimitText(LEN_128 - 1);
	m_chkDzTransSync.SetCheck(BST_CHECKED);
	m_chkDzTranscoding.SetCheck(BST_CHECKED);

	UI_UpdateDialog();
	return TRUE;
}

void CLS_DZPage::OnChannelChanged( int _iLogonID, int _iChannelNo, int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	if (m_iLogonID < 0) {
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	}

	m_iChannelNO = _iChannelNo;
	if (m_iChannelNO < 0) {
		m_iChannelNO = 0;
	}

	DZCommonEx tDZInfo;
	memset(&tDZInfo, 0, sizeof(DZCommonEx));
    tDZInfo.iBufSize = sizeof(tDZInfo);
    CString strParamType;
    m_edtParamType.GetWindowText(strParamType);
    tDZInfo.iDzType = StrToInt(strParamType);
	int iRet = -1;
    int iByteReturn = 0;
    iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_DZ_COMMON_EX, m_iChannelNO, &tDZInfo, sizeof(tDZInfo), &iByteReturn);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDZInfo(%d)", m_iLogonID);
		m_edtParam1.SetWindowText(tDZInfo.cDzParam[0]); 
		m_edtParam2.SetWindowText(tDZInfo.cDzParam[1]);  
		m_edtParam3.SetWindowText(tDZInfo.cDzParam[2]);  
		m_edtParam4.SetWindowText(tDZInfo.cDzParam[3]);  
		m_edtParam5.SetWindowText(tDZInfo.cDzParam[4]);  
		m_edtParam6.SetWindowText(tDZInfo.cDzParam[5]);  
		m_edtParam7.SetWindowText(tDZInfo.cDzParam[6]);  
		m_edtParam8.SetWindowText(tDZInfo.cDzParam[7]);  
		m_edtParam9.SetWindowText(tDZInfo.cDzParam[8]);  
		m_edtParam10.SetWindowText(tDZInfo.cDzParam[9]);  
		m_edtParam11.SetWindowText(tDZInfo.cDzParam[10]);  
		m_edtParam12.SetWindowText(tDZInfo.cDzParam[11]);  
		m_edtParam13.SetWindowText(tDZInfo.cDzParam[12]);  
		m_edtParam14.SetWindowText(tDZInfo.cDzParam[13]);  
		m_edtParam15.SetWindowText(tDZInfo.cDzParam[14]);  
		m_edtParam16.SetWindowText(tDZInfo.cDzParam[15]);  
		m_edtParam17.SetWindowText(tDZInfo.cDzParam[16]);  
		m_edtParam18.SetWindowText(tDZInfo.cDzParam[17]);  
		m_edtParam19.SetWindowText(tDZInfo.cDzParam[18]);
		m_edtParam20.SetWindowText(tDZInfo.cDzParam[19]);  
        m_edtParamType.SetWindowText(IntToCString(tDZInfo.iDzType));
        m_edtParamCount.SetWindowText(IntToCString(tDZInfo.iParamNum));
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDZInfo(%d)", m_iLogonID);
	}

	SetDlgItemInt(IDC_EDIT_TYPE_DZTRANS, 0);
	SetDlgItemInt(IDC_EDIT_COUNT_DZTRANS, 0);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS1)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS2)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS3)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS4)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS5)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS6)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS7)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS8)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS9)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS10)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS11)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS12)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS13)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS14)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS15)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS16)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS17)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS18)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS19)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS20)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS21)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS22)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS23)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS24)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS25)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS26)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS27)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS28)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS29)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS30)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS31)->SetWindowText("");
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS32)->SetWindowText("");
}

void CLS_DZPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();		
}

void CLS_DZPage::OnMainNotify(int _iLogonID, int _iWParam, void* _pvLParam, void* _pvUser)
{
	int iMsgType = LOWORD(_iWParam);
	if(WCM_DZ_TRANSPARENT == iMsgType && NULL != _pvLParam) {
		DzTransparentPara* ptPara = (DzTransparentPara*)_pvLParam;
		UpdateDzTransParaToUI(ptPara);
		AddLog(LOG_TYPE_MSG, "", "WCM_DZ_TRANSPARENT: ch=%d,type=%d,count=%d", ptPara->iChanNo, ptPara->iParaType, ptPara->iParaCount);
		SAFE_FREE(ptPara);
	}
}

void CLS_DZPage::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pvPara, int _iUser)
{
	if (_iLogonID != m_iLogonID) {
		return;
	}

	switch (_iParaType)
	{
	case PARA_DZSET_TRANSPARENT:
		if (NULL != _pvPara) {
			DzTransparentPara* ptPara = (DzTransparentPara*)_pvPara;
			AddLog(LOG_TYPE_MSG, "", "PARA_DZ_TRANSPARENT:ch=%d,type=%d,count=%d", ptPara->iChanNo, ptPara->iParaType, ptPara->iParaCount);
		}
		break;
	default:
		break;
	}
}

void CLS_DZPage::OnBnClickedButtonDzset()
{
	DZCommonEx tDZInfo;
	memset(&tDZInfo, 0, sizeof(tDZInfo));
    tDZInfo.iBufSize = sizeof(tDZInfo);
    CString strParamCount;
    m_edtParamCount.GetWindowText(strParamCount);
    tDZInfo.iParamNum = StrToInt(strParamCount);
    CString strParamType;
    m_edtParamType.GetWindowText(strParamType);
    tDZInfo.iDzType = StrToInt(strParamType);
	CString strParam1;
	m_edtParam1.GetWindowText(strParam1);
	strcpy_s(tDZInfo.cDzParam[0], sizeof(tDZInfo.cDzParam[0]), (LPSTR)(LPCTSTR)strParam1);
	CString strParam2;
	m_edtParam2.GetWindowText(strParam2);
	strcpy_s(tDZInfo.cDzParam[1], sizeof(tDZInfo.cDzParam[1]), (LPSTR)(LPCTSTR)strParam2);
	CString strParam3;
	m_edtParam3.GetWindowText(strParam3);
	strcpy_s(tDZInfo.cDzParam[2], sizeof(tDZInfo.cDzParam[2]), (LPSTR)(LPCTSTR)strParam3);
	CString strParam4;
	m_edtParam4.GetWindowText(strParam4);
	strcpy_s(tDZInfo.cDzParam[3], sizeof(tDZInfo.cDzParam[3]), (LPSTR)(LPCTSTR)strParam4);
	CString strParam5 ;
	m_edtParam5.GetWindowText(strParam5);
	strcpy_s(tDZInfo.cDzParam[4], sizeof(tDZInfo.cDzParam[4]), (LPSTR)(LPCTSTR)strParam5);
	CString strParam6;
	m_edtParam6.GetWindowText(strParam6);
	strcpy_s(tDZInfo.cDzParam[5], sizeof(tDZInfo.cDzParam[5]), (LPSTR)(LPCTSTR)strParam6);
	CString strParam7;
	m_edtParam7.GetWindowText(strParam7);
	strcpy_s(tDZInfo.cDzParam[6], sizeof(tDZInfo.cDzParam[6]), (LPSTR)(LPCTSTR)strParam7);
	CString strParam8;
	m_edtParam8.GetWindowText(strParam8);
	strcpy_s(tDZInfo.cDzParam[7], sizeof(tDZInfo.cDzParam[7]), (LPSTR)(LPCTSTR)strParam8);
	CString strParam9;
	m_edtParam9.GetWindowText(strParam9);
	strcpy_s(tDZInfo.cDzParam[8], sizeof(tDZInfo.cDzParam[8]), (LPSTR)(LPCTSTR)strParam9);
	CString strParam10;
	m_edtParam10.GetWindowText(strParam10);
	strcpy_s(tDZInfo.cDzParam[9], sizeof(tDZInfo.cDzParam[9]), (LPSTR)(LPCTSTR)strParam10);
	CString strParam11;
	m_edtParam11.GetWindowText(strParam11);
	strcpy_s(tDZInfo.cDzParam[10], sizeof(tDZInfo.cDzParam[10]), (LPSTR)(LPCTSTR)strParam11);
	CString strParam12;
	m_edtParam12.GetWindowText(strParam12);
	strcpy_s(tDZInfo.cDzParam[11], sizeof(tDZInfo.cDzParam[11]), (LPSTR)(LPCTSTR)strParam12);
	CString strParam13;
	m_edtParam13.GetWindowText(strParam13);
	strcpy_s(tDZInfo.cDzParam[12], sizeof(tDZInfo.cDzParam[12]), (LPSTR)(LPCTSTR)strParam13);
	CString strParam14;
	m_edtParam14.GetWindowText(strParam14);
	strcpy_s(tDZInfo.cDzParam[13], sizeof(tDZInfo.cDzParam[13]), (LPSTR)(LPCTSTR)strParam14);
	CString strParam15;
	m_edtParam15.GetWindowText(strParam15);
	strcpy_s(tDZInfo.cDzParam[14], sizeof(tDZInfo.cDzParam[14]), (LPSTR)(LPCTSTR)strParam15);
	CString strParam16;
	m_edtParam16.GetWindowText(strParam16);
	strcpy_s(tDZInfo.cDzParam[15], sizeof(tDZInfo.cDzParam[15]), (LPSTR)(LPCTSTR)strParam16);
	CString strParam17;
	m_edtParam17.GetWindowText(strParam17);
	strcpy_s(tDZInfo.cDzParam[16], sizeof(tDZInfo.cDzParam[16]), (LPSTR)(LPCTSTR)strParam17);
	CString strParam18;
	m_edtParam18.GetWindowText(strParam18);
	strcpy_s(tDZInfo.cDzParam[17], sizeof(tDZInfo.cDzParam[17]), (LPSTR)(LPCTSTR)strParam18);
	CString strParam19;
	m_edtParam19.GetWindowText(strParam19);
	strcpy_s(tDZInfo.cDzParam[18], sizeof(tDZInfo.cDzParam[18]), (LPSTR)(LPCTSTR)strParam19);
	CString strParam20;
	m_edtParam20.GetWindowText(strParam20);
	strcpy_s(tDZInfo.cDzParam[19], sizeof(tDZInfo.cDzParam[19]), (LPSTR)(LPCTSTR)strParam20);

	int iRet;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_DZ_COMMON_EX, m_iChannelNO, &tDZInfo, sizeof(tDZInfo));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig(%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig(%d)", m_iLogonID);
	}
}

void CLS_DZPage::UI_UpdateDialog()
{
	SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan("参数类型", "ParaType"));
	SetDlgItemText(IDC_STATIC_COUNT, GetTextByLan("参数个数", "ParaCount"));
	SetDlgItemTextEx(IDC_STATIC_PARAM1, IDS_CONFIG_DZ_PARAM1);
	SetDlgItemTextEx(IDC_STATIC_PARAM2, IDS_CONFIG_DZ_PARAM2);
	SetDlgItemTextEx(IDC_STATIC_PARAM3, IDS_CONFIG_DZ_PARAM3);
	SetDlgItemTextEx(IDC_STATIC_PARAM4, IDS_CONFIG_DZ_PARAM4);
	SetDlgItemTextEx(IDC_STATIC_PARAM5, IDS_CONFIG_DZ_PARAM5);
	SetDlgItemTextEx(IDC_STATIC_PARAM6, IDS_CONFIG_DZ_PARAM6);
	SetDlgItemTextEx(IDC_STATIC_PARAM7, IDS_CONFIG_DZ_PARAM7);
	SetDlgItemTextEx(IDC_STATIC_PARAM8, IDS_CONFIG_DZ_PARAM8);
	SetDlgItemTextEx(IDC_STATIC_PARAM9, IDS_CONFIG_DZ_PARAM9);
	SetDlgItemTextEx(IDC_STATIC_PARAM10, IDS_CONFIG_DZ_PARAM10);
	SetDlgItemTextEx(IDC_STATIC_PARAM11, IDS_CONFIG_DZ_PARAM11);
	SetDlgItemTextEx(IDC_STATIC_PARAM12, IDS_CONFIG_DZ_PARAM12);
	SetDlgItemTextEx(IDC_STATIC_PARAM13, IDS_CONFIG_DZ_PARAM13);
	SetDlgItemTextEx(IDC_STATIC_PARAM14, IDS_CONFIG_DZ_PARAM14);
	SetDlgItemTextEx(IDC_STATIC_PARAM15, IDS_CONFIG_DZ_PARAM15);
	SetDlgItemTextEx(IDC_STATIC_PARAM16, IDS_CONFIG_DZ_PARAM16);
	SetDlgItemTextEx(IDC_STATIC_PARAM17, IDS_CONFIG_DZ_PARAM17);
	SetDlgItemTextEx(IDC_STATIC_PARAM18, IDS_CONFIG_DZ_PARAM18);
	SetDlgItemTextEx(IDC_STATIC_PARAM19, IDS_CONFIG_DZ_PARAM19);
	SetDlgItemTextEx(IDC_STATIC_PARAM20, IDS_CONFIG_DZ_PARAM20);
	SetDlgItemTextEx(IDC_BUTTON_DZSET, IDS_CONFIG_DZ_SET);

	SetDlgItemText(IDC_STATIC_TYPE_DZTRANS, GetTextByLan("参数类型", "ParaType"));
	SetDlgItemText(IDC_STATIC_COUNT_DZTRANS, GetTextByLan("参数个数", "ParaCount"));
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS1, IDS_CONFIG_DZ_PARAM1);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS2, IDS_CONFIG_DZ_PARAM2);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS3, IDS_CONFIG_DZ_PARAM3);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS4, IDS_CONFIG_DZ_PARAM4);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS5, IDS_CONFIG_DZ_PARAM5);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS6, IDS_CONFIG_DZ_PARAM6);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS7, IDS_CONFIG_DZ_PARAM7);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS8, IDS_CONFIG_DZ_PARAM8);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS9, IDS_CONFIG_DZ_PARAM9);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS10, IDS_CONFIG_DZ_PARAM10);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS11, IDS_CONFIG_DZ_PARAM11);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS12, IDS_CONFIG_DZ_PARAM12);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS13, IDS_CONFIG_DZ_PARAM13);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS14, IDS_CONFIG_DZ_PARAM14);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS15, IDS_CONFIG_DZ_PARAM15);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS16, IDS_CONFIG_DZ_PARAM16);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS17, IDS_CONFIG_DZ_PARAM17);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS18, IDS_CONFIG_DZ_PARAM18);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS19, IDS_CONFIG_DZ_PARAM19);
	SetDlgItemTextEx(IDC_STATIC_PARAM_DZTRANS20, IDS_CONFIG_DZ_PARAM20);
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS21, GetTextByLan("参数21", "Param21"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS22, GetTextByLan("参数22", "Param22"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS23, GetTextByLan("参数23", "Param23"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS24, GetTextByLan("参数24", "Param24"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS25, GetTextByLan("参数25", "Param25"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS26, GetTextByLan("参数26", "Param26"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS27, GetTextByLan("参数27", "Param27"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS28, GetTextByLan("参数28", "Param28"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS29, GetTextByLan("参数29", "Param29"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS30, GetTextByLan("参数30", "Param30"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS31, GetTextByLan("参数31", "Param31"));
	SetDlgItemText(IDC_STATIC_PARAM_DZTRANS32, GetTextByLan("参数32", "Param32"));
	SetDlgItemTextEx(IDC_BUTTON_DZSET_DZTRANS, IDS_CONFIG_DZ_SET);
	SetDlgItemText(IDC_BUTTON_GET_DZTRANSPARA, GetTextByLan("获取", "Get"));
	SetDlgItemText(IDC_CHECK_DZTRANS_SYNC, GetTextByLan("同步调用", "Synchronous"));
	SetDlgItemText(IDC_CHECK_DZTRANS_CODING, GetTextByLan("字符转码", "Transcoding"));
	SetDlgItemText(IDC_CHECK_DZTRANS_NVR_LOCAL, GetTextByLan("nvr本地", "NvrLocal"));
}

void CLS_DZPage::OnBnClickedButtonDzsetDztrans()
{
	DzTransparentPara tDzTransPara;
	memset(&tDzTransPara,0,sizeof(DzTransparentPara));
	if (BST_CHECKED == m_chkDzTransParaNvrLocal.GetCheck()) {
		tDzTransPara.iChanNo = PARAM_CHANNEL_ALL;
	} else {
		tDzTransPara.iChanNo = m_iChannelNO;
	}
	tDzTransPara.iParaType = GetDlgItemInt(IDC_EDIT_TYPE_DZTRANS);
	tDzTransPara.iParaCount = GetDlgItemInt(IDC_EDIT_COUNT_DZTRANS);

	CString strParam1;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS1)->GetWindowText(strParam1);
	strcpy_s(tDzTransPara.cDzParam[0], sizeof(tDzTransPara.cDzParam[0]), (LPSTR)(LPCTSTR)strParam1);
	CString strParam2;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS2)->GetWindowText(strParam2);
	strcpy_s(tDzTransPara.cDzParam[1], sizeof(tDzTransPara.cDzParam[1]), (LPSTR)(LPCTSTR)strParam2);
	CString strParam3;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS3)->GetWindowText(strParam3);
	strcpy_s(tDzTransPara.cDzParam[2], sizeof(tDzTransPara.cDzParam[2]), (LPSTR)(LPCTSTR)strParam3);
	CString strParam4;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS4)->GetWindowText(strParam4);
	strcpy_s(tDzTransPara.cDzParam[3], sizeof(tDzTransPara.cDzParam[3]), (LPSTR)(LPCTSTR)strParam4);
	CString strParam5 ;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS5)->GetWindowText(strParam5);
	strcpy_s(tDzTransPara.cDzParam[4], sizeof(tDzTransPara.cDzParam[4]), (LPSTR)(LPCTSTR)strParam5);
	CString strParam6;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS6)->GetWindowText(strParam6);
	strcpy_s(tDzTransPara.cDzParam[5], sizeof(tDzTransPara.cDzParam[5]), (LPSTR)(LPCTSTR)strParam6);
	CString strParam7;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS7)->GetWindowText(strParam7);
	strcpy_s(tDzTransPara.cDzParam[6], sizeof(tDzTransPara.cDzParam[6]), (LPSTR)(LPCTSTR)strParam7);
	CString strParam8;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS8)->GetWindowText(strParam8);
	strcpy_s(tDzTransPara.cDzParam[7], sizeof(tDzTransPara.cDzParam[7]), (LPSTR)(LPCTSTR)strParam8);
	CString strParam9;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS9)->GetWindowText(strParam9);
	strcpy_s(tDzTransPara.cDzParam[8], sizeof(tDzTransPara.cDzParam[8]), (LPSTR)(LPCTSTR)strParam9);
	CString strParam10;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS10)->GetWindowText(strParam10);
	strcpy_s(tDzTransPara.cDzParam[9], sizeof(tDzTransPara.cDzParam[9]), (LPSTR)(LPCTSTR)strParam10);
	CString strParam11;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS11)->GetWindowText(strParam11);
	strcpy_s(tDzTransPara.cDzParam[10], sizeof(tDzTransPara.cDzParam[10]), (LPSTR)(LPCTSTR)strParam11);
	CString strParam12;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS12)->GetWindowText(strParam12);
	strcpy_s(tDzTransPara.cDzParam[11], sizeof(tDzTransPara.cDzParam[11]), (LPSTR)(LPCTSTR)strParam12);
	CString strParam13;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS13)->GetWindowText(strParam13);
	strcpy_s(tDzTransPara.cDzParam[12], sizeof(tDzTransPara.cDzParam[12]), (LPSTR)(LPCTSTR)strParam13);
	CString strParam14;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS14)->GetWindowText(strParam14);
	strcpy_s(tDzTransPara.cDzParam[13], sizeof(tDzTransPara.cDzParam[13]), (LPSTR)(LPCTSTR)strParam14);
	CString strParam15;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS15)->GetWindowText(strParam15);
	strcpy_s(tDzTransPara.cDzParam[14], sizeof(tDzTransPara.cDzParam[14]), (LPSTR)(LPCTSTR)strParam15);
	CString strParam16;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS16)->GetWindowText(strParam16);
	strcpy_s(tDzTransPara.cDzParam[15], sizeof(tDzTransPara.cDzParam[15]), (LPSTR)(LPCTSTR)strParam16);
	CString strParam17;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS17)->GetWindowText(strParam17);
	strcpy_s(tDzTransPara.cDzParam[16], sizeof(tDzTransPara.cDzParam[16]), (LPSTR)(LPCTSTR)strParam17);
	CString strParam18;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS18)->GetWindowText(strParam18);
	strcpy_s(tDzTransPara.cDzParam[17], sizeof(tDzTransPara.cDzParam[17]), (LPSTR)(LPCTSTR)strParam18);
	CString strParam19;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS19)->GetWindowText(strParam19);
	strcpy_s(tDzTransPara.cDzParam[18], sizeof(tDzTransPara.cDzParam[18]), (LPSTR)(LPCTSTR)strParam19);
	CString strParam20;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS20)->GetWindowText(strParam20);
	strcpy_s(tDzTransPara.cDzParam[19], sizeof(tDzTransPara.cDzParam[19]), (LPSTR)(LPCTSTR)strParam20);
	CString strParam21;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS21)->GetWindowText(strParam21);
	strcpy_s(tDzTransPara.cDzParam[20], sizeof(tDzTransPara.cDzParam[20]), (LPSTR)(LPCTSTR)strParam21);
	CString strParam22;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS22)->GetWindowText(strParam22);
	strcpy_s(tDzTransPara.cDzParam[21], sizeof(tDzTransPara.cDzParam[21]), (LPSTR)(LPCTSTR)strParam22);
	CString strParam23;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS23)->GetWindowText(strParam23);
	strcpy_s(tDzTransPara.cDzParam[22], sizeof(tDzTransPara.cDzParam[22]), (LPSTR)(LPCTSTR)strParam23);
	CString strParam24;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS24)->GetWindowText(strParam24);
	strcpy_s(tDzTransPara.cDzParam[23], sizeof(tDzTransPara.cDzParam[23]), (LPSTR)(LPCTSTR)strParam24);
	CString strParam25;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS25)->GetWindowText(strParam25);
	strcpy_s(tDzTransPara.cDzParam[24], sizeof(tDzTransPara.cDzParam[24]), (LPSTR)(LPCTSTR)strParam25);
	CString strParam26;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS26)->GetWindowText(strParam26);
	strcpy_s(tDzTransPara.cDzParam[25], sizeof(tDzTransPara.cDzParam[25]), (LPSTR)(LPCTSTR)strParam26);
	CString strParam27;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS27)->GetWindowText(strParam27);
	strcpy_s(tDzTransPara.cDzParam[26], sizeof(tDzTransPara.cDzParam[26]), (LPSTR)(LPCTSTR)strParam27);
	CString strParam28;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS28)->GetWindowText(strParam28);
	strcpy_s(tDzTransPara.cDzParam[27], sizeof(tDzTransPara.cDzParam[27]), (LPSTR)(LPCTSTR)strParam28);
	CString strParam29;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS29)->GetWindowText(strParam29);
	strcpy_s(tDzTransPara.cDzParam[28], sizeof(tDzTransPara.cDzParam[28]), (LPSTR)(LPCTSTR)strParam29);
	CString strParam30;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS30)->GetWindowText(strParam30);
	strcpy_s(tDzTransPara.cDzParam[29], sizeof(tDzTransPara.cDzParam[29]), (LPSTR)(LPCTSTR)strParam30);
	CString strParam31;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS31)->GetWindowText(strParam31);
	strcpy_s(tDzTransPara.cDzParam[30], sizeof(tDzTransPara.cDzParam[30]), (LPSTR)(LPCTSTR)strParam31);
	CString strParam32;
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS32)->GetWindowText(strParam32);
	strcpy_s(tDzTransPara.cDzParam[31], sizeof(tDzTransPara.cDzParam[31]), (LPSTR)(LPCTSTR)strParam32);

	bool blSync = true;
	if (BST_UNCHECKED == m_chkDzTransSync.GetCheck()) {
		blSync = false;
	}
	bool blTranscoding = true;
	if (BST_UNCHECKED == m_chkDzTranscoding.GetCheck()) {
		blTranscoding = false;
	}

	int iRet = NetClient_SetDzTransparentPara(m_iLogonID, &tDzTransPara, sizeof(tDzTransPara), blSync, blTranscoding);
	if (RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDzTransparentPara(%d, %d, %d)", m_iLogonID, m_iChannelNO, tDzTransPara.iParaType);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDzTransparentPara(%d, %d, %d)", m_iLogonID, m_iChannelNO, tDzTransPara.iParaType);
	}
}

void CLS_DZPage::OnBnClickedButtonGetDztranspara()
{
	DzTransparentPara tDzTransPara;
	memset(&tDzTransPara,0,sizeof(DzTransparentPara));
	if (BST_CHECKED == m_chkDzTransParaNvrLocal.GetCheck()) {
		tDzTransPara.iChanNo = PARAM_CHANNEL_ALL;
	} else {
		tDzTransPara.iChanNo = m_iChannelNO;
	}
	tDzTransPara.iParaType = GetDlgItemInt(IDC_EDIT_TYPE_DZTRANS);
	bool blSync = true;
	if (BST_UNCHECKED == m_chkDzTransSync.GetCheck()) {
		blSync = false;
	}
	bool blTranscoding = true;
	if (BST_UNCHECKED == m_chkDzTranscoding.GetCheck()) {
		blTranscoding = false;
	}
	int iRet = NetClient_GetDzTransparentPara(m_iLogonID, &tDzTransPara, sizeof(tDzTransPara), blSync, blTranscoding);
	if (RET_SUCCESS == iRet) {
		UpdateDzTransParaToUI(&tDzTransPara);
		AddLog(LOG_TYPE_SUCC, "", "NetClient_GetDzTransparentPara(%d, %d, %d)", m_iLogonID, m_iChannelNO, tDzTransPara.iParaType);
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_GetDzTransparentPara(%d, %d, %d)", m_iLogonID, m_iChannelNO, tDzTransPara.iParaType);
	}
}

void CLS_DZPage::UpdateDzTransParaToUI(DzTransparentPara* _ptDzTransPara)
{
	if (NULL == _ptDzTransPara) {
		return;
	}

	SetDlgItemInt(IDC_EDIT_TYPE_DZTRANS, _ptDzTransPara->iParaType);
	SetDlgItemInt(IDC_EDIT_COUNT_DZTRANS, _ptDzTransPara->iParaCount);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS1)->SetWindowText(_ptDzTransPara->cDzParam[0]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS2)->SetWindowText(_ptDzTransPara->cDzParam[1]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS3)->SetWindowText(_ptDzTransPara->cDzParam[2]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS4)->SetWindowText(_ptDzTransPara->cDzParam[3]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS5)->SetWindowText(_ptDzTransPara->cDzParam[4]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS6)->SetWindowText(_ptDzTransPara->cDzParam[5]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS7)->SetWindowText(_ptDzTransPara->cDzParam[6]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS8)->SetWindowText(_ptDzTransPara->cDzParam[7]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS9)->SetWindowText(_ptDzTransPara->cDzParam[8]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS10)->SetWindowText(_ptDzTransPara->cDzParam[9]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS11)->SetWindowText(_ptDzTransPara->cDzParam[10]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS12)->SetWindowText(_ptDzTransPara->cDzParam[11]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS13)->SetWindowText(_ptDzTransPara->cDzParam[12]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS14)->SetWindowText(_ptDzTransPara->cDzParam[13]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS15)->SetWindowText(_ptDzTransPara->cDzParam[14]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS16)->SetWindowText(_ptDzTransPara->cDzParam[15]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS17)->SetWindowText(_ptDzTransPara->cDzParam[16]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS18)->SetWindowText(_ptDzTransPara->cDzParam[17]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS19)->SetWindowText(_ptDzTransPara->cDzParam[18]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS20)->SetWindowText(_ptDzTransPara->cDzParam[19]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS21)->SetWindowText(_ptDzTransPara->cDzParam[20]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS22)->SetWindowText(_ptDzTransPara->cDzParam[21]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS23)->SetWindowText(_ptDzTransPara->cDzParam[22]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS24)->SetWindowText(_ptDzTransPara->cDzParam[23]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS25)->SetWindowText(_ptDzTransPara->cDzParam[24]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS26)->SetWindowText(_ptDzTransPara->cDzParam[25]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS27)->SetWindowText(_ptDzTransPara->cDzParam[26]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS28)->SetWindowText(_ptDzTransPara->cDzParam[27]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS29)->SetWindowText(_ptDzTransPara->cDzParam[28]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS30)->SetWindowText(_ptDzTransPara->cDzParam[29]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS31)->SetWindowText(_ptDzTransPara->cDzParam[30]);
	GetDlgItem(IDC_EDIT_PARAM_DZTRANS32)->SetWindowText(_ptDzTransPara->cDzParam[31]);
}
