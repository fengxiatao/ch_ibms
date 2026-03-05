
#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEvnetsAdvParam.h"


IMPLEMENT_DYNAMIC(CLS_VCAEvnetsAdvParam, CDialog)
#define ADJUST_EVENT_ADV_RECT(rc) {rc.top += 20; rc.left += 5; rc.right -= 5; rc.bottom -= 5;}

CLS_VCAEvnetsAdvParam::CLS_VCAEvnetsAdvParam(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCAEvnetsAdvParam::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNO = 0;
	m_iCurrentSel = 0;
	for(int i=0; i<MAX_EVENT_ADV_NUM; i++)
	{
		m_plArrEventAdvPage[i] = NULL;
	}
}

CLS_VCAEvnetsAdvParam::~CLS_VCAEvnetsAdvParam()
{
	for(int i=0; i<MAX_EVENT_ADV_NUM; i++)
	{
		if (m_plArrEventAdvPage[i])
		{
			m_plArrEventAdvPage[i]->DestroyWindow();
			delete m_plArrEventAdvPage[i];
			m_plArrEventAdvPage[i] = NULL;
		}
	}
}

void CLS_VCAEvnetsAdvParam::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_EVENT_ID, m_cboAdvEventType);
	DDX_Control(pDX, IDC_CBO_ADV_SCENEID, m_cboAdvSceneID);
	DDX_Control(pDX, IDC_CHECK_ALGO_DEBUG, m_chkOpenAlgoDebug);
	DDX_Control(pDX, IDC_CMB_SCENEID, m_cboAdvAnySceneID);
	DDX_Control(pDX, IDC_CMB_DEVTYPE, m_cboAdvDevType);
	DDX_Control(pDX, IDC_CMB_SCENEFOCUSTYPE, m_cboAdvFocusType);
	DDX_Control(pDX, IDC_CHECK_MOTION_DETECTION_CAR_ALARM_ENABLE, m_chkMotionDetectionCarAlarmEnable);
}


BEGIN_MESSAGE_MAP(CLS_VCAEvnetsAdvParam, CDialog)
	ON_CBN_SELCHANGE(IDC_CBO_EVENT_ID, &CLS_VCAEvnetsAdvParam::OnCbnSelchangeCboEventId)
	ON_CBN_SELCHANGE(IDC_CBO_ADV_SCENEID, &CLS_VCAEvnetsAdvParam::OnCbnSelchangeCboAdvSceneid)
	ON_BN_CLICKED(IDC_CHECK_ALGO_DEBUG, &CLS_VCAEvnetsAdvParam::OnBnClickedCheckAlgoDebug)

	ON_BN_CLICKED(IDC_BTN_SCENEADVSET, &CLS_VCAEvnetsAdvParam::OnBnClickedBtnSceneadvset)
	ON_CBN_SELCHANGE(IDC_CMB_SCENEID, &CLS_VCAEvnetsAdvParam::OnCbnSelchangeCmbSceneid)
	ON_CBN_SELCHANGE(IDC_CMB_DEVTYPE, &CLS_VCAEvnetsAdvParam::OnCbnSelchangeCmbDevtype)
	ON_BN_CLICKED(IDC_BUTTON_MOTION_DETECTION_CAR_ALARM_ENABLE, &CLS_VCAEvnetsAdvParam::OnBnClickedButtonMotionDetectionCarAlarmEnable)
END_MESSAGE_MAP()


BOOL CLS_VCAEvnetsAdvParam::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	GetDlgItem(IDC_CHECK_ALGO_DEBUG)->ShowWindow(SW_HIDE);
	UI_Updata();
	GetAnySceneAdvancedParam();
	OnCbnSelchangeCboAdvSceneid();
	return TRUE;
}

void CLS_VCAEvnetsAdvParam::OnLanguageChanged( int _iLanguage )
{
	UI_Updata();
	m_cboAdvEventType.SetCurSel(0);
	m_cboAdvSceneID.SetCurSel(0);
	for (int i=0; i<MAX_EVENT_ADV_NUM; i++)
	{
		if (m_plArrEventAdvPage[i])
		{
			m_plArrEventAdvPage[i]->OnLanguageChanged();
		}
	}
}

void CLS_VCAEvnetsAdvParam::OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iSelIndex = m_cboAdvEventType.GetCurSel();
	if (m_plArrEventAdvPage[iSelIndex])
	{
		m_plArrEventAdvPage[iSelIndex]->OnMainNotify(_iLogonID, _wParam, _iLParam, _iUser);
	}
}

void CLS_VCAEvnetsAdvParam::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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
	if (_iStreamNo < 0)
	{
		m_iStreamNO = 0;
	}
	else
	{
		m_iStreamNO = _iStreamNo;
	}
	OnCbnSelchangeCboEventId();

	UI_UpdateAlgoDebugInfo();
	GetAnySceneAdvancedParam();
	UI_UpdateMotionDetectionCarParam();
}


void CLS_VCAEvnetsAdvParam::UI_Updata()
{
	SetDlgItemTextEx(IDC_STC_ADV_EVENT_ID, IDS_VCA_EVENT_ID);
	SetDlgItemTextEx(IDC_STC_ADV_SCENEID, IDS_VCA_SCENE_ID);
	SetDlgItemTextEx(IDC_GBO_VCA_EVENT_ADV_PARAM, IDS_CONFIGPAGE_VCA_EVENTS_ADV);
	SetDlgItemText(IDC_CHECK_ALGO_DEBUG, GetTextByLan(_T("开启算法调试"), _T("Open Algo Debug")));
	SetDlgItemText(IDC_STATIC_ADVANCEDSCENEID, GetTextByLan(_T("场景ID"), _T("Scene Adv ID")));
	SetDlgItemText(IDC_STATIC_DEVTYPE, GetTextByLan(_T("设备类型"), _T("DevType")));
	SetDlgItemText(IDC_STATIC_SCENEFOCUSTYPE, GetTextByLan(_T("场景聚焦模式"), _T("Scene Focus Type")));
	SetDlgItemText(IDC_BTN_SCENEADVSET, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_CHECK_MOTION_DETECTION_CAR_ALARM_ENABLE, GetTextByLan(_T("移动侦测之车形报警使能"), _T("Motion detection car alarm enabled")));
	SetDlgItemText(IDC_BUTTON_MOTION_DETECTION_CAR_ALARM_ENABLE, GetTextByLan(_T("设置车形报警使能"), _T("Car shape alarm enable")));
	SetDlgItemText(IDC_STATIC_MOTION_DETECTION_CAR_PARAM, GetTextByLan(_T("移动侦测之车形报警参数"), _T("Motion detection car alarm param")));
	const CString strEvent[] = {
		GetTextEx(IDS_VCA_EVENT_RIVER), GetTextEx(IDS_CFG_FUNC_WATER_LEVEL)
	};

	m_cboAdvSceneID.ResetContent();
	int j;
	for (int i = 0; i < MAX_SCENE_NUM; ++i)
	{
		j = i + 1;
		m_cboAdvSceneID.InsertString(i, IntToCString(j));
		
	}
	m_cboAdvSceneID.SetCurSel(0);
	m_cboAdvEventType.ResetContent();
	for (int i=0; i<sizeof(strEvent)/sizeof(CString); i++)
	{
		m_cboAdvEventType.InsertString(i, strEvent[i]);
	}
	m_cboAdvEventType.SetCurSel(0);

	m_cboAdvAnySceneID.ResetContent();
	int iIndex;
	for(int i = 0; i < 32; i++){
		 iIndex = i + 1;
		m_cboAdvAnySceneID.InsertString(i,IntToCString(iIndex));
	}
	m_cboAdvAnySceneID.SetCurSel(0);
	
	m_cboAdvDevType.ResetContent();
	m_cboAdvDevType.InsertString(0, "0-IPC");
	m_cboAdvDevType.InsertString(1, "1-NVR");

	m_cboAdvDevType.SetCurSel(0);

	m_cboAdvFocusType.ResetContent();

	m_cboAdvFocusType.InsertString(0, GetTextByLan(_T("1-自动"), _T("1-Auto")));
	m_cboAdvFocusType.InsertString(1, GetTextByLan(_T("2-固定"), _T("2-Fixed")));
	m_cboAdvFocusType.SetCurSel(0);

	//int iUseRule = GetDemoUseRule();
	//if (RIVER_USE == iUseRule)
	//{
	//	GetDlgItem(IDC_CHECK_ALGO_DEBUG)->ShowWindow(SW_SHOW);
	//}
	//else
	//{
	//	GetDlgItem(IDC_CHECK_ALGO_DEBUG)->ShowWindow(SW_HIDE);
	//}
}

void CLS_VCAEvnetsAdvParam::OnCbnSelchangeCboEventId()
{
	int iSel = m_cboAdvEventType.GetCurSel();
	for (int i=0; i<MAX_EVENT_ADV_NUM; i++)
	{
		if (NULL != m_plArrEventAdvPage[i])
		{
			m_plArrEventAdvPage[i]->ShowWindow(SW_HIDE);	
		}
	}

	switch (iSel)
	{
	case RIVER_CLEAN_ADV_SEL:
		if (NULL == m_plArrEventAdvPage[RIVER_CLEAN_ADV_SEL])
		{
			m_plArrEventAdvPage[RIVER_CLEAN_ADV_SEL] = new CLS_VCAAdvParam_River();
			m_plArrEventAdvPage[RIVER_CLEAN_ADV_SEL]->Create(IDD_DLG_VCAPARAM_ADV_RIVER, this);
		}
		break;
	case WLD_ADV_SEL:
		if (NULL == m_plArrEventAdvPage[WLD_ADV_SEL])
		{
			m_plArrEventAdvPage[WLD_ADV_SEL] = new CLS_VCAAdvParamWld();
			m_plArrEventAdvPage[WLD_ADV_SEL]->Create(IDD_DLG_VCAPARAM_ADV_WLD, this);
		}
		break;
	default:
		break;
	}

	if (m_plArrEventAdvPage[iSel])
	{
		RECT rcShow = {0};
		GetDlgItem(IDC_GBO_VCA_EVENT_ADV_PARAM)->GetWindowRect(&rcShow);
		ScreenToClient(&rcShow);
		ADJUST_EVENT_ADV_RECT(rcShow);
		m_plArrEventAdvPage[iSel]->MoveWindow(&rcShow);
		m_plArrEventAdvPage[iSel]->SetSceneID(m_iScreenID);
		m_plArrEventAdvPage[iSel]->Init(m_iLogonID, m_iChannelNo, m_iStreamNO);
		m_plArrEventAdvPage[iSel]->ShowWindow(SW_SHOW);
	}
}

void CLS_VCAEvnetsAdvParam::OnCbnSelchangeCboAdvSceneid()
{
	// TODO: Add your control notification handler code here
	m_iScreenID = m_cboAdvSceneID.GetCurSel();
	OnCbnSelchangeCboEventId();
}

void CLS_VCAEvnetsAdvParam::OnBnClickedCheckAlgoDebug()
{
	int iRet = RET_FAILED;
	int iOpenEnable = 0;
	if (BST_CHECKED == m_chkOpenAlgoDebug.GetCheck())
	{
		iOpenEnable = ENABLE;
		iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_DZ_ALGO_DEBUG, PARAM_CHANNEL_ALL, iOpenEnable);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable::CI_COMMON_ID_DZ_ALGO_DEBUG failed!iOpenEnable(%d)", iOpenEnable);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable::CI_COMMON_ID_DZ_ALGO_DEBUG success!iOpenEnable(%d)", iOpenEnable);
		}
	}
	else
	{
		iOpenEnable = DISABLE;
		iRet = NetClient_SetCommonEnable(m_iLogonID, CI_COMMON_ID_DZ_ALGO_DEBUG, PARAM_CHANNEL_ALL, iOpenEnable);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetCommonEnable::CI_COMMON_ID_DZ_ALGO_DEBUG failed!iOpenEnable(%d)", iOpenEnable);
		}
		else
		{
			AddLog(LOG_TYPE_SUCC, "", "NetClient_SetCommonEnable::CI_COMMON_ID_DZ_ALGO_DEBUG success!iOpenEnable(%d)", iOpenEnable);
		}
	}
}

void CLS_VCAEvnetsAdvParam::UI_UpdateAlgoDebugInfo()
{
	int iOpenEnable = 0;
	int iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_DZ_ALGO_DEBUG, PARAM_CHANNEL_ALL, &iOpenEnable);
	if (RET_SUCCESS == iRet && iOpenEnable >= 0)
	{
		m_chkOpenAlgoDebug.SetCheck(iOpenEnable);
	}
}
void CLS_VCAEvnetsAdvParam::OnBnClickedBtnSceneadvset()
{	
	AnyScene tAnyScene = {0};
	tAnyScene.iBufSize = sizeof(AnyScene);
	tAnyScene.iSceneID = m_cboAdvAnySceneID.GetCurSel();
	tAnyScene.iDevType = m_cboAdvDevType.GetCurSel();
	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNO, &tAnyScene,sizeof(tAnyScene), &iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "OnBnClickedBtnSceneadvset::VCA_CMD_SET_ANYSENCEADVANCED failed!iRet(%d)", iRet);		
	}
	int iArithmeticEx = tAnyScene.iArithmetic;
	VcaAnySenceAdvanced tInfo = {0};
	tInfo.iChannelNo = m_iChannelNo;
	tInfo.iDevType = m_cboAdvDevType.GetCurSel();
	tInfo.iSceneId = m_cboAdvAnySceneID.GetCurSel();
	tInfo.iFocusType = m_cboAdvFocusType.GetCurSel();

	iRet = NetClient_VCASetConfig(m_iLogonID,VCA_CMD_ANYSENCEADCANCED,m_iChannelNo,&tInfo,sizeof(tInfo));
	if(RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "OnBnClickedBtnSceneadvset::VCA_CMD_SET_ANYSENCEADVANCED failed!iRet(%d)", iRet);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "OnBnClickedBtnSceneadvset::VCA_CMD_SET_ANYSENCEADVANCED Succ!iRet(%d)", iRet);
	}

	AnyScene tParam = {0};
	tParam.iBufSize = sizeof(AnyScene);
	tParam.iSceneID =  m_cboAdvAnySceneID.GetCurSel();
	tParam.iDevType =  m_cboAdvDevType.GetCurSel();
	tParam.iArithmeticEx = iArithmeticEx;

	iRet = NetClient_SetDevConfig(m_iLogonID,NET_CLIENT_ANYSCENE,m_iChannelNO,&tParam,sizeof(tParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig]NET_CLIENT_ANYSCENE fail!");
	}
}

void CLS_VCAEvnetsAdvParam::GetAnySceneAdvancedParam()
{


	VcaAnySenceAdvanced tInfo = {0};
	tInfo.iSize = sizeof(tInfo);

	int iReturn = -1;
	tInfo.iSceneId = m_cboAdvAnySceneID.GetCurSel();
	tInfo.iDevType = m_cboAdvDevType.GetCurSel();
    int iRet = NetClient_VCAGetConfig(m_iLogonID,VCA_CMD_ANYSENCEADCANCED,m_iChannelNo,&tInfo,tInfo.iSize);
	if(RET_SUCCESS == iRet)
	{
		m_cboAdvFocusType.SetCurSel(tInfo.iFocusType);
		AddLog(LOG_TYPE_SUCC, "", "GetAnySceneAdvancedParam::VCA_CMD_ANYSENCEADCANCED Succ!iRet(%d)", iRet);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "GetAnySceneAdvancedParam::VCA_CMD_ANYSENCEADCANCED failed!iRet(%d)", iRet);
	}
}

void CLS_VCAEvnetsAdvParam::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType, void* _pPara, int _iUserData)
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEvnetsAdvParam]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	switch(_iParaType)
	{
	case PARA_VCA_ANYSCENEADVANCED:
		{
			GetAnySceneAdvancedParam();
			AddLog(LOG_TYPE_SUCC,"","[CLS_VCAEvnetsAdvParam::OnParamChangeNotify]->PARA_VCA_ANYSCENEADVANCED (%d,%d)",m_iLogonID, m_iChannelNO);
		}
		break;
	default:
		break;
	}
}

void CLS_VCAEvnetsAdvParam::OnCbnSelchangeCmbSceneid()
{
	GetAnySceneAdvancedParam();
}

void CLS_VCAEvnetsAdvParam::OnCbnSelchangeCmbDevtype()
{
	GetAnySceneAdvancedParam();
}

void CLS_VCAEvnetsAdvParam::OnBnClickedButtonMotionDetectionCarAlarmEnable()
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_VCAEvnetsAdvParam::OnBnClickedButtonMotionDetectionCarAlarmEnable]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	MotionDetectCarParam info = {0};
	memset(&info, 0x0, sizeof(info));
	info.iEnable = m_chkMotionDetectionCarAlarmEnable.GetCheck();

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_MOTION_DETECTTION_CAR_PARAM, m_iChannelNO, &info, sizeof(info));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_SetDevConfig] seting motion detection car param return error %d = ", iRet);
		return;
	}
	AddLog(LOG_TYPE_SUCC, "", "[NetClient_SetDevConfig] seting motion detection car param success!");
}

void CLS_VCAEvnetsAdvParam::UI_UpdateMotionDetectionCarParam()
{
	MotionDetectCarParam info = {0};
	memset(&info, 0x0, sizeof(info));
	int iRetLen = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_MOTION_DETECTTION_CAR_PARAM, 
		m_iChannelNO, &info, sizeof(info), &iRetLen);
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig] geting motion detection car param return error %d = ", iRet);
		return;
	}
	m_chkMotionDetectionCarAlarmEnable.SetCheck(info.iEnable);
	AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig] geting motion detection car param success!");
}
