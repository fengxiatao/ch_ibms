// IOData.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "IOData.h"


// CLS_IOData dialog

IMPLEMENT_DYNAMIC(CLS_IOData, CDialog)

CLS_IOData::CLS_IOData(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_IOData::IDD, pParent)
{
	m_iLogonId = -1;
	m_iChannelNo = -1;
}

CLS_IOData::~CLS_IOData()
{
}

void CLS_IOData::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonId = _iLogonID;
	m_iChannelNo = _iChannelNo;
}

void CLS_IOData::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_DEV_TYPE, m_cboDevType);
	DDX_Control(pDX, IDC_CBO_IO_NO, m_cboIONo);
	DDX_Control(pDX, IDC_CBO_DEF_STA, m_cboDefaultState);
	DDX_Control(pDX, IDC_CBO_WORK_STA, m_cboWorkState);
	DDX_Control(pDX, IDC_EDT_DUTY_RADIO, m_edtDutyCycle);
	DDX_Control(pDX, IDC_EDT_LAST_TIME, m_edtDuration);
	DDX_Control(pDX, IDC_EDT_ADVANCED_TIME, m_LeadTime);
	DDX_Control(pDX, IDC_EDT_DOUBLING_FREQUENCY, m_edtDouFre);
	DDX_Control(pDX, IDC_COMBO_IO_ENABLE, m_cboDNEnable);
	DDX_Control(pDX, IDC_SLIDER_IO_LIGHT_OPEN, m_sldLightOpen);
	DDX_Control(pDX, IDC_SLIDER_IO_LIGHT_CLOSE, m_sldLightClose);
}

BOOL CLS_IOData::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	int iI = 0;
	InsertString(m_cboDefaultState, 0, IDS_ITS_PULSEWIDTH);
	InsertString(m_cboDefaultState, 1, IDS_ITS_ELECTRICAL_LEVEL);
	InsertString(m_cboWorkState, 0, IDS_ITS_WORK_LOW);
	InsertString(m_cboWorkState, 1, IDS_ITS_WORK_HIGH);
	for(iI = 1; iI <= 10; iI++)
	{
		CString cstrNum;
		cstrNum.Format("%d", iI);
		m_cboIONo.AddString(cstrNum);
	}
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_IOData::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialog();
}
void CLS_IOData::UI_UpdateDialog()
{
	//SetDlgItemTextEx(IDC_GP_OTHER_DEVICE_PARA, IDS_ITS_OTHER_DEVICE_PARA);
	SetDlgItemTextEx(IDC_GP_IO_OUT, IDS_ITS_IO_OUT);
	SetDlgItemTextEx(IDC_STC_IO_NO, IDS_ITS_IO_NO);
	SetDlgItemTextEx(IDC_STC_DEVICETYPE, IDS_ITS_DEVICE_TYPE);
	SetDlgItemTextEx(IDC_STC_DEFAULT_STATE, IDS_ITS_DEFAULT_STATE);
	SetDlgItemTextEx(IDC_STC_WORK_STATE, IDS_ITS_WORK_STATE);
	SetDlgItemTextEx(IDC_STC_DUTYFACTOR, IDS_ITS_DUTYFACTOR);
	SetDlgItemTextEx(IDC_STC_DURATION, IDS_ITS_DURATION);
	SetDlgItemTextEx(IDC_STC_TIME_IN_ADVANCE, IDS_ITS_TIME_IN_ADVANCE);
	SetDlgItemTextEx(IDC_STC_DOUBLING_FREQUENCY, IDS_ITS_DOUBLING_FREQUENCY);
	SetDlgItemTextEx(IDC_BTN_SET, IDS_ITS_IO_SET);

	m_cboDevType.ResetContent();
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextEx(IDS_ITS_FLASHLIGHT)),0);
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextEx(IDS_ITS_STROBELAMP)),1);
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextEx(IDS_ITS_POLARIZER)),2);
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextByLan("常亮灯","Regular light")),3);
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextByLan("频闪闪光","Frequency flash")),4);
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextByLan("红光/白光控制","Red white light control")),5);
	m_cboDevType.SetItemData(m_cboDevType.AddString(GetTextByLan("报警输出","Alarm output")),64);
	m_cboDevType.SetCurSel(0);

	InsertString(m_cboDefaultState, 0, IDS_ITS_PULSEWIDTH);
	InsertString(m_cboDefaultState, 1, IDS_ITS_ELECTRICAL_LEVEL);
	m_cboDefaultState.SetCurSel(0);

	InsertString(m_cboWorkState, 0, IDS_ITS_WORK_HIGH);
	InsertString(m_cboWorkState, 1, IDS_ITS_WORK_LOW);
	m_cboWorkState.SetCurSel(0);

	m_cboDNEnable.ResetContent();
	InsertString(m_cboDNEnable, 0, IDS_ADV_CHANNEL_DAYTIME);
	InsertString(m_cboDNEnable, 1, IDS_ADV_CHANNEL_NIGHT);
	m_cboDNEnable.SetCurSel(0);

	m_sldLightOpen.SetRange(0, 100);
	m_sldLightOpen.SetPos(1);
	SetDlgItemInt(IDC_STC_IO_LIGHT_OPEN_VALUE, m_sldLightOpen.GetPos());

	m_sldLightClose.SetRange(0, 100);
	m_sldLightClose.SetPos(1);
	SetDlgItemInt(IDC_STC_IO_LIGHT_CLOSE_VALUE, m_sldLightClose.GetPos());

	UI_UpdateFillLight();
}


BEGIN_MESSAGE_MAP(CLS_IOData, CDialog)
	ON_BN_CLICKED(IDC_BTN_SET, &CLS_IOData::OnBnClickedBtnSet)
	ON_CBN_SELCHANGE(IDC_CBO_IO_NO, &CLS_IOData::OnCbnSelchangeCboIoNo)
	ON_BN_CLICKED(IDC_BTN_IO_LIGHT_SET, &CLS_IOData::OnBnClickedBtnIoLightSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_IO_LIGHT_OPEN, &CLS_IOData::OnNMCustomdrawSliderIoLightOpen)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_IO_LIGHT_CLOSE, &CLS_IOData::OnNMCustomdrawSliderIoLightClose)
END_MESSAGE_MAP()


// CLS_IOData message handler

void CLS_IOData::OnBnClickedBtnSet()
{
	TITS_IOLinkInfo tIOdata;
	memset(&tIOdata, 0, sizeof(TITS_IOLinkInfo));
	int iRet = -1;
	tIOdata.m_iIONo = m_cboIONo.GetCurSel();
	tIOdata.m_iLinkDevice = m_cboDevType.GetItemData(m_cboDevType.GetCurSel());
	tIOdata.m_iDefaultState = m_cboDefaultState.GetCurSel();
	tIOdata.m_iWorkState = m_cboWorkState.GetCurSel();
	tIOdata.m_iDutyCycle = GetDlgItemInt(IDC_EDT_DUTY_RADIO);
	tIOdata.m_iAheadTime = GetDlgItemInt(IDC_EDT_ADVANCED_TIME);
	tIOdata.m_iFrequency = GetDlgItemInt(IDC_EDT_DOUBLING_FREQUENCY);
	tIOdata.m_iDevicePulse = GetDlgItemInt(IDC_EDT_LAST_TIME);
	tIOdata.m_iDayNightEnable = m_cboDNEnable.GetCurSel();
	iRet = NetClient_SetDevConfig(m_iLogonId, NET_CLIENT_IO_LINK_INFO, m_iChannelNo, &tIOdata, sizeof(TITS_IOLinkInfo));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSExtraInfo[ITS_LIGHTINFO_CMD_GET][NET_CLIENT_IO_LINK_INFO] (%d, %d)",m_iLogonId, m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSExtraInfo[ITS_LIGHTINFO_CMD_GET] (%d, %d),error(%d)",m_iLogonId, m_iChannelNo, GetLastError());
	}
}

void CLS_IOData::OnCbnSelchangeCboIoNo()
{
	TITS_IOLinkInfo tGetIOdata = {-1};
	int iRet = -1;
	int iReturn = -1;
	tGetIOdata.m_iIONo = m_cboIONo.GetCurSel();
	iRet = NetClient_GetDevConfig(m_iLogonId, NET_CLIENT_IO_LINK_INFO, m_iChannelNo, &tGetIOdata, sizeof(TITS_IOLinkInfo), &iReturn);
	if (0 == iRet)
	{
		for (int i=0; i<m_cboDevType.GetCount(); i++)
		{
			if (tGetIOdata.m_iLinkDevice == m_cboDevType.GetItemData(i))
			{
				m_cboDevType.SetCurSel(i);
				break;
			}
		}
		m_cboDefaultState.SetCurSel(tGetIOdata.m_iDefaultState);
		m_cboWorkState.SetCurSel(tGetIOdata.m_iWorkState);
		SetDlgItemInt(IDC_EDT_DUTY_RADIO,tGetIOdata.m_iDutyCycle);
		
		SetDlgItemInt(IDC_EDT_LAST_TIME,tGetIOdata.m_iDevicePulse);
		SetDlgItemInt(IDC_EDT_ADVANCED_TIME,tGetIOdata.m_iAheadTime);
		SetDlgItemInt(IDC_EDT_DOUBLING_FREQUENCY,tGetIOdata.m_iFrequency);
		m_cboWorkState.SetCurSel(tGetIOdata.m_iDayNightEnable);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig[NET_CLIENT_IO_LINK_INFO] (%d, %d)",m_iLogonId, m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig[NET_CLIENT_IO_LINK_INFO] (%d, %d),error(%d)",m_iLogonId, m_iChannelNo, GetLastError());
	}
}

void CLS_IOData::UI_UpdateFillLight()
{
	TrafficFillLight tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;

	int iReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonId, NET_CLIENT_TRAFFIC_FILL_LIGHT_INFO, m_iChannelNo, &tInfo, sizeof(tInfo), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_sldLightOpen.SetPos(tInfo.iOpenVal);
		m_sldLightClose.SetPos(tInfo.iCloseVal);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig[NET_CLIENT_TRAFFIC_FILL_LIGHT_INFO] (%d, %d),error(%d)",m_iLogonId, m_iChannelNo, GetLastError());
	}
}

void CLS_IOData::OnBnClickedBtnIoLightSet()
{
	TrafficFillLight tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iOpenVal = m_sldLightOpen.GetPos();
	tInfo.iCloseVal = m_sldLightClose.GetPos();

	int iRet = NetClient_SetDevConfig(m_iLogonId, NET_CLIENT_TRAFFIC_FILL_LIGHT_INFO, m_iChannelNo, &tInfo, sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetDevConfig[NET_CLIENT_TRAFFIC_FILL_LIGHT_INFO] (%d, %d)",m_iLogonId, m_iChannelNo);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig[NET_CLIENT_TRAFFIC_FILL_LIGHT_INFO] (%d, %d),error(%d)",m_iLogonId, m_iChannelNo, GetLastError());
	}
}

void CLS_IOData::OnNMCustomdrawSliderIoLightOpen(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_IO_LIGHT_OPEN_VALUE, m_sldLightOpen.GetPos());
	*pResult = 0;
}

void CLS_IOData::OnNMCustomdrawSliderIoLightClose(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_IO_LIGHT_CLOSE_VALUE, m_sldLightClose.GetPos());
	*pResult = 0;
}
