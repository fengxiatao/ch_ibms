// CLS_DlgXmlThreeDimTrans.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgXmlThreeDimTrans.h"


// CLS_DlgXmlThreeDimTrans dialog

IMPLEMENT_DYNAMIC(CLS_DlgXmlThreeDimTrans, CDialog)

CLS_DlgXmlThreeDimTrans::CLS_DlgXmlThreeDimTrans(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgXmlThreeDimTrans::IDD, pParent)
	, m_iSrcChannel(0)
	, m_iPan(0)
	, m_iTitl(0)
	, m_iZoom(0)
	, m_iDesChannel(0)
	, m_iCount(0)
	, m_iX(0)
	, m_iY(0)
	, m_iCountResult(0)
	, m_iSetPtzInfo(1)
{
	memset(&m_tXmlThreeDimTransCondition,0x00,sizeof(m_tXmlThreeDimTransCondition));
}

CLS_DlgXmlThreeDimTrans::~CLS_DlgXmlThreeDimTrans()
{
}

void CLS_DlgXmlThreeDimTrans::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_SRC_CHANNEL, m_iSrcChannel);
	DDX_Text(pDX, IDC_EDIT_SRC_CHANNEL3, m_iPan);
	DDX_Text(pDX, IDC_EDIT_SRC_CHANNEL4, m_iTitl);
	DDX_Text(pDX, IDC_EDIT_SRC_CHANNEL5, m_iZoom);
	DDX_Text(pDX, IDC_EDIT_SRC_CHANNEL2, m_iDesChannel);
	DDX_Text(pDX, IDC_EDIT_COUNT, m_iCount);
	DDX_Control(pDX, IDC_COMBO_NO, m_cboIndex);
	DDX_Text(pDX, IDC_EDIT_X, m_iX);
	DDX_Text(pDX, IDC_EDIT_Y, m_iY);
	DDX_Text(pDX, IDC_EDIT_TOTALCOUNT, m_iCountResult);
	DDX_Control(pDX, IDC_LIST_SMARTQUREY_RESULT, m_lstTransResult);
	DDX_Check(pDX, IDC_CHECK_SET_PTZINFO, m_iSetPtzInfo);
}


BEGIN_MESSAGE_MAP(CLS_DlgXmlThreeDimTrans, CDialog)
	ON_EN_CHANGE(IDC_EDIT_COUNT, &CLS_DlgXmlThreeDimTrans::OnEnChangeEditCount)
	ON_CBN_SELCHANGE(IDC_COMBO_NO, &CLS_DlgXmlThreeDimTrans::OnCbnSelchangeComboNo)
	ON_EN_CHANGE(IDC_EDIT_X, &CLS_DlgXmlThreeDimTrans::OnEnChangeEditX)
	ON_EN_CHANGE(IDC_EDIT_Y, &CLS_DlgXmlThreeDimTrans::OnEnChangeEditY)
	ON_BN_CLICKED(IDC_BUTTON_QUERY, &CLS_DlgXmlThreeDimTrans::OnBnClickedButtonQuery)
	ON_BN_CLICKED(IDC_CHECK_SET_PTZINFO, &CLS_DlgXmlThreeDimTrans::OnBnClickedCheckSetPtzinfo)
END_MESSAGE_MAP()


// CLS_DlgXmlThreeDimTrans message handlers

CString CLS_DlgXmlThreeDimTrans::IntToCStr(int _iNum)
{
	CString strNum;
	strNum.Format(_T("%d"), _iNum);
	return strNum;
}

BOOL CLS_DlgXmlThreeDimTrans::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();
	m_lstTransResult.SetExtendedStyle(m_lstTransResult.GetExtendedStyle()|LVS_EX_GRIDLINES|LVS_EX_FULLROWSELECT);

	m_lstTransResult.InsertColumn(0, GetTextByLan(_T("序号"), _T("index")), LVCFMT_CENTER, 100);
	m_lstTransResult.InsertColumn(1, GetTextByLan(_T("处理结果"), _T("ResultCode")), LVCFMT_CENTER, 80);
	m_lstTransResult.InsertColumn(2, GetTextByLan(_T("X"), _T("X")), LVCFMT_CENTER, 80);
	m_lstTransResult.InsertColumn(3, GetTextByLan(_T("Y"), _T("Y")), LVCFMT_CENTER, 80);

	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}


void CLS_DlgXmlThreeDimTrans::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
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
	//UI_UpdateInfo();
}

void CLS_DlgXmlThreeDimTrans::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialogText();
}

void CLS_DlgXmlThreeDimTrans::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_STATIC_SRC_CHANNEL,GetTextByLan(_T("源通道号"), _T("Src Channel")));
	SetDlgItemText(IDC_STATIC_PAN,GetTextByLan(_T("Pan"), _T("Pan")));
	SetDlgItemText(IDC_STATIC_TILT,GetTextByLan(_T("TiTl"), _T("Titl")));
	SetDlgItemText(IDC_STATIC_ZOOM,GetTextByLan(_T("Zoom"), _T("Zoom")));
	SetDlgItemText(IDC_STATIC_DES_CHANNEL,GetTextByLan(_T("目的通道号"), _T("Des Channel")));
	SetDlgItemText(IDC_STATIC_COUNT,GetTextByLan(_T("数目"), _T("Count")));
	SetDlgItemText(IDC_STATIC_CUR,GetTextByLan(_T("序号"), _T("Index")));
	SetDlgItemText(IDC_STATIC_X,GetTextByLan(_T("x坐标"), _T("X")));
	SetDlgItemText(IDC_STATIC_Y,GetTextByLan(_T("y坐标"), _T("Y")));
	SetDlgItemText(IDC_STATIC_TOTALFILE,GetTextByLan(_T("总数"), _T("TotalSize")));

}
void CLS_DlgXmlThreeDimTrans::UI_UpdateInfo()
{
	XmlThreeDimTransCondition tXmlThreeDimTransCondition = {0};
	XmlThreeDimTransResult tXmlThreeDimTransResult = {0};

	UpdateData(TRUE);

	tXmlThreeDimTransCondition.iChannelNo = m_iSrcChannel;
	tXmlThreeDimTransCondition.iAimChannelNo = m_iDesChannel;
	tXmlThreeDimTransCondition.iPan = m_iPan;
	tXmlThreeDimTransCondition.iTilt = m_iTitl;
	tXmlThreeDimTransCondition.iZoom = m_iZoom;
	tXmlThreeDimTransCondition.iCount = m_iCount;
	tXmlThreeDimTransCondition.iPtzSet = m_iSetPtzInfo;
	tXmlThreeDimTransCondition.uiRecvTimeOut = 3000;
	
	memcpy(&tXmlThreeDimTransCondition.tTPoint,&m_tXmlThreeDimTransCondition.tTPoint,sizeof(tXmlThreeDimTransCondition.tTPoint));

	int iRet = NetClient_XmlCmdConfig(m_iLogonID,NETXMLCMD_THREEDIMTRANS,&tXmlThreeDimTransCondition,sizeof(tXmlThreeDimTransCondition),
		&tXmlThreeDimTransResult,sizeof(XmlThreeDimTransResult));

	if(RET_SUCCESS == iRet)
	{
		AddDataToLst(tXmlThreeDimTransResult);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlCmdConfig[NETXMLCMD_THREEDIMTRANS] (%d)",m_iLogonID);
	}
}

void CLS_DlgXmlThreeDimTrans::AddDataToLst(XmlThreeDimTransResult &tXmlThreeDimTransResult)
{
	m_lstTransResult.DeleteAllItems();
	for (int i = 0; i <  tXmlThreeDimTransResult.iCount; i++)
	{
		m_lstTransResult.InsertItem(i, (LPCTSTR)IntToCStr(i+1));
		m_lstTransResult.SetItemText(i, 1, (LPCTSTR)(tXmlThreeDimTransResult.tTransResultPoint[i].cResult));
		m_lstTransResult.SetItemText(i, 2, (LPCTSTR)IntToCStr(tXmlThreeDimTransResult.tTransResultPoint[i].iX));
		m_lstTransResult.SetItemText(i, 3, (LPCTSTR)IntToCStr(tXmlThreeDimTransResult.tTransResultPoint[i].iY));

	}
	m_iCountResult = tXmlThreeDimTransResult.iCount;
	UpdateData(FALSE);
}


void CLS_DlgXmlThreeDimTrans::OnBnClickedButtonQuery()
{
	// TODO: Add your control notification handler code here
	UI_UpdateInfo();
}


void CLS_DlgXmlThreeDimTrans::OnEnChangeEditCount()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	m_cboIndex.ResetContent();
	for(int i = 0; i < m_iCount && i < MAX_3D_TRANSFROM_NUM; i++)
	{
		CString str;
		str.Format("%d",i);
		m_cboIndex.AddString(str);
	}
}

void CLS_DlgXmlThreeDimTrans::OnCbnSelchangeComboNo()
{
	// TODO: Add your control notification handler code here
	int index = m_cboIndex.GetCurSel();
	m_iX = m_tXmlThreeDimTransCondition.tTPoint[index].iX;
	m_iY = m_tXmlThreeDimTransCondition.tTPoint[index].iY;
	UpdateData(FALSE);
}

void CLS_DlgXmlThreeDimTrans::OnEnChangeEditX()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	m_tXmlThreeDimTransCondition.tTPoint[m_cboIndex.GetCurSel()].iX = m_iX;
}

void CLS_DlgXmlThreeDimTrans::OnEnChangeEditY()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	m_tXmlThreeDimTransCondition.tTPoint[m_cboIndex.GetCurSel()].iY = m_iY;
}

void CLS_DlgXmlThreeDimTrans::OnBnClickedCheckSetPtzinfo()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	if(USE_SET_PTZINFO == m_iSetPtzInfo)
	{
		GetDlgItem(IDC_EDIT_SRC_CHANNEL3)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_SRC_CHANNEL4)->EnableWindow(TRUE);
		GetDlgItem(IDC_EDIT_SRC_CHANNEL5)->EnableWindow(TRUE);
	}
	else
	{
		GetDlgItem(IDC_EDIT_SRC_CHANNEL3)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_SRC_CHANNEL4)->EnableWindow(FALSE);
		GetDlgItem(IDC_EDIT_SRC_CHANNEL5)->EnableWindow(FALSE);
	}
}
