
#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_QueueStand.h"

IMPLEMENT_DYNAMIC(CLS_QueueStand, CDialog)

CLS_QueueStand::CLS_QueueStand(CWnd* pParent /*=NULL*/)
: CLS_BasePage(CLS_QueueStand::IDD, pParent)
, m_iAlarmTime(0)
{
	m_iSensitivity = 50;
	m_iMinSize = 2;
	m_iMaxSize = 25;
	m_iAlarmTime = 30;
	memset(m_tValidRegion, 0, sizeof(ValidRgQueueStand) * MAX_REGION_COUNT_QUEUE_STAND);
}

CLS_QueueStand::~CLS_QueueStand()
{
	vector<vca_TPolygonEx> temp;
	temp.swap(m_vecMaskRegion);
}

BOOL CLS_QueueStand::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateUIText();
	for(int i = 0; i < LEN_8; ++i)
	{
		CString strNum;
		strNum.Format(_T("%d"), i + 1);
		m_cboValidRegNum.AddString(_T(strNum));
		m_cboRuleNum.AddString(strNum);
	}
	m_cboValidRegNum.SetCurSel(0);
	for(int i = 0; i < MAX_RULE_NUM_EX; ++i)
	{
		CString strNum;
		strNum.Format(_T("%d"), i);
		m_cboSceneNum.AddString(strNum);
	}
	m_cboModel.AddString(_T("nvr"));
	m_cboModel.AddString(_T("ipc"));

	m_cboRuleNum.SetCurSel(0);
	m_cboSceneNum.SetCurSel(0);
	m_cboModel.SetCurSel(0);

	//Rule ID is not supported, hide control
	m_cboRuleNum.ShowWindow(FALSE);
	CStatic *cStatic = (CStatic*)GetDlgItem(IDC_STATIC_RULENUM);
	cStatic->ShowWindow(FALSE);
	return TRUE;
}

void CLS_QueueStand::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iStreamNO = _iStreamNo;
	m_iChannelNO = _iChannelNo;
	GetQueueStandInfo();
}

void CLS_QueueStand::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_QueueStand::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_RULENUM, GetTextByLan(_T("规则号"), _T("RuleNum")));
	SetDlgItemText(IDC_STATIC_POLICE_SCENE, GetTextByLan(_T("场景"), _T("Scene")));
	SetDlgItemText(IDC_STATIC_MODEL, GetTextByLan(_T("模式"), _T("Model")));
	SetDlgItemText(IDC_STATIC_ENABLED, GetTextByLan(_T("使能"), _T("Enabled")));
	SetDlgItemText(IDC_STATIC_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
	SetDlgItemText(IDC_STATIC_DISPLAY_RULE, GetTextByLan(_T("显示报警规则"), _T("Displays the alert rule")));
	SetDlgItemText(IDC_STATIC_DISPLAY_STAT, GetTextByLan(_T("显示报警计数"), _T("Displays the alarm count")));
	SetDlgItemText(IDC_STATIC_DISPLAY_TARGET, GetTextByLan(_T("显示目标"), _T("Display the target")));
	SetDlgItemText(IDC_STATIC_MIN_SIZE, GetTextByLan(_T("最小尺寸"), _T("Min Size")));
	SetDlgItemText(IDC_STATIC_MAX_SIZE, GetTextByLan(_T("最大尺寸"), _T("Max Size")));
	SetDlgItemText(IDC_STATIC_ALARM_TIME, GetTextByLan(_T("最小报警间隔时间"), _T("MinAlarmTime")));
	SetDlgItemText(IDC_STATIC_VALIDLIST, GetTextByLan(_T("检测区域列表"), _T("Valid region list")));
	SetDlgItemText(IDC_STATIC_REGION_NUM, GetTextByLan(_T("检测区域编号"), _T("Detection area number")));
	SetDlgItemText(IDC_STATIC_REGION_ENABLED, GetTextByLan(_T("是否有效"), _T("Region Enabled")));
	SetDlgItemText(IDC_STATIC_VALID_COORD, GetTextByLan(_T("区域坐标"), _T("Region Coordinates")));
	SetDlgItemText(IDC_STATIC_MASKLIST, GetTextByLan(_T("屏蔽区域列表"), _T("Mask region list")));
	SetDlgItemText(IDC_STATIC_MASK_REGION_NUM, GetTextByLan(_T("屏蔽区域编号"), _T("Mask region num")));
	SetDlgItemText(IDC_STATIC_MASK_COORD, GetTextByLan(_T("区域坐标"), _T("Region Coordinates")));
	SetDlgItemText(IDC_BUTTON_DRAW_VALID, GetTextByLan(_T("绘制"), _T("Draw")));
	SetDlgItemText(IDC_BUTTON_DELETE_VALID, GetTextByLan(_T("删除"), _T("Delete")));
	SetDlgItemText(IDC_BUTTON_DRAW_NEW, GetTextByLan(_T("绘制新区域"), _T("DrawNew")));
	SetDlgItemText(IDC_BUTTON_DRAW_MASK, GetTextByLan(_T("绘制"), _T("Draw")));
	SetDlgItemText(IDC_BUTTON_DELETE_MASK, GetTextByLan(_T("删除"), _T("Delete")));
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_QueueStand::GetQueueStandInfo()
{
	XmlQueueStand tInfo;
	memset(&tInfo, 0, sizeof(XmlQueueStand));
	//tInfo.iRuleId = m_cboRuleNum.GetCurSel() + 1;
	//Rule id is not supported, write dead 1
	tInfo.iRuleId = 1;
	tInfo.iChannelNo = m_iChannelNO;
	tInfo.iSceneId = m_cboSceneNum.GetCurSel();
	tInfo.iModel = m_cboModel.GetCurSel();
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_QUEUESTAND, &tInfo, sizeof(XmlQueueStand), &tInfo, sizeof(XmlQueueStand));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_QueueStand::NetClient_XmlGetDevConfig[NETXMLCFG_QUEUESTAND] (%d)", m_iLogonID);
		m_chkEnabled.SetCheck(tInfo.iEnabled);
		m_chkDisplayRule.SetCheck(tInfo.iDisPlayRule);
		m_chkDisplayStat.SetCheck(tInfo.iDisPlayStat);
		m_chkDisplayTarget.SetCheck(tInfo.iDisPlayTarget);
		m_iSensitivity = tInfo.iSensitivity;
		m_iMinSize = tInfo.iMinSize;
		m_iMaxSize = tInfo.iMaxSize;
		m_iAlarmTime = tInfo.iAlarmTime;
		UpdateData(FALSE);
		memset(m_tValidRegion, 0, sizeof(ValidRgQueueStand) * MAX_REGION_COUNT_QUEUE_STAND);
		for(int i = 0; i < tInfo.iValidRegionCount && i < MAX_REGION_COUNT_QUEUE_STAND; ++i)
		{
			int iCur = tInfo.tValidRegion[i].iRegionNo - 1;
			memcpy(&m_tValidRegion[iCur], &tInfo.tValidRegion[i], sizeof(ValidRgQueueStand));
		}
		m_cboValidRegNum.SetCurSel(0);
		OnCbnSelchangeComboRegionNum();
		m_cboMaskRegNum.ResetContent();
		m_vecMaskRegion.clear();
		for(int i = 0; i < tInfo.iMaskRegionCount && i < MAX_REGION_COUNT_QUEUE_STAND; ++i)
		{
			CString strNum;
			strNum.Format(_T("%d"), i + 1);
			m_cboMaskRegNum.AddString(strNum);
			m_vecMaskRegion.push_back(tInfo.tMaskRegion[i]);
		}
		m_cboMaskRegNum.SetCurSel(0);
		OnCbnSelchangeComboMaskRegionNum();
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_QueueStand::NetClient_XmlGetDevConfig[NETXMLCFG_QUEUESTAND] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_QueueStand::ShowValidList(int _iIndex)
{
	if(_iIndex < 0 || _iIndex >= MAX_REGION_COUNT_QUEUE_STAND)
	{
		SetDlgItemText(IDC_EDIT_VALID_COORD, _T(""));
		return;
	}
	m_chkRegionEnabled.SetCheck(m_tValidRegion[_iIndex].iRegionEnabled);
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	char cSplitSection[]  = "(";
	char cSplitSection1[]  = ", ";
	char cSplitSection2[]  = ")";
	int iApdLen = (int)sizeof(int);
	int iSplitLen = (int)strlen(cSplitSection);
	int iSplitLen1 = (int)strlen(cSplitSection1);
	int iSplitLen2 = (int)strlen(cSplitSection2);
	for (int i = 0; i < m_tValidRegion[_iIndex].tRegionCoord.iPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX; ++i)
	{
		vca_TPolygonEx &tCoord = m_tValidRegion[_iIndex].tRegionCoord;
		char* pcOffset = NULL;
		int iSrcLen = (int)strlen(cPointBuf);
		if ((iSrcLen + 2 * iApdLen + iSplitLen + iSplitLen1 + iSplitLen2) > MAX_POINTBUF_LEN)
		{
			AddLog(LOG_TYPE_FAIL, "", "The string of point coordinate set exceeds the maximum length!");
			return;
		}
		pcOffset = cPointBuf + iSrcLen;
		sprintf(pcOffset, "%s%d%s%d%s", cSplitSection, tCoord.stPoints[i].iX, 
			cSplitSection1, tCoord.stPoints[i].iY, cSplitSection2);
	}
	SetDlgItemText(IDC_EDIT_VALID_COORD, _T(cPointBuf));
}

void CLS_QueueStand::ShowMaskList(int _iIndex)
{
	if(_iIndex < 0 || _iIndex >= (int)m_vecMaskRegion.size())
	{
		SetDlgItemText(IDC_EDIT_MASK_COORD, _T(""));
		return;
	}
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	char cSplitSection[]  = "(";
	char cSplitSection1[]  = ", ";
	char cSplitSection2[]  = ")";
	int iApdLen = (int)sizeof(int);
	int iSplitLen = (int)strlen(cSplitSection);
	int iSplitLen1 = (int)strlen(cSplitSection1);
	int iSplitLen2 = (int)strlen(cSplitSection2);

	for (int i = 0; i < m_vecMaskRegion[_iIndex].iPointNum && i < MAX_REGION_COUNT_QUEUE_STAND; ++i)
	{
		vca_TPolygonEx &tCoord = m_vecMaskRegion[_iIndex];
		char* pcOffset = NULL;
		int iSrcLen = (int)strlen(cPointBuf);
		if ((iSrcLen + 2*iApdLen + iSplitLen + iSplitLen1 + iSplitLen2) > MAX_POINTBUF_LEN)
		{
			AddLog(LOG_TYPE_FAIL, "", "The string of point coordinate set exceeds the maximum length!");
			return;
		}
		pcOffset = cPointBuf + iSrcLen;
		sprintf(pcOffset, "%s%d%s%d%s", cSplitSection, tCoord.stPoints[i].iX, 
			cSplitSection1, tCoord.stPoints[i].iY, cSplitSection2);
	}
	SetDlgItemText(IDC_EDIT_MASK_COORD, _T(cPointBuf));
}

BOOL CLS_QueueStand::SetVCAStatus(bool _bStatus)
{
	int iProType = 0;
	int iProMode = 0;
	NetClient_GetProductTypeEx(m_iLogonID, &iProMode, &iProType);

	VCASuspend tInfo = {0};

	// iProType : output product type: 0--reserverd, 1--ipc, 2--nvr
	if(2 == iProType)
	{
		// iDevType : 0-IPC, 1-NVR
		tInfo.iDevType = 1;
	}
	if(!_bStatus)			//Parameter FALSE means pause, TRUE means open
	{
		if (-1 == m_iLogonID)
		{
			AddLog(LOG_TYPE_FAIL, "", "CLS_VCAEventPage::failed logonID(%d)", m_iLogonID);
			return FALSE;
		}

		tInfo.iStatus = 0;	//0 means pause
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNO, SYNC_NET_CLIENT_VCA_SUSPEND, &tInfo, sizeof(VCASuspend), NULL, 0);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SyncSetDevCfg stop VCA failed logonID(%d)", m_iLogonID);
			return FALSE;
		}
	}
	else
	{
		tInfo.iStatus = 1;
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNO, SYNC_NET_CLIENT_VCA_SUSPEND, &tInfo, sizeof(VCASuspend), NULL, 0);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SyncSetDevCfg start VCA failed logonID(%d)", m_iLogonID);
			return FALSE;
		}
	}
	return TRUE;
}

void CLS_QueueStand::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_REGION_NUM, m_cboValidRegNum);
	DDX_Control(pDX, IDC_COMBO_MASK_REGION_NUM, m_cboMaskRegNum);
	DDX_Control(pDX, IDC_COMBO_RULENUM, m_cboRuleNum);
	DDX_Control(pDX, IDC_COMBO_POLICE_SCENE, m_cboSceneNum);
	DDX_Control(pDX, IDC_COMBO_MODEL, m_cboModel);

	DDX_Text(pDX, IDC_EDIT_SENSITIVITY, m_iSensitivity);
	DDV_MinMaxInt(pDX, m_iSensitivity, 0, 100);
	DDX_Text(pDX, IDC_EDIT_MIN_SIZE, m_iMinSize);
	DDV_MinMaxInt(pDX, m_iMinSize, 1, 50);
	DDX_Text(pDX, IDC_EDIT_MAX_SIZE, m_iMaxSize);
	DDV_MinMaxInt(pDX, m_iMaxSize, 8, 100);

	DDX_Control(pDX, IDC_CHECK_ENABLED, m_chkEnabled);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_RULE, m_chkDisplayRule);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_STAT, m_chkDisplayStat);
	DDX_Control(pDX, IDC_CHECK_DISPLAY_TARGET, m_chkDisplayTarget);
	DDX_Control(pDX, IDC_CHECK_REGION_ENABLED, m_chkRegionEnabled);
	DDX_Text(pDX, IDC_EDIT_ALARM_TIME, m_iAlarmTime);
	DDV_MinMaxInt(pDX, m_iAlarmTime, 0, 7200);
}

BEGIN_MESSAGE_MAP(CLS_QueueStand, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_DRAW_MASK, &CLS_QueueStand::OnBnClickedButtonDrawMask)
	ON_BN_CLICKED(IDC_BUTTON_DRAW_NEW, &CLS_QueueStand::OnBnClickedButtonDrawNew)
	ON_BN_CLICKED(IDC_BUTTON_DELETE_MASK, &CLS_QueueStand::OnBnClickedButtonDeleteMask)
	ON_BN_CLICKED(IDC_BUTTON_DRAW_VALID, &CLS_QueueStand::OnBnClickedButtonDrawValid)
	ON_BN_CLICKED(IDC_BUTTON_DELETE_VALID, &CLS_QueueStand::OnBnClickedButtonDeleteValid)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_QueueStand::OnBnClickedButtonSet)
	ON_CBN_SELCHANGE(IDC_COMBO_REGION_NUM, &CLS_QueueStand::OnCbnSelchangeComboRegionNum)
	ON_CBN_SELCHANGE(IDC_COMBO_MASK_REGION_NUM, &CLS_QueueStand::OnCbnSelchangeComboMaskRegionNum)
	ON_BN_CLICKED(IDC_CHECK_REGION_ENABLED, &CLS_QueueStand::OnBnClickedCheckRegionEnabled)
	ON_CBN_SELCHANGE(IDC_COMBO_RULENUM, &CLS_QueueStand::OnCbnSelchangeComboRulenum)
	ON_CBN_SELCHANGE(IDC_COMBO_POLICE_SCENE, &CLS_QueueStand::OnCbnSelchangeComboPoliceScene)
	ON_CBN_SELCHANGE(IDC_COMBO_MODEL, &CLS_QueueStand::OnCbnSelchangeComboModel)
END_MESSAGE_MAP()

void CLS_QueueStand::OnBnClickedButtonDrawMask()
{
	// Draw the shielded area that already exists
	if(m_cboMaskRegNum.GetCurSel() < 0 || m_cboMaskRegNum.GetCurSel() >= (int)m_vecMaskRegion.size())
	{
		return;
	}
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, VCA_MAX_POLYGON_POINT_NUMEX);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 2)
		{
			SetDlgItemText(IDC_EDIT_MASK_COORD, cPointBuf);
		}
		else
		{
			SetDlgItemText(IDC_EDIT_MASK_COORD, _T(""));
			delete m_pDlgVideoView;
			m_pDlgVideoView = NULL;
			return;
		}

		int iCurRegion = m_cboMaskRegNum.GetCurSel();
		vca_TPoint tPolygon[VCA_MAX_POLYGON_POINT_NUMEX];
		memset(tPolygon, 0, sizeof(vca_TPoint) * VCA_MAX_POLYGON_POINT_NUMEX);
		CString cstPolygon = cPointBuf;
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)tPolygon);
		vca_TPolygonEx &tInfo = m_vecMaskRegion[iCurRegion];
		memset(&tInfo, 0, sizeof(vca_TPolygonEx));

		tInfo.iPointNum = iPointNum;
		for (int i = 0; i < iPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX ; ++i)
		{
			tInfo.stPoints[i].iX = tPolygon[i].iX;
			tInfo.stPoints[i].iY = tPolygon[i].iY;
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_QueueStand::OnBnClickedButtonDrawNew()
{
	// Draw a new shielded area
	if(m_vecMaskRegion.size() >= MAX_REGION_COUNT_QUEUE_STAND)
	{
		return;
	}
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, VCA_MAX_POLYGON_POINT_NUMEX);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 2)
		{
			SetDlgItemText(IDC_EDIT_MASK_COORD, cPointBuf);
		}
		else
		{
			SetDlgItemText(IDC_EDIT_MASK_COORD, _T(""));
			delete m_pDlgVideoView;
			m_pDlgVideoView = NULL;
			return;
		}

		vca_TPoint tPolygon[VCA_MAX_POLYGON_POINT_NUMEX];
		memset(tPolygon, 0, sizeof(vca_TPoint) * VCA_MAX_POLYGON_POINT_NUMEX);
		CString cstPolygon = cPointBuf;
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)tPolygon);
		vca_TPolygonEx tInfo;
		memset(&tInfo, 0, sizeof(vca_TPolygonEx));

		tInfo.iPointNum = iPointNum;
		for (int i = 0; i < iPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX ; ++i)
		{
			tInfo.stPoints[i].iX = tPolygon[i].iX;
			tInfo.stPoints[i].iY = tPolygon[i].iY;
		}
		m_vecMaskRegion.push_back(tInfo);
		CString strNum;
		strNum.Format(_T("%d"), m_vecMaskRegion.size());
		m_cboMaskRegNum.AddString(strNum);
		m_cboMaskRegNum.SetCurSel(m_vecMaskRegion.size() - 1);
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_QueueStand::OnBnClickedButtonDeleteMask()
{
	// Remove a shielded area
	int iIndex = m_cboMaskRegNum.GetCurSel();
	if(iIndex < 0)
	{
		m_vecMaskRegion.clear();
		return;
	}
	if(iIndex >= (int)m_vecMaskRegion.size())
	{
		return;
	}
	int iCur = 0;
	for(vector<vca_TPolygonEx>::iterator it = m_vecMaskRegion.begin(); it != m_vecMaskRegion.end(); it++)
	{
		if(iCur++ == iIndex)
		{
			m_vecMaskRegion.erase(it);
			break;
		}
	}
	m_cboMaskRegNum.DeleteString(m_vecMaskRegion.size());
	m_cboMaskRegNum.SetCurSel(0);
	OnCbnSelchangeComboMaskRegionNum();
}

void CLS_QueueStand::OnBnClickedButtonDrawValid()
{
	// Draw the detection area
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, VCA_MAX_POLYGON_POINT_NUMEX);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 2)
		{
			SetDlgItemText(IDC_EDIT_VALID_COORD, cPointBuf);
		}
		else
		{
			SetDlgItemText(IDC_EDIT_VALID_COORD, _T(""));
			delete m_pDlgVideoView;
			m_pDlgVideoView = NULL;
			return;
		}

		int iCurRegion = m_cboValidRegNum.GetCurSel();
		if(iCurRegion < 0 || iCurRegion >= MAX_REGION_COUNT_QUEUE_STAND)
		{
			delete m_pDlgVideoView;
			m_pDlgVideoView = NULL;
			return;
		}
		vca_TPoint tPolygon[VCA_MAX_POLYGON_POINT_NUMEX];
		memset(tPolygon, 0, sizeof(vca_TPoint) * VCA_MAX_POLYGON_POINT_NUMEX);
		CString cstPolygon = cPointBuf;
		GetPointsFromString(cstPolygon, iPointNum, (POINT*)tPolygon);
		ValidRgQueueStand &tInfo = m_tValidRegion[iCurRegion];
		int iEnable = tInfo.iRegionEnabled;
		memset(&tInfo, 0, sizeof(ValidRgQueueStand));

		tInfo.iRegionEnabled = iEnable;
		tInfo.iRegionNo = iCurRegion + 1;
		tInfo.tRegionCoord.iPointNum = iPointNum;
		for (int i = 0; i < iPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX ; ++i)
		{
			tInfo.tRegionCoord.stPoints[i].iX = tPolygon[i].iX;
			tInfo.tRegionCoord.stPoints[i].iY = tPolygon[i].iY;
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_QueueStand::OnBnClickedButtonDeleteValid()
{
	// Delete a detection area
	int iIndex = m_cboValidRegNum.GetCurSel();
	if(iIndex < 0 || iIndex >= MAX_REGION_COUNT_QUEUE_STAND)
	{
		return;
	}
	memset(&m_tValidRegion[iIndex], 0, sizeof(ValidRgQueueStand));
	OnCbnSelchangeComboRegionNum();
}

void CLS_QueueStand::OnBnClickedButtonSet()
{
	// Set parameters
	UpdateData(TRUE);
	XmlQueueStand tInfo;
	memset(&tInfo, 0, sizeof(XmlQueueStand));
	//tInfo.iRuleId = m_cboRuleNum.GetCurSel() + 1;
	//Rule id is not supported, write dead 1
	tInfo.iRuleId = 1;
	tInfo.iChannelNo = m_iChannelNO;
	tInfo.iSceneId = m_cboSceneNum.GetCurSel();
	tInfo.iModel = m_cboModel.GetCurSel();
	tInfo.iEnabled = m_chkEnabled.GetCheck();
	tInfo.iSensitivity = GetDlgItemInt(IDC_EDIT_SENSITIVITY);
	tInfo.iDisPlayRule = m_chkDisplayRule.GetCheck();
	tInfo.iDisPlayStat = m_chkDisplayStat.GetCheck();
	tInfo.iDisPlayTarget = m_chkDisplayTarget.GetCheck();
	tInfo.iMinSize = m_iMinSize;
	tInfo.iMaxSize = m_iMaxSize;
	tInfo.iAlarmTime = m_iAlarmTime;
	int iIndex = 0;
	for(int i = 0; i < MAX_REGION_COUNT_QUEUE_STAND; ++i)
	{
		if(0 == m_tValidRegion[i].tRegionCoord.iPointNum)
		{
			continue;
		}
		memcpy(&tInfo.tValidRegion[iIndex++], &m_tValidRegion[i], sizeof(ValidRgQueueStand));
	}
	tInfo.iValidRegionCount = iIndex;
	tInfo.iMaskRegionCount = m_vecMaskRegion.size();
	for(int i = 0; i < MAX_REGION_COUNT_QUEUE_STAND && i < (int)m_vecMaskRegion.size(); ++i)
	{
		memcpy(&tInfo.tMaskRegion[i], &m_vecMaskRegion[i], sizeof(vca_TPolygonEx));
	}
	int iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_QUEUESTAND, &tInfo, sizeof(XmlQueueStand), NULL, 0);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "","CLS_QueueStand::NetClient_XmlSetDevConfig[NETXMLCFG_QUEUESTAND] (%d)", m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_QueueStand::NetClient_XmlSetDevConfig[NETXMLCFG_QUEUESTAND] (%d), error(%d)", m_iLogonID, GetLastError());
	}
}


void CLS_QueueStand::OnCbnSelchangeComboRegionNum()
{
	ShowValidList(m_cboValidRegNum.GetCurSel());
}

void CLS_QueueStand::OnCbnSelchangeComboMaskRegionNum()
{
	ShowMaskList(m_cboMaskRegNum.GetCurSel());
}

void CLS_QueueStand::OnBnClickedCheckRegionEnabled()
{
	// Whether the region is valid
	int iIndex = m_cboValidRegNum.GetCurSel();
	if(TRUE == m_chkRegionEnabled.GetCheck())
	{
		m_tValidRegion[iIndex].iRegionEnabled = 1;
	}
	else if(FALSE == m_chkRegionEnabled.GetCheck())
	{
		m_tValidRegion[iIndex].iRegionEnabled = 0;
	}
}

void CLS_QueueStand::OnCbnSelchangeComboRulenum()
{
	// Toggle the rule number
	GetQueueStandInfo();
}

void CLS_QueueStand::OnCbnSelchangeComboPoliceScene()
{
	// Toggle the scene number
	GetQueueStandInfo();
}

void CLS_QueueStand::OnCbnSelchangeComboModel()
{
	// Switch modes
	GetQueueStandInfo();
}

void CLS_QueueStand::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	SetVCAStatus(bShow ? 0 : 1);
}
