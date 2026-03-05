// .\Config\CLS_DlgThermography.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgThermography.h"

#define MAX_RECT_TEXT_LENGTH  128
#define COMMONENABLE_TEM_DETECT_ENABLE  1
#define COMMONENABLE_TEM_DETECT_DISABLE 2
// CLS_DlgThermography dialog

IMPLEMENT_DYNAMIC(CLS_DlgThermography, CDialog)

CLS_DlgThermography::CLS_DlgThermography(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgThermography::IDD, pParent)
{

}

CLS_DlgThermography::~CLS_DlgThermography()
{
}

void CLS_DlgThermography::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TI_CHAN, m_CboChannelNo);
	DDX_Control(pDX, IDC_COMBO_TI_TEMSCALE_TYPE, m_CboTemScaleType);
	DDX_Control(pDX, IDC_EDIT_BODYTEM_CORRECT, m_EdtBodyTemCorrect);
	DDX_Control(pDX, IDC_EDIT_TI_ITCORRECT, m_EdtIntellectCorrect);
	DDX_Control(pDX, IDC_CHECK_BODYTEM_CORRECT, m_ChkBodyTemCorrect);
	DDX_Control(pDX, IDC_CHECK_IT_CORRECT, m_CboIntellectCorrect);
	DDX_Control(pDX, IDC_CHECK_BLACKBODY_ENABLE, m_ChkBDCorrect);
	DDX_Control(pDX, IDC_COMBO_BDCORRECT_TYPE, m_CboBDCorrectType);
	DDX_Control(pDX, IDC_COMBO_BDNUM, m_CboBDNum);
	DDX_Control(pDX, IDC_EDIT_BDTEM1, m_EdtBDTem1);
	DDX_Control(pDX, IDC_EDIT_BDTEM2, m_EdtBDTem2);
	DDX_Control(pDX, IDC_COMBO_BDUNIT1, m_CboBDUnit1);
	DDX_Control(pDX, IDC_COMBO_BDTIME2, m_CboBDTemUnit2);
	DDX_Control(pDX, IDC_EDIT_BDDIS1, m_EdtBDDis1);
	DDX_Control(pDX, IDC_EDIT_BDDIS2, m_EdtBDDis2);
	DDX_Control(pDX, IDC_EDIT_BDPT1, m_EdtBDPT1);
	DDX_Control(pDX, IDC_EDIT_BDPT2, m_EdtBDPT2);
	DDX_Control(pDX, IDC_CHECK_COMMONENABLE_TEMDETEC, m_ChkComEnableTemDetec);
	DDX_Control(pDX, IDC_CHECK_BLACKBODY_DETECT_ENABLE, m_chkBkDetectEnable);
	DDX_Control(pDX, IDC_SLIDER_BLACKBODY_DETECT_THRESHOLD, m_sldBkThreshold);
	DDX_Control(pDX, IDC_SLIDER_SHARK_TIME, m_sldBkSharkTime);
	DDX_Control(pDX, IDC_COMBO_TEMPETURE_POSITION, m_cboTemPosition);
}


BEGIN_MESSAGE_MAP(CLS_DlgThermography, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_TI_TEMSCALE_TYPE_SET, &CLS_DlgThermography::OnBnClickedButtonTiTemscaleTypeSet)
	ON_BN_CLICKED(IDC_BUTTON_BODYTEM_CORRECT_SET, &CLS_DlgThermography::OnBnClickedButtonBodytemCorrectSet)
	ON_BN_CLICKED(IDC_BUTTON_TI_ITCORRECT_SET, &CLS_DlgThermography::OnBnClickedButtonTiItcorrectSet)
	ON_BN_CLICKED(IDC_BUTTON_BDSET, &CLS_DlgThermography::OnBnClickedButtonBdset)
	ON_BN_CLICKED(IDC_CHECK_BODYTEM_CORRECT, &CLS_DlgThermography::OnBnClickedCheckBodytemCorrect)
	ON_BN_CLICKED(IDC_CHECK_IT_CORRECT, &CLS_DlgThermography::OnBnClickedCheckItCorrect)
	ON_BN_CLICKED(IDC_CHECK_COMMONENABLE_TEMDETEC, &CLS_DlgThermography::OnBnClickedCheckCommonenableTemdetec)
	ON_BN_CLICKED(IDC_BUTTON_BK_DETECT, &CLS_DlgThermography::OnBnClickedButtonBkDetect)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_BLACKBODY_DETECT_THRESHOLD, &CLS_DlgThermography::OnNMCustomdrawSliderBlackbodyDetectThreshold)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_SHARK_TIME, &CLS_DlgThermography::OnNMCustomdrawSliderSharkTime)
	ON_CBN_SELCHANGE(IDC_COMBO_TEMPETURE_POSITION, &CLS_DlgThermography::OnCbnSelchangeComboTempeturePosition)
END_MESSAGE_MAP()


// CLS_DlgThermography message handler

BOOL CLS_DlgThermography::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;
}

void CLS_DlgThermography::UpdateUIText()
{
	m_CboTemScaleType.ResetContent();
	m_CboBDCorrectType.ResetContent();
	m_CboBDNum.ResetContent();
	m_CboBDUnit1.ResetContent();
	m_CboBDTemUnit2.ResetContent();

	m_CboTemScaleType.SetItemData(0, m_CboTemScaleType.AddString(GetTextByLan(_T("摄氏度"), _T("centigrade"))));
	m_CboTemScaleType.SetItemData(1, m_CboTemScaleType.AddString(GetTextByLan(_T("华氏度"), _T("Fahrenheit degree"))));
	//m_CboTemScaleType.SetItemData(2, m_CboTemScaleType.AddString(GetTextByLan(_T("开尔文"), _T("degree Kelvin"))));

	m_CboBDCorrectType.SetItemData(0, m_CboBDCorrectType.AddString(GetTextByLan(_T("单次校正"), _T("Single correction"))));
	m_CboBDCorrectType.SetItemData(1, m_CboBDCorrectType.AddString(GetTextByLan(_T("持续校正"), _T("Continuous correction"))));

	m_CboBDNum.SetItemData(0, m_CboBDNum.AddString("1"));
	m_CboBDNum.SetItemData(1, m_CboBDNum.AddString("2"));

	m_CboBDUnit1.SetItemData(0, m_CboBDUnit1.AddString(GetTextByLan(_T("摄氏度"), _T("centigrade"))));
	m_CboBDUnit1.SetItemData(1, m_CboBDUnit1.AddString(GetTextByLan(_T("华氏度"), _T("Fahrenheit degree"))));
	//m_CboBDUnit1.SetItemData(2, m_CboBDUnit1.AddString(GetTextByLan(_T("开尔文"), _T("degree Kelvin"))));    //The protocol is there, the device does not support it, the requirements are the same as IE, note it out

	m_CboBDTemUnit2.SetItemData(0, m_CboBDTemUnit2.AddString(GetTextByLan(_T("摄氏度"), _T("centigrade"))));
	m_CboBDTemUnit2.SetItemData(1, m_CboBDTemUnit2.AddString(GetTextByLan(_T("华氏度"), _T("Fahrenheit degree"))));
	//m_CboBDTemUnit2.SetItemData(2, m_CboBDTemUnit2.AddString(GetTextByLan(_T("开尔文"), _T("degree Kelvin"))));

	m_CboTemScaleType.SetCurSel(0);
	m_CboBDCorrectType.SetCurSel(0);
	m_CboBDNum.SetCurSel(0);
	m_CboBDUnit1.SetCurSel(0);
	m_CboBDTemUnit2.SetCurSel(0);

	m_sldBkThreshold.SetRange(0,100);
	m_sldBkThreshold.SetPos(0);
	SetDlgItemInt(IDC_STATIC_BLACKBODY_DETECT_THRESHOLD_VALUE, m_sldBkThreshold.GetPos());

	m_sldBkSharkTime.SetRange(0,100);
	m_sldBkSharkTime.SetPos(0);

	m_cboTemPosition.ResetContent();
	m_cboTemPosition.InsertString(0, "Reserved");
	m_cboTemPosition.InsertString(1, "Face");
	m_cboTemPosition.InsertString(2, "Eye");
	m_cboTemPosition.SetCurSel(0);

	SetDlgItemInt(IDC_STATIC_SHARK_TIME_VALUE, m_sldBkSharkTime.GetPos());

	SetDlgItemText(IDC_STATIC_TI_TEMSCALE_TYPE, GetTextByLan(_T("温标类型"), _T("TemStandard")));
	SetDlgItemText(IDC_CHECK_BODYTEM_CORRECT, GetTextByLan(_T("开启"), _T("Enable")));
	SetDlgItemText(IDC_CHECK_IT_CORRECT, GetTextByLan(_T("开启"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_BODYTEM_CORRECT, GetTextByLan(_T("体温补偿灵敏度"), _T("BodyTempSen")));
	SetDlgItemText(IDC_STATIC_TI_ITCORRECT, GetTextByLan(_T("智能校正灵敏度"), _T("ITCorrSen")));
	SetDlgItemText(IDC_STATIC_BLACKBODY_CORRECT, GetTextByLan(_T("黑体校正参数"), _T("ITCorrSen")));
	SetDlgItemText(IDC_CHECK_BLACKBODY_ENABLE, GetTextByLan(_T("黑体校正使能"), _T("BDCorrEnable")));
	SetDlgItemText(IDC_STATIC_BDCORRECT_TYPE, GetTextByLan(_T("黑体校正方式"), _T("BDCorrType")));
	SetDlgItemText(IDC_STATIC_BDNUM, GetTextByLan(_T("黑体个数"), _T("BDCount")));
	SetDlgItemText(IDC_STATIC_ID, GetTextByLan(_T("黑体ID"), _T("ID")));
	SetDlgItemText(IDC_STATIC_BDTEM, GetTextByLan(_T("温度数值"), _T("TempValue")));
	SetDlgItemText(IDC_STATIC_TEMUNIT, GetTextByLan(_T("温度单位"), _T("TempUnit")));
	SetDlgItemText(IDC_STATIC_BDDIS, GetTextByLan(_T("黑体距离:米"), _T("BDDis:M")));
	SetDlgItemText(IDC_STATIC_BDPT, GetTextByLan(_T("坐标（左上、右下）"), _T("Points")));

	SetDlgItemText(IDC_BUTTON_TI_TEMSCALE_TYPE_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_BODYTEM_CORRECT_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_TI_ITCORRECT_SET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_BUTTON_BDSET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_BDCORR_TEXT, GetTextByLan(_T("注: 目前只支持设置一个黑体参数"), _T("Note: only support one bold param now")));
	SetDlgItemText(IDC_CHECK_COMMONENABLE_TEMDETEC, GetTextByLan(_T("开启"), _T("Enable")));

	SetDlgItemText(IDC_STATIC_BLACKBODY_DETECT, GetTextByLan(_T("黑体异常报警"), _T("Black body abnormal alarm")));
	SetDlgItemText(IDC_CHECK_BLACKBODY_DETECT_ENABLE, GetTextByLan(_T("开启"), _T("Enable")));
	SetDlgItemText(IDC_STATIC_BLACKBODY_DETECT_THRESHOLD, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
	SetDlgItemText(IDC_STATIC_SHARK_TIME, GetTextByLan(_T("去抖时间(s)"), _T("Time to shake(s)")));
	SetDlgItemText(IDC_BUTTON_BK_DETECT, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_TEMP_POSITION, GetTextByLan(_T("测温部位"), _T("TemPos")));


	
	m_CboBDNum.EnableWindow(FALSE);
}

void CLS_DlgThermography::UpdateParam()
{
	UpdateUI_BkDetect();

	int iChanNo = 0;
	if (m_iChannelNo >= 0)
	{
		iChanNo = m_iChannelNo;	
	}
	int iReturn = -1;
	TemperatureScaleType tTemperatureScaleType = {0};
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_TEMPERATURE_STANDARD, iChanNo, &tTemperatureScaleType, (int)sizeof(tTemperatureScaleType), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_CboTemScaleType.SetCurSel(tTemperatureScaleType.iTempStandard - 1);
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_TEMPERATURE_STANDARD) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_TEMPERATURE_STANDARD) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}


	BodyTempCorrect tBodyTempCorrect = {0};
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BODYTEMP_CORRECT, iChanNo, &tBodyTempCorrect, (int)sizeof(tBodyTempCorrect), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_ChkBodyTemCorrect.SetCheck(tBodyTempCorrect.iBodyTempCorrectEnable);
		if (tBodyTempCorrect.iBodyTempCorrectEnable)
		{
			m_EdtBodyTemCorrect.EnableWindow(TRUE);
		}
		else
		{
			m_EdtBodyTemCorrect.EnableWindow(FALSE);
		}
		SetDlgItemInt(IDC_EDIT_BODYTEM_CORRECT, tBodyTempCorrect.iBodyTempCorrectSensitivity);
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_BODYTEMP_CORRECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_BODYTEMP_CORRECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
	
	IntelligentCorretct tIntelligentCorretct = {0};
    iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_INTELLIGENT_CORRECT, iChanNo, &tIntelligentCorretct, (int)sizeof(tIntelligentCorretct), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_CboIntellectCorrect.SetCheck(tIntelligentCorretct.iIntelligentCorrectEnable);
		if (tIntelligentCorretct.iIntelligentCorrectEnable)
		{
			m_EdtIntellectCorrect.EnableWindow(TRUE);
		}
		else
		{
			m_EdtIntellectCorrect.EnableWindow(FALSE);
		}
		SetDlgItemInt(IDC_EDIT_TI_ITCORRECT, tIntelligentCorretct.iIntelligentCorrectSensitivity);
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_INTELLIGENT_CORRECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_INTELLIGENT_CORRECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}

	BlackbodyCorrection tBlackbodyCorrection = {0};
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BLACKBODY_CORRECT, iChanNo, &tBlackbodyCorrection, (int)sizeof(tBlackbodyCorrection), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_ChkBDCorrect.SetCheck(tBlackbodyCorrection.iBlackBodyCorrectEnable);
		m_CboBDCorrectType.SetCurSel(tBlackbodyCorrection.iBlackBodyCorrectType - 1);
		m_CboBDNum.SetCurSel(tBlackbodyCorrection.iBlackBodyNum - 1);
		for (int i = 0; i < MAX_BLACKBODY_COUNT; i++)
		{
			CString cstrTemp;
			cstrTemp.Format("%.1f", (float)tBlackbodyCorrection.tParam[i].iBlackBodyTemp / 100.0);
			SetDlgItemText(IDC_EDIT_BDTEM1 + i, cstrTemp);
			CString cstrDis;
			cstrDis.Format("%.2f", (float)tBlackbodyCorrection.tParam[i].iBlackBodyDistance / 100.0);
			SetDlgItemText(IDC_EDIT_BDDIS1 + i, cstrDis);
			CString cstrRect;
			cstrRect.Format("%d:%d:%d:%d",  tBlackbodyCorrection.tParam[i].tRect.left, tBlackbodyCorrection.tParam[i].tRect.top, tBlackbodyCorrection.tParam[i].tRect.right, tBlackbodyCorrection.tParam[i].tRect.bottom);
			SetDlgItemText(IDC_EDIT_BDPT1 + i, cstrRect);
		}
		m_CboBDUnit1.SetCurSel(tBlackbodyCorrection.tParam[0].iBlackBodyTempUnit - 1);
		m_CboBDTemUnit2.SetCurSel(tBlackbodyCorrection.tParam[1].iBlackBodyTempUnit - 1);
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_BLACKBODY_CORRECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_BLACKBODY_CORRECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
	int iEnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_TEMDETECT, iChanNo, &iEnable);
	if (RET_SUCCESS == iRet)
	{
		if (COMMONENABLE_TEM_DETECT_ENABLE == iEnable)
		{
			m_ChkComEnableTemDetec.SetCheck(iEnable);
			GetDlgItem(IDC_BUTTON_TI_TEMSCALE_TYPE_SET)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_BODYTEM_CORRECT_SET)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_TI_ITCORRECT_SET)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_BDSET)->EnableWindow(TRUE);
		}
		//else if (COMMONENABLE_TEM_DETECT_DISABLE == iEnable)
		else
		{
			m_ChkComEnableTemDetec.SetCheck(FALSE);
			GetDlgItem(IDC_BUTTON_TI_TEMSCALE_TYPE_SET)->EnableWindow(FALSE);
			GetDlgItem(IDC_BUTTON_BODYTEM_CORRECT_SET)->EnableWindow(FALSE);
			GetDlgItem(IDC_BUTTON_TI_ITCORRECT_SET)->EnableWindow(FALSE);
			GetDlgItem(IDC_BUTTON_BDSET)->EnableWindow(FALSE);
		}
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_GetCommonEnable(CI_COMMON_ID_TEMDETECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_GetCommonEnable(CI_COMMON_ID_TEMDETECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}

	TemperaturePosition tTemperaturePosition = {0};
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_TEMPETURE_POSITION, iChanNo, &tTemperaturePosition, (int)sizeof(tTemperaturePosition), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		m_cboTemPosition.SetCurSel(tTemperaturePosition.iTempPosition);
	}
}

void CLS_DlgThermography::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	//UpdateUIText();
	UpdateParam();
}

void CLS_DlgThermography::OnLanguageChanged(int _iLanguage)
{	
	UpdateUIText();
	UpdateParam();
}

void CLS_DlgThermography::OnBnClickedButtonTiTemscaleTypeSet()
{
	// TODO: Add control notification handler code here
	int iChanNo = 0;
	if (m_iChannelNo >= 0)
	{
		iChanNo = m_iChannelNo;	
	}

	TemperatureScaleType tInfo = {0};
	tInfo.iChanNo = iChanNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iTempStandard = m_CboTemScaleType.GetCurSel() + 1;
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_TEMPERATURE_STANDARD, iChanNo, &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_TEMPERATURE_STANDARD) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_TEMPERATURE_STANDARD) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgThermography::OnBnClickedButtonBodytemCorrectSet()
{
	// TODO: Add control notification handler code here
	int iChanNo = 0;
	if (m_iChannelNo >= 0)
	{
		iChanNo = m_iChannelNo;	
	}

	BodyTempCorrect tInfo = {0};
	tInfo.iChanNo = iChanNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iBodyTempCorrectEnable = m_ChkBodyTemCorrect.GetCheck();
	tInfo.iBodyTempCorrectSensitivity = GetDlgItemInt(IDC_EDIT_BODYTEM_CORRECT);
	if (tInfo.iBodyTempCorrectSensitivity < 0 || tInfo.iBodyTempCorrectSensitivity > 100)
	{
		MessageBox("Param Error!","Info",0);
		return;
	}
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BODYTEMP_CORRECT, iChanNo, &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_BODYTEMP_CORRECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_BODYTEMP_CORRECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgThermography::OnBnClickedButtonTiItcorrectSet()
{
	// TODO: Add control notification handler code here
	int iChanNo = 0;
	if (m_iChannelNo >= 0)
	{
		iChanNo = m_iChannelNo;	
	}

	IntelligentCorretct tInfo = {0};
	tInfo.iChanNo = iChanNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iIntelligentCorrectEnable = m_CboIntellectCorrect.GetCheck();
	tInfo.iIntelligentCorrectSensitivity = GetDlgItemInt(IDC_EDIT_TI_ITCORRECT);
	if (tInfo.iIntelligentCorrectSensitivity < 0 || tInfo.iIntelligentCorrectSensitivity > 100)
	{
		MessageBox("Param Error!","Info",0);
		return;
	}
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_INTELLIGENT_CORRECT, iChanNo, &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_INTELLIGENT_CORRECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_INTELLIGENT_CORRECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgThermography::OnBnClickedButtonBdset()
{
	// TODO: Add control notification handler code here
	int iChanNo = 0;
	if (m_iChannelNo >= 0)
	{
		iChanNo = m_iChannelNo;	
	}

	BlackbodyCorrection tInfo = {0};
	tInfo.iChanNo = iChanNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iBlackBodyCorrectEnable = m_ChkBDCorrect.GetCheck();
	tInfo.iBlackBodyCorrectType = m_CboBDCorrectType.GetCurSel() + 1;
	tInfo.iBlackBodyNum = m_CboBDNum.GetCurSel() + 1; 
	tInfo.tParam[0].iBlackBodyTempUnit = m_CboBDUnit1.GetCurSel() + 1;
	tInfo.tParam[1].iBlackBodyTempUnit = m_CboBDTemUnit2.GetCurSel() + 1;
	for (int i = 0; i < tInfo.iBlackBodyNum && i < MAX_BLACKBODY_COUNT; i++)
	{
		tInfo.tParam[i].iBlackBodyId = i;
		CString  cstrTemp;
		GetDlgItemText(IDC_EDIT_BDTEM1 + i ,cstrTemp);
		tInfo.tParam[i].iBlackBodyTemp = (int)((atof(cstrTemp) + 0.001)*100);
		CString cstrDis;
		GetDlgItemText(IDC_EDIT_BDDIS1 + i ,cstrDis);
		tInfo.tParam[i].iBlackBodyDistance = (int)((atof(cstrDis) + 0.001)*100);
		CString cstrRect;
		GetDlgItemText(IDC_EDIT_BDPT1 + i ,cstrRect);
		char cRect[MAX_RECT_TEXT_LENGTH] = "";
		memcpy(cRect, cstrRect, min(MAX_RECT_TEXT_LENGTH, cstrRect.GetLength()));
		sscanf_s(cRect, "%d:%d:%d:%d", &tInfo.tParam[i].tRect.left, &tInfo.tParam[i].tRect.top, &tInfo.tParam[i].tRect.right, &tInfo.tParam[i].tRect.bottom);
	}
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BLACKBODY_CORRECT, iChanNo, &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_BLACKBODY_CORRECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_BLACKBODY_CORRECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgThermography::OnBnClickedCheckBodytemCorrect()
{
	// TODO: Add control notification handler code here
	if (!m_ChkBodyTemCorrect.GetCheck())
	{
		m_EdtBodyTemCorrect.EnableWindow(FALSE);
	}
	else
	{
		m_EdtBodyTemCorrect.EnableWindow(TRUE);
	}
}

void CLS_DlgThermography::OnBnClickedCheckItCorrect()
{
	// TODO: Add control notification handler code here
	if (!m_CboIntellectCorrect.GetCheck())
	{
		m_EdtIntellectCorrect.EnableWindow(FALSE);
	}
	else
	{
		m_EdtIntellectCorrect.EnableWindow(TRUE);
	}
}

void CLS_DlgThermography::OnBnClickedCheckCommonenableTemdetec()
{
	// TODO: Add control notification handler code here
	int iEnable = 0;
	if (m_ChkComEnableTemDetec.GetCheck()) {
		iEnable = COMMONENABLE_TEM_DETECT_ENABLE;
		GetDlgItem(IDC_BUTTON_TI_TEMSCALE_TYPE_SET)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_BODYTEM_CORRECT_SET)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_TI_ITCORRECT_SET)->EnableWindow(TRUE);
		GetDlgItem(IDC_BUTTON_BDSET)->EnableWindow(TRUE);
	}
	else {
		iEnable = COMMONENABLE_TEM_DETECT_DISABLE;
		GetDlgItem(IDC_BUTTON_TI_TEMSCALE_TYPE_SET)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_BODYTEM_CORRECT_SET)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_TI_ITCORRECT_SET)->EnableWindow(FALSE);
		GetDlgItem(IDC_BUTTON_BDSET)->EnableWindow(FALSE);
	}
 	int iChanNo = 0;
	if (m_iChannelNo >= 0)
	{
		iChanNo = m_iChannelNo;	
	}
	int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_TEMDETECT, iChanNo, iEnable);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] OnBnClickedCheckCommonenableTemdetec(CI_COMMON_ID_TEMDETECT) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] OnBnClickedCheckCommonenableTemdetec(CI_COMMON_ID_TEMDETECT) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}

}


void CLS_DlgThermography::OnBnClickedButtonBkDetect()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_DlgThermography::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	
	BlackBodyDetect tInfo = {0};
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iSize = sizeof(tInfo);
	tInfo.iEnable = m_chkBkDetectEnable.GetCheck();
	tInfo.iThreshold = m_sldBkThreshold.GetPos();
	tInfo.iSharkTime = m_sldBkSharkTime.GetPos();
	
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_BLACK_BODY_DETECT, m_iChannelNo, &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_BLACK_BODY_DETECT) Success LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_BLACK_BODY_DETECT) Failed LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgThermography::OnNMCustomdrawSliderBlackbodyDetectThreshold(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_BLACKBODY_DETECT_THRESHOLD_VALUE, m_sldBkThreshold.GetPos());
	*pResult = 0;
}

void CLS_DlgThermography::OnNMCustomdrawSliderSharkTime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_SHARK_TIME_VALUE, m_sldBkSharkTime.GetPos());
	*pResult = 0;
}

void CLS_DlgThermography::UpdateUI_BkDetect()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_DlgThermography::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}

	int iReturn = -1;
	BlackBodyDetect tInfo = {0};
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iSize = sizeof(tInfo);

	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_BLACK_BODY_DETECT, m_iChannelNo, &tInfo, (int)sizeof(tInfo), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		 m_chkBkDetectEnable.SetCheck(tInfo.iEnable);
		 m_sldBkThreshold.SetPos(tInfo.iThreshold);
		 m_sldBkSharkTime.SetPos(tInfo.iSharkTime);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_GetDevConfig(NET_CLIENT_BLACK_BODY_DETECT) Failed LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}
void CLS_DlgThermography::OnCbnSelchangeComboTempeturePosition()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_DlgThermography::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
		return;
	}
	TemperaturePosition tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iTempPosition = m_cboTemPosition.GetCurSel();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_TEMPETURE_POSITION, m_iChannelNo, &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_TEMPETURE_POSITION) Success LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgThermography] NetClient_SetDevConfig(NET_CLIENT_TEMPETURE_POSITION) Failed LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}
