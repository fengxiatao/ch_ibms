// CLS_3DMaskArea.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_3DMaskArea.h"


// CLS_3DMaskArea dialog

IMPLEMENT_DYNAMIC(CLS_3DMaskArea, CDialog)

CLS_3DMaskArea::CLS_3DMaskArea(CWnd* pParent /*=NULL*/)
	: CLS_VCAEventBasePage(CLS_3DMaskArea::IDD, pParent)
{

}

CLS_3DMaskArea::~CLS_3DMaskArea()
{
}

void CLS_3DMaskArea::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TYPE, m_cbo3DMaskType);
	DDX_Control(pDX, IDC_CHECK_ENABLE, m_chkEnable);
	DDX_Control(pDX, IDC_COMBOAREANO, m_cboAreaNo);
	DDX_Control(pDX, IDC_EDIT_POINTINFO, m_edtMaskPoint);
}


BEGIN_MESSAGE_MAP(CLS_3DMaskArea, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BUTTON_Draw, &CLS_3DMaskArea::OnBnClickedButtonDraw)
	ON_BN_CLICKED(IDC_CHECK_ENABLE, &CLS_3DMaskArea::OnBnClickedCheckEnable)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_3DMaskArea::OnBnClickedButtonSet)
	ON_CBN_SELCHANGE(IDC_COMBOAREANO, &CLS_3DMaskArea::OnCbnSelchangeComboareano)
	ON_CBN_SELCHANGE(IDC_COMBO_TYPE, &CLS_3DMaskArea::OnCbnSelchangeComboType)
END_MESSAGE_MAP()


// CLS_3DMaskArea message handlers

void CLS_3DMaskArea::OnBnClickedButtonDraw()
{
	// TODO: Add your control notification handler code here
	int iPointCount = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	GetInfoOnDrawVideo(&iPointCount, cPointBuf, NULL, DrawType_perimeter);
	m_iReferCount = iPointCount;
	POINT tPoints[SDK_MAX_MASKAREA_NUM] = {0}; 
	GetPointsFromString(cPointBuf, m_iReferCount, tPoints);
	CString cstrMsg;
	CString cstrTmp;
	for (int i = 0; i < m_iReferCount && i < MAX_REFERPOINT_NUM; ++i)
	{
		cstrTmp.Format("(%d,%d)", tPoints[i].x, tPoints[i].y);
		cstrMsg += cstrTmp;
	}
	m_edtMaskPoint.SetWindowText(cstrMsg);
}

void CLS_3DMaskArea::GetInfoOnDrawVideo(int* _piPointCount, char* _pcPointsBuf, RECT* _ptRect, int _iDrawType)
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return;
		}
	}
	/*The following code in order to get the corresponding parameters from the draw dialog*/
	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(_iDrawType);
	int iDirection = 0;
	if (NULL != _piPointCount && NULL != _pcPointsBuf)
	{
		m_pDlgVideoView->SetPointRegionParam(_pcPointsBuf, _piPointCount, &iDirection, TRUE);
	}

	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (NULL != _ptRect)
		{
			m_pDlgVideoView->GetPointCoordirate((int*)&_ptRect->left, (int*)&_ptRect->top, (int*)&_ptRect->right, (int*)&_ptRect->bottom);
		}
	}

	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_3DMaskArea::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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
	m_iStreamNO = _iStreamNo;

	UpdateParam();
}

BOOL CLS_3DMaskArea::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();
	UpdateUIText();
	return TRUE;

}

void CLS_3DMaskArea::UpdateUIText()
{
	m_cbo3DMaskType.ResetContent();
	m_cbo3DMaskType.SetItemData(m_cbo3DMaskType.AddString(GetTextByLan(_T("火点"), _T("Fire Point"))), 1);
	m_cbo3DMaskType.SetItemData(m_cbo3DMaskType.AddString(GetTextByLan(_T("烟火"), _T("Smoke"))), 2);
	m_cbo3DMaskType.SetItemData(m_cbo3DMaskType.AddString(GetTextByLan(_T("温度"), _T("Temperature"))), 3);
	m_cbo3DMaskType.SetCurSel(0);

	m_cboAreaNo.ResetContent();
	for (int i = 1;i <= 48; i++)
	{
		CString strAreaNo;
		strAreaNo.Format("%d",i);
		m_cboAreaNo.AddString(strAreaNo);
	}
	m_cboAreaNo.SetCurSel(0);

	SetDlgItemText(IDC_STATIC_MASK, GetTextByLan(_T("屏蔽区域"), _T("MASK Area")));
	SetDlgItemText(IDC_STATIC_TYPE, GetTextByLan(_T("类型"), _T("Type")));
	SetDlgItemText(IDC_STATIC_AREANO, GetTextByLan(_T("区域"), _T("Area")));
	SetDlgItemText(IDC_BUTTON_Draw, GetTextByLan(_T("绘制"), _T("Draw")));
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan(_T("设置"), _T("Set")));
}

void CLS_3DMaskArea::OnBnClickedCheckEnable()
{
	// TODO: Add your control notification handler code here
	int iRet = RET_FAILED;

	MaskAreaEnable tInfo = {0};
	tInfo.iSize = (int)sizeof(MaskAreaEnable);
	tInfo.iMaskType = m_cbo3DMaskType.GetItemData(m_cbo3DMaskType.GetCurSel());
	tInfo.iEnable = m_chkEnable.GetCheck();
	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_3DMASKAREAENABLE, m_iChannelNO, &tInfo, tInfo.iSize);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_3DMaskArea::OnBnClickedCheckEnable] NetClient_VCASetConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_3DMaskArea::OnBnClickedCheckEnable] NetClient_VCASetConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}

void CLS_3DMaskArea::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	int iRet = RET_FAILED;

	MaskAreaPara tInfo = {0};
	tInfo.iSize = (int)sizeof(MaskAreaPara);
	tInfo.iMaskType = m_cbo3DMaskType.GetItemData(m_cbo3DMaskType.GetCurSel());
	tInfo.iAreaNo = m_cboAreaNo.GetCurSel() + 1;
	CString strPoint = "";
	m_edtMaskPoint.GetWindowText(strPoint);
	int i = 0;
	while (strPoint.GetLength() != 0)
	{
		CString strp = "";
		strp = strPoint.Mid(strPoint.Find("("), strPoint.Find(")") + 1);
		strPoint = strPoint.Mid(strp.GetLength());
		sscanf_s((LPSTR)(LPCTSTR)strp, "(%d,%d)"
			, &tInfo.tPoint.stPoints[i].iX, &tInfo.tPoint.stPoints[i].iY);
		i++;
	}
	tInfo.tPoint.iPointNum = i;

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_3DMASKAREAPARA, m_iChannelNO, &tInfo, tInfo.iSize);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_3DMaskArea::OnBnClickedButtonSet] NetClient_VCASetConfig Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_3DMaskArea::OnBnClickedButtonSet] NetClient_VCASetConfig failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}

void CLS_3DMaskArea::UpdateParam()
{
	int iRet = RET_FAILED;

	MaskAreaPara tInfo = {0};
	tInfo.iSize = (int)sizeof(MaskAreaPara);
	tInfo.iMaskType = m_cbo3DMaskType.GetItemData(m_cbo3DMaskType.GetCurSel());
	tInfo.iAreaNo = m_cboAreaNo.GetCurSel() + 1;

	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_3DMASKAREAPARA, m_iChannelNO, &tInfo, tInfo.iSize);
	if (RET_SUCCESS == iRet)
	{
		CString strTotal = "";
		for (int i = 0; i < tInfo.tPoint.iPointNum; i++)
		{
			CString strPoint = "";
			strPoint.Format("(%d,%d)", tInfo.tPoint.stPoints[i].iX, tInfo.tPoint.stPoints[i].iY);
			strTotal += strPoint;
		}
		m_edtMaskPoint.SetWindowText(strTotal);
		AddLog(LOG_TYPE_SUCC,"","[CLS_3DMaskArea::OnBnClickedButtonGet]  Get VCA_CMD_3DMASKAREAPARA Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_3DMaskArea::OnBnClickedButtonGet] Get VCA_CMD_3DMASKAREAPARA failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}

	MaskAreaEnable tParam = {0};
	tParam.iSize = (int)sizeof(MaskAreaPara);
	tParam.iMaskType = m_cbo3DMaskType.GetItemData(m_cbo3DMaskType.GetCurSel());

	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_3DMASKAREAENABLE, m_iChannelNO, &tParam, tParam.iSize);
	m_chkEnable.SetCheck(tParam.iEnable);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_3DMaskArea::OnBnClickedButtonGet] VCA_CMD_3DMASKAREAENABLE Success! logonID(%d),m_iChNo(%d)"
			,m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_3DMaskArea::OnBnClickedButtonGet] VCA_CMD_3DMASKAREAENABLE failed! logonID(%d),m_iChNo(%d),_iLinkNo(%d),error(%d)"
			,m_iLogonID,m_iChannelNO,GetLastError());
	}
}

void CLS_3DMaskArea::SetVCAStatus(BOOL _bStatus)
{
	if(_bStatus)
	{
		if (-1 == m_iLogonID)
		{
			AddLog(LOG_TYPE_FAIL, "", "CLS_VCAEventPage::failed logonID(%d)", m_iLogonID);
			return;
		}

		int iStatus = 0;
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &iStatus, sizeof(int));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig failed logonID(%d)", m_iLogonID);
		}
	}
	else
	{
		int iStatus = 1;
		int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &iStatus, sizeof(int));
		if (iRet < 0)
		{
			AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig failed logonID(%d)", m_iLogonID);
		}
	}
}

void CLS_3DMaskArea::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	switch(LOWORD(_wParam))
	{
	case WCM_VCA_SUSPEND:
		{
			int iChannelNo = HIWORD(_wParam);
			if (iChannelNo == m_iChannelNO)
			{
				VCASuspendResult tParam = {0};
				tParam.iBufSize = sizeof(VCASuspendResult);
				int iRetBytes = 0;
				int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &tParam, sizeof(tParam), &iRetBytes);
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

void CLS_3DMaskArea::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	SetVCAStatus(bShow);
}



void CLS_3DMaskArea::OnCbnSelchangeComboareano()
{
	// TODO: Add your control notification handler code here
	UpdateParam();
}

void CLS_3DMaskArea::OnCbnSelchangeComboType()
{
	// TODO: Add your control notification handler code here
	UpdateParam();
}
