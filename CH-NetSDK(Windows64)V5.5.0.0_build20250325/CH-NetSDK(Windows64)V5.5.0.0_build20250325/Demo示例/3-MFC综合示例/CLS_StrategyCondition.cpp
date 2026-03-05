// CLS_StrategyCondition.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_StrategyCondition.h"

// CLS_StrategyCondition 对话框

IMPLEMENT_DYNAMIC(CLS_StrategyCondition, CDialog)

CLS_StrategyCondition::CLS_StrategyCondition(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_StrategyCondition::IDD, pParent)
{
	m_iPointNum = -1;
}

CLS_StrategyCondition::~CLS_StrategyCondition()
{
}

void CLS_StrategyCondition::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_CONDITION_NAME, m_cboConditionName);
	DDX_Control(pDX, IDC_EDIT_CONDITION_POINT, m_edtArea);
	DDX_Control(pDX, IDC_COMBO_CONDITION_CHECK, m_cboEventType);
	DDX_Control(pDX, IDC_SLIDER_CONDITION_SENSITIVITY, m_sldSensitivity);
	DDX_Control(pDX, IDC_EDIT_CONDITION_TIME, m_editTime);
	DDX_Control(pDX, IDC_CHECK_CONDITION_RED, m_chkColor[0]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_YELLOW, m_chkColor[1]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_BLUE, m_chkColor[2]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_GREEN, m_chkColor[3]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_BLACK, m_chkColor[4]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_WHITE, m_chkColor[5]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_PURPLE, m_chkColor[6]);
	DDX_Control(pDX, IDC_CHECK_CONDITION_GRAY, m_chkColor[7]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE1, m_chkCondition[0]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE2, m_chkCondition[1]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE3, m_chkCondition[2]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE4, m_chkCondition[3]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE5, m_chkCondition[4]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE6, m_chkCondition[5]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE7, m_chkCondition[6]);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE8, m_chkCondition[7]);
	DDX_Control(pDX, IDC_CBO_CONDITION_RULENO, m_cboStrategyNo);
	DDX_Control(pDX, IDC_CHK_CONDITION_RULE_ENABLE, m_cboRuleEnable);
}


BEGIN_MESSAGE_MAP(CLS_StrategyCondition, CDialog)
	ON_BN_CLICKED(IDC_BTN_CONDITION_POINT, &CLS_StrategyCondition::OnBnClickedBtnConditionPoint)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_CONDITION_SENSITIVITY, &CLS_StrategyCondition::OnNMCustomdrawSliderConditionSensitivity)
	ON_CBN_SELCHANGE(IDC_COMBO_CONDITION_NAME, &CLS_StrategyCondition::OnCbnSelchangeComboConditionName)
	ON_BN_CLICKED(IDC_BTN_CONDITION_SET, &CLS_StrategyCondition::OnBnClickedBtnConditionSet)
	ON_BN_CLICKED(IDC_BTN_CONDITION_GET_COLOR, &CLS_StrategyCondition::OnBnClickedBtnConditionGetColor)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_CBO_CONDITION_RULENO, &CLS_StrategyCondition::OnCbnSelchangeCboConditionRuleno)
	ON_BN_CLICKED(IDC_BTN_CONDITION_RULE_SET, &CLS_StrategyCondition::OnBnClickedBtnConditionRuleSet)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE1, &CLS_StrategyCondition::OnBnClickedChkConditionRule1)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE2, &CLS_StrategyCondition::OnBnClickedChkConditionRule2)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE3, &CLS_StrategyCondition::OnBnClickedChkConditionRule3)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE4, &CLS_StrategyCondition::OnBnClickedChkConditionRule4)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE5, &CLS_StrategyCondition::OnBnClickedChkConditionRule5)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE6, &CLS_StrategyCondition::OnBnClickedChkConditionRule6)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE7, &CLS_StrategyCondition::OnBnClickedChkConditionRule7)
	ON_BN_CLICKED(IDC_CHK_CONDITION_RULE8, &CLS_StrategyCondition::OnBnClickedChkConditionRule8)
END_MESSAGE_MAP()


// CLS_StrategyCondition 消息处理程序

BOOL CLS_StrategyCondition::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UI_UpdateUIText();
	UI_UpdateCondition();
	UI_UpdateStrategy();

	return TRUE;
}

void CLS_StrategyCondition::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNO = _iChannelNo;
	m_iStreamNO = _iStreamNo;

	UI_UpdateCondition();
	UI_UpdateStrategy();
}

void CLS_StrategyCondition::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_StrategyCondition::UI_UpdateUIText()
{
	m_cboConditionName.ResetContent();
	m_cboConditionName.AddString(GetTextByLan(_T("条件一人员"), _T("Condition1 personnel")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件二人员"), _T("Condition2 personnel")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件三车辆"), _T("Condition3 vehicle")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件四车辆"), _T("Condition4 vehicle")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件五颜色"), _T("Condition5 color")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件六颜色"), _T("Condition6 color")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件七声音"), _T("Condition7 sounds")));
	m_cboConditionName.AddString(GetTextByLan(_T("条件八声音"), _T("Condition8 sounds")));

	SetDlgItemText(IDC_STC_CONDITION_NAME, GetTextByLan(_T("条件"), _T("License plate library")));
	SetDlgItemText(IDC_STC_CONDITION_POINT, GetTextByLan(_T("区域坐标"), _T("License plate number")));
	SetDlgItemText(IDC_STC_CONDITION_COLOR, GetTextByLan(_T("颜色"), _T("Add")));
	SetDlgItemText(IDC_STC_CONDITION_CHECK, GetTextByLan(_T("检测事件"), _T("Search")));
	SetDlgItemText(IDC_STC_CONDITION_TIME, GetTextByLan(_T("滞留时间（s）"), _T("Edit")));
	SetDlgItemText(IDC_STC_CONDITION_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Delete")));

	SetDlgItemText(IDC_CHECK_CONDITION_RED, GetTextByLan(_T("红"), _T("Red")));
	SetDlgItemText(IDC_CHECK_CONDITION_YELLOW, GetTextByLan(_T("黄"), _T("Yellow")));
	SetDlgItemText(IDC_CHECK_CONDITION_BLUE, GetTextByLan(_T("蓝"), _T("Blue")));
	SetDlgItemText(IDC_CHECK_CONDITION_GREEN, GetTextByLan(_T("绿"), _T("Green")));
	SetDlgItemText(IDC_CHECK_CONDITION_BLACK, GetTextByLan(_T("黑"), _T("Black")));
	SetDlgItemText(IDC_CHECK_CONDITION_WHITE, GetTextByLan(_T("白"), _T("White")));
	SetDlgItemText(IDC_CHECK_CONDITION_PURPLE, GetTextByLan(_T("紫"), _T("Purple")));
	SetDlgItemText(IDC_CHECK_CONDITION_GRAY, GetTextByLan(_T("灰"), _T("Gray")));

	SetDlgItemText(IDC_BTN_CONDITION_POINT, GetTextByLan(_T("绘制"), _T("Draw")));
	SetDlgItemText(IDC_BTN_CONDITION_GET_COLOR, GetTextByLan(_T("画面取色"), _T("Color extraction")));
	SetDlgItemTextEx(IDC_BTN_CONDITION_SET, IDS_BTN_STORAGE_ANR_SET);
	SetDlgItemTextEx(IDC_BTN_CONDITION_RULE_SET, IDS_BTN_STORAGE_ANR_SET);

	m_cboEventType.ResetContent();
	m_cboEventType.AddString(GetTextByLan(_T("进入"), _T("In")));
	m_cboEventType.AddString(GetTextByLan(_T("离开"), _T("Out")));
	m_cboEventType.AddString(GetTextByLan(_T("滞留"), _T("stay")));

	m_sldSensitivity.SetRange(0, 100);
	m_sldSensitivity.SetPos(0);
	SetDlgItemInt(IDC_STC_CONDITION_SENSITIVITY_NUM, m_sldSensitivity.GetPos());
	
	m_editTime.SetLimitText(4);

	SetDlgItemText(IDC_GPB_STRATEGY_CONDITION, GetTextByLan(_T("条件设置"), _T("Strategy Condition")));
	SetDlgItemText(IDC_GPB_STRATEGY_INPUT, GetTextByLan(_T("智能场景"), _T("Strategy Input")));

	SetDlgItemText(IDC_STC_CONDITION_RULENO, GetTextByLan(_T("策略编号"), _T("Rule No")));
	SetDlgItemText(IDC_STC_CONDITION_RULE_NAME, GetTextByLan(_T("策略名称"), _T("Rule Name")));
	SetDlgItemText(IDC_STC_CONDITION_RULE_SET, GetTextByLan(_T("条件设置"), _T("Rule Set")));

	SetDlgItemText(IDC_CHK_CONDITION_RULE1, GetTextByLan(_T("条件一人员"), _T("Condition1 personnel")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE2, GetTextByLan(_T("条件二人员"), _T("Condition2 personnel")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE3, GetTextByLan(_T("条件三车辆"), _T("Condition3 vehicle")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE4, GetTextByLan(_T("条件四车辆"), _T("Condition4 vehicle")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE5, GetTextByLan(_T("条件五颜色"), _T("Condition5 color")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE6, GetTextByLan(_T("条件六颜色"), _T("Condition6 color")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE7, GetTextByLan(_T("条件七声音"), _T("Condition7 sounds")));
	SetDlgItemText(IDC_CHK_CONDITION_RULE8, GetTextByLan(_T("条件八声音"), _T("Condition8 sounds")));

	m_cboStrategyNo.ResetContent();
	for (int i=0; i<MAX_CONDITION_NUM ; i++)
	{
		m_cboStrategyNo.AddString(IntToCString(i+1));
	}

}

void CLS_StrategyCondition::UI_UpdateCondition()
{
	int iSelect = m_cboConditionName.GetCurSel();
	if (iSelect<0 || iSelect>=MAX_STRATEGY_NUM)
	{
		return;
	}
	
	XmlStrategyCondition tInfo = {0};

	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_STRATEGY_CONDITION, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if(RET_SUCCESS == iRet)
	{
		CString cstPolygonBuf = "";
		int iPointNum = tInfo.tRule[iSelect].iRegionNum;
		for(int i = 0; i<iPointNum && i<MAX_STRATEGY_REGION_POINT; i++)
		{
			cstPolygonBuf.AppendFormat("(%d, %d)", tInfo.tRule[iSelect].tRegionPoint[i].iX, tInfo.tRule[iSelect].tRegionPoint[i].iY);
		}
		SetDlgItemText(IDC_EDIT_CONDITION_POINT, cstPolygonBuf);
		m_iPointNum = iPointNum;

		m_cboEventType.SetCurSel(tInfo.tRule[iSelect].iEvent);

		SetDlgItemInt(IDC_EDIT_CONDITION_TIME,tInfo.tRule[iSelect].iTime);

		m_sldSensitivity.SetPos(tInfo.tRule[iSelect].iSensitivity);
		SetDlgItemInt(IDC_STC_CONDITION_SENSITIVITY_NUM, m_sldSensitivity.GetPos());

		OsStrArray strArray;
		os_split_str_to_arr(tInfo.tRule[iSelect].cColor, ',', &strArray);
		
		for (int j=0;j<CONDITION_COLOUR_NUM_MAX;j++)
		{
			m_chkColor[j].SetCheck(BST_UNCHECKED);
		}
		
		for(int i=0; i<strArray.iCount&&i<OS_MAX_ARRAY_COUNT; i++)
		{
			CString cstrTemp;
			cstrTemp.Format("%s",strArray.pcStr[i]);
			
			for (int j=0;j<CONDITION_COLOUR_NUM_MAX;j++)
			{
				if (cstrTemp == m_cColor[j])
				{
					m_chkColor[j].SetCheck(BST_CHECKED);
					break;
				}
			}
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlGetDevConfig[NETXMLCFG_STRATEGY_CONDITION] (%d)",m_iLogonID);
	}
}

void CLS_StrategyCondition::OnBnClickedBtnConditionPoint()
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
	m_iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &m_iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (m_iPointNum > 1)
		{
			m_edtArea.SetWindowText(cPointBuf);
		}
		else
		{
			m_edtArea.SetWindowText(_T(""));
		}
	}
	else
	{
		//Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_StrategyCondition::OnNMCustomdrawSliderConditionSensitivity(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_CONDITION_SENSITIVITY_NUM, m_sldSensitivity.GetPos());
	*pResult = 0;
}

void CLS_StrategyCondition::OnCbnSelchangeComboConditionName()
{
	UI_UpdateCondition();
}

void CLS_StrategyCondition::OnBnClickedBtnConditionSet()
{
	int iSelect = m_cboConditionName.GetCurSel();
	if (iSelect<0 || iSelect>=MAX_STRATEGY_NUM)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnConditionSet iSelect out of range(%d)",iSelect);
		return;
	}

	XmlStrategyCondition tInfo = {0};
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_STRATEGY_CONDITION, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if(RET_SUCCESS == iRet)
	{
		XmlResponseStatus tResponse = {0};

		POINT ptPolygon[MAX_STRATEGY_REGION_POINT] = {0} ;
		CString cstPolygon = "";
		GetDlgItemText(IDC_EDIT_CONDITION_POINT, cstPolygon);
		GetPointsFromString(cstPolygon, m_iPointNum, ptPolygon);
		tInfo.tRule[iSelect].iRegionNum = m_iPointNum;
		
		for (int i = 0; i < m_iPointNum && i <MAX_STRATEGY_REGION_POINT; i++)
		{
			tInfo.tRule[iSelect].tRegionPoint[i].iX = ptPolygon[i].x;
			tInfo.tRule[iSelect].tRegionPoint[i].iY = ptPolygon[i].y;
		}

		CString cstrColor;
		for (int j=0;j < CONDITION_COLOUR_NUM_MAX; j++)
		{
			if (BST_CHECKED == m_chkColor[j].GetCheck())
			{
				if (0 == j)
				{
					cstrColor.AppendFormat("%s",m_cColor[j]);
				}
				else
				{
					cstrColor.AppendFormat(",%s",m_cColor[j]);
				}
			}
		}
		strncpy_s(tInfo.tRule[iSelect].cColor, (LPSTR)(LPCTSTR)cstrColor, min(sizeof(tInfo.tRule[iSelect].cColor), cstrColor.GetLength()));
		
		tInfo.tRule[iSelect].iEvent = m_cboEventType.GetCurSel();
		tInfo.tRule[iSelect].iTime = GetDlgItemInt(IDC_EDIT_CONDITION_TIME);
		tInfo.tRule[iSelect].iSensitivity = m_sldSensitivity.GetPos();

		iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_STRATEGY_CONDITION, &tInfo, sizeof(tInfo), &tResponse, sizeof(tResponse));
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_XmlSetDevConfig[NETXMLCFG_STRATEGY_CONDITION] (%d)",m_iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlGetDevConfig[NETXMLCFG_STRATEGY_CONDITION] (%d)",m_iLogonID);
	}

}

void CLS_StrategyCondition::OnBnClickedBtnConditionGetColor()
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
	m_pDlgVideoView->SetDrawType(DrawType_GaugePoint);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection,TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		XmlPositionColorIn tInfo = {0};

		POINT ptPolygon[MAX_STRATEGY_REGION_POINT] = {0} ;
		GetPointsFromString(cPointBuf, iPointNum, ptPolygon);
		if (iPointNum>0)
		{
			tInfo.iScreenX = ptPolygon[iPointNum-1].x;
			tInfo.iScreenY = ptPolygon[iPointNum-1].y;
		}

		XmlPositionColorResult tResult = {0};
		int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_POSITION_COLOR, &tInfo, sizeof(tInfo), &tResult, sizeof(tResult));
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_XmlGetDevConfig[NETXMLCFG_POSITION_COLOR] (%d)",m_iLogonID);
		}
		else
		{
			for (int j=0;j<CONDITION_COLOUR_NUM_MAX;j++)
			{
				m_chkColor[j].SetCheck(BST_UNCHECKED);
			}

			CString cstrTemp;
			cstrTemp.Format("%s",tResult.cColor);

			for (int j=0;j<CONDITION_COLOUR_NUM_MAX;j++)
			{
				if (cstrTemp == m_cColor[j])
				{
					m_chkColor[j].SetCheck(BST_CHECKED);
					break;
				}
			}
		}
	}
	else
	{
		//Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

BOOL CLS_StrategyCondition::SetVCAStatus(bool _bStatus)
{
	int iProType = 0;
	int iProMode = 0;
	NetClient_GetProductTypeEx(m_iLogonID, &iProMode, &iProType);

	VCASuspend tInfo = {0};
	// iProType : output product type: 0--reserverd, 1--ipc, 2--nvr
	if(NVRecord_PRODUCT == iProType)
	{
		// iDevType : 0-IPC, 1-NVR
		tInfo.iDevType = 1;
	}
	if(!_bStatus)			//Parameter false means pause, true means open
	{
		tInfo.iStatus = VCA_OPT_SUSPEND;	//0 means pause
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNO, SYNC_NET_CLIENT_VCA_SUSPEND, &tInfo, sizeof(VCASuspend), NULL, 0);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SyncSetDevCfg stop VCA failed logonID(%d)", m_iLogonID);
			return FALSE;
		}
	}
	else
	{
		tInfo.iStatus = VCA_OPT_OPENVCA;
		int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNO, SYNC_NET_CLIENT_VCA_SUSPEND, &tInfo, sizeof(VCASuspend), NULL, 0);
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SyncSetDevCfg start VCA failed logonID(%d)", m_iLogonID);
			return FALSE;
		}
	}
	return TRUE;
}

void CLS_StrategyCondition::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);
	SetVCAStatus(bShow ? false : true);
}

void CLS_StrategyCondition::UI_UpdateStrategy()
{
	int iSelect = m_cboStrategyNo.GetCurSel();
	if (iSelect<0 || iSelect>=MAX_CONDITION_NUM)
	{
		return;
	}

	XmlStrategyInput tInfo = {0};
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_STRATEGY_INPUT, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if(RET_SUCCESS == iRet)
	{
		m_cboRuleEnable.SetCheck(tInfo.tCondition[iSelect].iEnabled);
		SetDlgItemText(IDC_EDIT_CONDITION_RULE_NAME, tInfo.tCondition[iSelect].cRuleName);

		OsStrArray strArray;
		os_split_str_to_arr(tInfo.tCondition[iSelect].cCondition, ',', &strArray);

		for (int i=0; i<MAX_STRATEGY_NUM; i++)
		{
			m_chkCondition[i].SetCheck(BST_UNCHECKED);
		}

		for(int i=0; i<strArray.iCount&&i<MAX_STRATEGY_NUM; i++)
		{
			int iIndex = atoi(strArray.pcStr[i]);
			if (0 < iIndex && iIndex<= MAX_STRATEGY_NUM)
			{
				m_chkCondition[iIndex-1].SetCheck(BST_CHECKED);
			}
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlGetDevConfig[NETXMLCFG_STRATEGY_INPUT] (%d)",m_iLogonID);
	}
}



void CLS_StrategyCondition::OnCbnSelchangeCboConditionRuleno()
{
	UI_UpdateStrategy();
}

void CLS_StrategyCondition::OnBnClickedBtnConditionRuleSet()
{
	int iSelect = m_cboStrategyNo.GetCurSel();
	if (iSelect<0 || iSelect>=MAX_CONDITION_NUM)
	{
		AddLog(LOG_TYPE_FAIL,"","OnBnClickedBtnConditionRuleSet iSelect out of range(%d)",iSelect);
		return;
	}

	XmlStrategyInput tInfo = {0};
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_STRATEGY_INPUT, &tInfo, sizeof(tInfo), &tInfo, sizeof(tInfo));
	if(RET_SUCCESS == iRet)
	{
		XmlResponseStatus tResponse = {0};
		tInfo.tCondition[iSelect].iEnabled = m_cboRuleEnable.GetCheck();
		
		CString cstrRuleName;
		GetDlgItemText(IDC_EDIT_CONDITION_RULE_NAME ,cstrRuleName);
		strncpy_s(tInfo.tCondition[iSelect].cRuleName, (LPSTR)(LPCTSTR)cstrRuleName, min(sizeof(tInfo.tCondition[iSelect].cRuleName), cstrRuleName.GetLength()));

		CString cstrStrategy;
		int iSelectedNum = 0;
		for (int i=0; i<MAX_STRATEGY_NUM; i++)
		{
			if (iSelectedNum > MAX_CONDITION_NUM)
			{
				break;
			}

			if (m_chkCondition[i].GetCheck())
			{
				cstrStrategy.AppendFormat("%d,", i+1);
				iSelectedNum++;
			}
			
		}
		cstrStrategy = cstrStrategy.Left(cstrStrategy.GetLength()-1);
		strncpy_s(tInfo.tCondition[iSelect].cCondition, (LPSTR)(LPCTSTR)cstrStrategy, min(sizeof(tInfo.tCondition[iSelect].cCondition), cstrStrategy.GetLength()));

		iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_STRATEGY_INPUT, &tInfo, sizeof(tInfo), &tResponse, sizeof(tResponse));
		if (RET_SUCCESS != iRet)
		{
			AddLog(LOG_TYPE_FAIL,"","NetClient_XmlSetDevConfig[NETXMLCFG_STRATEGY_INPUT] (%d)",m_iLogonID);
		}
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlGetDevConfig[NETXMLCFG_STRATEGY_INPUT] (%d)",m_iLogonID);
	}
}

void CLS_StrategyCondition::ChkConditionRuleTip(int _iIndex)
{
	int iSelectedNum = 0;
	for (int i=0; i<MAX_STRATEGY_NUM; i++)
	{
		if (m_chkCondition[i].GetCheck())
		{
			iSelectedNum++;
		}
	}

	if (0 <= _iIndex && _iIndex< MAX_STRATEGY_NUM)
	{
		if (m_chkCondition[_iIndex].GetCheck() && iSelectedNum > MAX_CONDITION_NUM)
		{
			m_chkCondition[_iIndex].SetCheck(BST_UNCHECKED);
			MessageBox(GetTextByLan("最多只能选择4个条件!","You can select a maximum of four conditions!"),GetTextEx(IDS_CONFIG_PROMPT),MB_OK);
		}
	}
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule1()
{
	ChkConditionRuleTip(0);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule2()
{
	ChkConditionRuleTip(1);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule3()
{
	ChkConditionRuleTip(2);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule4()
{
	ChkConditionRuleTip(3);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule5()
{
	ChkConditionRuleTip(4);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule6()
{
	ChkConditionRuleTip(5);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule7()
{
	ChkConditionRuleTip(6);
}

void CLS_StrategyCondition::OnBnClickedChkConditionRule8()
{
	ChkConditionRuleTip(7);
}
