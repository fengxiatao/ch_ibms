// VCAEVENT_Structured.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_Structured.h"
#include "../VCAEventPage.h"


// CLS_VCAEVENT_Structured dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_Structured, CDialog)

CLS_VCAEVENT_Structured::CLS_VCAEVENT_Structured(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_VCAEVENT_Structured::IDD, pParent)
{

}

CLS_VCAEVENT_Structured::~CLS_VCAEVENT_Structured()
{
}

void CLS_VCAEVENT_Structured::DoDataExchange(CDataExchange* pDX)
{
	CLS_VCAEventBasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_PUSHMODE, m_comPushMode);
	DDX_Control(pDX, IDC_SLIDER_STRUCT_SENSIT, m_sldSensitivity);
	DDX_Control(pDX, IDC_EDIT_PUSH_LEVEL, m_edtPushLevel);
	DDX_Control(pDX, IDC_COMB_PUSH_LEVEL, m_comPushLevel);
	DDX_Control(pDX, IDC_EDIT_MIN_FACESIZE, m_edtMinFace);
	DDX_Control(pDX, IDC_EDIT_PLATESIZE, m_edtMinPlate);
	DDX_Control(pDX, IDC_COMBO_SNAP_MODE, m_comSnapMode);
	DDX_Control(pDX, IDC_COMBO_SNAP_TIME, m_comSnapTime);
	DDX_Control(pDX, IDC_SLIDER_STRUCT_BRIGHT, m_sldBright);
	DDX_Control(pDX, IDC_CHECK_SHOW_TARGETFRAME, m_chkShowTarget);
	DDX_Control(pDX, IDC_CHECK_PLATE_EXPOSURE_ENABLE, m_chkPlateExposureBox);
	DDX_Control(pDX, IDC_CHECK_SHOW_RULEFRAME, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_STRUCT_FACE, m_chkDetectType[0]);
	DDX_Control(pDX, IDC_CHECK_STRUCT_PERSON, m_chkDetectType[1]);
	DDX_Control(pDX, IDC_CHECK_STRUCT_PLATE, m_chkDetectType[2]);
	DDX_Control(pDX, IDC_CHECK_STRUCT_MOTORVEHICLE, m_chkDetectType[3]);
	DDX_Control(pDX, IDC_CHECK_STRUCT_NONMOTORVEHICLE, m_chkDetectType[4]);
	DDX_Control(pDX, IDC_SLD_FACE_QUALITY, m_sldFace);
	DDX_Control(pDX, IDC_SLD_PERSON_QUALITY, m_sldPerson);
	DDX_Control(pDX, IDC_SLD_PLATE_QUALITY, m_sldPlate);
	DDX_Control(pDX, IDC_SLD_MOTORVEHICLE_QUALITY, m_sldMotorVehicle);
	DDX_Control(pDX, IDC_SLD_NONMOTORVEHICLE_QUALITY, m_sldNonMotorVehicle);
	DDX_Control(pDX, IDC_EDIT_STRUCT_POINTNUM, m_edtPointNum);
	DDX_Control(pDX, IDC_EDIT_STRUCT_AREA, m_edtAreaInfo);
	DDX_Control(pDX, IDC_SLID_STRUCT_BIGIMG_QUALITY, m_sldBigImage);
	DDX_Control(pDX, IDC_SLID_STRUCT_SMALLIMG_QUALITY, m_sldSmallImage);

	DDX_Control(pDX, IDC_COMBO_EXPOSURE_TYPE, m_comExposureType);
	DDX_Control(pDX, IDC_COMBO_EXPOSURE_ENABLE, m_comExposureEnable);
	DDX_Control(pDX, IDC_COMBO_PLATEALARMTYPE, m_comPlateAlarmType);
	DDX_Control(pDX, IDC_COMBO_DELAY_TIME, m_comDelayTime);
	DDX_Control(pDX, IDC_COMBO_INTERVAL_TIME, m_comIntervalTime);
	DDX_Control(pDX, IDC_COMBO_DEF_PROVINCE, m_comDefProvince);
}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_Structured, CDialog)
	ON_WM_SHOWWINDOW()
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_STRUCT_SENSIT, &CLS_VCAEVENT_Structured::OnNMCustomdrawSliderStructSensit)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_STRUCT_BRIGHT, &CLS_VCAEVENT_Structured::OnNMCustomdrawSliderStructBright)
	ON_CBN_SELCHANGE(IDC_COMBO_PUSHMODE, &CLS_VCAEVENT_Structured::OnCbnSelchangeComboPushmode)
	ON_CBN_SELCHANGE(IDC_COMBO_SNAP_MODE, &CLS_VCAEVENT_Structured::OnCbnSelchangeComboSnapMode)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_FACE_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSldFaceQuality)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_PERSON_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSldPersonQuality)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_PLATE_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSldPlateQuality)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_MOTORVEHICLE_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSldMotorvehicleQuality)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_NONMOTORVEHICLE_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSldNonmotorvehicleQuality)
	ON_BN_CLICKED(IDC_BUTTON_STRUCT_SAVE, &CLS_VCAEVENT_Structured::OnBnClickedButtonStructSave)
	ON_BN_CLICKED(IDC_BTN_STRUCT_DRAW, &CLS_VCAEVENT_Structured::OnBnClickedBtnStructDraw)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLID_STRUCT_BIGIMG_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSlidStructBigimgQuality)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLID_STRUCT_SMALLIMG_QUALITY, &CLS_VCAEVENT_Structured::OnNMCustomdrawSlidStructSmallimgQuality)
END_MESSAGE_MAP()


// CLS_VCAEVENT_Structured message handler

BOOL CLS_VCAEVENT_Structured::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	m_sldSensitivity.SetRange(0,5);
 	
	CString strPushMode[] = {GetTextEx(IDS_VCA_STRUCT_FASTEST),GetTextEx(IDS_VCA_STRUCT_OPTIMAL), GetTextEx(IDS_VCA_STRUCT_CUSTOMIZE), GetTextEx(IDS_VCA_STRUCT_TIMING), GetTextEx(IDS_VCA_STRUCT_HITLINE)};
 	m_comPushMode.ResetContent();
 	for(int i=0; i<sizeof(strPushMode)/sizeof(CString); i++)
	{
		m_comPushMode.InsertString(i, strPushMode[i]);
	}

	CString strPushLevel[] = {GetTextEx(IDS_VCA_STRUCT_FAST),GetTextEx(IDS_VCA_STRUCT_MEDIUM), GetTextEx(IDS_VCA_STRUCT_SLOW)};
 	m_comPushLevel.ResetContent();
	for(int i=0; i<sizeof(strPushLevel)/sizeof(CString); i++)
	{
		m_comPushLevel.InsertString(i, strPushLevel[i]);
	}
	
	CString strSnapMode[] = {GetTextEx(IDS_VCA_STRUCT_CAPALL),GetTextEx(IDS_VCA_STRUCT_QUALITY), GetTextEx(IDS_VCA_STRUCT_CUSTOMIZE)};
	for(int i=0; i<sizeof(strSnapMode)/sizeof(CString); i++)
	{
		m_comSnapMode.InsertString(i, strSnapMode[i]);
	}
	
	m_comSnapTime.ResetContent();
	for(int i=0; i<10; i++)
	{
		m_comSnapTime.InsertString(i, IntToCString(i+1));
	}

	m_sldBright.SetRange(0,255);

	m_sldFace.SetRange(0,100);
	m_sldPerson.SetRange(0,100);
	m_sldPlate.SetRange(0,100);
	m_sldMotorVehicle.SetRange(0,100);
	m_sldNonMotorVehicle.SetRange(0,100);
	m_sldBigImage.SetRange(0,100);
	m_sldSmallImage.SetRange(0,100);

	//首次推送延迟时间
	m_IndexToDedayTime.insert(std::make_pair(0, 500));
	m_IndexToDedayTime.insert(std::make_pair(1, 1000));
	m_IndexToDedayTime.insert(std::make_pair(2, 2000));

	m_DelayTimeToIndex.insert(std::make_pair(500, 0));
	m_DelayTimeToIndex.insert(std::make_pair(1000, 1));
	m_DelayTimeToIndex.insert(std::make_pair(2000, 2));

	//时间间隔
	m_IndexToTimeSpace.insert(std::make_pair(0, 100));
	m_IndexToTimeSpace.insert(std::make_pair(1, 200));
	m_IndexToTimeSpace.insert(std::make_pair(2, 300));
	m_IndexToTimeSpace.insert(std::make_pair(3, 500));
	m_IndexToTimeSpace.insert(std::make_pair(4, 1000));
	m_IndexToTimeSpace.insert(std::make_pair(5, 2000));

	m_TimeSpaceToIndex.insert(std::make_pair(100, 0));
	m_TimeSpaceToIndex.insert(std::make_pair(200, 1));
	m_TimeSpaceToIndex.insert(std::make_pair(300, 2));
	m_TimeSpaceToIndex.insert(std::make_pair(500, 3));
	m_TimeSpaceToIndex.insert(std::make_pair(1000, 4));
	m_TimeSpaceToIndex.insert(std::make_pair(2000, 5));

	//国家标准行政区划代码
	m_IndexToProvince.insert(std::make_pair(0, 11));
	m_IndexToProvince.insert(std::make_pair(1, 12));
	m_IndexToProvince.insert(std::make_pair(2, 13));
	m_IndexToProvince.insert(std::make_pair(3, 14));
	m_IndexToProvince.insert(std::make_pair(4, 15));
	m_IndexToProvince.insert(std::make_pair(5, 21));
	m_IndexToProvince.insert(std::make_pair(6, 22));
	m_IndexToProvince.insert(std::make_pair(7, 23));
	m_IndexToProvince.insert(std::make_pair(8, 31));
	m_IndexToProvince.insert(std::make_pair(9, 32));
	m_IndexToProvince.insert(std::make_pair(10, 33));
	m_IndexToProvince.insert(std::make_pair(11, 34));
	m_IndexToProvince.insert(std::make_pair(12, 35));
	m_IndexToProvince.insert(std::make_pair(13, 36));
	m_IndexToProvince.insert(std::make_pair(14, 37));
	m_IndexToProvince.insert(std::make_pair(15, 38));
	m_IndexToProvince.insert(std::make_pair(16, 41));
	m_IndexToProvince.insert(std::make_pair(17, 42));
	m_IndexToProvince.insert(std::make_pair(18, 43));
	m_IndexToProvince.insert(std::make_pair(19, 44));
	m_IndexToProvince.insert(std::make_pair(20, 45));
	m_IndexToProvince.insert(std::make_pair(21, 46));
	m_IndexToProvince.insert(std::make_pair(22, 47));
	m_IndexToProvince.insert(std::make_pair(23, 48));
	m_IndexToProvince.insert(std::make_pair(24, 50));
	m_IndexToProvince.insert(std::make_pair(25, 51));
	m_IndexToProvince.insert(std::make_pair(26, 52));
	m_IndexToProvince.insert(std::make_pair(27, 53));
	m_IndexToProvince.insert(std::make_pair(28, 54));
	m_IndexToProvince.insert(std::make_pair(29, 61));
	m_IndexToProvince.insert(std::make_pair(30, 62));
	m_IndexToProvince.insert(std::make_pair(31, 63));
	m_IndexToProvince.insert(std::make_pair(32, 64));
	m_IndexToProvince.insert(std::make_pair(33, 65));

	m_ProvinceToIndex.insert(std::make_pair(11, 0));
	m_ProvinceToIndex.insert(std::make_pair(12, 1));
	m_ProvinceToIndex.insert(std::make_pair(13, 2));
	m_ProvinceToIndex.insert(std::make_pair(14, 3));
	m_ProvinceToIndex.insert(std::make_pair(15, 4));
	m_ProvinceToIndex.insert(std::make_pair(21, 5));
	m_ProvinceToIndex.insert(std::make_pair(22, 6));
	m_ProvinceToIndex.insert(std::make_pair(23, 7));
	m_ProvinceToIndex.insert(std::make_pair(31, 8));
	m_ProvinceToIndex.insert(std::make_pair(32, 9));
	m_ProvinceToIndex.insert(std::make_pair(33, 10));
	m_ProvinceToIndex.insert(std::make_pair(34, 11));
	m_ProvinceToIndex.insert(std::make_pair(35, 12));
	m_ProvinceToIndex.insert(std::make_pair(36, 13));
	m_ProvinceToIndex.insert(std::make_pair(37, 14));
	m_ProvinceToIndex.insert(std::make_pair(38, 15));
	m_ProvinceToIndex.insert(std::make_pair(41, 16));
	m_ProvinceToIndex.insert(std::make_pair(42, 17));
	m_ProvinceToIndex.insert(std::make_pair(43, 18));
	m_ProvinceToIndex.insert(std::make_pair(44, 19));
	m_ProvinceToIndex.insert(std::make_pair(45, 20));
	m_ProvinceToIndex.insert(std::make_pair(46, 21));
	m_ProvinceToIndex.insert(std::make_pair(47, 22));
	m_ProvinceToIndex.insert(std::make_pair(48, 23));
	m_ProvinceToIndex.insert(std::make_pair(50, 24));
	m_ProvinceToIndex.insert(std::make_pair(51, 25));
	m_ProvinceToIndex.insert(std::make_pair(52, 26));
	m_ProvinceToIndex.insert(std::make_pair(53, 27));
	m_ProvinceToIndex.insert(std::make_pair(54, 28));
	m_ProvinceToIndex.insert(std::make_pair(61, 29));
	m_ProvinceToIndex.insert(std::make_pair(62, 30));
	m_ProvinceToIndex.insert(std::make_pair(63, 31));
	m_ProvinceToIndex.insert(std::make_pair(64, 32));
	m_ProvinceToIndex.insert(std::make_pair(65, 33));

	m_comDelayTime.ResetContent();
	m_comIntervalTime.ResetContent();
	m_comDefProvince.ResetContent();
	CString strTmp;
	for (std::map<int, int>::iterator iter = m_IndexToDedayTime.begin(); iter != m_IndexToDedayTime.end(); iter++)
	{
		strTmp.Format(_T("%d"), iter->second);
		m_comDelayTime.InsertString(iter->first, strTmp);
	}
	for (std::map<int, int>::iterator iter = m_IndexToTimeSpace.begin(); iter != m_IndexToTimeSpace.end(); iter++)
	{
		strTmp.Format(_T("%d"), iter->second);
		m_comIntervalTime.InsertString(iter->first, strTmp);
	}
	for (std::map<int, int>::iterator iter = m_IndexToProvince.begin(); iter != m_IndexToProvince.end(); iter++)
	{
		strTmp.Format(_T("%d"), iter->second);
		m_comDefProvince.InsertString(iter->first, strTmp);
	}

    UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_VCAEVENT_Structured::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}

}


void CLS_VCAEVENT_Structured::OnLanguageChanged()
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAEVENT_Structured::UpdateUIText()
{
	SetDlgItemTextEx(IDC_STC_STRUCT_EVENTSEL, IDS_VCA_STRUCT_EVENT_SEL);
	SetDlgItemTextEx(IDC_STC_STRUCT_SENSIT, IDS_VCA_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_PUSHMODE, IDS_VCA_STRUCT_PUSHMODE);
	SetDlgItemTextEx(IDC_STC_PUSH_LEVEL, IDS_VCA_STRUCT_PUSH_LEVEL);
	SetDlgItemTextEx(IDC_STC_MIN_FACESIZE, IDS_VCA_STRUCT_MIN_FACE);
	SetDlgItemTextEx(IDC_STC_MIN_PLATESIZE, IDS_VCA_STRUCT_MIN_PLATE);
	SetDlgItemTextEx(IDC_STC_SNAP_MODE, IDS_VCA_STRUCT_CAP_MODE);
	SetDlgItemTextEx(IDC_STC_SNAP_TIME, IDS_VCA_STRUCT_CAPNUM);
	SetDlgItemTextEx(IDC_STC_STRUCT_BRIGHT, IDS_VCA_STRUCT_EXP_BRIGHTNESS);
	SetDlgItemTextEx(IDC_CHECK_SHOW_TARGETFRAME, IDS_VCA_STRUCT_SHOW_TARGET);
	SetDlgItemTextEx(IDC_CHECK_PLATE_EXPOSURE_ENABLE, IDS_CHECK_SHOW_TARGETFRAME);
	SetDlgItemTextEx(IDC_CHECK_SHOW_RULEFRAME, IDS_VCA_STRUCT_SHOW_RULE);
	SetDlgItemTextEx(IDC_BUTTON_STRUCT_SAVE, IDS_VCA_TARPIC_SAVE);
	SetDlgItemTextEx(IDC_STC_STRUCT_TIMING, IDS_VCA_STRUCT_STC_TIMING);

	SetDlgItemTextEx(IDC_CHECK_STRUCT_FACE, IDS_VCA_STRUCT_FACE);
	SetDlgItemTextEx(IDC_CHECK_STRUCT_PERSON, IDS_VCA_STRUCT_PERSON);
	SetDlgItemTextEx(IDC_CHECK_STRUCT_PLATE, IDS_VCA_STRUCT_PLATE);
	SetDlgItemTextEx(IDC_CHECK_STRUCT_MOTORVEHICLE, IDS_VCA_STRUCT_MOTOR_VEHICLE);
	SetDlgItemTextEx(IDC_CHECK_STRUCT_NONMOTORVEHICLE, IDS_VCA_STRUCT_NONMOTOR_VEHICLE);
	SetDlgItemTextEx(IDC_STC_STRUCT_FACE_QUALITY, IDS_VCA_STRUCT_FACE_QUALITY);
	SetDlgItemTextEx(IDC_STC_STRUCT_PERSON_QUALITY, IDS_VCA_STRUCT_PERSON_QUALITY);
	SetDlgItemTextEx(IDC_STC_STRUCT_PLATE_QUALITY, IDS_VCA_STRUCT_PLATE_QUALITY);
	SetDlgItemTextEx(IDC_STC_STRUCT_MOTORVEHICLE_QUALITY, IDS_VCA_STRUCT_MOTORVEHICLE_QUALITY);
	SetDlgItemTextEx(IDC_STC_STRUCT_NONMOTORVEHICLE_QUALITY, IDS_VCA_STRUCT_NONMOTORVEHICLE_QUALITY);

	SetDlgItemTextEx(IDC_STC_STRUCT_POINTNUM, IDS_VCAEVENT_POLYGON_POINT_NUM);
	SetDlgItemTextEx(IDC_STATIC_STRUCT_AREA, IDS_VCAEVENT_POLYGON);
	SetDlgItemTextEx(IDC_BTN_STRUCT_DRAW, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_STC_STRUCT_BIGIMG_QUALITY, IDS_VCA_STRUCT_BIGIMG_QUALITY);
	SetDlgItemTextEx(IDC_STC_STRUCT_SMALLIMG_QUALITY, IDS_VCA_STRUCT_SMALLIMG_QUALITY);
	SetDlgItemTextEx(IDC_STATIC_EXPOSURE_TYPE, IDS_TEXT_EXPOSURE_TYPE);
	SetDlgItemTextEx(IDC_STATIC_EXPOSURE_ENABLE, IDS_TEXT_EXPOSURE_ENABLE);
	SetDlgItemTextEx(IDC_STATIC_PLATEALARMTYPE, IDS_CONFIG_ITS_LICENSE);

	SetDlgItemTextEx(IDC_STATIC_DELAY_TIME, IDS_TEXT_DELAY_TIME);
	SetDlgItemTextEx(IDC_STATIC_INTERVAL_TIME, IDS_TEXT_INTERVAL_TIME);
	SetDlgItemTextEx(IDC_STATIC_DEF_PROVINCE, IDS_TEXT_DEF_PROVINCE);
	SetDlgItemTextEx(IDC_STATIC_PLATE_EXPOSURE_BRIGHT, IDS_PLATE_EXPOSURE_BRIGHT);
	SetDlgItemTextEx(IDC_STATIC_PLATE_EXPOSURE_TIME, IDS_PLATE_EXPOSURE_TIME);
		
    m_comExposureType.ResetContent();
    m_comExposureType.InsertString(0, GetTextByLan("保留", "Reserve"));
    m_comExposureType.InsertString(1, GetTextByLan("长帧", "Long Frame"));
    m_comExposureType.InsertString(2, GetTextByLan("短帧", "Short Frame"));

    m_comExposureEnable.ResetContent();
    m_comExposureEnable.InsertString(0, GetTextByLan("不使能", "Disabled"));
    m_comExposureEnable.InsertString(1, GetTextByLan("使能", "Enabled"));

    m_comPlateAlarmType.ResetContent();
    m_comPlateAlarmType.InsertString(0, GetTextByLan("保留", "Reserve"));
    m_comPlateAlarmType.InsertString(1, GetTextByLan("车牌黑名单报警", "Plate Blacklist Alarm"));

}

void CLS_VCAEVENT_Structured::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	GetFuncAbilityPushMode();
	GetFuncAbilityDetectType();
	UpdataManCarMixInfo();
}

void CLS_VCAEVENT_Structured::UpdataManCarMixInfo()
{
	VcaManCarDetectArithmetic tVca = {0};

	int iRet = -1;
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_MANCAR_DETECTARITHMETIC, m_iChannelNO, &tVca, sizeof(VcaManCarDetectArithmetic));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_Structured::NetClient_VCAGetConfig[VCA_CMD_VEHICLE_DETECTARITHMETIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{		
		SetParamDetectType(tVca.iType);
		m_sldSensitivity.SetPos(tVca.iSensitiv);
		m_comPushMode.SetCurSel(tVca.iPushMode - 1);

		if (ARI_PUSHMODE_CUSTOM == tVca.iPushMode)
		{
			m_comPushLevel.SetCurSel(tVca.iPushLevel - 1);
			m_comPushLevel.EnableWindow(TRUE);
			m_edtPushLevel.EnableWindow(FALSE);
		}
		else if (ARI_PUSHMODE_TIMING == tVca.iPushMode)
		{
			SetDlgItemInt(IDC_EDIT_PUSH_LEVEL, tVca.iPushLevel);
			m_edtPushLevel.EnableWindow(TRUE);
			m_comPushLevel.EnableWindow(FALSE);
		}
		else
		{
			m_comPushLevel.EnableWindow(FALSE);
			m_edtPushLevel.EnableWindow(FALSE);
		}
		m_comSnapMode.SetCurSel(tVca.iSnapMode - 1);	

		if (ARI_SNAPMODE_CUSTOM == tVca.iSnapMode)
		{
			//Quality slider released
			m_sldFace.EnableWindow(TRUE);
			m_sldPerson.EnableWindow(TRUE);
			m_sldPlate.EnableWindow(TRUE);
			m_sldMotorVehicle.EnableWindow(TRUE);
			m_sldNonMotorVehicle.EnableWindow(TRUE);

			m_sldFace.SetPos(tVca.iFaceQuality);
			m_sldPerson.SetPos(tVca.iHumanQuality);
			m_sldPlate.SetPos(tVca.iPlateQuality);
			m_sldMotorVehicle.SetPos(tVca.iVehicleQuality);
			m_sldNonMotorVehicle.SetPos(tVca.iCycleQuality);
		}
		else
		{
			//Grayed out quality slider
			m_sldFace.EnableWindow(FALSE);
			m_sldPerson.EnableWindow(FALSE);
			m_sldPlate.EnableWindow(FALSE);
			m_sldMotorVehicle.EnableWindow(FALSE);
			m_sldNonMotorVehicle.EnableWindow(FALSE);
		}

		m_sldBigImage.SetPos(tVca.iBigbkimgQuality);
		m_sldSmallImage.SetPos(tVca.iSmallimgQuality);

		m_comSnapTime.SetCurSel(tVca.iSnapTimes -1);

		m_sldBright.SetPos(tVca.iExposureBright);

		SetDlgItemInt(IDC_EDIT_MIN_FACESIZE, tVca.iMinFaceSize);
		SetDlgItemInt(IDC_EDIT_PLATESIZE, tVca.iMinPlateSize);
		SetDlgItemInt(IDC_EDIT_PLATE_EXPOSURE_BRIGHT, tVca.iPlateExposureBright);
		SetDlgItemInt(IDC_EDIT_PLATE_EXPOSURE_TIME, tVca.iPlateExposureTime);
		m_edtMinFace.EnableWindow(TRUE);
		m_edtMinPlate.EnableWindow(TRUE);
	
		if (ARI_PUSHMODE_TOUCHLINE == tVca.iPushMode)//hit the line
		{
			SetDlgItemInt(IDC_EDIT_STRUCT_POINTNUM, tVca.iTripPointNum);
			CString cstPolygonBuf;
			for(int i = 0; i < tVca.iTripPointNum; i++)
			{
				cstPolygonBuf.AppendFormat("(%d, %d)", tVca.ptTripArea[i].iX, tVca.ptTripArea[i].iY);
			}
			SetDlgItemText(IDC_EDIT_STRUCT_AREA, cstPolygonBuf);
		}
		else
		{
			SetDlgItemInt(IDC_EDIT_STRUCT_POINTNUM, tVca.iPointNum);
			CString cstPolygonBuf;
			for(int i = 0; i < tVca.iPointNum; i++)
			{
				cstPolygonBuf.AppendFormat("(%d, %d)", tVca.ptArea[i].iX, tVca.ptArea[i].iY);
			}
			SetDlgItemText(IDC_EDIT_STRUCT_AREA, cstPolygonBuf);
		}

		m_chkShowTarget.SetCheck(tVca.iDisplayTarget);
		m_chkPlateExposureBox.SetCheck(tVca.iPlateExposureEnable);
		
		m_chkShowRule.SetCheck(tVca.iDisplayRule);

        m_comExposureType.SetCurSel(tVca.iExposureType);
        m_comExposureEnable.SetCurSel(tVca.iExposureEnable);
        m_comPlateAlarmType.SetCurSel(tVca.iPlateAlarmType);
		
		m_comDelayTime.SetCurSel(m_DelayTimeToIndex[tVca.iDelayPushSpan]);
		m_comIntervalTime.SetCurSel(m_TimeSpaceToIndex[tVca.iTimeSpace]);
		m_comDefProvince.SetCurSel(m_ProvinceToIndex[tVca.iDefaultProvince]);	
	}
}

void CLS_VCAEVENT_Structured::OnBnClickedButtonStructSave()
{
	if (m_iLogonID < 0 || m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_Structured::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNO);
		return;
	}

	VcaManCarDetectArithmetic tVca = {0};
	int iRet = -1;
	

	tVca.iSize = sizeof(VcaManCarDetectArithmetic);
	tVca.iSceneID = m_iSceneID;
	tVca.iType = ReturnParamDetectType();
	tVca.iSensitiv = m_sldSensitivity.GetPos();
	tVca.iDisplayTarget = (BST_CHECKED == m_chkShowTarget.GetCheck()) ? 1 : 0;
	tVca.iPlateExposureEnable = (BST_CHECKED == m_chkPlateExposureBox.GetCheck()) ? 1 : 0;
	tVca.iExposureBright = m_sldBright.GetPos();
	tVca.iDisplayRule = (BST_CHECKED == m_chkShowRule.GetCheck()) ? 1 : 0;
	tVca.iPushMode = (m_comPushMode.GetCurSel()) + 1;

	if (ARI_PUSHMODE_CUSTOM == tVca.iPushMode)
	{
		tVca.iPushLevel = (m_comPushLevel.GetCurSel()) + 1;
		m_comPushLevel.EnableWindow(TRUE);
	}
	else if (ARI_PUSHMODE_TIMING == tVca.iPushMode)
	{
		tVca.iPushLevel = GetDlgItemInt(IDC_EDIT_PUSH_LEVEL);
		m_edtPushLevel.EnableWindow(TRUE);
	}
	else
	{
		m_comPushLevel.EnableWindow(FALSE);
		m_edtPushLevel.EnableWindow(FALSE);
	}

	tVca.iSnapMode = (m_comSnapMode.GetCurSel()) + 1;	
	tVca.iSnapTimes = (m_comSnapTime.GetCurSel()) + 1;	
	tVca.iMinFaceSize = GetDlgItemInt(IDC_EDIT_MIN_FACESIZE);
	tVca.iMinPlateSize = GetDlgItemInt(IDC_EDIT_PLATESIZE);
	tVca.iFaceQuality = m_sldFace.GetPos();
	tVca.iHumanQuality = m_sldPerson.GetPos();
	tVca.iPlateQuality = m_sldPlate.GetPos();
	tVca.iVehicleQuality = m_sldMotorVehicle.GetPos();
	tVca.iCycleQuality = m_sldNonMotorVehicle.GetPos();
	tVca.iBigbkimgQuality = m_sldBigImage.GetPos();
	tVca.iSmallimgQuality = m_sldSmallImage.GetPos();

	if (ARI_PUSHMODE_TOUCHLINE == tVca.iPushMode)//hit the line
	{
		tVca.iTripPointNum = GetDlgItemInt(IDC_EDIT_STRUCT_POINTNUM);
		
		TPoint ptPolygon[MAX_MANCAR_DETECT_TRIPAREA_COUNT] = {0} ;
		CString cstPolygon = "";
		GetDlgItemText(IDC_EDIT_STRUCT_AREA, cstPolygon);
		GetPointsFromString(cstPolygon, tVca.iTripPointNum, ptPolygon);
		for (int i = 0; i < tVca.iTripPointNum; i++)
		{
			tVca.ptTripArea[i] = ptPolygon[i];
		}

		//When hitting the line type, the tVca.ptArea polygon area should be set to full screen
		tVca.iPointNum = 4;
		tVca.ptArea[0].iX = 1;
		tVca.ptArea[0].iY = 1;
		tVca.ptArea[1].iX = 1;
		tVca.ptArea[1].iY = 10000;
		tVca.ptArea[2].iX = 10000;
		tVca.ptArea[2].iY = 10000;
		tVca.ptArea[3].iX = 10000;
		tVca.ptArea[3].iY = 1;
	}
	else
	{
		tVca.iPointNum = GetDlgItemInt(IDC_EDIT_STRUCT_POINTNUM);

		TPoint ptPolygon[MAX_MANCAR_DETECT_AREA_COUNT] = {0} ;
		CString cstPolygon = "";
		GetDlgItemText(IDC_EDIT_STRUCT_AREA, cstPolygon);
		GetPointsFromString(cstPolygon, tVca.iPointNum, ptPolygon);
		for (int i = 0; i < tVca.iPointNum; i++)
		{
			tVca.ptArea[i] = ptPolygon[i];
		}
	}

    tVca.iExposureType = (m_comExposureType.GetCurSel());	
    tVca.iExposureEnable = (m_comExposureEnable.GetCurSel());	
    tVca.iPlateAlarmType = (m_comPlateAlarmType.GetCurSel());

	tVca.iDelayPushSpan = m_IndexToDedayTime[m_comDelayTime.GetCurSel()];	
	tVca.iTimeSpace = m_IndexToTimeSpace[m_comIntervalTime.GetCurSel()];	
	tVca.iDefaultProvince = m_IndexToProvince[m_comDefProvince.GetCurSel()];
	tVca.iPlateExposureBright = GetDlgItemInt(IDC_EDIT_PLATE_EXPOSURE_BRIGHT);
	tVca.iPlateExposureTime = GetDlgItemInt(IDC_EDIT_PLATE_EXPOSURE_TIME);
	//算法要求顶点必须大于等于4
	if (4 > tVca.iPointNum)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAEVENT_Structured::Invalid Points greater than or equal to 4, logon id(%d), tVca.iPointNum(%d)", m_iLogonID, tVca.iPointNum);
		AfxMessageBox(GetTextByLan("点数必须大于等于4", "Points greater than or equal to 4"));
		return;
	}
	
	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_MANCAR_DETECTARITHMETIC, m_iChannelNO, &tVca, sizeof(VcaManCarDetectArithmetic));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_Structured::NetClient_VCASetConfig[VCA_CMD_TARGET_PICTURE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_Structured::NetClient_VCASetConfig[VCA_CMD_TARGET_PICTURE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_Structured::OnCbnSelchangeComboPushmode()
{
	int iIndex = m_comPushMode.GetCurSel();
	
	if (PUSHMODE_CUSTOM == (iIndex + 1))//custom mode
	{
		m_comPushLevel.EnableWindow(TRUE);
		m_comPushLevel.SetCurSel(0);
		m_edtPushLevel.EnableWindow(FALSE);
	}
	else if (PUSHMODE_TIMING == (iIndex + 1))//timing
	{
		m_edtPushLevel.EnableWindow(TRUE);
		m_comPushLevel.EnableWindow(FALSE);
	}
	else
	{
		m_comPushLevel.EnableWindow(FALSE);
		m_edtPushLevel.EnableWindow(FALSE);
	}

}

void CLS_VCAEVENT_Structured::OnCbnSelchangeComboSnapMode()
{
	int iIndex = m_comSnapMode.GetCurSel();

	if (ARI_SNAPMODE_CUSTOM == (iIndex + 1))//customize
	{
		m_sldFace.EnableWindow(TRUE);
		m_sldPerson.EnableWindow(TRUE);
		m_sldPlate.EnableWindow(TRUE);
		m_sldMotorVehicle.EnableWindow(TRUE);
		m_sldNonMotorVehicle.EnableWindow(TRUE);
	}
	else
	{
		m_sldFace.EnableWindow(FALSE);
		m_sldPerson.EnableWindow(FALSE);
		m_sldPlate.EnableWindow(FALSE);
		m_sldMotorVehicle.EnableWindow(FALSE);
		m_sldNonMotorVehicle.EnableWindow(FALSE);
	}
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSliderStructSensit(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);	
	SetDlgItemInt(IDC_STC_STRUCT_SENSIT_NUM, m_sldSensitivity.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSliderStructBright(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_BRIGHT_NUM, m_sldBright.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSldFaceQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_FACE_QUALITY_NUM, m_sldFace.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSldPersonQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_PERSON_QUALITY_NUM, m_sldPerson.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSldPlateQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_PLATE_QUALITY_NUM, m_sldPlate.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSldMotorvehicleQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_MOTORVEHICLE_QUALITY_NUM, m_sldMotorVehicle.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSldNonmotorvehicleQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_NONMOTORVEHICLE_QUALITY_NUM, m_sldNonMotorVehicle.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSlidStructBigimgQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_BIGIMG_QUALITY_NUM, m_sldBigImage.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_Structured::OnNMCustomdrawSlidStructSmallimgQuality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_STRUCT_SMALLIMG_QUALITY_NUM, m_sldSmallImage.GetPos());
	*pResult = 0;
}

int CLS_VCAEVENT_Structured::ReturnParamDetectType()
{
	int _param = 0;
	for(int i = DETECT_TYPE_NUM; i != -1; i--)
	{
		if (!IsWindow(m_chkDetectType[i].GetSafeHwnd()))
		{
			continue;
		}

		if(m_chkDetectType[i].IsWindowEnabled())
		{
			_param |= m_chkDetectType[i].GetCheck()<<i;
		}		
	}
	return _param;
}

void CLS_VCAEVENT_Structured::SetParamDetectType( int _param )
{
	int _iStatus;
	for(int i = DETECT_TYPE_NUM - 1; i != -1; i--)
	{
		if (!IsWindow(m_chkDetectType[i].GetSafeHwnd()))
		{
			continue;
		}

		_iStatus = (_param >> i) & 0x01;
		m_chkDetectType[i].SetCheck(_iStatus);
	}
}

void CLS_VCAEVENT_Structured::GetFuncAbilityPushMode()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_Structured::GetFuncAbilityPushMode] Error  LogonID!");
		return;
	}

	int iByteReturn = -1;
	FuncAbilityLevel stFunAbilityLevel = {0};
	stFunAbilityLevel.iSize = sizeof(stFunAbilityLevel);
	stFunAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFunAbilityLevel.iSubFuncType  = GETVCA_FUNC_ABILITY_PUSHMODE;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stFunAbilityLevel, sizeof(stFunAbilityLevel), &iByteReturn);
	if (iRet < 0 || strlen(stFunAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_Structured::GetFuncAbility] GetDevConfig NET_CLIENT_GET_FUNC_ABILITY Failed! m_iLogonID %d", m_iLogonID);
		return;
	}

	CString strPushMode[] = {GetTextEx(IDS_VCA_STRUCT_FASTEST),GetTextEx(IDS_VCA_STRUCT_OPTIMAL), GetTextEx(IDS_VCA_STRUCT_CUSTOMIZE), GetTextEx(IDS_VCA_STRUCT_TIMING), GetTextEx(IDS_VCA_STRUCT_HITLINE)};
	m_comPushMode.ResetContent();

	CString cstrTempParam = stFunAbilityLevel.cParam;
	int iFuncPara = _ttoi(cstrTempParam);
	for (int i = 0; i < ARI_PUSHMODE_TOUCHLINE; i++)
	{
		BOOL blTempChk = (iFuncPara & 0x01<<i)?TRUE:FALSE;
		if (blTempChk)
		{
			m_comPushMode.InsertString(i, strPushMode[i]);
		}
		else
		{
			CString cstrForPushMode;
			cstrForPushMode.Format("%s Nonsupport",strPushMode[i]);
			m_comPushMode.InsertString(i, cstrForPushMode);
		}
	}

}

void CLS_VCAEVENT_Structured::GetFuncAbilityDetectType()
{
	if (m_iLogonID < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_Structured::GetFuncAbilityDetectType] Error  LogonID!");
		return;
	}

	int iByteReturn = -1;
	FuncAbilityLevel stFunAbilityLevel = {0};
	memset(&stFunAbilityLevel , 0 ,sizeof(FuncAbilityLevel));
	stFunAbilityLevel.iSize = sizeof(stFunAbilityLevel);
	stFunAbilityLevel.iMainFuncType = MAIN_FUNC_TYPE_VCA;
	stFunAbilityLevel.iSubFuncType  = GETVCA_FUNC_ABILITY_DETECT_TYPE;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_GET_FUNC_ABILITY, m_iChannelNO, (void*)&stFunAbilityLevel, sizeof(stFunAbilityLevel), &iByteReturn);
	if (iRet < 0 || strlen(stFunAbilityLevel.cParam) < 1)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEVENT_Structured::GetFuncAbilityDetectType] GetDevConfig NET_CLIENT_GET_FUNC_ABILITY Failed! m_iLogonID %d", m_iLogonID);
		return;
	}

	CString cstrTempParam = stFunAbilityLevel.cParam;
	int iFuncPara = _ttoi(cstrTempParam);
	for (int i = 0; i <DETECT_TYPE_NUM; i++)
	{
		BOOL blTempChk = (iFuncPara & 0x01<<i)?TRUE:FALSE;
		if (blTempChk)
		{
			m_chkDetectType[i].EnableWindow(TRUE);
		}
		else
		{
			m_chkDetectType[i].EnableWindow(FALSE);
		}
	}
}
void CLS_VCAEVENT_Structured::OnBnClickedBtnStructDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 1)
		{
			m_edtAreaInfo.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_STRUCT_POINTNUM, iPointNum);
		}
		else
		{
			m_edtAreaInfo.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_STRUCT_POINTNUM, 0);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}


void CLS_VCAEVENT_Structured::GetPointsFromString(CString _strPoints, int _iPointNum, TPoint* _poPoint)
{
	int iLength = _strPoints.GetLength()+1;
	char* pcData = new char [iLength];
	memset(pcData, 0, iLength);
	memcpy(pcData, _strPoints.GetBuffer(), iLength-1);
	char* p1 = pcData;
	char* p2 = NULL;
	int iPointIndex = 0;
	for (int i = 0; i < iLength; ++i)
	{
		p2 = strstr(p1, ")");
		if (p2 == NULL)
			break;

		char cCell[200] = {0};
		int iX = 0, iY = 0;
		memcpy(cCell, p1, p2-p1+1);
		sscanf_s(cCell, "(%d,%d)", &iX, &iY);
		_poPoint[iPointIndex].iX = iX;
		_poPoint[iPointIndex].iY = iY;
		if (++iPointIndex == _iPointNum)
			break;

		if ((p1 = p2+1) >= pcData+iLength)
			break;
	}
	delete [] pcData;
	pcData = NULL;
}
