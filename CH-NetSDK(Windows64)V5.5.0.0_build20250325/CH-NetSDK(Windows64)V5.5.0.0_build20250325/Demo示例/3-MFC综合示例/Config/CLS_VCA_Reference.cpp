// CLS_VCA_Reference.cpp : Implementation file
//

#include "stdafx.h"
#include "NetClientDemo.h"
#include ".\CLS_VCA_Reference.h"
#define MAX_SCENEID_NUM						16

// CLS_VCA_Reference dialog

IMPLEMENT_DYNAMIC(CLS_VCA_Reference, CDialog)

CLS_VCA_Reference::CLS_VCA_Reference(CWnd* pParent /*=NULL*/)
	: CLS_BasePage(CLS_VCA_Reference::IDD, pParent)
{
	m_iPointTypeIndex = 0;
	m_iPointNum = 4;
	memset(&m_iPixelPointX, 0, sizeof(m_iPixelPointX));
	memset(&m_iPixelPointY, 0, sizeof(m_iPixelPointY));
	memset(&m_iPixelPointZ, 0, sizeof(m_iPixelPointZ));
	memset(&m_iWorldPointX, 0, sizeof(m_iWorldPointX));
	memset(&m_iWorldPointY, 0, sizeof(m_iWorldPointY));
	memset(&m_iWorldPointZ, 0, sizeof(m_iWorldPointZ));
	memset(&m_iIpcPara, 0, sizeof(m_iIpcPara));
	memset(&m_iCoordinate0Para, 0, sizeof(m_iCoordinate0Para));
	memset(&m_iCoordinateXPara, 0, sizeof(m_iCoordinateXPara));
	memset(&m_iCoordinateYPara, 0, sizeof(m_iCoordinateYPara));
}

CLS_VCA_Reference::~CLS_VCA_Reference()
{
}

void CLS_VCA_Reference::DoDataExchange(CDataExchange* pDX)
{
	CLS_BasePage::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_LIST_PIXEL_POINT, m_listPixel);
	DDX_Control(pDX, IDC_LIST_WORLD_POINT, m_listWorld);
	DDX_Control(pDX, IDC_COMBO_POINT_TYPE, m_cboPointType);
	DDX_Control(pDX, IDC_COMBO_REFERENCE_SCENEID, m_cboSceneId);
	DDX_Control(pDX, IDC_COMBO_POINT_NUM, m_cboPointNum);
	DDX_Control(pDX, IDC_EDIT_IPC_HIGH, m_editPicHigh);
	DDX_Control(pDX, IDC_BUTTON_SAVE_POINT_PARA, m_btSaveInfo);
	DDX_Control(pDX, IDC_BUTTON_GET_REFERENCE, m_btGetInfo);
	DDX_Control(pDX, IDC_BUTTON_SET_REFERENCE, m_btSetPara);
	DDX_Control(pDX, IDC_EDIT_POINTX0, m_editPointX[0]);
	DDX_Control(pDX, IDC_EDIT_POINTX1, m_editPointX[1]);
	DDX_Control(pDX, IDC_EDIT_POINTX2, m_editPointX[2]);
	DDX_Control(pDX, IDC_EDIT_POINTX3, m_editPointX[3]);
	DDX_Control(pDX, IDC_EDIT_POINTX4, m_editPointX[4]);
	DDX_Control(pDX, IDC_EDIT_POINTX5, m_editPointX[5]);
	DDX_Control(pDX, IDC_EDIT_POINTX6, m_editPointX[6]);
	DDX_Control(pDX, IDC_EDIT_POINTX7, m_editPointX[7]);
	DDX_Control(pDX, IDC_EDIT_POINTX8, m_editPointX[8]);
	DDX_Control(pDX, IDC_EDIT_POINTX9, m_editPointX[9]);

	DDX_Control(pDX, IDC_EDIT_POINTY0, m_editPointY[0]);
	DDX_Control(pDX, IDC_EDIT_POINTY1, m_editPointY[1]);
	DDX_Control(pDX, IDC_EDIT_POINTY2, m_editPointY[2]);
	DDX_Control(pDX, IDC_EDIT_POINTY3, m_editPointY[3]);
	DDX_Control(pDX, IDC_EDIT_POINTY4, m_editPointY[4]);
	DDX_Control(pDX, IDC_EDIT_POINTY5, m_editPointY[5]);
	DDX_Control(pDX, IDC_EDIT_POINTY6, m_editPointY[6]);
	DDX_Control(pDX, IDC_EDIT_POINTY7, m_editPointY[7]);
	DDX_Control(pDX, IDC_EDIT_POINTY8, m_editPointY[8]);
	DDX_Control(pDX, IDC_EDIT_POINTY9, m_editPointY[9]);

	DDX_Control(pDX, IDC_EDIT_POINTZ0, m_editPointZ[0]);
	DDX_Control(pDX, IDC_EDIT_POINTZ1, m_editPointZ[1]);
	DDX_Control(pDX, IDC_EDIT_POINTZ2, m_editPointZ[2]);
	DDX_Control(pDX, IDC_EDIT_POINTZ3, m_editPointZ[3]);
	DDX_Control(pDX, IDC_EDIT_POINTZ4, m_editPointZ[4]);
	DDX_Control(pDX, IDC_EDIT_POINTZ5, m_editPointZ[5]);
	DDX_Control(pDX, IDC_EDIT_POINTZ6, m_editPointZ[6]);
	DDX_Control(pDX, IDC_EDIT_POINTZ7, m_editPointZ[7]);
	DDX_Control(pDX, IDC_EDIT_POINTZ8, m_editPointZ[8]);
	DDX_Control(pDX, IDC_EDIT_POINTZ9, m_editPointZ[9]);

	DDX_Text(pDX, IDC_EDIT_IPC_PARAX, m_iIpcPara[0]);
	DDX_Text(pDX, IDC_EDIT_IPC_PARAY, m_iIpcPara[1]);
	DDX_Text(pDX, IDC_EDIT_IPC_PARAZ, m_iIpcPara[2]);
	DDX_Text(pDX, IDC_EDIT_COORD_X_PARAX, m_iCoordinate0Para[0]);
	DDX_Text(pDX, IDC_EDIT_COORD_X_PARAY, m_iCoordinate0Para[1]);
	DDX_Text(pDX, IDC_EDIT_COORD_X_PARAZ, m_iCoordinate0Para[2]);
	DDX_Text(pDX, IDC_EDIT_COORD_Y_PARAX, m_iCoordinateXPara[0]);
	DDX_Text(pDX, IDC_EDIT_COORD_Y_PARAY, m_iCoordinateXPara[1]);
	DDX_Text(pDX, IDC_EDIT_COORD_Y_PARAZ, m_iCoordinateXPara[2]);
	DDX_Text(pDX, IDC_EDIT_COORD_Z_PARAX, m_iCoordinateYPara[0]);
	DDX_Text(pDX, IDC_EDIT_COORD_Z_PARAY, m_iCoordinateYPara[1]);
	DDX_Text(pDX, IDC_EDIT_COORD_Z_PARAZ, m_iCoordinateYPara[2]);
}


BOOL CLS_VCA_Reference::OnInitDialog()
{
	CLS_BasePage::OnInitDialog();
	for(int i = 0; i < MAX_SCENEID_NUM; i++) {
		CString str;
		str.Format(_T("%d"), i);
		m_cboSceneId.AddString(str);
	}
	m_cboSceneId.SetCurSel(0);
	for(int i = 4; i <= MAX_COORDINATE_POINT_NUM; i++) {	//Number of reference object coordinate points 4~10
		CString str;
		str.Format(_T("%d"), i);
		m_cboPointNum.AddString(str);
	}
	m_cboPointNum.SetCurSel(0);
	SetEditState(4);

	m_listPixel.InsertColumn(0, _T(""), LVCFMT_CENTER, 0);
	m_listPixel.InsertColumn(1, _T("X"), LVCFMT_CENTER, 85);
	m_listPixel.InsertColumn(2, _T("Y"), LVCFMT_CENTER, 85);
	m_listPixel.InsertColumn(3, _T("Z"), LVCFMT_CENTER, 85);
	m_listPixel.DeleteColumn(0);

	m_listWorld.InsertColumn(0, _T(""), LVCFMT_CENTER, 0);
	m_listWorld.InsertColumn(1, _T("X"), LVCFMT_CENTER, 85);
	m_listWorld.InsertColumn(2, _T("Y"), LVCFMT_CENTER, 85);
	m_listWorld.InsertColumn(3, _T("Z"), LVCFMT_CENTER, 85);
	m_listWorld.DeleteColumn(0);

	UI_UpdateUIText();
	return TRUE;
}

void CLS_VCA_Reference::OnLanguageChanged(int _iLanguage)
{
	UI_UpdateUIText();
}

void CLS_VCA_Reference::UI_UpdateUIText()
{
	SetDlgItemText(IDC_STATIC_REFERENCE_SCENEID, GetTextByLan(_T("场景ID"), _T("Scene ID")));
	SetDlgItemText(IDC_STATIC_POINT_NUM, GetTextByLan(_T("参考物坐标点个数"), _T("Number of reference coordinate points")));
	SetDlgItemText(IDC_STATIC_PIC_HIGH, GetTextByLan(_T("相机安装高度"), _T("Camera mounting height")));
	SetDlgItemText(IDC_STATIC_SELECT_REFERENCE, GetTextByLan(_T("坐标类型"), _T("Coordinate type")));
	SetDlgItemText(IDC_STATIC_FIRST_POINT, GetTextByLan(_T("第1个坐标"), _T("1st coordinate")));

	SetDlgItemText(IDC_STATIC_SECOND_POINT, GetTextByLan(_T("第2个坐标"), _T("2nd coordinate")));
	SetDlgItemText(IDC_STATIC_THIRD_POINT, GetTextByLan(_T("第3个坐标"), _T("3rd coordinate")));
	SetDlgItemText(IDC_STATIC_FOURTH_POINT, GetTextByLan(_T("第4个坐标"), _T("4th coordinate")));
	SetDlgItemText(IDC_STATIC_FIFTH_POINT, GetTextByLan(_T("第5个坐标"), _T("5th coordinate")));
	SetDlgItemText(IDC_STATIC_SIXTH_POINT, GetTextByLan(_T("第6个坐标"), _T("6th coordinate")));

	SetDlgItemText(IDC_STATIC_SEVENTH_POINT, GetTextByLan(_T("第7个坐标"), _T("7th coordinate")));
	SetDlgItemText(IDC_STATIC_EIGHTH_POINT, GetTextByLan(_T("第8个坐标"), _T("8t coordinate")));
	SetDlgItemText(IDC_STATIC_NINTH_POINT, GetTextByLan(_T("第9个坐标"), _T("9th coordinate")));
	SetDlgItemText(IDC_STATIC_TENTH_POINT, GetTextByLan(_T("第10个坐标"), _T("10th coordinate")));
	SetDlgItemText(IDC_STATIC_IPC_PARA, GetTextByLan(_T("相对原点坐标参数"), _T("Relative origin coordinate parameters")));

	SetDlgItemText(IDC_STATIC_COORD_X_PARA, GetTextByLan(_T("地图坐标系0点参数"), _T("Map coordinate system 0 point parameters")));
	SetDlgItemText(IDC_STATIC_COORD_Y_PARA, GetTextByLan(_T("地图坐标系X轴上选定的参数"), _T("Selected parameters on the X axis of the map coordinate system")));
	SetDlgItemText(IDC_STATIC_COORD_Z_PARA, GetTextByLan(_T("地图坐标系Y轴上选定的参数"), _T("Selected parameters on the Y axis of the map coordinate system")));
	SetDlgItemText(IDC_STATIC_PIXEL_POINT, GetTextByLan(_T("像素坐标"), _T("Pixel coordinates")));
	SetDlgItemText(IDC_STATIC_WORLD_POINT, GetTextByLan(_T("世界坐标"), _T("World coordinates")));

	m_btSaveInfo.SetWindowText(GetTextByLan(_T("保存坐标信息"), _T("Save info")));
	m_btSetPara.SetWindowText(GetTextByLan(_T("设置"), _T("Set")));
	m_btGetInfo.SetWindowText(GetTextByLan(_T("获取"), _T("Get")));

	m_cboPointType.ResetContent();
	m_cboPointType.AddString(GetTextByLan(_T("像素坐标"), _T("Pixel coordinates")));
	m_cboPointType.AddString(GetTextByLan(_T("世界坐标"), _T("World coordinates")));
	m_cboPointType.SetCurSel(m_iPointTypeIndex);
}

void CLS_VCA_Reference::SetEditState(int _iPointNum)
{
	for(int i = 0; i < MAX_COORDINATE_POINT_NUM; i++)
	{
		m_editPointX[i].SetWindowText(_T(""));
		m_editPointY[i].SetWindowText(_T(""));
		m_editPointZ[i].SetWindowText(_T(""));
	}
	for(int i = 0; i < _iPointNum; i++)
	{
		m_editPointX[i].EnableWindow(TRUE);
		m_editPointY[i].EnableWindow(TRUE);
		m_editPointZ[i].EnableWindow(TRUE);
	}
	for(int i = _iPointNum; i < MAX_COORDINATE_POINT_NUM; i++)
	{
		m_editPointX[i].EnableWindow(FALSE);
		m_editPointY[i].EnableWindow(FALSE);
		m_editPointZ[i].EnableWindow(FALSE);
	}
	if(0 == m_iPointTypeIndex)
	{
		ShowPixelPoint();
	}else if(1 == m_iPointTypeIndex)
	{
		ShowWorldPoint();
	}
}

void CLS_VCA_Reference::ShowPixelPoint()
{
	CString strX;
	CString strY;
	CString strZ;
	for(int i = 0; i < MAX_COORDINATE_POINT_NUM; i++)
	{
		if(!m_editPointX[i].IsWindowEnabled())
		{
			break;
		}
		strX.Empty();
		strY.Empty();
		strZ.Empty();
		strX.Format(_T("%d"), m_iPixelPointX[i]);
		m_editPointX[i].SetWindowText(strX);
		strY.Format(_T("%d"), m_iPixelPointY[i]);
		m_editPointY[i].SetWindowText(strY);
		strZ.Format(_T("%d"), m_iPixelPointZ[i]);
		m_editPointZ[i].SetWindowText(strZ);
	}
}

void CLS_VCA_Reference::ShowWorldPoint()
{
	CString strX;
	CString strY;
	CString strZ;
	for(int i = 0; i < MAX_COORDINATE_POINT_NUM; i++)
	{
		if(!m_editPointX[i].IsWindowEnabled())
		{
			break;
		}
		strX.Empty();
		strY.Empty();
		strZ.Empty();
		strX.Format(_T("%d"), m_iWorldPointX[i]);
		m_editPointX[i].SetWindowText(strX);
		strY.Format(_T("%d"), m_iWorldPointY[i]);
		m_editPointY[i].SetWindowText(strY);
		strZ.Format(_T("%d"), m_iWorldPointZ[i]);
		m_editPointZ[i].SetWindowText(strZ);
	}
}

CString CLS_VCA_Reference::IntToCstr(int _iNum)
{
	CString strNum;
	strNum.Format(_T("%d"), _iNum);
	return strNum;
}

BEGIN_MESSAGE_MAP(CLS_VCA_Reference, CDialog)
	ON_BN_CLICKED(IDC_BUTTON_GET_REFERENCE, &CLS_VCA_Reference::OnBnClickedButtonGetReference)
	ON_BN_CLICKED(IDC_BUTTON_SET_REFERENCE, &CLS_VCA_Reference::OnBnClickedButtonSetReference)
	ON_BN_CLICKED(IDC_BUTTON_SAVE_POINT_PARA, &CLS_VCA_Reference::OnBnClickedButtonSavePointPara)
	ON_CBN_SELCHANGE(IDC_COMBO_POINT_NUM, &CLS_VCA_Reference::OnCbnSelchangeComboPointNum)
	ON_CBN_SELCHANGE(IDC_COMBO_POINT_TYPE, &CLS_VCA_Reference::OnCbnSelchangeComboPointType)
END_MESSAGE_MAP()

// CLS_VCA_Reference message handler

void CLS_VCA_Reference::OnBnClickedButtonGetReference()
{
	// TODO: Add control notification handler code here
	CalibrationReferenceInfo tInfo;
	memset(&tInfo, 0, sizeof(CalibrationReferenceInfo));
	tInfo.iSceneId = m_cboSceneId.GetCurSel();
	int iRet = NetClient_VCAGetConfig(m_iLogonID, VCA_CMD_CALIBRATION_REFERENCE, m_iChannelNO, &tInfo, sizeof(CalibrationReferenceInfo));
	if(RET_SUCCESS == iRet)
	{
		m_listPixel.DeleteAllItems();
		m_listWorld.DeleteAllItems();
		for(int i = 0; i < MAX_COORDINATE_POINT_NUM && i < tInfo.iPointNum; i++)
		{
			m_iPixelPointX[i] = tInfo.tPointImg[i].iX;
			m_listPixel.InsertItem(i, (LPCTSTR)IntToCstr(tInfo.tPointImg[i].iX));
			m_iPixelPointY[i] = tInfo.tPointImg[i].iY;
			m_listPixel.SetItemText(i, 1, (LPCTSTR)IntToCstr(tInfo.tPointImg[i].iY));
			m_iPixelPointZ[i] = tInfo.tPointImg[i].iZ;
			m_listPixel.SetItemText(i, 2, (LPCTSTR)IntToCstr(tInfo.tPointImg[i].iZ));
			m_iWorldPointX[i] = tInfo.tPointWld[i].iX;
			m_listWorld.InsertItem(i, (LPCTSTR)IntToCstr(tInfo.tPointWld[i].iX));
			m_iWorldPointY[i] = tInfo.tPointWld[i].iY;
			m_listWorld.SetItemText(i, 1, (LPCTSTR)IntToCstr(tInfo.tPointWld[i].iY));
			m_iWorldPointZ[i] = tInfo.tPointWld[i].iZ;
			m_listWorld.SetItemText(i, 2, (LPCTSTR)IntToCstr(tInfo.tPointWld[i].iZ));
		}
		m_iPointNum = tInfo.iPointNum - 4;
		m_cboPointNum.SetCurSel(m_iPointNum);
		OnCbnSelchangeComboPointNum();
		m_editPicHigh.SetWindowText(IntToCstr(tInfo.iIpcHigh));
		m_iIpcPara[0] = tInfo.tIpcPara.iX;
		m_iIpcPara[1] = tInfo.tIpcPara.iY;
		m_iIpcPara[2] = tInfo.tIpcPara.iZ;
		m_iCoordinate0Para[0] = tInfo.tCoordinateOPara.iX;
		m_iCoordinate0Para[1] = tInfo.tCoordinateOPara.iY;
		m_iCoordinate0Para[2] = tInfo.tCoordinateOPara.iZ;
		m_iCoordinateXPara[0] = tInfo.tCoordinateXPara.iX;
		m_iCoordinateXPara[1] = tInfo.tCoordinateXPara.iY;
		m_iCoordinateXPara[2] = tInfo.tCoordinateXPara.iZ;
		m_iCoordinateYPara[0] = tInfo.tCoordinateYPara.iX;
		m_iCoordinateYPara[1] = tInfo.tCoordinateYPara.iY;
		m_iCoordinateYPara[2] = tInfo.tCoordinateYPara.iZ;
		UpdateData(FALSE);
		AddLog(LOG_TYPE_SUCC, "","CLS_VCA_Reference::NetClient_VCAGetConfig[VCA_CMD_CALIBRATION_REFERENCE] (%d, %d)", m_iLogonID, m_iChannelNO);
	}else {
		AddLog(LOG_TYPE_FAIL,"","CLS_VCA_Reference::NetClient_VCAGetConfig[VCA_CMD_CALIBRATION_REFERENCE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
}

void CLS_VCA_Reference::OnBnClickedButtonSetReference()
{
	// TODO: Add control notification handler code here
	UpdateData(TRUE);
	CalibrationReferenceInfo tInfo;
	memset(&tInfo, 0, sizeof(CalibrationReferenceInfo));
	tInfo.iSceneId = m_cboSceneId.GetCurSel();
	tInfo.iPointNum = m_iPointNum;
	for(int i =0; i < m_iPointNum && i < MAX_COORDINATE_POINT_NUM; i++)
	{
		tInfo.tPointImg[i].iX = m_iPixelPointX[i];
		tInfo.tPointImg[i].iY = m_iPixelPointY[i];
		tInfo.tPointImg[i].iZ = m_iPixelPointZ[i];
		tInfo.tPointWld[i].iX = m_iWorldPointX[i];
		tInfo.tPointWld[i].iY = m_iWorldPointY[i];
		tInfo.tPointWld[i].iZ = m_iWorldPointZ[i];
	}
	tInfo.iIpcHigh = GetDlgItemInt(IDC_EDIT_IPC_HIGH);
	tInfo.tIpcPara.iX = m_iIpcPara[0];
	tInfo.tIpcPara.iY = m_iIpcPara[1];
	tInfo.tIpcPara.iZ = m_iIpcPara[2];
	tInfo.tCoordinateOPara.iX = m_iCoordinate0Para[0];
	tInfo.tCoordinateOPara.iY = m_iCoordinate0Para[1];
	tInfo.tCoordinateOPara.iZ = m_iCoordinate0Para[2];
	tInfo.tCoordinateXPara.iX = m_iCoordinateXPara[0];
	tInfo.tCoordinateXPara.iY = m_iCoordinateXPara[1];
	tInfo.tCoordinateXPara.iZ = m_iCoordinateXPara[2];
	tInfo.tCoordinateYPara.iX = m_iCoordinateYPara[0];
	tInfo.tCoordinateYPara.iY = m_iCoordinateYPara[1];
	tInfo.tCoordinateYPara.iZ = m_iCoordinateYPara[2];
	int iRet = NetClient_VCASetConfig(m_iLogonID, VCA_CMD_CALIBRATION_REFERENCE, m_iChannelNO, &tInfo, sizeof(CalibrationReferenceInfo));
	if(RET_SUCCESS == iRet) {
		AddLog(LOG_TYPE_SUCC, "","CLS_VCA_Reference::NetClient_VCASetConfig[VCA_CMD_CALIBRATION_REFERENCE] (%d, %d)", m_iLogonID, m_iChannelNO);
	} else {
		AddLog(LOG_TYPE_FAIL,"","CLS_VCA_Reference::NetClient_VCASetConfig[VCA_CMD_CALIBRATION_REFERENCE] (%d, %d), error(%d)", m_iLogonID, m_iChannelNO, GetLastError());
	}
	return;
}

void CLS_VCA_Reference::OnBnClickedButtonSavePointPara()
{
	// TODO: Add control notification handler code here
	if(0 == m_iPointTypeIndex)
	{
		for(int i = 0; i < MAX_COORDINATE_POINT_NUM; i++)
		{
			if(!m_editPointX[i].IsWindowEnabled())
			{
				break;
			}
			m_iPixelPointX[i] = GetDlgItemInt(m_editPointX[i].GetDlgCtrlID());
			m_iPixelPointY[i] = GetDlgItemInt(m_editPointY[i].GetDlgCtrlID());
			m_iPixelPointZ[i] = GetDlgItemInt(m_editPointZ[i].GetDlgCtrlID());
		}
	}else if(1 == m_iPointTypeIndex)
	{
		for(int i = 0; i < MAX_COORDINATE_POINT_NUM; i++)
		{
			if(!m_editPointX[i].IsWindowEnabled())
			{
				break;
			}
			m_iWorldPointX[i] = GetDlgItemInt(m_editPointX[i].GetDlgCtrlID());
			m_iWorldPointY[i] = GetDlgItemInt(m_editPointY[i].GetDlgCtrlID());
			m_iWorldPointZ[i] = GetDlgItemInt(m_editPointZ[i].GetDlgCtrlID());
		}
	}
}

void CLS_VCA_Reference::OnCbnSelchangeComboPointNum()
{
	// TODO: Add control notification handler code here
	int iCurPointNumIndex = m_cboPointNum.GetCurSel();
	m_iPointNum = iCurPointNumIndex + 4;
	SetEditState(m_iPointNum);
}

void CLS_VCA_Reference::OnCbnSelchangeComboPointType()
{
	// TODO: Add control notification handler code here
	m_iPointTypeIndex = m_cboPointType.GetCurSel();
	if(0 == m_iPointTypeIndex)
	{
		ShowPixelPoint();
	}else if(1 == m_iPointTypeIndex)
	{
		ShowWorldPoint();
	}
}
