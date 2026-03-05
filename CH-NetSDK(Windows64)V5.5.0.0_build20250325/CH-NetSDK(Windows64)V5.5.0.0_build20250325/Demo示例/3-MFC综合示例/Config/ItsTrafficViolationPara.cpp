// ItsTrafficViolationPara.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "ItsTrafficViolationPara.h"


// Cls_ItsTrafficViolationPara dialog
#define MIN_WHISTLE_SNAPSHOT_NUM    1
#define MAX_WHISTLE_SNAPSHOT_NUM    11

IMPLEMENT_DYNAMIC(Cls_ItsTrafficViolationPara, CDialog)

Cls_ItsTrafficViolationPara::Cls_ItsTrafficViolationPara(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(Cls_ItsTrafficViolationPara::IDD, pParent)
{

}

Cls_ItsTrafficViolationPara::~Cls_ItsTrafficViolationPara()
{
}

void Cls_ItsTrafficViolationPara::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_CALL, m_edtCall);
	DDX_Control(pDX, IDC_EDIT_NO_SEAT, m_edtNoSeat);
    DDX_Control(pDX, IDC_COMBO_WHISTLESNAPSHOT, m_cboWhistleSnapshot);
}


BEGIN_MESSAGE_MAP(Cls_ItsTrafficViolationPara, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET, &Cls_ItsTrafficViolationPara::OnBnClickedButtonSet)
    ON_BN_CLICKED(IDC_BUTTON_SET_WHISTLESNAPSHOT, &Cls_ItsTrafficViolationPara::OnBnClickedButtonSetWhistlesnapshot)
END_MESSAGE_MAP()

BOOL Cls_ItsTrafficViolationPara::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateDialog();

	return TRUE;
}

void Cls_ItsTrafficViolationPara::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if(_iChannelNo < 0)
	{
		m_iChannelNO = 0;
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	UI_UpdateDialog();
}

void Cls_ItsTrafficViolationPara::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void Cls_ItsTrafficViolationPara::UI_UpdateDialog()
{
	SetDlgItemText(IDC_STATIC_CALL,GetTextByLan(_T("接打电话灵敏度"), _T("Call Sensitivity")));
	SetDlgItemText(IDC_STATIC_NO_SEAT,GetTextByLan(_T("不系安全带灵敏度"), _T("No Seat Belt Sensitivity")));
	SetDlgItemText(IDC_BUTTON_SET,GetTextByLan(_T("设置"), _T("Set")));

	SetDlgItemInt(IDC_EDIT_CALL, TRAFFIC_VIOLATION_PARA_SENSITIVITY);
	SetDlgItemInt(IDC_EDIT_NO_SEAT, TRAFFIC_VIOLATION_PARA_SENSITIVITY);

    SetDlgItemText(IDC_BUTTON_SET_WHISTLESNAPSHOT,GetTextByLan(_T("设置"), _T("Set")));
    SetDlgItemText(IDC_STATIC, GetTextByLan(_T("鸣笛抓拍提前抓拍帧"), _T("Whistle Snapshot")));

    SetDlgItemText(IDC_STATIC, GetTextByLan(_T("鸣笛抓拍提前抓拍帧"), _T("Whistle Snapshot")));

    int iCurSel = m_cboWhistleSnapshot.GetCurSel();
    m_cboWhistleSnapshot.ResetContent();
    for (int i = MIN_WHISTLE_SNAPSHOT_NUM; i < MAX_WHISTLE_SNAPSHOT_NUM; i++)
    {
        m_cboWhistleSnapshot.SetItemData(m_cboWhistleSnapshot.AddString(IntToCString(i)), i);
    }
    m_cboWhistleSnapshot.SetCurSel(((iCurSel < 0) ? 0 : iCurSel));
	UI_UpdateData();
}

void Cls_ItsTrafficViolationPara::UI_UpdateData()
{
	TrafficViolationPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNO;
	int iRet = NetClient_GetITSExtraInfo (m_iLogonID, ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA, m_iChannelNO, &tInfo, sizeof(TrafficViolationPara));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetITSExtraInfo[ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA](%d, %d)",m_iLogonID, m_iChannelNO);
		SetDlgItemInt(IDC_EDIT_CALL, tInfo.iCallSensitivity);
		SetDlgItemInt(IDC_EDIT_NO_SEAT, tInfo.iNoSeatBeltSensitivity);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetITSExtraInfo[ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA] (%d, %d),error(%d)",m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void Cls_ItsTrafficViolationPara::OnBnClickedButtonSet()
{
	TrafficViolationPara tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNO;
	tInfo.iCallSensitivity = GetDlgItemInt(IDC_EDIT_CALL);
	if(tInfo.iCallSensitivity < 0 || tInfo.iCallSensitivity > 100)
	{
		MessageBox(GetTextByLan("接电话灵敏度，请输入0~100!","CallSensitivity，Please Input0~100!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}

	tInfo.iNoSeatBeltSensitivity = GetDlgItemInt(IDC_EDIT_NO_SEAT);
	if(tInfo.iNoSeatBeltSensitivity < 0 || tInfo.iNoSeatBeltSensitivity > 100)
	{
		MessageBox(GetTextByLan("不系安全带灵敏度，请输入0~100!","NoSeatBeltSensitivity，Please Input0~100!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		return;
	}

	int iRet = NetClient_SetITSExtraInfo (m_iLogonID, ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA, m_iChannelNO, &tInfo, sizeof(TrafficViolationPara));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSExtraInfo[ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA] (%d, %d)",m_iLogonID, m_iChannelNO);

	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSExtraInfo[ITS_EXTRAINFO_CMD_TRAFFIC_VIOLATION_PARA] (%d, %d),error(%d)",m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void Cls_ItsTrafficViolationPara::OnBnClickedButtonSetWhistlesnapshot()
{
    ITS_WHISTLESNAPSHOTRESULT tInfo = {0};
    tInfo.iSize = sizeof(tInfo);
    tInfo.iChannelNo = m_iChannelNO;
    tInfo.iCapFrameNo = m_cboWhistleSnapshot.GetItemData(m_cboWhistleSnapshot.GetCurSel());


    int iRet = NetClient_CmdConfig(m_iLogonID, CMD_WHISTLESNAPSHOT, m_iChannelNO, &tInfo, sizeof(ITS_WHISTLESNAPSHOTRESULT), &tInfo, sizeof(ITS_WHISTLESNAPSHOTRESULT));
    if(0 == iRet)
    {
        AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig[CMD_WHISTLESNAPSHOT] (%d, %d), result(0:reserve;1:Success,2:fail)(%d)",m_iLogonID, m_iChannelNO, tInfo.iResult);

    }
    else
    {
        AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[CMD_WHISTLESNAPSHOT] (%d, %d),error(%d)",m_iLogonID, m_iChannelNO, GetLastError());
    }
}
