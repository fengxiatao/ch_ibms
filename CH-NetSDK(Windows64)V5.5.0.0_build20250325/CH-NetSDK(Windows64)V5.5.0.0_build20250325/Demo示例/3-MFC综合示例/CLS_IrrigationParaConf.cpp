// CLS_IrrigationParaConf.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_IrrigationParaConf.h"

#define RAIN_FALL_ALARM_PARA_DEFAULT80       80
#define RAIN_FALL_ALARM_PARA_DEFAULT120      120
#define RAIN_FALL_ALARM_PARA_DEFAULT160      160
#define	VCA_MAX_RULE_NUM_EX				     8		

// CLS_IrrigationParaConf dialog

IMPLEMENT_DYNAMIC(CLS_IrrigationParaConf, CDialog)

CLS_IrrigationParaConf::CLS_IrrigationParaConf(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_IrrigationParaConf::IDD, pParent)
{

}

CLS_IrrigationParaConf::~CLS_IrrigationParaConf()
{
}

void CLS_IrrigationParaConf::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_RAIN_FALL_ALERT_VALUE, m_cboRainFallAlertThershold);
	DDX_Control(pDX, IDC_COMBO_SENCEID, m_cboSenceID);
	DDX_Control(pDX, IDC_COMBO_RULE_ID, m_cboRuleID);
	DDX_Control(pDX, IDC_COMBO_DATA_SROUCE, m_cboWaterLevelAlertDataSource);
	DDX_Control(pDX, IDC_CHECK_MIAN_CHANNEL, m_btnIsMainChannel);
	DDX_Control(pDX, IDC_EDIT_LINKAGE_TIME, m_edtLinkageTime);
	DDX_Control(pDX, IDC_EDIT_RAIN_TIME_INTERVAL, m_edtRainTimeInterval);
	DDX_Control(pDX, IDC_COMBO_ALGO_TYPE, m_cboAlgoType);
	DDX_Control(pDX, IDC_CHECK_RAINFALL, m_chkRainEnable);
	DDX_Control(pDX, IDC_CHECK_ALERTWATERENABLE, m_chkWaterLevelAlert);
	DDX_Control(pDX, IDC_EDIT_RAIN_FALL_DEFAULT_UPLOAD_CHECK_TIME, m_edtDefaultUploadTime);
	DDX_Control(pDX, IDC_EDIT_UPLOAD_CHECK_TIME, m_edtUploadCheckTime1);
	DDX_Control(pDX, IDC_EDIT_UPLOAD_CHECK_TIME_HEAVY_RAIN, m_edtUploadCheckTime2);
	DDX_Control(pDX, IDC_EDIT_UPLOAD_CHECK_TIME_TORRENTIAL_RAIN, m_edtUploadCheckTime3);
	DDX_Control(pDX, IDC_EDIT_THERSHOLD, m_edtThreShold);
	DDX_Control(pDX, IDC_EDIT_THERSHOLD_HEAVY_RAIN, m_edtTherShold2);
	DDX_Control(pDX, IDC_EDIT_THERSHOLD_TORRENTIAL_RAIN, m_edtTherShold3);
	DDX_Control(pDX, IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD, m_edtAlertData);
	DDX_Control(pDX, IDC_EDIT_WATER_LINKAGE_TIME, m_edtWaterLinkAge);
	DDX_Control(pDX, IDC_EDIT_DEFAULT_UPLOAD_CHECK_TIME, m_edtWaterUploadTime);
	DDX_Control(pDX, IDC_EDIT_FIRST_PHASE_UPLAOD_CHECK_TIME, m_edtFirstUploadTime);
	DDX_Control(pDX, IDC_EDIT_SECOND_PHASE_CHECK_TIME, m_edtSecondTime);
	DDX_Control(pDX, IDC_EDIT_THRID_PHASE_CHECK_TIME, m_edtThirdTime);
	DDX_Control(pDX, IDC_EDIT_FIRST_THERSHOLD, m_edtWaterThershold1);
	DDX_Control(pDX, IDC_EDIT_SECOND_SHERSHOLD, m_edtWaterThershold2);
	DDX_Control(pDX, IDC_EDIT_THRID_SHERSHOLD, m_edtWaterThershold3);
	DDX_Control(pDX, IDC_EDIT_LOWWATER, m_edtLowWaterLevel);
}


BEGIN_MESSAGE_MAP(CLS_IrrigationParaConf, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET_RAINFALL, &CLS_IrrigationParaConf::OnBnClickedButtonSetRainfallPara)
	ON_BN_CLICKED(IDC_BUTTON_SET_ALERT_WATER_LEVEL, &CLS_IrrigationParaConf::OnBnClickedButtonSetAlertWaterLevelPara)
	ON_CBN_SELCHANGE(IDC_COMBO_SENCEID, &CLS_IrrigationParaConf::OnCbnSelchangeComboSenceID)
	ON_CBN_SELCHANGE(IDC_COMBO_RULE_ID, &CLS_IrrigationParaConf::OnCbnSelchangeComboRuleId)
	ON_CBN_SELCHANGE(IDC_COMBO_ALGO_TYPE, &CLS_IrrigationParaConf::OnCbnSelchangeComboAlgoType)
	ON_WM_CTLCOLOR()
	ON_BN_CLICKED(IDC_CHECK_RAINFALL, &CLS_IrrigationParaConf::OnBnClickedCheckRainfall)
	ON_BN_CLICKED(IDC_CHECK_ALERTWATERENABLE, &CLS_IrrigationParaConf::OnBnClickedCheckAlertwaterenable)
	ON_STN_CLICKED(IDC_STATIC_ALGO_TYPE, &CLS_IrrigationParaConf::OnStnClickedStaticAlgoType)
	ON_BN_CLICKED(IDC_BUTTON_ALARM_CONTROL, &CLS_IrrigationParaConf::OnBnClickedButtonAlarmControl)
END_MESSAGE_MAP()


// CLS_IrrigationParaConf message handler
void CLS_IrrigationParaConf::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG,"","[CLS_IrrigationParaConf::OnParamChangeNotify]->Invalid logon id(%d)", _iLogonID);
		return;
	}

	if (_iChannelNo == m_iChannelNO)
	{
		switch(_iParaType)
		{
		case PARA_ALARM_RAINFALL:
			{				
				AddLog(LOG_TYPE_SUCC,"","[CLS_IrrigationParaConf::OnParamChangeNotify]->PARA_ALARM_RAINFALL (%d,%d)",m_iLogonID, m_iChannelNO);
				UI_UpdatePara();
			}
			break;
		case PARA_ALARM_ALERTWATER:
			{
				AddLog(LOG_TYPE_SUCC,"","[CLS_IrrigationParaConf::OnParamChangeNotify]->PARA_ALARM_ALERTWATER (%d,%d)",m_iLogonID, m_iChannelNO);
				UI_UpdatePara();
			}
			break;
		default:
			break;
		}
	}
}

void CLS_IrrigationParaConf::OnBnClickedButtonSetRainfallPara()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "[OnBnClickedButtonSetRainfallPara]->Invalid Logon id(%d)", m_iLogonID);
		return;
	}
	//int iEnable = m_chkRainEnable.GetCheck();
	//int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_RAINFALL_ALARM, m_iChannelNO, iEnable);
	//if(0 == iRet)
	//{
	//	AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable[CI_COMMON_ID_RAINFALL_ALARM] success!");
	//}
	//else
	//{
	//	AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable[CI_COMMON_ID_RAINFALL_ALARM] fail!");
	//}
	if (!CheckParaValid())
	{
		MessageBox(GetTextByLan(_T("请重新填写雨量阈值(中雨<大雨<暴雨)"),_T("Please Input Rainfall threshold data again(Medium Rain < Heavy Rain < Heavy Rain)")));
		return;
	}
	RainFallAlarmInfo tInfo = {0};
	tInfo.iSize = sizeof(RainFallAlarmInfo);
	tInfo.iUploadTime = GetDlgItemInt(IDC_EDIT_RAIN_FALL_DEFAULT_UPLOAD_CHECK_TIME);
	tInfo.iRainfallValue = GetDlgItemInt(IDC_COMBO_RAIN_FALL_ALERT_VALUE);
	tInfo.iAlarmTime = GetDlgItemInt(IDC_EDIT_LINKAGE_TIME);
	tInfo.iRainTimeInter = GetDlgItemInt(IDC_EDIT_RAIN_TIME_INTERVAL);
	tInfo.iModerateRainUploadTime = GetDlgItemInt(IDC_EDIT_UPLOAD_CHECK_TIME);
	tInfo.iModerateRainThershold = GetDlgItemInt(IDC_EDIT_THERSHOLD);
	tInfo.iHeavyRainUploadTime = GetDlgItemInt(IDC_EDIT_UPLOAD_CHECK_TIME_HEAVY_RAIN);
	tInfo.iHeavyRainThershold = GetDlgItemInt(IDC_EDIT_THERSHOLD_HEAVY_RAIN);
	tInfo.iTorrentialRainUploadTime = GetDlgItemInt(IDC_EDIT_UPLOAD_CHECK_TIME_TORRENTIAL_RAIN);
	tInfo.iTorrentialRainThershold = GetDlgItemInt(IDC_EDIT_THERSHOLD_TORRENTIAL_RAIN);

	if((tInfo.iRainfallValue > 0 && tInfo.iRainfallValue < 255)&&(tInfo.iUploadTime > 0)&&(tInfo.iAlarmTime > 0 && tInfo.iAlarmTime < 600)&&(tInfo.iRainTimeInter > 0)&&(tInfo.iModerateRainUploadTime > 0)&&(tInfo.iModerateRainThershold > 0)&&(tInfo.iHeavyRainUploadTime > 0)&&(tInfo.iHeavyRainThershold > 0)&&(tInfo.iTorrentialRainUploadTime > 0)&&(tInfo.iTorrentialRainThershold > 0))
	{
		int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNO, ALARM_TYPE_RAINFALL, CMD_ALARM_IN_RAINFALL_PARA, &tInfo);
		if(iRet != 0)
		{
			AddLog(LOG_TYPE_FAIL,"","[OnBnClickedButtonSetRainfallPara]->NetClient_SetAlarmConfig(m_iLogonID %d)", m_iLogonID);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC,"","[OnBnClickedButtonSetRainfallPara]->NetClient_SetAlarmConfig(m_iLogonID %d)", m_iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[OnBnClickedButtonSetRainfallPara]->Illegal Parameter(m_iLogonID %d)", m_iLogonID);
		MessageBox(GetTextByLan(_T("设置雨量预警参数失败！！！"),_T("Set RainFall Alarm Parameter Failed！！！")));
		UI_UpdatePara();
	}
}

void CLS_IrrigationParaConf::OnBnClickedButtonSetAlertWaterLevelPara()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "[OnBnClickedButtonSetAlertWaterLevelPara]->Invalid Logon id(%d)", m_iLogonID);
		return;
	}
	int iRet = RET_FAILED;
	//int iEnable = m_chkWaterLevelAlert.GetCheck();
	//int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_ALERTWATER_ALARM, m_iChannelNO, iEnable);
	//if(0 == iRet)
	//{
	//	AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable[CI_COMMON_ID_ALERTWATER_ALARM] success!");
	//}
	//else
	//{
	//	AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable[CI_COMMON_ID_ALERTWATER_ALARM] fail!");
	//}
	if (!CheckParaValidInWater())
	{
		MessageBox(GetTextByLan(_T("请重新填写积水深度阈值(第一段阈值<第二段阈值<第三段阈值)"),_T("Please Input Rainfall threshold data again(First Data < Second Data < Third Data)")));
		return;
	}
	AlertWaterAlarmInfo tInfo = {0};
	tInfo.iSize = sizeof(AlertWaterAlarmInfo);
	CString sAlertWaterValue = "";
	
	GetDlgItemText(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD, sAlertWaterValue);
	//Convert string to float
	CString sTmp;
	double fValue = atof(sAlertWaterValue);
	if (fValue < 0)
	{
		fValue -= 0.0005;
	}
	else
		fValue += 0.0005;
	tInfo.iAlertWaterValue = (int)(fValue*1000);
	
	tInfo.iAlarmLinkTime = GetDlgItemInt(IDC_EDIT_WATER_LINKAGE_TIME);
	tInfo.iSenceID = m_cboSenceID.GetCurSel();
	tInfo.iRuleID = m_cboRuleID.GetCurSel();
	tInfo.iDefaultUploadTime = GetDlgItemInt(IDC_EDIT_DEFAULT_UPLOAD_CHECK_TIME); 
	tInfo.iCheckStand = m_cboWaterLevelAlertDataSource.GetCurSel() + 1; 
	tInfo.iIsMainChannel = m_btnIsMainChannel.GetCheck();
	tInfo.iFirstWaterLevelUploadTime = GetDlgItemInt(IDC_EDIT_FIRST_PHASE_UPLAOD_CHECK_TIME);
	tInfo.iFirstWaterLevelThershold = GetDlgItemInt(IDC_EDIT_FIRST_THERSHOLD);
	tInfo.iSecondWaterLevelUploadTime = GetDlgItemInt(IDC_EDIT_SECOND_PHASE_CHECK_TIME);
	tInfo.iSecondWaterLevelThershold = GetDlgItemInt(IDC_EDIT_SECOND_SHERSHOLD);
	tInfo.iThirdWaterLevelUploadTime = GetDlgItemInt(IDC_EDIT_THRID_PHASE_CHECK_TIME);
	tInfo.iThirdWaterLevelThershold = GetDlgItemInt(IDC_EDIT_THRID_SHERSHOLD);
	tInfo.iAlgorithmType = m_cboAlgoType.GetCurSel() + 1;

	for (int i = 0;i < MAX_WATER_THERSHOLD_NUM; i++)
	{
		GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD + i)->GetWindowText(sAlertWaterValue);
		if (sAlertWaterValue.IsEmpty())
		{
			continue;
		}
		fValue = (float)atof(sAlertWaterValue);
		if (fValue < 0)
		{
			fValue -= 0.0005;
		}
		else
			fValue += 0.0005;
		tInfo.iWaterAlarm[tInfo.iWaterNum] = (int)(fValue*1000);
		tInfo.iWaterNum++;
	}
	m_edtLowWaterLevel.GetWindowText(sAlertWaterValue);
	fValue = (float)atof(sAlertWaterValue);
	if (fValue < 0)
	{
		fValue -= 0.0005;
	}
	else
		fValue += 0.0005;
	tInfo.iWaterLowValue = (int)(fValue*1000);
	if((tInfo.iAlertWaterValue > -10000000 && tInfo.iAlertWaterValue < 10000000) && (tInfo.iDefaultUploadTime > 0)&&(tInfo.iAlarmLinkTime > 0 && tInfo.iAlarmLinkTime < 600)&&(tInfo.iFirstWaterLevelUploadTime > 0)&&(tInfo.iFirstWaterLevelThershold > 0)&&(tInfo.iSecondWaterLevelUploadTime > 0)&&(tInfo.iSecondWaterLevelThershold > 0)&&(tInfo.iThirdWaterLevelUploadTime > 0)&&(tInfo.iThirdWaterLevelThershold > 0))
	{
		int iRet = NetClient_SetAlarmConfig(m_iLogonID, m_iChannelNO, ALARM_TYPE_ALERT_WATER_LEVEL, CMD_ALARM_IN_ALERTWATER_PARA, &tInfo);
		if(iRet != 0)
		{ 
			AddLog(LOG_TYPE_FAIL,"","[OnBnClickedButtonSetAlertWaterLevelPara]->NetClient_SetAlarmConfig(m_iLogonID %d)", m_iLogonID);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC,"","[OnBnClickedButtonSetAlertWaterLevelPara]->NetClient_SetAlarmConfig(m_iLogonID %d)", m_iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[OnBnClickedButtonSetAlertWaterLevelPara]->Illegal Parameter(m_iLogonID %d)", m_iLogonID);
		MessageBox(GetTextByLan(_T("设置警戒水位预警参数失败!!!"),_T("Set Alert Water Alarm Parameter Fail!!!")));
		UI_UpdatePara();
	}
}

BOOL CLS_IrrigationParaConf::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UI_UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control	
}

void CLS_IrrigationParaConf::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateUIText();	
}

void CLS_IrrigationParaConf::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else 
		m_iLogonID = _iLogonID;

	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;    
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	UI_UpdateAlgoType();
	UI_UpdatePara();
}

void CLS_IrrigationParaConf::UI_UpdatePara()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_MSG, "", "Invalid Logon id(%d)", m_iLogonID);
		return;
	}
	int iRet = -1;
	int iEnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_VCA_RESOURCE, m_iChannelNO, &iEnable);
	if (RET_SUCCESS == iRet)
	{
		if ((0 == (iEnable >> 4)) && (0 == (iEnable >> 3)))
		{
			GetDlgItem(IDC_CHECK_ALERTWATERENABLE)->EnableWindow(FALSE);
			m_chkRainEnable.EnableWindow(FALSE);
			GetDlgItem(IDC_BUTTON_SET_RAINFALL)->EnableWindow(FALSE);
			GetDlgItem(IDC_BUTTON_SET_ALERT_WATER_LEVEL)->EnableWindow(FALSE);
			return;
		}
		else
		{
			GetDlgItem(IDC_CHECK_ALERTWATERENABLE)->EnableWindow(TRUE);
			m_chkRainEnable.EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_SET_RAINFALL)->EnableWindow(TRUE);
			GetDlgItem(IDC_BUTTON_SET_ALERT_WATER_LEVEL)->EnableWindow(TRUE);
		}
	}
	BOOL bFlag = FALSE;
	RainFallAlarmInfo tInfo = {0};
	tInfo.iSize = sizeof(RainFallAlarmInfo);
	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNO, ALARM_TYPE_RAINFALL, CMD_ALARM_IN_RAINFALL_PARA, &tInfo);
	if (0 == iRet)
	{
		SetDlgItemInt(IDC_COMBO_RAIN_FALL_ALERT_VALUE, tInfo.iRainfallValue);
		SetDlgItemInt(IDC_EDIT_LINKAGE_TIME, tInfo.iAlarmTime);
		SetDlgItemInt(IDC_EDIT_RAIN_TIME_INTERVAL, tInfo.iRainTimeInter);
		SetDlgItemInt(IDC_EDIT_RAIN_FALL_DEFAULT_UPLOAD_CHECK_TIME, tInfo.iUploadTime);
		SetDlgItemInt(IDC_EDIT_UPLOAD_CHECK_TIME, tInfo.iModerateRainUploadTime);
		SetDlgItemInt(IDC_EDIT_THERSHOLD, tInfo.iModerateRainThershold);
		SetDlgItemInt(IDC_EDIT_UPLOAD_CHECK_TIME_HEAVY_RAIN, tInfo.iHeavyRainUploadTime);
		SetDlgItemInt(IDC_EDIT_THERSHOLD_HEAVY_RAIN, tInfo.iHeavyRainThershold);
		SetDlgItemInt(IDC_EDIT_UPLOAD_CHECK_TIME_TORRENTIAL_RAIN, tInfo.iTorrentialRainUploadTime);
		SetDlgItemInt(IDC_EDIT_THERSHOLD_TORRENTIAL_RAIN, tInfo.iTorrentialRainThershold);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[UI_UpdatePara]->NetClient_GetAlarmConfig[CMD_ALARM_IN_RAINFALL_PARA] failed! LogonID(%d), iRet(%d)",m_iLogonID, iRet);
	}
	AlertWaterAlarmInfo tmpInfo = {0};
	tmpInfo.iSize = sizeof(AlertWaterAlarmInfo);
	tmpInfo.iAlgorithmType = m_cboAlgoType.GetCurSel() + 1;
	tmpInfo.iSenceID = m_cboSenceID.GetCurSel();
	tmpInfo.iRuleID = m_cboRuleID.GetCurSel();
	//The water level alarm data source only has the warning water level, mainly to distinguish the radar water level gauge and the water gauge algorithm, and the radar water level gauge only occupies scene 0, that is to say, only scene 0 needs the parameter of the water level alarm data source.
	if((1 == tmpInfo.iAlgorithmType) && (0 == tmpInfo.iSenceID))
	{
		m_cboWaterLevelAlertDataSource.EnableWindow(TRUE);
		m_cboWaterLevelAlertDataSource.SetCurSel(1);
	}
	else
	{
		m_cboWaterLevelAlertDataSource.SetCurSel(1);
		m_cboWaterLevelAlertDataSource.EnableWindow(FALSE);
		bFlag = TRUE;
	}

	iRet = NetClient_GetAlarmConfig(m_iLogonID, m_iChannelNO, ALARM_TYPE_ALERT_WATER_LEVEL, CMD_ALARM_IN_ALERTWATER_PARA, &tmpInfo);
	if (0 == iRet)
	{
		if (tmpInfo.iSize != 0)
		{
			int i = 0;
			for (i = 0;i < tmpInfo.iWaterNum; i++)
			{
				float fTmp = (float)(tmpInfo.iWaterAlarm[i]/1000.0);
				CString sTmp;
				sTmp.Format(_T("%.3f"), fTmp);
				SetDlgItemText(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD + i, sTmp);
			}
			for (;i < MAX_WATER_THERSHOLD_NUM; i++)
			{
				SetDlgItemText(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD + i, "");//If there are less than 5 warning values, fill in the blank
			}

			float fTmp = (float)(tmpInfo.iWaterLowValue/1000.0);
			CString sTmpLow;
			sTmpLow.Format(_T("%.3f"), fTmp);
			m_edtLowWaterLevel.SetWindowText(sTmpLow);


			SetAlertCountByAlgo(tmpInfo.iAlgorithmType);
			SetDlgItemInt(IDC_EDIT_WATER_LINKAGE_TIME, tmpInfo.iAlarmLinkTime);
			SetDlgItemInt(IDC_COMBO_SENCEID, tmpInfo.iSenceID);
			SetDlgItemInt(IDC_COMBO_RULE_ID, tmpInfo.iRuleID);
			SetDlgItemInt(IDC_EDIT_DEFAULT_UPLOAD_CHECK_TIME, tmpInfo.iDefaultUploadTime);
			if( FALSE == bFlag)
			{
				if(tmpInfo.iCheckStand < 1)
				{
					AddLog(LOG_TYPE_MSG,"","[UI_UpdatePara]->NetClient_GetAlarmConfig[CMD_ALARM_IN_ALERTWATER_PARA] Illegal CheckStand(%d)",tmpInfo.iCheckStand);
					m_cboWaterLevelAlertDataSource.SetCurSel(1);
				}
				else
				{
					m_cboWaterLevelAlertDataSource.SetCurSel(tmpInfo.iCheckStand - 1);
				}
			}
			m_btnIsMainChannel.SetCheck(tmpInfo.iIsMainChannel);
			SetDlgItemInt(IDC_EDIT_FIRST_PHASE_UPLAOD_CHECK_TIME, tmpInfo.iFirstWaterLevelUploadTime);
			SetDlgItemInt(IDC_EDIT_FIRST_THERSHOLD, tmpInfo.iFirstWaterLevelThershold);
			SetDlgItemInt(IDC_EDIT_SECOND_PHASE_CHECK_TIME, tmpInfo.iSecondWaterLevelUploadTime);
			SetDlgItemInt(IDC_EDIT_SECOND_SHERSHOLD, tmpInfo.iSecondWaterLevelThershold);
			SetDlgItemInt(IDC_EDIT_THRID_PHASE_CHECK_TIME, tmpInfo.iThirdWaterLevelUploadTime);
			SetDlgItemInt(IDC_EDIT_THRID_SHERSHOLD, tmpInfo.iThirdWaterLevelThershold);
			if(tmpInfo.iAlgorithmType < 1)
			{
				AddLog(LOG_TYPE_MSG,"","[UI_UpdatePara]->NetClient_GetAlarmConfig[CMD_ALARM_IN_ALERTWATER_PARA] Illegal Algorithm Type(%d)",tmpInfo.iAlgorithmType);
				m_cboAlgoType.SetCurSel(0);
			}
			else
			{
				m_cboAlgoType.SetCurSel(tmpInfo.iAlgorithmType -1);
			}
		}
		else
		{
			m_cboWaterLevelAlertDataSource.SetCurSel(1);
			m_btnIsMainChannel.SetCheck(tmpInfo.iIsMainChannel);
			SetDlgItemInt(IDC_EDIT_WATER_LINKAGE_TIME, DEFAULT_ALARMLINKTIME);
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD)->SetWindowText("");
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD2)->SetWindowText("");
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD3)->SetWindowText("");
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD4)->SetWindowText("");
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD5)->SetWindowText("");
			SetAlertCountByAlgo(tmpInfo.iAlgorithmType);
			SetDlgItemInt(IDC_EDIT_WATER_LINKAGE_TIME, DEFAULT_ALARMLINKTIME);
			SetDlgItemInt(IDC_EDIT_DEFAULT_UPLOAD_CHECK_TIME, DEFAULT_UPLOADTIME);
			SetDlgItemInt(IDC_EDIT_FIRST_PHASE_UPLAOD_CHECK_TIME, DEFAULT_FIRSTWATERLEVELUPLOADTIME);
			SetDlgItemInt(IDC_EDIT_FIRST_THERSHOLD, DEFAULT_FIRSTWATERLEVELTHERSHOLD);
			SetDlgItemInt(IDC_EDIT_SECOND_PHASE_CHECK_TIME, DEFAULT_SECWATERLEVELUPLOADTIME);
			SetDlgItemInt(IDC_EDIT_SECOND_SHERSHOLD, DEFAULT_SECWATERLEVELTHERSHOLD);
			SetDlgItemInt(IDC_EDIT_THRID_PHASE_CHECK_TIME, DEFAULT_THIRDWATERLEVELUPLOADTIME);
			SetDlgItemInt(IDC_EDIT_THRID_SHERSHOLD, DEFAULT_THIRDWATERLEVELTHERSHOLD);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[UI_UpdatePara]->NetClient_GetAlarmConfig[CMD_ALARM_IN_ALERTWATER_PARA] failed! LogonID(%d), iRet(%d)",m_iLogonID, iRet);
	}
	int iRainfallAlertEnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_RAINFALL_ALARM, m_iChannelNO, &iRainfallAlertEnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_RAINFALL_ALARM] success!");
		m_chkRainEnable.SetCheck(iRainfallAlertEnable);
		GetDlgItem(IDC_BUTTON_SET_RAINFALL)->EnableWindow(iRainfallAlertEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_RAINFALL_ALARM] fail!");
	}

	int iWaterLevelAlertEnable = -1;
	iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_ALERTWATER_ALARM, m_iChannelNO, &iWaterLevelAlertEnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_ALERTWATER_ALARM] success!");
		m_chkWaterLevelAlert.SetCheck(iWaterLevelAlertEnable);
		GetDlgItem(IDC_BUTTON_SET_ALERT_WATER_LEVEL)->EnableWindow(iWaterLevelAlertEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]NetClient_GetCommonEnable[CI_COMMON_ID_ALERTWATER_ALARM] fail!");
	}

}

void CLS_IrrigationParaConf::UI_UpdateUIText()
{
	SetDlgItemText(IDC_GROUP_BOX_RAINFALL, GetTextByLan(_T("雨量"), _T("Rainfall")));
	SetDlgItemText(IDC_STATIC_RAINFALL_THERSHOLD, GetTextByLan(_T("雨量预警阈值"), _T("RainfallAlertThershold")));
	SetDlgItemText(IDC_STATIC_MILLIMETER, GetTextByLan(_T("毫米"), _T("mm")));
	SetDlgItemText(IDC_STATIC_LINKAGE_TIME, GetTextByLan(_T("联动时长"), _T("LinkageTime")));
	SetDlgItemText(IDC_STATIC_SECOND, GetTextByLan(_T("秒"), _T("Seconds")));
	SetDlgItemText(IDC_STATIC_RAIN_TIME_INTERVAL, GetTextByLan(_T("降雨场次检测"), _T("RainTimeInterval")));
	SetDlgItemText(IDC_STATIC_MINUTE, GetTextByLan(_T("分钟"), _T("Minutes")));
	SetDlgItemText(IDC_STATIC_MODERATE_RAIN_UPLOAD, GetTextByLan(_T("中雨上传检测时间"), _T("ModerateRainUploadTime")));
	SetDlgItemText(IDC_STATIC_UNIT_MINUTE, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_HEAVY_RAIN_UPLOAD_TIME, GetTextByLan(_T("大雨上传检测时间"), _T("HeavyRainUploadTime")));
	SetDlgItemText(IDC_STATIC_UNIT_MINUTE_HEAVY_RAIN, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_TORRENTIAL_RAIN_UPLOAD_TIME, GetTextByLan(_T("暴雨上传检测时间"), _T("TerrientalRainUploadTime")));
	SetDlgItemText(IDC_STATIC_UNIT_MINUTE_TORRENTIAL_RAIN, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_THERSHOLD, GetTextByLan(_T("阈值"), _T("Thershold")));
	SetDlgItemText(IDC_STATIC_THERSHOLD_HEAVY_RAIN, GetTextByLan(_T("阈值"), _T("Thershold")));
	SetDlgItemText(IDC_STATIC_THERSHOLD_TORRENTIAL_RAIN, GetTextByLan(_T("阈值"), _T("Thershold")));
	SetDlgItemText(IDC_STATIC_UNIT_CM_HOUR, GetTextByLan(_T("单位: 毫米/小时"), _T("Unit: mm/h")));
	SetDlgItemText(IDC_STATIC_UNIT_CM_HOUR_HEAVY_RAIN, GetTextByLan(_T("单位: 毫米/小时"), _T("Unit: mm/h")));
	SetDlgItemText(IDC_STATIC_UNIT_CM_HOUR_TORRENTIAL, GetTextByLan(_T("单位: 毫米/小时"), _T("Unit: mm/h")));
	SetDlgItemText(IDC_BUTTON_SET_RAINFALL, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_ALERT_WATER_LEVEL, GetTextByLan(_T("警戒水位/积水深度"), _T("AlertWaterLevel/AccumulateWaterAlert")));
	SetDlgItemText(IDC_STATIC_SENCEID, GetTextByLan(_T("场景号"), _T("SenceID")));
	SetDlgItemText(IDC_CHECK_MIAN_CHANNEL, GetTextByLan(_T("主航道"), _T("MainChannel")));
	SetDlgItemText(IDC_STATIC_RULEID, GetTextByLan(_T("规则号"), _T("RuleID")));
	SetDlgItemText(IDC_STATIC_DATA_SROUCE, GetTextByLan(_T("水位报警数据来源"), _T("WaterLevelALertDataSource")));
	SetDlgItemText(IDC_STATIC_WATER_THRESHOLD, GetTextByLan(_T("警戒预警值(不足五个填空)"), _T("AlertValue(less five do not fill the blank)")));
	SetDlgItemText(IDC_STATIC_UNIT_WATER_LEVEL, GetTextByLan(_T("米"), _T("m")));
	SetDlgItemText(IDC_STATIC_WATER_LEVEL_LINKAGE_TIME, GetTextByLan(_T("报警联动时长"), _T("AlertLinkTime")));
	SetDlgItemText(IDC_STATIC_UNIT_LINKAGE_TIME_SECOND, GetTextByLan(_T("秒"), _T("Seconds")));
	SetDlgItemText(IDC_STATIC_DEFAULT_UPLOAD_CHECK_TIME, GetTextByLan(_T("默认上传检测时间"), _T("DefaultUplaodCheckTime")));
	SetDlgItemText(IDC_STATIC_UNIT_DEFAULT_UPLOAD_CHECK_TIME, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_FIRST_UPLOAD_CHECK_TIME, GetTextByLan(_T("水位第一变化段上传检测时间"), _T("FirstPhaseUploadCheckTime")));
	SetDlgItemText(IDC_STATIC_SECOND_UPLOAD_CHECK_TIME, GetTextByLan(_T("水位第二变化段上传检测时间"), _T("SecondPhaseUploadCheckTime")));
	SetDlgItemText(IDC_STATIC_THRID_UPLOAD_CHECK_TIME, GetTextByLan(_T("水位第三变化段上传检测时间"), _T("ThersholdPhaseUploadCheckTime")));
	SetDlgItemText(IDC_STATIC_FIRST_PHASE, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_SECOND_PHASE, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_THRID_PHASE, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_FIRST_THERSHOLD, GetTextByLan(_T("阈值"), _T("Thershold")));
	SetDlgItemText(IDC_STATIC_SECOND_THESHOLD, GetTextByLan(_T("阈值"), _T("Thershold")));
	SetDlgItemText(IDC_STATIC_THRID_THESHOLD,  GetTextByLan(_T("阈值"), _T("Thershold")));
	SetDlgItemText(IDC_STATIC_FIRST_UNIT, GetTextByLan(_T("单位: 厘米/小时"), _T("Unit: cm/hour")));
	SetDlgItemText(IDC_STATIC_SECOND_UNIT, GetTextByLan(_T("单位: 厘米/小时"), _T("Unit: cm/hour")));
	SetDlgItemText(IDC_STATIC_THRID_UNIT, GetTextByLan(_T("单位: 厘米/小时"), _T("Unit: cm/hour")));
	SetDlgItemText(IDC_BUTTON_SET_ALERT_WATER_LEVEL, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_STATIC_RAIN_FALL_DEFAULT_UPLOAD_CHECK_TIME, GetTextByLan(_T("开始上传检测时间"), _T("StartUploadCheckTime")));
	SetDlgItemText(IDC_STATIC_UNIT_MINUTES, GetTextByLan(_T("单位: 分钟"), _T("Unit: Minute")));
	SetDlgItemText(IDC_STATIC_ALGO_TYPE, GetTextByLan(_T("算法类型"), _T("AlgoType")));
	SetDlgItemText(IDC_STATIC_INFO, GetTextByLan(_T("*编辑框内所有数据均为大于0的整数"),_T("*All data in the edit box are integers greater than 0")));
	SetDlgItemText(IDC_CHECK_ALERTWATERENABLE, GetTextByLan(_T("使能"),_T("Enable")));
	SetDlgItemText(IDC_CHECK_RAINFALL, GetTextByLan(_T("使能"),_T("Enable")));
	SetDlgItemText(IDC_STATIC_ALARM, GetTextByLan(_T("报警模块"), _T("Alarm")));
	SetDlgItemText(IDC_STATIC_ALARM_CONTROL, GetTextByLan(_T("远程控制报警"), _T("RemoteControlAlarm")));

	CString strRainfallAlertThershold;
	
	m_cboRainFallAlertThershold.ResetContent();
	strRainfallAlertThershold.Format("%d", RAIN_FALL_ALARM_PARA_DEFAULT80);
	m_cboRainFallAlertThershold.InsertString(0, strRainfallAlertThershold);
	strRainfallAlertThershold.Format("%d", RAIN_FALL_ALARM_PARA_DEFAULT120);
	m_cboRainFallAlertThershold.InsertString(1,  strRainfallAlertThershold);
	strRainfallAlertThershold.Format("%d", RAIN_FALL_ALARM_PARA_DEFAULT160);
	m_cboRainFallAlertThershold.InsertString(2,  strRainfallAlertThershold);
	m_cboRainFallAlertThershold.SetCurSel(0);
	
	m_btnIsMainChannel.SetCheck(BST_CHECKED);

	m_cboSenceID.ResetContent();
	for(int i = 0; i < MAX_SCENE_NUM; ++i)
	{
		CString strSenceID;
		strSenceID.Format("%d", i+1);
		m_cboSenceID.InsertString(i, strSenceID);
	}
	m_cboSenceID.SetCurSel(0);

	m_cboRuleID.ResetContent();
	for(int i = 0; i < VCA_MAX_RULE_NUM_EX; ++i)
	{
		CString strRuleID;
		strRuleID.Format("%d", i+1);
		m_cboRuleID.InsertString(i, strRuleID);
	}
	m_cboRuleID.SetCurSel(0);
	
	m_cboWaterLevelAlertDataSource.ResetContent();
	m_cboWaterLevelAlertDataSource.InsertString(0, GetTextByLan(_T("雷达水位计"), _T("RadarWaterLevelGague")));
	m_cboWaterLevelAlertDataSource.InsertString(1, GetTextByLan(_T("算法"), _T("Algorithm ")));
	m_cboWaterLevelAlertDataSource.SetCurSel(1);

	m_cboAlgoType.ResetContent();
	m_cboAlgoType.InsertString(0, GetTextByLan(_T("警戒水位"), _T("AlertWaterLevel")));
	m_cboAlgoType.InsertString(1, GetTextByLan(_T("积水深度"), _T("AccumulateWaterAlert")));
	m_cboAlgoType.SetCurSel(0);

	m_edtLinkageTime.SetLimitText(3);
	m_edtRainTimeInterval.SetLimitText(3);
	m_edtDefaultUploadTime.SetLimitText(3);
	m_edtUploadCheckTime1.SetLimitText(3);
	m_edtUploadCheckTime2.SetLimitText(3);
	m_edtUploadCheckTime3.SetLimitText(3);
	m_edtThreShold.SetLimitText(3);
	m_edtTherShold2.SetLimitText(3);
	m_edtTherShold3.SetLimitText(3);

	m_edtAlertData.SetLimitText(16);
	m_edtWaterLinkAge.SetLimitText(3);
	m_edtWaterUploadTime.SetLimitText(3);
	m_edtFirstUploadTime.SetLimitText(3);
	m_edtSecondTime.SetLimitText(3);
	m_edtThirdTime.SetLimitText(3);
	m_edtWaterThershold1.SetLimitText(3);
	m_edtWaterThershold2.SetLimitText(3);
	m_edtWaterThershold3.SetLimitText(3);
}

void CLS_IrrigationParaConf::OnCbnSelchangeComboSenceID()
{
	UI_UpdateAlgoType();
	UI_UpdatePara();
}

void CLS_IrrigationParaConf::OnCbnSelchangeComboRuleId()
{
	UI_UpdateAlgoType();
	UI_UpdatePara();
}

void CLS_IrrigationParaConf::OnCbnSelchangeComboAlgoType()
{
	UI_UpdatePara();
}

BOOL CLS_IrrigationParaConf::CheckParaValid()
{
	BOOL iRet = TRUE;
	CString strRainData1,strRainData2,strRainData3;
	int iRain1, iRain2, iRain3 = 0;
	GetDlgItem(IDC_EDIT_THERSHOLD)->GetWindowText(strRainData1);
	GetDlgItem(IDC_EDIT_THERSHOLD_HEAVY_RAIN)->GetWindowText(strRainData2);
	GetDlgItem(IDC_EDIT_THERSHOLD_TORRENTIAL_RAIN)->GetWindowText(strRainData3);
	iRain1 = _ttoi(strRainData1);
	iRain2 = _ttoi(strRainData2);
	iRain3 = _ttoi(strRainData3);
	if (!(iRain1 < iRain2 && iRain2 < iRain3))
	{
		iRet = FALSE;
	}
	return iRet;

}

BOOL CLS_IrrigationParaConf::CheckParaValidInWater()
{
	BOOL iRet = TRUE;
	CString strRainData1,strRainData2,strRainData3;
	int iRain1, iRain2, iRain3 = 0;
	GetDlgItem(IDC_EDIT_FIRST_THERSHOLD)->GetWindowText(strRainData1);
	GetDlgItem(IDC_EDIT_SECOND_SHERSHOLD)->GetWindowText(strRainData2);
	GetDlgItem(IDC_EDIT_THRID_SHERSHOLD)->GetWindowText(strRainData3);
	iRain1 = _ttoi(strRainData1);
	iRain2 = _ttoi(strRainData2);
	iRain3 = _ttoi(strRainData3);
	if (!(iRain1 < iRain2 && iRain2 < iRain3))
	{
		iRet = FALSE;
	}
	return iRet;
}

void CLS_IrrigationParaConf::SetAlertCountByAlgo(int _iAlgo)
{
	if (_iAlgo == 1)
	{
		for (int i = 1;i < 5; i++)
		{
			SetDlgItemText(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD + i, "");
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD + i)->EnableWindow(FALSE);
		}
	}
	else if (_iAlgo == 2)
	{
		for (int i = 0;i < 5; i++)
		{
			GetDlgItem(IDC_EDIT_WATER_LEVEL_ALARM_THERSHOLD + i)->EnableWindow(TRUE);
		}
	}
}

HBRUSH CLS_IrrigationParaConf::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CLS_BasePage::OnCtlColor(pDC, pWnd, nCtlColor);

	// TODO:  Change any attributes of the DC here
	if (IDC_STATIC_INFO == pWnd->GetDlgCtrlID())
	{
		pDC->SetTextColor(RGB(255, 0, 0));//set text color to red
	}
	// TODO:  Return a different brush if the default is not desired
	return hbr;
}

void CLS_IrrigationParaConf::UI_UpdateAlgoType()
{
	int iSceneID = m_cboSenceID.GetCurSel();
	int iRuleID = m_cboRuleID.GetCurSel();
	VCARuleParam  tParam = {0};
	tParam.stRule.iRuleID = iRuleID;
	tParam.stRule.iSceneID = iSceneID;
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RULE_PARAM, m_iChannelNO, &tParam, sizeof(VCARuleParam));
	if (iRet < 0 || !tParam.stRule.iValid)
	{
		m_cboAlgoType.SetCurSel(0);
		return;
	}
	int iEventID = tParam.iEventID;
	if (iEventID == VCA_EVENT_WATER_LEVEL_DETECT)
	{
		m_cboAlgoType.SetCurSel(0);
	}
	else if(iEventID == VCA_EVENT_SEDIMENT)
	{
		m_cboAlgoType.SetCurSel(1);
	}
}

void CLS_IrrigationParaConf::OnBnClickedCheckRainfall()
{
	// TODO: Add your control notification handler code here
	int iEnable = m_chkRainEnable.GetCheck();
	int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_RAINFALL_ALARM, m_iChannelNO, iEnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable[CI_COMMON_ID_RAINFALL_ALARM] success!");
		GetDlgItem(IDC_BUTTON_SET_RAINFALL)->EnableWindow(iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable[CI_COMMON_ID_RAINFALL_ALARM] fail!");
	}
}


void CLS_IrrigationParaConf::OnBnClickedCheckAlertwaterenable()
{
	// TODO: Add your control notification handler code here
	int iEnable = m_chkWaterLevelAlert.GetCheck();
	int iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_ALERTWATER_ALARM, m_iChannelNO, iEnable);
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable[CI_COMMON_ID_ALERTWATER_ALARM] success!");
		GetDlgItem(IDC_BUTTON_SET_ALERT_WATER_LEVEL)->EnableWindow(iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable[CI_COMMON_ID_ALERTWATER_ALARM] fail!");
	}
}


void CLS_IrrigationParaConf::OnStnClickedStaticAlgoType()
{
	// TODO: Add your control notification handler code here
}

void CLS_IrrigationParaConf::OnBnClickedButtonAlarmControl()
{
	RemoteAlarmState tInfo = {0};
	tInfo.iAlarmStatus = 1;
	RemoteAlarmResult tResult = {0};

	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_REMOTE_CONTROL_ALARM, m_iChannelNO, &tInfo, sizeof(tInfo) ,&tResult, sizeof(tResult));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig CMD_REMOTE_CONTROL_ALARM(%d,%d)",m_iLogonID, m_iChannelNO);
		if (0 == tResult.iResult)
		{
			SetDlgItemTextEx(IDC_EDIT_RESULT,IDS_SET_RESULT_SUCCEED);
		}else
		{
			SetDlgItemTextEx(IDC_EDIT_RESULT,IDS_SET_RESULT_FAILED);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig CMD_REMOTE_CONTROL_ALARM(%d,%d)",m_iLogonID, m_iChannelNO);
	}

}
