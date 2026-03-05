// CLS_DlgSendCommonData.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgSendCommonData.h"


// CLS_DlgSendCommonData dialog

IMPLEMENT_DYNAMIC(CLS_DlgSendCommonData, CDialog)

CLS_DlgSendCommonData::CLS_DlgSendCommonData(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgSendCommonData::IDD, pParent)
	, m_csData(_T(""))
{

}

CLS_DlgSendCommonData::~CLS_DlgSendCommonData()
{
}

void CLS_DlgSendCommonData::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_DATA, m_csData);
}


BEGIN_MESSAGE_MAP(CLS_DlgSendCommonData, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SEND, &CLS_DlgSendCommonData::OnBnClickedButtonSend)
END_MESSAGE_MAP()


// CLS_DlgSendCommonData message handlers

BOOL CLS_DlgSendCommonData::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	UI_UpdateDialogText();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}



void CLS_DlgSendCommonData::OnChannelChanged( int _iLogonID,int _iChannelNo,int /*_iStreamNo*/ )
{
	m_iLogonID = _iLogonID;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
}

void CLS_DlgSendCommonData::OnLanguageChanged( int _iLanguage)
{
	UI_UpdateDialogText();
}

void CLS_DlgSendCommonData::OnBnClickedButtonSend()
{
	// TODO: Add your control notification handler code here
	UpdateData(TRUE);
	CommonData tCommonData = {0};
	tCommonData.iDataType = 0;
	tCommonData.iDataLen = m_csData.GetLength();
	strcpy_s(tCommonData.cData,sizeof(tCommonData.cData),m_csData.GetBuffer(0));
	

	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_SET_COMMONDATA,m_iChannelNo,&tCommonData,sizeof(CommonData),NULL,0);
	if(RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig[CMD_SET_COMMONDATA] (%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig[CMD_SET_COMMONDATA] (%d)",m_iLogonID);
	}


}

void CLS_DlgSendCommonData::UI_UpdateDialogText()
{
	SetDlgItemText(IDC_BUTTON_SEND,GetTextByLan(_T("·¢ËÍ"), _T("Send")));
}