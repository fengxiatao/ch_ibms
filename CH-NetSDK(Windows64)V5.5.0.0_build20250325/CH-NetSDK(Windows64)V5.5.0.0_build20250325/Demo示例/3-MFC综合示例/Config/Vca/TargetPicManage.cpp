// F:\SDK\SDK_ALL\trunk\Demo\NetClientDemo\Config\Vca\TargetPicManage.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "TargetPicManage.h"


// CLS_DlgCfgTargetPicMng dialog

IMPLEMENT_DYNAMIC(CLS_DlgCfgTargetPicMng, CDialog)

CLS_DlgCfgTargetPicMng::CLS_DlgCfgTargetPicMng(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_DlgCfgTargetPicMng::IDD, pParent)
{

}

CLS_DlgCfgTargetPicMng::~CLS_DlgCfgTargetPicMng()
{
}

void CLS_DlgCfgTargetPicMng::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_FACEWIDTH, m_edtPicWidth);
	DDX_Control(pDX, IDC_EDIT_FACEHEIGHT, m_edtFaceHeight);
	DDX_Control(pDX, IDC_EDIT_BODYHEIGHT, m_edtBodyHeight);
}


BEGIN_MESSAGE_MAP(CLS_DlgCfgTargetPicMng, CLS_BasePage)
	ON_BN_CLICKED(IDC_BTN_TARGETPIC_SAVE, &CLS_DlgCfgTargetPicMng::OnBnClickedBtnTargetpicSave)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_RADIO_CUSTOM, &CLS_DlgCfgTargetPicMng::OnBnClickedRadioCustom)
	ON_BN_CLICKED(IDC_RADIO_HEADPIC, &CLS_DlgCfgTargetPicMng::OnBnClickedRadioHeadpic)
	ON_BN_CLICKED(IDC_RADIO_MIDBODYPIC, &CLS_DlgCfgTargetPicMng::OnBnClickedRadioMidbodypic)
	ON_BN_CLICKED(IDC_RADIO_WHOLEBODYPIC, &CLS_DlgCfgTargetPicMng::OnBnClickedRadioWholebodypic)
END_MESSAGE_MAP()


// CLS_DlgCfgTargetPicMng message handler

BOOL CLS_DlgCfgTargetPicMng::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();

	((CButton *)GetDlgItem(IDC_RADIO_CUSTOM))->SetCheck(TRUE);//selected

	m_edtPicWidth.SetLimitText(4);
	m_edtFaceHeight.SetLimitText(4);
	m_edtBodyHeight.SetLimitText(4);

	SetEditContralDisable(FALSE);

	return TRUE;  // return TRUE unless you set the focus to a control
}

void CLS_DlgCfgTargetPicMng::OnChannelChanged(int _iLogonID,int _iChannelNo,int _iStreamNo)
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
	if (_iStreamNo < 0)
	{
		m_iStreamNo = 0;
	}
	else
	{
		m_iStreamNo = _iStreamNo;
	}

	UpdatePageUI();
}

void CLS_DlgCfgTargetPicMng::OnLanguageChanged(int _iLanguage)
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_DlgCfgTargetPicMng::UpdateUIText()
{
	SetDlgItemTextEx(IDC_STATIC_TARGETPIC_SIZE, IDS_VCA_TARPIC_PICSIZE);
	SetDlgItemTextEx(IDC_RADIO_CUSTOM, IDS_VCA_TARPIC_CUSTOM);
	SetDlgItemTextEx(IDC_RADIO_HEADPIC, IDS_VCA_TARPIC_HEADPHOTO);
	SetDlgItemTextEx(IDC_RADIO_MIDBODYPIC, IDS_VCA_TARPIC_HALFPHOTO);
	SetDlgItemTextEx(IDC_RADIO_WHOLEBODYPIC, IDS_VCA_TARPIC_FULLHOTO);
	SetDlgItemTextEx(IDC_STATIC_FACEWIDTH1, IDS_VCA_TARPIC_WIDTH);
	SetDlgItemTextEx(IDC_STATIC_FACEWIDTH2, IDS_VCA_TARPIC_IMAGEWIDTH);
	SetDlgItemTextEx(IDC_STATIC_FACEHEIGHT1, IDS_VCA_TARPIC_FACEPART_HEIGHT);
	SetDlgItemTextEx(IDC_STATIC_FACEHEIGHT2, IDS_VCA_TARPIC_FACEHEIGHT);
	SetDlgItemTextEx(IDC_STATIC_BODYHEIGHT1, IDS_VCA_TARPIC_BODYPART_HEIGHT);
	SetDlgItemTextEx(IDC_STATIC_BODYHEIGHT2, IDS_VCA_TARPIC_BODY_HEIGHT);
	SetDlgItemTextEx(IDC_BTN_TARGETPIC_SAVE, IDS_VCA_TARPIC_SAVE);
	SetDlgItemTextEx(IDC_BTN_TARGETPIC_RESET, IDS_VCA_TARPIC_RESET);
}

void CLS_DlgCfgTargetPicMng::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		return;
	}

	VcaTargetPicture vca;
	memset(&vca,0,sizeof(VcaTargetPicture));


	int iRet = -1;
	iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_TARGET_PICTURE, m_iChannelNO, &vca, sizeof(VcaTargetPicture));

	if (iRet >= 0)
	{
		SetPicTypeCheck(vca.iTargetPictureType);

		if (PICTURE_TYPE_CUSTOMIZE == vca.iTargetPictureType)
		{
			SetEditContralDisable(TRUE);
			SetDlgItemInt(IDC_EDIT_FACEWIDTH, vca.iPictureWidth);
			SetDlgItemInt(IDC_EDIT_FACEHEIGHT, vca.iFaceHeight);
			SetDlgItemInt(IDC_EDIT_BODYHEIGHT, vca.iBodyHeight);
		}
		else if (PICTURE_TYPE_HEAD_PHOTO == vca.iTargetPictureType)//Mug shot
		{
			SetDlgItemInt(IDC_EDIT_FACEWIDTH, 150);
			SetDlgItemInt(IDC_EDIT_FACEHEIGHT, 150);
			SetDlgItemInt(IDC_EDIT_BODYHEIGHT, 50);
		}
		else if (PICTURE_TYPE_HALFBODY_PHOTO == vca.iTargetPictureType)//Half-length photo
		{
			SetDlgItemInt(IDC_EDIT_FACEWIDTH, 300);
			SetDlgItemInt(IDC_EDIT_FACEHEIGHT, 200);
			SetDlgItemInt(IDC_EDIT_BODYHEIGHT, 350);
		}
		else if (PICTURE_TYPE_FULLBODY_PHOTO == vca.iTargetPictureType)//full-body shot
		{
			SetDlgItemInt(IDC_EDIT_FACEWIDTH, 300);
			SetDlgItemInt(IDC_EDIT_FACEHEIGHT, 200);
			SetDlgItemInt(IDC_EDIT_BODYHEIGHT, 700);
		}

		
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCfgTargetPicMng::NetClient_VCAGetConfig[VCA_CMD_TARGET_PICTURE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_DlgCfgTargetPicMng::OnBnClickedBtnTargetpicSave()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_DlgCfgTargetPicMng::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNo);
		return;
	}

	VcaTargetPicture vca;
	memset(&vca,0,sizeof(VcaTargetPicture));

	if (((CButton *)GetDlgItem(IDC_RADIO_CUSTOM))->GetCheck())
	{
		vca.iTargetPictureType = PICTURE_TYPE_CUSTOMIZE;//customize
	}
	else if (((CButton *)GetDlgItem(IDC_RADIO_HEADPIC))->GetCheck())
	{
		vca.iTargetPictureType = PICTURE_TYPE_HEAD_PHOTO;//Mug shot
	}
	else if (((CButton *)GetDlgItem(IDC_RADIO_MIDBODYPIC))->GetCheck())
	{
		vca.iTargetPictureType = PICTURE_TYPE_HALFBODY_PHOTO;//Half-length photo
	}
	else if (((CButton *)GetDlgItem(IDC_RADIO_WHOLEBODYPIC))->GetCheck())
	{
		vca.iTargetPictureType = PICTURE_TYPE_FULLBODY_PHOTO;//full-body shot
	}
	
	vca.iPictureWidth	   = GetDlgItemInt(IDC_EDIT_FACEWIDTH);
	vca.iFaceHeight        = GetDlgItemInt(IDC_EDIT_FACEHEIGHT);
	vca.iBodyHeight        = GetDlgItemInt(IDC_EDIT_BODYHEIGHT);
	vca.iSize			   = sizeof(vca);

	int iRet = -1;

	iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_TARGET_PICTURE, m_iChannelNO, &vca, sizeof(VcaTargetPicture));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_DlgCfgTargetPicMng::NetClient_VCASetConfig[VCA_CMD_TARGET_PICTURE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_DlgCfgTargetPicMng::NetClient_VCASetConfig[VCA_CMD_TARGET_PICTURE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}


}

void CLS_DlgCfgTargetPicMng::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);

	// TODO: add message handler code here

	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_DlgCfgTargetPicMng::OnBnClickedRadioCustom()
{
	SetEditContralDisable(TRUE);
}

void CLS_DlgCfgTargetPicMng::OnBnClickedRadioHeadpic()
{
	SetEditContralDisable(FALSE);
	SetDlgItemInt(IDC_EDIT_FACEWIDTH, 150);
	SetDlgItemInt(IDC_EDIT_FACEHEIGHT, 150);
	SetDlgItemInt(IDC_EDIT_BODYHEIGHT, 50);
}

void CLS_DlgCfgTargetPicMng::OnBnClickedRadioMidbodypic()
{
	SetEditContralDisable(FALSE);
	SetDlgItemInt(IDC_EDIT_FACEWIDTH, 300);
	SetDlgItemInt(IDC_EDIT_FACEHEIGHT, 200);
	SetDlgItemInt(IDC_EDIT_BODYHEIGHT, 350);
}

void CLS_DlgCfgTargetPicMng::OnBnClickedRadioWholebodypic()
{
	SetEditContralDisable(FALSE);
	SetDlgItemInt(IDC_EDIT_FACEWIDTH, 300);
	SetDlgItemInt(IDC_EDIT_FACEHEIGHT, 200);
	SetDlgItemInt(IDC_EDIT_BODYHEIGHT, 700);
}

void CLS_DlgCfgTargetPicMng::SetEditContralDisable(int _iStatus)
{
	m_edtPicWidth.EnableWindow(_iStatus);
	m_edtFaceHeight.EnableWindow(_iStatus);
	m_edtBodyHeight.EnableWindow(_iStatus);
}

void CLS_DlgCfgTargetPicMng::SetPicTypeCheck(int _iType)
{
	if (PICTURE_TYPE_CUSTOMIZE == _iType)
	{
		((CButton *)GetDlgItem(IDC_RADIO_CUSTOM))->SetCheck(TRUE);//customize
		((CButton *)GetDlgItem(IDC_RADIO_HEADPIC))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_MIDBODYPIC))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_WHOLEBODYPIC))->SetCheck(FALSE);
	}
	else if (PICTURE_TYPE_HEAD_PHOTO == _iType)
	{
		((CButton *)GetDlgItem(IDC_RADIO_HEADPIC))->SetCheck(TRUE);//Mug shot
		((CButton *)GetDlgItem(IDC_RADIO_CUSTOM))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_MIDBODYPIC))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_WHOLEBODYPIC))->SetCheck(FALSE);
	}
	else if (PICTURE_TYPE_HALFBODY_PHOTO == _iType)
	{
		((CButton *)GetDlgItem(IDC_RADIO_MIDBODYPIC))->SetCheck(TRUE);//Half-length photo
		((CButton *)GetDlgItem(IDC_RADIO_CUSTOM))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_HEADPIC))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_WHOLEBODYPIC))->SetCheck(FALSE);
	}
	else if (PICTURE_TYPE_FULLBODY_PHOTO == _iType)
	{
		((CButton *)GetDlgItem(IDC_RADIO_WHOLEBODYPIC))->SetCheck(TRUE);//full-body shot
		((CButton *)GetDlgItem(IDC_RADIO_CUSTOM))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_HEADPIC))->SetCheck(FALSE);
		((CButton *)GetDlgItem(IDC_RADIO_MIDBODYPIC))->SetCheck(FALSE);
	}
}

