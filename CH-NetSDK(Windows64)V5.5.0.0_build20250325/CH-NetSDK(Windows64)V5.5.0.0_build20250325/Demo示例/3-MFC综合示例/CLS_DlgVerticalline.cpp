// CLS_DlgVerticalline.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgVerticalline.h"


// CLS_DlgVerticalline dialog

IMPLEMENT_DYNAMIC(CLS_DlgVerticalline, CDialog)

CLS_DlgVerticalline::CLS_DlgVerticalline(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgVerticalline::IDD, pParent)
{
	m_iLogonID = -1;
	int m_iChannelNo = -1;
	int m_iStreamNo = -1;
}

CLS_DlgVerticalline::~CLS_DlgVerticalline()
{
}

void CLS_DlgVerticalline::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_SCENE, m_cboSceneID);
	DDX_Control(pDX, IDC_COMBO_OPERATETYPE, m_cboOperateType);
	DDX_Control(pDX, IDC_EDIT_VERTICALNO, m_edtVerticalNo);
	DDX_Control(pDX, IDC_EDIT_STARTDISTANCE, m_edtStartDistance);
	DDX_Control(pDX, IDC_EDIT_BOTTOM, m_edtBottomDistance);
	DDX_Control(pDX, IDC_EDIT_COUNT, m_edtCount);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW1, m_edtWaterFlow[0]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW2, m_edtWaterFlow[1]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW3, m_edtWaterFlow[2]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW4, m_edtWaterFlow[3]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW5, m_edtWaterFlow[4]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW6, m_edtWaterFlow[5]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW7, m_edtWaterFlow[6]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW8, m_edtWaterFlow[7]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW9, m_edtWaterFlow[8]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW10, m_edtWaterFlow[9]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW11, m_edtWaterFlow[10]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW12, m_edtWaterFlow[11]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW13, m_edtWaterFlow[12]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW14, m_edtWaterFlow[13]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW15, m_edtWaterFlow[14]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW16, m_edtWaterFlow[15]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW17, m_edtWaterFlow[16]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW18, m_edtWaterFlow[17]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW19, m_edtWaterFlow[18]);
	DDX_Control(pDX, IDC_EDIT_WATERFLOW20, m_edtWaterFlow[19]);

	DDX_Control(pDX, IDC_EDIT_WATERLEVEL1, m_edtWaterLevel[0]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL2, m_edtWaterLevel[1]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL3, m_edtWaterLevel[2]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL4, m_edtWaterLevel[3]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL5, m_edtWaterLevel[4]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL6, m_edtWaterLevel[5]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL7, m_edtWaterLevel[6]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL8, m_edtWaterLevel[7]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL9, m_edtWaterLevel[8]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL10, m_edtWaterLevel[9]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL11, m_edtWaterLevel[10]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL12, m_edtWaterLevel[11]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL13, m_edtWaterLevel[12]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL14, m_edtWaterLevel[13]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL15, m_edtWaterLevel[14]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL16, m_edtWaterLevel[15]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL17, m_edtWaterLevel[16]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL18, m_edtWaterLevel[17]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL19, m_edtWaterLevel[18]);
	DDX_Control(pDX, IDC_EDIT_WATERLEVEL20, m_edtWaterLevel[19]);
}


BEGIN_MESSAGE_MAP(CLS_DlgVerticalline, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_DlgVerticalline::OnBnClickedButtonSet)
END_MESSAGE_MAP()


// CLS_DlgVerticalline message handlers

BOOL CLS_DlgVerticalline::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_cboSceneID.ResetContent();
	for (int i = 0; i < MAX_SCENE_NUM; i++)
	{
		CString strSceneID;
		strSceneID.Format("%d", i+1);
		m_cboSceneID.AddString(strSceneID);
	}
	m_cboSceneID.SetCurSel(0);

	m_cboOperateType.ResetContent();
	m_cboOperateType.SetItemData(m_cboOperateType.AddString(GetTextByLan(_T("Ôö¼Ó"), _T("Add"))), 1);
	m_cboOperateType.SetItemData(m_cboOperateType.AddString(GetTextByLan(_T("±à¼­"), _T("Edit"))), 2);
	m_cboOperateType.SetItemData(m_cboOperateType.AddString(GetTextByLan(_T("É¾³ý"), _T("Delete"))), 3);
	m_cboOperateType.SetCurSel(0);
	return TRUE;
}

void CLS_DlgVerticalline::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	m_iStreamNo = _iStreamNo;
	return;
}

void CLS_DlgVerticalline::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	VerTiCallIne tInfo = {0};
	tInfo.iSize = (int)sizeof(VerTiCallIne);
	tInfo.iSceneId = m_cboSceneID.GetCurSel();
	tInfo.iControl = m_cboOperateType.GetItemData(m_cboOperateType.GetCurSel());
	CString strCount = "";
	m_edtCount.GetWindowText(strCount);
	tInfo.iCoefNum = _ttoi(strCount);
	m_edtVerticalNo.GetWindowText(strCount);
	tInfo.iVLineId = _ttoi(strCount);
	for (int i = 0; i < tInfo.iCoefNum; i++)
	{
		CString strIndex = "";
		m_edtWaterFlow[i].GetWindowText(strIndex);
		tInfo.tInfo[i].iCoef = _ttoi(strIndex);
		m_edtWaterLevel[i].GetWindowText(strIndex);
		tInfo.tInfo[i].iWaterLevel = _ttoi(strIndex);
	}
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_VERTICALLINE, m_iChannelNo, &tInfo, tInfo.iSize, NULL ,0);
	if (iRet == 0 && tInfo.iResult==0)
	{
		MessageBox(GetTextByLan(_T("³É¹¦"),_T("Success")));
	}
	else
	{
		CString strIndex = "";
		strIndex.Format("%d",tInfo.iResult);
		strIndex = GetTextByLan(_T("Ê§°Ü,´íÎóÂë = "),_T("Failed£¬errno = ")) + strIndex;
		MessageBox(strIndex);
	}
}
