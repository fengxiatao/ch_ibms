
#ifndef _COMMON_CITYS_H
#define _COMMON_CITYS_H

void UI_InitProvience(CComboBox &_cbo);
void UI_InitCitys(CComboBox &_cbo, int _iProvience);
void UI_UpdataProvience(CComboBox &_cbo, int _iProvience);
void UI_UpdataCitys(CComboBox &_cbo, int _iCitys);
CString GetPlaceStr(int _iPlace);
void UI_InitCountry(CComboBox &_cbo);
void UI_UpdateCountry(CComboBox &_cbo, int _iCountry);
void InitCountryMap();
#endif
