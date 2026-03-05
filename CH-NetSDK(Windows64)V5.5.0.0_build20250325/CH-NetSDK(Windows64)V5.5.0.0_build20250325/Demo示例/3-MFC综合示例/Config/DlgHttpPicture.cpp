// DlgWirelessSilent.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "DlgHttpPicture.h"


// CLS_HttpPicture dialog

IMPLEMENT_DYNAMIC(CLS_HttpPicture, CDialog)

CLS_HttpPicture::CLS_HttpPicture(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_HttpPicture::IDD, pParent)
	, m_iServerNum(1)
	, m_csIP(_T(""))
	, m_csURL(_T(""))
	, m_csUserName(_T(""))
	, m_csPsw(_T(""))
	, m_iPort(3001)
{

}

CLS_HttpPicture::~CLS_HttpPicture()
{
}

void CLS_HttpPicture::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_HTTPPIC_SERVERNUM, m_iServerNum);
	DDV_MinMaxInt(pDX, m_iServerNum, 0, 32);
	DDX_Control(pDX, IDC_COMBO_HTTPPIC_SERVER, m_cboServer);
	DDX_Text(pDX, IDC_EDIT_HTTPPIC_IP, m_csIP);
	DDV_MaxChars(pDX, m_csIP, 256);
	DDX_Text(pDX, IDC_EDIT_HTTPPIC_URL, m_csURL);
	DDV_MaxChars(pDX, m_csURL, 256);
	DDX_Text(pDX, IDC_EDIT_HTTPPIC_UserName, m_csUserName);
	DDV_MaxChars(pDX, m_csUserName, 64);
	DDX_Text(pDX, IDC_EDIT_HTTPPIC_PASSWORD, m_csPsw);
	DDV_MaxChars(pDX, m_csPsw, 64);
	DDX_Text(pDX, IDC_EDIT_HTTPPIC_Port, m_iPort);
	DDV_MinMaxInt(pDX, m_iPort, 1, 65515);
}


BEGIN_MESSAGE_MAP(CLS_HttpPicture, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_HttpPicture::OnBnClickedButtonSet)
	ON_CBN_SELCHANGE(IDC_COMBO_HTTPPIC_SERVER, &CLS_HttpPicture::OnCbnSelchangeComboHttppicServer)
	ON_EN_CHANGE(IDC_EDIT_HTTPPIC_SERVERNUM, &CLS_HttpPicture::OnEnChangeEditHttppicServernum)
	ON_BN_CLICKED(IDC_BUTTON_SETTEST, &CLS_HttpPicture::OnBnClickedButtonSettest)
	ON_BN_CLICKED(IDC_BUTTON_SET_SERVER, &CLS_HttpPicture::OnBnClickedButtonSetServer)
END_MESSAGE_MAP()


// CLS_HttpPicture message handlers


BOOL CLS_HttpPicture::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	// TODO:  Add extra initialization here
	InitPageUI();
	GetHttpPicStreamData();
	OnEnChangeEditHttppicServernum();
	return TRUE;  // return TRUE unless you set the focus to a control
	// EXCEPTION: OCX Property Pages should return FALSE
}
void CLS_HttpPicture::InitPageUI()
{
	SetDlgItemText(IDC_BUTTON_SET, GetTextByLan("…Ë÷√", "Set"));
}

void CLS_HttpPicture::OnBnClickedButtonSet()
{
	// TODO: Add your control notification handler code here
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_HttpPicture::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	UpdateData(TRUE);

	m_tHttpPicStreamParam.iServerNum = m_iServerNum;

	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_HTTPPICSTREAM, m_iChannelNO, &m_tHttpPicStreamParam, sizeof(HttpPicStreamParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_HTTPPICSTREAM fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_HTTPPICSTREAM SUCCESS!");

	}
}
void CLS_HttpPicture::GetHttpPicStreamData()
{
	memset(&m_tHttpPicStreamParam,0x00,sizeof(m_tHttpPicStreamParam));

	int iBytesReturned = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_HTTPPICSTREAM, m_iChannelNO, &m_tHttpPicStreamParam, sizeof(HttpPicStreamParam),&iBytesReturned);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_GetDevConfig]NET_CLIENT_HTTPPICSTREAM fail!");
	}
	else
	{
		AddLog(LOG_TYPE_SUCC, "", "[NetClient_GetDevConfig]NET_CLIENT_HTTPPICSTREAM SUCCESS!");

	}
}

void CLS_HttpPicture::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	if (m_iLogonID < 0)
	{
		return;
	}

}

void CLS_HttpPicture::OnChannelChanged( int _iLogonID,int _iChannelNo, int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	if (m_iChannelNO < 0)
	{
		m_iChannelNO = 0;
	} 
	else
	{
		m_iChannelNO = _iChannelNo;
	}

}

void CLS_HttpPicture::OnLanguageChanged(int _iLanguage)
{
	InitPageUI();
}

void CLS_HttpPicture::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (-1 == m_iLogonID || -1 == m_iChannelNO)
	{
		AddLog(LOG_TYPE_FAIL, "", "[CLS_IrrigationGeneralConfig]Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	if (PARA_HTTPPICSTREAM == _iParaType)
	{
		GetHttpPicStreamData();
	}
}

void CLS_HttpPicture::OnCbnSelchangeComboHttppicServer()
{
	// TODO: Add your control notification handler code here

	int iCurIndex = m_cboServer.GetCurSel();
	if(iCurIndex >= 0 && iCurIndex < MAX_HTTP_SERVER_NUM)
	{
		HttpPicStream &tHttpPicStream = m_tHttpPicStreamParam.tHttpPicStream[iCurIndex];
		m_csIP = tHttpPicStream.cIPAdress;
		m_csURL = tHttpPicStream.cURL;
		m_csUserName = tHttpPicStream.cUserName;
		m_csPsw = tHttpPicStream.cPassWord;

		UpdateData(FALSE);
	}
}

void CLS_HttpPicture::OnEnChangeEditHttppicServernum()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CLS_BasePage::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
	UpdateData(TRUE);
	m_cboServer.Clear();
	CString str;
	for(int i = 0; i < m_iServerNum && i < MAX_HTTP_SERVER_NUM; i++)
	{
		str.Format("%d",i);
		m_cboServer.AddString(str);
	}

	m_cboServer.SetCurSel(0);

}

void CLS_HttpPicture::OnBnClickedButtonSettest()
{
	// TODO: Add your control notification handler code here
	HttpPicStream tHttpPicStream = {0};
	tHttpPicStream.iSize = sizeof(HttpPicStream);

	UpdateData(TRUE);
	strcpy_s(tHttpPicStream.cIPAdress,sizeof(tHttpPicStream.cIPAdress),m_csIP.GetBuffer(0));
	strcpy_s(tHttpPicStream.cURL,sizeof(tHttpPicStream.cURL),m_csURL.GetBuffer(0));
	strcpy_s(tHttpPicStream.cUserName,sizeof(tHttpPicStream.cUserName),m_csUserName.GetBuffer(0));
	strcpy_s(tHttpPicStream.cPassWord,sizeof(tHttpPicStream.cPassWord),m_csPsw.GetBuffer(0));
	tHttpPicStream.iPort = m_iPort;

	HttpPicTestResult tHttpPicTestResult;
	tHttpPicTestResult.iSize = sizeof(HttpPicTestResult);

	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_HTTPPICTEST, m_iChannelNO, (void *)&tHttpPicStream, tHttpPicStream.iSize, (void *)&tHttpPicTestResult, tHttpPicTestResult.iSize);
	if(RET_SUCCESS == iRet)
	{
		if(RET_SUCCESS==tHttpPicTestResult.iResult)
		{
			AfxMessageBox("Http Test Success!");
		}else{
			AfxMessageBox("Http Test Faild!");
		}
	}else{
		AddLog(LOG_TYPE_FAIL, "", "[NetClient_CmdConfig]CMD_HTTPPICTEST fail!");
	}
}

void CLS_HttpPicture::OnBnClickedButtonSetServer()
{
	// TODO: Add your control notification handler code here
	int iCurIndex = m_cboServer.GetCurSel();
	if(iCurIndex >= 0 && iCurIndex < MAX_HTTP_SERVER_NUM)
	{
		UpdateData(TRUE);
		HttpPicStream &tHttpPicStream = m_tHttpPicStreamParam.tHttpPicStream[iCurIndex];
		strcpy_s(tHttpPicStream.cIPAdress,sizeof(tHttpPicStream.cIPAdress),m_csIP.GetBuffer(0));
		strcpy_s(tHttpPicStream.cURL,sizeof(tHttpPicStream.cURL),m_csURL.GetBuffer(0));
		strcpy_s(tHttpPicStream.cUserName,sizeof(tHttpPicStream.cUserName),m_csUserName.GetBuffer(0));
		strcpy_s(tHttpPicStream.cPassWord,sizeof(tHttpPicStream.cPassWord),m_csPsw.GetBuffer(0));
		tHttpPicStream.iPort = m_iPort;
	}
}
