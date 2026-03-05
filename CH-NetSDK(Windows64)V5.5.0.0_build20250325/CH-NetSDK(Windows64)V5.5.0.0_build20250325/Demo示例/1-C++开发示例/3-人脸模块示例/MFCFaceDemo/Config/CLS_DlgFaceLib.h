
#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "CLS_PageBase.h"

class CLS_DlgFaceLib : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceLib)

public:
	CLS_DlgFaceLib(CWnd* pParent = NULL);
	virtual ~CLS_DlgFaceLib();

	enum { IDD = IDD_DLG_CFG_FACE_LIB };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);

	DECLARE_MESSAGE_MAP()

	map<int, FaceLibInfo> m_mapFaceLibInfo;

public:
	CListCtrl	m_lstLibInfo;
	CStatic		m_stcLibSPCount;

	void 		UI_Init();
	void 		UI_UptateData();
	void 		UI_UpdataList();

public:
	afx_msg void OnBnClickedBtnLibAdd();
	afx_msg void OnBnClickedBtnLibModify();
	afx_msg void OnBnClickedBtnLibDelete();
	afx_msg void OnNMDblclkLstLibInfo(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnLibModifyIpc();
	afx_msg void OnBnClickedButtonLibClear();
};
