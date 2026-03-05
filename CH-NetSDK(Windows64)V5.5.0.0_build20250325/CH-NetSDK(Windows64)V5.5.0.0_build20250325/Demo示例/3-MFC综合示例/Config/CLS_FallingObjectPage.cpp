
#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_FallingObjectPage.h"


IMPLEMENT_DYNAMIC(CLS_FallingObjectPage, CDialog)

CLS_FallingObjectPage::CLS_FallingObjectPage(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_FallingObjectPage::IDD, pParent)
	, m_iDectSensitivity(0)
	, m_iMaskSensitivity(0)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	ZeroMemory(&m_tXmlFallObjInfo, sizeof(XmlFallingObject));
}

CLS_FallingObjectPage::~CLS_FallingObjectPage()
{
}

void CLS_FallingObjectPage::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_MODE3, m_cbDectDevType);
	DDX_Control(pDX, IDC_COMBO_MODE33, m_cbMaskDevType);
	DDX_Control(pDX, IDC_COMBO_RELUID, m_cbDectRuleID);
	DDX_Control(pDX, IDC_COMBO_RELUID1, m_cbMaskRuleID);
	DDX_Control(pDX, IDC_COMBO_ALARMRLUE6, m_cbDectAlarmRule);
	DDX_Control(pDX, IDC_COMBO_ALARMRLUE66, m_cbMaskAlarmRule);
	DDX_Control(pDX, IDC_COMBO_ALARMNUM7, m_cbDectAlarmNum);
	DDX_Control(pDX, IDC_COMBO_ALARMNUM77, m_cbMaskAlarmNum);
	DDX_Control(pDX, IDC_COMBO_TARGET8, m_cbDectTarget);
	DDX_Control(pDX, IDC_COMBO_TARGET88, m_cbMaskTarget);
	DDX_Control(pDX, IDC_COMBO_TRACK9, m_cbDectTrack);
	DDX_Control(pDX, IDC_COMBO_TRACK99, m_cbMaskTrack);
	DDX_Control(pDX, IDC_CHECK_ENABLE4, m_checkDectEnable);
	DDX_Control(pDX, IDC_CHECK_ENABLE44, m_checkMaskEnable);

	DDX_Text(pDX, IDC_SLIDER_SENSITIVITY5, m_iDectSensitivity);
	DDV_MinMaxInt(pDX, m_iDectSensitivity, 0, 100);
	DDX_Text(pDX, IDC_SLIDER_SENSITIVITY55, m_iMaskSensitivity);
	DDV_MinMaxInt(pDX, m_iMaskSensitivity, 0, 100);

	DDX_Control(pDX, IDC_COMBO_DETECT_REGON1, m_cbDetectRegNum);
	DDX_Control(pDX, IDC_CHECK_DETECTREG_ENABL2, m_checkInerDectEnab);
	DDX_Control(pDX, IDC_COMBO_OSDTYPE3, m_cbOsdType);
	DDX_Control(pDX, IDC_EDIT_DEC_REG_POINTCOORD, m_editDectPoint);
	DDX_Control(pDX, IDC_BUTTON_DEC_REG_DRAWPOINT, m_btDectDraw);

	DDX_Control(pDX, IDC_COMBO_MASKNUM, m_cbMaskNum);
	DDX_Control(pDX, IDC_EDIT_MASK_REG_POINTCOORD, m_editMaskPoint);
	DDX_Control(pDX, IDC_BUTTON_MASK_DRAWPOINT, m_btMaskDraw);
	DDX_Control(pDX, IDC_BUTTON_ADDMASKREG, m_btMaskAdd);	
	DDX_Control(pDX, IDC_BUTTON_DELETEMASKREG, m_btMaskDelete);


}


BEGIN_MESSAGE_MAP(CLS_FallingObjectPage, CDialog)
	ON_CBN_SELCHANGE(IDC_COMBO_DETECT_REGON1, &CLS_FallingObjectPage::OnCbnSelchangeComboDetectRegon1)
	ON_CBN_SELCHANGE(IDC_COMBO_MASKNUM, &CLS_FallingObjectPage::OnCbnSelchangeComboMasknum)
	ON_BN_CLICKED(IDC_BUTTON_DEC_REG_DRAWPOINT, &CLS_FallingObjectPage::OnBnClickedDrawDect)
	ON_BN_CLICKED(IDC_BUTTON_MASK_DRAWPOINT, &CLS_FallingObjectPage::OnBnClickedDrawMask)
	ON_BN_CLICKED(IDC_BUTTON_SET_DECTREG_INFO, &CLS_FallingObjectPage::OnBnClickedButtonSetDectRegion)
	ON_BN_CLICKED(IDC_BUTTON_SET_MASKREG_INFO, &CLS_FallingObjectPage::OnBnClickedButtonSetMaskRegion)
	ON_CBN_SELCHANGE(IDC_COMBO_RELUID, &CLS_FallingObjectPage::OnCbnSelchangeComboDectReluid)
	ON_CBN_SELCHANGE(IDC_COMBO_RELUID1, &CLS_FallingObjectPage::OnCbnSelchangeComboReluid)
	ON_BN_CLICKED(IDC_BUTTON_ADDMASKREG, &CLS_FallingObjectPage::OnBnClickedButtonAddmaskreg)
	ON_BN_CLICKED(IDC_BUTTON_DELETEMASKREG, &CLS_FallingObjectPage::OnBnClickedButtonDeletemaskreg)
END_MESSAGE_MAP()

void CLS_FallingObjectPage::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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
	FirstUpdateDevInfo();
}

void CLS_FallingObjectPage::UI_UpdateDialog()
{
	DectRegInitial();
	MaskRegInitial();
}

bool CLS_FallingObjectPage::UI_UpdateFallingObjInfo()
{
	if (m_iLogonID < 0) {
		return false;
	}

	if (NULL == NetClient_XmlGetDevConfig) {
		AddLog(LOG_TYPE_FAIL, "", "NULL == NetClient_XmlGetDevConfig");
		return false;
	}
//	memset(&m_tXmlFallObjInfo, 0, sizeof(XmlFallingObject));
// 	OnInitDialog();
	int iRet = NetClient_XmlGetDevConfig(m_iLogonID, NETXMLCFG_CMD_FALLINGOBJECT, &m_tXmlFallObjInfo, sizeof(XmlFallingObject), &m_tXmlFallObjInfo, sizeof(XmlFallingObject));
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlGetDevConfig(%d,%d,NETXMLCFG_CMD_FALLINGOBJECT)",m_iLogonID,m_iChannelNo);
		return false;	
	}
	return true;
}


BOOL CLS_FallingObjectPage::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UI_UpdateDialog();
	return TRUE;
}

void CLS_FallingObjectPage::ShowDectInfoToUI()
{
	m_cbDectRuleID.SetCurSel(m_tXmlFallObjInfo.iRuleId-1);
	SetDlgItemInt(IDC_EDIT_CHANNUM1, m_tXmlFallObjInfo.iChannelNo);
	SetDlgItemInt(IDC_EDIT_SCENEID2, m_tXmlFallObjInfo.iSceneId);
	m_cbDectDevType.SetCurSel(m_tXmlFallObjInfo.iModel);
	m_checkDectEnable.SetCheck(m_tXmlFallObjInfo.iEnabled);
	SetDlgItemInt(IDC_SLIDER_SENSITIVITY5, m_tXmlFallObjInfo.iSensitivity);
	m_cbDectAlarmRule.SetCurSel(m_tXmlFallObjInfo.iDisPlayRule);
	m_cbDectAlarmNum.SetCurSel(m_tXmlFallObjInfo.iDisPlayStat);
	m_cbDectTarget.SetCurSel(m_tXmlFallObjInfo.iDisPlayTarget);
	m_cbDectTrack.SetCurSel(m_tXmlFallObjInfo.iDisPlayTrack);
	SetDlgItemInt(IDC_EDIT_MINSIZE10, m_tXmlFallObjInfo.iMinSize);
	SetDlgItemInt(IDC_EDIT_MAXSIZE11, m_tXmlFallObjInfo.iMaxSize);
	SetDlgItemInt(IDC_EDIT_DETECT_REGOIN12, m_tXmlFallObjInfo.iValidRegionCount);
}

void CLS_FallingObjectPage::ShowMaskInfoToUI()
{
	m_cbMaskRuleID.SetCurSel(m_tXmlFallObjInfo.iRuleId-1);
	SetDlgItemInt(IDC_EDIT_CHANNUM11, m_tXmlFallObjInfo.iChannelNo);
	SetDlgItemInt(IDC_EDIT_SCENEID22, m_tXmlFallObjInfo.iSceneId);
	m_cbMaskDevType.SetCurSel(m_tXmlFallObjInfo.iModel);
	m_checkMaskEnable.SetCheck(m_tXmlFallObjInfo.iEnabled);
	SetDlgItemInt(IDC_SLIDER_SENSITIVITY55, m_tXmlFallObjInfo.iSensitivity);
	m_cbMaskAlarmRule.SetCurSel(m_tXmlFallObjInfo.iDisPlayRule);
	m_cbMaskAlarmNum.SetCurSel(m_tXmlFallObjInfo.iDisPlayStat);
	m_cbMaskTarget.SetCurSel(m_tXmlFallObjInfo.iDisPlayTarget);
	m_cbMaskTrack.SetCurSel(m_tXmlFallObjInfo.iDisPlayTrack);
	SetDlgItemInt(IDC_EDIT_MINSIZE1010, m_tXmlFallObjInfo.iMinSize);
	SetDlgItemInt(IDC_EDIT_MAXSIZE1111, m_tXmlFallObjInfo.iMaxSize);
	SetDlgItemInt(IDC_EDIT_MASK_REGOIN13, m_tXmlFallObjInfo.iMaskRegionCount);
}

void CLS_FallingObjectPage::DefaultShowRegionInfo(int _iRegionFlag)
{
	if (FALLINGOBJECT_VALID_REGION == _iRegionFlag)
	{
		SetDlgItemText(IDC_EDIT_NUMPOINT, "");
		int iRet1 = m_tXmlFallObjInfo.tValidRegion[0].iRegionNo;
		if (iRet1 <= 0)
		{
			return;
		}
		m_cbDetectRegNum.SetCurSel(iRet1 - 1);
		ShowDetectAreaInfo(m_tXmlFallObjInfo.tValidRegion[0]);
	}
	else if (FALLINGOBJECT_MASK_REGION == _iRegionFlag)
	{
		SetDlgItemText(IDC_EDIT_NUMPOINT, "");
		int iRet = m_tXmlFallObjInfo.iMaskRegionCount;
		if (0 == iRet)
		{
			m_cbMaskNum.ResetContent();
			return;
		}
		m_cbMaskNum.SetCurSel(0);
		ShowMaskAreaInfo(m_tXmlFallObjInfo.tMaskRegion[0]);
	}
}

void CLS_FallingObjectPage::ShowDetectAreaInfo( ValidRegionFall _tValidReg )
{
	
	m_cbOsdType.SetCurSel(_tValidReg.iOsdType);
	m_checkInerDectEnab.SetCheck(_tValidReg.iRegionEnabled);
	SetDlgItemInt(IDC_EDIT_COORD_X4, _tValidReg.iOsdPosX);
	SetDlgItemInt(IDC_EDIT_COORD_Y5, _tValidReg.iOsdPosY);
	
	//Display the point coordinates sent by the device to the interface in string form
	vca_TPolygonEx &tPloygon = _tValidReg.tRegionCoord;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	char cSplitSection[]  = "(";
	char cSplitSection1[]  = ", ";
	char cSplitSection2[]  = ")";
	int iApdLen = (int)sizeof(int);
	int iSplitLen = (int)strlen(cSplitSection);
	int iSplitLen1 = (int)strlen(cSplitSection1);
	int iSplitLen2 = (int)strlen(cSplitSection2);
	
	for (int i = 0; i < tPloygon.iPointNum && i < VCA_MAX_POLYGON_POINT_NUMEX; i++)
	{
		char* pcOffset = NULL;
		int iSrcLen = (int)strlen(cPointBuf);
		if ((iSrcLen + 2*iApdLen + iSplitLen + iSplitLen1 + iSplitLen2) > MAX_POINTBUF_LEN)
		{
			AddLog(LOG_TYPE_FAIL, "", "The string of point coordinate set exceeds the maximum length!");
			return;
		}
		pcOffset = cPointBuf + iSrcLen;
		sprintf(pcOffset, "%s%d%s%d%s", cSplitSection, tPloygon.stPoints[i].iX, 
			cSplitSection1, tPloygon.stPoints[i].iY, cSplitSection2);
	}
	CString strTemp;
	int iCurDetcNum = m_cbDetectRegNum.GetCurSel();
	if (tPloygon.iPointNum > VCA_MAX_POLYGON_POINT_NUMEX)
	{
		AddLog(LOG_TYPE_FAIL, "", "the number of points in the current valid area %d exceeds 32, and only 32 points are supported!", iCurDetcNum);
		return;
	}
	strTemp.Format(_T("%d"), _tValidReg.tRegionCoord.iPointNum);
	SetDlgItemText(IDC_EDIT_DEC_REG_POINTNUM, strTemp);

	SetDlgItemText(IDC_EDIT_DEC_REG_POINTCOORD, "");
	m_editDectPoint.ReplaceSel(cPointBuf);
}

void CLS_FallingObjectPage::OnCbnSelchangeComboDetectRegon1()
{
	int iValidRegionCount = m_tXmlFallObjInfo.iValidRegionCount;
	int iRet = m_cbDetectRegNum.GetCurSel() + 1;
	int iTempNo;
	int iFlag = 0;		//0 means there is no current detection area number in the information sent by the device
	for (int i = 0; i < iValidRegionCount && i < MAX_REGIONFALL_COUNT; i++)
	{
		iTempNo = m_tXmlFallObjInfo.tValidRegion[i].iRegionNo;
		if (iRet == iTempNo)
		{
			iFlag = 1;	//1 means there is current detection area number in the information sent by the device
			ShowDetectAreaInfo(m_tXmlFallObjInfo.tValidRegion[i]);
			break;
		}
	}
	if (0 == iFlag)		//No information of the current detection area number is displayed on the interface
	{
		AfxMessageBox(_T("The detect regoin of the current number is not used!"));
		m_cbOsdType.SetCurSel(-1);
		m_checkInerDectEnab.SetCheck(0);
		SetDlgItemText(IDC_EDIT_COORD_X4, "");
		SetDlgItemText(IDC_EDIT_COORD_Y5, "");
		SetDlgItemText(IDC_EDIT_DEC_REG_POINTCOORD, "");
		SetDlgItemText(IDC_EDIT_DEC_REG_POINTNUM, "");
	}
}

void CLS_FallingObjectPage::OnCbnSelchangeComboMasknum()
{
	int iRet = m_cbMaskNum.GetCurSel();
	
	if (iRet >= 0)
	{
		int iPointNum = m_tXmlFallObjInfo.tMaskRegion[iRet].iPointNum;
		if (0 == iPointNum)
		{
			SetDlgItemText(IDC_EDIT_MASK_REG_POINTCOORD, "");
			SetDlgItemText(IDC_EDIT_MASK_REG_POINTCOORD, "");
			SetDlgItemText(IDC_EDIT_NUMPOINT, "");
			return;
		}
		ShowMaskAreaInfo(m_tXmlFallObjInfo.tMaskRegion[iRet]);
	}
}

void CLS_FallingObjectPage::ShowMaskAreaInfo( vca_TPolygonEx _tMaskReg )
{
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	char cSplitSection[]  = "(";
	char cSplitSection1[]  = ", ";
	char cSplitSection2[]  = ")";
	int iApdLen = (int)sizeof(int);
	int iSplitLen = (int)strlen(cSplitSection);
	int iSplitLen1 = (int)strlen(cSplitSection1);
	int iSplitLen2 = (int)strlen(cSplitSection2);

	for (int i = 0; i < _tMaskReg.iPointNum && i < MAX_REGIONFALL_COUNT; i++)
	{
		char* pcOffset = NULL;
		int iSrcLen = (int)strlen(cPointBuf);
		if ((iSrcLen + 2*iApdLen + iSplitLen + iSplitLen1 + iSplitLen2) > MAX_POINTBUF_LEN)
		{
			AddLog(LOG_TYPE_FAIL, "", "The string of point coordinate set exceeds the maximum length!");
			return;
		}
		pcOffset = cPointBuf + iSrcLen;
		sprintf(pcOffset, "%s%d%s%d%s", cSplitSection, _tMaskReg.stPoints[i].iX, 
			cSplitSection1, _tMaskReg.stPoints[i].iY, cSplitSection2);
	}
	CString strTemp;
	int iCurMaskNum = m_cbMaskNum.GetCurSel();
	if (_tMaskReg.iPointNum > VCA_MAX_POLYGON_POINT_NUMEX)
	{
		AddLog(LOG_TYPE_FAIL, "", "the number of points in the current mask area %d exceeds 32, and only 32 points are supported!", iCurMaskNum);
		return;
	}
	strTemp.Format(_T("%d"), m_tXmlFallObjInfo.tMaskRegion[iCurMaskNum].iPointNum);
	
	SetDlgItemText(IDC_EDIT_NUMPOINT, strTemp);
	SetDlgItemText(IDC_EDIT_MASK_REG_POINTCOORD, "");
	m_editMaskPoint.ReplaceSel(cPointBuf);
}

void CLS_FallingObjectPage::OnBnClickedDrawDect()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}
	m_pDlgVideoView->Init(0, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, VCA_MAX_POLYGON_POINT_NUMEX);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[2048] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 1)
		{
			m_editDectPoint.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_DEC_REG_POINTNUM, iPointNum);
		}
		else
		{
			m_editDectPoint.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_DEC_REG_POINTNUM, 0);
		}

		int iRegionNo = m_cbDetectRegNum.GetCurSel() + 1;
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_DEC_REG_POINTNUM);
		vca_TPoint ptPolygon[VCA_MAX_POLYGON_POINT_NUMEX] = {0} ;
		CString cstPolygon = "";
		m_editDectPoint.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		ValidRegionFall tValidRegInfo = {0};
		
		tValidRegInfo.iRegionNo = iRegionNo;
		tValidRegInfo.tRegionCoord.iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i< VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			tValidRegInfo.tRegionCoord.stPoints[i].iX = ptPolygon[i].iX;
			tValidRegInfo.tRegionCoord.stPoints[i].iY = ptPolygon[i].iY;
		}
		//Iterate all, whether it is a new detect region number
		int iCurDectRegCnt = m_tXmlFallObjInfo.iValidRegionCount;
		int iFlag = 0;		//0 means there is no current detection area number in the information sent by the device
		for (int j = 0; j < iCurDectRegCnt && j < MAX_REGIONFALL_COUNT; j++)
		{
			int iTempRegNum = m_tXmlFallObjInfo.tValidRegion[j].iRegionNo;
			if (iRegionNo == iTempRegNum)
			{
				iFlag = 1;	//1 means modification of device information with current detection area number
				memset(&m_tXmlFallObjInfo.tValidRegion[j], 0, sizeof(ValidRegionFall));
				memcpy(&m_tXmlFallObjInfo.tValidRegion[j], &tValidRegInfo, sizeof(ValidRegionFall));
				break;
			}
		}

		if (0 == iFlag)		//the current number is new and stored behind the existing number
		{
			memcpy(&m_tXmlFallObjInfo.tValidRegion[iCurDectRegCnt], &tValidRegInfo, sizeof(ValidRegionFall));
		}

		UpdateDrawFinishRegionNum(FALLINGOBJECT_VALID_REGION);
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;

}
void CLS_FallingObjectPage::OnBnClickedDrawMask()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(0, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter, VCA_MAX_POLYGON_POINT_NUMEX);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[2048] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection, TRUE);
	if (-1 == iSetRet)
	{
		delete m_pDlgVideoView;
		m_pDlgVideoView = NULL;
		return ;
	}
	if (IDOK == m_pDlgVideoView->DoModal())
	{
		if (iPointNum > 1)
		{
			m_editMaskPoint.SetWindowText(cPointBuf);
			SetDlgItemInt(IDC_EDIT_NUMPOINT, iPointNum);
		}
		else
		{
			m_editMaskPoint.SetWindowText(_T(""));
			SetDlgItemInt(IDC_EDIT_NUMPOINT, 0);
		}
		
		int iRegionNo = m_cbMaskNum.GetCurSel();
		int iPointNumTemp = GetDlgItemInt(IDC_EDIT_NUMPOINT);
		vca_TPoint ptPolygon[VCA_MAX_POLYGON_POINT_NUMEX] = {0} ;
		CString cstPolygon = "";
		m_editMaskPoint.GetWindowText(cstPolygon);
		GetPointsFromString(cstPolygon, iPointNumTemp, (POINT*)ptPolygon);
		ValidRegionFall tValidRegInfo = {0};
		memset(&m_tXmlFallObjInfo.tMaskRegion[iRegionNo], 0, sizeof(vca_TPolygonEx));
		m_tXmlFallObjInfo.tMaskRegion[iRegionNo].iPointNum = iPointNumTemp;
		for (int i = 0; i < iPointNumTemp && i< VCA_MAX_POLYGON_POINT_NUMEX ; i++)
		{
			m_tXmlFallObjInfo.tMaskRegion[iRegionNo].stPoints[i].iX = ptPolygon[i].iX;
			m_tXmlFallObjInfo.tMaskRegion[iRegionNo].stPoints[i].iY = ptPolygon[i].iY;
		}
		UpdateDrawFinishRegionNum(FALLINGOBJECT_MASK_REGION);
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;

}

void CLS_FallingObjectPage::UpdateDrawFinishRegionNum(int _iRegoinType)
{
	int iRegionNum = 0;
	if (FALLINGOBJECT_VALID_REGION == _iRegoinType)
	{
		for (int i = 0;i < 8;i++)
		{
			if (m_tXmlFallObjInfo.tValidRegion[i].tRegionCoord.iPointNum > 1)
			{
				iRegionNum++;
			}
		}
		SetDlgItemInt(IDC_EDIT_DETECT_REGOIN12, iRegionNum);
		m_tXmlFallObjInfo.iValidRegionCount = iRegionNum;
	}
	else if (FALLINGOBJECT_MASK_REGION == _iRegoinType)
	{
		iRegionNum = m_tXmlFallObjInfo.iMaskRegionCount+1;
		SetDlgItemInt(IDC_EDIT_MASK_REGOIN13, iRegionNum);
		m_tXmlFallObjInfo.iMaskRegionCount = iRegionNum;
	}
}

void CLS_FallingObjectPage::OnBnClickedButtonSetDectRegion()
{
	if (m_iLogonID < 0) 
	{
		return;
	}

	if (NULL == NetClient_XmlSetDevConfig) {
		AddLog(LOG_TYPE_FAIL, "", "NULL == NetClient_XmlSetDevConfig");
		return;
	}

	int iCurRegNum = m_cbDetectRegNum.GetCurSel() + 1;
	int iCurRegcnt = GetDlgItemInt(IDC_EDIT_DETECT_REGOIN12);
	int iFlag = 0;		//0 means there is no current detection area number in the information sent by the device
	for (int i = 0; i < iCurRegcnt && i < MAX_REGIONFALL_COUNT; i++)
	{
		int iTempRegNum = m_tXmlFallObjInfo.tValidRegion[i].iRegionNo;
		if (iTempRegNum == iCurRegNum)
		{
			iFlag = 1;	//1 means modification of device information with current detection area number
			m_tXmlFallObjInfo.tValidRegion[i].iOsdType = m_cbOsdType.GetCurSel();
			m_tXmlFallObjInfo.tValidRegion[i].iRegionEnabled = m_checkInerDectEnab.GetCheck();
			m_tXmlFallObjInfo.tValidRegion[i].iOsdPosX = GetDlgItemInt(IDC_EDIT_COORD_X4);
			m_tXmlFallObjInfo.tValidRegion[i].iOsdPosY = GetDlgItemInt(IDC_EDIT_COORD_Y5);
			break;
		}
	}
	if (0 == iFlag)		//the current number is new and stored behind the existing number
	{
		m_tXmlFallObjInfo.tValidRegion[iCurRegcnt].iRegionNo = iCurRegNum;
		m_tXmlFallObjInfo.tValidRegion[iCurRegcnt].iOsdType = m_cbOsdType.GetCurSel();
		m_tXmlFallObjInfo.tValidRegion[iCurRegcnt].iRegionEnabled = m_checkInerDectEnab.GetCheck();
		m_tXmlFallObjInfo.tValidRegion[iCurRegcnt].iOsdPosX = GetDlgItemInt(IDC_EDIT_COORD_X4);
		m_tXmlFallObjInfo.tValidRegion[iCurRegcnt].iOsdPosY = GetDlgItemInt(IDC_EDIT_COORD_Y5);
	}
	
	
	UpdateUIRegoinDectSet();
	int iStatus = 0;
	int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNO, SYNC_NET_CLIENT_VCA_SUSPEND, &iStatus, sizeof(int), NULL, 0);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SyncSetDevCfg stop VCA failed logonID(%d)", m_iLogonID);
		return;
	}
	
	iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_CMD_FALLINGOBJECT, &m_tXmlFallObjInfo, sizeof(XmlFallingObject), NULL, 0);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlSetDevConfig(%d,%d,FALLINGOBJECT)",m_iLogonID,m_iChannelNo);
		return;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_XmlSetDevConfig(%d,%d,FALLINGOBJECT)",m_iLogonID, m_iChannelNo);
	}
	Sleep(1000);
	iStatus = 1;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &iStatus, sizeof(int));
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig start VCA failed logonID(%d)", m_iLogonID);
		return;
	}
}

void CLS_FallingObjectPage::OnBnClickedButtonSetMaskRegion()
{
	if (m_iLogonID < 0) 
	{
		return;
	}

	if (NULL == NetClient_XmlSetDevConfig) {
		AddLog(LOG_TYPE_FAIL, "", "NULL == NetClient_XmlSetDevConfig");
		return;
	}

	int iCurRegNum = m_cbDetectRegNum.GetCurSel() + 1;
	int iCurRegcnt = GetDlgItemInt(IDC_EDIT_DETECT_REGOIN12);

	UpdateUIRegoinMaskSet();
	int iStatus = 0;
	int iRet = NetClient_SyncSetDevCfg(m_iLogonID, m_iChannelNO, SYNC_NET_CLIENT_VCA_SUSPEND, &iStatus, sizeof(int), NULL, 0);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SyncSetDevCfg stop VCA failed logonID(%d)", m_iLogonID);
		return;
	}

	iRet = NetClient_XmlSetDevConfig(m_iLogonID, NETXMLCFG_CMD_FALLINGOBJECT, &m_tXmlFallObjInfo, sizeof(XmlFallingObject), NULL, 0);
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_XmlSetDevConfig(%d,%d,NETXMLCFG_CMD_FALLINGOBJECT)",m_iLogonID,m_iChannelNo);
		return;
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_XmlSetDevConfig(%d,%d,FALLINGOBJECT)",m_iLogonID, m_iChannelNo);
	}
	Sleep(1000);
	iStatus = 1;
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_VCA_SUSPEND, m_iChannelNO, &iStatus, sizeof(int));
	if (iRet != 0)
	{
		AddLog(LOG_TYPE_FAIL, "", "NetClient_SetDevConfig start VCA failed logonID(%d)", m_iLogonID);
		return;
	}
}
void CLS_FallingObjectPage::UpdateUIRegoinDectSet()
{
	m_tXmlFallObjInfo.iRuleId = GetDlgItemInt(IDC_COMBO_RELUID);
	m_tXmlFallObjInfo.iChannelNo = GetDlgItemInt(IDC_EDIT_CHANNUM1);
	m_tXmlFallObjInfo.iSceneId = GetDlgItemInt(IDC_EDIT_SCENEID2);
	m_tXmlFallObjInfo.iModel = m_cbDectDevType.GetCurSel();
	m_tXmlFallObjInfo.iEnabled = m_checkDectEnable.GetCheck();
	m_tXmlFallObjInfo.iSensitivity = GetDlgItemInt(IDC_SLIDER_SENSITIVITY5);
	m_tXmlFallObjInfo.iDisPlayRule =m_cbDectAlarmRule.GetCurSel();
	m_tXmlFallObjInfo.iDisPlayStat = m_cbDectAlarmNum.GetCurSel();
	m_tXmlFallObjInfo.iDisPlayTarget = m_cbDectTarget.GetCurSel();
	m_tXmlFallObjInfo.iDisPlayTrack = m_cbDectTrack.GetCurSel();
	m_tXmlFallObjInfo.iMinSize = GetDlgItemInt(IDC_EDIT_MINSIZE10);
	m_tXmlFallObjInfo.iMaxSize = GetDlgItemInt(IDC_EDIT_MAXSIZE11);
	m_tXmlFallObjInfo.iValidRegionCount = GetDlgItemInt(IDC_EDIT_DETECT_REGOIN12);
}

void CLS_FallingObjectPage::UpdateUIRegoinMaskSet()
{
	m_tXmlFallObjInfo.iRuleId = GetDlgItemInt(IDC_COMBO_RELUID1);
	m_tXmlFallObjInfo.iChannelNo = GetDlgItemInt(IDC_EDIT_CHANNUM11);
	m_tXmlFallObjInfo.iSceneId = GetDlgItemInt(IDC_EDIT_SCENEID22);
	m_tXmlFallObjInfo.iModel = m_cbMaskDevType.GetCurSel();
	m_tXmlFallObjInfo.iEnabled = m_checkMaskEnable.GetCheck();
	m_tXmlFallObjInfo.iSensitivity = GetDlgItemInt(IDC_SLIDER_SENSITIVITY55);
	m_tXmlFallObjInfo.iDisPlayRule =m_cbMaskAlarmRule.GetCurSel();
	m_tXmlFallObjInfo.iDisPlayStat = m_cbMaskAlarmNum.GetCurSel();
	m_tXmlFallObjInfo.iDisPlayTarget = m_cbMaskTarget.GetCurSel();
	m_tXmlFallObjInfo.iDisPlayTrack = m_cbMaskTrack.GetCurSel();
	m_tXmlFallObjInfo.iMinSize = GetDlgItemInt(IDC_EDIT_MINSIZE1010);
	m_tXmlFallObjInfo.iMaxSize = GetDlgItemInt(IDC_EDIT_MAXSIZE1111);
	m_tXmlFallObjInfo.iMaskRegionCount = GetDlgItemInt(IDC_EDIT_MASK_REGOIN13);
}

void CLS_FallingObjectPage::OnCbnSelchangeComboDectReluid()
{
	memset(&m_tXmlFallObjInfo, 0, sizeof(XmlFallingObject));
	
	int iRuleId = GetDlgItemInt(IDC_COMBO_RELUID);
	int iSceneId = m_cbDectRuleID.GetCurSel()+1;
	int iModel = m_cbDectDevType.GetCurSel();
	int iChannelNo = m_iChannelNo;
	DectRegInitial();
	m_tXmlFallObjInfo.iRuleId = iRuleId;
	m_tXmlFallObjInfo.iSceneId = iSceneId;
	m_tXmlFallObjInfo.iModel = iModel;
	m_tXmlFallObjInfo.iChannelNo = iChannelNo;

	int iRet = UI_UpdateFallingObjInfo();
	if (false == iRet)
	{
		SetDlgItemInt(IDC_COMBO_RELUID, iRuleId);
		m_cbDectRuleID.SetCurSel(iSceneId - 1);
		m_cbDectDevType.SetCurSel(iModel);
		SetDlgItemInt(IDC_EDIT_CHANNUM1, iChannelNo);
		return;
	}
	ShowDectInfoToUI();
	
	DefaultShowRegionInfo(FALLINGOBJECT_VALID_REGION);
	
}
void CLS_FallingObjectPage::OnCbnSelchangeComboReluid()
{
	memset(&m_tXmlFallObjInfo, 0, sizeof(XmlFallingObject));

	int iRuleId = GetDlgItemInt(IDC_COMBO_RELUID1);
	int iSceneId = m_cbMaskRuleID.GetCurSel()+1;
	int iModel = m_cbMaskDevType.GetCurSel();
	int iChannelNo = m_iChannelNo;
	MaskRegInitial();
	m_tXmlFallObjInfo.iRuleId = iRuleId;
	m_tXmlFallObjInfo.iSceneId = iSceneId;
	m_tXmlFallObjInfo.iModel = iModel;
	m_tXmlFallObjInfo.iChannelNo = iChannelNo;

	int iRet = UI_UpdateFallingObjInfo();
	if (false == iRet)
	{
		SetDlgItemInt(IDC_COMBO_RELUID1, iRuleId);
		m_cbMaskRuleID.SetCurSel(iSceneId - 1);
		m_cbMaskDevType.SetCurSel(iModel);
		SetDlgItemInt(IDC_EDIT_CHANNUM11, iChannelNo);
		return;
	}
	ShowMaskInfoToUI();
	DefaultShowRegionInfo(FALLINGOBJECT_VALID_REGION);
}

void CLS_FallingObjectPage::OnBnClickedButtonAddmaskreg()
{
	int iRet = m_tXmlFallObjInfo.iMaskRegionCount;
	CString strTemp;
	m_cbMaskNum.ResetContent();
	for (int i = 0; i <= iRet; i++)
	{
		strTemp.Format(_T("%d"), i+1);
		m_cbMaskNum.InsertString(i, strTemp);
	}
	m_cbMaskNum.SetCurSel(iRet);
	SetDlgItemText(IDC_EDIT_MASK_REGOIN13, strTemp);
	m_btMaskDraw.EnableWindow(TRUE);
	SetDlgItemText(IDC_EDIT_MASK_REG_POINTCOORD, "");
	SetDlgItemText(IDC_EDIT_NUMPOINT, "");

}
void CLS_FallingObjectPage::OnBnClickedButtonDeletemaskreg()
{
	int iMaskRegNum = m_cbMaskNum.GetCurSel();
	int iMaskCount = m_tXmlFallObjInfo.iMaskRegionCount;
	if (0 == iMaskCount || iMaskCount > 8)
	{
		return;
	}
	if (iMaskRegNum == iMaskCount)	//Only the number is simply added, and no processing has been done for the number, so only the number be deleted
	{
		m_cbMaskNum.ResetContent();
		CString strTemp;
		for (int i = 0; i < iMaskCount && i < MAX_REGIONFALL_COUNT; i++)
		{
			strTemp.Format(_T("%d"), i+1);
			m_cbMaskNum.InsertString(i, strTemp);
		}
		SetDlgItemInt(IDC_EDIT_MASK_REGOIN13, iMaskCount);
		m_cbMaskNum.SetCurSel(iMaskCount-1);
		if ((iMaskCount -1) >= 0 && iMaskCount <= MAX_REGIONFALL_COUNT)
		{
			ShowMaskAreaInfo(m_tXmlFallObjInfo.tMaskRegion[iMaskCount -1]);
		}
		return;
	}
	//Delete the current numbered information
	vca_TPolygonEx	tMaskRegion[MAX_REGIONFALL_COUNT] = {0};
	memcpy(&tMaskRegion, &m_tXmlFallObjInfo.tMaskRegion, MAX_REGIONFALL_COUNT *sizeof(vca_TPolygonEx));
	memset(&m_tXmlFallObjInfo.tMaskRegion, 0, MAX_REGIONFALL_COUNT *sizeof(vca_TPolygonEx));
	int iNum = 0;
	for (int i = 0; i < iMaskCount && i < MAX_REGIONFALL_COUNT; i++)
	{
		if (iMaskRegNum == i)
		{
			continue;
		}
		memcpy(&m_tXmlFallObjInfo.tMaskRegion[iNum], &tMaskRegion[i], sizeof(vca_TPolygonEx));
		iNum++;
	}
	m_tXmlFallObjInfo.iMaskRegionCount--;
	iMaskCount--;
	m_cbMaskNum.ResetContent();
	CString strTemp;
	for (int i = 0; i < iMaskCount && i < MAX_REGIONFALL_COUNT; i++)
	{
		strTemp.Format(_T("%d"), i+1);
		m_cbMaskNum.InsertString(i, strTemp);
	}
	SetDlgItemInt(IDC_EDIT_MASK_REGOIN13, m_tXmlFallObjInfo.iMaskRegionCount);
	m_cbMaskNum.SetCurSel(iMaskCount - 1);
	if ((iMaskCount -1) >= 0 && iMaskCount <= MAX_REGIONFALL_COUNT)
	{
		ShowMaskAreaInfo(m_tXmlFallObjInfo.tMaskRegion[iMaskCount -1]);
	}

}

void CLS_FallingObjectPage::OnBnClickedButtonDeleteDectreg()
{
	SetDlgItemText(IDC_EDIT_MASK_REG_POINTCOORD, "");
	SetDlgItemText(IDC_EDIT_NUMPOINT, "");
	int iDetecRegNum = m_cbDetectRegNum.GetCurSel() + 1;
	int iDetecCount = m_tXmlFallObjInfo.iValidRegionCount;
	if (iDetecCount <= 1)
	{
		AddLog(LOG_TYPE_FAIL,"","Currently, there is only one detection area, which cannot be cleared!");
		return;
	}
	ValidRegionFall tValidRegFall[MAX_REGIONFALL_COUNT] = {0};
	memcpy(&tValidRegFall, &m_tXmlFallObjInfo.tValidRegion, MAX_REGIONFALL_COUNT *sizeof(ValidRegionFall));
	memset(&m_tXmlFallObjInfo.tValidRegion, 0, MAX_REGIONFALL_COUNT *sizeof(ValidRegionFall));
	int iNum = 0;
	for (int i = 0; i < iDetecCount && i < MAX_REGIONFALL_COUNT; i++)
	{
		int iCurRegNum = tValidRegFall[i].iRegionNo;
		if ((iDetecRegNum+1) == iCurRegNum)
		{
			continue;
		}
		memcpy(&m_tXmlFallObjInfo.tValidRegion[iNum], &tValidRegFall[i], sizeof(ValidRegionFall));
		iNum++;
	}
	m_tXmlFallObjInfo.iValidRegionCount--;
	SetDlgItemInt(IDC_EDIT_DETECT_REGOIN12, m_tXmlFallObjInfo.iValidRegionCount);
} 


void CLS_FallingObjectPage::OnLanguageChanged( int _iLanguage )
{
	UI_UpdateDialog();
	memset(&m_tXmlFallObjInfo, 0, sizeof(XmlFallingObject));
	FirstUpdateDevInfo();
}

void CLS_FallingObjectPage::DectRegInitial()
{
	m_cbDectDevType.ResetContent();
	m_cbDectDevType.InsertString(0, GetTextByLan(_T("nvr"), _T("nvr")));
	m_cbDectDevType.InsertString(1, GetTextByLan(_T("ipc"), _T("ipc")));

	SetDlgItemText(IDC_EDIT_CHANNUM1, "");
	m_cbDectRuleID.ResetContent();
	m_cbDectRuleID.InsertString(0,"1");
	m_cbDectRuleID.InsertString(1,"2");
	m_cbDectRuleID.InsertString(2,"3");
	m_cbDectRuleID.InsertString(3,"4");
	m_cbDectRuleID.InsertString(4,"5");
	m_cbDectRuleID.InsertString(5,"6");
	m_cbDectRuleID.InsertString(6,"7");
	m_cbDectRuleID.InsertString(7,"8");
	m_cbDectRuleID.InsertString(8,"9");
	m_cbDectRuleID.InsertString(9, "10");
	m_cbDectRuleID.InsertString(10, "11");
	m_cbDectRuleID.InsertString(11, "12");
	m_cbDectRuleID.InsertString(12, "13");
	m_cbDectRuleID.InsertString(13, "14");
	m_cbDectRuleID.InsertString(14, "15");
	SetDlgItemText(IDC_EDIT_SCENEID2, "");
	SetDlgItemText(IDC_EDIT_MINSIZE10, "");
	SetDlgItemText(IDC_EDIT_MAXSIZE11, "");
	SetDlgItemText(IDC_SLIDER_SENSITIVITY5, "");

	m_cbDectAlarmRule.ResetContent();
	m_cbDectAlarmRule.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbDectAlarmRule.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbDectAlarmRule.SetCurSel(-1);

	m_cbDectAlarmNum.ResetContent();
	m_cbDectAlarmNum.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbDectAlarmNum.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbDectAlarmNum.SetCurSel(-1);

	m_cbDectTarget.ResetContent();
	m_cbDectTarget.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbDectTarget.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbDectTarget.SetCurSel(-1);

	m_cbDectTrack.ResetContent();
	m_cbDectTrack.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbDectTrack.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbDectTrack.SetCurSel(-1);

	m_checkDectEnable.SetCheck(0);

	SetDlgItemText(IDC_STATIC_RULE_ID, GetTextByLan(_T("规则ID"), _T("Rule ID")));
	SetDlgItemText(IDC_STATIC_CHANNELNUM, GetTextByLan(_T("通道号"), _T("Channel Num")));
	SetDlgItemText(IDC_STATIC_SCENE_ID, GetTextByLan(_T("场景ID"), _T("Scene ID")));
	SetDlgItemText(IDC_STATIC_MODE_FALL, GetTextByLan(_T("设备类型"), _T("Device Type")));
	SetDlgItemText(IDC_STATIC_ALARM_RULE, GetTextByLan(_T("报警规则"), _T("Alarm Rule")));
	SetDlgItemText(IDC_STATIC_ALARM_COUNT, GetTextByLan(_T("报警计数"), _T("Alarm Count")));
	SetDlgItemText(IDC_STATIC_TARGET, GetTextByLan(_T("目标"), _T("Target")));
	SetDlgItemText(IDC_STATIC_TRACK, GetTextByLan(_T("轨迹"), _T("Track")));
	SetDlgItemText(IDC_STATIC_SENSITIVITY, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
	SetDlgItemText(IDC_STATIC_MIN_SIZE, GetTextByLan(_T("最小尺寸"), _T("Min Size")));
	SetDlgItemText(IDC_STATIC_MAX_SIZE, GetTextByLan(_T("最大尺寸"), _T("Max Size")));
	SetDlgItemText(IDC_STATIC_DETECT_COUNT, GetTextByLan(_T("区域数量"), _T("Region Count")));
	SetDlgItemText(IDC_STATIC_MASK_COUNT, GetTextByLan(_T("区域数量"), _T("Region Count")));

	SetDlgItemText(IDC_STATIC_DETEC_REG_SET, GetTextByLan(_T("检测区域设置"), _T("Detec Regn Set")));

	//IDC_STATIC_REGOIN_TYPE
	SetDlgItemText(IDC_STATIC_DETEC_REG_NUM, GetTextByLan(_T("区域编号"), _T("Region Num")));
	SetDlgItemText(IDC_STATIC_OSD_TYPE, GetTextByLan(_T("Osd 类型"), _T("OSD Type")));
	SetDlgItemText(IDC_STATIC_COORD_X, GetTextByLan(_T("横坐标"), _T("X")));
	SetDlgItemText(IDC_STATIC_COORD_Y, GetTextByLan(_T("纵坐标"), _T("Y")));
	SetDlgItemText(IDC_STATIC_DEC_REG_POINTNUM, GetTextByLan(_T("点个数"), _T("Coord Num")));
	SetDlgItemText(IDC_BUTTON_DELETE_DEC_REG, GetTextByLan(_T("删除区域"), _T("Delete Region")));
	SetDlgItemText(IDC_BUTTON_DEC_REG_DRAWPOINT, GetTextByLan(_T("区域绘制"), _T("Draw Region")));
	SetDlgItemText(IDC_BUTTON_SET_DECTREG_INFO, GetTextByLan(_T("检测区域设置"), _T("Detec Area Set")));
	SetDlgItemText(IDC_CHECK_ENABLE4, GetTextByLan(_T("使能"), _T("Enable")));
	SetDlgItemText(IDC_CHECK_DETECTREG_ENABL2, GetTextByLan(_T("区域使能"), _T("Regoin Enable")));
	
	m_cbDetectRegNum.ResetContent();
	m_cbDetectRegNum.InsertString(0, "1");
	m_cbDetectRegNum.InsertString(1, "2");
	m_cbDetectRegNum.InsertString(2, "3");
	m_cbDetectRegNum.InsertString(3, "4");
	m_cbDetectRegNum.InsertString(4, "5");
	m_cbDetectRegNum.InsertString(5, "6");
	m_cbDetectRegNum.InsertString(6, "7");
	m_cbDetectRegNum.InsertString(7, "8");

	m_cbOsdType.ResetContent();
	m_cbOsdType.InsertString(0, GetTextByLan(_T("不叠加"), _T("Not Osd")));
	m_cbOsdType.InsertString(1, GetTextByLan(_T("高空抛物报警"), _T("Falling Object Alarm")));
	m_cbOsdType.SetCurSel(-1);

	
	SetDlgItemText(IDC_EDIT_DETECT_REGOIN12, "");

	SetDlgItemText(IDC_EDIT_COORD_X4, "");
	SetDlgItemText(IDC_EDIT_COORD_Y5, "");
	m_checkInerDectEnab.SetCheck(0);
	SetDlgItemText(IDC_EDIT_DEC_REG_POINTCOORD, "");
	SetDlgItemText(IDC_EDIT_DEC_REG_POINTNUM, "");
}

void CLS_FallingObjectPage::MaskRegInitial()
{
	m_cbMaskDevType.ResetContent();
	m_cbMaskDevType.InsertString(0, GetTextByLan(_T("nvr"), _T("nvr")));
	m_cbMaskDevType.InsertString(1, GetTextByLan(_T("ipc"), _T("ipc")));

	SetDlgItemText(IDC_EDIT_CHANNUM11, "");

	m_cbMaskRuleID.ResetContent();
	m_cbMaskRuleID.InsertString(0,"1");
	m_cbMaskRuleID.InsertString(1,"2");
	m_cbMaskRuleID.InsertString(2,"3");
	m_cbMaskRuleID.InsertString(3,"4");
	m_cbMaskRuleID.InsertString(4,"5");
	m_cbMaskRuleID.InsertString(5,"6");
	m_cbMaskRuleID.InsertString(6,"7");
	m_cbMaskRuleID.InsertString(7,"8");
	m_cbMaskRuleID.InsertString(8,"9");
	m_cbMaskRuleID.InsertString(9, "10");
	m_cbMaskRuleID.InsertString(10, "11");
	m_cbMaskRuleID.InsertString(11, "12");
	m_cbMaskRuleID.InsertString(12, "13");
	m_cbMaskRuleID.InsertString(13, "14");
	m_cbMaskRuleID.InsertString(14, "15");
//m_cbMaskRuleID.SetCurSel(0);

	SetDlgItemText(IDC_EDIT_SCENEID22, "");
	SetDlgItemText(IDC_EDIT_MINSIZE10, "");
	SetDlgItemText(IDC_EDIT_MAXSIZE11, "");
	SetDlgItemText(IDC_SLIDER_SENSITIVITY55, "");

	m_cbMaskAlarmRule.ResetContent();
	m_cbMaskAlarmRule.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbMaskAlarmRule.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbMaskAlarmRule.SetCurSel(-1);

	m_cbMaskAlarmNum.ResetContent();
	m_cbMaskAlarmNum.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbMaskAlarmNum.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbMaskAlarmNum.SetCurSel(-1);

	m_cbMaskTarget.ResetContent();
	m_cbMaskTarget.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbMaskTarget.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbMaskTarget.SetCurSel(-1);

	m_cbMaskTrack.ResetContent();
	m_cbMaskTrack.InsertString(0, GetTextByLan(_T("显示"), _T("Display")));
	m_cbMaskTrack.InsertString(1, GetTextByLan(_T("不显示"), _T("Not Display")));
	m_cbMaskTrack.SetCurSel(-1);

	m_checkMaskEnable.SetCheck(0);

	SetDlgItemText(IDC_STATIC_RULE_ID2, GetTextByLan(_T("规则ID"), _T("Rule ID")));
	SetDlgItemText(IDC_STATIC_CHANNELNUM2, GetTextByLan(_T("通道号"), _T("Channel Num")));
	SetDlgItemText(IDC_STATIC_SCENE_ID2, GetTextByLan(_T("场景ID"), _T("Scene ID")));
	//	SetDlgItemInt(IDC_EDIT_SCENEID2,0);
	SetDlgItemText(IDC_STATIC_MODE_FALL2, GetTextByLan(_T("设备类型"), _T("Device Type")));
	SetDlgItemText(IDC_STATIC_ALARM_RULE2, GetTextByLan(_T("报警规则"), _T("Alarm Rule")));
	SetDlgItemText(IDC_STATIC_ALARM_COUNT2, GetTextByLan(_T("报警计数"), _T("Alarm Count")));
	SetDlgItemText(IDC_STATIC_TARGET2, GetTextByLan(_T("目标"), _T("Target")));
	SetDlgItemText(IDC_STATIC_TRACK2, GetTextByLan(_T("轨迹"), _T("Track")));
	SetDlgItemText(IDC_STATIC_SENSITIVITY2, GetTextByLan(_T("灵敏度"), _T("Sensitivity")));
	SetDlgItemText(IDC_STATIC_MIN_SIZE2, GetTextByLan(_T("最小尺寸"), _T("Min Size")));
	SetDlgItemText(IDC_STATIC_MAX_SIZE2, GetTextByLan(_T("最大尺寸"), _T("Max Size")));
	SetDlgItemText(IDC_STATIC_MASK_COUNT, GetTextByLan(_T("区域数量"), _T("Region Count")));
	SetDlgItemText(IDC_STATIC_MASK_REG_SET, GetTextByLan(_T("屏蔽区域设置"), _T("Mask Regn Set")));
	SetDlgItemText(IDC_STATIC_MASK_REGNUM, GetTextByLan(_T("区域编号"), _T("Region Num")));
	SetDlgItemText(IDC_STATIC_MASK_POINTNUM, GetTextByLan(_T("点个数"), _T("Coord Num")));
	SetDlgItemText(IDC_BUTTON_ADDMASKREG, GetTextByLan(_T("添加区域"), _T("Add Regoin")));
	SetDlgItemText(IDC_BUTTON_DELETEMASKREG, GetTextByLan(_T("删除区域"), _T("Delete Region")));
	SetDlgItemText(IDC_BUTTON_MASK_DRAWPOINT, GetTextByLan(_T("区域绘制"), _T("Draw Region")));
	SetDlgItemText(IDC_BUTTON_SET_MASKREG_INFO, GetTextByLan(_T("屏蔽区域设置"), _T("Mask Area Set")));
	SetDlgItemText(IDC_CHECK_ENABLE44, GetTextByLan(_T("使能"), _T("Enable")));

	m_cbMaskNum.SetCurSel(-1);
	SetDlgItemText(IDC_EDIT_MINSIZE1010, "");
	SetDlgItemText(IDC_EDIT_MAXSIZE1111, "");
	SetDlgItemText(IDC_EDIT_MASK_REGOIN13, "");
	SetDlgItemText(IDC_EDIT_MASK_REG_POINTCOORD, "");
	SetDlgItemText(IDC_EDIT_NUMPOINT, "");
}

void CLS_FallingObjectPage::FirstUpdateDevInfo()
{
	//First, default:iRuleId = 1, iSceneId = 0, iModel = 1, 
	m_tXmlFallObjInfo.iRuleId = 1;
	m_tXmlFallObjInfo.iSceneId = 0;
	m_tXmlFallObjInfo.iModel = 1;
	m_tXmlFallObjInfo.iChannelNo = m_iChannelNo;

	int iRet = UI_UpdateFallingObjInfo();
	if (false == iRet)
	{
		return;
	}

	if (m_tXmlFallObjInfo.iMaskRegionCount > 0)
	{
		m_cbMaskNum.ResetContent();
		CString strTemp;
		for (int i = 0; i < m_tXmlFallObjInfo.iMaskRegionCount; i++)
		{
			strTemp.Format(_T("%d"), i+1);
			m_cbMaskNum.InsertString(i, strTemp);
		}
	}
	ShowDectInfoToUI();
	ShowMaskInfoToUI();
	DefaultShowRegionInfo(FALLINGOBJECT_VALID_REGION);
	DefaultShowRegionInfo(FALLINGOBJECT_MASK_REGION);
}


