
#pragma once
#include "afxcmn.h"
#include "afxwin.h"

typedef enum{
	DLG_TYPE_FACE_INPORT = 0,
	DLG_TYPE_FACE_OUTPORT,
	DLG_TYPE_LIB_INPORT,
	DLG_TYPE_LIB_OUTPORT,
}DLG_TYPE_FACE;

class CLS_DlgCfgProcess : public CDialog
{
	DECLARE_DYNAMIC(CLS_DlgCfgProcess)

public:
	CLS_DlgCfgProcess(CWnd* pParent = NULL);
	virtual ~CLS_DlgCfgProcess();

	enum { IDD = IDD_DLG_CFG_FACE_PROCESS };

	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()

public:
	CProgressCtrl m_process;

	double	m_iTotalCount;
	int		m_iSuccCount;
	int		m_iFailCount;
	double	m_iLeftCount;
	int		m_iDlgType;
	int		m_iPos;

	void	SetTotalCount(double _iCount){m_iTotalCount = _iCount;};
	void	SetLeftCount(double _iCount){m_iLeftCount = _iCount;};
	void	SetDealIndex(double _iIndex){m_iLeftCount = m_iTotalCount - _iIndex;};
	void	SetResult(int _blRet);
	void	SetDlgType(int _iType){m_iDlgType = _iType;};
	void	ShowReslt();
};
