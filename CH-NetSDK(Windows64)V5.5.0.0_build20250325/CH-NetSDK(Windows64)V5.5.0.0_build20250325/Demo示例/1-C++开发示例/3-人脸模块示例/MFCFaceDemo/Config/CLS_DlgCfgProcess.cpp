
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgCfgProcess.h"
#include "CommonFun.h"

IMPLEMENT_DYNAMIC(CLS_DlgCfgProcess, CDialog)

CLS_DlgCfgProcess::CLS_DlgCfgProcess(CWnd* pParent /*=NULL*/)
	: CDialog(CLS_DlgCfgProcess::IDD, pParent)
{
	m_iTotalCount = 0;
	m_iFailCount = 0;
	m_iSuccCount = 0;
	m_iLeftCount = 0;
	m_iDlgType = 0;
	m_iPos = 0;
}

CLS_DlgCfgProcess::~CLS_DlgCfgProcess()
{
}

void CLS_DlgCfgProcess::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_PROGRESS_EXPORT, m_process);
}


BEGIN_MESSAGE_MAP(CLS_DlgCfgProcess, CDialog)
	ON_WM_SHOWWINDOW()
END_MESSAGE_MAP()

void CLS_DlgCfgProcess::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CDialog::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		ShowReslt();
	}
}

void CLS_DlgCfgProcess::SetResult(int _blRet)
{
	if (DLG_TYPE_LIB_INPORT == m_iDlgType)
	{
		m_iPos = _blRet;
	}
	else 
	{
		if (_blRet)
		{
			m_iSuccCount ++;
		}
		else
		{
			m_iFailCount ++;
		}
	}

	ShowReslt();	
}

void CLS_DlgCfgProcess::ShowReslt()
{
	if (DLG_TYPE_LIB_INPORT == m_iDlgType)
	{
		int iPos = m_iPos;
		if (iPos < 0)
		{
			iPos = 0;
		}
		m_process.SetPos(iPos);
		SetDlgItemText(IDC_STC_EXPORT_PROCESS, (CString)(IntToStr(iPos) + "%"));

		if (100 == m_iPos)
		{
			m_iSuccCount = 1;
		}
		else if (-1 == m_iPos)
		{
			m_iFailCount = 1;
		}

		CString cstrRet;
		cstrRet.Format(_T("success: %d, fail: %d, total: %d"), m_iSuccCount, m_iFailCount, (int)m_iTotalCount);
		SetDlgItemText(IDC_STC_EXPORT_RESULT, cstrRet);	
	}
	else
	{
		int iPos = 0;
		if (m_iTotalCount > 0)
		{
			iPos = (int)((m_iTotalCount - m_iLeftCount)*100/m_iTotalCount);
		}
		m_process.SetPos(iPos);
		SetDlgItemText(IDC_STC_EXPORT_PROCESS, (CString)(IntToStr(iPos) + "%"));

		CString cstrRet;
		cstrRet.Format(_T("success: %d, fail: %d, total: %d"), m_iSuccCount, m_iFailCount, (int)m_iTotalCount);
		SetDlgItemText(IDC_STC_EXPORT_RESULT, cstrRet);	
	}
}
