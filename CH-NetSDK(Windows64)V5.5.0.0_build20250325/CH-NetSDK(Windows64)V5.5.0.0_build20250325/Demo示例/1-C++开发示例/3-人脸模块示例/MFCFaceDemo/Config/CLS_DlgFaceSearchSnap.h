#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"
#include "afxcmn.h"
#include "afxdtctl.h"

// CLS_DlgFaceSearchSnap Dialog

class CLS_DlgFaceSearchSnap : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceSearchSnap)

public:
	CLS_DlgFaceSearchSnap(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceSearchSnap();

	void UI_Init();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_SEARCH_SNAP };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()


public:
	afx_msg void OnBnClickedBtnSearchSnapPath();
	afx_msg void OnBnClickedBtnSearchSnapCut();
	afx_msg void OnBnClickedButtonQuery();
	afx_msg void OnBnClickedButtonProcess();
	afx_msg void OnBnClickedButtonResult();
	afx_msg void OnNMCustomdrawSldSearchSnapSimilar(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedCheckChanAll();
	afx_msg void OnCbnSelchangeCboSearchSnapCutret();

	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);

	void OnMainNotify(int _iLogonID, int _wParam, void* _iLParam);
	void UI_InitFaceList(CListCtrl& _lst);	
	void UI_UpdateList(FaceSearchSnapResult &_tResult);
	int GetChanList(QueryChanNo* _pList, int _iMaxCount);

private:
	int				m_iTaskId;
	int				m_iQueryProcess;
	unsigned int	m_iDLFacePicId;

	CString			m_cstrDLPath;
	CStatic			m_stcProcessQuery;
	char			m_cFileName[LEN_256];
	CSliderCtrl	    m_sldSimlarity;
	CComboBox		m_cboSortMode;
	CDateTimeCtrl	m_dtBegTime;
	CDateTimeCtrl	m_dtEndTime;
	CButton			m_chkChanAll;
	CButton			m_chkChanNo[LEN_32];
	QueryChanNo		m_tQueryChan[MAX_QUERY_LIST_COUNT];
	CListCtrl		m_lstSearchSnapResult;
	CStatic			m_stcPicShow;
	CEdit			m_edtPicPath;
	CComboBox		m_cboCutResult;
	CImage*			m_pImageLocal;			//Select pictures locally
public:
	afx_msg void OnPaint();
};
