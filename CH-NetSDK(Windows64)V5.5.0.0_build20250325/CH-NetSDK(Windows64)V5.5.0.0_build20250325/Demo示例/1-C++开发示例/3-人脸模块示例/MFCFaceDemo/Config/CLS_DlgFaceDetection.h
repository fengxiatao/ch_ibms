#pragma once
#include "CLS_PageBase.h"
#include "afxwin.h"
#include "afxcmn.h"

// CLS_DlgFaceDetection Dialog

class CLS_DlgFaceDetection : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceDetection)

public:
	CLS_DlgFaceDetection(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceDetection();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_DETECTION };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
private:
	
	void UI_Update();
	int GetAnyScene();
	int SetAnyScene();
	int GetFaceDetect();
	int SetFaceDetect();
	int GetBigPicUploadParam();
	int SetBigPicUploadParam();
	int GetSmallPicUploadParam();
	int SetSmallPicUploadParam();

public:
	
// 	int		m_iLogonID;
// 	int		m_iChannelNo;
// 	int		m_iStreamNo;
// 	int		m_iChanCount;

	//virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);

	void			UI_Init();
	void			UI_UptateData();

	CComboBox m_cboTTCL;
	CComboBox m_cboZPCS;
	CComboBox m_cboZPMS;
	CSliderCtrl m_sldZHZL;
	CEdit m_edtZXRLCC;
	CButton m_chkEnableFaceDetect;

	

	afx_msg void OnBnClickedButtonSet();
	afx_msg void OnNMCustomdrawSliderZhzl(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeComboZpms();
	CComboBox m_cboDetectType;
	afx_msg void OnCbnSelchangeComboFaceDetectState();
	CSliderCtrl m_sldBright;
	CButton m_chkShowRule;
	CButton m_chkShowTarget;
	CSliderCtrl m_sldSnapSpace;
	CSliderCtrl m_sldBigPicQuality;
	CSliderCtrl m_sldSmallPicQuality;
	CButton m_chkSnapBigPic;
	CButton m_chkBigPicOsd;
	afx_msg void OnNMCustomdrawSliderZpjg(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderRlbgld(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderFacedetectBigpic(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnNMCustomdrawSliderFacedetectSmallpic(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnCbnSelchangeComboTtcl();
};
