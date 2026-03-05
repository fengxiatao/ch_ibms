#include "../BasePage.h"
#include "afxwin.h"
#include "afxcmn.h"


// IrrigationNotify dialog

class IrrigationNotify : public CLS_BasePage
{
	DECLARE_DYNAMIC(IrrigationNotify)

public:
	IrrigationNotify(CWnd* pParent = NULL);   // standard constructor
	virtual ~IrrigationNotify();

// Dialog Data
	enum { IDD = IDD_DLG_CFG_IRRIGATION_NOTIFY };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	virtual BOOL OnInitDialog();
	virtual void OnLanguageChanged(int _iLanguage);
	virtual void OnChannelChanged(int _iLogonID, int _iChannelNo, int _iStreamNo);
	virtual void OnParamChangeNotify(int _iLogonID, int _iChannelNo, int _iParaType,void* _pPara,int _iUserData);
	void UpdateIrrigationNotify(int _iLogonID, int _iChannelNo, IrrigationPara* _ptIrrigationPara);
	void UI_UpdateUIText();
	CString GetStringByType(int _iType);
	CString GetStringBySrc(int _iSrc);
private:
	int m_iLogonID;
	int m_iChannelNo;
	IrrigationPara m_tIrrigationPara;
	CListCtrl m_lstIrrigationNotify;
public:
	afx_msg void OnLvnItemchangedListIrrigationNotify(NMHDR *pNMHDR, LRESULT *pResult);
	//CListCtrl m_listWaterFlow;
	CListCtrl m_lstWaterFlow;
    afx_msg void OnBnClickedButtonQuery();
    CComboBox m_cboDataType;
    afx_msg void OnBnClickedButtonQueryCustomType();
};
