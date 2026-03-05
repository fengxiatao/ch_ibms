// E:\SDK_ALL\trunk\Demo\NetClientDemo\Config\CLS_TribleVCA.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\Config\CLS_TribleVCA.h"


// CLS_TribleVCA dialog

IMPLEMENT_DYNAMIC(CLS_TribleVCA, CDialog)

CLS_TribleVCA::CLS_TribleVCA(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_TribleVCA::IDD, pParent)
{

}

CLS_TribleVCA::~CLS_TribleVCA()
{
}

void CLS_TribleVCA::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_SMD_ENCODE, m_chkSmdEncode);
	DDX_Control(pDX, IDC_CHECK_SMD_IMAGE, m_chkSmdArea);
}


BEGIN_MESSAGE_MAP(CLS_TribleVCA, CDialog)
	ON_BN_CLICKED(IDC_BUTTON1, &CLS_TribleVCA::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_CHECK_SMD_ENCODE, &CLS_TribleVCA::OnBnClickedCheckSmdEncode)
	ON_BN_CLICKED(IDC_CHECK_SMD_IMAGE, &CLS_TribleVCA::OnBnClickedCheckSmdImage)
	ON_BN_CLICKED(IDC_CHECK_SMD_SCENE, &CLS_TribleVCA::OnBnClickedCheckSmdScene)
END_MESSAGE_MAP()


// CLS_TribleVCA message handler
BOOL CLS_TribleVCA::OnInitDialog()
{
	CDialog::OnInitDialog();

	UpdateUI();

	GetAbilitity();

	return TRUE;  
}

void CLS_TribleVCA::UpdateUI()
{
	SetDlgItemText(IDC_STATIC_ABILITY, GetTextByLan(_T("能力集"), _T("Abilitity")));
	SetDlgItemText(IDC_CHECK_SMD_ENCODE, GetTextByLan(_T("智能编码使能"), _T("SMD_ENCODE")));
	SetDlgItemText(IDC_CHECK_SMD_IMAGE, GetTextByLan(_T("智能图像使能"), _T("SMD_IMAGE")));
	SetDlgItemText(IDC_STATIC_SMD_ALARM_AREA, GetTextByLan(_T("智能报警区域"), _T("SMD_ALARM_AREA")));
	SetDlgItemText(IDC_STATIC_SMDENCODE_ENABLE, GetTextByLan(_T("智能编码"), _T("SMD_ENCODE")));
	SetDlgItemText(IDC_STATIC_SMDIMAGE_ABILITITY, GetTextByLan(_T("智能图像"), _T("SMD_IMAGE")));
	SetDlgItemText(IDC_STATIC_SMDALARMAREA_ABILITITY, GetTextByLan(_T("智能报警区域"), _T("SMD_ALARM_AREA")));
	SetDlgItemText(IDC_STATIC_MULTI_AREA_ABILITITY, GetTextByLan(_T("多区域报警"), _T("MULTI_AREA_ALARM")));
	SetDlgItemText(IDC_STATIC_LEFT, GetTextByLan(_T("左边距"), _T("Left")));
	SetDlgItemText(IDC_STATIC_TOP, GetTextByLan(_T("上边距"), _T("Top")));
	SetDlgItemText(IDC_STATIC_RIGHT, GetTextByLan(_T("右边距"), _T("Right")));
	SetDlgItemText(IDC_STATIC_BOTTOM, GetTextByLan(_T("下边距"), _T("Bottom")));
	SetDlgItemText(IDC_BUTTON1, GetTextByLan(_T("设置"), _T("Set")));
	m_chkSmdArea.SetCheck(0);
	m_chkSmdEncode.SetCheck(0);
}

void CLS_TribleVCA::GetAbilitity()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[ CLS_TribleVCA::GetAbilitity] Error  LogonID!");
		return;
	}


	int iByteReturn = -1;
	FuncAbilityLevel stSmdEncodeAbility = {0};
	stSmdEncodeAbility.iSize = sizeof(stSmdEncodeAbility);
	stSmdEncodeAbility.iMainFuncType = 0x9;
	stSmdEncodeAbility.iSubFuncType = 36;
	
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stSmdEncodeAbility, sizeof(stSmdEncodeAbility), &iByteReturn);
	if (iRet < 0 || strlen(stSmdEncodeAbility.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[ CLS_TribleVCA::GetAbilitity] GetDevConfig 0x9 Failed! m_iLogonID %d iSubFuncType = 36", m_iLogonID);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_SMDENCODE_ENABLE, _ttoi(stSmdEncodeAbility.cParam) );
	}

	iByteReturn = -1;
	FuncAbilityLevel stSmdImageAbility = {0};
	stSmdImageAbility.iSize = sizeof(stSmdImageAbility);
	stSmdImageAbility.iMainFuncType = 0x9;
	stSmdImageAbility.iSubFuncType = 37;

	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stSmdImageAbility, sizeof(stSmdImageAbility), &iByteReturn);
	if (iRet < 0 || strlen(stSmdImageAbility.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[ CLS_TribleVCA::GetAbilitity] GetDevConfig 0x9 Failed! m_iLogonID %d iSubFuncType = 37", m_iLogonID);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_SMDIMAGE_ABILITITY, _ttoi(stSmdImageAbility.cParam) );
	}

	iByteReturn = -1;
	FuncAbilityLevel stAlarmAreaAbility = {0};
	stAlarmAreaAbility.iSize = sizeof(stAlarmAreaAbility);
	stAlarmAreaAbility.iMainFuncType = 0x9;
	stAlarmAreaAbility.iSubFuncType = 38;

	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stAlarmAreaAbility, sizeof(stAlarmAreaAbility), &iByteReturn);
	if (iRet < 0 || strlen(stAlarmAreaAbility.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[ CLS_TribleVCA::GetAbilitity] GetDevConfig 0x9 Failed! m_iLogonID %d iSubFuncType = 38", m_iLogonID);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_ALARMAREA_ABILITITY, _ttoi(stAlarmAreaAbility.cParam) );
	}

	iByteReturn = -1;
	FuncAbilityLevel stMultiAreaAbility = {0};
	stMultiAreaAbility.iSize = sizeof(stMultiAreaAbility);
	stMultiAreaAbility.iMainFuncType = 0x9;
	stMultiAreaAbility.iSubFuncType = 53;

	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stMultiAreaAbility, sizeof(stMultiAreaAbility), &iByteReturn);
	if (iRet < 0 || strlen(stMultiAreaAbility.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[ CLS_TribleVCA::GetAbilitity] GetDevConfig 0x9 Failed! m_iLogonID %d iSubFuncType = 53", m_iLogonID);
	}
	else
	{
		SetDlgItemInt(IDC_EDIT__MULTI_AREA_ABILITITY, _ttoi(stMultiAreaAbility.cParam) );
	}
}

void CLS_TribleVCA::OnLanguageChanged( int _iLanguage)
{
	UpdateUI();
	UpdateParameter();
}

void CLS_TribleVCA::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	SmdSceneEnable tInfo = {0};
	int iRet = -1;
	int iReturn = -1;
	switch(_iParaType)
	{
	case PARA_SMD_SCENE_ENABLE:
		iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SMD_SCENE_ENABLE, m_iChannelNO, &tInfo, sizeof(SmdSceneEnable),&iReturn);
		if (iRet >= 0)
		{
			AddLog(LOG_TYPE_SUCC, "", "[OnParamChangeNotify]NET_CLIENT_SMD_SCENE_ENABLE,%d",tInfo.iEnable);
		}
		break;
	}
}

void CLS_TribleVCA::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else 
	{
		m_iLogonID = _iLogonID;
	}
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;    
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}

	UpdateParameter();
	GetAbilitity();

}

void CLS_TribleVCA::UpdateParameter()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_TribleVCA]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	SmdEncodeEnable tEncodeEnable = {0};
	int iReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SMD_ENCODE_ENABLE,m_iChannelNO, &tEncodeEnable, sizeof(tEncodeEnable), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_SMD_ENCODE_ENABLE fail!");
	}
	else
	{
		m_chkSmdEncode.SetCheck(tEncodeEnable.iEnable);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_SMD_ENCODE_ENABLE success!");
	}
	
	SmdImageEnable tImageEnable = {0};
	iReturn = -1;
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SMD_IMAGE_ENABLE,m_iChannelNO, &tImageEnable, sizeof(tImageEnable), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_SMD_IMAGE_ENABLE fail!");
	}
	else
	{
		m_chkSmdArea.SetCheck(tImageEnable.iEnable);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_SMD_IMAGE_ENABLE success!");
	}
	
	SmdAlarmArea tAlarmArea = {0};
	iReturn = -1;
	 
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SMD_ALARM_AREA,m_iChannelNO, &tAlarmArea, sizeof(tAlarmArea), &iReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_SMD_ALARM_AREA fail!");
	}
	else
	{
		SetDlgItemInt(IDC_EDIT_LEFT, tAlarmArea.iLeftMargin);
		SetDlgItemInt(IDC_EDIT_TOP, tAlarmArea.iTopMargin);
		SetDlgItemInt(IDC_EDIT_RIGHT, tAlarmArea.iRightMargin);
		SetDlgItemInt(IDC_EDIT_BOTTOM, tAlarmArea.iBottomMargin);
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_SMD_ALARM_AREA success!");
	}

}

void CLS_TribleVCA::OnBnClickedButton1()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_TribleVCA]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	SmdAlarmArea tInfo = {0};
	tInfo.iLeftMargin = GetDlgItemInt(IDC_EDIT_LEFT);
	tInfo.iBottomMargin = GetDlgItemInt(IDC_EDIT_BOTTOM);
	tInfo.iTopMargin = GetDlgItemInt(IDC_EDIT_TOP);
	tInfo.iRightMargin = GetDlgItemInt(IDC_EDIT_RIGHT);
	if((tInfo.iLeftMargin < 0 || tInfo.iBottomMargin < 0 || tInfo.iTopMargin < 0 || tInfo.iRightMargin < 0) || (tInfo.iLeftMargin + tInfo.iRightMargin) >= 10000 || (tInfo.iTopMargin + tInfo.iBottomMargin) >= 10000)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig] Illegal Parameter!");
		return;
	}

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SMD_ALARM_AREA, m_iChannelNO, &tInfo, sizeof(SmdAlarmArea));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_ALARM_AREA fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_ALARM_AREA success!");
	}

}

void CLS_TribleVCA::OnBnClickedCheckSmdEncode()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_TribleVCA]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	SmdEncodeEnable tInfo = {0};
	tInfo.iEnable = m_chkSmdEncode.GetCheck();
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SMD_ENCODE_ENABLE, m_iChannelNO, &tInfo, sizeof(SmdEncodeEnable));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_ENCODE_ENABLE fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_ENCODE_ENABLE success!");
	}
}

void CLS_TribleVCA::OnBnClickedCheckSmdImage()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_TribleVCA]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	SmdImageEnable tInfo = {0};
	tInfo.iEnable = m_chkSmdArea.GetCheck();
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SMD_IMAGE_ENABLE, m_iChannelNO, &tInfo, sizeof(SmdImageEnable));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_IMAGE_ENABLE fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_IMAGE_ENABLE success!");
	}
}


void CLS_TribleVCA::OnBnClickedCheckSmdScene()
{
	// TODO: Add control notification handler code here
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_TribleVCA]Invalid Logon id or Channel number(%d,%d)", m_iLogonID, m_iChannelNO);
		return;
	}
	SmdSceneEnable tInfo = {0};
	tInfo.iEnable = m_chkSmdArea.GetCheck();
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SMD_SCENE_ENABLE, m_iChannelNO, &tInfo, sizeof(SmdSceneEnable));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_SCENE_ENABLE fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig]NET_CLIENT_SMD_SCENE_ENABLE success!");
	}
}
