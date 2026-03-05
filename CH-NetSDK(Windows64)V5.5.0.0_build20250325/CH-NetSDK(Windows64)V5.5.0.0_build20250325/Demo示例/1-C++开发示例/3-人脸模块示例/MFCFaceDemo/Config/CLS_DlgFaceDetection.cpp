// CLS_DlgFaceDetection.cpp : Implementation file
//

#include "stdafx.h"
#include "MfcFaceDemo.h"
#include "CLS_DlgFaceDetection.h"


// CLS_DlgFaceDetection Dialog

IMPLEMENT_DYNAMIC(CLS_DlgFaceDetection, CLS_PageBase)

CLS_DlgFaceDetection::CLS_DlgFaceDetection(CWnd* pParent /*=NULL*/)
	: CLS_PageBase(CLS_DlgFaceDetection::IDD, pParent)
{

}

CLS_DlgFaceDetection::~CLS_DlgFaceDetection()
{
}

void CLS_DlgFaceDetection::DoDataExchange(CDataExchange* pDX)
{
	CLS_PageBase::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_TTCL, m_cboTTCL);
	DDX_Control(pDX, IDC_COMBO_ZPCS, m_cboZPCS);
	DDX_Control(pDX, IDC_COMBO_ZPMS, m_cboZPMS);
	DDX_Control(pDX, IDC_SLIDER_ZHZL, m_sldZHZL);
	DDX_Control(pDX, IDC_EDIT_ZXRLCC, m_edtZXRLCC);
	DDX_Control(pDX, IDC_CHECK_ENABLE_FACE_DETECT, m_chkEnableFaceDetect);
	DDX_Control(pDX, IDC_COMBO_FACE_DETECT_STATE, m_cboDetectType);
	DDX_Control(pDX, IDC_SLIDER_RLBGLD, m_sldBright);
	DDX_Control(pDX, IDC_CHECK_DETECT_SHOWRULE, m_chkShowRule);
	DDX_Control(pDX, IDC_CHECK_DETECT_SHOWTARGET, m_chkShowTarget);
	DDX_Control(pDX, IDC_SLIDER_ZPJG, m_sldSnapSpace);
	DDX_Control(pDX, IDC_SLIDER_FACEDETECT_BIGPIC, m_sldBigPicQuality);
	DDX_Control(pDX, IDC_SLIDER_FACEDETECT_SMALLPIC, m_sldSmallPicQuality);
	DDX_Control(pDX, IDC_CHECK_FACEDETECT_SNAPBIGPIC, m_chkSnapBigPic);
	DDX_Control(pDX, IDC_CHECK_FACEDETECT_BIGPICOSD, m_chkBigPicOsd);
}


BEGIN_MESSAGE_MAP(CLS_DlgFaceDetection, CLS_PageBase)
	ON_BN_CLICKED(IDC_BUTTON_SET, &CLS_DlgFaceDetection::OnBnClickedButtonSet)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_ZHZL, &CLS_DlgFaceDetection::OnNMCustomdrawSliderZhzl)
	ON_CBN_SELCHANGE(IDC_COMBO_ZPMS, &CLS_DlgFaceDetection::OnCbnSelchangeComboZpms)
	ON_CBN_SELCHANGE(IDC_COMBO_FACE_DETECT_STATE, &CLS_DlgFaceDetection::OnCbnSelchangeComboFaceDetectState)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_ZPJG, &CLS_DlgFaceDetection::OnNMCustomdrawSliderZpjg)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_RLBGLD, &CLS_DlgFaceDetection::OnNMCustomdrawSliderRlbgld)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_FACEDETECT_BIGPIC, &CLS_DlgFaceDetection::OnNMCustomdrawSliderFacedetectBigpic)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER_FACEDETECT_SMALLPIC, &CLS_DlgFaceDetection::OnNMCustomdrawSliderFacedetectSmallpic)
	ON_CBN_SELCHANGE(IDC_COMBO_TTCL, &CLS_DlgFaceDetection::OnCbnSelchangeComboTtcl)
END_MESSAGE_MAP()


// CLS_DlgFaceDetection Message Handler

void CLS_DlgFaceDetection::UI_Init()
{
	
	m_cboDetectType.ResetContent();
	m_cboDetectType.SetItemData(m_cboDetectType.AddString(_T("IPC Detect")), 0);
 	m_cboDetectType.SetItemData(m_cboDetectType.AddString(_T("NVR Detect")), 1);
	m_cboDetectType.SetCurSel(0);

	m_cboTTCL.ResetContent();
	m_cboTTCL.SetItemData(m_cboTTCL.AddString(_T("Fastest")), 1);
	m_cboTTCL.SetItemData(m_cboTTCL.AddString(_T("Quality best")), 2);
	m_cboTTCL.SetItemData(m_cboTTCL.AddString(_T("Timing")), 4);
	m_cboTTCL.SetItemData(m_cboTTCL.AddString(_T("Consecutively")), 6);
	m_cboTTCL.SetCurSel(0);

	m_cboZPCS.ResetContent();
	CString cstrSnapTimes;
	for(int i = 1; i <= 3; ++i)
	{
		cstrSnapTimes.Format(_T("%d"), i);
		m_cboZPCS.SetItemData(m_cboZPCS.AddString(cstrSnapTimes), i);
	}
	m_cboZPCS.SetCurSel(0);

	m_cboZPMS.ResetContent();
	m_cboZPMS.SetItemData(m_cboZPMS.AddString(_T("All snap")), 1);
	m_cboZPMS.SetItemData(m_cboZPMS.AddString(_T("High quality")), 2);
	m_cboZPMS.SetItemData(m_cboZPMS.AddString(_T("Custom")), 3);
	m_cboZPMS.SetCurSel(0);

	m_sldZHZL.SetRange(1, 100);
	m_sldZHZL.SetLineSize(1);

	m_edtZXRLCC.SetLimitText(5);

	m_sldSnapSpace.SetRange(1, 255);
	m_sldBright.SetRange(1, 255);
	m_sldBigPicQuality.SetRange(1, 100);
	m_sldSmallPicQuality.SetRange(1, 100);
}

void CLS_DlgFaceDetection::UI_UptateData()
{
	if (m_iLogonID < 0)
	{
		return;
	}

	GetAnyScene();
	GetFaceDetect();
	GetBigPicUploadParam();
	GetSmallPicUploadParam();
	UI_Update();
}

// void CLS_DlgFaceDetection::OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo)
// {
// 	m_iLogonID = _iLogonID;
// 	m_iChannelNo = _iChannelNo;
// 	m_iStreamNo = _iStreamNo;
// 	NetClient_GetChannelNum(m_iLogonID, &m_iChanCount);
// 	if (m_iChanCount > 1)
// 	{
// 		m_iChanCount -= 1;
// 	}
// 
// 	UI_UptateData();
// }


void CLS_DlgFaceDetection::UI_Update()
{		
 	int iIndex = m_cboDetectType.GetCurSel();
	m_cboDetectType.ResetContent();
 	m_cboDetectType.SetItemData(m_cboDetectType.AddString(_T("IPC Detect")), 0);
 	if (m_iChanCount>1)
 	{
 		m_cboDetectType.SetItemData(m_cboDetectType.AddString(_T("NVR Detect")), 1);
		m_cboDetectType.SetCurSel(iIndex);
 	}
	else
	{
		m_cboDetectType.SetCurSel(0);
	}
 	

	int iDetectType = (int)m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel());
	int iEnable = -1;
	int iRet = NetClient_GetCommonEnable(m_iLogonID, CI_COMMON_ID_VCA_RESOURCE, m_iChannelNo, &iEnable);
	
	if (1 == iDetectType && 1 == GetFaceAbility(m_iLogonID,9,25) && 1 == (iEnable >> 1) )
	{
		m_chkEnableFaceDetect.EnableWindow(FALSE);
	}
	else
	{
		m_chkEnableFaceDetect.EnableWindow(TRUE);
	}
	
	int iSnapMode = (int)m_cboZPMS.GetItemData(m_cboZPMS.GetCurSel());
	if (iSnapMode == 3)
	{
		m_sldZHZL.EnableWindow(TRUE);
	} 
	else
	{
		m_sldZHZL.EnableWindow(FALSE);
	}

	int iPushMode = (int)m_cboTTCL.GetItemData(m_cboTTCL.GetCurSel());
	if (1 == iPushMode || 2 == iPushMode)//Push map strategy: the fastest and the best
	{
		m_cboZPCS.EnableWindow(TRUE);
		m_sldSnapSpace.EnableWindow(FALSE);
	}
	else if (4 == iPushMode)//Push map policy: timing
	{
		m_cboZPCS.EnableWindow(TRUE);
		m_sldSnapSpace.EnableWindow(TRUE);
	}
	else if(6 == iPushMode)//Push strategy: continuous
	{
		m_cboZPCS.EnableWindow(FALSE);
		m_sldSnapSpace.EnableWindow(FALSE);
	}

 	int iAbility = GetFaceAbility(m_iLogonID,9,15);
 	if (iAbility>>13 & 0x01)
 	{
 		m_sldBigPicQuality.ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_FACEDETECT_BIGPIC)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_FACEDETECT_BIGPIC_VALUE)->ShowWindow(SW_SHOW);
 	}
 	else
 	{
		m_sldBigPicQuality.ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_FACEDETECT_BIGPIC)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_FACEDETECT_BIGPIC_VALUE)->ShowWindow(SW_HIDE);
 	}
	if (iAbility>>14 & 0x01)
	{
		m_sldSmallPicQuality.ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_FACEDETECT_SMALLPIC)->ShowWindow(SW_SHOW);
		GetDlgItem(IDC_STATIC_FACEDETECT_SMALLPIC_VALUE	)->ShowWindow(SW_SHOW);
	}
	else
	{
		m_sldSmallPicQuality.ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_FACEDETECT_SMALLPIC)->ShowWindow(SW_HIDE);
		GetDlgItem(IDC_STATIC_FACEDETECT_SMALLPIC_VALUE)->ShowWindow(SW_HIDE);
	}
	if (iAbility>>15 & 0x01)
	{
		m_chkSnapBigPic.ShowWindow(SW_SHOW);
	}
	else
	{
		m_chkSnapBigPic.ShowWindow(SW_HIDE);
	}
	if (iAbility>>16 & 0x01)
	{
		m_chkBigPicOsd.ShowWindow(SW_SHOW);
	}
	else
	{
		m_chkBigPicOsd.ShowWindow(SW_HIDE);
	}
	
}

void CLS_DlgFaceDetection::OnBnClickedButtonSet()
{
	if (-1 == m_iChannelNo)
	{
		return;
	}

	int iMinFaceSize = GetDlgItemInt(IDC_EDIT_ZXRLCC);
	if (iMinFaceSize > 10000 || iMinFaceSize < 1)
	{
		MessageBox(_T("Minimum face size can be entered:1~10000!"),"Tips",MB_OK);
		return;
	}

	int iRet = -1;
	if (0 != SetAnyScene())
	{
		return;
	}
	if (0 != SetFaceDetect())
	{
		return;
	}

	if (m_sldBigPicQuality.IsWindowVisible() && m_chkSnapBigPic.IsWindowVisible() && m_chkBigPicOsd.IsWindowVisible())
	{
		SetBigPicUploadParam();
	}

	if (m_sldSmallPicQuality.IsWindowVisible())
	{
		SetSmallPicUploadParam();
	}
	
	MessageBox(_T("Set success!"),"Tips",MB_OK);
}

void CLS_DlgFaceDetection::OnCbnSelchangeComboFaceDetectState()
{
	UI_UptateData();
}

void CLS_DlgFaceDetection::OnCbnSelchangeComboTtcl()
{
	UI_Update();
}

void CLS_DlgFaceDetection::OnCbnSelchangeComboZpms()
{
	UI_Update();
}

int CLS_DlgFaceDetection::GetAnyScene()
{
	int iReturnByte = 0;
	int iRet = -1;

	AnyScene stAnyScene = {0};
	stAnyScene.iBufSize = sizeof(stAnyScene);
	stAnyScene.iSceneID = 0;
	stAnyScene.iDevType = (int)(m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel()));
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNo, &stAnyScene, sizeof(stAnyScene), &iReturnByte);
	if (iRet == 0)
	{
		int iEnableFaceDetect = (stAnyScene.iArithmetic>>2) & 0x01;
		m_chkEnableFaceDetect.SetCheck(iEnableFaceDetect);
		//AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_ANYSCENE);
	}
	else
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_ANYSCENE);
	}

	return iRet;
}

int CLS_DlgFaceDetection::SetAnyScene()
{
	int iReturnByte = 0;
	int iRet = -1;

	AnyScene stAnyScene = {0};
	stAnyScene.iBufSize = sizeof(stAnyScene);
	stAnyScene.iSceneID = 0;
	stAnyScene.iDevType = (int)(m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel()));
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNo, &stAnyScene, sizeof(stAnyScene), &iReturnByte);
	if (iRet != 0)
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_ANYSCENE);
		return iRet;
	}

	stAnyScene.iDevType = (int)(m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel()));
	int iFace = m_chkEnableFaceDetect.GetCheck();
	if (iFace)
	{
		stAnyScene.iArithmetic |= (iFace<<2);
	}
	else
	{
		stAnyScene.iArithmetic = stAnyScene.iArithmetic&~(1<<2);
	}

	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_ANYSCENE, m_iChannelNo, &stAnyScene, sizeof(stAnyScene));
	if (0 != iRet)
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_ANYSCENE);
	}

	return iRet;
}

int CLS_DlgFaceDetection::GetFaceDetect()
{
	int iReturnByte = 0;
	int iRet = -1;

	FaceDetectArithmetic tParam = {0};
	tParam.iBufSize = sizeof(tParam);
	tParam.iSceneID = 0;
	tParam.iDevType = (int)(m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel()));
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, &tParam, sizeof(tParam),&iReturnByte);
	if (0 == iRet)
	{
		for (int i = 0; i < m_cboTTCL.GetCount(); ++i)
		{
			if (m_cboTTCL.GetItemData(i) == tParam.iPushMode )
			{
				m_cboTTCL.SetCurSel(i);
				break;
			}
		}
		for (int i = 0; i < m_cboZPCS.GetCount(); ++i)
		{
			if (m_cboZPCS.GetItemData(i) == tParam.iSnapTimes )
			{
				m_cboZPCS.SetCurSel(i);
				break;
			}
		}
		for (int i = 0; i < m_cboZPMS.GetCount(); ++i)
		{
			if (m_cboZPMS.GetItemData(i) == tParam.iSnapMode )
			{
				m_cboZPMS.SetCurSel(i);
				break;
			}
		}
		m_sldZHZL.SetPos(tParam.iSnapLevel);
		SetDlgItemInt(IDC_STATIC_ZHZL_VALUE, m_sldZHZL.GetPos());
		SetDlgItemInt(IDC_EDIT_ZXRLCC, tParam.iMinSizeEx);

		m_sldSnapSpace.SetPos(tParam.iSnapSpace);
		SetDlgItemInt(IDC_STATIC_ZPJG_VALUE, m_sldSnapSpace.GetPos());
		m_sldBright.SetPos(tParam.iExposureBright);
		SetDlgItemInt(IDC_STATIC_RLBGLD_VALUE, m_sldBright.GetPos());
		
		m_chkShowRule.SetCheck(tParam.iDisplayRule);
		m_chkShowTarget.SetCheck(tParam.iDisplayTarget);

		UI_Update();
		//AddLog(LOG_TYPE_SUCC,"","NetClient_GetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC);
	}
	else
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC);
	}

	return iRet;
}

int CLS_DlgFaceDetection::SetFaceDetect()
{
	int iReturnByte = 0;
	int iRet = -1;

	FaceDetectArithmetic tParam = {0};
	tParam.iBufSize = sizeof(tParam);
	tParam.iSceneID = 0;
	tParam.iDevType = (int)(m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel()));
	iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, &tParam, sizeof(tParam),&iReturnByte);
	if (0 != iRet)
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_GetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC);
		return iRet;
	}

	tParam.iDevType = (int)(m_cboDetectType.GetItemData(m_cboDetectType.GetCurSel()));
	tParam.iPushMode =(int) m_cboTTCL.GetItemData(m_cboTTCL.GetCurSel());
	
	if (1 == tParam.iPushMode || 2 == tParam.iPushMode)
	{
		tParam.iSnapTimes = (int)m_cboZPCS.GetItemData(m_cboZPCS.GetCurSel());//When the push map strategy is the fastest and best, the number of snapshots iSnapTimes takes effect
	}
	else if (4 == tParam.iPushMode)
	{
		tParam.iSnapTimes = (int)m_cboZPCS.GetItemData(m_cboZPCS.GetCurSel());//When the push map policy is timed, the number of snapshots iSnapTimes and snapshot interval iSnapSpace take effect
		tParam.iSnapSpace = m_sldSnapSpace.GetPos();
	}
	else if(6 == tParam.iPushMode)
	{
		//When the push map strategy is continuous, the number of snapshots and snapshot interval do not take effect
	}

	tParam.iSnapMode = (int)m_cboZPMS.GetItemData(m_cboZPMS.GetCurSel());
	if (tParam.iSnapMode == 3)
	{
		tParam.iSnapLevel = m_sldZHZL.GetPos();//When the capture mode is user-defined, the iSnapLevel comprehensive quality takes effect
	} 
	
	tParam.iMinSizeEx = GetDlgItemInt(IDC_EDIT_ZXRLCC);
	tParam.iExposureBright = m_sldBright.GetPos();
	tParam.iDisplayRule = m_chkShowRule.GetCheck();
	tParam.iDisplayTarget = m_chkShowTarget.GetCheck();
	
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNo, &tParam, sizeof(tParam));
	if (0 != iRet)
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_SetDevConfig(%d,%d)",m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC);
	}

	return iRet;
}

int CLS_DlgFaceDetection::GetBigPicUploadParam()
{
	PicStreamUploadParam tInfo = {0};
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 0;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, &tInfo, sizeof(PicStreamUploadParam));
	if (0 == iRet)
	{
		m_sldBigPicQuality.SetPos(tInfo.iQpvalue);
		SetDlgItemInt(IDC_STATIC_FACEDETECT_BIGPIC_VALUE, m_sldBigPicQuality.GetPos());

		m_chkSnapBigPic.SetCheck(tInfo.iSnapEnable);
		m_chkBigPicOsd.SetCheck(tInfo.iIsOsd);
	}
	else
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_VCAGetConfig(%d,%d)",m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM);
	}

	return iRet;
}

int CLS_DlgFaceDetection::SetBigPicUploadParam()
{
	PicStreamUploadParam tInfo = {0};
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 0;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, &tInfo, sizeof(PicStreamUploadParam));
	if (0 != iRet)
	{
		return iRet;
	}
	
	tInfo.iSize = sizeof(PicStreamUploadParam);
	tInfo.iSceneId		= 0;
	tInfo.iRuleNo		= 0;
	tInfo.iPicType		= 0;
	
	tInfo.iSnapEnable	= m_chkSnapBigPic.GetCheck();
	tInfo.iIsOsd		= m_chkBigPicOsd.GetCheck();
	tInfo.iQpvalue		= m_sldBigPicQuality.GetPos();
	
	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, &tInfo, sizeof(PicStreamUploadParam));
	if (0 != iRet)
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_VCASetConfig(%d,%d)",m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM);
	}

	return iRet;
}


int CLS_DlgFaceDetection::GetSmallPicUploadParam()
{
	PicStreamUploadParam tInfo = {0};
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 1;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, &tInfo, sizeof(PicStreamUploadParam));
	if (0 == iRet)
	{
		m_sldSmallPicQuality.SetPos(tInfo.iQpvalue);
		SetDlgItemInt(IDC_STATIC_FACEDETECT_SMALLPIC_VALUE, m_sldSmallPicQuality.GetPos());
	}
	else
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_VCAGetConfig(%d,%d)",m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM);
	}

	return iRet;
}

int CLS_DlgFaceDetection::SetSmallPicUploadParam()
{
	PicStreamUploadParam tInfo = {0};
	tInfo.iSize		= sizeof(PicStreamUploadParam);
	tInfo.iSceneId	= 0;
	tInfo.iPicType	= 1;

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, &tInfo, sizeof(PicStreamUploadParam));
	if (0 != iRet)
	{
		return iRet;
	}

	tInfo.iSize = sizeof(PicStreamUploadParam);
	tInfo.iSceneId		= 0;
	tInfo.iRuleNo		= 0;
	tInfo.iPicType		= 1;
	tInfo.iQpvalue		= m_sldSmallPicQuality.GetPos();

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNo, &tInfo, sizeof(PicStreamUploadParam));
	if (0 != iRet)
	{
		//AddLog(LOG_TYPE_FAIL,"","NetClient_VCASetConfig(%d,%d)",m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM);
	}

	return iRet;
}

void CLS_DlgFaceDetection::OnNMCustomdrawSliderZhzl(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	SetDlgItemInt(IDC_STATIC_ZHZL_VALUE, m_sldZHZL.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceDetection::OnNMCustomdrawSliderZpjg(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_ZPJG_VALUE, m_sldSnapSpace.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceDetection::OnNMCustomdrawSliderRlbgld(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_RLBGLD_VALUE, m_sldBright.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceDetection::OnNMCustomdrawSliderFacedetectBigpic(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_FACEDETECT_BIGPIC_VALUE, m_sldBigPicQuality.GetPos());
	*pResult = 0;
}

void CLS_DlgFaceDetection::OnNMCustomdrawSliderFacedetectSmallpic(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STATIC_FACEDETECT_SMALLPIC_VALUE, m_sldSmallPicQuality.GetPos());
	*pResult = 0;
}
