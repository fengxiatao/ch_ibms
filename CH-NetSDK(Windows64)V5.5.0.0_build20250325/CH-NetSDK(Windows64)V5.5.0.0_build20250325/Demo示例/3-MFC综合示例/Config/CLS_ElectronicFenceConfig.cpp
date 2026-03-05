// CLS_ElectronicFenceConfig.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_ElectronicFenceConfig.h"


// CLS_ElectronicFenceConfig dialog

IMPLEMENT_DYNAMIC(CLS_ElectronicFenceConfig, CDialog)

CLS_ElectronicFenceConfig::CLS_ElectronicFenceConfig(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ElectronicFenceConfig::IDD, pParent)
	, m_iRadius(0)
	, m_iLongiitudeDegree(0)
	, m_iLongiitudeMinute(0)
	, m_iLongiitudeSecond(0)
	, m_iLatitudeDegree(0)
	, m_iLatitudeMinute(0)
	, m_iLatitudeSecond(0)
	, m_cExtInfo(_T(""))
{

}

CLS_ElectronicFenceConfig::~CLS_ElectronicFenceConfig()
{
}

BOOL CLS_ElectronicFenceConfig::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateUIText();
	UpdateData(FALSE);
	OnBnClickedButtonGet();
	OnBnClickedButtonExtinfoGet();
	return TRUE;
}

void CLS_ElectronicFenceConfig::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
	OnBnClickedButtonGet();
	OnBnClickedButtonExtinfoGet();
}

void CLS_ElectronicFenceConfig::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;
	m_iStreamNO = _iStreamNo;
	m_iChannelNO = _iChannelNo;
	OnBnClickedButtonGet();
	OnBnClickedButtonExtinfoGet();
}

void CLS_ElectronicFenceConfig::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE, GetTextByLan(_T("电子围栏"), _T("Electronic fence")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_RADIUS, GetTextByLan(_T("围栏半径"), _T("Electronic fence radius")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LONGITUDE, GetTextByLan(_T("经度"), _T("longitude")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LONGITUDE_DEGREE, GetTextByLan(_T("经度-时"), _T("longitude degree")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LONGITUDE_MINUTE, GetTextByLan(_T("经度-分"), _T("longitude minute")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LONGITUDE_SECOND, GetTextByLan(_T("经度-秒"), _T("longitude second")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LATITUDE, GetTextByLan(_T("纬度"), _T("latitude")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LATITUDE_DEGREE, GetTextByLan(_T("纬度-时"), _T("latitude degree")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LATITUDE_MINUTE, GetTextByLan(_T("纬度-分"), _T("latitude minute")));
	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_LATITUDE_SECOND, GetTextByLan(_T("纬度-秒"), _T("latitude second")));

	m_comboIsEnbale.ResetContent();
	m_comboIsEnbale.AddString(GetTextByLan(_T("0-关闭"), _T("0-close")));
	m_comboIsEnbale.AddString(GetTextByLan(_T("1-开启"), _T("1-opening")));

	m_comboLongitude.ResetContent();
	m_comboLongitude.AddString(GetTextByLan(_T("0-东"), _T("0-east")));
	m_comboLongitude.AddString(GetTextByLan(_T("1-西"), _T("1-west")));

	m_comboLatitude.ResetContent();
	m_comboLatitude.AddString(GetTextByLan(_T("0-南"), _T("0-south")));
	m_comboLatitude.AddString(GetTextByLan(_T("1-北"), _T("1-north")));

	SetDlgItemText(IDC_BUTTON_ELECTRONIC_FENCE_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_ELECTRONIC_FENCE_GET, GetTextByLan(_T("获取"), _T("Get")));


	SetDlgItemText(IDC_STATIC_ELECTRONIC_FENCE_EXTINFO, GetTextByLan(_T("算法扩展信息"), _T("Extend info")));

	SetDlgItemText(IDC_BUTTON_EXTINFO_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_EXTINFO_GET, GetTextByLan(_T("获取"), _T("Get")));
}

void CLS_ElectronicFenceConfig::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_ELECTRONIC_FENCE, m_comboIsEnbale);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_RADIUS, m_iRadius);
	DDV_MinMaxInt(pDX, m_iRadius, 0, 10000000);

	DDX_Control(pDX, IDC_COMBO_ELECTRONIC_FENCE_LONGITUDE, m_comboLongitude);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_LONGITUDE_DEGREE, m_iLongiitudeDegree);
	DDV_MinMaxInt(pDX, m_iLongiitudeDegree, 0, 180);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_LONGITUDE_MINUTE, m_iLongiitudeMinute);
	DDV_MinMaxInt(pDX, m_iLongiitudeMinute, 0, 59);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_LONGITUDE_SECOND, m_iLongiitudeSecond);
	DDV_MinMaxInt(pDX, m_iLongiitudeSecond, 0, 5999);

	DDX_Control(pDX, IDC_COMBO_ELECTRONIC_FENCE_LATITUDE, m_comboLatitude);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_LATITUDE_DEGREE, m_iLatitudeDegree);
	DDV_MinMaxInt(pDX, m_iLatitudeDegree, 0, 90);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_LATITUDE_MINUTE, m_iLatitudeMinute);
	DDV_MinMaxInt(pDX, m_iLatitudeMinute, 0, 59);
	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_LATITUDE_SECOND, m_iLatitudeSecond);
	DDV_MinMaxInt(pDX, m_iLatitudeSecond, 0, 5999);

	DDX_Text(pDX, IDC_EDIT_ELECTRONIC_FENCE_EXTINFO, m_cExtInfo);
	DDV_MaxChars(pDX, m_cExtInfo, 1023);
}


BEGIN_MESSAGE_MAP(CLS_ElectronicFenceConfig, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_ELECTRONIC_FENCE_SET, &CLS_ElectronicFenceConfig::OnBnClickedButtonSet)
	ON_BN_CLICKED(IDC_BUTTON_ELECTRONIC_FENCE_GET, &CLS_ElectronicFenceConfig::OnBnClickedButtonGet)
	ON_BN_CLICKED(IDC_BUTTON_EXTINFO_SET, &CLS_ElectronicFenceConfig::OnBnClickedButtonExtinfoSet)
	ON_BN_CLICKED(IDC_BUTTON_EXTINFO_GET, &CLS_ElectronicFenceConfig::OnBnClickedButtonExtinfoGet)
END_MESSAGE_MAP()


void CLS_ElectronicFenceConfig::OnBnClickedButtonSet()
{
	UpdateData(TRUE);
	WaterElecFence tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	tInfo.iEnable =  m_comboIsEnbale.GetCurSel();
	tInfo.iRadius = m_iRadius;
	tInfo.iLongitude = m_comboLongitude.GetCurSel();
	tInfo.iLonDegree = m_iLongiitudeDegree;
	tInfo.iLonMinute = m_iLongiitudeMinute;
	tInfo.iLonSecond = m_iLongiitudeSecond;
	tInfo.iLatitude = m_comboLatitude.GetCurSel();
	tInfo.iLatDegree = m_iLatitudeDegree;
	tInfo.iLatMinute = m_iLatitudeMinute;
	tInfo.iLatSecond = m_iLatitudeSecond;

	//NET_CLIENT_IRRI_ELECFENCE:
	//int __stdcall NetClient_SetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void* _lpInBuffer, int _iInBufferSize);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRI_ELECFENCE, m_iChannelNO, &tInfo, sizeof(WaterElecFence));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_ElectronicFenceConfig::NetClient_SetDevConfig[NET_CLIENT_IRRI_ELECFENCE] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_ElectronicFenceConfig::NetClient_SetDevConfig[NET_CLIENT_IRRI_ELECFENCE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_ElectronicFenceConfig::OnBnClickedButtonGet()
{
	WaterElecFence tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	int iBytesReturned = 0;

	//NET_CLIENT_IRRI_ELECFENCE:
	//int __stdcall NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void* _lpOutBuffer, int _iOutBufferSize, int* _lpBytesReturned);
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IRRI_ELECFENCE, m_iChannelNO, &tInfo, sizeof(WaterElecFence), &iBytesReturned);
	if(RET_SUCCESS == iRet)
	{
		m_comboIsEnbale.SetCurSel(tInfo.iEnable);
		m_iRadius = tInfo.iRadius;
		m_comboLongitude.SetCurSel(tInfo.iLongitude);
		m_iLongiitudeDegree = tInfo.iLonDegree;
		m_iLongiitudeMinute = tInfo.iLonMinute;
		m_iLongiitudeSecond = tInfo.iLonSecond;
		m_comboLatitude.SetCurSel(tInfo.iLatitude);
		m_iLatitudeDegree = tInfo.iLatDegree;
		m_iLatitudeMinute = tInfo.iLatMinute;
		m_iLatitudeSecond = tInfo.iLatSecond;
		AddLog(LOG_TYPE_SUCC, "","CLS_ElectronicFenceConfig::NetClient_GetDevConfig[NET_CLIENT_IRRI_ELECFENCE] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
		UpdateData(FALSE);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_ElectronicFenceConfig::NetClient_GetDevConfig[NET_CLIENT_IRRI_ELECFENCE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_ElectronicFenceConfig::OnBnClickedButtonExtinfoSet()
{
	UpdateData(TRUE);
	IrriAlgExtInfo tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	strcpy_s(tInfo.cExtInfo,sizeof(tInfo.cExtInfo),m_cExtInfo.GetBuffer(0));

	//NET_CLIENT_IRRI_ALGEXTINFO:
	//int __stdcall NetClient_SetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void* _lpInBuffer, int _iInBufferSize);
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_IRRI_ALGEXTINFO, m_iChannelNO, &tInfo, sizeof(IrriAlgExtInfo));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_ElectronicFenceConfig::NetClient_SetDevConfig[NET_CLIENT_IRRI_ALGEXTINFO] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_ElectronicFenceConfig::NetClient_SetDevConfig[NET_CLIENT_IRRI_ALGEXTINFO] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_ElectronicFenceConfig::OnBnClickedButtonExtinfoGet()
{
	IrriAlgExtInfo tInfo;
	memset(&tInfo, 0, sizeof(tInfo));
	int iBytesReturned = 0;

	//NET_CLIENT_IRRI_ELECFENCE:
	//int __stdcall NetClient_GetDevConfig(int _iLogonID, int _iCommand, int _iChannel, void* _lpOutBuffer, int _iOutBufferSize, int* _lpBytesReturned);
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_IRRI_ALGEXTINFO, m_iChannelNO, &tInfo, sizeof(IrriAlgExtInfo), &iBytesReturned);
	if(RET_SUCCESS == iRet)
	{
		m_cExtInfo = tInfo.cExtInfo;
		AddLog(LOG_TYPE_SUCC, "","OnBnClickedButtonExtinfoGet::NetClient_GetDevConfig[NET_CLIENT_IRRI_ALGEXTINFO] (%d, %d),iResult = %d", m_iLogonID, m_iChannelNO, iRet);
		UpdateData(FALSE);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedButtonExtinfoGet::NetClient_GetDevConfig[NET_CLIENT_IRRI_ALGEXTINFO] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}
