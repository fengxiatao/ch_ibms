// HttpXmlCfg.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "HttpXmlCfg.h"

#define MAX_HTTP_REQURL		1024
#define MAX_XML_TEXTLEN		100 *1024

// CLS_HttpXmlCfg dialog

IMPLEMENT_DYNAMIC(CLS_HttpXmlCfg, CDialog)

CLS_HttpXmlCfg::CLS_HttpXmlCfg(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_HttpXmlCfg::IDD, pParent)
{

}

CLS_HttpXmlCfg::~CLS_HttpXmlCfg()
{
}

void CLS_HttpXmlCfg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_XML_REQUEST_URL, m_edtXmlRequestUrl);
	DDX_Control(pDX, IDC_COMBO_XML_OPT, m_cboXmlOpt);
	DDX_Control(pDX, IDC_EDIT_OUTPUT_XML, m_edtInputXml);
	DDX_Control(pDX, IDC_EDIT_INPUT_XML, m_edtOutputXml);
	DDX_Control(pDX, IDC_COMBO_XML_NET_MODE, m_cboXmlNetMode);
}


BEGIN_MESSAGE_MAP(CLS_HttpXmlCfg, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_SEND_XML_CFG, &CLS_HttpXmlCfg::OnBnClickedButtonSendXmlCfg)
END_MESSAGE_MAP()


// CLS_HttpXmlCfg message handler

BOOL CLS_HttpXmlCfg::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	m_edtXmlRequestUrl.SetLimitText(MAX_HTTP_REQURL);
	m_edtInputXml.SetLimitText(MAX_XML_TEXTLEN);
	m_edtOutputXml.SetLimitText(MAX_XML_TEXTLEN);
	m_edtXmlRequestUrl.SetWindowText("/CGI/Streaming/channels/1/type/1");
	m_edtInputXml.SetWindowText("Input Xml Text Here!");
	m_edtOutputXml.SetWindowText("Output Xml Text Here!");
	UI_UpdateText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}

void CLS_HttpXmlCfg::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
{
	if (_iLogonID < 0)
	{
		m_iLogonID = 0;
	}
	else
	{
		m_iLogonID = _iLogonID;
	}
}

void CLS_HttpXmlCfg::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateText();
}

void CLS_HttpXmlCfg::UI_UpdateText()
{
	SetDlgItemText(IDC_BUTTON_SEND_XML_CFG, GetTextByLan(_T("·¢ËÍ"), _T("Send")));

	m_cboXmlOpt.ResetContent();
	int iIndex = m_cboXmlOpt.AddString("GET");
	m_cboXmlOpt.SetItemData(iIndex, XMLCFG_METHOD_GET);
	iIndex = m_cboXmlOpt.AddString("PUT");
	m_cboXmlOpt.SetItemData(iIndex, XMLCFG_METHOD_PUT);
	iIndex = m_cboXmlOpt.AddString("POST");
	m_cboXmlOpt.SetItemData(iIndex, XMLCFG_METHOD_POST);
	iIndex = m_cboXmlOpt.AddString("DELETE");
	m_cboXmlOpt.SetItemData(iIndex, XMLCFG_METHOD_DELETE);

	m_cboXmlNetMode.ResetContent();
	m_cboXmlNetMode.AddString("HTTP");
	m_cboXmlNetMode.AddString("TCP");
}


void CLS_HttpXmlCfg::OnBnClickedButtonSendXmlCfg()
{
	if (m_iLogonID < 0) {
		return;
	}

	int iRet = RET_FAILED;
	CString cstrUrl;
	CString cstrInputXml;
	XmlCfgInPara tInPara = {0};
	XmlCfgOutPara tOutPara = {0};
	tInPara.pcRequestUrl = (char*)malloc(MAX_HTTP_REQURL);
	if (NULL == tInPara.pcRequestUrl) {
		goto END;
	}
	memset(tInPara.pcRequestUrl, 0, MAX_HTTP_REQURL);

	tInPara.pvInBuf = malloc(MAX_XML_TEXTLEN);
	if (NULL == tInPara.pvInBuf) {
		goto END;
	}
	memset(tInPara.pvInBuf, 0, MAX_XML_TEXTLEN);

	tOutPara.pvOutputBuf = malloc(MAX_XML_TEXTLEN);
	if (NULL == tOutPara.pvOutputBuf) {
		goto END;
	}
	memset(tOutPara.pvOutputBuf, 0, MAX_XML_TEXTLEN);

	tInPara.iMethod = m_cboXmlOpt.GetCurSel();
	tInPara.iNetMode = m_cboXmlNetMode.GetCurSel();
	m_edtXmlRequestUrl.GetWindowText(cstrUrl);
	m_edtInputXml.GetWindowText(cstrInputXml);
	strcpy(tInPara.pcRequestUrl, cstrUrl.GetBuffer());
	tInPara.iRequestUrlLen = cstrUrl.GetLength();
	strcpy((char*)tInPara.pvInBuf, cstrInputXml.GetBuffer());
	tInPara.iInBufSize = MAX_XML_TEXTLEN;
	tInPara.iRecvTimeOut = 10 * 1000;
	tOutPara.iOutBufSize = MAX_XML_TEXTLEN;

	iRet = NetClient_HttpXmlConfig(m_iLogonID, &tInPara, sizeof(XmlCfgInPara), &tOutPara, sizeof(XmlCfgOutPara));
	if (RET_SUCCESS == iRet) {
		char cTemp[MAX_XML_TEXTLEN] = {0};
		wchar_t wchar[MAX_XML_TEXTLEN] = {0};
		int iwclen = MultiByteToWideChar(CP_UTF8, 0, (char*)tOutPara.pvOutputBuf, tOutPara.iOutBufSize, wchar, MAX_XML_TEXTLEN); 
		int imblen = 0;
		if (iwclen>0)
		{
			imblen = WideCharToMultiByte(CP_ACP, 0, wchar, iwclen, cTemp, MAX_XML_TEXTLEN, NULL, NULL); 
		}

		if (iwclen>0 && imblen>0)
		{
			m_edtOutputXml.SetWindowText((LPCTSTR)(cTemp));
		}
		else
		{
			m_edtOutputXml.SetWindowText((LPCTSTR)(tOutPara.pvOutputBuf));
		}
		
		AddLog(LOG_TYPE_SUCC, "", "NetClient_HttpXmlConfig succ!");
	} else {
		AddLog(LOG_TYPE_FAIL, "", "NetClient_HttpXmlConfig fail! iRet=%d",iRet);
	}

END:
	if (NULL != tInPara.pcRequestUrl)
	{
		free(tInPara.pcRequestUrl);
		tInPara.pcRequestUrl = NULL;
	}
	if (NULL != tInPara.pvInBuf)
	{
		free(tInPara.pvInBuf);
		tInPara.pvInBuf = NULL;
	}
	if (NULL != tOutPara.pvOutputBuf)
	{
		free(tOutPara.pvOutputBuf);
		tOutPara.pvOutputBuf = NULL;
	}
}
