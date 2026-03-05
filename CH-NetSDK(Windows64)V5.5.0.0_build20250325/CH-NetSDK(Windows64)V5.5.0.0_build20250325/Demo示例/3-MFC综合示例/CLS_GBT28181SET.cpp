// CLS_GBT28181SET.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_GBT28181SET.h"


// CLS_GBT28181SET dialog

IMPLEMENT_DYNAMIC(CLS_GBT28181Set, CDialog)
#define  MAX_REGVALIDITY		2000000000
#define  MIN_REGVALIDITY		1
#define  MAX_COMMUNCATION_PORT	65535
#define  MIN_COMMUNCATION_PORT	1 
CLS_GBT28181Set::CLS_GBT28181Set(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_GBT28181Set::IDD, pParent)
{

}

CLS_GBT28181Set::~CLS_GBT28181Set()
{
}

void CLS_GBT28181Set::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_ADDRESS, m_edtIPAddress);
	DDX_Control(pDX, IDC_EDIT_PORT, m_edtPort);
	DDX_Control(pDX, IDC_EDIT_SERVERID, m_edtServerID);
	DDX_Control(pDX, IDC_EDIT_DEVICEID, m_edtDeviceID);
	DDX_Control(pDX, IDC_EDIT_ACCOUNT, m_edtAccount);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_edtPassword);
	DDX_Control(pDX, IDC_EDIT_REGISTER_INDATE, m_edtRegValidity);
	DDX_Control(pDX, IDC_EDIT_LIVETIME, m_edtKeepalive);
	DDX_Control(pDX, IDC_EDIT_HEARTBEAT_INTERVAL, m_edtHeartBeatInterval);
	DDX_Control(pDX, IDC_EDIT_HEARTBEAT_COUNT, m_edtHeartBeatTimes);
	DDX_Control(pDX, IDC_CHECK_REGISTER, m_chkNeedReg);
	DDX_Control(pDX, IDC_CHECK_ENABLE, m_chkEnable);
	
}

BEGIN_MESSAGE_MAP(CLS_GBT28181Set, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_SAVE, &CLS_GBT28181Set::OnBnClickedButtonSave)
	ON_WM_SHOWWINDOW()
	ON_WM_CLOSE()
END_MESSAGE_MAP()


// CLS_GBT28181SET message handlers
BOOL CLS_GBT28181Set::OnInitDialog()
{
	CDialog::OnInitDialog();
	
	return TRUE;
}

void CLS_GBT28181Set::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CDialog::OnShowWindow(bShow, nStatus);
	
	Init();
}


void CLS_GBT28181Set::Init()
{
	DZ_INFO_PARAM vDZInfoGB;
	memset(&vDZInfoGB,0,sizeof(DZ_INFO_PARAM));

	int iRet = NetClient_GetDZInfo(m_iLogonID, &vDZInfoGB);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_GBT28181Set::Init][NetClient_GetDZInfo] (%d)",m_iLogonID);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_GBT28181Set::Init][NetClient_GetDZInfo](%d)",m_iLogonID);
	}
	CString cstrTempIP;
	cstrTempIP = vDZInfoGB.m_cParam1;
	m_edtIPAddress.SetWindowText(cstrTempIP);
	CString sTemp;
	int iServerPort = 0;
	sTemp = CString(vDZInfoGB.m_cParam2);
	iServerPort = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_PORT,iServerPort);

	SetDlgItemText(IDC_EDIT_SERVERID,CString(vDZInfoGB.m_cParam3));
	SetDlgItemText(IDC_EDIT_DEVICEID,CString(vDZInfoGB.m_cParam4));
	SetDlgItemText(IDC_EDIT_ACCOUNT,CString(vDZInfoGB.m_cParam5));
	SetDlgItemText(IDC_EDIT_PASSWORD,CString(vDZInfoGB.m_cParam6));

	int iRegVality = 0;
	sTemp = CString(vDZInfoGB.m_cParam7);
	iRegVality = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_REGISTER_INDATE,iRegVality);

	int iKeepalive = 0;
	sTemp = CString(vDZInfoGB.m_cParam8);
	iKeepalive = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_LIVETIME,iKeepalive);

	int iHeartInterval = 0;
	sTemp = CString(vDZInfoGB.m_cParam9);
	iHeartInterval = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_HEARTBEAT_INTERVAL,iHeartInterval);

	int iHeartTimes = 0;
	sTemp = CString(vDZInfoGB.m_cParam10);
	iHeartTimes = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_HEARTBEAT_COUNT,iHeartTimes);

	int iRegEnable = 0;
	sTemp = CString(vDZInfoGB.m_cParam11);
	iRegEnable = atoi(sTemp);
	CButton *pbtnRegEnable = (CButton *)GetDlgItem(IDC_CHECK_REGISTER);
	pbtnRegEnable->SetCheck(iRegEnable);

	int iEnable = 0;
	sTemp = CString(vDZInfoGB.m_cParam12);
	iEnable = atoi(sTemp);
	CButton *pbtnEnable = (CButton *)GetDlgItem(IDC_CHECK_ENABLE);
	pbtnEnable->SetCheck(iEnable);

	int iLocalPort = 0;
	sTemp = CString(vDZInfoGB.m_cParam13);
	iLocalPort = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_SIP_LOCAL_PORT,iLocalPort);

	int iSipRegion = 0;
	sTemp = CString(vDZInfoGB.m_cParam14);
	iSipRegion = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_SIP_REGION,iSipRegion);

	int iPlatformID = 0;
	sTemp = CString(vDZInfoGB.m_cParam15);
	iPlatformID = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_PLATFORM_ID,iPlatformID);

	int iProtocolversion= 0;
	sTemp = CString(vDZInfoGB.m_cParam16);
	iProtocolversion = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_PROTOCOL_VERSION,iProtocolversion);

	int iSigtran = 0;
	sTemp = CString(vDZInfoGB.m_cParam17);
	iSigtran = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_SIGTRAN,iSigtran);

	int iRegisterStatus = 0;
	sTemp = CString(vDZInfoGB.m_cParam18);
	iRegisterStatus = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_REGISTER_STATUS,iRegisterStatus);

	int iStreamIndex = 0;
	sTemp = CString(vDZInfoGB.m_cParam19);
	iStreamIndex = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_STREAM_INDEX,iStreamIndex);

	int iStreamTransMode = 0;
	sTemp = CString(vDZInfoGB.m_cParam20);
	iStreamTransMode = atoi(sTemp);
	SetDlgItemInt(IDC_EDIT_STREAM_TRANS_MODE,iStreamTransMode);

}

void CLS_GBT28181Set::OnLanguageChanged(int _iLanguage)
{
	SetDlgItemText(IDC_STATIC_ADDRESS, GetTextByLan(_T("IP地址"), _T("IP Address")));
	SetDlgItemText(IDC_STATIC_PORT, GetTextByLan(_T("端口号"), _T("Port")));
	SetDlgItemText(IDC_STATIC_SERVERID, GetTextByLan(_T("服务器ID"), _T("ServerID")));
	SetDlgItemText(IDC_STATIC_DEVICEID, GetTextByLan(_T("设备ID"), _T("DeviceID")));
	SetDlgItemText(IDC_STATIC_ACCOUNT, GetTextByLan(_T("用户名"), _T("Account")));
	SetDlgItemText(IDC_STATIC_PASSWORD, GetTextByLan(_T("密码"), _T("Password")));
	
	SetDlgItemText(IDC_STATIC_REGISTER_VALIDITY, GetTextByLan(_T("注册有效期"), _T("Register Validity")));
	SetDlgItemText(IDC_STATIC_KEEP_LIVE, GetTextByLan(_T("保活时间"), _T("Keep Live")));
	SetDlgItemText(IDC_STATIC_HEARTINTERVAL, GetTextByLan(_T("心跳间隔"), _T("Heart Interval")));
	SetDlgItemText(IDC_STATIC_HEART_TIME, GetTextByLan(_T("心跳次数"), _T("Heart Time")));
	
	SetDlgItemText(IDC_CHECK_REGISTER, GetTextByLan(_T("需要注册"), _T("Need Register")));
	SetDlgItemText(IDC_BUTTON_SAVE, GetTextByLan(_T("保存"), _T("Save")));

	SetDlgItemText(IDC_CHECK_ENABLE, GetTextByLan(_T("是否启用"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_SIP_LOCAL_PORT, GetTextByLan(_T("本地端口号"), _T("Local Port")));
	SetDlgItemText(IDC_STATIC_SIP_REGION, GetTextByLan(_T("SIP域"), _T("Region")));
	SetDlgItemText(IDC_STATIC_PLATFORM_ID, GetTextByLan(_T("平台编号"), _T("Platform ID")));
	SetDlgItemText(IDC_STATIC_PROTOCOL_VERSION, GetTextByLan(_T("协议版本"), _T("Protocol Version")));
	SetDlgItemText(IDC_STATIC_SIGTRAN, GetTextByLan(_T("信令传输协议"), _T("Sigtran")));
	SetDlgItemText(IDC_STATIC_REGISTER_STATUS, GetTextByLan(_T("注册状态"), _T("Registration Ststus")));
	SetDlgItemText(IDC_STATIC_STREAM_INDEX, GetTextByLan(_T("码流索引"), _T("Stream Index")));
	SetDlgItemText(IDC_STATIC_STREAM_TRANS_MODE, GetTextByLan(_T("码流传输方式"), _T("Stream Trans Mode")));
	
}

bool CLS_GBT28181Set::OnCheckValue(int _iNum, int _iStart, int _iEnd, int _iTitle )
{
	if(_iNum < _iStart || _iNum > _iEnd)
	{
		MessageBox(GetTextByLan(_T("请检查输入数据的数据是否正确"),_T("Please check whether the input data is correct")));
		return false;
	}
	return true;
}

void CLS_GBT28181Set::OnBnClickedButtonSave()
{
	DZ_INFO_PARAM vDZInfoGB;
	memset(&vDZInfoGB,0,sizeof(DZ_INFO_PARAM));
	
	CString cstrIp;
	m_edtIPAddress.GetWindowText(cstrIp);
	memcpy(vDZInfoGB.m_cParam1,cstrIp,cstrIp.GetLength());
		
	int iPort = GetDlgItemInt(IDC_EDIT_PORT);
	CString strPort;
	strPort.Format(_T("%d"),iPort);

	if (OnCheckValue(iPort,MIN_COMMUNCATION_PORT,MAX_COMMUNCATION_PORT,IDC_EDIT_PORT))
	{
		strcpy_s(vDZInfoGB.m_cParam2,(LPSTR)(LPCTSTR)strPort);
	}
	else
	{
		return;
	}

	CString strServerID;
	((CEdit *)GetDlgItem(IDC_EDIT_SERVERID))->GetWindowText(strServerID);

	strcpy_s(vDZInfoGB.m_cParam3,(LPSTR)(LPCTSTR)strServerID.Trim());

	CString strDeviceID;
	((CEdit *)GetDlgItem(IDC_EDIT_DEVICEID))->GetWindowText(strDeviceID);

	strcpy_s(vDZInfoGB.m_cParam4,(LPSTR)(LPCTSTR)strDeviceID.Trim());

	CString strUserName;
	((CEdit *)GetDlgItem(IDC_EDIT_ACCOUNT))->GetWindowText(strUserName);	

	strcpy_s(vDZInfoGB.m_cParam5,(LPSTR)(LPCTSTR)strUserName);

	CString strPassword;
	((CEdit *)GetDlgItem(IDC_EDIT_PASSWORD))->GetWindowText(strPassword);	
	strcpy_s(vDZInfoGB.m_cParam6,(LPSTR)(LPCTSTR)strPassword);

	int iRegValidity = GetDlgItemInt(IDC_EDIT_REGISTER_INDATE);
	CString strRegValidity;
	strRegValidity.Format(_T("%d"),iRegValidity);
	if (OnCheckValue(iRegValidity,MIN_REGVALIDITY,MAX_REGVALIDITY,IDC_EDIT_REGISTER_INDATE))
	{
		strcpy_s(vDZInfoGB.m_cParam7,(LPSTR)(LPCTSTR)strRegValidity);
	}
	else
	{
		return;
	}

	int iKeepalive = GetDlgItemInt(IDC_EDIT_LIVETIME);
	CString strKeepalive;
	strKeepalive.Format(_T("%d"),iKeepalive);
	strcpy_s(vDZInfoGB.m_cParam8,(LPSTR)(LPCTSTR)strKeepalive);

	int iHeartInterval = GetDlgItemInt(IDC_EDIT_HEARTBEAT_INTERVAL);
	CString strHeartInterval;
	strHeartInterval.Format(_T("%d"),iHeartInterval);
	strcpy_s(vDZInfoGB.m_cParam9,(LPSTR)(LPCTSTR)strHeartInterval);

	int iHeartTimes = GetDlgItemInt(IDC_EDIT_HEARTBEAT_COUNT);
	CString strHeartTimes;
	strHeartTimes.Format(_T("%d"),iHeartTimes);
	strcpy_s(vDZInfoGB.m_cParam10,(LPSTR)(LPCTSTR)strHeartTimes);

	CButton *pbtnRegEnable = (CButton *)GetDlgItem(IDC_CHECK_REGISTER);
	CString strRegEnable;
	int iRegEnable = pbtnRegEnable->GetCheck();
	strRegEnable.Format(_T("%d"),iRegEnable);
	strcpy_s(vDZInfoGB.m_cParam11,(LPSTR)(LPCTSTR)strRegEnable);
	
	CButton *pbtnEnable = (CButton *)GetDlgItem(IDC_CHECK_ENABLE);
	CString strEnable;
	int iEnable = pbtnEnable->GetCheck();
	strEnable.Format(_T("%d"),iEnable);
	strcpy_s(vDZInfoGB.m_cParam12,(LPSTR)(LPCTSTR)strEnable);
	
	int iLocalport = GetDlgItemInt(IDC_EDIT_SIP_LOCAL_PORT);
	CString strLocalport;
	strLocalport.Format(_T("%d"),iLocalport);
	strcpy_s(vDZInfoGB.m_cParam13,(LPSTR)(LPCTSTR)strLocalport);

	int iSipRegion = GetDlgItemInt(IDC_EDIT_SIP_REGION);
	CString strSipRegion;
	strSipRegion.Format(_T("%d"),iSipRegion);
	strcpy_s(vDZInfoGB.m_cParam14,(LPSTR)(LPCTSTR)strSipRegion);

	int iPlatformID = GetDlgItemInt(IDC_EDIT_PLATFORM_ID);
	CString strPlatformID;
	strPlatformID.Format(_T("%d"),iPlatformID);
	strcpy_s(vDZInfoGB.m_cParam15,(LPSTR)(LPCTSTR)strPlatformID);

	int iProtocolVersion = GetDlgItemInt(IDC_EDIT_PROTOCOL_VERSION);
	CString strProtocolVersion;
	strProtocolVersion.Format(_T("%d"),iProtocolVersion);
	strcpy_s(vDZInfoGB.m_cParam16,(LPSTR)(LPCTSTR)strProtocolVersion);

	int iSigtran = GetDlgItemInt(IDC_EDIT_SIGTRAN);
	CString strSigtran;
	strSigtran.Format(_T("%d"),iSigtran);
	strcpy_s(vDZInfoGB.m_cParam17,(LPSTR)(LPCTSTR)strSigtran);

	int iRegistStatus = GetDlgItemInt(IDC_EDIT_REGISTER_STATUS);
	CString strRegistStatus;
	strRegistStatus.Format(_T("%d"),iRegistStatus);
	strcpy_s(vDZInfoGB.m_cParam18,(LPSTR)(LPCTSTR)strRegistStatus);

	int iStreamIndex = GetDlgItemInt(IDC_EDIT_STREAM_INDEX);
	CString strStreamIndex;
	strStreamIndex.Format(_T("%d"),iStreamIndex);
	strcpy_s(vDZInfoGB.m_cParam19,(LPSTR)(LPCTSTR)strStreamIndex);

	int iStreamTransMode = GetDlgItemInt(IDC_EDIT_STREAM_TRANS_MODE);
	CString strStreamTransMode;
	strStreamTransMode.Format(_T("%d"),iStreamTransMode);
	strcpy_s(vDZInfoGB.m_cParam20,(LPSTR)(LPCTSTR)strStreamTransMode);

	int iRet = NetClient_SetDZInfo(m_iLogonID, &vDZInfoGB);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_GBT28181Set::OnBnClickedButtonSave][NetClient_SetDZInfo] (%d)",m_iLogonID);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_GBT28181Set::OnBnClickedButtonSave][NetClient_SetDZInfo](%d)",m_iLogonID);
	}
}
