// .\Config\CLS_DlgCalibrateXY.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "CLS_DlgCalibrateXY.h"

#define AUTOTEST_MULT_PARA_NUM   7

// CLS_DlgCalibrateXY dialog

IMPLEMENT_DYNAMIC(CLS_DlgCalibrateXY, CDialog)

CLS_DlgCalibrateXY::CLS_DlgCalibrateXY(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgCalibrateXY::IDD, pParent)
{

}

CLS_DlgCalibrateXY::~CLS_DlgCalibrateXY()
{
	memset(m_tParam, 0, sizeof(m_tParam));
}

void CLS_DlgCalibrateXY::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_CALIBRATEXY_POSNUM, m_CboCalibrateXYPosNum);
	DDX_Control(pDX, IDC_COMBO_CALIBRATEXY_POSINDEX, m_CboCalibrateXYPosIndex);
	DDX_Control(pDX, IDC_COMBO_CALIBRATEXY_CHNUM, m_CboCalibrateXYChNum);
	DDX_Control(pDX, IDC_COMBO_CALIBRATEXY_PICNO, m_CboCalibrateXYPicNo);
	DDX_Control(pDX, IDC_COMBO_TESTITEM, m_CboTestItem);
	DDX_Control(pDX, IDC_COMBO_PARANUM, m_CboParaNum);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM1, m_EdtTestParam1);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM2, m_EdtTestParam2);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM3, m_EdtTestParam3);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM4, m_EdtTestParam4);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM5, m_EdtTestParam5);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM6, m_EdtTestParam6);
	DDX_Control(pDX, IDC_EDIT_TESTPARAM7, m_EdtTestParam7);
}


BEGIN_MESSAGE_MAP(CLS_DlgCalibrateXY, CLS_BasePage)
	ON_BN_CLICKED(IDC_BUTTON_CALIBRATEXY_SAVE, &CLS_DlgCalibrateXY::OnBnClickedButtonCalibratexySave)
	ON_BN_CLICKED(IDC_BUTTON_CALIBRATEXY_SET, &CLS_DlgCalibrateXY::OnBnClickedButtonCalibratexySet)
	ON_BN_CLICKED(IDC_BUTTON_AUTOTESTMULT_SET, &CLS_DlgCalibrateXY::OnBnClickedButtonAutotestmultSet)
	ON_CBN_SELCHANGE(IDC_COMBO_CALIBRATEXY_CHNUM, &CLS_DlgCalibrateXY::OnCbnSelchangeComboCalibratexyChnum)
	ON_CBN_SELCHANGE(IDC_COMBO_CALIBRATEXY_PICNO, &CLS_DlgCalibrateXY::OnCbnSelchangeComboCalibratexyPicno)
	ON_CBN_SELCHANGE(IDC_COMBO_CALIBRATEXY_POSNUM, &CLS_DlgCalibrateXY::OnCbnSelchangeComboCalibratexyPosnum)
	ON_CBN_SELCHANGE(IDC_COMBO_PARANUM, &CLS_DlgCalibrateXY::OnCbnSelchangeComboParanum)
END_MESSAGE_MAP()


// CLS_DlgCalibrateXY message handler
BOOL CLS_DlgCalibrateXY::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;
}

void CLS_DlgCalibrateXY::UpdateUIText()
{
	m_CboCalibrateXYPosNum.ResetContent();
	m_CboCalibrateXYPosIndex.ResetContent();
	m_CboCalibrateXYChNum.ResetContent();
	m_CboCalibrateXYPicNo.ResetContent();

	for (int i = 0; i < LEN_16; i++)
	{
		m_CboCalibrateXYPosNum.InsertString(i, IntToCString(i + 1));
		m_CboCalibrateXYPosIndex.InsertString(i, IntToCString(i + 1));
	}
	m_CboCalibrateXYPosNum.SetCurSel(0);    
	m_CboCalibrateXYPosIndex.SetCurSel(0);


	for (int i = 0; i < LEN_32; i++)
	{
		m_CboCalibrateXYChNum.InsertString(i, IntToCString(i + 1));
		m_CboCalibrateXYPicNo.InsertString(i, IntToCString(i));
	}
	m_CboCalibrateXYChNum.SetCurSel(0);
	m_CboCalibrateXYPicNo.SetCurSel(0);

	m_CboTestItem.ResetContent();
	m_CboTestItem.SetItemData(0, m_CboTestItem.AddString(GetTextByLan(_T("1-热像模组锅盖标定"), _T("1-Calibration of Lid"))));
	m_CboTestItem.SetItemData(1, m_CboTestItem.AddString(GetTextByLan(_T("2-热像模组温度标定"), _T("2-Calibration of Tem"))));
	m_CboTestItem.SetItemData(2, m_CboTestItem.AddString(GetTextByLan(_T("3-热像模组距离标定"), _T("3-Calibration of Dis"))));
	m_CboTestItem.SetCurSel(0);

	m_CboParaNum.ResetContent();
	for (int i = 0; i < AUTOTEST_MULT_PARA_NUM; i++)
	{
		m_CboParaNum.InsertString(i, IntToCString(i + 1));
	}

	m_EdtTestParam1.EnableWindow(FALSE);
	m_EdtTestParam2.EnableWindow(FALSE);
	m_EdtTestParam3.EnableWindow(FALSE);
	m_EdtTestParam4.EnableWindow(FALSE);
	m_EdtTestParam5.EnableWindow(FALSE);
	m_EdtTestParam6.EnableWindow(FALSE);
	m_EdtTestParam7.EnableWindow(FALSE);

}

void CLS_DlgCalibrateXY::UpdateParam()
{
	CalibrationInfo tPara = {0};
	tPara.iSize = (int)sizeof(CalibrationInfo);
	tPara.iCalPtNo = m_CboCalibrateXYPosIndex.GetCurSel();
	int iReturn = -1;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_CALIBRATEXY, m_iChannelNo, &tPara, (int)sizeof(tPara), &iReturn);
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgCalibrateXY] NetClient_GetDevConfig(NET_CLIENT_CALIBRATEXY) Success!LogonID(%d)",m_iLogonID);

		m_CboCalibrateXYPosNum.SetCurSel(tPara.iCalPtCount-1);
		//UpdataIndexCountByPosNum(tPara.iPosNum);
		m_CboCalibrateXYPosIndex.SetCurSel(tPara.iCalPtNo);
		m_CboCalibrateXYChNum.SetCurSel(tPara.iCalScreenCount - 1);
		int iIndex = m_CboCalibrateXYPicNo.GetCurSel();
		SetDlgItemInt(IDC_EDIT_CALIBRATEXY_CHNO, tPara.tParam[iIndex].iChanNo);
		SetDlgItemInt(IDC_EDIT_CALIBRATEXY_X, tPara.tParam[iIndex].tPoint.iX);
		SetDlgItemInt(IDC_EDIT_CALIBRATEXY_Y, tPara.tParam[iIndex].tPoint.iY);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgCalibrateXY] NetClient_GetDevConfig(NET_CLIENT_CALIBRATEXY) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgCalibrateXY::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
{
	m_iLogonID = _iLogonID;
	m_iChannelNo = _iChannelNo;
	if (_iChannelNo < 0)
	{
		m_iChannelNo = 0;
	}
	else
	{
		m_iChannelNo = _iChannelNo;
	}
	//UpdateUIText();
	UpdateParam();
}

void CLS_DlgCalibrateXY::UpdataIndexCountByPosNum(int _iPosNum)
{
	for (int i = 0; i < _iPosNum && i < LEN_16; i++)
	{
		m_CboCalibrateXYPosIndex.InsertString(i, IntToCString(i + 1));
	}
}
void CLS_DlgCalibrateXY::OnBnClickedButtonCalibratexySave()
{
	// TODO: Add control notification handler code here
	int iIndex = m_CboCalibrateXYPicNo.GetCurSel();
	if ( iIndex < 0 || iIndex >= LEN_32)
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgCalibrateXY]Save Param");
		return;
	}
	m_tParam[iIndex].iChanNo = GetDlgItemInt(IDC_EDIT_CALIBRATEXY_CHNO);
	m_tParam[iIndex].tPoint.iX = GetDlgItemInt(IDC_EDIT_CALIBRATEXY_X);
	m_tParam[iIndex].tPoint.iY = GetDlgItemInt(IDC_EDIT_CALIBRATEXY_Y);

	AddLog(LOG_TYPE_SUCC,"","[CLS_DlgCalibrateXY]Save Param");
}

void CLS_DlgCalibrateXY::OnBnClickedButtonCalibratexySet()
{
	// TODO: Add control notification handler code here
	if (m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgCalibrateXY] m_iChannelNO(%d)",m_iChannelNO);
	}
	CalibrationInfo tPara = {0};
	tPara.iSize = (int)sizeof(CalibrationInfo);
	tPara.iCalPtCount = m_CboCalibrateXYPosNum.GetCurSel()+1;
	tPara.iCalPtNo = m_CboCalibrateXYPosIndex.GetCurSel();
	tPara.iCalScreenCount = m_CboCalibrateXYChNum.GetCurSel()+1;
	memcpy(tPara.tParam, m_tParam, sizeof(tPara.tParam));
	int iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_CALIBRATEXY, m_iChannelNo, &tPara, (int)sizeof(tPara));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgCalibrateXY] NetClient_SetDevConfig(NET_CLIENT_CALIBRATEXY) Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgCalibrateXY] NetClient_SetDevConfig(NET_CLIENT_CALIBRATEXY) Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgCalibrateXY::OnBnClickedButtonAutotestmultSet()
{
	// TODO: Add control notification handler code here
	if (m_iChannelNO < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgCalibrateXY] m_iChannelNO(%d)",m_iChannelNO);
	}
	AutotestMultQuery tInfo = {0};
	tInfo.iSize = (int)sizeof(tInfo);
	tInfo.iChannelNo = m_iChannelNo<0 ? 0 : m_iChannelNo;
	tInfo.iTestItem = m_CboTestItem.GetCurSel() + 1;
	tInfo.iParamNum = m_CboParaNum.GetCurSel() + 1;
	for (int i = 0; i < tInfo.iParamNum && i < AUTOTEST_MULT_PARA_NUM; i++)
	{
		CString cstrParam;
		GetDlgItem(IDC_EDIT_TESTPARAM1+i)->GetWindowText(cstrParam);
		memcpy(tInfo.pcParam[i], cstrParam.GetBuffer(), sizeof(tInfo.pcParam[i]));
	}
	//int iRet = NetClient_InnerAutoTest(m_iLogonID, AUTOTESTMULT, &tInfo, (int)sizeof(tInfo));
	int iRet = NetClient_SendCommand(m_iLogonID,  COMMAND_ID_AUTOTESTMULT,  m_iChannelNO,  &tInfo, (int)sizeof(tInfo));
	if (RET_SUCCESS == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","[CLS_DlgCalibrateXY]NetClient_SendCommand(COMMAND_ID_AUTOTESTMULT)Success!LogonID(%d)",m_iLogonID);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","[CLS_DlgCalibrateXY]NetClient_SendCommand(COMMAND_ID_AUTOTESTMULT)Failed!LogonID(%d), Error(0x%08x)",m_iLogonID, ::GetLastError());
	}
}

void CLS_DlgCalibrateXY::OnCbnSelchangeComboCalibratexyChnum()
{
	// TODO: Add control notification handler code here
	memset(m_tParam, 0, sizeof(m_tParam));

	m_CboCalibrateXYPicNo.ResetContent();
	int iCount = m_CboCalibrateXYChNum.GetCurSel();
	for (int i = 0; i <= iCount && i < LEN_32; i++)
	{
		m_CboCalibrateXYPicNo.InsertString(i, IntToCString(i));
	}
	m_CboCalibrateXYPicNo.SetCurSel(0);
// 	SetDlgItemInt(IDC_EDIT_CALIBRATEXY_CHNO, 0);
// 	SetDlgItemInt(IDC_EDIT_CALIBRATEXY_X, 0);
// 	SetDlgItemInt(IDC_EDIT_CALIBRATEXY_Y, 0);
	UpdateParam();
}

void CLS_DlgCalibrateXY::OnCbnSelchangeComboCalibratexyPicno()
{
	// TODO: Add control notification handler code here
	UpdateParam();
}

void CLS_DlgCalibrateXY::OnCbnSelchangeComboCalibratexyPosnum()
{
	// TODO: Add control notification handler code here
	int iCount = m_CboCalibrateXYPosNum.GetCurSel();
	m_CboCalibrateXYPosIndex.ResetContent();
	for (int i = 0; i < iCount && i < LEN_16; i++)
	{
		m_CboCalibrateXYPosIndex.InsertString(i, IntToCString(i + 1));
	}
	m_CboCalibrateXYPosIndex.SetCurSel(0);
}

void CLS_DlgCalibrateXY::OnCbnSelchangeComboParanum()
{
	// TODO: Add control notification handler code here
	int iCount = m_CboParaNum.GetCurSel();

	for (int i = 0; i < AUTOTEST_MULT_PARA_NUM; i++)
	{
		if (i <= iCount)
		{
			GetDlgItem(IDC_EDIT_TESTPARAM1+i)->EnableWindow(TRUE);
		}
		else
		{
			GetDlgItem(IDC_EDIT_TESTPARAM1+i)->EnableWindow(FALSE);
		}
	}

}

void CLS_DlgCalibrateXY::OnMainNotify(int _iLogonID,int _wParam, void* _iLParam, void* _iUser)
{
	int iMsgType = _wParam & 0xFFFF;
	if (WCM_AUTOTESTMULT == iMsgType)
	{
		AutotestMultResult *ptInfo = (AutotestMultResult*)_iLParam;
		AutotestMultResult tInfo = {0};
		if (NULL != ptInfo)
		{
			int iCpySize = min(ptInfo->iSize, sizeof(AutotestMultResult));
			memcpy(&tInfo, ptInfo, iCpySize);
		}

		SetDlgItemInt(IDC_EDIT_AUTESTMULT_RESULT, tInfo.iTestResult);
		SetDlgItemText(IDC_EDIT_AUTOTESTMULT_INFO, tInfo.cTestInfo);
	}
}
