// DlgVcaRefBoundaryInfo.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgVcaRefBoundaryInfo.h"


// DlgVcaRefBoundaryInfo dialog

IMPLEMENT_DYNAMIC(DlgVcaRefBoundaryInfo, CDialog)

DlgVcaRefBoundaryInfo::DlgVcaRefBoundaryInfo(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(DlgVcaRefBoundaryInfo::IDD, pParent)
	, m_blEnable(FALSE)
	, m_iPointNum(4)
{
	memset(&m_tRefBoundaryArr, 0, sizeof(m_tRefBoundaryArr));
}

DlgVcaRefBoundaryInfo::~DlgVcaRefBoundaryInfo()
{
}

BOOL DlgVcaRefBoundaryInfo::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	m_listRefBoundary.SetExtendedStyle(LVS_EX_FULLROWSELECT | LVS_EX_HEADERDRAGDROP | LVS_EX_GRIDLINES);
	//Initialize the comboBox of the number of boundary points
	for(int i = 4; i <= REFERENCE_POINT_NUM_MAX; i++)
	{
		CString strNum;
		strNum.Format("%d", i);						// The minimum number of boundary points is 4
		m_cboPointNum.AddString(strNum);
	}
	m_cboPointNum.SetCurSel(0);
	m_iPointNum = m_cboPointNum.GetCurSel() + 4;	//Convert the combo subscript into the number of boundary points for storage
	UI_UpdateText();								//update interface
	UpdateList(m_iPointNum);
	return TRUE;
}

void DlgVcaRefBoundaryInfo::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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
	GetVcaRefBoundaryInfo();
}

void DlgVcaRefBoundaryInfo::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_REF_BOUNDARY_INFO, m_listRefBoundary);
	DDX_Check(pDX, IDC_CHECK_ENABLE, m_blEnable);
	DDX_Control(pDX, IDC_COMBO_POINT_NUM, m_cboPointNum);
}


void DlgVcaRefBoundaryInfo::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateText();
	UpdateList(m_iPointNum);
}

void DlgVcaRefBoundaryInfo::UI_UpdateText()
{
	SetDlgItemText(IDC_STATIC_POINT_NUM, GetTextByLan(_T("边界点个数"), _T("Point num")));
	SetDlgItemText(IDC_BUTTON_SAVE, GetTextByLan(_T("设置"), _T("Set")));
	SetDlgItemText(IDC_CHECK_ENABLE, GetTextByLan(_T("使能"), _T("Enable")));
	SetDlgItemText(IDC_BUTTON_SAVE_LINE, GetTextByLan(_T("保存当前行"), _T("Save current line")));

	while(m_listRefBoundary.DeleteColumn(0));
	m_listRefBoundary.InsertColumn(0, GetTextByLan(_T("预留"), _T("Reserve")), LVCFMT_CENTER, 0);
	m_listRefBoundary.InsertColumn(1, GetTextByLan(_T("边界点编号"), _T("Border point ID")), LVCFMT_CENTER, 100);
	m_listRefBoundary.InsertColumn(2, GetTextByLan(_T("X(m)"), _T("X(m)")), LVCFMT_CENTER, 170);
	m_listRefBoundary.InsertColumn(3, GetTextByLan(_T("Y(m)"), _T("Y(m)")), LVCFMT_CENTER, 170);
	m_listRefBoundary.InsertColumn(4, GetTextByLan(_T("Z(m)"), _T("Z(m)")), LVCFMT_CENTER, 170);
	m_listRefBoundary.DeleteColumn(0);

	GetVcaRefBoundaryInfo();
}

CString DlgVcaRefBoundaryInfo::ProtToRealCoor(int _iCoor)
{
	double dCoor = _iCoor - 10000000;
	dCoor /= 1000;
	CString strCoor;
	strCoor.Format("%0.3f", dCoor);
	return strCoor;
}

void DlgVcaRefBoundaryInfo::UpdateList(int _iRowLines)
{
	m_listRefBoundary.DeleteAllItems();	//Clear all lines in list
	for(int i = 0; i < _iRowLines && i < REFERENCE_POINT_NUM_MAX; i++)
	{
		CString strNum;
		strNum.Format("%d", i);
		m_listRefBoundary.InsertItem(i, strNum.GetBuffer());
		m_listRefBoundary.SetItemText(i, 1, ProtToRealCoor(m_tRefBoundaryArr.tWldPointCoord[i].iX));
		m_listRefBoundary.SetItemText(i, 2, ProtToRealCoor(m_tRefBoundaryArr.tWldPointCoord[i].iY));
		m_listRefBoundary.SetItemText(i, 3, ProtToRealCoor(m_tRefBoundaryArr.tWldPointCoord[i].iZ));
	}
	//The first row is selected by default
	m_listRefBoundary.SetItemState(0, LVIS_FOCUSED | LVIS_SELECTED,LVIS_FOCUSED | LVIS_SELECTED);
	m_listRefBoundary.SetSelectionMark(0);
}

void DlgVcaRefBoundaryInfo::GetVcaRefBoundaryInfo()
{
	memset(&m_tRefBoundaryArr, 0, sizeof(m_tRefBoundaryArr));
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_REFBOUNDARYINFO, m_iChannelNO, &m_tRefBoundaryArr, sizeof(m_tRefBoundaryArr));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_VCAGetConfig[VCA_CMD_REFBOUNDARYINFO] (%d, %d)", m_iLogonID, m_iChannelNO);
		if(0 == m_tRefBoundaryArr.iEnable)
		{
			m_blEnable = FALSE;
		}
		else if(1 == m_tRefBoundaryArr.iEnable)
		{
			m_blEnable = TRUE;
		}
		else
		{
			memset(&m_tRefBoundaryArr, 0, sizeof(m_tRefBoundaryArr));
			return;					//The received protocol content (enable) is illegal
		}
		UpdateData(FALSE);			//update enable status
		if(m_tRefBoundaryArr.iPointNum < 4 || m_tRefBoundaryArr.iPointNum > REFERENCE_POINT_NUM_MAX)
		{
			memset(&m_tRefBoundaryArr, 0, sizeof(m_tRefBoundaryArr));
			return;					//The received protocol content (number of coordinate points) is illegal
		}
		m_iPointNum = m_tRefBoundaryArr.iPointNum;
		m_cboPointNum.SetCurSel(m_iPointNum - 4);
		UpdateList(m_iPointNum);	//update list content
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCAGetConfig[VCA_CMD_REFBOUNDARYINFO] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}


BEGIN_MESSAGE_MAP(DlgVcaRefBoundaryInfo, CDialog)
	ON_BN_CLICKED(IDC_CHECK_ENABLE, &DlgVcaRefBoundaryInfo::OnBnClickedCheckEnable)
	ON_BN_CLICKED(IDC_BUTTON_SAVE, &DlgVcaRefBoundaryInfo::OnBnClickedButtonSave)
	ON_CBN_SELCHANGE(IDC_COMBO_POINT_NUM, &DlgVcaRefBoundaryInfo::OnCbnSelchangeComboPointNum)
	ON_NOTIFY(LVN_ITEMCHANGED, IDC_LIST_REF_BOUNDARY_INFO, &DlgVcaRefBoundaryInfo::OnLvnItemchangedListRefBoundaryInfo)
	ON_BN_CLICKED(IDC_BUTTON_SAVE_LINE, &DlgVcaRefBoundaryInfo::OnBnClickedButtonSaveLine)
END_MESSAGE_MAP()


// DlgVcaRefBoundaryInfo message handler

void DlgVcaRefBoundaryInfo::OnBnClickedCheckEnable()
{
	UpdateData(TRUE);
}

void DlgVcaRefBoundaryInfo::OnBnClickedButtonSave()
{
	// TODO: Add control notification handler code here
	RefBoundaryInfo tTemp;
	memset(&tTemp, 0, sizeof(tTemp));
	tTemp.iEnable = m_blEnable;
	tTemp.iPointNum = m_iPointNum;
	tTemp.iSceneId = 0;
	memcpy(tTemp.tWldPointCoord, m_tRefBoundaryArr.tWldPointCoord, sizeof(ReferenceCoordinate) * m_iPointNum);
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_REFBOUNDARYINFO, m_iChannelNO, &tTemp, sizeof(tTemp));
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC, "", "NetClient_VCASetConfig[VCA_CMD_REFBOUNDARYINFO] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_VCASetConfig[VCA_CMD_REFBOUNDARYINFO] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void DlgVcaRefBoundaryInfo::OnCbnSelchangeComboPointNum()
{
	//When the item selected by combo changes, the number of coordinate points and the list are updated synchronously
	m_iPointNum = m_cboPointNum.GetCurSel() + 4;
	UpdateList(m_iPointNum);
}

void DlgVcaRefBoundaryInfo::OnLvnItemchangedListRefBoundaryInfo(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMLISTVIEW pNMLV = reinterpret_cast<LPNMLISTVIEW>(pNMHDR);
	// TODO: Add control notification handler code here
	if(pNMLV->uChanged == LVIF_STATE)
	{
		if(pNMLV->uNewState)
		{
			int nIndex = pNMLV->iItem;
			CString strX;
			CString strY;
			CString strZ;
			CString strLineNum;
			strX = m_listRefBoundary.GetItemText(nIndex, 1);
			strY = m_listRefBoundary.GetItemText(nIndex, 2);
			strZ = m_listRefBoundary.GetItemText(nIndex, 3);
			strLineNum.Format("%d", nIndex);
			SetDlgItemText(IDC_EDIT_X, strX);
			SetDlgItemText(IDC_EDIT_Y, strY);
			SetDlgItemText(IDC_EDIT_Z, strZ);
			SetDlgItemText(IDC_STATIC_LINE_NUM, strLineNum);
		}
	}
	*pResult = 0;
}

void DlgVcaRefBoundaryInfo::OnBnClickedButtonSaveLine()
{
	// TODO: Add control notification handler code here
	CString strX;
	CString strY;
	CString strZ;
	double dCoorX = 0;
	double dCoorY = 0;
	double dCoorZ = 0;
	int iIndex = m_listRefBoundary.GetSelectionMark();
	if(-1 == iIndex)
	{
		return;
	}
	GetDlgItemText(IDC_EDIT_X, strX);
	GetDlgItemText(IDC_EDIT_Y, strY);
	GetDlgItemText(IDC_EDIT_Z, strZ);
	dCoorX = atof(strX.GetBuffer());
	dCoorY = atof(strY.GetBuffer());
	dCoorZ = atof(strZ.GetBuffer());
	
	dCoorX = dCoorX * 1000 + 10000000;
	dCoorY = dCoorY * 1000 + 10000000;
	dCoorZ = dCoorZ * 1000 + 10000000;

	if((int)dCoorX < COORDINATE_MIN || (int)dCoorX > COORDINATE_MAX 
		|| (int)dCoorY < COORDINATE_MIN || (int)dCoorY > COORDINATE_MAX
		|| (int)dCoorZ < COORDINATE_MIN || (int)dCoorZ > COORDINATE_MAX)
	{
		return;
	}

	m_tRefBoundaryArr.tWldPointCoord[iIndex].iX = static_cast<int>(dCoorX);
	m_tRefBoundaryArr.tWldPointCoord[iIndex].iY = static_cast<int>(dCoorY);
	m_tRefBoundaryArr.tWldPointCoord[iIndex].iZ = static_cast<int>(dCoorZ);
	UpdateList(m_iPointNum);
}
