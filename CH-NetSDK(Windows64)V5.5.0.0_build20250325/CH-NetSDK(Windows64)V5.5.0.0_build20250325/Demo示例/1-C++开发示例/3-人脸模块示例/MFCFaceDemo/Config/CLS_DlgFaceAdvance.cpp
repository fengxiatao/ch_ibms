
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceAdvance.h"


// CLS_DlgFaceAdvance Dialog

IMPLEMENT_DYNAMIC(CLS_DlgFaceAdvance, CLS_PageBase)

CLS_DlgFaceAdvance::CLS_DlgFaceAdvance(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceAdvance::IDD, pParent)
{

}

CLS_DlgFaceAdvance::~CLS_DlgFaceAdvance()
{
}

void CLS_DlgFaceAdvance::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_ADV_LIBKEY, m_cboLibKey);
	DDX_Control(pDX, IDC_EDT_ADV_FILEPATH, m_edtFilePath);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceAdvance, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_ADV_LIBKEY_QUERY, &CLS_DlgFaceAdvance::OnBnClickedBtnAdvLibkeyQuery)
	ON_BN_CLICKED(IDC_BTN_ADV_FILE_PATH, &CLS_DlgFaceAdvance::OnBnClickedBtnAdvFilePath)
	ON_BN_CLICKED(IDC_BTN_ADV_IMPORT, &CLS_DlgFaceAdvance::OnBnClickedBtnAdvImport)
	ON_BN_CLICKED(IDC_BTN_ADV_EXPOR, &CLS_DlgFaceAdvance::OnBnClickedBtnAdvExpor)
END_MESSAGE_MAP()


void CLS_DlgFaceAdvance::OnBnClickedBtnAdvLibkeyQuery()
{
	QueryLibkey(m_cboLibKey);
}

void CLS_DlgFaceAdvance::OnBnClickedBtnAdvFilePath()
{
	OpenBoxPath(m_edtFilePath);
}

void Upgrade_Notify(int _iLogonID, int _iServerState, void* _iUserData)
{
	CLS_DlgFaceAdvance* pcls = (CLS_DlgFaceAdvance*)_iUserData;
	if (NULL != pcls)
	{
		CString cstrPos = IntToStr(_iServerState) + "%";
		pcls->GetDlgItem(IDC_STC_ADV_EXPORT_PROCESS)->SetWindowText(cstrPos);
	}
}

void CLS_DlgFaceAdvance::OnBnClickedBtnAdvImport()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);

	UpgradeV5 tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iType = 1;
	m_edtFilePath.GetWindowText(tInfo.cFilePath, sizeof(tInfo.cFilePath));
	strncpy_s(tInfo.cParam, IntToStr(m_tFaceLibInfo[iLibKeySel].tFaceLib.iLibKey), sizeof(tInfo.cParam));
	if (0 == m_tFaceLibInfo[iLibKeySel].tFaceLib.iLibKey && strlen(m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID) > 0)
	{
		strncpy_s(tInfo.cParam, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tInfo.cParam));
	}
	tInfo.cbUpgradeNotify = Upgrade_Notify;
	tInfo.pUser = this;

	GetDlgItem(IDC_STC_ADV_EXPORT_PROCESS)->SetWindowText("");
	NetClient_Upgrade_V5(m_iLogonID, UPGRADE_WEB, &tInfo, sizeof(tInfo));
}

void CLS_DlgFaceAdvance::OnBnClickedBtnAdvExpor()
{
	int iLibKeySel = 0;
	CHECK_LIB_KEY(m_cboLibKey, iLibKeySel);

	CString sFolder = "";  
	COleDateTime m_Date = COleDateTime::GetCurrentTime ();
	CString strFileName;
	strFileName = m_Date.Format("%Y%m%d%H%M%S");                               //File name of the exported data
	CFileDialog fileDlg(FALSE, "box", strFileName, OFN_HIDEREADONLY|OFN_OVERWRITEPROMPT, _T("box files(*.box)|*.box||"));
	if (IDOK == fileDlg.DoModal())
	{
		sFolder = fileDlg.GetPathName();
	}

	int iRetturn = 0;
	ExportConfig tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iCount = 1;
	CString cstrLib = "facelib:" + IntToStr(m_tFaceLibInfo[iLibKeySel].tFaceLib.iLibKey);
	if (0 == m_tFaceLibInfo[iLibKeySel].tFaceLib.iLibKey && strlen(m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID) > 0)
	{
		cstrLib = "facelib:";
		cstrLib =+ m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID;
	}
	strncpy_s(tInfo.cFileList[0], cstrLib, FILE_COUNT);
	strncpy_s(tInfo.cFileOut, sFolder, sizeof(tInfo.cFileOut));
	NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_EXPORT_CONFIG, m_iChannelNo, &tInfo, sizeof(tInfo), &iRetturn);
}