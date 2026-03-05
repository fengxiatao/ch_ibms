#pragma once
#include "afxwin.h"


// DualUsersAuth 对话框

//用户信息结构体
typedef struct tagT_UserInfo
{
	CString strUser;
	CString strPwd;
}T_UserInfo, *pT_UserInfo;

class DualUsersAuthDlg : public CDialog
{
	DECLARE_DYNAMIC(DualUsersAuthDlg)

public:
	/*
	iTimes:第几个用户认证（1~2）
	*/
	DualUsersAuthDlg(int iTimes, CWnd* pParent = NULL);   // 标准构造函数
	virtual ~DualUsersAuthDlg();

// 对话框数据
	enum { IDD = IDD_DIG_DUAL_USERS_AUTH };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV 支持

	DECLARE_MESSAGE_MAP()

	virtual BOOL OnInitDialog();

public:
	afx_msg void OnBnClickedOk();
	afx_msg void OnBnClickedCancel();
	void GetUserInfo(T_UserInfo &_tUserInfo);
private:
	CEdit m_editUser;
	CEdit m_editPwd;
	T_UserInfo m_tUserInfo;
	int m_iTimes;
};
