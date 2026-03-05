// VCAEVENT_AudioDiagnoseNew.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include "VCAEVENT_FaceRecNew.h"

#define DEFAULT_ZORO			0			//default value is 0
#define SENSITIVITY_MIN			0			//minimum sensitivity
#define SENSITIVITY_MAX			5			//Maximum sensitivity
#define AUDIO_DIAGNOSE_RULEID	9			//Audio diagnostic ruleid with 9
#define LEN_3					3			//length is 3
#define POINT_NUM_MIN			2			//minimum points
#define POINT_NUM_MAX			15			// maximum number of points
#define SIZE_MIN_VALUE			0			//minimum size
#define SIZE_MAX_VALUE			10000		//Maximum size

#define LEVEL_EXPOLIGHT_MIN         0
#define LEVEL_EXPOLIGHT_MAX         255
// CLS_VCAEVENT_FaceRecNew dialog

IMPLEMENT_DYNAMIC(CLS_VCAEVENT_FaceRecNew, CDialog)

CLS_VCAEVENT_FaceRecNew::CLS_VCAEVENT_FaceRecNew(CWnd* pParent /*=NULL*/)
: CLS_VCAEventBasePage(CLS_VCAEVENT_FaceRecNew::IDD, pParent)
{

}

CLS_VCAEVENT_FaceRecNew::~CLS_VCAEVENT_FaceRecNew()
{
}

void CLS_VCAEVENT_FaceRecNew::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_CHK_FACEREC_NEW_ENABLE, m_chkEnable);
	DDX_Control(pDX, IDC_EDT_FACEREC_NEW_MAX_SIZE, m_edtMaxSize);
	DDX_Control(pDX, IDC_EDT_FACEREC_NEW_MIN_SIZE, m_edtMinSize);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_ALGO_LEVEL, m_cboAlgoLevel);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_SENSITIVITY, m_cboSensitivity);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_PIC_SCAL, m_cboPicScal);
	DDX_Control(pDX, IDC_EDT_FACEREC_NEW_SNAP_SPACE, m_edtSnapSpace);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_SNAP_TIMES, m_cboSnapTimes);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_POLYGON_POINT_NUM, m_cboPolygonPointNum);
	DDX_Control(pDX, IDC_EDT_FACEREC_NEW_POLYGON_AREA, m_edtPolygonArea);
	DDX_Control(pDX, IDC_SLD_FACEREC_NEW_MAX_SIZE, m_sldMaxSize);
	DDX_Control(pDX, IDC_SLD_FACEREC_NEW_MIN_SIZE, m_sldMinSize);
	DDX_Control(pDX, IDC_CHK_FACEREC_NEW_TARGET_BOX, m_chkTargetBox);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_DEVTYPE, m_cboDevType);
	DDX_Control(pDX, IDC_COMBO_SNAPMODE, m_cboSnapMode);
	DDX_Control(pDX, IDC_EDIT_BKPICQ, m_iQpvalueBig);
	DDX_Control(pDX, IDC_EDIT_FACESMALLQ, m_iQpvalueSmall);
	DDX_Control(pDX, IDC_SLIDER1, m_iExposureLight);
	DDX_Control(pDX, IDC_STATIC_DATAEXPO, m_dataExpoLight);
	DDX_Control(pDX, IDC_CHK_FACEREC_NEW_IDENT_ENABLE, m_chkIdentEnable);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_PUSH_MODE, m_cboPushMode);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_PUSH_LEVEL, m_cboPushLevel);
	DDX_Control(pDX, IDC_EDIT_FACEREC_NEW_PUSH_TIME, m_edtPushTime);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_SNAP_MODE, m_cboSnapType);
	DDX_Control(pDX, IDC_SLD_FACEREC_NEW_SNAP_LEVEL, m_sldSnapLevel);
	DDX_Control(pDX, IDC_CHK_FACEREC_NEW_RULE_BOX, m_chkRuleBox);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_PICSEND_TYPE, m_cboPicSendType);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_PICOSD, m_cboPicOsd);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_UPLOAD_ENABLE, m_cboPicUploadEnable);
	DDX_Control(pDX, IDC_SLD_FACEREC_NEW_UPLOAD_PICQUALITY, m_sldUploadPicQuality);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_FACE_FRAME, m_cboFaceFrameEnbale);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_TIME_SPACE, m_cboTimeInterval);
	DDX_Control(pDX, IDC_CBO_FACEREC_NEW_FIRST_DELYTIME, m_cboTimeDelay);

    DDX_Control(pDX, IDC_CHK_FACEATTR_ALARM0, m_chkFaceAttrAlarmEnable[0]);
    DDX_Control(pDX, IDC_CHK_FACEATTR_ALARM1, m_chkFaceAttrAlarmEnable[1]);
    DDX_Control(pDX, IDC_CHK_FACEATTR_ALARM2, m_chkFaceAttrAlarmEnable[2]);
    DDX_Control(pDX, IDC_CHK_FACEATTR_ALARM3, m_chkFaceAttrAlarmEnable[3]);
    DDX_Control(pDX, IDC_CBO_DELAYPUSHSPAN, m_cboDelayPushSpan);

}


BEGIN_MESSAGE_MAP(CLS_VCAEVENT_FaceRecNew, CDialog)
	ON_WM_SHOWWINDOW()
	ON_BN_CLICKED(IDC_BTN_FACEREC_NEW_SET, &CLS_VCAEVENT_FaceRecNew::OnBnClickedBtnFacerecNewSet)
	ON_BN_CLICKED(IDC_BTN_FACEREC_NEW_POLYGON_AREA_DRAW, &CLS_VCAEVENT_FaceRecNew::OnBnClickedBtnFacerecNewPolygonAreaDraw)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_FACEREC_NEW_MAX_SIZE, &CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewMaxSize)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_FACEREC_NEW_MIN_SIZE, &CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewMinSize)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLIDER1, &CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSlider1)
	ON_CBN_SELCHANGE(IDC_CBO_FACEREC_NEW_PUSH_MODE, &CLS_VCAEVENT_FaceRecNew::OnCbnSelchangeCboFacerecNewPushMode)
	ON_CBN_SELCHANGE(IDC_CBO_FACEREC_NEW_SNAP_MODE, &CLS_VCAEVENT_FaceRecNew::OnCbnSelchangeCboFacerecNewSnapMode)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_FACEREC_NEW_SNAP_LEVEL, &CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewSnapLevel)
	ON_BN_CLICKED(IDC_BTN_FACEREC_NEW_PICUPLOAD_SET, &CLS_VCAEVENT_FaceRecNew::OnBnClickedBtnFacerecNewPicuploadSet)
	ON_CBN_SELCHANGE(IDC_CBO_FACEREC_NEW_PICSEND_TYPE, &CLS_VCAEVENT_FaceRecNew::OnCbnSelchangeCboFacerecNewPicsendType)
	ON_NOTIFY(NM_CUSTOMDRAW, IDC_SLD_FACEREC_NEW_UPLOAD_PICQUALITY, &CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewUploadPicquality)
END_MESSAGE_MAP()


// CLS_VCAEVENT_FaceRecNew message handler


BOOL CLS_VCAEVENT_FaceRecNew::OnInitDialog()
{
	CLS_VCAEventBasePage::OnInitDialog();

	UpdateUIText();

	return TRUE;  // return TRUE unless you set the focus to a control
	// Exception: OCX property page should return FALSE
}


void CLS_VCAEVENT_FaceRecNew::OnShowWindow(BOOL bShow, UINT nStatus)
{
	CLS_VCAEventBasePage::OnShowWindow(bShow, nStatus);

	if (bShow)
	{
		UpdatePageUI();
	}
}


void CLS_VCAEVENT_FaceRecNew::OnLanguageChanged()
{	
	UpdateUIText();
	UpdatePageUI();
}

void CLS_VCAEVENT_FaceRecNew::UpdateUIText()
{
	SetDlgItemTextEx(IDC_CHK_FACEREC_NEW_ENABLE, IDS_VCA_FACE_SNAPSHOT_ENABLE);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_MAX_SIZE, IDS_VCAEVENT_MAX_FACE_SIZE);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_MIN_SIZE, IDS_VCAEVENT_MIN_FACE_SIZE);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_ALGO_LEVEL, IDS_VCAEVENT_ALGO_RUN_LEVEL);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_SENSITIVITY, IDS_CONFIG_ITS_ILLEGALPARK_SENSITIVITY);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_PIC_SCAL, IDS_VCAEVENT_PICTURE_SCALING);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_SNAP_SPACE, IDS_CONFIG_FTP_INTERVAL);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_SNAP_TIMES, IDS_VCAEVENT_SNAP_TIMES);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_POLYGON_POINT_NUM, IDS_VCAEVENT_POLYGON_POINT_NUM);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_POLYGON_AREA, IDS_VCAEVENT_POLYGON);
	SetDlgItemTextEx(IDC_BTN_FACEREC_NEW_POLYGON_AREA_DRAW, IDS_VCA_DRAWWING);
	SetDlgItemTextEx(IDC_BTN_FACEREC_NEW_SET, IDS_SET);
	SetDlgItemTextEx(IDC_CHK_FACEREC_NEW_TARGET_BOX, IDS_VCAEVENT_SHOW_TARGET_BOX);
	SetDlgItemTextEx(IDC_CHK_FACEREC_NEW_RULE_BOX, IDS_VCA_STRUCT_SHOW_RULE);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_PUSH_MODE, IDS_VCA_STRUCT_PUSHMODE);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_PUSH_LEVEL, IDS_VCA_STRUCT_PUSH_LEVEL);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_PUSH_TIME, IDS_VCA_STRUCT_STC_TIMING);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_SNAP_MODE, IDS_VCA_STRUCT_CAP_MODE);
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_SNAP_LEVEL, IDS_VCA_STRUCT_CAPLEVEL);
	SetDlgItemText(IDC_STC_FACEREC_NEW_PICSEND_MODE, GetTextByLan("图片上传模式", "Image upload mode"));
	SetDlgItemText(IDC_STC_FACEREC_NEW_PICOSD, GetTextByLan("背景图叠字符", "Background overlay character"));
	SetDlgItemText(IDC_STC_FACEREC_NEW_UPLOAD_ENABLE, GetTextByLan("上传使能", "Upload enable"));
	SetDlgItemText(IDC_STC_FACEREC_NEW_UPLOAD_PICQUALITY, GetTextByLan("抓拍图片质量", "Snap picture quality"));
	SetDlgItemText(IDC_STC_FACEREC_FACE_FRAME, GetTextByLan("背景图叠人脸框", "FaceFrameEnable"));
	SetDlgItemText(IDC_STC_FACEREC_NEW_FIRST_DELYTIME, GetTextByLan("首张延时时间", "First delay time"));
	SetDlgItemTextEx(IDC_STC_FACEREC_NEW_TIME_SPACE, IDS_ITS_TIME);


    SetDlgItemText(IDC_CHK_FACEATTR_ALARM0, GetTextByLan("口罩", "Mask"));
    SetDlgItemText(IDC_CHK_FACEATTR_ALARM1, GetTextByLan("无口罩", "No Mask"));
    SetDlgItemText(IDC_CHK_FACEATTR_ALARM2, GetTextByLan("安全帽", "Helmet"));
    SetDlgItemText(IDC_CHK_FACEATTR_ALARM3, GetTextByLan("无安全帽", "No Helmet"));

    m_cboDelayPushSpan.ResetContent();
    const CString cstSecond[] = {"2","5","10","20","30"};
    for (int i = 0; i < sizeof(cstSecond)/sizeof(CString); i++)
    {
        CString strVal = cstSecond[i] + "s";
        m_cboDelayPushSpan.SetItemData(m_cboDelayPushSpan.AddString(strVal), _ttoi(cstSecond[i]));
    }
    m_cboDelayPushSpan.SetCurSel(0);

	m_cboAlgoLevel.ResetContent();
	const CString cstLevel[] = {"0","1","2","3","4","5"};
	for (int i = 0; i < sizeof(cstLevel)/sizeof(CString); i++)
	{
		m_cboAlgoLevel.InsertString(i, cstLevel[i]);
	}
	m_cboAlgoLevel.SetCurSel(0);

	m_cboSensitivity.ResetContent();
	const CString cstSens[] = {"0","1","2","3","4","5"};
	for (int i = 0; i < sizeof(cstSens)/sizeof(CString); i++)
	{
		m_cboSensitivity.InsertString(i, cstSens[i]);
	}
	m_cboSensitivity.SetCurSel(0);

	m_cboSnapTimes.ResetContent();
	const CString cstSnapTimes[] = {"1","2","3","4","5","6","7","8"};
	for (int i = 0; i < sizeof(cstSnapTimes)/sizeof(CString); i++)
	{
		m_cboSnapTimes.InsertString(i, cstSnapTimes[i]);
	}
	m_cboSnapTimes.SetCurSel(0);

	m_cboPicScal.ResetContent();
	const CString cstPicScal[] = {"1","2","3","4","5","6","7","8","9","10"};
	for (int i = 0; i < sizeof(cstPicScal)/sizeof(CString); i++)
	{
		m_cboPicScal.InsertString(i, cstPicScal[i]);
	}
	m_cboPicScal.SetCurSel(0);

	CString cstTemp = "";
	m_cboPolygonPointNum.ResetContent();
	for (int i = (POINT_NUM_MIN - 2); i <= (POINT_NUM_MAX - 2); i++)
	{	
		cstTemp.Format("%d",i + 2);
		m_cboPolygonPointNum.InsertString(i, cstTemp);
	}
	m_cboPolygonPointNum.SetCurSel(0);

	m_edtMaxSize.SetLimitText(LEN_3);
	m_edtMinSize.SetLimitText(LEN_3);
	m_edtSnapSpace.SetLimitText(LEN_32 - 1);

	m_sldMaxSize.SetRange(SIZE_MIN_VALUE + 1, SIZE_MAX_VALUE);
	m_sldMaxSize.SetPos(DEFAULT_ZORO + 1);
	SetDlgItemInt(IDC_STC_FACEREC_NEW_MAX_SIZE_NUM, m_sldMaxSize.GetPos());

	m_sldMinSize.SetRange(SIZE_MIN_VALUE, SIZE_MAX_VALUE - 1);
	m_sldMinSize.SetPos(DEFAULT_ZORO);
	SetDlgItemInt(IDC_STC_FACEREC_NEW_MIN_SIZE_NUM, m_sldMinSize.GetPos());	

	m_iExposureLight.SetRange(LEVEL_EXPOLIGHT_MIN+1, LEVEL_EXPOLIGHT_MAX);
    m_sldMaxSize.SetPos(DEFAULT_ZORO + 1);
    SetDlgItemInt(IDC_STATIC_DATAEXPO, m_sldMinSize.GetPos());	

	const CString csDevType[] = {"IPC","NVR"};
	for (int i = 0;i < sizeof(csDevType)/sizeof(CString);i++)
	{
		m_cboDevType.InsertString(i, csDevType[i]);
	}
	const CString csSnapMode[] = {"face", "vehicle", "hybrid"};
	for (int i = 0;i < sizeof(csSnapMode)/sizeof(CString); i++)
	{
		m_cboSnapMode.InsertString(i, csSnapMode[i]);
	}

	CString strPushMode[] = {GetTextEx(IDS_VCA_STRUCT_FASTEST),GetTextEx(IDS_VCA_STRUCT_OPTIMAL), GetTextEx(IDS_VCA_STRUCT_CUSTOMIZE), GetTextEx(IDS_VCA_STRUCT_TIMING), GetTextEx(IDS_VCA_STRUCT_ENTRANCE_GUARD), GetTextEx(IDS_VCA_STATEMENT_TYPE_DEFAULT), GetTextEx(IDS_ALARM_CHANNEL)};
	m_cboPushMode.ResetContent();
	for(int i=0; i<sizeof(strPushMode)/sizeof(CString); i++)
	{
		m_cboPushMode.InsertString(i, strPushMode[i]);
	}

	CString strPushLevel[] = {GetTextEx(IDS_VCA_STRUCT_FAST),GetTextEx(IDS_VCA_STRUCT_MEDIUM), GetTextEx(IDS_VCA_STRUCT_SLOW)};
	m_cboPushLevel.ResetContent();
	for(int i=0; i<sizeof(strPushLevel)/sizeof(CString); i++)
	{
		m_cboPushLevel.InsertString(i, strPushLevel[i]);
	}

	CString strSnapMode[] = {GetTextEx(IDS_VCA_STRUCT_CAPALL),GetTextEx(IDS_VCA_STRUCT_QUALITY), GetTextEx(IDS_VCA_STRUCT_CUSTOMIZE)};
	m_cboSnapType.ResetContent();
	for(int i=0; i<sizeof(strSnapMode)/sizeof(CString); i++)
	{
		m_cboSnapType.InsertString(i, strSnapMode[i]);
	}

	m_sldSnapLevel.SetRange(0, 100);
	m_sldSnapLevel.SetPos(0);
	SetDlgItemInt(IDC_STC_FACEREC_NEW_SNAP_NUM, m_sldSnapLevel.GetPos());	

	m_cboPicSendType.ResetContent();
	m_cboPicSendType.InsertString(0, GetTextByLan("背景大图", "Background"));
	m_cboPicSendType.InsertString(1, GetTextByLan("特写小图", "Background"));
	m_cboPicSendType.SetCurSel(0);

	m_cboPicOsd.ResetContent();
	m_cboPicOsd.InsertString(0, GetTextByLan("不叠加", "Not add"));
	m_cboPicOsd.InsertString(1, GetTextByLan("叠加", "Add"));
	m_cboPicOsd.SetCurSel(0);

	m_cboPicUploadEnable.ResetContent();
	m_cboPicUploadEnable.InsertString(0, GetTextByLan("不上传", "Not upload"));
	m_cboPicUploadEnable.InsertString(1, GetTextByLan("上传", "Upload"));
	m_cboPicUploadEnable.SetCurSel(0);

	m_sldUploadPicQuality.SetRange(1, 100);
	m_sldUploadPicQuality.SetPos(1);
	SetDlgItemInt(IDC_STC_FACEREC_NEW_UPLOAD_PICQUALITY_NUM, m_sldUploadPicQuality.GetPos());

	m_cboFaceFrameEnbale.ResetContent();
	m_cboFaceFrameEnbale.InsertString(0, GetTextByLan("不叠加", "Not add"));
	m_cboFaceFrameEnbale.InsertString(1, GetTextByLan("叠加", "Add"));
	m_cboFaceFrameEnbale.SetCurSel(0);

	m_cboTimeInterval.ResetContent();
	m_cboTimeInterval.SetItemData(m_cboTimeInterval.AddString("100ms"), 100);
	m_cboTimeInterval.SetItemData(m_cboTimeInterval.AddString("200ms"), 200);
	m_cboTimeInterval.SetItemData(m_cboTimeInterval.AddString("300ms"), 300);
	m_cboTimeInterval.SetItemData(m_cboTimeInterval.AddString("500ms"), 500);
	m_cboTimeInterval.SetItemData(m_cboTimeInterval.AddString("1000ms"), 1000);
	m_cboTimeInterval.SetItemData(m_cboTimeInterval.AddString("2000ms"), 2000);
	m_cboTimeInterval.SetCurSel(0);

	m_cboTimeDelay.ResetContent();
	m_cboTimeDelay.SetItemData(m_cboTimeDelay.AddString("500ms"), 500);
	m_cboTimeDelay.SetItemData(m_cboTimeDelay.AddString("1000ms"), 1000);
	m_cboTimeDelay.SetItemData(m_cboTimeDelay.AddString("2000ms"), 2000);
	m_cboTimeDelay.SetCurSel(0);
}

void CLS_VCAEVENT_FaceRecNew::UpdatePageUI()
{
	UpdateUIFaceDetectParam();

	UpdateUIPicStreamUploadParam();
} 

void CLS_VCAEVENT_FaceRecNew::OnBnClickedBtnFacerecNewSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	FaceDetectArithmetic fda = {0};

	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNO, &fda, sizeof(FaceDetectArithmetic), &iByteReturn);
	if (0 != iRet)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		memcpy(&fda, 0, sizeof(fda));
	}
	fda.iBufSize = sizeof(FaceDetectArithmetic);
	fda.iSnapEnable = m_chkEnable.GetCheck();
	fda.iLevel = m_cboAlgoLevel.GetCurSel();
	fda.iMaxSizeEx = m_sldMaxSize.GetPos();
	fda.iMinSizeEx = m_sldMinSize.GetPos();
	fda.iPicScale = m_cboPicScal.GetCurSel() + 1;
	fda.iPointNum = m_cboPolygonPointNum.GetCurSel() + 2;
	fda.iSceneID = m_iSceneID;
	fda.iSensitiv = m_cboSensitivity.GetCurSel();
	fda.iDisplayTarget = m_chkTargetBox.GetCheck();
	fda.iSnapSpace = GetDlgItemInt(IDC_EDT_FACEREC_NEW_SNAP_SPACE);
	fda.iExposureBright = m_iExposureLight.GetPos();
	fda.iSnapTimes = m_cboSnapTimes.GetCurSel() + 1;
	
	POINT ptPolygon[MAX_FACE_DETECT_AREA_COUNT] = {0} ;
	CString cstPolygon = "";
	GetDlgItemText(IDC_EDT_FACEREC_NEW_POLYGON_AREA, cstPolygon);
	GetPointsFromString(cstPolygon, fda.iPointNum, ptPolygon);
	for (int i = 0; i < fda.iPointNum; i++)
	{
		fda.ptArea[i] = ptPolygon[i];
	}
	
	fda.iDentification = m_chkIdentEnable.GetCheck()+1;//0 not supported 1 not enabled 2 enabled
	fda.iDevType = m_cboDevType.GetCurSel();
	CString csQpvalueBig,csiQpvalueSmall;
	m_iQpvalueBig.GetWindowText(csQpvalueBig);
	m_iQpvalueSmall.GetWindowText(csiQpvalueSmall);
	fda.iQpvalueBig = _ttoi(csQpvalueBig);
	fda.iQpvalueSmall = _ttoi(csiQpvalueSmall);
	fda.iAlgSnapMode = m_cboSnapMode.GetCurSel();

	fda.iDisplayRule = m_chkRuleBox.GetCheck();
	fda.iPushMode = m_cboPushMode.GetCurSel() + 1;
	if (3 == fda.iPushMode)//customize
	{
		fda.iPushLevel = m_cboPushLevel.GetCurSel() + 1;
	}
	else if(4 == fda.iPushMode)//timing
	{
		fda.iPushLevel = GetDlgItemInt(IDC_EDIT_FACEREC_NEW_PUSH_TIME);
	}
	else if (2 == fda.iPushMode)//optimal
	{
		fda.iDelayTime = (int)m_cboTimeDelay.GetItemData(m_cboTimeDelay.GetCurSel());
	}
	
	fda.iSnapMode = m_cboSnapType.GetCurSel() + 1;
	if (3 == fda.iSnapMode)//customize
	{
		fda.iSnapLevel = m_sldSnapLevel.GetPos();
	}

	fda.iTimeSpace = (int)m_cboTimeInterval.GetItemData(m_cboTimeInterval.GetCurSel());

    for (int idx = 0; idx < CHK_FACEATTR_ALARM_CUR_NUM; idx++)
    {
        fda.iFaceAttrAlarm[idx] = (BST_CHECKED == m_chkFaceAttrAlarmEnable[idx].GetCheck()) ? 1 : 0;
    }
    fda.iDelayPushSpan = m_cboDelayPushSpan.GetItemData(m_cboDelayPushSpan.GetCurSel());
	iRet = NetClient_SetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNO, &fda, sizeof(FaceDetectArithmetic));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_FaceRecNew::NetClient_SetDevConfig[NET_CLIENT_FACE_DETECT_ARITHMETIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_FaceRecNew::NetClient_SetDevConfig[NET_CLIENT_FACE_DETECT_ARITHMETIC] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_FaceRecNew::OnBnClickedBtnFacerecNewPolygonAreaDraw()
{
	if (NULL == m_pDlgVideoView)
	{
		m_pDlgVideoView = new CLS_VideoViewForDraw();
		if (NULL == m_pDlgVideoView)
		{
			return ;
		}
	}

	m_pDlgVideoView->Init(m_iLogonID, m_iChannelNO, m_iStreamNO);
	m_pDlgVideoView->SetDrawType(DrawType_perimeter);
	int iPointNum = 0;
	int iDirection = 0;
	char cPointBuf[MAX_POINTBUF_LEN] = {0};
	int iSetRet = m_pDlgVideoView->SetPointRegionParam(cPointBuf, &iPointNum, &iDirection);
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
			m_edtPolygonArea.SetWindowText(cPointBuf);
			m_cboPolygonPointNum.SetCurSel(iPointNum - 2);
		}
		else
		{
			m_edtPolygonArea.SetWindowText(_T(""));
			m_cboPolygonPointNum.SetCurSel(-1);
		}
	}
	else
	{
		// TODO: Nothing
	}
	delete m_pDlgVideoView;
	m_pDlgVideoView = NULL;
}

void CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewMaxSize(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinSize = m_sldMinSize.GetPos();
	int iMaxSize = m_sldMaxSize.GetPos();
	SetDlgItemInt(IDC_STC_FACEREC_NEW_MAX_SIZE_NUM, iMaxSize);
	if (iMinSize >= iMaxSize)
	{
		m_sldMinSize.SetPos(iMaxSize - 1);
		SetDlgItemInt(IDC_STC_FACEREC_NEW_MIN_SIZE_NUM, m_sldMinSize.GetPos());
	}

	*pResult = 0;
}

void CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewMinSize(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);

	int iMinSize = m_sldMinSize.GetPos();
	int iMaxSize = m_sldMaxSize.GetPos();
	SetDlgItemInt(IDC_STC_FACEREC_NEW_MIN_SIZE_NUM, iMinSize);
	if (iMaxSize <= iMinSize)
	{
		m_sldMaxSize.SetPos(iMinSize + 1);
		SetDlgItemInt(IDC_STC_FACEREC_NEW_MAX_SIZE_NUM, m_sldMaxSize.GetPos());
	}

	*pResult = 0;
}

void CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSlider1(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	// TODO: Add your control notification handler code here
	int data = m_iExposureLight.GetPos();
    SetDlgItemInt(IDC_STATIC_DATAEXPO, m_iExposureLight.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_FaceRecNew::OnCbnSelchangeCboFacerecNewPushMode()
{
	int iPushMode = m_cboPushMode.GetCurSel() + 1;
	if (3 == iPushMode)//customize
	{
		m_cboPushLevel.EnableWindow(TRUE);
		m_edtPushTime.EnableWindow(FALSE);
		m_cboTimeDelay.EnableWindow(FALSE);
	}
	else if (4 == iPushMode)//timing
	{
		m_cboPushLevel.EnableWindow(FALSE);
		m_edtPushTime.EnableWindow(TRUE);
		m_cboTimeDelay.EnableWindow(FALSE);
	}
	else if (2 == iPushMode)//optimal
	{
		m_cboPushLevel.EnableWindow(FALSE);
		m_edtPushTime.EnableWindow(FALSE);
		m_cboTimeDelay.EnableWindow(TRUE);
	}
	else
	{
		m_cboPushLevel.EnableWindow(FALSE);
		m_edtPushTime.EnableWindow(FALSE);
		m_cboTimeDelay.EnableWindow(FALSE);
	}
}

void CLS_VCAEVENT_FaceRecNew::OnCbnSelchangeCboFacerecNewSnapMode()
{
	int iSnapMode = m_cboSnapType.GetCurSel() + 1;
	if (3 == iSnapMode)//customize
	{
		m_sldSnapLevel.EnableWindow(TRUE);
	}
	else
	{
		m_sldSnapLevel.EnableWindow(FALSE);
	}

}

void CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewSnapLevel(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_FACEREC_NEW_SNAP_NUM, m_sldSnapLevel.GetPos());
	*pResult = 0;
}

void CLS_VCAEVENT_FaceRecNew::OnBnClickedBtnFacerecNewPicuploadSet()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	PicStreamUploadParam tInfo = {0};
	tInfo.iSize = sizeof(PicStreamUploadParam);
	tInfo.iSceneId		= m_iSceneID;
	tInfo.iRuleNo		= m_iRuleID;
	tInfo.iPicType		= m_cboPicSendType.GetCurSel();
	tInfo.iSnapEnable	= m_cboPicUploadEnable.GetCurSel();
	tInfo.iIsOsd		= m_cboPicOsd.GetCurSel();
	tInfo.iQpvalue		= m_sldUploadPicQuality.GetPos();
	tInfo.iFaceFrameEnable	= m_cboFaceFrameEnbale.GetCurSel();

	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNO, &tInfo, sizeof(PicStreamUploadParam));
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_FaceRecNew::NetClient_SetDevConfig[VCA_CMD_PICSTREAM_UPLOADPARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_FaceRecNew::NetClient_SetDevConfig[VCA_CMD_PICSTREAM_UPLOADPARAM] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
	
}

void CLS_VCAEVENT_FaceRecNew::UpdateUIFaceDetectParam()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}

	FaceDetectArithmetic fda = {0};
	fda.iSceneID = m_iSceneID;

	int iByteReturn = 0;
	int iRet = NetClient_GetDevConfig(m_iLogonID, NET_CLIENT_FACE_DETECT_ARITHMETIC, m_iChannelNO, &fda, sizeof(FaceDetectArithmetic), &iByteReturn);
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_FaceRecNew::NetClient_GetDevConfig[NET_CLIENT_FACE_DETECT_ARITHMETIC] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_chkEnable.SetCheck(fda.iSnapEnable);
		m_chkTargetBox.SetCheck(fda.iDisplayTarget);

		m_cboAlgoLevel.SetCurSel(fda.iLevel);
		m_cboPicScal.SetCurSel(fda.iPicScale - 1);
		m_cboPolygonPointNum.SetCurSel(fda.iPointNum - 2);
		m_cboSensitivity.SetCurSel(fda.iSensitiv);
		m_cboSnapTimes.SetCurSel(fda.iSnapTimes - 1);	
		if (fda.iDentification > 0)
		{
			m_chkIdentEnable.EnableWindow(TRUE);
			m_chkIdentEnable.SetCheck(fda.iDentification -1);//0 not supported 1 not enabled 2 enabled
		}
		else
		{
			m_chkIdentEnable.EnableWindow(FALSE);
		}
		m_cboDevType.SetCurSel(fda.iDevType);//0 ipc 1 nvr
		m_cboSnapMode.SetCurSel(fda.iAlgSnapMode);

		m_sldMaxSize.SetPos(fda.iMaxSizeEx);
		int iPostMax = m_sldMaxSize.GetPos();
		SetDlgItemInt(IDC_STC_FACEREC_NEW_MAX_SIZE_NUM, iPostMax);
		m_sldMinSize.SetPos(fda.iMinSizeEx);
		int iPostMin = m_sldMinSize.GetPos();
		SetDlgItemInt(IDC_STC_FACEREC_NEW_MIN_SIZE_NUM, iPostMin);

		SetDlgItemInt(IDC_EDT_FACEREC_NEW_SNAP_SPACE, fda.iSnapSpace);

		m_iExposureLight.SetPos(fda.iExposureBright);
		SetDlgItemInt(IDC_STATIC_DATAEXPO, m_iExposureLight.GetPos());

		CString cstPolygonBuf = "";
		int iPointNum = fda.iPointNum;
		if ((iPointNum >= POINT_NUM_MIN) && (iPointNum <= POINT_NUM_MAX))
		{
			for(int i = 0; i < iPointNum; i++)
			{
				cstPolygonBuf.AppendFormat("(%d, %d)", fda.ptArea[i].x, fda.ptArea[i].y);
			}
			SetDlgItemText(IDC_EDT_FACEREC_NEW_POLYGON_AREA, cstPolygonBuf);
		}
		CString strQPic;
		strQPic.Format("%d",fda.iQpvalueBig);
		m_iQpvalueBig.SetWindowText(strQPic);
		strQPic.Format("%d",fda.iQpvalueSmall);
		m_iQpvalueSmall.SetWindowText(strQPic);

		m_cboSnapMode.SetCurSel(fda.iAlgSnapMode);

		m_chkRuleBox.SetCheck(fda.iDisplayRule);

		m_cboPushMode.SetCurSel(fda.iPushMode-1);
		if (3 == fda.iPushMode)//customize
		{
			m_cboPushLevel.SetCurSel(fda.iPushLevel-1);
			m_cboPushLevel.EnableWindow(TRUE);
			m_edtPushTime.EnableWindow(FALSE);
			m_cboTimeDelay.EnableWindow(FALSE);
		}
		else if (4 == fda.iPushMode)//timing
		{
			SetDlgItemInt(IDC_EDIT_FACEREC_NEW_PUSH_TIME, fda.iPushLevel);
			m_cboPushLevel.EnableWindow(FALSE);
			m_edtPushTime.EnableWindow(TRUE);
			m_cboTimeDelay.EnableWindow(FALSE);
		}
		else if (2 == fda.iPushMode)//optimal
		{
			for (int i = 0; i < m_cboTimeDelay.GetCount(); ++i)
			{
				if (m_cboTimeDelay.GetItemData(i) == fda.iDelayTime )
				{
					m_cboTimeDelay.SetCurSel(i);
					break;
				}
			}
			
			m_cboPushLevel.EnableWindow(FALSE);
			m_edtPushTime.EnableWindow(FALSE);
			m_cboTimeDelay.EnableWindow(TRUE);
		}
		else
		{
			m_cboPushLevel.EnableWindow(FALSE);
			m_edtPushTime.EnableWindow(FALSE);
			m_cboTimeDelay.EnableWindow(FALSE);
		}

		m_cboSnapType.SetCurSel(fda.iSnapMode-1);
		if (3 == fda.iSnapMode)//customize
		{
			m_sldSnapLevel.SetPos(fda.iSnapLevel);
			SetDlgItemInt(IDC_STC_FACEREC_NEW_SNAP_NUM, m_sldSnapLevel.GetPos());
			m_sldSnapLevel.EnableWindow(TRUE);
		}
		else
		{
			m_sldSnapLevel.EnableWindow(FALSE);
		}

		for (int i = 0; i < m_cboTimeInterval.GetCount(); ++i)
		{
			if (m_cboTimeInterval.GetItemData(i) == fda.iTimeSpace )
			{
				m_cboTimeInterval.SetCurSel(i);
				break;
			}
		}

        for (int i = 0; i < m_cboDelayPushSpan.GetCount(); ++i)
        {
            if (m_cboDelayPushSpan.GetItemData(i) == fda.iDelayPushSpan )
            {
                m_cboDelayPushSpan.SetCurSel(i);
                break;
            }
        }

        for (int idx = 0; idx < CHK_FACEATTR_ALARM_CUR_NUM; idx++)
        {
            if (1 == fda.iFaceAttrAlarm[idx])
                m_chkFaceAttrAlarmEnable[idx].SetCheck(BST_CHECKED);
            else
                m_chkFaceAttrAlarmEnable[idx].SetCheck(BST_UNCHECKED);
        }

		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_FaceRecNew::NetClient_GetDevConfig[NET_CLIENT_FACE_DETECT_ARITHMETIC] (%d, %d)", m_iLogonID, m_iChannelNO);
	}
}

void CLS_VCAEVENT_FaceRecNew::UpdateUIPicStreamUploadParam()
{
	if (m_iLogonID == -1 || m_iChannelNO == -1)
	{
		AddLog(LOG_TYPE_MSG, "", "CLS_VCAEVENT_FaceRecNew::Invalid Logon id or Channel number(%d,%d)", m_iLogonID,m_iChannelNO);
		return;
	}
	
	PicStreamUploadParam tInfo = {0};
	tInfo.iSize = sizeof(PicStreamUploadParam);
	tInfo.iSceneId		= m_iSceneID;
	tInfo.iRuleNo		= m_iRuleID;
	tInfo.iPicType		= m_cboPicSendType.GetCurSel();

	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_PICSTREAM_UPLOADPARAM, m_iChannelNO, &tInfo, sizeof(PicStreamUploadParam));
	
	if (iRet < 0)
	{
		AddLog(LOG_TYPE_FAIL,"","CLS_VCAEVENT_FaceRecNew::NetClient_GetDevConfig[VCA_CMD_PICSTREAM_UPLOADPARAM] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	else
	{
		m_cboPicUploadEnable.SetCurSel(tInfo.iSnapEnable);
		m_cboPicOsd.SetCurSel(tInfo.iIsOsd);
		
		m_sldUploadPicQuality.SetPos(tInfo.iQpvalue);
		int iPicQuality = m_sldUploadPicQuality.GetPos();
		SetDlgItemInt(IDC_STC_FACEREC_NEW_UPLOAD_PICQUALITY_NUM, iPicQuality);
		m_cboFaceFrameEnbale.SetCurSel(tInfo.iFaceFrameEnable);
		AddLog(LOG_TYPE_SUCC,"","CLS_VCAEVENT_FaceRecNew::NetClient_GetDevConfig[VCA_CMD_PICSTREAM_UPLOADPARAM] (%d, %d)", m_iLogonID, m_iChannelNO);

	}

}
void CLS_VCAEVENT_FaceRecNew::OnCbnSelchangeCboFacerecNewPicsendType()
{
	UpdateUIPicStreamUploadParam();
}

void CLS_VCAEVENT_FaceRecNew::OnNMCustomdrawSldFacerecNewUploadPicquality(NMHDR *pNMHDR, LRESULT *pResult)
{
	LPNMCUSTOMDRAW pNMCD = reinterpret_cast<LPNMCUSTOMDRAW>(pNMHDR);
	SetDlgItemInt(IDC_STC_FACEREC_NEW_UPLOAD_PICQUALITY_NUM, m_sldUploadPicQuality.GetPos());
	*pResult = 0;
}
