#pragma once
#include "VCAEventBasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


// CLS_VcaDeliverGoods dialog

class CLS_VcaDeliverGoods : public CLS_VCAEventBasePage
{
	DECLARE_DYNAMIC(CLS_VcaDeliverGoods)

public:
	CLS_VcaDeliverGoods(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_VcaDeliverGoods();

// dialog data
	enum { IDD = IDD_DLG_VCAEVENT_DELIVERGOODS };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	void UpdateUIText();
	void UpdatePageUI();
	afx_msg void OnBnClickedBtnDelivergoodsSet();
	
	vca_TPoint    m_tPoints[MAX_VCA_DELIVERGOODS_POINT_NUM];
	afx_msg void OnBnClickedBtnDelivergoodsRegionDraw();
	
	CButton m_chkEventEnable;
	CButton m_chkShowRule;
	CButton m_chkShowAlarmNum;
	CButton m_chkShowTargetBox;
	CComboBox m_cboColor;
	CComboBox m_cboAlarmColor;
	CComboBox m_cboDevType;
	CEdit m_editRegionPoins;
	afx_msg void OnShowWindow(BOOL bShow, UINT nStatus);
};
