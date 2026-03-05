#pragma once
#include "afxwin.h"
#include ".\Config\Events\VCAEventBasePage.h"
#include "net_sdk_types.h"
#include "afxcmn.h"
#define MAX_COORDINATE_POINT_NUM		10
#define COORDINATE_NUMBER				3

// CLS_VCA_Reference dialog

class CLS_VCA_Reference : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_VCA_Reference)

public:
	CLS_VCA_Reference(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VCA_Reference();

// dialog data
	enum { IDD = IDD_DLG_CFG_VCA_REFERENCE };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	void UI_UpdateUIText();
	void SetEditState(int _iPointNum);
	void ShowPixelPoint();
	void ShowWorldPoint();

	DECLARE_MESSAGE_MAP()
	CString IntToCstr(int _iNum);
public:
	afx_msg void OnBnClickedButtonGetReference();
	afx_msg void OnBnClickedButtonSetReference();
	afx_msg void OnBnClickedButtonSavePointPara();
	CListCtrl m_listPixel;
	CListCtrl m_listWorld;
	// Point type, switch between pixel coordinates and world coordinates
	CComboBox m_cboPointType;
	CComboBox m_cboSceneId;
	CComboBox m_cboPointNum;
	CEdit m_editPicHigh;
	afx_msg void OnStnClickedStaticPointNum();
	afx_msg void OnCbnSelchangeComboPointNum();
	CButton m_btSaveInfo;
	CButton m_btGetInfo;
	CButton m_btSetPara;

private:
	int m_iPointTypeIndex;
	int m_iPointNum;
	int m_iPixelPointX[MAX_COORDINATE_POINT_NUM];		//pixel coordinates
	int m_iPixelPointY[MAX_COORDINATE_POINT_NUM];
	int m_iPixelPointZ[MAX_COORDINATE_POINT_NUM];
	int m_iWorldPointX[MAX_COORDINATE_POINT_NUM];		//world coordinates
	int m_iWorldPointY[MAX_COORDINATE_POINT_NUM];
	int m_iWorldPointZ[MAX_COORDINATE_POINT_NUM];
	CEdit m_editPointX[MAX_COORDINATE_POINT_NUM];
	CEdit m_editPointY[MAX_COORDINATE_POINT_NUM];
	CEdit m_editPointZ[MAX_COORDINATE_POINT_NUM];

	int m_iIpcPara[COORDINATE_NUMBER];
	int m_iCoordinate0Para[COORDINATE_NUMBER];
	int m_iCoordinateXPara[COORDINATE_NUMBER];
	int m_iCoordinateYPara[COORDINATE_NUMBER];
public:
	afx_msg void OnCbnSelchangeComboPointType();
};
