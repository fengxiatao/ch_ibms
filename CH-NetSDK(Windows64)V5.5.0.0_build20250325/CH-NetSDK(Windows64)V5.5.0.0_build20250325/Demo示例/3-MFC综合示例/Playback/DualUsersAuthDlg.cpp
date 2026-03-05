// DualUsersAuth.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DualUsersAuthDlg.h"
#include "./Common/CommonFun.h"


// DualUsersAuth 对话框

IMPLEMENT_DYNAMIC(DualUsersAuthDlg, CDialog)

DualUsersAuthDlg::DualUsersAuthDlg(int iTimes, CWnd* pParent /*=NULL*/)
	: m_iTimes(iTimes)
	, CDialog(DualUsersAuthDlg::IDD, pParent)
{
}

DualUsersAuthDlg::~DualUsersAuthDlg()
{
}

void DualUsersAuthDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_USER, m_editUser);
	DDX_Control(pDX, IDC_EDIT_PWD, m_editPwd);
}


BEGIN_MESSAGE_MAP(DualUsersAuthDlg, CDialog)
	ON_BN_CLICKED(IDOK, &DualUsersAuthDlg::OnBnClickedOk)
	ON_BN_CLICKED(IDCANCEL, &DualUsersAuthDlg::OnBnClickedCancel)
END_MESSAGE_MAP()


// DualUsersAuth 消息处理程序
BOOL DualUsersAuthDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	SetWindowText(GetTextEx(IDS_TEXT_DUALUSERS_NON_ADMIN));
	SetDlgItemTextEx(IDOK, IDS_CONFIG_VCA_DRAWING_OK);
	SetDlgItemTextEx(IDCANCEL, IDS_CFG_STORAGE_DISKMAG_CANCEL);
	SetDlgItemTextEx(IDC_STATIC_USER, IDS_PE_HOTBACKUP_USERNAME);
	SetDlgItemTextEx(IDC_STATIC_PWD, IDS_PE_HOTBACKUP_PASSWORD);
	if (2 == m_iTimes)
	{
		//第2个用户固定为admin
		SetWindowText(GetTextEx(IDS_TEXT_DUALUSERS_ADMIN));
		m_editUser.EnableWindow(FALSE);
		m_editUser.SetWindowText(_T("admin"));
	}

	return TRUE;
}


void DualUsersAuthDlg::OnBnClickedOk()
{
	// TODO: 在此添加控件通知处理程序代码
	m_editUser.GetWindowText(m_tUserInfo.strUser);
	m_editPwd.GetWindowText(m_tUserInfo.strPwd);
	OnOK();
}

void DualUsersAuthDlg::OnBnClickedCancel()
{
	// TODO: 在此添加控件通知处理程序代码
	OnCancel();
}

void DualUsersAuthDlg::GetUserInfo(T_UserInfo &_tUserInfo)
{
	_tUserInfo.strUser = m_tUserInfo.strUser;
	_tUserInfo.strPwd = m_tUserInfo.strPwd;
}
