// CLS_DlgCfgGPSLocation.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgCfgGPSLocation.h"


// CLS_DlgCfgGPSLocation dialog

IMPLEMENT_DYNAMIC(CLS_DlgCfgGPSLocation, CDialog)

CLS_DlgCfgGPSLocation::CLS_DlgCfgGPSLocation(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgCfgGPSLocation::IDD, pParent)
{

}

CLS_DlgCfgGPSLocation::~CLS_DlgCfgGPSLocation()
{
}

void CLS_DlgCfgGPSLocation::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_GPS_LONGITUDE, m_cboLongitude);
	DDX_Control(pDX, IDC_COMBO_GPS_LATITUDE, m_cboLatitude);
	DDX_Control(pDX, IDC_COMBO_GPS_MODE, m_cboMode);
	DDX_Control(pDX, IDC_CHECK_GPS_UPDATA, m_chkUpdata);
	DDX_Control(pDX, IDC_EDIT_GPS_INTERVAL, m_editInterval);
}


BEGIN_MESSAGE_MAP(CLS_DlgCfgGPSLocation, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_GPS_SET, &CLS_DlgCfgGPSLocation::OnBnClickedButtonGpsSet)
	ON_BN_CLICKED(IDC_BUTTON_GPS_INTERVAL_SET, &CLS_DlgCfgGPSLocation::OnBnClickedButtonIntervalSet)
END_MESSAGE_MAP()


// CLS_DlgCfgGPSLocation message handler

BOOL CLS_DlgCfgGPSLocation::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_DlgCfgGPSLocation::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_DlgCfgGPSLocation::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (m_iChannelNO < 0) {
		m_iChannelNO = 0;
	} else {
		m_iChannelNO = _iChannelNo;
	}

	if (_iStreamNo < 0) {
		m_iStreamNO = 0;
	} else {
		m_iStreamNO = _iStreamNo;
	}

	UpdatePageUI();
}

void CLS_DlgCfgGPSLocation::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_DlgCfgGPSLocation::UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_GPS_MODE, GetTextByLan(_T("模式选择"), _T("Select mode")));
	SetDlgItemText(IDC_STATIC_GPS_LONGITUDE, GetTextByLan(_T("经度"), _T("longitude")));
	SetDlgItemText(IDC_STATIC_GPS_LATITUDE, GetTextByLan(_T("纬度"), _T("latitude")));
	SetDlgItemText(IDC_STATIC_GPS_HEIGHT, GetTextByLan(_T("高程"), _T("elevation")));
	SetDlgItemText(IDC_STATIC_GPS_HEIGHT_MODIFY, GetTextByLan(_T("修正高程"), _T("The revised elevation")));
	SetDlgItemText(IDC_STATIC_GPS_INTERVAL, GetTextByLan(_T("GPS上报时间间隔(s)"), _T("GPS reporting interval (s)")));
	SetDlgItemText(IDC_CHECK_GPS_UPDATA, GetTextByLan(_T("实时更新"), _T("Updated in real time")));
	
	SetDlgItemText(IDC_STATIC_GPS_DEGREE_MODIFY, GetTextByLan(_T("修正经度"), _T("Fix longitude")));
	SetDlgItemText(IDC_STATIC_GPS_MINUTE_MODIFY, GetTextByLan(_T("修正经度分"), _T("Correct longitude")));
	SetDlgItemText(IDC_STATIC_GPS_SECOND_MODIFY, GetTextByLan(_T("修正经度秒"), _T("Fixed longitude seconds")));
	SetDlgItemText(IDC_STATIC_GPS_DEGREE2_MODIFY, GetTextByLan(_T("修正经度"), _T("Fix longitude")));
	SetDlgItemText(IDC_STATIC_GPS_MINUTE2_MODIFY, GetTextByLan(_T("修正经度分"), _T("Correct longitude")));
	SetDlgItemText(IDC_STATIC_GPS_SECOND2_MODIFY, GetTextByLan(_T("修正经度秒"), _T("Fixed longitude seconds")));

	SetDlgItemTextEx(IDC_BUTTON_GPS_SET, IDS_SET);
	SetDlgItemTextEx(IDC_BUTTON_GPS_INTERVAL_SET, IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_GPS_DEGREE, IDS_DEGREE);
	SetDlgItemTextEx(IDC_STATIC_GPS_DEGREE2, IDS_DEGREE);
	SetDlgItemTextEx(IDC_STATIC_GPS_MINUTE, IDS_MINUTE);
	SetDlgItemTextEx(IDC_STATIC_GPS_MINUTE2, IDS_MINUTE);
	SetDlgItemTextEx(IDC_STATIC_GPS_SECOND, IDS_SECOND);
	SetDlgItemTextEx(IDC_STATIC_GPS_SECOND2, IDS_SECOND);

	CString cstrMode[] = {GetTextByLan(_T("手动"), _T("Manual")), GetTextByLan(_T("自动"), _T("Auto"))};
	m_cboMode.ResetContent();
	for(int i=0; i<sizeof(cstrMode)/sizeof(CString); i++)
	{
		m_cboMode.InsertString(i, cstrMode[i]);
	}
	m_cboMode.SetCurSel(0);

	CString cstrLongitude[] = {GetTextByLan(_T("东"), _T("East")), GetTextByLan(_T("西"), _T("West"))};
	m_cboLongitude.ResetContent();
	for(int i=0; i<sizeof(cstrLongitude)/sizeof(CString); i++)
	{
		m_cboLongitude.InsertString(i, cstrLongitude[i]);
	}
	m_cboLongitude.SetCurSel(0);

	CString cstrLatitude[] = {GetTextByLan(_T("南"), _T("South")), GetTextByLan(_T("北"), _T("North"))};
	m_cboLatitude.ResetContent();
	for(int i=0; i<sizeof(cstrLatitude)/sizeof(CString); i++)
	{
		m_cboLatitude.InsertString(i, cstrLatitude[i]);
	}
	m_cboLatitude.SetCurSel(0);

	m_chkUpdata.SetCheck(TRUE);

	m_editInterval.SetLimitText(5);

}

void CLS_DlgCfgGPSLocation::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}
	
	UpdateGpsInterval();
	UpdateGpsInfo();
}
void CLS_DlgCfgGPSLocation::OnBnClickedButtonGpsSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	GeografhyLocation tInfo = {0};
	tInfo.iBufSize = sizeof(tInfo);
	
	tInfo.iType = m_cboMode.GetCurSel();
	tInfo.tLongitudeInfo.iDirection = m_cboLongitude.GetCurSel();
	tInfo.tLongitudeInfo.iDegree = GetDlgItemInt(IDC_EDIT_GPS_DEGREE);
	tInfo.tLongitudeInfo.iMinute = GetDlgItemInt(IDC_EDIT_GPS_MINUTE);
	tInfo.tLongitudeInfo.iSecond = GetDlgItemInt(IDC_EDIT_GPS_SECOND);

	tInfo.tLatitudeInfo.iDirection = m_cboLatitude.GetCurSel();
	tInfo.tLatitudeInfo.iDegree = GetDlgItemInt(IDC_EDIT_GPS_DEGREE2);
	tInfo.tLatitudeInfo.iMinute = GetDlgItemInt(IDC_EDIT_GPS_MINUTE2);
	tInfo.tLatitudeInfo.iSecond = GetDlgItemInt(IDC_EDIT_GPS_SECOND2);

	tInfo.iHeight = GetDlgItemInt(IDC_EDIT_GPS_HEIGHT);

	tInfo.tOffsetLongitudeInfo.iDegree = GetDlgItemInt(IDC_EDIT_GPS_DEGREE_MODIFY);
	tInfo.tOffsetLongitudeInfo.iMinute = GetDlgItemInt(IDC_EDIT_GPS_MINUTE_MODIFY);
	tInfo.tOffsetLongitudeInfo.iSecond = GetDlgItemInt(IDC_EDIT_GPS_SECOND_MODIFY);

	tInfo.tOffsetLatitudeInfo.iDegree = GetDlgItemInt(IDC_EDIT_GPS_DEGREE2_MODIFY);
	tInfo.tOffsetLatitudeInfo.iMinute = GetDlgItemInt(IDC_EDIT_GPS_MINUTE2_MODIFY);
	tInfo.tOffsetLatitudeInfo.iSecond = GetDlgItemInt(IDC_EDIT_GPS_SECOND2_MODIFY);

	tInfo.ioffSetHeight = GetDlgItemInt(IDC_EDIT_GPS_HEIGHT_MODIFY);

	int iRet = RET_FAILED;
	int iProductMode = 0;
	int iProductType = 0;
	NetClient_GetProductTypeEx(m_iLogonID, &iProductMode, &iProductType);
	if (IPCamera_PRODUCT == iProductType) {
		iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GEOGRAFHY_LOCATION, PARAM_CHANNEL_ALL, &tInfo, sizeof(tInfo));
	} else {
		iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GEOGRAFHY_LOCATION, m_iChannelNO, &tInfo, sizeof(tInfo));
	}
	if (iRet < 0) {
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCfgGPSLocation::NetClient_SetDevConfig[NET_CLIENT_GEOGRAFHY_LOCATION] (%d, %d)", m_iLogonID, m_iChannelNO);
	} else {
		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCfgGPSLocation::NetClient_GetDevConfig[NET_CLIENT_GEOGRAFHY_LOCATION] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_DlgCfgGPSLocation::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (_iLogonID < 0 || _iLogonID != m_iLogonID)
	{
		return;
	}

	//if (_iChannelNo == m_iChannelNO)//Only refresh the channel whose parameter has changed
	{
		switch(_iParaType)
		{
		case  PARA_GEOGRAFHY_LOCATION:
			{
				if(m_chkUpdata.GetCheck())
				{
					GeografhyLocation* ptInfo = (GeografhyLocation*)_pPara;
					if (NULL == ptInfo)
					{
						return;
					}
					m_cboMode.SetCurSel(ptInfo->iType);
					m_cboLongitude.SetCurSel(ptInfo->tLongitudeInfo.iDirection);
					SetDlgItemInt(IDC_EDIT_GPS_DEGREE, ptInfo->tLongitudeInfo.iDegree);
					SetDlgItemInt(IDC_EDIT_GPS_MINUTE, ptInfo->tLongitudeInfo.iMinute);
					SetDlgItemInt(IDC_EDIT_GPS_SECOND, ptInfo->tLongitudeInfo.iSecond);

					m_cboLatitude.SetCurSel(ptInfo->tLatitudeInfo.iDirection);
					SetDlgItemInt(IDC_EDIT_GPS_DEGREE2, ptInfo->tLatitudeInfo.iDegree);
					SetDlgItemInt(IDC_EDIT_GPS_MINUTE2, ptInfo->tLatitudeInfo.iMinute);
					SetDlgItemInt(IDC_EDIT_GPS_SECOND2, ptInfo->tLatitudeInfo.iSecond);

					SetDlgItemInt(IDC_EDIT_GPS_HEIGHT, ptInfo->iHeight);

					SetDlgItemInt(IDC_EDIT_GPS_DEGREE_MODIFY, ptInfo->tOffsetLongitudeInfo.iDegree);
					SetDlgItemInt(IDC_EDIT_GPS_MINUTE_MODIFY, ptInfo->tOffsetLongitudeInfo.iMinute);
					SetDlgItemInt(IDC_EDIT_GPS_SECOND_MODIFY, ptInfo->tOffsetLongitudeInfo.iSecond);

					SetDlgItemInt(IDC_EDIT_GPS_DEGREE2_MODIFY, ptInfo->tOffsetLatitudeInfo.iDegree);
					SetDlgItemInt(IDC_EDIT_GPS_MINUTE2_MODIFY, ptInfo->tOffsetLatitudeInfo.iMinute);
					SetDlgItemInt(IDC_EDIT_GPS_SECOND2_MODIFY, ptInfo->tOffsetLatitudeInfo.iSecond);
		
					SetDlgItemInt(IDC_EDIT_GPS_HEIGHT_MODIFY, ptInfo->ioffSetHeight);
				}
			}
			break;
		default:
			break;
		}
	}
}

void CLS_DlgCfgGPSLocation::OnBnClickedButtonIntervalSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	int iInterval = GetDlgItemInt(IDC_EDIT_GPS_INTERVAL);
	if (iInterval<3)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_DlgCfgGPSLocation::OnBnClickedButtonIntervalSet Please enter the effective range");
		return;
	}

	GPSTime tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChkInterval = iInterval;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_GPS_TIME, m_iChannelNO, &tInfo, sizeof(tInfo));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCfgGPSLocation::NetClient_SetDevConfig[NET_CLIENT_GPS_TIME] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCfgGPSLocation::NetClient_SetDevConfig[NET_CLIENT_GPS_TIME] (%d, %d)", m_iLogonID, m_iChannelNO);

	}
}

int CLS_DlgCfgGPSLocation::UpdateGpsInterval()
{
	int iBytesReturned;

	GPSTime tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChkInterval = GetDlgItemInt(IDC_EDIT_GPS_INTERVAL);

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GPS_TIME, m_iChannelNO, &tInfo, sizeof(tInfo),&iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCfgGPSLocation::NetClient_GetDevConfig[NET_CLIENT_GPS_TIME] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_GPS_INTERVAL, tInfo.iChkInterval);
	}

	return iRet;
}

int CLS_DlgCfgGPSLocation::UpdateGpsInfo()
{
	int iBytesReturned;

	GeografhyLocation tInfo = {0};
	tInfo.iBufSize = sizeof(tInfo);

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GEOGRAFHY_LOCATION, m_iChannelNO, &tInfo, sizeof(tInfo), &iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCfgGPSLocation::NetClient_GetDevConfig[NET_CLIENT_GEOGRAFHY_LOCATION] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		m_cboMode.SetCurSel(tInfo.iType);
		m_cboLongitude.SetCurSel(tInfo.tLongitudeInfo.iDirection);
		SetDlgItemInt(IDC_EDIT_GPS_DEGREE, tInfo.tLongitudeInfo.iDegree);
		SetDlgItemInt(IDC_EDIT_GPS_MINUTE, tInfo.tLongitudeInfo.iMinute);
		SetDlgItemInt(IDC_EDIT_GPS_SECOND, tInfo.tLongitudeInfo.iSecond);

		m_cboLatitude.SetCurSel(tInfo.tLatitudeInfo.iDirection);
		SetDlgItemInt(IDC_EDIT_GPS_DEGREE2, tInfo.tLatitudeInfo.iDegree);
		SetDlgItemInt(IDC_EDIT_GPS_MINUTE2, tInfo.tLatitudeInfo.iMinute);
		SetDlgItemInt(IDC_EDIT_GPS_SECOND2, tInfo.tLatitudeInfo.iSecond);

		SetDlgItemInt(IDC_EDIT_GPS_HEIGHT, tInfo.iHeight);

		SetDlgItemInt(IDC_EDIT_GPS_DEGREE_MODIFY, tInfo.tOffsetLongitudeInfo.iDegree);
		SetDlgItemInt(IDC_EDIT_GPS_MINUTE_MODIFY, tInfo.tOffsetLongitudeInfo.iMinute);
		SetDlgItemInt(IDC_EDIT_GPS_SECOND_MODIFY, tInfo.tOffsetLongitudeInfo.iSecond);

		SetDlgItemInt(IDC_EDIT_GPS_DEGREE2_MODIFY, tInfo.tOffsetLatitudeInfo.iDegree);
		SetDlgItemInt(IDC_EDIT_GPS_MINUTE2_MODIFY, tInfo.tOffsetLatitudeInfo.iMinute);
		SetDlgItemInt(IDC_EDIT_GPS_SECOND2_MODIFY, tInfo.tOffsetLatitudeInfo.iSecond);

		SetDlgItemInt(IDC_EDIT_GPS_HEIGHT_MODIFY, tInfo.ioffSetHeight);
	}

	return iRet;
}
