// ExtendedParam.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "ExtendedParam.h"


// CLS_ExtendedParam dialog

IMPLEMENT_DYNAMIC(CLS_ExtendedParam, CDialog)

CLS_ExtendedParam::CLS_ExtendedParam(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_ExtendedParam::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannel = -1;
}

CLS_ExtendedParam::~CLS_ExtendedParam()
{
}

void CLS_ExtendedParam::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_GP_BASIC_PARAM, m_gpBasicParam);
	DDX_Control(pDX, IDC_STC_NIGHT_BIT_VALUE, m_stcNightBitValue);
	DDX_Control(pDX, IDC_EDT_NIGHT_BIT_VALUE, m_edtNightBitValue);
	DDX_Control(pDX, IDC_BTN_SET_BASIC_PARAM, m_btnNightBitValue);
	DDX_Control(pDX, IDC_GP_FILL_LIGHT_CONTROL, m_gpFillLightControl);
	DDX_Control(pDX, IDC_STC_DAY_THRESHOLD_VALUE, m_stcDayThresholdValue);
	DDX_Control(pDX, IDC_STC_NIGHT_THRESHOLD_VALUE, m_stcNightThresholdValue);
	DDX_Control(pDX, IDC_EDT_DAY_THRESHOLD_VALUE, m_edtDayThresholdValue);
	DDX_Control(pDX, IDC_EDT_NIGHT_THRESHOLD_VALUE, m_edtNightThresholdValue);
	DDX_Control(pDX, IDC_BTN_SET_FILL_LIGHT_CONTROL, m_btnSetFillLightControl);
	DDX_Control(pDX, IDC_GP_TRAFFIC_DAY_NIGHT_TIME, m_gpTrafficDayNIghtTime);
	DDX_Control(pDX, IDC_STC_DAY_TIME, m_stcDayTime);
	DDX_Control(pDX, IDC_STC_NIGHT_TIME, m_stcNightTime);
	DDX_Control(pDX, IDC_EDT_DAY_TIME_HOUR, m_edtDayTimeHour);
	DDX_Control(pDX, IDC_EDT_DAY_TIME_MINUTE, m_edtDayTimeMinute);
	DDX_Control(pDX, IDC_EDT_NIGHT_TIME_HOUR, m_edtNightTimeHour);
	DDX_Control(pDX, IDC_EDT_NIGHT_TIME_MINUTE, m_edtNightTimeMiunte);
	DDX_Control(pDX, IDC_BTN_SET_TRAFFIC_DAY_NIGHT_TIME, m_btnTrafficDayNightTime);
	DDX_Control(pDX, IDC_COMBO_PLATE_FILTER_TYPE, m_cboPlateFilterType);
	DDX_Control(pDX, IDC_COMBO_SINGLE_SCENEID, m_cboSceneId);
	DDX_Control(pDX, IDC_COMBO_SINGLE_CUT_ENABLE, m_cboCutEnable);
	DDX_Control(pDX, IDC_COMBO_SINGLE_CUT_RANGE, m_cboCutRange);
	DDX_Control(pDX, IDC_COMBO_SINGLE_SOURCE_PIC, m_cboSourcePic);
	DDX_Control(pDX, IDC_COMBO_SINGLE_OSD_ENABLE, m_cboOsdEnable);
}


BEGIN_MESSAGE_MAP(CLS_ExtendedParam, CDialog)
	ON_BN_CLICKED(IDC_BTN_SET_TRAFFIC_DAY_NIGHT_TIME, &CLS_ExtendedParam::OnBnClickedBtnSetTrafficDayNightTime)
	ON_BN_CLICKED(IDC_BTN_SET_FILL_LIGHT_CONTROL, &CLS_ExtendedParam::OnBnClickedBtnSetFillLightControl)
	ON_BN_CLICKED(IDC_BTN_SET_BASIC_PARAM, &CLS_ExtendedParam::OnBnClickedBtnSetBasicParam)
	ON_BN_CLICKED(IDC_BUTTON_PLATE_FILTER, &CLS_ExtendedParam::OnBnClickedButtonPlateFilter)
	ON_CBN_SELCHANGE(IDC_COMBO_PLATE_FILTER_TYPE, &CLS_ExtendedParam::OnCbnSelchangeComboPlateFilterType)
	ON_BN_CLICKED(IDC_BUTTON_SINGLE_SET, &CLS_ExtendedParam::OnBnClickedButtonSingleSet)
	ON_CBN_SELCHANGE(IDC_COMBO_SINGLE_SCENEID, &CLS_ExtendedParam::OnCbnSelchangeComboSingleSceneid)
END_MESSAGE_MAP()


// CLS_ExtendedParam message handler


BOOL CLS_ExtendedParam::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateDialog();
	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_ExtendedParam::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannel = _iChannelNo;
	if(m_iChannel < 0) 
	{
		m_iChannel = 0;
	}
	UI_UpdateDialog();
}

void CLS_ExtendedParam::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateDialog();
}

void CLS_ExtendedParam::UI_UpdateBasicParamData()
{
	int iDayRange = 0;
	int iNightRange = 0;
	int iColorGrayChange = 0;
	int iRet = NetClient_GetColorParam(m_iLogonID, m_iChannel, &iColorGrayChange,  &iDayRange, &iNightRange);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetColorParam (%d,%d)",m_iLogonID,m_iChannel);
		SetDlgItemInt(IDC_EDT_DAY_THRESHOLD_VALUE, iDayRange);
		SetDlgItemInt(IDC_EDT_NIGHT_THRESHOLD_VALUE, iNightRange);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetColorParam (%d,%d)",m_iLogonID,m_iChannel);
	}
}
void CLS_ExtendedParam::UI_UpdateFillLightControlData()
{
	int iDayRange = 0;
	int iNightRange = 0;
	int iColorGrayChange = 0;
	int iTemp = 0;
	int iRet1 = NetClient_GetColorParam(m_iLogonID, m_iChannel, &iColorGrayChange,  &iDayRange, &iNightRange);
	if(iRet1 == 0)
	{
		SetDlgItemInt(IDC_EDT_DAY_THRESHOLD_VALUE, iDayRange);
		SetDlgItemInt(IDC_EDT_NIGHT_THRESHOLD_VALUE, iNightRange);
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetColorParam(%d,%d)", m_iLogonID, m_iChannel);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetColorParam(%d,%d),error(%d)", m_iLogonID, m_iChannel, GetLastError());
	}


	DZ_INFO_PARAM tInputGetValue = {0};
	int iRet = NetClient_GetDZInfo(m_iLogonID, &tInputGetValue);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDZInfo (%d)",m_iLogonID);
		SetDlgItemText(IDC_EDT_NIGHT_BIT_VALUE, (LPSTR)tInputGetValue.m_cParam11);
		//GetDlgItem(IDC_EDT_NIGHT_BIT_VALUE)->SetWindowText((LPSTR)tInputGetValue.m_cParam11);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDZInfo (%d)",m_iLogonID);
	}
}
//Refresh traffic day and night time periods
void CLS_ExtendedParam::UI_UpdateTrafficDayNightTimeData()
{
	int iGet = 0;
	int iRet = NetClient_GetITSDayNight(m_iLogonID, &iGet);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSDayNight (%d)",m_iLogonID);
		SetDlgItemInt(IDC_EDT_NIGHT_TIME_MINUTE, iGet & 0x00000FF);
		SetDlgItemInt(IDC_EDT_NIGHT_TIME_HOUR, (iGet>>8) & 0x00000FF);
		SetDlgItemInt(IDC_EDT_DAY_TIME_MINUTE, (iGet>>16) & 0x00000FF);
		SetDlgItemInt(IDC_EDT_DAY_TIME_HOUR, (iGet>>24) & 0x00000FF);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSDayNight (%d)",m_iLogonID);
	}

}

void CLS_ExtendedParam::UI_UpdateDialog()
{
	SetDlgItemTextEx(IDC_GP_BASIC_PARAM, IDS_ITS_ARITHMETIC_BASIC_NUMBER);
	SetDlgItemTextEx(IDC_STC_NIGHT_BIT_VALUE, IDS_ITS_NIGHTTHRESHOLD);
	SetDlgItemTextEx(IDC_GP_FILL_LIGHT_CONTROL, IDS_ITS_FILL_LIGHT_CONTROL);
	SetDlgItemTextEx(IDC_STC_DAY_THRESHOLD_VALUE, IDS_ITS_DAY_THRESHOLD_VALUE);
	SetDlgItemTextEx(IDC_STC_NIGHT_THRESHOLD_VALUE, IDS_ITS_NIGHT_THRESHOLD_VALUE);
	SetDlgItemTextEx(IDC_GP_TRAFFIC_DAY_NIGHT_TIME, IDS_ITS_TRAFFIC_DAY_NIGHT_TIME);
	SetDlgItemTextEx(IDC_STC_DAY_TIME, IDS_ITS_DAY);
	SetDlgItemTextEx(IDC_STC_NIGHT_TIME, IDS_ITS_NIGHT);
	SetDlgItemTextEx(IDC_BTN_SET_BASIC_PARAM, IDS_SET);
	SetDlgItemTextEx(IDC_BTN_SET_FILL_LIGHT_CONTROL, IDS_SET);
	SetDlgItemTextEx(IDC_BTN_SET_TRAFFIC_DAY_NIGHT_TIME, IDS_SET);

	m_cboPlateFilterType.ResetContent();
	m_cboPlateFilterType.AddString(GetTextEx(IDS_CONFIG_ADV_ALLOWIP));
	m_cboPlateFilterType.AddString(GetTextEx(IDS_CONFIG_ADV_FORBIDIP));
	m_cboPlateFilterType.SetCurSel(0);

	m_cboSceneId.ResetContent();
	for (int i= 0;i<MAX_SCENE_NUM;i++)
	{
		CString cstrSceneId;
		cstrSceneId.Format("%d",i+1);
		m_cboSceneId.AddString(cstrSceneId);
	}
	m_cboSceneId.SetCurSel(0);

	m_cboCutEnable.ResetContent();
	m_cboCutEnable.AddString(GetTextEx(IDS_ITS_NOT_ENABLE));
	m_cboCutEnable.AddString(GetTextEx(IDS_CONFIG_ECOP_IMGDISPOSAL_ENABLE));
	m_cboCutEnable.SetCurSel(0);

	m_cboCutRange.ResetContent();
	m_cboCutRange.SetItemData(m_cboCutRange.AddString("1/4"), 4);
	m_cboCutRange.SetItemData(m_cboCutRange.AddString("1/9"), 9);
	m_cboCutRange.SetItemData(m_cboCutRange.AddString("1/16"), 16);
	m_cboCutRange.SetCurSel(0);

	m_cboSourcePic.ResetContent();
	m_cboSourcePic.AddString(GetTextByLan("择优","Excellent"));
	m_cboSourcePic.AddString(GetTextByLan("第一张","First sheet"));
	m_cboSourcePic.AddString(GetTextByLan("第二张","Second sheet"));
	m_cboSourcePic.SetCurSel(0);

	m_cboOsdEnable.ResetContent();
	m_cboOsdEnable.AddString(GetTextEx(IDS_ITS_NOT_ENABLE));
	m_cboOsdEnable.AddString(GetTextEx(IDS_CONFIG_ECOP_IMGDISPOSAL_ENABLE));
	m_cboOsdEnable.SetCurSel(0);


	UI_UpdateBasicParamData();
	UI_UpdateFillLightControlData();
	UI_UpdateTrafficDayNightTimeData();
	UI_UpdatePlateFilterInfo();
	UI_UpdateSingleCutPicFeature();
}

void CLS_ExtendedParam::OnBnClickedBtnSetTrafficDayNightTime()
{
	unsigned int InputTime = 0;
	unsigned int uiTemp = 0;
	unsigned int uiDayHour = GetDlgItemInt(IDC_EDT_DAY_TIME_HOUR);
	unsigned int uiDayMinute = GetDlgItemInt(IDC_EDT_DAY_TIME_MINUTE);
	unsigned int uiNightHour = GetDlgItemInt(IDC_EDT_NIGHT_TIME_HOUR);
	unsigned int uiNightMinute = GetDlgItemInt(IDC_EDT_NIGHT_TIME_MINUTE);
	if(uiDayHour > uiNightHour)
	{
		uiTemp = uiDayHour;
		uiDayHour = uiNightHour;
		uiNightHour = uiTemp;
		uiTemp = uiDayMinute;
		uiDayMinute = uiNightMinute;
		uiNightMinute = uiTemp;
	}
	else if((uiDayHour == uiNightHour)&&(uiDayMinute > uiNightMinute))
	{
		uiTemp = uiDayMinute;
		uiDayMinute = uiNightMinute;
		uiNightMinute = uiTemp;
	}
	InputTime = min(uiDayHour, HOUR_LIMIT);
	InputTime = (InputTime<<8)|min(uiDayMinute, MINUTE_LIMIT);
	InputTime = (InputTime<<8)|min(uiNightHour, HOUR_LIMIT);
	InputTime = (InputTime<<8)|min(uiNightMinute, MINUTE_LIMIT);
	int iRet = NetClient_SetITSDayNight(m_iLogonID, InputTime);
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetITSDayNight (%d,%d)",m_iLogonID,InputTime);
		SetDlgItemInt(IDC_EDT_DAY_TIME_HOUR, uiDayHour);
		SetDlgItemInt(IDC_EDT_DAY_TIME_MINUTE, uiDayMinute);
		SetDlgItemInt(IDC_EDT_NIGHT_TIME_HOUR, uiNightHour);
		SetDlgItemInt(IDC_EDT_NIGHT_TIME_MINUTE, uiNightMinute);
	} 
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetITSDayNight (%d,%d)",m_iLogonID,InputTime);
	}
}

void CLS_ExtendedParam::OnBnClickedBtnSetFillLightControl()
{
	int iDayRange = 0;
	int iNightRange = 0;
	int iColorGrayChange = 0;
	int iTemp = 0;
	int iRet1 = NetClient_GetColorParam(m_iLogonID, m_iChannel, &iColorGrayChange,  &iDayRange, &iNightRange);
	if (0 != iRet1)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetColorParam (%d,%d)",m_iLogonID, m_iChannel);
	} 
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetColorParam (%d,%d)",m_iLogonID, m_iChannel);
		iDayRange = GetDlgItemInt(IDC_EDT_DAY_THRESHOLD_VALUE);
		if(iDayRange > 255)
		{
			iDayRange = 255;
		}
		else if(iDayRange < 0)
		{
			iDayRange = 0;
		}
		iNightRange = GetDlgItemInt(IDC_EDT_NIGHT_THRESHOLD_VALUE);
		if(iNightRange > 255)
		{
			iNightRange = 255;
		}
		else if(iNightRange < 0)
		{
			iNightRange = 0;
		}

		if(iDayRange < iNightRange)
		{
			iTemp = iDayRange;
			iDayRange = iNightRange;
			iNightRange = iTemp;
		}
		int iRet2 = NetClient_SetColorParam(m_iLogonID, m_iChannel, iColorGrayChange,  iDayRange, iNightRange);
		if (0 == iRet2)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetColorParam (%d,%d)",m_iLogonID, m_iChannel);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetColorParam (%d,%d)",m_iLogonID, m_iChannel);
		}
	}
}

void CLS_ExtendedParam::OnBnClickedBtnSetBasicParam()
{
	DZ_INFO_PARAM tInputSetValue = {0};
	int iRet1 = NetClient_GetDZInfo(m_iLogonID, &tInputSetValue);
	if (0 != iRet1)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_GetDZInfo (%d)",m_iLogonID);
		
	} 
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_GetDZInfo (%d)",m_iLogonID);
		memcpy(tInputSetValue.m_cParam1,"DETECTBASIC",sizeof("DETECTBASIC"));
		GetDlgItem(IDC_EDT_NIGHT_BIT_VALUE)->GetWindowText((LPSTR)(tInputSetValue.m_cParam11),LEN_64);
		int iRet2 = NetClient_SetDZInfo(m_iLogonID, &tInputSetValue);
		if (0 == iRet2)
		{
			AddLog(LOG_TYPE_SUCC,"","NetClient_SetDZInfo (%d)",m_iLogonID);
		} 
		else
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_SetDZInfo (%d)",m_iLogonID);
		}
	}
	
}

void CLS_ExtendedParam::OnBnClickedButtonPlateFilter()
{
	ItsPlateFilterParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannel;
	tInfo.iFilterType = m_cboPlateFilterType.GetCurSel();

	
	CString cstrPlate = "";
	GetDlgItemText(IDC_EDIT_PLATE_FILTER_TEXT, cstrPlate);

	CString cstrOnePlate = "";
	int iPos = -1;
	int iPlateNum = 0;
	iPos = cstrPlate.Find(',');
	if (iPos >= 0)
	{
		while(iPos >= 0)
		{
			cstrOnePlate = cstrPlate.Left(iPos);
			cstrPlate = cstrPlate.Right(cstrPlate.GetLength() - iPos - 1);
			iPos = cstrPlate.Find(',');

			if (iPlateNum<MAX_PLATE_FILTER_NUM-1)
			{
				if ("" != cstrOnePlate)
				{
					memcpy(tInfo.cPlateFilterInfo[iPlateNum], cstrOnePlate.GetBuffer(), min(cstrOnePlate.GetLength(),LEN_32));
					iPlateNum++;
				}
				
				if (iPos < 0 && "" != cstrPlate)
				{
					memcpy(tInfo.cPlateFilterInfo[iPlateNum], cstrPlate.GetBuffer(), min(cstrOnePlate.GetLength(),LEN_32));
					iPlateNum++;
				}
			}
		}
	}
	else if(iPos < 0 && "" != cstrPlate)
	{
		memcpy(tInfo.cPlateFilterInfo[iPlateNum], cstrPlate.GetBuffer(), min(cstrPlate.GetLength(),LEN_32));
		iPlateNum++;
	}

	
	tInfo.iPlateFilterNum = iPlateNum;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_PLATE_FILTER_PARAM, m_iChannel, &tInfo, sizeof(tInfo));
	if(RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_ExtendedParam::NetClient_SetDevConfig[NET_CLIENT_PLATE_FILTER_PARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannel, GetLastError());
	}
}

void CLS_ExtendedParam::UI_UpdatePlateFilterInfo()
{
	ItsPlateFilterParam tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannel;
	tInfo.iFilterType = m_cboPlateFilterType.GetCurSel();

	int iByteReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_PLATE_FILTER_PARAM, m_iChannel, &tInfo, sizeof(tInfo),&iByteReturn);
	if(RET_SUCCESS == iRet)
	{
		CString cstrPlate = "";
		for (int i=0;i<tInfo.iPlateFilterNum && i<MAX_PLATE_FILTER_NUM;i++)
		{
			if (0 != i)
			{
				cstrPlate += ',';
			}
			cstrPlate.AppendFormat("%s",tInfo.cPlateFilterInfo[i]);
		}

		SetDlgItemText(IDC_EDIT_PLATE_FILTER_TEXT, cstrPlate);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","UI_UpdatePlateFilterInfo::NetClient_GetDevConfig[NET_CLIENT_PLATE_FILTER_PARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannel, GetLastError());
	}

}

void CLS_ExtendedParam::UI_UpdateSingleCutPicFeature()
{
	SingleCutPicFeature tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannel;
	tInfo.iSceneId = m_cboSceneId.GetCurSel();
	

	int iByteReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_SINGLE_PIC_FEATURE, m_iChannel, &tInfo, sizeof(tInfo),&iByteReturn);
	if(RET_SUCCESS == iRet)
	{
		m_cboCutEnable.SetCurSel(tInfo.iCutPicEnable);
		for (int i=0;i<m_cboCutRange.GetCount();i++)
		{
			if (tInfo.iCutRange == m_cboCutRange.GetItemData(i))
			{
				m_cboCutRange.SetCurSel(i);
				break;
			}
		}
		m_cboSourcePic.SetCurSel(tInfo.iSourcePic);
		m_cboOsdEnable.SetCurSel(tInfo.iOsdEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","UI_UpdatePlateFilterInfo::NetClient_GetDevConfig[NET_CLIENT_SINGLE_PIC_FEATURE] (%d, %d), error(%d)", m_iLogonID, m_iChannel, GetLastError());
	}
}

void CLS_ExtendedParam::OnCbnSelchangeComboPlateFilterType()
{
	UI_UpdatePlateFilterInfo();
}

void CLS_ExtendedParam::OnBnClickedButtonSingleSet()
{
	SingleCutPicFeature tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannel;
	tInfo.iSceneId = m_cboSceneId.GetCurSel();
	tInfo.iCutPicEnable = m_cboCutEnable.GetCurSel();
	tInfo.iCutRange = m_cboCutRange.GetItemData(m_cboCutRange.GetCurSel());
	tInfo.iSourcePic = m_cboSourcePic.GetCurSel();
	tInfo.iOsdEnable = m_cboOsdEnable.GetCurSel();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_SINGLE_PIC_FEATURE, m_iChannel, &tInfo, sizeof(tInfo));
	if(RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_ExtendedParam::NetClient_SetDevConfig[NET_CLIENT_SINGLE_PIC_FEATURE] (%d, %d), error(%d)", m_iLogonID, m_iChannel, GetLastError());
	}
}

void CLS_ExtendedParam::OnCbnSelchangeComboSingleSceneid()
{
	UI_UpdateSingleCutPicFeature();
}
