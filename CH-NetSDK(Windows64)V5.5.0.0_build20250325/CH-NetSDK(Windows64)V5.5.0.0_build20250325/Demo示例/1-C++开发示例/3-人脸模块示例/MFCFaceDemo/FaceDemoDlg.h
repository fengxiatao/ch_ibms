
#pragma once
#include "afxwin.h"
#include "afxcmn.h"
#include "CLS_PageBase.h"

enum CONFIG_PAGE_INDEX
{
	PAGE_MIN = 0,
	PAGE_FACE_LIB = PAGE_MIN,
	PAGE_FACE_PIC,	
	PAGE_FACE_FEATURE,	
	PAGE_FACE_SCHEDULE,	
	PAGE_FACE_ALARM,
	PAGE_FACE_SEARCH,
	PAGE_FACE_STREAM,
	PAGE_FACE_ADVANCE,
	PAGE_FACE_ALARM_LINK,
	PAGE_FACE_SEARCH_SNAP,
	PAGE_FACE_DETECTION,
	PAGE_FACE_LIB_SYNC,
	PAGE_MAX
};

typedef union tagParamChangeNotifyParam
{
	int					iEnable;	
	FaceLibSyncResult	tFaceLibSyncResult;
}ParamChangeNotifyParam, *pParamChangeNotifyParam;

struct _PARAMCHANGE_NOTIFY_DATA 
{
	int m_iLogonID;
	int m_iChannelNo;
	PARATYPE m_iParaType;
	STR_Para m_pPara;
	int m_iUserData;
	ParamChangeNotifyParam m_utParam;
};

class CFaceDemoDlg : public CDialog
{
// Construction
public:
	CFaceDemoDlg(CWnd* pParent = NULL);	// Standard constructor

// Dialog data
	enum { IDD = IDD_DEMOFACE_DIALOG };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	// Implementation
protected:
	HICON m_hIcon;
	// Generated message mapping function
	virtual BOOL OnInitDialog();
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

public:
	CEdit m_edtPort;
	CEdit m_edtIpAddr;
	CEdit m_edtUserName;
	CEdit m_edtPassword;
	CComboBox m_cboChanNo;
	CTreeCtrl m_treeConfig;

	int		m_iLogonId;

	void SDK_Init();

	void UI_Init();
	void UI_InitTree(int _iDevType);
	void UI_EnableLogon(BOOL _blEnbale);
	void UI_EnableConfig(BOOL _blEnbale);
	

	void ShowPage(int _iIndex);

private:
	CLS_PageBase* m_pPage[PAGE_MAX];
	CLS_PageBase* m_pCurPage;
	int	m_iPageIndex;

public:
	static void MainNotify(int _iLogonId, long _iWparam, void* _iParam, void* _iUser); 
	LRESULT OnMainNotify(WPARAM wp, LPARAM lp);

	static void ParamChangeNotify( int _iLogonID, int _iChan, PARATYPE _iParaType,STR_Para* _pPara,void* _iUser );
	LRESULT OnParamChangeNotify( WPARAM wParam, LPARAM lParam );

	virtual void OnOK();
	virtual void OnCancel();	
	afx_msg void OnClose();
	afx_msg void OnTvnSelchangedTreeConfig(NMHDR *pNMHDR, LRESULT *pResult);
	afx_msg void OnBnClickedBtnLogon();
	afx_msg void OnBnClickedBtnLogoff();
	afx_msg void OnCbnSelchangeCboChanno();
	int GetFaceAbility();
private:
	CButton m_rdoNormalMode;
	CButton m_rdoActiveMode;
	CEdit m_edtWanPort;
	int m_iLogonMode;
public:
	afx_msg void OnBnClickedRadioNormalMode();
	afx_msg void OnBnClickedRadioActiveMode();	
};
