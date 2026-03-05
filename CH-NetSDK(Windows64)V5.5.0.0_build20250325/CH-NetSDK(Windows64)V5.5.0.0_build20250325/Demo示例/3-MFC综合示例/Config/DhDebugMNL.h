#pragma once
#include "afxwin.h"
#include "NetClientTypes.h"

typedef struct __tagAnalogTest
{	
	int iType;
	int iData; 
}AnalogTest;

typedef struct __tagAnalogInfo
{
	char	cUnit[LEN_16];		//unit
	int		iCollectTime;		//collection interval
	int		iRangeUpLevel;		// upper limit of range
	int		iRangeUpValue;		// upper limit of range
	int		iRangeDownLevel;	// lower limit of range
	int		iRangeDownValue;	// lower limit of range
	int		iAlarmUpLevel;		//Alarm upper limit
	int		iAlarmDownLevel;	// alarm lower limit
	int		iDisAlarmUpLevel;	// alarm upper limit
	int		iDisAlarmDownLevel;	//lower alarm limit
}AnalogInfo;

// CLS_DhDebugMNL dialog

class CLS_DhDebugMNL : public CDialog
{
	DECLARE_DYNAMIC(CLS_DhDebugMNL)

public:
	CLS_DhDebugMNL(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_DhDebugMNL();

// dialog data
	enum { IDD = IDD_DLG_CFG_DH_DEBUG_MNL };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	int m_iLogonID;
	int m_iInportNo;
	CSpinButtonCtrl m_spinDelayTime;
	virtual BOOL OnInitDialog();
	BOOL Init(int _iLogonID, int _iInportNo);
	BOOL UpdateDialog();
	afx_msg void OnBnClickedBtnMnlSet();

	BOOL StructToString(AnalogTest* _pSrc,CString& _strDest);
	//BOOL StringToStruct(CString _strSrc,AnalogTest* _pDest);
	BOOL StringToStruct(CString _strSrc,AnalogInfo* _pDest);
};
