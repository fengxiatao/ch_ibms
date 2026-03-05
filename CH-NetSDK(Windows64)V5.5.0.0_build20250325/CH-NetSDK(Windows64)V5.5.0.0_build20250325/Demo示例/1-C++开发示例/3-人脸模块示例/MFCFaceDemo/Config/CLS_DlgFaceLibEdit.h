#pragma once
#include "afxcmn.h"
#include "CLS_PageBase.h"
#include "TDFilterEdit.h"
#include "afxwin.h"

class CLS_DlgFaceLibEdit : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceLibEdit)

public:
	CLS_DlgFaceLibEdit(CWnd* pParent = NULL);
	virtual ~CLS_DlgFaceLibEdit();

	enum { IDD = IDD_DLG_CFG_FACE_LIB_EDIT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX); 

	DECLARE_MESSAGE_MAP()

	FaceLibInfo		m_tLibInfo;
	BOOL			m_blLocal;//Whether to operate the local face database

public:
	TDFilterEdit	m_edtLibName;
	TDFilterEdit	m_edtDescrip;
	CSliderCtrl		m_sldSimilar;

	void			UI_Init();

	void			SetLibInfo(FaceLibInfo& _tInfo){m_tLibInfo=_tInfo;};

	void			SetLocal(BOOL _blLocal){m_blLocal=_blLocal;};
	
	afx_msg void	OnShowWindow(BOOL bShow, UINT nStatus);
	afx_msg void	OnNMCustomdrawSldFaceLibEditSimilar(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void	OnClose();
	afx_msg void	OnBnClickedBtnFaceLibEditConfirm();
	afx_msg void	OnBnClickedBtnFaceLibEditCancle();
};
