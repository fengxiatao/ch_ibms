#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"
#include "afxdtctl.h"
#include "afxcmn.h"
#include <list>

using namespace std;

class CLS_DlgFacePic : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFacePic)

public:
	CLS_DlgFacePic(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFacePic();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_PIC };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()

public:
	//Internal member variable
	FaceQuery		m_tQueryInfo;
	FaceQueryResult	m_tFaceInfo[FACE_MAX_PAGE_COUNT];
	int				m_iCurPage;
	int				m_iTolalPage;
	//Image download, import and export
	void*			m_pDlgPicEdit;
	BOOL			m_blInportThread;		//Import thread status
	list <CString>	m_listInportFacePic;	//Import picture path
	void*			m_pDlgPreocess;
	list <FaceInfo>	m_listDLFacePic;		//Export picture linked list
	CString			m_cstrDLPicPath;		//Export picture saving path

public:	
	//Control Variables
	CEdit			m_edtCardNum;
	CEdit			m_edtFaceName;
	CStatic			m_stcPageShow;
	CComboBox		m_cboLibKey;
	CComboBox		m_cboSex;
	CComboBox		m_cboCardType;
	CComboBox		m_cboModelStatus;
	CComboBox		m_cboPage;
	CListCtrl		m_lstFaceInfo;
	CComboBox	    m_cboCountry;
	CEdit			m_edtCompany;
	CEdit			m_edtAddress;

	void			UI_Init();
	void			UI_UptateData();
	void			UI_ShowPage(int _iPageNo);
	void			UI_UpdateFaceList(FaceInfo& _tInfo, int _iLibIndex=-1);

	//Import and export pictures
	void			OnTheadInPort();
	void			StartDownloadNextFacePic(int iRet);
	
public:
	afx_msg void OnBnClickedBtnPicQuery();
	afx_msg void OnBnClickedBtnPicAdd();
	afx_msg void OnBnClickedBtnPicModify();
	afx_msg void OnBnClickedBtnPicDelete();
	afx_msg void OnNMDblclkLstPicInfo(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnPicPageFirst();
	afx_msg void OnBnClickedBtnPicPagePre();
	afx_msg void OnBnClickedBtnPicPageNext();
	afx_msg void OnBnClickedBtnPicPageLast();
	afx_msg void OnCbnSelchangeCboPicPage();
	afx_msg void OnBnClickedBtnPicInport();
	afx_msg void OnBnClickedBtnPicOutportAll();
};
