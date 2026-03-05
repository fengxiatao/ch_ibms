#pragma once
#include "afxwin.h"
#include "BasePage.h"
#include "Common/Ini.h"

// CLS_IS_SUPPORT_FUNC dialog

typedef struct _tagCommonUse
{
	int         iValue;
	CString 	cstrCH;
	CString 	cstrEN;
}CommonUse, *pCommonUse;

struct FunAbility
{
	CommonUse m_MainType;
	std::map<int,CommonUse> m_MapSubType;
};
static std::map<int,FunAbility> s_mapFunAbility;

class CLS_IS_SUPPORT_FUNC : public CLS_BasePage
{
	DECLARE_DYNAMIC(CLS_IS_SUPPORT_FUNC)

public:
	CLS_IS_SUPPORT_FUNC(CWnd* pParent = NULL);   // Standard constructor
	virtual ~CLS_IS_SUPPORT_FUNC();
	virtual BOOL OnInitDialog();
	virtual void OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo);
	void ReadFuncFromConfig();

// dialog data
	enum { IDD = IDD_DLG_CFG_FUNC_IS_SUPPORT };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	CComboBox m_cboFuncType;
	afx_msg void OnBnClickedButtonCheck();

	int m_iLogonID;
	int m_iChannelNo;
	int m_iStreamNo;
	CComboBox m_cboSubtype;
	afx_msg void OnCbnSelchangeComboFuncType();
	CEdit m_CEditFuncResult;


	//typedef std::map<int, FuncTypeInfo> MapMainTypeItem; 
	//MapMainTypeItem m_MapMainType;
	//typedef std::map<int, FuncTypeInfo> MapSubTypeItem;
	//MapSubTypeItem m_MapSubType[LEN_64];
	afx_msg void OnCbnSelchangeComboSubtype();
    afx_msg void OnBnClickedButtonCheckAllFunc();
};
