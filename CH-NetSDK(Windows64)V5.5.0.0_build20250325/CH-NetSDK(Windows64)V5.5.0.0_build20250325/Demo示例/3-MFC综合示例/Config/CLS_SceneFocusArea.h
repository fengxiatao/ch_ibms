#pragma once
#include "../BasePage.h"
#include "afxwin.h"
#include "Events/VideoViewForDraw.h"


// CLS_SceneFocusArea dialog

class CLS_SceneFocusArea : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_SceneFocusArea)

public:
	CLS_SceneFocusArea(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_SceneFocusArea();

// dialog data
	enum { IDD = IDD_DIG_CFG_VCA_SCENE_FOCUSAREA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
    virtual BOOL OnInitDialog();
    virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
    virtual void OnLanguageChanged(int _iLanguage);
    virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
    virtual void OnMainNotify( int _iLogonID,int _wParam, void* _lParam, void*_iUserData );
    void addAreaToList(char* cPointBuf);
    CLS_VideoViewForDraw* m_pDlgVideoView;
    CEdit m_edtDarwPoint;
    TInterestedArea m_tCurrentInterestRect;

    void UpdatePageUI();
    void InitPageUI();
    int m_iLogonID;
    int m_iChannelNo;
    afx_msg void OnBnClickedButtonSet();
    afx_msg void OnBnClickedButtonDraw();

    CComboBox m_comboScene;
    CComboBox m_comboAlgType;
    CComboBox m_comboFocusAreaNum;
    CListBox  m_listPoints;
    CButton   m_chkEnable;

    int		m_iLeft;	//Left margin - X coordinate of upper left corner
    int		m_iTop;	//Top margin - Y coordinate of upper left corner
    int		m_iRight;	//Right margin - X coordinate of lower right corner
    int		m_iBottom;	//Bottom margin - Y coordinate of the lower right corner

    afx_msg void OnBnClickedButtonClearlist();
    int                 m_stAreasNum;
    vca_TPolygonEx		m_stAreas[MAX_VCA_FOCUS_AREA];
};
