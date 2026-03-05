// LS_VCAAdvanceParam.cpp : implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAAdvanceParam.h"

#define SCENE_SUM								16		// total number of scenes
#define FOREGRD_MIN_DIFF_FROM					4		//foreground min difference min
#define FOREGRD_MIN_DIFF_TO						100		//foreground min difference max
#define FOREGRD_MAX_DIFF_FROM					4		//Foreground max difference min
#define FOREGRD_MAX_DIFF_TO						100		//foreground max difference max
#define BACKGRD_UPDATE_SPEED_FROM				1		//The minimum background update speed
#define BACKGRD_UPDATE_SPEED_TO					10		// background update speed maximum
#define TARGET_AFFIRM_FRAME_FROM				1		//The target confirms the minimum number of frames
#define TARGET_AFFIRM_FRAME_TO					30		//The maximum number of target confirmation frames
#define TARGET_AFFIRM_FRAME_DEFAULT				16		// target confirmation frame number default value
#define BLEND_BACKGRD_TIME_FROM					1		// blend into the background time minimum
#define BLEND_BACKGRD_TIME_TO					30		// merge into the background time maximum
#define BLEND_BACKGRD_TIME_DEFAULT				16		//Incorporate the default value of the background time
#define TARGET_MERGE_SENSI_FROM					0		// Target merge sensitivity minimum
#define TARGET_MERGE_SENSI_TO					100		//Maximum target merge sensitivity

#define MIN_SIZE_FROM							0		//The minimum target minimum number of pixels
#define MIN_SIZE_TO								100		//target minimum number of pixels maximum
#define MAX_SIZE_FROM							0		//target maximum number of pixels minimum
#define MAX_SIZE_TO								100		//Maximum maximum number of target pixels

#define MIN_WIDTH_FROM							0		//target minimum width minimum
#define MIN_WIDTH_TO							100		//target min width max
#define MAX_WIDTH_FROM							0		//target max width min
#define MAX_WIDTH_TO							100		//target max width max

#define MIN_HEIGHT_FROM							0		//target minimum height minimum
#define MIN_HEIGHT_TO							100		//target minimum height maximum
#define MAX_HEIGHT_FROM							0		//target maximum height minimum
#define MAX_HEIGHT_TO							100		//target max height max

#define MIN_WHRADIO_FROM						0		//target minimum aspect ratio minimum
#define MIN_WHRADIO_TO							100		//target minimum aspect ratio maximum
#define MAX_WHRADIO_FROM						0		//target maximum aspect ratio minimum
#define MAX_WHRADIO_TO							100		//target maximum aspect ratio maximum value

#define MIN_SPEED_FROM							-1		//The target minimum speed minimum value (-1 is unlimited)
#define MIN_SPEED_TO							100		//target minimum speed maximum
#define MAX_SPEED_FROM							-1		//Target maximum speed minimum value (-1 is unlimited)
#define MAX_SPEED_TO							100		//target max speed max

#define MAX_TARGET_CHECK_SENSITIVITY			100		//Maximum target detection sensitivity
#define DEFAULT_TARGET_CHECK_SENSITIVITY		40		//The default value of target detection sensitivity
#define MIN_TARGET_CHECK_SENSITIVITY			1		//Minimum target detection sensitivity

// CLS_VCAAdvanceParam dialog

IMPLEMENT_DYNAMIC(CLS_VCAAdvanceParam, CDialog)

CLS_VCAAdvanceParam::CLS_VCAAdvanceParam(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCAAdvanceParam::IDD, pParent)
{
	m_iLogonID = -1;
	m_iChannelNo = 0;
	m_iStreamNo = 0;
}

CLS_VCAAdvanceParam::~CLS_VCAAdvanceParam()
{
}

void CLS_VCAAdvanceParam::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_EDIT_TAR_MINSIZE, m_edtTarMinSize);
	DDX_Control(pDX, IDC_EDIT_TAR_MAXSIZE, m_edtTarMaxSize);
	DDX_Control(pDX, IDC_EDIT_TAR_MINWIDTH, m_edtTarMinWidth);
	DDX_Control(pDX, IDC_EDIT_TAR_MAXWIDTH, m_edtTarMaxWidth);
	DDX_Control(pDX, IDC_EDIT_TAR_MINHEIGHT, m_edtTarMinHeight);
	DDX_Control(pDX, IDC_EDIT_TAR_MAXHEIGHT, m_edtTarMaxHeight);
	DDX_Control(pDX, IDC_EDIT_TAR_MINWHRADIO, m_edtTarMinWHRadio);
	DDX_Control(pDX, IDC_EDIT_TAR_MAXWHRADIO, m_edtTarMaxWHRadio);
	DDX_Control(pDX, IDC_EDIT_TAR_MINSPEED, m_edtTarMinSpeed);
	DDX_Control(pDX, IDC_EDIT_TAR_MAXSPEED, m_edtTarMaxSpeed);
	DDX_Control(pDX, IDC_COMBO_Sensitivity, m_cboSensitivity);
	DDX_Control(pDX, IDC_COMBO_VCA_ENABLE, m_cboEnable);
	DDX_Control(pDX, IDC_CBO_VCA_SCENE_NUM, m_cboSceneNum);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_FOREGROUND_MIN_DIFF, m_sldForegrdMinDiff);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_FOREGROUND_MAX_DIFF, m_sldForegrdMaxDiff);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_BACKGROUND_UPDATE_SPEED, m_sldBackgrdUpdateSpeed);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_TARGET_AFFIRM_FRAME_NUM, m_sldTargetAffirmFrame);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_BLEND_BACKGROUND_TIME, m_sldBlendBackgrdTime);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_TARGET_MERGE_SENSITIVITY, m_sldTargetMergeSensi);
	DDX_Control(pDX, IDC_SLD_TAR_MINSIZE, m_sldMinSize);
	DDX_Control(pDX, IDC_SLD_TAR_MAXSIZE, m_sldMaxSize);
	DDX_Control(pDX, IDC_SLD_TAR_MINWIDTH, m_sldMinWidth);
	DDX_Control(pDX, IDC_SLD_TAR_MAXWIDTH, m_sldMaxWidth);
	DDX_Control(pDX, IDC_SLD_TAR_MINHEIGHT, m_sldMinHeight);
	DDX_Control(pDX, IDC_SLD_TAR_MAXHEIGHT, m_sldMaxHeight);
	DDX_Control(pDX, IDC_SLD_TAR_MINWHRADIO, m_sldMinWHRadio);
	DDX_Control(pDX, IDC_SLD_TAR_MAXWHRADIO, m_sldMaxWHRadio);
	DDX_Control(pDX, IDC_SLD_TAR_MINSPEED, m_sldMinSpeed);
	DDX_Control(pDX, IDC_SLD_TAR_MAXSPEED, m_sldMaxSpeed);
	DDX_Control(pDX, IDC_SLD_ADV_PARA_TARGET_CHECK_SENSITIVITY, m_sldTargetCheckSensitivity);
	DDX_Control(pDX, IDC_COMBO_RESUME_TIME, m_cboRecoverTime);
}


BEGIN_MESSAGE_MAP(CLS_VCAAdvanceParam, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_AdvParamSet, &CLS_VCAAdvanceParam::OnBnClickedButtonAdvparamset)
	ON_WM_SHOWWINDOW()
	ON_CBN_SELCHANGE(IDC_COMBO_VCA_ENABLE, &CLS_VCAAdvanceParam::OnCbnSelchangeComboVcaEnable)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_FOREGROUND_MIN_DIFF, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaForegroundMinDiff)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_FOREGROUND_MAX_DIFF, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaForegroundMaxDiff)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_BACKGROUND_UPDATE_SPEED, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaBackgroundUpdateSpeed)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_TARGET_AFFIRM_FRAME_NUM, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaTargetAffirmFrameNum)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_BLEND_BACKGROUND_TIME, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaBlendBackgroundTime)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_TARGET_MERGE_SENSITIVITY, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaTargetMergeSensitivity)
	ON_CBN_SELCHANGE(IDC_CBO_VCA_SCENE_NUM, &CLS_VCAAdvanceParam::OnCbnSelchangeCboVcaSceneNum)
	ON_CBN_SELCHANGE(IDC_COMBO_Sensitivity, &CLS_VCAAdvanceParam::OnCbnSelchangeComboSensitivity)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MINSIZE, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinsize)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MAXSIZE, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxsize)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MINWIDTH, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinwidth)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MAXWIDTH, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxwidth)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MINHEIGHT, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinheight)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MAXHEIGHT, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxheight)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MINWHRADIO, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinwhradio)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MAXWHRADIO, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxwhradio)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MINSPEED, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinspeed)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_TAR_MAXSPEED, &CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxspeed)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_ADV_PARA_TARGET_CHECK_SENSITIVITY, &CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaTargetCheckSensitivity)
	ON_CBN_SELCHANGE(IDC_COMBO_RESUME_TIME, &CLS_VCAAdvanceParam::OnCbnSelchangeComboResumeTime)
END_MESSAGE_MAP()


// CLS_VCAAdvanceParam message handlers



BOOL CLS_VCAAdvanceParam::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();

	UpdateUIText();
	return TRUE; 
}

void CLS_VCAAdvanceParam::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_BasePage::OnShowWindow(bShow, nStatus);
	if (bShow)
	{
		UpdatePageUI();
	}
}

void CLS_VCAAdvanceParam::OnChannelChanged( int _iLogonID,int _iChannelNo,int _iStreamNo )
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

	UI_UpdateSceneResumeTime();
}

void CLS_VCAAdvanceParam::OnLanguageChanged( int _iLanguage )
{
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAAdvanceParam::UpdateUIText()
{
	SetDlgItemTextEx(IDC_STATIC_TargetMinSize, IDS_VCA_ADV_MINSIZE);
	SetDlgItemTextEx(IDC_STATIC_TargetMaxSize, IDS_VCA_ADV_MAXSIZE);
	SetDlgItemTextEx(IDC_STATIC_TargetMinWidth, IDS_VCA_ADV_MINWIDTH);
	SetDlgItemTextEx(IDC_STATIC_TargetMaxWidth, IDS_VCA_ADV_MAXWIDTH);
	SetDlgItemTextEx(IDC_STATIC_TargetMinHeight, IDS_VCA_ADV_MINHEIGHT);
	SetDlgItemTextEx(IDC_STATIC_TargetMaxHeight, IDS_VCA_ADV_MAXHEIGHT);
	SetDlgItemTextEx(IDC_STATIC_TargetMinWHRadio, IDS_VCA_ADV_MINWHRADIO);
	SetDlgItemTextEx(IDC_STATIC_TargetMaxWHRadio, IDS_VCA_ADV_MAXWHRADIO);
	SetDlgItemTextEx(IDC_STATIC_TargetMinSpeed, IDS_VCA_ADV_MINSPEED);
	SetDlgItemTextEx(IDC_STATIC_TargetMaxSpeed, IDS_VCA_ADV_MAXSPEED);
	SetDlgItemTextEx(IDC_BUTTON_AdvParamSet,IDS_SET);
	SetDlgItemTextEx(IDC_STATIC_SENSITIVITY, IDS_VCA_ADV_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_FOREGROUND_MIN_DIFF, IDS_VCA_FOREGROUND_MIN_DIFF);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_FOREGROUND_MAX_DIFF, IDS_VCA_FOREGROUND_MAX_DIFF);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_BACKGROUND_UPDATE_SPEED, IDS_VCA_BACKGROUND_UPDATE_SPEED);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_TARGET_AFFIRM_FRAME_NUM, IDS_VCA_TARGET_AFFIRM_FRAME);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_BLEND_BACKGROUND_TIME, IDS_VCA_BLEND_BACKGROUND_TIME);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_TARGET_MERGE_SENSITIVITY, IDS_VCA_TARGET_MERGE_SENSITIVITY);
	SetDlgItemTextEx(IDC_BUTTON_AbandumSet, IDS_SET);
	SetDlgItemTextEx(IDC_STC_ADV_PARA_SCENE_NUM, IDS_AREA_NUM);
	SetDlgItemTextEx(IDC_STC_MINSPEED_EXPLAIN, IDS_VCA_EXPLAIN_NO_LIMIT);
	SetDlgItemTextEx(IDC_STC_MAXSPEED_EXPLAIN, IDS_VCA_EXPLAIN_NO_LIMIT);
	SetDlgItemText(IDC_STC_ADV_PARA_TARGET_CHECK_SENSITIVITY, GetTextByLan(_T("目标检测灵敏度"), _T("Target Check Sensitivity")));
	SetDlgItemText(IDC_STATIC_RESUME_TIME, GetTextByLan(_T("场景恢复时间（秒）"), _T("Scene recovery time (seconds)")));
	
	CString strSen[] = {GetTextEx(IDS_VCA_SENS_LOW), 
		GetTextEx(IDS_VCA_SENS_MIDDLE),
		GetTextEx(IDS_VCA_SENS_HIGH)
	};
	m_cboSensitivity.ResetContent();
	for (int i=0; i < sizeof(strSen)/sizeof(CString); i++)
	{
		m_cboSensitivity.InsertString(i, strSen[i]);
	}
	m_cboSensitivity.SetCurSel(0);

	const CString strEntype[] = {
		GetTextEx(IDS_CONFIG_VIDEOPARAM_DISABLE),
		GetTextEx(IDS_CONFIG_VIDEOPARAM_ENABLE)
	};
	m_cboEnable.ResetContent();
	for (int i=0; i<sizeof(strEntype)/sizeof(CString); i++)
	{
		m_cboEnable.InsertString(i, strEntype[i]);
	}
	m_cboEnable.SetCurSel(0);

	m_cboSceneNum.ResetContent();
	for (int i = 0; i < MAX_SCENE_NUM; i++)
	{
		m_cboSceneNum.InsertString(i, IntToCString(i + 1));
	}
	m_cboSceneNum.SetCurSel(0);

	m_cboRecoverTime.ResetContent();
	m_cboRecoverTime.InsertString(0, "10");
	m_cboRecoverTime.SetItemData(0, 10);
	m_cboRecoverTime.InsertString(1, "30");
	m_cboRecoverTime.SetItemData(1, 30);
	m_cboRecoverTime.InsertString(2, "60");
	m_cboRecoverTime.SetItemData(2, 60);
	m_cboRecoverTime.InsertString(3, "120");
	m_cboRecoverTime.SetItemData(3, 120);
	m_cboRecoverTime.InsertString(4, "300");
	m_cboRecoverTime.SetItemData(4, 300);
	m_cboRecoverTime.SetCurSel(0);

	m_sldForegrdMinDiff.SetRange(FOREGRD_MIN_DIFF_FROM, FOREGRD_MIN_DIFF_TO - 1);
	m_sldForegrdMinDiff.SetPos(FOREGRD_MIN_DIFF_FROM);
	SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MIN_DIFF_VALUE, m_sldForegrdMinDiff.GetPos());

	m_sldForegrdMaxDiff.SetRange(FOREGRD_MAX_DIFF_FROM + 1, FOREGRD_MAX_DIFF_TO);
	m_sldForegrdMaxDiff.SetPos(FOREGRD_MAX_DIFF_FROM + 1);
	SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MAX_DIFF_VALUE, m_sldForegrdMaxDiff.GetPos());

	m_sldBackgrdUpdateSpeed.SetRange(BACKGRD_UPDATE_SPEED_FROM, BACKGRD_UPDATE_SPEED_TO);
	m_sldBackgrdUpdateSpeed.SetPos(BACKGRD_UPDATE_SPEED_FROM);
	SetDlgItemInt(IDC_STC_ADV_PARA_BACKGROUND_UPDATE_SPEED_VALUE, m_sldBackgrdUpdateSpeed.GetPos());
	
	m_sldTargetAffirmFrame.SetRange(TARGET_AFFIRM_FRAME_FROM, TARGET_AFFIRM_FRAME_TO);
	m_sldTargetAffirmFrame.SetPos(TARGET_AFFIRM_FRAME_DEFAULT);
	SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_AFFIRM_FRAME_NUM_VALUE, m_sldTargetAffirmFrame.GetPos());

	m_sldBlendBackgrdTime.SetRange(BLEND_BACKGRD_TIME_FROM, BLEND_BACKGRD_TIME_TO);
	m_sldBlendBackgrdTime.SetPos(BLEND_BACKGRD_TIME_DEFAULT);
	SetDlgItemInt(IDC_STC_ADV_PARA_BLEND_BACKGROUND_TIME_VALUE, m_sldBlendBackgrdTime.GetPos());

	m_sldTargetCheckSensitivity.SetRange(MIN_TARGET_CHECK_SENSITIVITY, MAX_TARGET_CHECK_SENSITIVITY);
	m_sldTargetCheckSensitivity.SetPos(DEFAULT_TARGET_CHECK_SENSITIVITY);
	SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_CHECK_SENSITIVITY_VALUE, m_sldTargetCheckSensitivity.GetPos());

	m_sldTargetMergeSensi.SetRange(TARGET_MERGE_SENSI_FROM, TARGET_MERGE_SENSI_TO);
	m_sldTargetMergeSensi.SetPos(TARGET_MERGE_SENSI_FROM);
	SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_MERGE_SENSITIVITY_VALUE, m_sldTargetMergeSensi.GetPos());
	
	m_sldMinSize.SetRange(MIN_SIZE_FROM, MIN_SIZE_TO - 1);
	m_sldMinSize.SetPos(MIN_SIZE_FROM);
	SetDlgItemInt(IDC_STC_TAR_MINSIZE_NUM, m_sldMinSize.GetPos());

	m_sldMaxSize.SetRange(MAX_SIZE_FROM + 1, MAX_SIZE_TO);
	m_sldMaxSize.SetPos(MAX_SIZE_FROM + 1);
	SetDlgItemInt(IDC_STC_TAR_MAXSIZE_NUM, m_sldMaxSize.GetPos());

	m_sldMinWidth.SetRange(MIN_WIDTH_FROM, MIN_WIDTH_TO - 1);
	m_sldMinWidth.SetPos(MIN_WIDTH_FROM);
	SetDlgItemInt(IDC_STC_TAR_MINWIDTH_NUM, m_sldMinWidth.GetPos());

	m_sldMaxWidth.SetRange(MAX_WIDTH_FROM + 1, MAX_WIDTH_TO);
	m_sldMaxWidth.SetPos(MAX_WIDTH_FROM + 1);
	SetDlgItemInt(IDC_STC_TAR_MAXWIDTH_NUM, m_sldMaxWidth.GetPos());

	m_sldMinHeight.SetRange(MIN_HEIGHT_FROM, MIN_HEIGHT_TO - 1);
	m_sldMinHeight.SetPos(MIN_HEIGHT_FROM);
	SetDlgItemInt(IDC_STC_TAR_MINHEIGHT_NUM, m_sldMinHeight.GetPos());

	m_sldMaxHeight.SetRange(MAX_HEIGHT_FROM + 1, MAX_HEIGHT_TO);
	m_sldMaxHeight.SetPos(MAX_HEIGHT_FROM + 1);
	SetDlgItemInt(IDC_STC_TAR_MAXHEIGHT_NUM, m_sldMaxHeight.GetPos());

	m_sldMinWHRadio.SetRange(MIN_WHRADIO_FROM, MIN_WHRADIO_TO - 1);
	m_sldMinWHRadio.SetPos(MIN_WHRADIO_FROM);
	SetDlgItemInt(IDC_STC_TAR_MINWHRADIO_NUM, m_sldMinWHRadio.GetPos());

	m_sldMaxWHRadio.SetRange(MAX_WHRADIO_FROM + 1, MAX_WHRADIO_TO);
	m_sldMaxWHRadio.SetPos(MAX_WHRADIO_FROM + 1);
	SetDlgItemInt(IDC_STC_TAR_MAXWHRADIO_NUM, m_sldMaxWHRadio.GetPos());

	m_sldMinSpeed.SetRange(MIN_SPEED_FROM, MIN_SPEED_TO);
	m_sldMinSpeed.SetPos(MIN_SPEED_FROM);
	SetDlgItemInt(IDC_STC_TAR_MINSPEED_NUM, m_sldMinSpeed.GetPos());

	m_sldMaxSpeed.SetRange(MAX_SPEED_FROM, MAX_SPEED_TO);
	m_sldMaxSpeed.SetPos(MAX_SPEED_FROM);
	SetDlgItemInt(IDC_STC_TAR_MAXSPEED_NUM, m_sldMaxSpeed.GetPos());
}

void CLS_VCAAdvanceParam::UpdatePageUI()
{
	if (m_iLogonID == -1 || m_iChannelNo == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAAdvanceParam::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNo);
	}

	VCAAdvanceParam vap;
	memset(&vap,0,sizeof(VCAAdvanceParam));
	vap.iBufSize = sizeof(VCAAdvanceParam);
	vap.iSceneID = m_cboSceneNum.GetCurSel();

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_ADVANCE_PARAM, m_iChannelNo, &vap, sizeof(VCAAdvanceParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAAdvanceParam::NetClient_VCAGetConfig[VCA_CMD_ADVANCE_PARAM] (%d, %d), error (%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
	else
	{
		m_sldMinSize.SetPos(vap.iTargetMinSize);
		SetDlgItemInt(IDC_STC_TAR_MINSIZE_NUM, m_sldMinSize.GetPos());
		m_sldMaxSize.SetPos(vap.iTargetMaxSize);
		SetDlgItemInt(IDC_STC_TAR_MAXSIZE_NUM, m_sldMaxSize.GetPos());
		m_sldMinWidth.SetPos(vap.iTargetMinWidth);
		SetDlgItemInt(IDC_STC_TAR_MINWIDTH_NUM, m_sldMinWidth.GetPos());
		m_sldMaxWidth.SetPos(vap.iTargetMaxWidth);
		SetDlgItemInt(IDC_STC_TAR_MAXWIDTH_NUM, m_sldMaxWidth.GetPos());
		m_sldMinHeight.SetPos(vap.iTargetMinHeight);
		SetDlgItemInt(IDC_STC_TAR_MINHEIGHT_NUM, m_sldMinHeight.GetPos());
		m_sldMaxHeight.SetPos(vap.iTargetMaxHeight);
		SetDlgItemInt(IDC_STC_TAR_MAXHEIGHT_NUM, m_sldMaxHeight.GetPos());
		m_sldMinWHRadio.SetPos(vap.iTargetMinWHRatio);
		SetDlgItemInt(IDC_STC_TAR_MINWHRADIO_NUM, m_sldMinWHRadio.GetPos());
		m_sldMaxWHRadio.SetPos(vap.iTargetMaxWHRatio);
		SetDlgItemInt(IDC_STC_TAR_MAXWHRADIO_NUM, m_sldMaxWHRadio.GetPos());
		m_sldMinSpeed.SetPos(vap.iTargetMinSpeed);
		SetDlgItemInt(IDC_STC_TAR_MINSPEED_NUM, m_sldMinSpeed.GetPos());
		m_sldMaxSpeed.SetPos(vap.iTargetMaxSpeed);
		SetDlgItemInt(IDC_STC_TAR_MAXSPEED_NUM, m_sldMaxSpeed.GetPos());

		m_cboSensitivity.SetCurSel(vap.iSensitivity);
		m_cboEnable.SetCurSel(vap.iTargetEnable);

		m_sldForegrdMinDiff.SetPos(vap.iForegroundMinDiff);
		SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MIN_DIFF_VALUE, m_sldForegrdMinDiff.GetPos());
		m_sldForegrdMaxDiff.SetPos(vap.iForegroundMaxDiff);
		SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MAX_DIFF_VALUE, m_sldForegrdMaxDiff.GetPos());
		m_sldBackgrdUpdateSpeed.SetPos(vap.iBackUpdateSpeed);
		SetDlgItemInt(IDC_STC_ADV_PARA_BACKGROUND_UPDATE_SPEED_VALUE, m_sldBackgrdUpdateSpeed.GetPos());
		m_sldTargetAffirmFrame.SetPos(vap.iRealTargetTime);
		SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_AFFIRM_FRAME_NUM_VALUE, m_sldTargetAffirmFrame.GetPos());
		m_sldBlendBackgrdTime.SetPos(vap.iBlendBackTime);
		SetDlgItemInt(IDC_STC_ADV_PARA_BLEND_BACKGROUND_TIME_VALUE, m_sldBlendBackgrdTime.GetPos());
		m_sldTargetMergeSensi.SetPos(vap.iTarMergeSensitiv);
		SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_MERGE_SENSITIVITY_VALUE, m_sldTargetMergeSensi.GetPos());
		m_sldTargetCheckSensitivity.SetPos(vap.iTarCheckSensitiv);
		SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_CHECK_SENSITIVITY_VALUE, m_sldTargetCheckSensitivity.GetPos());

		AddLog(LOG_TYPE_SUCC,"","CLS_VCAAdvanceParam::NetClient_VCAGetConfig[VCA_CMD_ADVANCE_PARAM] (%d, %d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_VCAAdvanceParam::OnCbnSelchangeComboVcaEnable()
{
	OnBnClickedButtonAdvparamset();
}

void CLS_VCAAdvanceParam::OnBnClickedButtonAdvparamset()
{
	if (m_iLogonID < 0 || m_iChannelNo < 0)
	{
		AddLog(LOG_TYPE_MSG,"","CLS_VCAAdvanceParam::Invalid logon id(%d), channel no(%d)", m_iLogonID, m_iChannelNo);
		return;
	}

	VCAAdvanceParam vap;
	memset(&vap,0,sizeof(VCAAdvanceParam));

	vap.iTargetMinSize = m_sldMinSize.GetPos();
	vap.iTargetMaxSize = m_sldMaxSize.GetPos();
	vap.iTargetMinWidth = m_sldMinWidth.GetPos();
	vap.iTargetMaxWidth = m_sldMaxWidth.GetPos();
	vap.iTargetMinHeight = m_sldMinHeight.GetPos();
	vap.iTargetMaxHeight = m_sldMaxHeight.GetPos();
	vap.iTargetMinWHRatio = m_sldMinWHRadio.GetPos();
	vap.iTargetMaxWHRatio = m_sldMaxWHRadio.GetPos();
	vap.iTargetMinSpeed = m_sldMinSpeed.GetPos();
	vap.iTargetMaxSpeed = m_sldMaxSpeed.GetPos();

	vap.iSensitivity = m_cboSensitivity.GetCurSel();
	vap.iTargetEnable = m_cboEnable.GetCurSel();
	vap.iSceneID = m_cboSceneNum.GetCurSel();

	vap.iForegroundMinDiff = m_sldForegrdMinDiff.GetPos();
	vap.iForegroundMaxDiff = m_sldForegrdMaxDiff.GetPos();
	vap.iBackUpdateSpeed = m_sldBackgrdUpdateSpeed.GetPos();
	vap.iRealTargetTime = m_sldTargetAffirmFrame.GetPos();
	vap.iBlendBackTime = m_sldBlendBackgrdTime.GetPos();
	vap.iTarMergeSensitiv = m_sldTargetMergeSensi.GetPos();
	vap.iTarCheckSensitiv = m_sldTargetCheckSensitivity.GetPos();

	vap.iBufSize = sizeof(vap);

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_ADVANCE_PARAM, m_iChannelNo, &vap, sizeof(VCAAdvanceParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAAdvanceParam::NetClient_VCASetConfig[VCA_CMD_ADVANCE_PARAM] (%d, %d), error (%d)", m_iLogonID, m_iChannelNo, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAAdvanceParam::NetClient_VCASetConfig[VCA_CMD_ADVANCE_PARAM] (%d, %d)", m_iLogonID, m_iChannelNo);
	}
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaForegroundMinDiff(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	int iMinDiff = m_sldForegrdMinDiff.GetPos();
	int iMaxDiff = m_sldForegrdMaxDiff.GetPos();
	SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MIN_DIFF_VALUE, iMinDiff);
	if (iMaxDiff <= iMinDiff)
	{
		m_sldForegrdMaxDiff.SetPos(iMinDiff + 1);
		SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MAX_DIFF_VALUE, iMinDiff + 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaForegroundMaxDiff(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinDiff = m_sldForegrdMinDiff.GetPos();
	int iMaxDiff = m_sldForegrdMaxDiff.GetPos();
	SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MAX_DIFF_VALUE, iMaxDiff);
	if (iMinDiff >= iMaxDiff)
	{
		m_sldForegrdMinDiff.SetPos(iMaxDiff - 1);
		SetDlgItemInt(IDC_STC_ADV_PARA_FOREGROUND_MIN_DIFF_VALUE, iMaxDiff - 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaBackgroundUpdateSpeed(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	SetDlgItemInt(IDC_STC_ADV_PARA_BACKGROUND_UPDATE_SPEED_VALUE, m_sldBackgrdUpdateSpeed.GetPos());

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaTargetAffirmFrameNum(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_AFFIRM_FRAME_NUM_VALUE, m_sldTargetAffirmFrame.GetPos());

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaBlendBackgroundTime(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	SetDlgItemInt(IDC_STC_ADV_PARA_BLEND_BACKGROUND_TIME_VALUE, m_sldBlendBackgrdTime.GetPos());

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaTargetMergeSensitivity(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_MERGE_SENSITIVITY_VALUE, m_sldTargetMergeSensi.GetPos());

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldAdvParaTargetCheckSensitivity(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_ADV_PARA_TARGET_CHECK_SENSITIVITY_VALUE, m_sldTargetCheckSensitivity.GetPos());
	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnCbnSelchangeCboVcaSceneNum()
{
	UpdatePageUI();
}

void CLS_VCAAdvanceParam::OnCbnSelchangeComboSensitivity()
{
	OnBnClickedButtonAdvparamset();
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinsize(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinSize = m_sldMinSize.GetPos();
	int iMaxSize = m_sldMaxSize.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MINSIZE_NUM, iMinSize);
	if (iMaxSize <= iMinSize)
	{
		m_sldMaxSize.SetPos(iMinSize + 1);
		SetDlgItemInt(IDC_STC_TAR_MAXSIZE_NUM, iMinSize + 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxsize(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinSize = m_sldMinSize.GetPos();
	int iMaxSize = m_sldMaxSize.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MAXSIZE_NUM, iMaxSize);
	if (iMinSize >= iMaxSize)
	{
		m_sldMinSize.SetPos(iMaxSize - 1);
		SetDlgItemInt(IDC_STC_TAR_MINSIZE_NUM, iMaxSize - 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinwidth(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinWidth = m_sldMinWidth.GetPos();
	int iMaxWidth = m_sldMaxWidth.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MINWIDTH_NUM, iMinWidth);
	if (iMaxWidth <= iMinWidth)
	{
		m_sldMaxWidth.SetPos(iMinWidth + 1);
		SetDlgItemInt(IDC_STC_TAR_MAXWIDTH_NUM, iMinWidth + 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxwidth(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinWidth = m_sldMinWidth.GetPos();
	int iMaxWidth = m_sldMaxWidth.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MAXWIDTH_NUM, iMaxWidth);
	if (iMinWidth >= iMaxWidth)
	{
		m_sldMinWidth.SetPos(iMaxWidth - 1);
		SetDlgItemInt(IDC_STC_TAR_MINWIDTH_NUM, iMaxWidth - 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinheight(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	int iMinHeight = m_sldMinHeight.GetPos();
	int iMaxHeight = m_sldMaxHeight.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MINHEIGHT_NUM, iMinHeight);
	if (iMaxHeight <= iMinHeight)
	{
		m_sldMaxHeight.SetPos(iMinHeight + 1);
		SetDlgItemInt(IDC_STC_TAR_MAXHEIGHT_NUM, iMinHeight + 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxheight(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinHeight = m_sldMinHeight.GetPos();
	int iMaxHeight = m_sldMaxHeight.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MAXHEIGHT_NUM, iMaxHeight);
	if (iMinHeight >= iMaxHeight)
	{
		m_sldMinHeight.SetPos(iMaxHeight - 1);
		SetDlgItemInt(IDC_STC_TAR_MINHEIGHT_NUM, iMaxHeight - 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinwhradio(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	int iMinWHRadio = m_sldMinWHRadio.GetPos();
	int iMaxWHRadio = m_sldMaxWHRadio.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MINWHRADIO_NUM, iMinWHRadio);
	if (iMaxWHRadio <= iMinWHRadio)
	{
		m_sldMaxWHRadio.SetPos(iMinWHRadio + 1);
		SetDlgItemInt(IDC_STC_TAR_MAXWHRADIO_NUM, iMinWHRadio + 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxwhradio(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinWHRadio = m_sldMinWHRadio.GetPos();
	int iMaxWHRadio = m_sldMaxWHRadio.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MAXWHRADIO_NUM, iMaxWHRadio);
	if (iMinWHRadio >= iMaxWHRadio)
	{
		m_sldMinWHRadio.SetPos(iMaxWHRadio - 1);
		SetDlgItemInt(IDC_STC_TAR_MINWHRADIO_NUM, iMaxWHRadio - 1);
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMinspeed(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	
	int iMinSpeed = m_sldMinSpeed.GetPos();
	int iMaxSpeed = m_sldMaxSpeed.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MINSPEED_NUM, iMinSpeed);

	if (MAX_SPEED_FROM == m_sldMaxSpeed.GetPos())
	{
		m_sldMinSpeed.SetRangeMax(MAX_SPEED_TO);
		return;
	}
	else
	{
		m_sldMinSpeed.SetRangeMax(MAX_SPEED_TO - 1);
	}

	if ((iMaxSpeed <= iMinSpeed) && (MIN_SPEED_FROM != iMinSpeed))
	{
		m_sldMaxSpeed.SetPos(iMinSpeed + 1);
		SetDlgItemInt(IDC_STC_TAR_MAXSPEED_NUM, iMinSpeed + 1);
	}
	if (MIN_SPEED_FROM == iMinSpeed)
	{
		m_sldMaxSpeed.SetPos(MAX_SPEED_FROM);
		SetDlgItemInt(IDC_STC_TAR_MAXSPEED_NUM, m_sldMaxSpeed.GetPos());
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnNMCustomdrawSldTarMaxspeed(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinSpeed = m_sldMinSpeed.GetPos();
	int iMaxSpeed = m_sldMaxSpeed.GetPos();
	SetDlgItemInt(IDC_STC_TAR_MAXSPEED_NUM, iMaxSpeed);
	if ((iMinSpeed >= iMaxSpeed) && (MAX_SPEED_FROM != iMaxSpeed) && ((MAX_SPEED_FROM + 1) != iMaxSpeed))
	{
		m_sldMinSpeed.SetPos(iMaxSpeed - 1);
		SetDlgItemInt(IDC_STC_TAR_MINSPEED_NUM, iMaxSpeed - 1);
	}
	if ((MAX_SPEED_FROM + 1) == iMaxSpeed)
	{
		m_sldMaxSpeed.SetPos(iMaxSpeed);
		SetDlgItemInt(IDC_STC_TAR_MINSPEED_NUM, iMaxSpeed);
	}
	if (((MAX_SPEED_FROM + 1) < iMaxSpeed) && (MIN_SPEED_FROM == iMinSpeed))
	{
		m_sldMinSpeed.SetPos(MIN_SPEED_FROM + 1);
		SetDlgItemInt(IDC_STC_TAR_MINSPEED_NUM, m_sldMinSpeed.GetPos());
	}

	*pResult = 0;
}

void CLS_VCAAdvanceParam::OnCbnSelchangeComboResumeTime()
{
	VCASceneRecovery tVCAScene = {0};
	tVCAScene.iBufSize = sizeof(VCASceneRecovery);
	tVCAScene.iChanNo = m_iChannelNo;
	tVCAScene.iSceneId = 0;//The current usage scene is all scene values
	int iIndex = m_cboRecoverTime.GetCurSel();
	tVCAScene.iRecoveryTime = m_cboRecoverTime.GetItemData(iIndex);
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_SCENEREV, m_iChannelNo, &tVCAScene, sizeof(VCASceneRecovery));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_VCASetConfig::VCA_CMD_SCENEREV fail!");
	}
}

void CLS_VCAAdvanceParam::UI_UpdateSceneResumeTime()
{
	VCASceneRecovery tVCAScene = {0};
	tVCAScene.iBufSize = sizeof(VCASceneRecovery);
	tVCAScene.iChanNo = m_iChannelNo;
	tVCAScene.iSceneId = 0;//Currently only use scene 1, as all scenes use
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_SCENEREV, m_iChannelNo, &tVCAScene, sizeof(VCAAdvanceParam));
	if (RET_SUCCESS != iRet)
	{
		AddLog(LOG_TYPE_FAIL,"","NetClient_VCAGetConfig::VCA_CMD_SCENEREV fail!");
	}
	else
	{
		for (int i = 0; i < m_cboRecoverTime.GetCount(); ++i)
		{
			if (tVCAScene.iRecoveryTime == m_cboRecoverTime.GetItemData(i))
			{
				m_cboRecoverTime.SetCurSel(i);
				break;
			}
		}
	}
}
