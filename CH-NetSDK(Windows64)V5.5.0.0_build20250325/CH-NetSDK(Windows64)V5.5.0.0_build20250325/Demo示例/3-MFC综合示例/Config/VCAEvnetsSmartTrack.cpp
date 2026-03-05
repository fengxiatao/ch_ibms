
#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEvnetsSmartTrack.h"


IMPLEMENT_DYNAMIC(CLS_VCAEvnetsSmartTrack, CDialog)

CLS_VCAEvnetsSmartTrack::CLS_VCAEvnetsSmartTrack(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCAEvnetsSmartTrack::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
}

CLS_VCAEvnetsSmartTrack::~CLS_VCAEvnetsSmartTrack()
{

}

void CLS_VCAEvnetsSmartTrack::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDT_TRACK_MULRIPE, m_edtTrackMulripe);
	DDX_Control(pDX, IDC_EDT_TRACK_DOME_HEIGHT, m_edtDomeHeight);
	DDX_Control(pDX, IDC_EDT_TRACK_MAX_TIME, m_edtMaxTrackTime);
	DDX_Control(pDX, IDC_EDT_TRACK_MAX_STATIC_TRACK, m_edtMaxStaticTime);
	DDX_Control(pDX, IDC_EDT_TRACK_MIN_SIZE, m_edtMaxSize);
	DDX_Control(pDX, IDC_EDT_TRACK_MAX_SIZE, m_edtMinSize);
	DDX_Control(pDX, IDC_CHK_TRACK_FIND_FACE_BACK, m_chkTrack[0]);
	DDX_Control(pDX, IDC_CHK_TRACK_DEFCOVER, m_chkTrack[1]);
	DDX_Control(pDX, IDC_CHK_TRACK_LIMINT_ENABLE, m_chkTrack[2]);
	DDX_Control(pDX, IDC_CHK_TRACK_SHOW_TARGET, m_chkTrack[3]);
	DDX_Control(pDX, IDC_CBO_TRACK_SCENE_ID, m_cboSenceId);
	DDX_Control(pDX, IDC_CBO_TRACK_LIMIT_STATUS, m_cboLimitStatus);
	DDX_Control(pDX, IDC_CHECK_CALLVISION, m_chkCallSimilarVision);
	DDX_Control(pDX, IDC_COMBO_TARGETTYPE, m_cboCheckTargetType);
}


BEGIN_MESSAGE_MAP(CLS_VCAEvnetsSmartTrack, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_SET_TRACK_LIMIT_STATUS, &CLS_VCAEvnetsSmartTrack::OnBnClickedBtnSetTrackLimitStatus)
	ON_BN_CLICKED(IDC_BTN_SET_TRACK_ARITHMATIC, &CLS_VCAEvnetsSmartTrack::OnBnClickedBtnSetTrackArithmatic)
	ON_CBN_SELCHANGE(IDC_CBO_TRACK_SCENE_ID, &CLS_VCAEvnetsSmartTrack::OnCbnSelchangeCboTrackSceneId)
END_MESSAGE_MAP()


BOOL CLS_VCAEvnetsSmartTrack::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	
	UI_InitDlgItemText();
	
	return TRUE;
}

void CLS_VCAEvnetsSmartTrack::OnLanguageChanged( int _iLanguage )
{
	UI_InitDlgItemText();
}

void CLS_VCAEvnetsSmartTrack::OnMainNotify( int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	switch(LOWORD(_wParam))
	{
	case WCM_VCA_SUSPEND:
		{
			int iChannelNo = HIWORD(_wParam);
			if (iChannelNo == m_iChannelNo)
			{
				VCASuspendResult tParam = {0};
				tParam.iBufSize = sizeof(VCASuspendResult);
				int iRetBytes = 0;
				int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNo, &tParam, sizeof(tParam), &iRetBytes);
				if(0 == tParam.iStatus && 2 == tParam.iResult)
				{
					MessageBox(GetTextEx(IDS_VCA__SUSPEND_READONLY), GetTextEx(IDS_CONFIG_PROMPT), MB_OK|MB_TOPMOST);
				}
				else if (0 == tParam.iStatus && 1 == tParam.iResult)//Paused successfully
				{
					AddLog(LOG_TYPE_SUCC, "", "[OnMainNotify]WCM_VCA_SUSPEND success!", m_iLogonID);
				}
			}
		}
		break;
	}
}

void CLS_VCAEvnetsSmartTrack::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
{
	m_iLogonID = _iLogonID;

	m_iChannelNo = (_iChannelNo > 0) ? _iChannelNo : 0;

	//int iStatus = 0;
	//int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNo, &iStatus, sizeof(int));
	//if (iRet < 0)
	//{
	//	AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig[NET_CLIENT_VCA_SUSPEND] failed logonID(%d)", m_iLogonID);
	//}
	//else
	//{
	//	AddLog(LOG_TYPE_SUCC, "", "NetClient_SetDevConfig [NET_CLIENT_VCA_SUSPEND]", m_iLogonID);
	//}
	
	UI_UpdateTrackLimitStatus();
	UI_UpdateTrackArithmatic();
}

void CLS_VCAEvnetsSmartTrack::UI_InitDlgItemText()
{
	SetDlgItemTextEx(IDC_GRP_TRACK_LIMIT_STATUS, IDS_TRACK_LIMIT);
	SetDlgItemTextEx(IDC_STC_TRACK_LIMIT_STATUS, IDS_TRACK_LIMIT);
	SetDlgItemTextEx(IDC_BTN_SET_TRACK_LIMIT_STATUS, IDS_CFG_USER_SET);
	SetDlgItemTextEx(IDC_BTN_SET_TRACK_ARITHMATIC, IDS_CFG_USER_SET);
	SetDlgItemTextEx(IDC_STC_TRACK_SCENE_ID, IDS_VCA_SCENE_ID);
	SetDlgItemTextEx(IDC_GRP_TRACK_ARITHMATIC, IDS_TRACK_ARITHMATIC);
	SetDlgItemTextEx(IDC_STC_TRACK_MULRIPE, IDS_TRACK_MULRIPE);
	SetDlgItemTextEx(IDC_STC_TRACK_MAX_TIME, IDS_TRACK_MAX_TIME);
	SetDlgItemTextEx(IDC_STC_TRACK_DOME_HEIGHT, IDS_TRACK_DOME_HEIGHT);
	SetDlgItemTextEx(IDC_STC_TRACK_MAX_STATIC_TRACK, IDS_MAX_STATIC_TRACK_TIME);
	SetDlgItemTextEx(IDC_STC_TRACK_MIN_SIZE, IDS_VCA_ABMINSIZE);
	SetDlgItemTextEx(IDC_STC_TRACK_MAX_SIZE, IDS_VCA_MAXSIZE);
	SetDlgItemTextEx(IDC_CHK_TRACK_FIND_FACE_BACK, IDS_TRACK_FIND_FACE_BACK);
	SetDlgItemTextEx(IDC_CHK_TRACK_DEFCOVER, IDS_TRACK_DEFCOVER);
	SetDlgItemTextEx(IDC_CHK_TRACK_LIMINT_ENABLE, IDS_TRACK_LIMINT_ENABLE);
	SetDlgItemTextEx(IDC_CHK_TRACK_SHOW_TARGET, IDS_TRACK_SHOW_TARGET);
	SetDlgItemText(IDC_CHECK_CALLVISION, GetTextByLan(_T("变倍到相似视角"),_T("Call Similar Vision")));
	SetDlgItemText(IDC_STATIC_TARGET_TYPE, GetTextByLan(_T("区分类型"),_T("Target Type")));

	//Scenes
	int iSelIndex = m_cboSenceId.GetCurSel();
	m_cboSenceId.ResetContent();
	for (int iSecne = 0; iSecne < MAX_SCENE_NUM; iSecne++)
	{
		m_cboSenceId.SetItemData(m_cboSenceId.AddString(IntToString(iSecne + 1)), iSecne);
	}
	m_cboSenceId.SetCurSel((iSelIndex < 0 || iSelIndex >= m_cboSenceId.GetCount()) ? 0 : iSelIndex);

	//limit type
	iSelIndex = m_cboLimitStatus.GetCurSel();
	m_cboLimitStatus.ResetContent();
	m_cboLimitStatus.SetItemData(m_cboLimitStatus.AddString(GetTextEx(IDS_UP_LIMIT)), 1);
	m_cboLimitStatus.SetItemData(m_cboLimitStatus.AddString(GetTextEx(IDS_DOWN_LIMIT)), 2);
	m_cboLimitStatus.SetItemData(m_cboLimitStatus.AddString(GetTextEx(IDS_LEFT_LIMIT)), 3);
	m_cboLimitStatus.SetItemData(m_cboLimitStatus.AddString(GetTextEx(IDS_RIGHT_LIMIT)), 4);
	m_cboLimitStatus.SetCurSel((iSelIndex < 0 || iSelIndex >= m_cboLimitStatus.GetCount()) ? 0 : iSelIndex);

	iSelIndex = m_cboCheckTargetType.GetCurSel();
	m_cboCheckTargetType.ResetContent();
	m_cboCheckTargetType.SetItemData(m_cboCheckTargetType.AddString(GetTextByLan(_T("不区分"),_T("None"))), 0);
	m_cboCheckTargetType.SetItemData(m_cboCheckTargetType.AddString(GetTextByLan(_T("人"),_T("Human"))), 1);
	m_cboCheckTargetType.SetItemData(m_cboCheckTargetType.AddString(GetTextByLan(_T("车"),_T("Car"))), 2);
	m_cboCheckTargetType.SetItemData(m_cboCheckTargetType.AddString(GetTextByLan(_T("人和车"),_T("Human & Car"))), 3);
	m_cboLimitStatus.SetCurSel((iSelIndex < 0 || iSelIndex >= m_cboCheckTargetType.GetCount()) ? 0 : iSelIndex);

}

void CLS_VCAEvnetsSmartTrack::OnCbnSelchangeCboTrackSceneId()
{
	UI_UpdateTrackLimitStatus();
	UI_UpdateTrackArithmatic();
}

void CLS_VCAEvnetsSmartTrack::OnBnClickedBtnSetTrackLimitStatus()
{
	TrackAstrictLocation tTrackLimit = {0};
	tTrackLimit.iBufSize = sizeof(tTrackLimit);
	tTrackLimit.iSceneID = m_cboSenceId.GetItemData(m_cboSenceId.GetCurSel());
	tTrackLimit.iType = m_cboLimitStatus.GetItemData(m_cboLimitStatus.GetCurSel());

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_TRACK_ASTRICT_LOCATION, m_iChannelNo, &tTrackLimit, sizeof(tTrackLimit));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","[SetTrackLimitStatus] SetDevConfig NET_CLIENT_TRACK_ASTRICT_LOCATION failed! (%d, %d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","[SetTrackLimitStatus] SetDevConfig NET_CLIENT_TRACK_ASTRICT_LOCATION success! (%d, %d)", m_iLogonID, m_iChannelNo);
	}

}

void CLS_VCAEvnetsSmartTrack::OnBnClickedBtnSetTrackArithmatic()
{
	// set tracking parameters
	TrackArithmetic tTrackArithmetic = {0};
	tTrackArithmetic.iBufSize = sizeof(TrackArithmetic);
	tTrackArithmetic.iZoomRate = GetDlgItemInt(IDC_EDT_TRACK_MULRIPE);
	tTrackArithmetic.iMaxFallowTime = GetDlgItemInt(IDC_EDT_TRACK_MAX_TIME);
	tTrackArithmetic.iHeight = GetDlgItemInt(IDC_EDT_TRACK_DOME_HEIGHT);
	tTrackArithmetic.iDesStopTime = GetDlgItemInt(IDC_EDT_TRACK_MAX_STATIC_TRACK);
	tTrackArithmetic.iMaxSize = GetDlgItemInt(IDC_EDT_TRACK_MAX_SIZE);
	tTrackArithmetic.iMinSize = GetDlgItemInt(IDC_EDT_TRACK_MIN_SIZE);
	tTrackArithmetic.iSceneID = m_cboSenceId.GetItemData(m_cboSenceId.GetCurSel());
	tTrackArithmetic.iCallSimilarVision = m_chkCallSimilarVision.GetCheck();
	tTrackArithmetic.iTargetTypeCheck = m_cboCheckTargetType.GetCurSel();

	for (int i = 0; i < MAX_TRACK_CHK_TYPE; i++)
	{
		tTrackArithmetic.iEnable |= m_chkTrack[i].GetCheck() << i;
	}
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_TRACK_ARITHMETIC, m_iChannelNo, &tTrackArithmetic, sizeof(tTrackArithmetic));
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","[SetTrackArithmatic] SetDevConfig NET_CLIENT_TRACK_ARITHMETIC failed! (%d, %d)", m_iLogonID, m_iChannelNo);
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","[SetTrackArithmatic] SetDevConfig NET_CLIENT_TRACK_ARITHMETIC success! (%d, %d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_VCAEvnetsSmartTrack::UI_UpdateTrackLimitStatus()
{
	TrackAstrictLocation tTrackLimit = {0};
	tTrackLimit.iBufSize = sizeof(tTrackLimit);
	tTrackLimit.iSceneID = m_cboSenceId.GetItemData(m_cboSenceId.GetCurSel());
	
	int iByteRet = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_TRACK_ASTRICT_LOCATION, m_iChannelNo, &tTrackLimit, sizeof(tTrackLimit), &iByteRet);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","[GetTrackLimitStatus] GetDevConfig NET_CLIENT_TRACK_ASTRICT_LOCATION failed! (%d, %d)", m_iLogonID, m_iChannelNo);
		goto EXIT_FUNC;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","[GetTrackLimitStatus] GetDevConfig NET_CLIENT_TRACK_ASTRICT_LOCATION success! (%d, %d)", m_iLogonID, m_iChannelNo);
	}

	m_cboLimitStatus.SetCurSel(GetCboSel(&m_cboLimitStatus, tTrackLimit.iType));

EXIT_FUNC:
	return;
}

void CLS_VCAEvnetsSmartTrack::UI_UpdateTrackArithmatic()
{
	// set tracking parameters
	TrackArithmetic tTrackArithmetic = {0};
	tTrackArithmetic.iBufSize = sizeof(TrackArithmetic);
	tTrackArithmetic.iSceneID = m_cboSenceId.GetItemData(m_cboSenceId.GetCurSel());

	int iByteRet = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_TRACK_ARITHMETIC, m_iChannelNo, &tTrackArithmetic, sizeof(tTrackArithmetic), &iByteRet);
	if (iRet < RET_SUCCESS)
	{
		AddLog(LOG_TYPE_FAIL,"","[GetTrackArithmatic] GetDevConfig NET_CLIENT_TRACK_ARITHMETIC failed! (%d, %d)", m_iLogonID, m_iChannelNo);
		goto EXIT_FUNC;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","[GetTrackArithmatic] GetDevConfig NET_CLIENT_TRACK_ARITHMETIC success! (%d, %d)", m_iLogonID, m_iChannelNo);
	}

	SetDlgItemInt(IDC_EDT_TRACK_MULRIPE, tTrackArithmetic.iZoomRate);
	SetDlgItemInt(IDC_EDT_TRACK_MAX_TIME, tTrackArithmetic.iMaxFallowTime);
	SetDlgItemInt(IDC_EDT_TRACK_DOME_HEIGHT, tTrackArithmetic.iHeight);
	SetDlgItemInt(IDC_EDT_TRACK_MAX_STATIC_TRACK, tTrackArithmetic.iDesStopTime);
	SetDlgItemInt(IDC_EDT_TRACK_MAX_SIZE, tTrackArithmetic.iMaxSize);
	SetDlgItemInt(IDC_EDT_TRACK_MIN_SIZE, tTrackArithmetic.iMinSize);

	for (int i = 0; i < MAX_TRACK_CHK_TYPE; i++)
	{
		int iChk = (0 == (tTrackArithmetic.iEnable & 1 << i)) ? BST_UNCHECKED : BST_CHECKED;
		m_chkTrack[i].SetCheck(iChk);
	}
	m_chkCallSimilarVision.SetCheck(tTrackArithmetic.iCallSimilarVision);
	m_cboCheckTargetType.SetCurSel(tTrackArithmetic.iTargetTypeCheck);

EXIT_FUNC:
	return;

}

void CLS_VCAEvnetsSmartTrack::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	SetVCAStatus(bShow);
}

void CLS_VCAEvnetsSmartTrack::SetVCAStatus(bool _bStatus)
{
	if(_bStatus)
	{
		if (-1 == m_iLogonID)
		{
			AddLog(LOG_TYPE_FAIL, "", "CLS_VCAEventPage::failed logonID(%d)", m_iLogonID);
			return;
		}

		int iStatus = 0;
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNo, &iStatus, sizeof(int));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig failed logonID(%d)", m_iLogonID);
		}
	}
	else
	{
		int iStatus = 1;
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNo, &iStatus, sizeof(int));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig failed logonID(%d)", m_iLogonID);
		}
	}
}

