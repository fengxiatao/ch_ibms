#pragma once
#include ".\Config\Events\VCAEventBasePage.h"
#include "afxwin.h"


// CLS_3DMaskArea dialog

class CLS_3DMaskArea : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_3DMaskArea)

public:
	CLS_3DMaskArea(CWnd* pParent = NULL);   // standard constructor
	virtual ~CLS_3DMaskArea();

// Dialog Data
	enum { IDD = IDD_DIALOG_3DMASK };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedButtonDraw();
	CComboBox m_cbo3DMaskType;
	CButton m_chkEnable;
	CComboBox m_cboAreaNo;
	CEdit m_edtMaskPoint;
	int m_iReferCount;
	void GetInfoOnDrawVideo(int* _piPointCount, char* _pcPointsBuf, RECT* _ptRect, int _iDrawType);
	void UpdateUIText();
	void UpdateParam();
public:
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	virtual void OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser);
	virtual BOOL OnInitDialog();
	afx_msg void OnBnClickedCheckEnable();
	afx_msg void OnBnClickedButtonSet();
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
	void SetVCAStatus(BOOL _bStatus);
	afx_msg void OnCbnSelchangeComboareano();
	afx_msg void OnCbnSelchangeComboType();
};
