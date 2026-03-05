// TargetDetect.cpp : 实现文件
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "TargetDetect.h"


// TargetDetect 对话框

IMPLEMENT_DYNAMIC(CLS_TargetDetect, CDialog)

CLS_TargetDetect::CLS_TargetDetect(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_TargetDetect::IDD, pParent)
{

}

CLS_TargetDetect::~CLS_TargetDetect()
{
}

void CLS_TargetDetect::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHECK_TARGET_SWITCH, m_chkSwitch);
	DDX_Control(pDX, IDC_CHECK_DATA_TRANFER, m_chkDataTransferSwitch);
}


BEGIN_MESSAGE_MAP(CLS_TargetDetect, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_CHECK_DATA_TRANFER, &CLS_TargetDetect::OnBnClickedCheckDataTranfer2)
END_MESSAGE_MAP()

BOOL CLS_TargetDetect::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	UpdateUIText();


	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_TargetDetect::UpdateUIText()
{
	SetDlgItemText(IDC_CHECK_DATA_TRANFER, GetTextByLan("数据上报开关", "Data Transfer Switch"));
	SetDlgItemText(IDC_CHECK_TARGET_SWITCH, GetTextByLan("更新", "Updata"));
	SetDlgItemText(IDC_STATIC_TARGET_CHANNAL, GetTextByLan("通道号", "ChannelNo"));
	SetDlgItemText(IDC_STATIC_TARGET_GROUP, GetTextByLan("组号", "Group ID"));
	SetDlgItemText(IDC_STATIC_GROUP_DATA, GetTextByLan("单组总数据N个数", "Single Data number"));
	SetDlgItemText(IDC_STATIC_TARGET_DATA_NO, GetTextByLan("数据编号", "Data Number"));
	SetDlgItemText(IDC_STATIC_TGRID, GetTextByLan("目标id", "Target ID"));
	SetDlgItemText(IDC_STATIC_TGTTYPE, GetTextByLan("目标类型", "Target Type"));
	SetDlgItemText(IDC_STATIC_TGTSCORE, GetTextByLan("目标分值", "Target Score"));
	SetDlgItemText(IDC_STATIC_TARGET_X1, GetTextByLan("左上角X1", "Up Left X1"));
	SetDlgItemText(IDC_STATIC_TARGET_Y1, GetTextByLan("左上角Y1", "Up Left Y1"));
	SetDlgItemText(IDC_STATIC_TARGET_X2, GetTextByLan("右下角X2", "Low Riget X2"));
	SetDlgItemText(IDC_STATIC_TARGET_Y2, GetTextByLan("右下角Y1", "Low Riget Y2"));
	SetDlgItemText(IDC_STATIC_TARGET_TIME, GetTextByLan("时间", "Time"));
	
}
void CLS_TargetDetect::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	// TODO: Add your message handler code here
	if (bShow)
	{
		UI_UpdatePage();
	}

}
void CLS_TargetDetect::UI_UpdatePage()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}
}

void CLS_TargetDetect::OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData)
{
	if (_iLogonID < 0 || _iLogonID != m_iLogonID)
	{
		return;
	}

	//if (_iChannelNo == m_iChannelNO)//Only refresh the channel whose parameter has changed
	{
		switch(_iParaType)
		{
		case  PARA_TARGET_REALTIME_RESULT:
			{
				CString strMsg;
				TargetQueryResult* ptInfo = (TargetQueryResult*)_pPara;
				if (NULL == ptInfo)
				{
					return;
				}
				strMsg.Format(_T("[CLS_TargetDetect::OnParamChangeNotify]通道号:%d；组号:%d；单组总数据N个数:%d；数据编号1:%d；目标id:%d；目标类型:%d；目标分值:%d；X1:%d-Y1:%d-X2:%d-Y2:%d;\n"),
					ptInfo->iChannelNo,ptInfo->iGroupNO,ptInfo->iGroupDataCnt,ptInfo->iDataNo,ptInfo->iTgtId,ptInfo->iTgtType,ptInfo->iTgtScore,ptInfo->iPosX1,ptInfo->iPosY1,ptInfo->iPosX2,ptInfo->iPosY2,ptInfo->llCurrentTime,ptInfo->iCurrentMsTime);
				OutputDebugString(strMsg);
				if(m_chkSwitch.GetCheck())
				{
					SetDlgItemInt(IDC_EDIT_TARGET_CHANNAL, ptInfo->iChannelNo);
					SetDlgItemInt(IDC_EDIT_TARGET_GROUP, ptInfo->iGroupNO);
					SetDlgItemInt(IDC_EDIT_GROUP_DATA, ptInfo->iGroupDataCnt);
					SetDlgItemInt(IDC_EDIT_TARGET_DATA_NO, ptInfo->iDataNo);
					SetDlgItemInt(IDC_EDIT_TGRID, ptInfo->iTgtId);
					SetDlgItemInt(IDC_EDIT_TGTTYPE, ptInfo->iTgtType);
					SetDlgItemInt(IDC_EDIT_TGTSCORE, ptInfo->iTgtScore);
					SetDlgItemInt(IDC_EDIT_TARGET_X1, ptInfo->iPosX1);
					SetDlgItemInt(IDC_EDIT_TARGET_Y1, ptInfo->iPosY1);
					SetDlgItemInt(IDC_EDIT_TARGET_X2, ptInfo->iPosX2);
					SetDlgItemInt(IDC_EDIT_TARGET_Y2, ptInfo->iPosY2);

					CString cstTargetTime;
					cstTargetTime.AppendFormat("(%lld, %d)", ptInfo->llCurrentTime, ptInfo->iCurrentMsTime);
					SetDlgItemText(IDC_EDIT_TARGET_TIME, cstTargetTime);
				}
			}
			break;
		default:
			break;
		}
	}
}
void CLS_TargetDetect::OnBnClickedCheckDataTranfer2()
{
	TargetParaSwitch tInfo = {0};
	TargetParaSwitchResult tResult;
	if(m_chkDataTransferSwitch.GetCheck())
	{
		tInfo.iEnable = 1;
	}else
	{
		tInfo.iEnable = 0;
	}
	int iRet = NetClient_CmdConfig(m_iLogonID,CMD_TARGET_START_STOP_TRANSFER,m_iChannelNO, &tInfo, sizeof(tInfo),&tResult, sizeof(tResult));
	if(0 == iRet)
	{
		AddLog(LOG_TYPE_SUCC,"","NetClient_CmdConfig(%d,%d,%d,%d)"
			,m_iLogonID,CMD_TARGET_START_STOP_TRANSFER,m_iChannelNO,tInfo.iEnable);
	}
	else
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_CmdConfig(%d,%d,%d,%d)"
			,m_iLogonID,CMD_TARGET_START_STOP_TRANSFER,m_iChannelNO,tInfo.iEnable);
	}
}
