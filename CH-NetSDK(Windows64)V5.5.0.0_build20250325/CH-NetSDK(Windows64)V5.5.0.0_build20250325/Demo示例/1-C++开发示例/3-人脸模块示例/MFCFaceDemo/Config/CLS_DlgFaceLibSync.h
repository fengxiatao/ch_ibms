#pragma once

#include "CLS_PageBase.h"
#include "afxcmn.h"
#include "afxwin.h"

// CLS_DlgFaceLibSync Dialog

#define LIBSYNC_QUERY_CHANNEL_NUM	 320	//The maximum number of channels to obtain the library synchronization status is. The device does not support the acquisition of a single channel, and the maximum number of NVR channels is 320 temporarily


struct VectorSyncResult 
{
	vector<FaceLibSyncResult*> vecInfo;
};

struct InnerSyncResult 
{
	VectorSyncResult tResult[FACE_MAX_KEY_COUNT][LIBSYNC_QUERY_CHANNEL_NUM] ;
};

class CLS_DlgFaceLibSync : public CLS_PageBase
{
	DECLARE_DYNAMIC(CLS_DlgFaceLibSync)

public:
	CLS_DlgFaceLibSync(CWnd* pParent = NULL);   // Standard Constructors
	virtual ~CLS_DlgFaceLibSync();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_FACE_LIB_SYNC };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUser);
	void OnLogoff();

	void UI_Init();
	void UI_UptateData();

	void UI_UpdataSyncInfo();
	void FaceLibSync(int _iCmd);

	void UpdateFaceLibSyncResult(FaceLibSyncResult* _tInfo);
	void UI_UpdataSyncResult();

	void ReleaseFaceLibSyncResult(int _iLibkeySel);
	
	afx_msg void OnBnClickedBtnLibStartSync();
	afx_msg void OnBnClickedBtnLibStopSync();
	afx_msg void OnBnClickedBtnLibDeltSync();
	afx_msg void OnBnClickedBtnLibDeltSyncLib();
	afx_msg void OnBnClickedBtnLibRefeshSync();
	afx_msg void OnBnClickedBtnLibClearFailInfo();

	afx_msg void OnCbnSelchangeCboSyncLibKey();
	afx_msg void OnNMClickLstLibSyncInfo(NMHDR *pNMHDR, LRESULT *pResult);

	CListCtrl m_lstLibSyncInfo;
	CComboBox m_cboLibKey;
	
	InnerSyncResult m_tSyncResult;
	
	
	
	afx_msg void OnBnClickedBtnLibStartSyncAddto();
};
