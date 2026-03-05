// CLS_DlgDecrpt.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "BaseWindow.h"
#include "CLS_DlgDecrpt.h"


// CLS_DlgDecrpt dialog

IMPLEMENT_DYNAMIC(CLS_DlgDecrpt, CDialog)

CLS_DlgDecrpt::CLS_DlgDecrpt(CWnd* pParent /*=NULL*/)
	: CDialog(CLS_DlgDecrpt::IDD, pParent)
	, m_csPsw(_T(""))
{

}

CLS_DlgDecrpt::~CLS_DlgDecrpt()
{
}

void CLS_DlgDecrpt::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_PSW, m_csPsw);
}


BEGIN_MESSAGE_MAP(CLS_DlgDecrpt, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_DECRYPT, &CLS_DlgDecrpt::OnBnClickedButtonDecrypt)
	ON_WM_CTLCOLOR()
END_MESSAGE_MAP()


// CLS_DlgDecrpt message handlers

void CLS_DlgDecrpt::OnBnClickedButtonDecrypt()
{
	UpdateData(TRUE);

	TVideoDecrypt tDecrypt = {0};
	tDecrypt.iChannel = m_iChannelNO;
	tDecrypt.iStreamNo = m_iStreamNO;
	strcpy_s(tDecrypt.cDecryptKey,sizeof(tDecrypt.cDecryptKey),(LPCSTR)(LPCTSTR)m_csPsw);

	int iRet = NetClient_SetVideoDecrypt(m_iLogonID, m_iChannelNO, &tDecrypt, sizeof(tDecrypt));
	if (0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_SetVideoDecrypt(%d,%d)",m_iLogonID,m_iChannelNO);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_SetVideoDecrypt(%d,%d)",m_iLogonID,m_iChannelNO);
	}

	ShowWindow(SW_HIDE);
}

HBRUSH CLS_DlgDecrpt::OnCtlColor(CDC* pDC, CWnd* pWnd, UINT nCtlColor)
{
	HBRUSH hbr = CDialog::OnCtlColor(pDC, pWnd, nCtlColor);

	if(IDC_STATIC == pWnd-> GetDlgCtrlID())
	{
		pDC->SetTextColor(RGB(255,0,0)); 
	}
	if ((CTLCOLOR_DLG == nCtlColor) || (CTLCOLOR_STATIC == nCtlColor))
	{
		pDC->SetBkColor(RGB(0,0,0)); 
		return CreateSolidBrush(RGB(0,0,0));
	}
	return hbr;
}
