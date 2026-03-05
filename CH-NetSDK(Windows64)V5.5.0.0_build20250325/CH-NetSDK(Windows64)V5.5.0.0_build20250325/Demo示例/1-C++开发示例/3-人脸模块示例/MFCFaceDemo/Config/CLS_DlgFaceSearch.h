#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"
#include "afxcmn.h"

class CLS_DlgFaceSearch : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceSearch)

public:
	CLS_DlgFaceSearch(CWnd* pParent = NULL);
	virtual ~CLS_DlgFaceSearch();

	enum { IDD = IDD_DLG_CFG_FACE_SEARCH };

protected:
	virtual void DoDataExchange(CDataExchange* pDX); 

	DECLARE_MESSAGE_MAP()

private:
	CImage*			m_pImageLocal;			//Select pictures locally
	vector<FaceInfo> m_vecFaceInfo;
	void*			m_pDlgPicEdit;

public:
	CComboBox		m_cboLibKey;
	CSliderCtrl 	m_sldSimilar;
	CListCtrl		m_lstFaceInfo;

public:
	void			UI_Init();
	void			UI_UptateData();
	void			StartDownloadNextFacePic(int iRet);

	afx_msg void	OnPaint();
	afx_msg void	OnBnClickedBtnSearchPath();
	afx_msg void	OnNMCustomdrawSldSearchSimilar(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void	OnBnClickedBtnSearchSearch();
	afx_msg void	OnNMDblclkLstSearchInfo(NMHDR *pNMHDR, LRESULT *pResult);
};
