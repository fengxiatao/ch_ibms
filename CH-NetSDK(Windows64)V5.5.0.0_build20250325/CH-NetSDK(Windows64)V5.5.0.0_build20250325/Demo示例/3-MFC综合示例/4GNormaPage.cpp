// 4GNormaPage.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "4GNormalPage.h"


// CLS_4GNormal dialog

IMPLEMENT_DYNAMIC(CLS_4GNormal, CDialog)

CLS_4GNormal::CLS_4GNormal(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_4GNormal::IDD, pParent)
{

}

CLS_4GNormal::~CLS_4GNormal()
{
}

void CLS_4GNormal::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_4GNORMAL_TYPE, m_cbo4GType);
	DDX_Control(pDX, IDC_COMBO_4GCONTROL, m_cbo4GControl);
	DDX_Control(pDX, IDC_COMBO_4GNORMAL_STATUS, m_cbo4GStatus);
	DDX_Control(pDX, IDC_CHECK_INFO, m_chkInfo);
	DDX_Control(pDX, IDC_CHECK_PIC, m_chkPicInfo);
	DDX_Control(pDX, IDC_EDT_IMEI, m_edt_IMEI);
	DDX_Control(pDX, IDC_EDT_ICCID, m_edt_ICCID);
}


BEGIN_MESSAGE_MAP(CLS_4GNormal, CDialog)
	ON_BN_CLICKED(IDC_BUTTON4GCONTROL, &CLS_4GNormal::OnBnClickedButton4gcontrol)
	ON_BN_CLICKED(IDC_BUTTON_4GNORMAL_GETSTATUS, &CLS_4GNormal::OnBnClickedButton4gnormalGetstatus)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()


// CLS_4GNormal message handlers

BOOL CLS_4GNormal::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_cbo4GControl.SetCurSel(0);
	m_cbo4GControl.ShowWindow(SW_HIDE);
	m_cbo4GType.SetCurSel(0);
	m_cbo4GStatus.SetCurSel(0);

	m_cbo4GType.AddString("Mobile 3G");
	m_cbo4GType.AddString("Telecom 3G");
	m_cbo4GType.AddString("Unicom 3G");
	m_cbo4GType.AddString("unknown");
    m_cbo4GType.AddString("Mobile 4G");
	m_cbo4GType.AddString("Telecom 4G");
	m_cbo4GType.AddString("Unicom 4G");
	m_cbo4GType.AddString("Mobile Network");
	

	m_cbo4GStatus.AddString("0-not online");
	m_cbo4GStatus.AddString("1-Online");
	m_cbo4GStatus.AddString("2-dialing");

	return TRUE;
}

void CLS_4GNormal::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

}
void CLS_4GNormal::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		Update4GUpLoad();
		OnBnClickedButton4gnormalGetstatus();
	}
}

void CLS_4GNormal::OnBnClickedButton4gcontrol()
{
	// TODO: Add your control notificat
	TNetReduceFlowCtrl tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	int iState = m_chkInfo.GetCheck();
	iState |= (m_chkPicInfo.GetCheck() << 1);
	tInfo.iEnable = iState;
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_NET_REDUCE, m_iChannelNo, &tInfo, tInfo.iSize);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID, m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetAlarmConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
}

void CLS_4GNormal::OnBnClickedButton4gnormalGetstatus()
{
	// TODO: Add your control notification handler code here
	//UI_UpdateDeviceStatus();
	int iRet = RET_FAILED;
	T4GParam tParam = {0};
	int iBytesReturn = -1;
	tParam.iSize = (int)sizeof(T4GParam);
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET4GDEVICE, m_iChannelNo, &tParam, tParam.iSize, &iBytesReturn);
	if (RET_SUCCESS == iRet)
	{
		m_cbo4GType.SetCurSel(tParam.i3GDeviceType);

		m_cbo4GStatus.SetCurSel(tParam.iStatus);
		SetDlgItemInt(IDC_EDIT_4GNORMAL_INTENSITY, tParam.iIntensity);
		GetDlgItem(IDC_EDIT_4GNORMAL_IP)->SetWindowText(tParam.pcIP);
		GetDlgItem(IDC_EDIT_4GNORMAL_STARTTIME)->SetWindowText(tParam.pcStarttime);
		GetDlgItem(IDC_EDT_IMEI)->SetWindowText(tParam.pcIMEI);
		GetDlgItem(IDC_EDT_ICCID)->SetWindowText(tParam.pcICCID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","Get3GDeviceStatus Fail!LogonID(%d)",m_iLogonID);
	}
}

void CLS_4GNormal::UI_UpdateDeviceStatus()
{
	int i3GDeviceType;
	int i3GIntensity;
	int   i3GStatus = -1;
	char c3GIP[16] = {0};
	char c3Starttime[64] = {0};
	if (NetClient_Get3GDeviceStatus(m_iLogonID,&i3GDeviceType,&i3GStatus,&i3GIntensity,c3GIP,c3Starttime)==0)
	{
		m_cbo4GType.SetCurSel(i3GDeviceType);

		if (i3GStatus == 0 || i3GStatus == 1 || i3GStatus == 2)
			m_cbo4GStatus.SetCurSel(i3GStatus);
		else
			m_cbo4GStatus.SetCurSel(-1);
		SetDlgItemInt(IDC_EDIT_4GNORMAL_INTENSITY, i3GIntensity);
		GetDlgItem(IDC_EDIT_4GNORMAL_IP)->SetWindowText(c3GIP);
		GetDlgItem(IDC_EDIT_4GNORMAL_STARTTIME)->SetWindowText(c3Starttime);
	}
	else
		AddLog(LOG_TYPE_FAIL,"","Get3GDeviceStatus Fail!LogonID(%d)",m_iLogonID);
}

void CLS_4GNormal::Update4GUpLoad()
{
	GetDlgItem(IDC_BUTTON4GCONTROL)->EnableWindow(IsSupport4GLoad());
	TNetReduceFlowCtrl tInfo = {0};
	tInfo.iSize = (int)sizeof(tInfo);
	int index = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_NET_REDUCE,m_iChannelNo,&tInfo, tInfo.iSize,&index);
	if (RET_SUCCESS == iRet)
	{
		m_chkInfo.SetCheck(tInfo.iEnable & 0x01);
		m_chkPicInfo.SetCheck((tInfo.iEnable>>1)&0x01);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetAlarmConfig (%d,%d)",m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetAlarmConfig (%d,%d)",m_iLogonID,m_iChannelNo);
	}
}

BOOL CLS_4GNormal::IsSupport4GLoad()
{
	int iAblity = -1;
	FuncAbilityLevel stFuncAbilityLevel = {0};
	stFuncAbilityLevel.iSize = sizeof(FuncAbilityLevel);
	stFuncAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_NET;
	stFuncAbilityLevel.iSubFuncType = 13;
	int iByteReturned = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO,\
		&stFuncAbilityLevel, sizeof(stFuncAbilityLevel), &iByteReturned);
	if (iRet < 0 || strlen(stFuncAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_4GNormal::GetFuncRoi] GetDevConfig MAIN_FUNC_TYPE_SYSTEM Failed! m_iLogonID %d", m_iLogonID);
		return FALSE;
	}
	BOOL blTempChk = ((stFuncAbilityLevel.cParam[0]) == '1')?TRUE:FALSE;
	return blTempChk;
}
