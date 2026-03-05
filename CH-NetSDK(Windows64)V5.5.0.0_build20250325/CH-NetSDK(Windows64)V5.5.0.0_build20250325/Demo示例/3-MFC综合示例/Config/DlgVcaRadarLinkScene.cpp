// DlgVcaRadarLinkScene.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgVcaRadarLinkScene.h"


// DlgVcaRadarLinkScene dialog

IMPLEMENT_DYNAMIC(DlgVcaRadarLinkScene, CDialog)

DlgVcaRadarLinkScene::DlgVcaRadarLinkScene(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(DlgVcaRadarLinkScene::IDD, pParent)
{
	m_vecAnyScene.clear();
	memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
}

DlgVcaRadarLinkScene::~DlgVcaRadarLinkScene()
{
	m_listLinkSceneParam.ReleaseControls();
}

BOOL DlgVcaRadarLinkScene::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateText();
	return TRUE;
}

void DlgVcaRadarLinkScene::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNO = 0;
	}
	else
	{
		m_iChannelNO = _iChannelNo;
	}
	GetVcaRadarLinkSceneParam();
}

void DlgVcaRadarLinkScene::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_LINK_SCENE_PARAM, m_listLinkSceneParam);
}


void DlgVcaRadarLinkScene::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateText();
}

BOOL DlgVcaRadarLinkScene::SetVCAStatus(bool _bStatus)
{
	int iProType = 0;
	int iProMode = 0;
	NetClient_GetProductTypeEx(m_iLogonID, &iProMode, &iProType);

	VCASuspend tInfo = {0};
	if(2 == iProType)
	{
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
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &tInfo, sizeof(tInfo));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig failed logonID(%d)", m_iLogonID);
			return FALSE;
		}
	}
	else
	{
		tInfo.iStatus = 1;
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &tInfo, sizeof(tInfo));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig failed logonID(%d)", m_iLogonID);
			return FALSE;
		}
	}
	return TRUE;
}

void DlgVcaRadarLinkScene::UI_UpdateText()
{
	SetDlgItemText(IDC_STATIC_LINK_SCENE_PARAM, GetTextByLan(_T("雷达联动场景参数"), _T("Radar linkage scene param")));
	SetDlgItemText(IDC_BUTTON_ADD, GetTextByLan(_T("添加"), _T("Add")));
	SetDlgItemText(IDC_BUTTON_DELETE, GetTextByLan(_T("删除"), _T("Delete")));
	SetDlgItemText(IDC_BUTTON_SAVE, GetTextByLan(_T("保存"), _T("Save")));
	SetDlgItemText(IDC_BUTTON_GET, GetTextByLan(_T("获取参数"), _T("Get Param")));

	m_listLinkSceneParam.DeleteAllColumns();
	m_listLinkSceneParam.InsertColumn(0, GetTextByLan(_T("预留"), _T("Reserve")), LVCFMT_CENTER, 0);
	m_listLinkSceneParam.InsertColumn(1, GetTextByLan(_T("场景ID"), _T("Scene ID")), LVCFMT_CENTER, 110);
	m_listLinkSceneParam.InsertColumn(2, GetTextByLan(_T("开始位置(cm)"), _T("Start Position(cm)")), LVCFMT_CENTER, 258);
	m_listLinkSceneParam.InsertColumn(3, GetTextByLan(_T("结束位置(cm)"), _T("End Position(cm)")), LVCFMT_CENTER, 258);
	m_listLinkSceneParam.DeleteColumn(0);

	m_listLinkSceneParam.SetRowHeigt(NeuRowHeigt);
	m_listLinkSceneParam.SetExtendedStyle(LVS_EX_GRIDLINES | LVS_EX_FULLROWSELECT|LVS_EX_CHECKBOXES);
	m_listLinkSceneParam.SetHeaderHeight((float)1.1);
	m_listLinkSceneParam.SetHeaderFontHW(HeaderFontHEX,NULL);
	m_listLinkSceneParam.SetParentHwnd(m_hWnd);

	m_listLinkSceneParam.ShowWindow(SW_SHOW);

	GetVcaRadarLinkSceneParam();
}

BEGIN_MESSAGE_MAP(DlgVcaRadarLinkScene, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_ADD, &DlgVcaRadarLinkScene::OnBnClickedButtonAdd)
	ON_BN_CLICKED(IDC_BUTTON_DELETE, &DlgVcaRadarLinkScene::OnBnClickedButtonDelete)
	ON_BN_CLICKED(IDC_BUTTON_SAVE, &DlgVcaRadarLinkScene::OnBnClickedButtonSave)
	ON_BN_CLICKED(IDC_BUTTON_GET, &DlgVcaRadarLinkScene::OnBnClickedButtonGet)
END_MESSAGE_MAP()


// DlgVcaRadarLinkScene message handler

//Judging whether the start:end value pair range is legal
BOOL DlgVcaRadarLinkScene::CheckNumPairRange(int _iStart, int _iEnd)
{
	if(_iStart < 0 || _iStart > 100000 || _iEnd < 0 || _iEnd > 100000)
	{
		MessageBox(GetTextByLan(_T("请输入[0,100000]范围的数据"), _T("please input in [0,100000]")));
		return FALSE;
	}
	if(_iStart >= _iEnd)
	{
		MessageBox(GetTextByLan(_T("开始位置坐标要小于结束位置坐标"), _T("StarPos should be less than EndPos")));
		return FALSE;
	}
	return TRUE;
}

//Insert the obtained information into the list contrl

void DlgVcaRadarLinkScene::GetVcaRadarLinkSceneParam()
{
	memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_RADARLINKSCENE, m_iChannelNO, &m_tRadarLinkSceneArr, sizeof(RadarLinkSceneArr));
	if(RET_SUCCESS == iRet)
	{
		m_listLinkSceneParam.DeleteAllItems();
		m_listLinkSceneParam.ResetContent();
		m_listLinkSceneParam.SetItemControl(COMBOBOX, 0);
		m_listLinkSceneParam.SetItemControl(EDITBOX, 1);
		m_listLinkSceneParam.SetItemControl(EDITBOX, 2);

		AnyScene sScene = {0};
		int iReturnByte = 0;
		m_listLinkSceneParam.ResetContent();
		m_vecAnyScene.clear();
		for (int i = 0; i < MAX_SCENE_NUM; ++i)//loop 0-32 scenes
		{
			sScene.iSceneID = i + 1;
			NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNO, &sScene, sizeof(AnyScene), &iReturnByte);
			m_vecAnyScene.push_back(sScene);
			//Number the enabled scene and insert it into the combox
			m_listLinkSceneParam.AddToControlsData(0, IntToStr(i + 1).c_str());//i is index
		}

		if (0 == m_vecAnyScene.size())
		{
			m_listLinkSceneParam.ShowControls(FALSE);
			m_listLinkSceneParam.Invalidate();
		}
		m_listLinkSceneParam.ShowWindow(SW_SHOW);
		m_listLinkSceneParam.Invalidate(TRUE);

		if(0 == m_vecAnyScene.size())
		{
			MessageBox(GetTextByLan(_T("使能的场景数量为0"), _T("The number of enabled scenes is 0")));
			return;
		}

		for(int i = 0; i < MAX_RADAR_LINK_SCENE_NUM && i < m_tRadarLinkSceneArr.iParaListNum; i++)
		{
			//Insert the obtained information into the list control
			m_listLinkSceneParam.InsertItem(i, _T(""));
			LVITEM item;
			memset(&item, 0, sizeof(LVITEM));
			item.mask = LVIF_TEXT;
			item.iItem = i;
			CString cstrTmp = "";

			//scene ID
			item.iSubItem = 0;
			cstrTmp = IntToStr(m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iSceneId + 1).c_str() ;
			item.pszText = (LPSTR)(LPCTSTR)cstrTmp;
			m_listLinkSceneParam.SetItem(&item);
			m_listLinkSceneParam.SetItemData(i, m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iSceneId + 1);

			// start position
			item.iSubItem = 1;
			CString strStartPos = IntToCString(m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis);
			item.pszText = (LPSTR)(LPCTSTR)(strStartPos);
			m_listLinkSceneParam.SetItem(&item);
			m_listLinkSceneParam.SetItemData(i, m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis);

			// end position
			item.iSubItem = 2;
			CString strEndPos = IntToCString(m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis);
			item.pszText = (LPSTR)(LPCTSTR)(strEndPos);
			m_listLinkSceneParam.SetItem(&item);
			m_listLinkSceneParam.SetItemData(i, m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis);
		}
		int iCount = m_listLinkSceneParam.GetItemCount();
		if(0 == iCount)
		{
			//The first row is selected
			m_listLinkSceneParam.SetItemState(0, LVIS_SELECTED|LVIS_FOCUSED, LVIS_SELECTED|LVIS_FOCUSED);
			m_listLinkSceneParam.SetItemColor(0, COLOR_SET);
		}
		else
		{
			// current row is canceled
			m_listLinkSceneParam.SetItemState(iCount-1, 0, LVIS_SELECTED|LVIS_FOCUSED);
			m_listLinkSceneParam.SetItemColor(iCount-1, COLOR_CANCEL);
			//current row is selected
			m_listLinkSceneParam.SetItemState(iCount, LVIS_SELECTED|LVIS_FOCUSED, LVIS_SELECTED|LVIS_FOCUSED);
			m_listLinkSceneParam.SetItemColor(iCount, COLOR_SET);
		}
		m_listLinkSceneParam.MoveControlToFocusItem(iCount - 1);
		AddLog(LOG_TYPE_SUCC, "","NetClient_VCAGetConfig[VCA_CMD_RADARLINKSCENE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_VCAGetConfig[VCA_CMD_RADARLINKSCENE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void DlgVcaRadarLinkScene::AddNewLine()
{
	m_listLinkSceneParam.GetAndDisplayControlsData();
	m_listLinkSceneParam.ShowControls(false);

	int iCount = m_listLinkSceneParam.GetItemCount();
	if(iCount >= MAX_RADAR_LINK_SCENE_NUM)
	{
		MessageBox(GetTextByLan(_T("最多有32个场景"), _T("Up to 32 scenes")));
		return;
	}

	if (0 == iCount)
	{
		m_listLinkSceneParam.SetItemControl(COMBOBOX, 0);
		m_listLinkSceneParam.SetItemControl(EDITBOX, 1);
		m_listLinkSceneParam.SetItemControl(EDITBOX, 2);
		m_listLinkSceneParam.SetLimitTextLength(1, LEN_8);
		m_listLinkSceneParam.SetLimitTextLength(2, LEN_8);
	}

	AnyScene sScene = {0};
	int iReturnByte = 0;
	m_listLinkSceneParam.ResetContent();
	m_vecAnyScene.clear();
	for (int i = 0; i < MAX_SCENE_NUM; i++)//loop 0-32 scenes
	{
		sScene.iSceneID = i + 1;
		NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNO, &sScene, sizeof(AnyScene), &iReturnByte);
		m_vecAnyScene.push_back(sScene);
		//Number the enabled scene and insert it into the combox
		m_listLinkSceneParam.AddToControlsData(0, IntToStr(i + 1).c_str());//i is index 
	}

	//insert a new row into the list
	m_listLinkSceneParam.InsertItem(iCount, _T(""));
	LVITEM item;
	memset(&item, 0, sizeof(LVITEM));
	item.mask = LVIF_TEXT;
	item.iItem = iCount;
	CString cstrTmp = "";

	//scene ID
	item.iSubItem = 0;
	cstrTmp = IntToCString(m_vecAnyScene[0].iSceneID);
	item.pszText = (LPSTR)(LPCTSTR)cstrTmp;
	m_listLinkSceneParam.SetItem(&item);
	m_listLinkSceneParam.SetItemData(0, m_vecAnyScene[0].iSceneID);

	//Start position by default write 0
	CString strStartTime = _T("0");
	item.iSubItem = 1;
	item.pszText = (LPSTR)(LPCTSTR)strStartTime;
	m_listLinkSceneParam.SetItem(&item);

	//The end position defaults to 0
	CString strEndTime = _T("0");
	item.iSubItem = 2;
	item.pszText = (LPSTR)(LPCTSTR)strEndTime;
	m_listLinkSceneParam.SetItem(&item);

	if(0 == iCount)
	{
		//The first row is selected
		m_listLinkSceneParam.SetItemState(0, LVIS_SELECTED|LVIS_FOCUSED, LVIS_SELECTED|LVIS_FOCUSED);
		m_listLinkSceneParam.SetItemColor(0, COLOR_SET);
	}
	else
	{
		//current row is canceled
		m_listLinkSceneParam.SetItemState(iCount-1, 0, LVIS_SELECTED|LVIS_FOCUSED);
		m_listLinkSceneParam.SetItemColor(iCount-1, COLOR_CANCEL);
		//current row is selected
		m_listLinkSceneParam.SetItemState(iCount, LVIS_SELECTED|LVIS_FOCUSED, LVIS_SELECTED|LVIS_FOCUSED);
		m_listLinkSceneParam.SetItemColor(iCount, COLOR_SET);
	}
	m_listLinkSceneParam.MoveControlToFocusItem(iCount);
}

void DlgVcaRadarLinkScene::DeleteSelectLine()
{
	int iStartCount = m_listLinkSceneParam.GetItemCount();
	if(0 == iStartCount)
	{
		return;
	}
	for(int i = iStartCount; i > 0; i--)
	{
		if (BST_CHECKED == m_listLinkSceneParam.GetCheck(i-1))
		{
			m_listLinkSceneParam.DeleteItem(i-1);
		}
	}

	int iEndCount = m_listLinkSceneParam.GetItemCount();
	if (0 == iEndCount)
	{
		m_listLinkSceneParam.ShowControls(FALSE);
		m_listLinkSceneParam.ReleaseControls();
	} 
	else
	{
		m_listLinkSceneParam.MoveControlToFocusItem(0);
	}

	m_listLinkSceneParam.Invalidate();
}

void DlgVcaRadarLinkScene::SaveAndSet()
{
	//Save begin
	m_listLinkSceneParam.GetAndDisplayControlsData();
	int iTotalCount = m_listLinkSceneParam.GetItemCount();
	if(iTotalCount < 0)
	{
		return;
	}
	memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
	m_tRadarLinkSceneArr.iParaCnt = 3;
	int iIndex = 0; 
	for(; iIndex < iTotalCount && iIndex < MAX_RADAR_LINK_SCENE_NUM; iIndex++)
	{
		CString strSceneID = m_listLinkSceneParam.GetItemText(iIndex, 0);
		m_tRadarLinkSceneArr.tRadarLinkSceneInfo[iIndex].iSceneId = StrToInt(strSceneID) - 1;
		CString strStart = m_listLinkSceneParam.GetItemText(iIndex, 1);
		CString strEnd = m_listLinkSceneParam.GetItemText(iIndex, 2);
		int iStart = _ttoi(strStart);
		int iEnd = _ttoi(strEnd);
		if(!CheckNumPairRange(iStart, iEnd))
		{
			//There has been a pop-up reminder in CheckNumPairRange(), and it will not pop up again here
			memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
			return;
		}
		int i = 0;
		for(; i < m_tRadarLinkSceneArr.iParaListNum && i < MAX_RADAR_LINK_SCENE_NUM; i++)
		{
			if(m_tRadarLinkSceneArr.tRadarLinkSceneInfo[iIndex].iSceneId == m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iSceneId)
			{
				memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
				MessageBox(GetTextByLan(_T("列表中不能有重复场景ID！"), _T("There cannot be duplicate scene IDS in the list！")));
				return;
			}
			if(iStart < m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis
				&& iStart >= m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis)
			{
				memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
				MessageBox(GetTextByLan(_T("不同场景之间的位置范围不能有交集"), _T("The position range between different scenes cannot have intersection")));
				return;
			}
			if(iEnd <= m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis
				&& iEnd > m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis)
			{
				memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
				MessageBox(GetTextByLan(_T("不同场景之间的位置范围不能有交集"), _T("The position range between different scenes cannot have intersection")));
				return;
			}
			if(iEnd <= m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis
				&& iStart >= m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis)
			{
				memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
				MessageBox(GetTextByLan(_T("不同场景之间的位置范围不能有交集"), _T("The position range between different scenes cannot have intersection")));
				return;
			}
			if(iEnd >= m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis
				&& iStart <= m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis)
			{
				memset(&m_tRadarLinkSceneArr, 0, sizeof(m_tRadarLinkSceneArr));
				MessageBox(GetTextByLan(_T("不同场景之间的位置范围不能有交集"), _T("The position range between different scenes cannot have intersection")));
				return;
			}
		}
		m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iStarPosDis = iStart;
		m_tRadarLinkSceneArr.tRadarLinkSceneInfo[i].iEndPosDis = iEnd;
		m_tRadarLinkSceneArr.iParaListNum++;
	}
	if(m_tRadarLinkSceneArr.iParaListNum < 0)
	{
		return;
	}
	//Save end
	//close the algorithm
	if(SetVCAStatus(FALSE))
	{
		//Set begin
		m_tRadarLinkSceneArr.iRadarLinkSceneInfoSize = sizeof(RadarLinkSceneInfo);
		int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_RADARLINKSCENE, m_iChannelNO, &m_tRadarLinkSceneArr, sizeof(m_tRadarLinkSceneArr));
		if(RET_SUCCESS == iRet)
		{
			AddLog(LOG_TYPE_SUCC, "", "NetClient_VCASetConfig[VCA_CMD_RADARLINKSCENE](%d, %d)", m_iLogonID, m_iChannelNO);
		}
		else
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_VCASetConfig[VCA_CMD_RADARLINKSCENE](%d, %d)", m_iLogonID, m_iChannelNO);
		}
		//Set end
		//open the algorithm
		SetVCAStatus(TRUE);
	}
}

void DlgVcaRadarLinkScene::OnBnClickedButtonAdd()
{
	AddNewLine();
}

void DlgVcaRadarLinkScene::OnBnClickedButtonDelete()
{
	DeleteSelectLine();
}

void DlgVcaRadarLinkScene::OnBnClickedButtonSave()
{
	SaveAndSet();
}

void DlgVcaRadarLinkScene::OnBnClickedButtonGet()
{
	GetVcaRadarLinkSceneParam();
}
