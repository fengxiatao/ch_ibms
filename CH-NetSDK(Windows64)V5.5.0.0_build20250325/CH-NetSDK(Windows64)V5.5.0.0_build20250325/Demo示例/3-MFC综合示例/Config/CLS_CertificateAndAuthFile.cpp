// CLS_CertificateAndAuthFile.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\CLS_CertificateAndAuthFile.h"


// CLS_CertificateAndAuthFile dialog

IMPLEMENT_DYNAMIC(CLS_CertificateAndAuthFile, CDialog)

CLS_CertificateAndAuthFile::CLS_CertificateAndAuthFile(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_CertificateAndAuthFile::IDD, pParent)
{

}

CLS_CertificateAndAuthFile::~CLS_CertificateAndAuthFile()
{
}

void CLS_CertificateAndAuthFile::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_REQUEST_TYPE, m_cboRequestType);
	DDX_Control(pDX, IDC_COMBO_CERTIFICATE_COUNT, m_cboCertificateCount);
	DDX_Control(pDX, IDC_EDIT_COUNTRY_NAME, m_editCountryName);
	DDX_Control(pDX, IDC_EDIT_PASSWORD, m_editPassWord);
	DDX_Control(pDX, IDC_EDIT_STATEORPROVINCE_NAME, m_editStateName);
	DDX_Control(pDX, IDC_EDIT_LOCALITY_NAME, m_editLocalityName);
	DDX_Control(pDX, IDC_EDIT_ORGNAZATION_NAME, m_editOrgnazationName);
	DDX_Control(pDX, IDC_EDIT_ORGNAZATION_UNIT_NAME, m_editOrgnazationUnitName);
	DDX_Control(pDX, IDC_EDIT_EMAIL, m_editEmail);
	DDX_Control(pDX, IDC_EDIT_SIP_DEVICE_ID, m_editSIPDeviceID);
	DDX_Control(pDX, IDC_LIST_CERTIFICATE_INFO, m_listCertificateInfo);
	DDX_Control(pDX, IDC_BUTTON_GET_CERTIFICATE, m_btGet);
	DDX_Control(pDX, IDC_BUTTON_CREATE_AUTHFILE, m_btCreate);
}


BOOL CLS_CertificateAndAuthFile::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	for(int i = 0; i < MAX_CERTIFICATE_NUM; i++)
	{
		CString strNum;
		strNum.Format(_T("%d"), i + 1);
		m_cboCertificateCount.AddString(strNum);
	}
	UI_UpdateUIText();
	m_cboRequestType.SetCurSel(0);
	return TRUE;
}

void CLS_CertificateAndAuthFile::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_CertificateAndAuthFile::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_REQUEST_TYPE, GetTextByLan(_T("请求类型"), _T("RequestType")));
	SetDlgItemText(IDC_STATIC_COUNTRY_NAME, GetTextByLan(_T("国家"), _T("CountryName")));
	SetDlgItemText(IDC_STATIC_PASSWORD, GetTextByLan(_T("密码(最少4位)"), _T("Pwd(least 4 nums)")));
	SetDlgItemText(IDC_STATIC_STATEORPROVINCE_NAME, GetTextByLan(_T("省或州"), _T("StateOrProvinceName")));
	SetDlgItemText(IDC_STATIC_LOCALITY_NAME, GetTextByLan(_T("地区"), _T("LocalityName")));
	SetDlgItemText(IDC_STATIC_ORGNAZATION_NAME, GetTextByLan(_T("组织"), _T("OrgnazationName")));
	SetDlgItemText(IDC_STATIC_ORGNAZATION_UNIT_NAME, GetTextByLan(_T("单位"), _T("OrgnazationUnitName")));
	SetDlgItemText(IDC_STATIC_EMAIL, GetTextByLan(_T("邮件"), _T("Email")));
	SetDlgItemText(IDC_STATIC_SIP_DEVICE_ID, GetTextByLan(_T("sip中的设备ID"), _T("SIPDeviceID")));
	SetDlgItemText(IDC_STATIC_CERTIFICATE_COUNT, GetTextByLan(_T("证书总个数"), _T("Certificate Count")));
	SetDlgItemText(IDC_STATIC_CERTIFICATE_INFO, GetTextByLan(_T("证书信息"), _T("Info")));
	m_btCreate.SetWindowText(GetTextByLan(_T("创建"), _T("Create")));
	m_btGet.SetWindowText(GetTextByLan(_T("获取"), _T("Get")));
	int iIndex = m_cboRequestType.GetCurSel();
	m_cboRequestType.ResetContent();
	m_cboRequestType.AddString(GetTextByLan(_T("GB35114证书请求文件"), _T("Gb35114 certificate request file")));
	m_cboRequestType.SetCurSel(iIndex);
	while(m_listCertificateInfo.DeleteColumn(0));
	m_listCertificateInfo.InsertColumn(0, GetTextByLan(_T("保留"), _T("Retain")), LVCFMT_CENTER, 0);
	m_listCertificateInfo.InsertColumn(1, GetTextByLan(_T("证书序号"), _T("Certificate serial number")), LVCFMT_CENTER, 100);
	m_listCertificateInfo.InsertColumn(2, GetTextByLan(_T("证书ID"), _T("Certificate ID")), LVCFMT_CENTER, 100);
	m_listCertificateInfo.InsertColumn(3, GetTextByLan(_T("有效期开始时间"), _T("Validity start time")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(4, GetTextByLan(_T("有效期结束时间"), _T("Validity end time")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(5, GetTextByLan(_T("证书状态"), _T("Certificate status")), LVCFMT_CENTER, 100);

	m_listCertificateInfo.InsertColumn(6, GetTextByLan(_T("使用者"), _T("User")), LVCFMT_CENTER, 100);
	m_listCertificateInfo.InsertColumn(7, GetTextByLan(_T("证书颁发者的名字"), _T("certificate issuer name")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(8, GetTextByLan(_T("证书颁发者的国家"), _T("certificate issuer country")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(9, GetTextByLan(_T("证书颁发者的省或州"), _T("certificate issuer Province or state")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(10, GetTextByLan(_T("证书颁发者的地区"), _T("certificate issuer region")), LVCFMT_CENTER, 120);

	m_listCertificateInfo.InsertColumn(11, GetTextByLan(_T("证书颁发者的组织"), _T("certificate issuer organization")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(12, GetTextByLan(_T("证书颁发者的单位"), _T("certificate issuer Company")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(13, GetTextByLan(_T("证书颁发者的邮件"), _T("certificate issuer Email")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(14, GetTextByLan(_T("证书归属人的设备标识"), _T("Certificate owner Equipment identification")), LVCFMT_CENTER, 130);
	m_listCertificateInfo.InsertColumn(15, GetTextByLan(_T("证书归属人的国家"), _T("Certificate owner country")), LVCFMT_CENTER, 120);


	m_listCertificateInfo.InsertColumn(16, GetTextByLan(_T("证书归属人的省或州"), _T("Certificate owner Province or state")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(17, GetTextByLan(_T("证书归属人的地区"), _T("Certificate owner region")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(18, GetTextByLan(_T("证书归属人的组织"), _T("Certificate owner organization")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(19, GetTextByLan(_T("证书归属人的单位"), _T("Certificate owner Company")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.InsertColumn(20, GetTextByLan(_T("证书归属人的邮件"), _T("Certificate owner email")), LVCFMT_CENTER, 120);

	m_listCertificateInfo.InsertColumn(21, GetTextByLan(_T("证书序列号"), _T("Certificate serial number")), LVCFMT_CENTER, 120);
	m_listCertificateInfo.DeleteColumn(0);
}

CString CLS_CertificateAndAuthFile::IntToCstr(int _iNum)
{
	CString strNum;
	strNum.Format(_T("%d"), _iNum);
	return strNum;
}

CString CLS_CertificateAndAuthFile::ArrayToCstr(char *_cArray)
{
	CString strArray;
	strArray = _cArray;
	return strArray;
}

BEGIN_MESSAGE_MAP(CLS_CertificateAndAuthFile, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_GET_CERTIFICATE, &CLS_CertificateAndAuthFile::OnBnClickedButtonGetCertificate)
	ON_BN_CLICKED(IDC_BUTTON_CREATE_AUTHFILE, &CLS_CertificateAndAuthFile::OnBnClickedButtonCreateAuthfile)
END_MESSAGE_MAP()


// CLS_CertificateAndAuthFile message handler

void CLS_CertificateAndAuthFile::OnBnClickedButtonGetCertificate()
{
	m_listCertificateInfo.DeleteAllItems();
	Certificate tInfo;
	memset(&tInfo, 0, sizeof(Certificate));
	tInfo.iNum = 1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_NET_CERATIFICATE, m_iChannelNO, &tInfo, sizeof(tInfo), NULL);
	if(RET_SUCCESS == iRet) {
		m_cboCertificateCount.SetCurSel(tInfo.iCount - 1);
		for(int i = 0; i < tInfo.iCount && i < MAX_CERTIFICATE_NUM; i++)
		{
			memset(&tInfo, 0, sizeof(Certificate));
			tInfo.iNum = i + 1;
			iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_NET_CERATIFICATE, m_iChannelNO, &tInfo, sizeof(tInfo), NULL);
			m_listCertificateInfo.InsertItem(i, IntToCstr(tInfo.iNum));
			m_listCertificateInfo.SetItemText(i, 1, tInfo.pcServerCertificateID);
			m_listCertificateInfo.SetItemText(i, 2, tInfo.pcStartTime);
			m_listCertificateInfo.SetItemText(i, 3, tInfo.pcEndTime);
			m_listCertificateInfo.SetItemText(i, 4, IntToCstr(tInfo.iState));
			m_listCertificateInfo.SetItemText(i, 5, IntToCstr(tInfo.iUser));
			m_listCertificateInfo.SetItemText(i, 6, tInfo.cIssuerCommonName);
			m_listCertificateInfo.SetItemText(i, 7, tInfo.cIssuerCountryName);
			m_listCertificateInfo.SetItemText(i, 8, tInfo.cIssuerStateOrProvinceName);
			m_listCertificateInfo.SetItemText(i, 9, tInfo.cIssuerLocalityName	);
			m_listCertificateInfo.SetItemText(i, 10, tInfo.cIssuerOrgnazationName);
			m_listCertificateInfo.SetItemText(i, 11, tInfo.cIssuerOrgnazationUnitName);
			m_listCertificateInfo.SetItemText(i, 12, tInfo.cIssuerEmail);
			m_listCertificateInfo.SetItemText(i, 13, tInfo.cSubjectCommonName);
			m_listCertificateInfo.SetItemText(i, 14, tInfo.cSubjectCountryName);
			m_listCertificateInfo.SetItemText(i, 15, tInfo.cSubjectStateOrProvinceName);
			m_listCertificateInfo.SetItemText(i, 16, tInfo.cSubjectLocalityName);
			m_listCertificateInfo.SetItemText(i, 17, tInfo.cSubjectOrgnazationName);
			m_listCertificateInfo.SetItemText(i, 18, tInfo.cSubjectOrgnazationUnitName);
			m_listCertificateInfo.SetItemText(i, 19, tInfo.cSubjectEmail);
			m_listCertificateInfo.SetItemText(i, 20, tInfo.cSerialNumber);
		}
		AddLog(LOG_TYPE_SUCC, "","CLS_CertificateAndAuthFile::NetClient_GetDevConfig[NET_CLIENT_NET_CERATIFICATE] (%d,),iResult = %d", m_iLogonID, iRet);
	} else {
		AddLog(LOG_TYPE_FAIL,"","CLS_CertificateAndAuthFile::NetClient_GetDevConfig[NET_CLIENT_NET_CERATIFICATE] (%d,), error(%d)", m_iLogonID, GetLastError());
	}
}

void CLS_CertificateAndAuthFile::OnBnClickedButtonCreateAuthfile()
{
	CreateAuthenticateFile tInfo;
	memset(&tInfo, 0, sizeof(CreateAuthenticateFile));
	CreateAuthenticateFileResult tResult;
	memset(&tResult, 0, sizeof(CreateAuthenticateFileResult));
	tInfo.iRequestType = m_cboRequestType.GetCurSel() + 1;
	CString strTemp;
	GetDlgItemText(IDC_EDIT_COUNTRY_NAME, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cCountryName, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_PASSWORD, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cPassword, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_STATEORPROVINCE_NAME, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cStateOrProvinceName, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_LOCALITY_NAME, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cLocalityName, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_ORGNAZATION_NAME, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cOrgnazationName, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_ORGNAZATION_UNIT_NAME, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cOrgnazationUnitName, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_EMAIL, strTemp);
	if(strTemp.GetLength() < LEN_32) {
		memcpy(tInfo.cEmail, strTemp, strTemp.GetLength()+1);
	}
	strTemp.Empty();
	GetDlgItemText(IDC_EDIT_SIP_DEVICE_ID, strTemp);
	if(strTemp.GetLength() < LEN_64) {
		memcpy(tInfo.cSIPDeviceID, strTemp, strTemp.GetLength()+1);
	}
	int iRet = NetClient_CmdConfig(m_iLogonID, CMD_CREATE_AUTHENTICATEFILE, m_iChannelNO, &tInfo, sizeof(CreateAuthenticateFile), &tResult, sizeof(tResult));
	if(RET_SUCCESS == iRet) {
		if(RET_SUCCESS == tResult.iState)
		{
			AddLog(LOG_TYPE_SUCC, "","CLS_CertificateAndAuthFile::NetClient_CmdConfig[CMD_CREATE_AUTHENTICATEFILE] (%d,),iResult = %d", m_iLogonID, iRet);
		}else {
			AddLog(LOG_TYPE_FAIL,"","CLS_CertificateAndAuthFile::NetClient_CmdConfig[CMD_CREATE_AUTHENTICATEFILE] (%d,), error(%d)", m_iLogonID, GetLastError());
		}
	}else {
		AddLog(LOG_TYPE_FAIL,"","CLS_CertificateAndAuthFile::NetClient_CmdConfig[CMD_CREATE_AUTHENTICATEFILE] (%d,), error(%d)", m_iLogonID, GetLastError());
	}
}
