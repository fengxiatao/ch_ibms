
#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceFearutre.h"


IMPLEMENT_DYNAMIC(CLS_DlgFaceFearutre, CLS_PageBase)

CLS_DlgFaceFearutre::CLS_DlgFaceFearutre(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceFearutre::IDD, pParent)
{

}

CLS_DlgFaceFearutre::~CLS_DlgFaceFearutre()
{
}

void CLS_DlgFaceFearutre::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CBO_FEATURE_LIBKEY, m_cboFeatureLibKey);
	DDX_Control(pDX, IDC_CBO_FEATURE_FACEKEY, m_cboFeatureFacekey);
	DDX_Control(pDX, IDC_EDT_FEATURE_PICPATH, m_edtFeaturePicPath);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceFearutre, CLS_PageBase)
	ON_BN_CLICKED(IDC_BTN_FEATURE_LIBKEY, &CLS_DlgFaceFearutre::OnBnClickedBtnFeatureLibkey)
	ON_BN_CLICKED(IDC_BTN_FEATURE_FACEKEY, &CLS_DlgFaceFearutre::OnBnClickedBtnFeatureFacekey)
	ON_BN_CLICKED(IDC_BTN_FEATURE_PICPATH, &CLS_DlgFaceFearutre::OnBnClickedBtnFeaturePicpath)
	ON_BN_CLICKED(IDC_BTN_FEATURE_QUERY, &CLS_DlgFaceFearutre::OnBnClickedBtnFeatureQuery)
	ON_BN_CLICKED(IDC_BTN_FEATURE_CALC, &CLS_DlgFaceFearutre::OnBnClickedBtnFeatureCalc)
END_MESSAGE_MAP()


void CLS_DlgFaceFearutre::OnBnClickedBtnFeatureLibkey()
{
	QueryLibkey(m_cboFeatureLibKey);
}

void CLS_DlgFaceFearutre::OnBnClickedBtnFeatureFacekey()
{
	m_cboFeatureFacekey.ResetContent();

	int iLibKeySel = m_cboFeatureLibKey.GetCurSel();
	if (iLibKeySel < 0)
	{
		return;
	}

	FaceQuery tQuery = {0};
	tQuery.iSize = sizeof(tQuery);
	tQuery.iLibKey = (int)m_cboFeatureLibKey.GetItemData(iLibKeySel);
	tQuery.iPageCount = FACE_MAX_PAGE_COUNT;
	tQuery.iPageNo = 0;
	strncpy_s(tQuery.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tQuery.cLibUUID));
	strncpy_s(tQuery.cBirthStart, "1970-01-01", sizeof(tQuery.cBirthStart));
	strncpy_s(tQuery.cBirthEnd, GetCurTimeStr(), sizeof(tQuery.cBirthEnd));

	memset(&m_tFacePicInfo, 0, sizeof(m_tFacePicInfo));
	int iRet = FaceConfig(FACE_CMD_QUERY, &tQuery, sizeof(tQuery), &m_tFacePicInfo,sizeof(FaceQueryResult));
	if (0 == iRet)
	{
		for (int iIdx = 0; iIdx < FACE_MAX_PAGE_COUNT && iIdx < m_tFacePicInfo[0].iPageCount; ++iIdx)
		{
			if (m_tFacePicInfo[iIdx].iSize > 0)
			{
				m_cboFeatureFacekey.SetItemData(m_cboFeatureFacekey.AddString(m_tFacePicInfo[iIdx].tFace.cName), m_tFacePicInfo[iIdx].tFace.iLibKey);
			}			
		}
		m_cboFeatureFacekey.SetCurSel(0);
	}
}

void CLS_DlgFaceFearutre::OnBnClickedBtnFeaturePicpath()
{
	OpenPicPath(m_edtFeaturePicPath);
}

void CLS_DlgFaceFearutre::OnBnClickedBtnFeatureQuery()
{
	int iLibKeySel = m_cboFeatureLibKey.GetCurSel();
	int iFaceKeySel = m_cboFeatureFacekey.GetCurSel();
	if (iLibKeySel < 0 || iFaceKeySel < 0)
	{
		return;
	}

	FaceFeatureQuery tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iLibKey = (int)m_cboFeatureLibKey.GetItemData(iLibKeySel);
	tInfo.iFaceKey = (int)m_cboFeatureLibKey.GetItemData(iFaceKeySel);
	strncpy_s(tInfo.cLibUUID, m_tFaceLibInfo[iLibKeySel].tFaceLib.cLibUUID, sizeof(tInfo.cLibUUID));
	strncpy_s(tInfo.cFaceUUID, m_tFacePicInfo[iFaceKeySel].tFace.cFaceUUID, sizeof(tInfo.cFaceUUID));

	FaceFeatureResult tResult = {0};
	FaceConfig(FACE_CMD_FEATURE_QUERY, &tInfo, sizeof(tInfo), &tResult, sizeof(tResult));

	int iNum = tResult.iFaceNum;
}

void CLS_DlgFaceFearutre::OnBnClickedBtnFeatureCalc()
{
	FaceFeatureCalc tInfo = {0};
	tInfo.iSize = sizeof(tInfo);
	tInfo.iChanNo = m_iChannelNo;
	tInfo.iPicType = 0;
	m_edtFeaturePicPath.GetWindowText(tInfo.cPicPath, sizeof(tInfo.cPicPath));

	FaceFeatureResult tResult = {0};
	tResult.iSize = sizeof(tResult);
	int iRet = FaceConfig(FACE_CMD_FEATURE_CALC, &tInfo, sizeof(tInfo), &tResult, sizeof(tResult));

	int iNum = tResult.iFaceNum;
}
