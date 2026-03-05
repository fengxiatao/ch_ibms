
#include "stdafx.h"
#include "CommonCitys.h"
#include <map>

using namespace std;

struct ProvienceStr 
{
	CString cstrProvience;		//Provinces, municipalities and special administrative regions
	int iSel;
	map<int, CString> mapCitys;	//city
	ProvienceStr()
	{
		iSel = 0;
	}
};

static map<int, ProvienceStr> s_mapProvience;
static void InitCityMap();

static map<int, CString> s_mapCountry;

void UI_InitCountry(CComboBox &_cbo)
{
	InitCountryMap();
	_cbo.ResetContent();
	int iSel = 0;
	map<int, CString>::iterator it = s_mapCountry.begin();
	for (;	it != s_mapCountry.end(); ++it) {
		_cbo.SetItemData(_cbo.AddString(it->second), it->first);
	}
	_cbo.SetCurSel(0);
}

void UI_UpdateCountry(CComboBox &_cbo, int _iCountry)
{
	for (int i = 0; i < _cbo.GetCount(); ++i)
	{
		if (_iCountry == _cbo.GetItemData(i))
		{
			_cbo.SetCurSel(i);
			break;
		}
	}
}

void UI_InitProvience(CComboBox &_cbo)
{
	InitCityMap();
	_cbo.ResetContent();
	int iSel = 0;
	map<int, ProvienceStr>::iterator it = s_mapProvience.begin();
	for (;	it != s_mapProvience.end(); ++it) {
		it->second.iSel = iSel++;
		_cbo.SetItemData(_cbo.AddString(it->second.cstrProvience), it->first);
	}
	_cbo.SetCurSel(0);
}

void UI_InitCitys(CComboBox &_cbo, int _iProvience)
{
	InitCityMap();
	_cbo.ResetContent();
	map<int, ProvienceStr>::iterator it = s_mapProvience.find(_iProvience);
	if (s_mapProvience.end() == it)
	{
		return;
	}
	map<int, CString>::iterator itCitys = it->second.mapCitys.begin();
	for (;	itCitys != it->second.mapCitys.end(); ++itCitys) {
		_cbo.SetItemData(_cbo.AddString(itCitys->second), itCitys->first);
	}
	_cbo.SetCurSel(0);
}

void UI_UpdataProvience(CComboBox &_cbo, int _iProvience)
{
	InitCityMap();

	map<int, ProvienceStr>::iterator it = s_mapProvience.find(_iProvience);
	if (s_mapProvience.end() == it)
	{
		return;
	}
	if (it->second.iSel < _cbo.GetCount())
	{
		_cbo.SetCurSel(it->second.iSel);
	}
}

void UI_UpdataCitys(CComboBox &_cbo, int _iCitys)
{
	for (int i = 0; i < _cbo.GetCount(); ++i)
	{
		if (_iCitys == _cbo.GetItemData(i))
		{
			_cbo.SetCurSel(i);
			break;
		}
	}
}

CString GetPlaceStr(int _iPlace)
{
	InitCityMap();

	CString cstr;
	int iProvience = HIWORD(_iPlace);
	map<int, ProvienceStr>::iterator it = s_mapProvience.find(iProvience);
	if (s_mapProvience.end() != it)
	{
		cstr = it->second.cstrProvience;
		map<int, CString>::iterator itCitys = it->second.mapCitys.find(LOWORD(_iPlace));
		if (itCitys != it->second.mapCitys.end())
		{
			cstr += itCitys->second;
		}
	}
	return cstr;
}

void InitCityMap()
{
	if (s_mapProvience.size() > 0) 
	{
		return;
	}

#ifdef ENGLISH_VERSION
	//Unknown
	ProvienceStr tUnKnowEn;
	tUnKnowEn.cstrProvience = "Unknown ";
	tUnKnowEn.mapCitys.insert(pair<int, CString>(0, "Unknown"));
	s_mapProvience.insert(pair<int, ProvienceStr>(0, tUnKnowEn));
	return;
#endif

	//unknown
	ProvienceStr tUnKnow;
	tUnKnow.cstrProvience = "unknown";
	tUnKnow.mapCitys.insert(pair<int, CString>(0, "unknown"));
	s_mapProvience.insert(pair<int, ProvienceStr>(0, tUnKnow));
	//Beijing
	ProvienceStr tBJ;
	tBJ.cstrProvience = "Beijing";
	tBJ.mapCitys.insert(pair<int, CString>(0, "unknown"));
	tBJ.mapCitys.insert(pair<int, CString>(1, "Municipal district"));
	s_mapProvience.insert(pair<int, ProvienceStr>(11, tBJ));
	//Tianjin
	ProvienceStr tTJ;
	tTJ.cstrProvience = "Tianjin";
	tTJ.mapCitys.insert(pair<int, CString>(0, "unknown"));
	tTJ.mapCitys.insert(pair<int, CString>(1, "Municipal district"));
	s_mapProvience.insert(pair<int, ProvienceStr>(12, tTJ));
	//Hebei Province
	ProvienceStr tHE;
	tHE.cstrProvience = "Hebei Province";
	tHE.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHE.mapCitys.insert(pair<int, CString>(1, 	"Shijiazhuang City"));
	tHE.mapCitys.insert(pair<int, CString>(2, 	"Tangshan City"));
	tHE.mapCitys.insert(pair<int, CString>(3, 	"Qinhuangdao City"));
	tHE.mapCitys.insert(pair<int, CString>(4, 	"Handan City"));
	tHE.mapCitys.insert(pair<int, CString>(5, 	"Xingtai City"));
	tHE.mapCitys.insert(pair<int, CString>(6, 	"Baoding City"));
	tHE.mapCitys.insert(pair<int, CString>(7, 	"Zhangjiakou City"));
	tHE.mapCitys.insert(pair<int, CString>(8, 	"Cangzhou City"));
	tHE.mapCitys.insert(pair<int, CString>(9, 	"Langfang City"));
	tHE.mapCitys.insert(pair<int, CString>(10,	"Hengshui City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(13, tHE));
	//Shanxi Province
	ProvienceStr tSX;
	tSX.cstrProvience = "Shanxi Province";
	tSX.mapCitys.insert(pair<int, CString>(0,	"unknown"));
	tSX.mapCitys.insert(pair<int, CString>(1, 	"Taiyuan City"));
	tSX.mapCitys.insert(pair<int, CString>(2, 	"Datong City"));
	tSX.mapCitys.insert(pair<int, CString>(3, 	"Yangquan City"));
	tSX.mapCitys.insert(pair<int, CString>(4, 	"Changzhi City"));
	tSX.mapCitys.insert(pair<int, CString>(5, 	"Jincheng City"));
	tSX.mapCitys.insert(pair<int, CString>(6, 	"Shuozhou City"));
	tSX.mapCitys.insert(pair<int, CString>(7, 	"Jinzhong City"));
	tSX.mapCitys.insert(pair<int, CString>(8, 	"Yuncheng City"));
	tSX.mapCitys.insert(pair<int, CString>(9, 	"Xinzhou City"));
	tSX.mapCitys.insert(pair<int, CString>(10,	"Linfen City"));
	tSX.mapCitys.insert(pair<int, CString>(11,	"Luliang City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(14, tSX));
	//Inner Mongolia Autonomous Region
	ProvienceStr tNM;
	tNM.cstrProvience = "Inner Mongolia Autonomous Region";
	tNM.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tNM.mapCitys.insert(pair<int, CString>(1, 	"Hohhot City"));
	tNM.mapCitys.insert(pair<int, CString>(2, 	"Baotou City"));
	tNM.mapCitys.insert(pair<int, CString>(3, 	"Wuhai City"));
	tNM.mapCitys.insert(pair<int, CString>(4, 	"Chifeng City"));
	tNM.mapCitys.insert(pair<int, CString>(5, 	"Tongliao City"));
	tNM.mapCitys.insert(pair<int, CString>(6, 	"Hulun Buir City"));
	tNM.mapCitys.insert(pair<int, CString>(7, 	"Bayannur City"));
	tNM.mapCitys.insert(pair<int, CString>(8, 	"Ulanqab City"));
	tNM.mapCitys.insert(pair<int, CString>(0x16,	"Xing'an League"));
	tNM.mapCitys.insert(pair<int, CString>(0x19,	"Xilingol League"));
	tNM.mapCitys.insert(pair<int, CString>(0x1D,	"Alashan League"));
	s_mapProvience.insert(pair<int, ProvienceStr>(15, tNM));
	//Liaoning Province
	ProvienceStr tLN;
	tLN.cstrProvience = "Liaoning Province";
	tLN.mapCitys.insert(pair<int, CString>(0,	"unknown"));
	tLN.mapCitys.insert(pair<int, CString>(1, 	"Shenyang "));
	tLN.mapCitys.insert(pair<int, CString>(2, 	"Dalian "));
	tLN.mapCitys.insert(pair<int, CString>(3, 	"Anshan City"));
	tLN.mapCitys.insert(pair<int, CString>(4, 	"Fushun City"));
	tLN.mapCitys.insert(pair<int, CString>(5, 	"Benxi City"));
	tLN.mapCitys.insert(pair<int, CString>(6, 	"Dandong City"));
	tLN.mapCitys.insert(pair<int, CString>(7, 	"Jinzhou City"));
	tLN.mapCitys.insert(pair<int, CString>(8, 	"Yingkou City"));
	tLN.mapCitys.insert(pair<int, CString>(9, 	"Fuxin City"));
	tLN.mapCitys.insert(pair<int, CString>(10, 	"Liaoyang City"));
	tLN.mapCitys.insert(pair<int, CString>(11, 	"Panjin City"));
	tLN.mapCitys.insert(pair<int, CString>(12, 	"Tieling City"));
	tLN.mapCitys.insert(pair<int, CString>(13, 	"Chaoyang City"));
	tLN.mapCitys.insert(pair<int, CString>(14, 	"Huludao City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(21, tLN));
	//Jilin Province
	ProvienceStr tJL;
	tJL.cstrProvience = "Jilin Province";
	tJL.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tLN.mapCitys.insert(pair<int, CString>(1, 	"Changchun City"));
	tLN.mapCitys.insert(pair<int, CString>(2, 	"Jilin City"));
	tLN.mapCitys.insert(pair<int, CString>(3, 	"Siping City"));
	tLN.mapCitys.insert(pair<int, CString>(4, 	"Liaoyuan City"));
	tLN.mapCitys.insert(pair<int, CString>(5, 	"Tonghua City"));
	tLN.mapCitys.insert(pair<int, CString>(6, 	"Baishan City"));
	tLN.mapCitys.insert(pair<int, CString>(7, 	"Songyuan City"));
	tLN.mapCitys.insert(pair<int, CString>(8, 	"Baicheng City"));
	tLN.mapCitys.insert(pair<int, CString>(9, 	"Yanbian Korean Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(22, tJL));
	//Heilongjiang Province
	ProvienceStr tHL;
	tHL.cstrProvience = "Heilongjiang Province";
	tHL.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHL.mapCitys.insert(pair<int, CString>(1, 	"Harbin"));
	tHL.mapCitys.insert(pair<int, CString>(2, 	"Qiqihar"));
	tHL.mapCitys.insert(pair<int, CString>(3, 	"Jixi City"));
	tHL.mapCitys.insert(pair<int, CString>(4, 	"Hegang City"));
	tHL.mapCitys.insert(pair<int, CString>(5, 	"Shuangyashan"));
	tHL.mapCitys.insert(pair<int, CString>(6, 	"Daqing City"));
	tHL.mapCitys.insert(pair<int, CString>(7, 	"Yichun City"));
	tHL.mapCitys.insert(pair<int, CString>(8, 	"Jiamusi City"));
	tHL.mapCitys.insert(pair<int, CString>(9, 	"Qitaihe City"));
	tHL.mapCitys.insert(pair<int, CString>(10, 	"Mudanjiang City"));
	tHL.mapCitys.insert(pair<int, CString>(11, 	"Heihe City"));
	tHL.mapCitys.insert(pair<int, CString>(12, 	"Suihua City"));
	tHL.mapCitys.insert(pair<int, CString>(0x1B, "Greater Khingan Region"));
	s_mapProvience.insert(pair<int, ProvienceStr>(23, tHL));

	//Shanghai
	ProvienceStr tSH;
	tSH.cstrProvience = "Shanghai";
	tSH.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tSH.mapCitys.insert(pair<int, CString>(1, 	"Municipal district"));
	s_mapProvience.insert(pair<int, ProvienceStr>(31, tSH));
	//Jiangsu Province
	ProvienceStr tJS;
	tJS.cstrProvience = "Jiangsu Province";
	tJS.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tJS.mapCitys.insert(pair<int, CString>(1, 	"Nanjing City"));
	tJS.mapCitys.insert(pair<int, CString>(2, 	"Wuxi City"));
	tJS.mapCitys.insert(pair<int, CString>(3, 	"Xuzhou City"));
	tJS.mapCitys.insert(pair<int, CString>(4, 	"Changzhou City"));
	tJS.mapCitys.insert(pair<int, CString>(5, 	"Suzhou City"));
	tJS.mapCitys.insert(pair<int, CString>(6, 	"Nantong City"));
	tJS.mapCitys.insert(pair<int, CString>(7, 	"Lianyungang City"));
	tJS.mapCitys.insert(pair<int, CString>(8, 	"Huai'an City"));
	tJS.mapCitys.insert(pair<int, CString>(9, 	"Yancheng City"));
	tJS.mapCitys.insert(pair<int, CString>(10, 	"Yangzhou City"));
	tJS.mapCitys.insert(pair<int, CString>(11, 	"Zhenjiang City"));
	tJS.mapCitys.insert(pair<int, CString>(12, 	"Taizhou City"));
	tJS.mapCitys.insert(pair<int, CString>(13, 	"Suqian City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(32, tJS));
	//Zhejiang Province
	ProvienceStr tZJ;
	tZJ.cstrProvience = "Zhejiang Province";
	tZJ.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tZJ.mapCitys.insert(pair<int, CString>(1, 	"Hangzhou City"));
	tZJ.mapCitys.insert(pair<int, CString>(2, 	"Ningbo City"));
	tZJ.mapCitys.insert(pair<int, CString>(3, 	"Wenzhou City"));
	tZJ.mapCitys.insert(pair<int, CString>(4, 	"Jiaxing City"));
	tZJ.mapCitys.insert(pair<int, CString>(5, 	"Huzhou City"));
	tZJ.mapCitys.insert(pair<int, CString>(6, 	"Jinhua City"));
	tZJ.mapCitys.insert(pair<int, CString>(7, 	"Quzhou City"));
	tZJ.mapCitys.insert(pair<int, CString>(8, 	"Zhoushan City"));
	tZJ.mapCitys.insert(pair<int, CString>(9, 	"Taizhou City"));
	tZJ.mapCitys.insert(pair<int, CString>(10,	"Lishui City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(33, tZJ));
	//Anhui Province
	ProvienceStr tAH;
	tAH.cstrProvience = "Anhui Province";
	tAH.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tAH.mapCitys.insert(pair<int, CString>(1, 	"Hefei"));
	tAH.mapCitys.insert(pair<int, CString>(2, 	"Wuhu City"));
	tAH.mapCitys.insert(pair<int, CString>(3, 	"Bengbu City"));
	tAH.mapCitys.insert(pair<int, CString>(4, 	"Huainan City"));
	tAH.mapCitys.insert(pair<int, CString>(5, 	"Ma'anshan City"));
	tAH.mapCitys.insert(pair<int, CString>(6, 	"Huaibei City"));
	tAH.mapCitys.insert(pair<int, CString>(7, 	"Tongling City"));
	tAH.mapCitys.insert(pair<int, CString>(8, 	"Anqing City"));
	tAH.mapCitys.insert(pair<int, CString>(9, 	"Huangshan City"));
	tAH.mapCitys.insert(pair<int, CString>(10, 	"Chuzhou City"));
	tAH.mapCitys.insert(pair<int, CString>(11, 	"Fuyang City"));
	tAH.mapCitys.insert(pair<int, CString>(12, 	"Suzhou City"));
	tAH.mapCitys.insert(pair<int, CString>(13, 	"Chaohu City"));
	tAH.mapCitys.insert(pair<int, CString>(14, 	"Lu'an City"));
	tAH.mapCitys.insert(pair<int, CString>(15, 	"Bozhou City"));
	tAH.mapCitys.insert(pair<int, CString>(16, 	"Chizhou City"));
	tAH.mapCitys.insert(pair<int, CString>(17, 	"Xuancheng City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(34, tAH));
	//Fujian Province
	ProvienceStr tFJ;
	tFJ.cstrProvience = "Fujian Province";
	tFJ.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tFJ.mapCitys.insert(pair<int, CString>(1, 	"Fuzhou City"));
	tFJ.mapCitys.insert(pair<int, CString>(2, 	"Xiamen City"));
	tFJ.mapCitys.insert(pair<int, CString>(3, 	"Putian City"));
	tFJ.mapCitys.insert(pair<int, CString>(4, 	"Sanming City"));
	tFJ.mapCitys.insert(pair<int, CString>(5, 	"Quanzhou City"));
	tFJ.mapCitys.insert(pair<int, CString>(6, 	"Zhangzhou City"));
	tFJ.mapCitys.insert(pair<int, CString>(7, 	"Nanping City"));
	tFJ.mapCitys.insert(pair<int, CString>(8, 	"Longyan City"));
	tFJ.mapCitys.insert(pair<int, CString>(9, 	"Ningde City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(35, tFJ));
	//Jiangxi Province
	ProvienceStr tJX;
	tJX.cstrProvience = "Jiangxi Province";
	tJX.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tJX.mapCitys.insert(pair<int, CString>(1, 	"Nanchang City"));
	tJX.mapCitys.insert(pair<int, CString>(2, 	"Jingdezhen City"));
	tJX.mapCitys.insert(pair<int, CString>(3, 	"Jiujiang City"));
	tJX.mapCitys.insert(pair<int, CString>(4, 	"Xinyu City"));
	tJX.mapCitys.insert(pair<int, CString>(5, 	"Ganzhou City"));
	tJX.mapCitys.insert(pair<int, CString>(6, 	"Ji'an City"));
	tJX.mapCitys.insert(pair<int, CString>(7, 	"Yichun City"));
	tJX.mapCitys.insert(pair<int, CString>(8, 	"Fuzhou City"));
	tJX.mapCitys.insert(pair<int, CString>(9, 	"Shangrao City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(36, tJX));
	//Shandong Province
	ProvienceStr tSD;
	tSD.cstrProvience = "Shandong Province";
	tSD.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tSD.mapCitys.insert(pair<int, CString>(1, 	"Jinan City"));
	tSD.mapCitys.insert(pair<int, CString>(2, 	"Qingdao City"));
	tSD.mapCitys.insert(pair<int, CString>(3, 	"Zibo City"));
	tSD.mapCitys.insert(pair<int, CString>(4, 	"Zaozhuang City"));
	tSD.mapCitys.insert(pair<int, CString>(5, 	"Dongying City"));
	tSD.mapCitys.insert(pair<int, CString>(6, 	"Yantai City"));
	tSD.mapCitys.insert(pair<int, CString>(7, 	"Weifang City"));
	tSD.mapCitys.insert(pair<int, CString>(8, 	"Jining City"));
	tSD.mapCitys.insert(pair<int, CString>(9, 	"Tai'an City"));
	tSD.mapCitys.insert(pair<int, CString>(10, 	"Weihai City"));
	tSD.mapCitys.insert(pair<int, CString>(11, 	"Rizhao City"));
	tSD.mapCitys.insert(pair<int, CString>(12, 	"Laiwu City"));
	tSD.mapCitys.insert(pair<int, CString>(13, 	"Dezhou City"));
	tSD.mapCitys.insert(pair<int, CString>(14, 	"Liaocheng City"));
	tSD.mapCitys.insert(pair<int, CString>(15, 	"Binzhou City"));
	tSD.mapCitys.insert(pair<int, CString>(16, 	"Heze City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(37, tSD));
	//Henan Province
	ProvienceStr tHA;
	tHA.cstrProvience = "Henan Province";
	tHA.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHA.mapCitys.insert(pair<int, CString>(1, 	"Zhengzhou City"));
	tHA.mapCitys.insert(pair<int, CString>(2, 	"Kaifeng City"));
	tHA.mapCitys.insert(pair<int, CString>(3, 	"Luoyang City"));
	tHA.mapCitys.insert(pair<int, CString>(4, 	"Pingdingshan City"));
	tHA.mapCitys.insert(pair<int, CString>(5, 	"Anyang City"));
	tHA.mapCitys.insert(pair<int, CString>(6, 	"Hebi City"));
	tHA.mapCitys.insert(pair<int, CString>(7, 	"Xinxiang City"));
	tHA.mapCitys.insert(pair<int, CString>(8, 	"Jiaozuo City"));
	tHA.mapCitys.insert(pair<int, CString>(9, 	"Puyang City"));
	tHA.mapCitys.insert(pair<int, CString>(10, 	"Xuchang City"));
	tHA.mapCitys.insert(pair<int, CString>(11, 	"Luohe City"));
	tHA.mapCitys.insert(pair<int, CString>(12, 	"Sanmenxia City"));
	tHA.mapCitys.insert(pair<int, CString>(13, 	"Nanyang City"));
	tHA.mapCitys.insert(pair<int, CString>(14, 	"Shangqiu City"));
	tHA.mapCitys.insert(pair<int, CString>(15, 	"Xinyang City"));
	tHA.mapCitys.insert(pair<int, CString>(16, 	"Zhoukou City"));
	tHA.mapCitys.insert(pair<int, CString>(17, 	"Zhumadian City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(41, tHA));
	//Hubei province
	ProvienceStr tHB;
	tHB.cstrProvience = "Hubei province";
	tHB.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHB.mapCitys.insert(pair<int, CString>(1, 	"Wuhan City"));
	tHB.mapCitys.insert(pair<int, CString>(2, 	"Huangshi City"));
	tHB.mapCitys.insert(pair<int, CString>(3, 	"Shiyan City"));
	tHB.mapCitys.insert(pair<int, CString>(4, 	"Yichang City"));
	tHB.mapCitys.insert(pair<int, CString>(5, 	"Xiangyang City"));
	tHB.mapCitys.insert(pair<int, CString>(6, 	"Ezhou City"));
	tHB.mapCitys.insert(pair<int, CString>(7, 	"Jingmen City"));
	tHB.mapCitys.insert(pair<int, CString>(8, 	"Xiaogan City"));
	tHB.mapCitys.insert(pair<int, CString>(9, 	"Jingzhou City"));
	tHB.mapCitys.insert(pair<int, CString>(10, 	"Huanggang City"));
	tHB.mapCitys.insert(pair<int, CString>(11, 	"Xianning City"));
	tHB.mapCitys.insert(pair<int, CString>(12, 	"Suizhou City"));
	tHB.mapCitys.insert(pair<int, CString>(13, 	"Enshi City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(42, tHB));
	//Hunan Province
	ProvienceStr tHN;
	tHN.cstrProvience = "Hunan Province";
	tHN.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHN.mapCitys.insert(pair<int, CString>(1, 	"Changsha City"));
	tHN.mapCitys.insert(pair<int, CString>(2, 	"Zhuzhou City"));
	tHN.mapCitys.insert(pair<int, CString>(3, 	"Xiangtan City"));
	tHN.mapCitys.insert(pair<int, CString>(4, 	"Hengyang City"));
	tHN.mapCitys.insert(pair<int, CString>(5, 	"Shaoyang City"));
	tHN.mapCitys.insert(pair<int, CString>(6, 	"Yueyang City"));
	tHN.mapCitys.insert(pair<int, CString>(7, 	"Changde City"));
	tHN.mapCitys.insert(pair<int, CString>(8, 	"Zhangjiajie City"));
	tHN.mapCitys.insert(pair<int, CString>(9,	"Yiyang City"));
	tHN.mapCitys.insert(pair<int, CString>(10, 	"Chenzhou City"));
	tHN.mapCitys.insert(pair<int, CString>(11, 	"Yongzhou City"));
	tHN.mapCitys.insert(pair<int, CString>(12, 	"Huaihua City"));
	tHN.mapCitys.insert(pair<int, CString>(13, 	"Loudi City"));
	tHN.mapCitys.insert(pair<int, CString>(14, 	"Xiangxi Tujia and Miao Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(43, tHN));
	//Guangdong Province
	ProvienceStr tGD;
	tGD.cstrProvience = "Guangdong Province";
	tGD.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tGD.mapCitys.insert(pair<int, CString>(1, 	"Guangzhou City"));
	tGD.mapCitys.insert(pair<int, CString>(2, 	"Shaoguan City"));
	tGD.mapCitys.insert(pair<int, CString>(3, 	"Shenzhou City"));
	tGD.mapCitys.insert(pair<int, CString>(4, 	"Zhuhai City"));
	tGD.mapCitys.insert(pair<int, CString>(5, 	"Shantou City"));
	tGD.mapCitys.insert(pair<int, CString>(6, 	"Fengshan City"));
	tGD.mapCitys.insert(pair<int, CString>(7, 	"Jiangmen City"));
	tGD.mapCitys.insert(pair<int, CString>(8, 	"Zhanjiang City"));
	tGD.mapCitys.insert(pair<int, CString>(9, 	"Maoming City"));
	tGD.mapCitys.insert(pair<int, CString>(10, 	"Zhaoqing City"));
	tGD.mapCitys.insert(pair<int, CString>(11, 	"Huizhou City"));
	tGD.mapCitys.insert(pair<int, CString>(12, 	"Meizhou City"));
	tGD.mapCitys.insert(pair<int, CString>(13, 	"Shanwei City"));
	tGD.mapCitys.insert(pair<int, CString>(14, 	"Heyuan City"));
	tGD.mapCitys.insert(pair<int, CString>(15, 	"Yangjiang City"));
	tGD.mapCitys.insert(pair<int, CString>(16, 	"Qingyuan City"));
	tGD.mapCitys.insert(pair<int, CString>(17, 	"Dongguan City"));
	tGD.mapCitys.insert(pair<int, CString>(18, 	"Zhoushan City"));
	tGD.mapCitys.insert(pair<int, CString>(19, 	"Chaozhou City"));
	tGD.mapCitys.insert(pair<int, CString>(20, 	"Yunfu City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(44, tGD));
	//Guangxi Zhuang Autonomous Region
	ProvienceStr tGX;
	tGX.cstrProvience = "Guangxi Zhuang Autonomous Region";
	tGX.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tGX.mapCitys.insert(pair<int, CString>(1, 	"Nanning City"));
	tGX.mapCitys.insert(pair<int, CString>(2, 	"Liuzhou City"));
	tGX.mapCitys.insert(pair<int, CString>(3, 	"Guilin City"));
	tGX.mapCitys.insert(pair<int, CString>(4, 	"Wuzhou City"));
	tGX.mapCitys.insert(pair<int, CString>(5, 	"Beihai City"));
	tGX.mapCitys.insert(pair<int, CString>(6, 	"Fangchenggang City"));
	tGX.mapCitys.insert(pair<int, CString>(7, 	"Qinzhou City"));
	tGX.mapCitys.insert(pair<int, CString>(8, 	"Guigang City"));
	tGX.mapCitys.insert(pair<int, CString>(9, 	"Yulin City"));
	tGX.mapCitys.insert(pair<int, CString>(10, 	"Baise City"));
	tGX.mapCitys.insert(pair<int, CString>(11, 	"Hezhou City"));
	tGX.mapCitys.insert(pair<int, CString>(12, 	"Hechi City"));
	tGX.mapCitys.insert(pair<int, CString>(13, 	"Laibin City"));
	tGX.mapCitys.insert(pair<int, CString>(14, 	"Chongzuo City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(45, tGX));
	//Hainan
	ProvienceStr tHI;
	tHI.cstrProvience = "Hainan";
	tHI.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHI.mapCitys.insert(pair<int, CString>(1, 	"Haikou City"));
	tHI.mapCitys.insert(pair<int, CString>(2, 	"The city of Sanya"));
	s_mapProvience.insert(pair<int, ProvienceStr>(46, tHI));
	//Chongqing City
	ProvienceStr tCQ;
	tCQ.cstrProvience = "Chongqing City";
	tCQ.mapCitys.insert(pair<int, CString>(0,	"unknown"));
	tCQ.mapCitys.insert(pair<int, CString>(1,	"Municipal district"));
	s_mapProvience.insert(pair<int, ProvienceStr>(50, tCQ));
	//Sichuan Province
	ProvienceStr tSC;
	tSC.cstrProvience = "Sichuan Province";
	tSC.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tSC.mapCitys.insert(pair<int, CString>(1, 	"Chengdu City"));
	tSC.mapCitys.insert(pair<int, CString>(2, 	"Zigong City"));
	tSC.mapCitys.insert(pair<int, CString>(3, 	"Panzhihua City"));
	tSC.mapCitys.insert(pair<int, CString>(4, 	"Luzhou City"));
	tSC.mapCitys.insert(pair<int, CString>(5, 	"Deyang City"));
	tSC.mapCitys.insert(pair<int, CString>(6, 	"Mianyang City"));
	tSC.mapCitys.insert(pair<int, CString>(7, 	"Guangyuan City"));
	tSC.mapCitys.insert(pair<int, CString>(8, 	"Suining City"));
	tSC.mapCitys.insert(pair<int, CString>(9, 	"Neijiang City"));
	tSC.mapCitys.insert(pair<int, CString>(10, 	"Leshan City"));
	tSC.mapCitys.insert(pair<int, CString>(11, 	"Nanchong City"));
	tSC.mapCitys.insert(pair<int, CString>(12, 	"Meishan City"));
	tSC.mapCitys.insert(pair<int, CString>(13, 	"Yibin City"));
	tSC.mapCitys.insert(pair<int, CString>(14, 	"Guang'an City"));
	tSC.mapCitys.insert(pair<int, CString>(15, 	"Dazhou City"));
	tSC.mapCitys.insert(pair<int, CString>(16, 	"Ya'an City"));
	tSC.mapCitys.insert(pair<int, CString>(17, 	"Bazhong City"));
	tSC.mapCitys.insert(pair<int, CString>(18, 	"Ziyang City"));
	tSC.mapCitys.insert(pair<int, CString>(19, 	"Aba Tibetan and Qiang Autonomous Prefecture"));
	tSC.mapCitys.insert(pair<int, CString>(20, 	"Ganzi Tibetan Autonomous Prefecture"));
	tSC.mapCitys.insert(pair<int, CString>(20, 	"Liangshan Yi Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(51, tSC));
	//Guizhou Province
	ProvienceStr tGZ;
	tGZ.cstrProvience = "Guizhou Province";
	tGZ.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tGZ.mapCitys.insert(pair<int, CString>(1, 	"Guiyang City"));
	tGZ.mapCitys.insert(pair<int, CString>(2, 	"Liupanshui City"));
	tGZ.mapCitys.insert(pair<int, CString>(3, 	"Zunyi City"));
	tGZ.mapCitys.insert(pair<int, CString>(4, 	"Anshun City"));
	tGZ.mapCitys.insert(pair<int, CString>(5, 	"Tongren District"));
	tGZ.mapCitys.insert(pair<int, CString>(6, 	"Bijie District"));
	tGZ.mapCitys.insert(pair<int, CString>(7, 	"Qiandongnan Miao and Dong Autonomous Prefecture"));
	tGZ.mapCitys.insert(pair<int, CString>(8, 	"Qiannan Buyei and Miao Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(52, tGZ));
	//Yunnan Province
	ProvienceStr tYN;
	tYN.cstrProvience = "Yunnan Province";
	tYN.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tYN.mapCitys.insert(pair<int, CString>(1, 	"Kunming City"));
	tYN.mapCitys.insert(pair<int, CString>(3, 	"Qujing City"));
	tYN.mapCitys.insert(pair<int, CString>(4, 	"Yuxi City"));
	tYN.mapCitys.insert(pair<int, CString>(5, 	"Baoshan City"));
	tYN.mapCitys.insert(pair<int, CString>(6, 	"Mountain simultaneous"));
	tYN.mapCitys.insert(pair<int, CString>(7, 	"Lijiang City"));
	tYN.mapCitys.insert(pair<int, CString>(8, 	"Pu'er City"));
	tYN.mapCitys.insert(pair<int, CString>(9, 	"Lincang City"));
	tYN.mapCitys.insert(pair<int, CString>(0x17,"Chuxiong Yi Autonomous Prefecture"));
	tYN.mapCitys.insert(pair<int, CString>(0x19,"Honghe Hani and Yi Autonomous Prefecture"));
	tYN.mapCitys.insert(pair<int, CString>(0x1C,"Xishuangbanna Prefecture"));
	tYN.mapCitys.insert(pair<int, CString>(0x1D,"Dali Bai Autonomous Prefecture"));
	tYN.mapCitys.insert(pair<int, CString>(0x1F,"Dehong Dai and Jingpo Autonomous Prefecture"));
	tYN.mapCitys.insert(pair<int, CString>(0x21,"Nujiang Lisu Autonomous Prefecture"));
	tYN.mapCitys.insert(pair<int, CString>(0x32,"Diqing Tibetan Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(53, tYN));
	//Tibet Autonomous Region
	ProvienceStr tXZ;
	tXZ.cstrProvience = "Tibet Autonomous Region";
	tXZ.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tXZ.mapCitys.insert(pair<int, CString>(1, 	"Lhasa City"));
	tXZ.mapCitys.insert(pair<int, CString>(0x15,"Changdu District"));
	tXZ.mapCitys.insert(pair<int, CString>(0x16,"Nanshan area"));
	tXZ.mapCitys.insert(pair<int, CString>(0x17,"Xigaze Region"));
	tXZ.mapCitys.insert(pair<int, CString>(0x18,"Naqu Area"));
	tXZ.mapCitys.insert(pair<int, CString>(0x19,"Ali region"));
	tXZ.mapCitys.insert(pair<int, CString>(0x1A,"Linzhi District"));
	s_mapProvience.insert(pair<int, ProvienceStr>(54, tXZ));
	//Shaanxi Province
	ProvienceStr tSN;
	tSN.cstrProvience = "Shaanxi Province";
	tSN.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tSN.mapCitys.insert(pair<int, CString>(1, 	"Xi'an City"));
	tSN.mapCitys.insert(pair<int, CString>(2, 	"Tongchuan City"));;
	tSN.mapCitys.insert(pair<int, CString>(3, 	"Baoji City"));
	tSN.mapCitys.insert(pair<int, CString>(4, 	"Xianyang City"));
	tSN.mapCitys.insert(pair<int, CString>(5, 	"Weinan City"));
	tSN.mapCitys.insert(pair<int, CString>(6, 	"Yan'an City"));
	tSN.mapCitys.insert(pair<int, CString>(7, 	"Hanzhong City"));
	tSN.mapCitys.insert(pair<int, CString>(8, 	"Yulin City"));
	tSN.mapCitys.insert(pair<int, CString>(9, 	"Ankang City"));
	tSN.mapCitys.insert(pair<int, CString>(10,	"Shangluo City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(61, tSN));
	//Gansu Province
	ProvienceStr tGS;
	tGS.cstrProvience = "Gansu Province";
	tGS.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tGS.mapCitys.insert(pair<int, CString>(1, 	"Lanzhou City"));
	tGS.mapCitys.insert(pair<int, CString>(2, 	"Jiayuguan City"));
	tGS.mapCitys.insert(pair<int, CString>(3, 	"Jinchang City"));
	tGS.mapCitys.insert(pair<int, CString>(4, 	"Silver City"));
	tGS.mapCitys.insert(pair<int, CString>(5, 	"Tianshui City"));
	tGS.mapCitys.insert(pair<int, CString>(6, 	"Wuwei City"));
	tGS.mapCitys.insert(pair<int, CString>(7, 	"Zhangye City"));
	tGS.mapCitys.insert(pair<int, CString>(8, 	"Pingliang City"));
	tGS.mapCitys.insert(pair<int, CString>(9, 	"Jiuquan City"));
	tGS.mapCitys.insert(pair<int, CString>(10, 	"Qingyang City"));
	tGS.mapCitys.insert(pair<int, CString>(11, 	"Dingxi City"));
	tGS.mapCitys.insert(pair<int, CString>(12, 	"Longnan City"));
	tGS.mapCitys.insert(pair<int, CString>(13, 	"Linxia Hui Autonomous Prefecture"));
	tGS.mapCitys.insert(pair<int, CString>(14, 	"Gannan Tibetan Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(62, tGS));
	//Qinghai Province
	ProvienceStr tQH;
	tQH.cstrProvience = "Qinghai Province";
	tQH.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tQH.mapCitys.insert(pair<int, CString>(1, 	"Xining City"));
	tQH.mapCitys.insert(pair<int, CString>(0x15,"Haidong Area"));
	tQH.mapCitys.insert(pair<int, CString>(0x16,"Haibei Tibetan Autonomous Prefecture"));
	tQH.mapCitys.insert(pair<int, CString>(0x17,"Huangnan Tibetan Autonomous Prefecture"));
	tQH.mapCitys.insert(pair<int, CString>(0x19,"Hainan Tibetan Autonomous Prefecture"));
	tQH.mapCitys.insert(pair<int, CString>(0x1A,"Golog Tibetan Autonomous Prefecture"));
	tQH.mapCitys.insert(pair<int, CString>(0x1B,"Yushu Tibetan Autonomous Prefecture"));
	tQH.mapCitys.insert(pair<int, CString>(0x1C,"Haixi Mongolian and Tibetan Autonomous Prefecture"));
	s_mapProvience.insert(pair<int, ProvienceStr>(63, tQH));
	//Ningxia Hui Autonomous Region
	ProvienceStr tNX;
	tNX.cstrProvience = "Ningxia Hui Autonomous Region";
	tNX.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tNX.mapCitys.insert(pair<int, CString>(1, 	"Yinchuan City"));
	tNX.mapCitys.insert(pair<int, CString>(2, 	"Shizuishan City"));
	tNX.mapCitys.insert(pair<int, CString>(3, 	"Wuzhong City"));
	tNX.mapCitys.insert(pair<int, CString>(4, 	"Guyuan City"));
	tNX.mapCitys.insert(pair<int, CString>(5, 	"Zhongwei City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(64, tNX));
	//Xinjiang Uygur Autonomous Region
	ProvienceStr tXJ;
	tXJ.cstrProvience = "Xinjiang Uygur Autonomous Region";
	tXJ.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tXJ.mapCitys.insert(pair<int, CString>(1, 	"Urumqi City"));
	tXJ.mapCitys.insert(pair<int, CString>(2, 	"Karamay City"));
	tXJ.mapCitys.insert(pair<int, CString>(0x17,"Changji Hui Autonomous Prefecture"));
	tXJ.mapCitys.insert(pair<int, CString>(0x1B,"Bortala Mongol Autonomous Prefecture"));
	tXJ.mapCitys.insert(pair<int, CString>(0x1C,"Bayingolin Mongolian Autonomous Prefecture"));
	tXJ.mapCitys.insert(pair<int, CString>(0x1D,"Aksu Region"));
	tXJ.mapCitys.insert(pair<int, CString>(0x1E,"Kizilsu region"));
	tXJ.mapCitys.insert(pair<int, CString>(0x1F,"Kashgar region"));
	tXJ.mapCitys.insert(pair<int, CString>(0x20,"Hotan Region"));
	tXJ.mapCitys.insert(pair<int, CString>(0x28,"Ili region"));
	tXJ.mapCitys.insert(pair<int, CString>(0x2A,"Tacheng District"));
	tXJ.mapCitys.insert(pair<int, CString>(0x2B,"Altay Region"));
	tXJ.mapCitys.insert(pair<int, CString>(0x5A,"administrative divisions of autonomous regions and counties directly under the Central Government"));
	s_mapProvience.insert(pair<int, ProvienceStr>(65, tXJ));
	//Taiwan Province
	ProvienceStr tTW;
	tTW.cstrProvience = "Taiwan Province";
	tTW.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tTW.mapCitys.insert(pair<int, CString>(1, 	"Taipei City"));
	tTW.mapCitys.insert(pair<int, CString>(2, 	"Xinbei City"));
	tTW.mapCitys.insert(pair<int, CString>(3, 	"Taoyuan City"));
	tTW.mapCitys.insert(pair<int, CString>(4, 	"Taichung City"));
	tTW.mapCitys.insert(pair<int, CString>(5, 	"Tainan City"));
	tTW.mapCitys.insert(pair<int, CString>(6, 	"Kaohsiung City"));
	s_mapProvience.insert(pair<int, ProvienceStr>(71, tTW));
	//Hong Kong Special Administrative Region
	ProvienceStr tHK;
	tHK.cstrProvience = "Hong Kong Special Administrative Region";
	tHK.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tHK.mapCitys.insert(pair<int, CString>(1, 	"Hong Kong Island"));
	tHK.mapCitys.insert(pair<int, CString>(2, 	"Kowloon Peninsula"));
	tHK.mapCitys.insert(pair<int, CString>(3, 	"New Territories"));
	s_mapProvience.insert(pair<int, ProvienceStr>(81, tHK));
	//Macao Special Administrative Region
	ProvienceStr tMO;
	tMO.cstrProvience = "Macao Special Administrative Region";
	tMO.mapCitys.insert(pair<int, CString>(0, 	"unknown"));
	tMO.mapCitys.insert(pair<int, CString>(1, 	"Macau Peninsula"));
	tMO.mapCitys.insert(pair<int, CString>(2, 	"Outlying islands"));
	s_mapProvience.insert(pair<int, ProvienceStr>(82, tMO));
}

void InitCountryMap()
{
	if (s_mapCountry.size() > 0) 
	{
		return;
	}
	s_mapCountry.insert(pair<int, CString>(0, 		"Unknown"));
	s_mapCountry.insert(pair<int, CString>(4, 		"Afghanistan"));
	s_mapCountry.insert(pair<int, CString>(248,		"Aland Islands"));
	s_mapCountry.insert(pair<int, CString>(8, 		"Republic of Albania"));
	s_mapCountry.insert(pair<int, CString>(12, 		"Democratic People's Republic of Algeria"));
	s_mapCountry.insert(pair<int, CString>(16, 		"American Samoa"));
	s_mapCountry.insert(pair<int, CString>(20, 		"Principality of Andorra"));
	s_mapCountry.insert(pair<int, CString>(24, 		"Republic of Angola"));
	s_mapCountry.insert(pair<int, CString>(660,		"Anguilla"));
	s_mapCountry.insert(pair<int, CString>(10, 		"Antarctica"));
	s_mapCountry.insert(pair<int, CString>(28, 		"Antigua and Barbuda"));
	s_mapCountry.insert(pair<int, CString>(32,		"Argentine Republic"));
	s_mapCountry.insert(pair<int, CString>(51, 		"Republic of Armenia"));
	s_mapCountry.insert(pair<int, CString>(533,		"Aruba"));
	s_mapCountry.insert(pair<int, CString>(36, 		"Commonwealth of Australia"));
	s_mapCountry.insert(pair<int, CString>(40, 		"Republic of Austria"));
	s_mapCountry.insert(pair<int, CString>(31, 		"Republic of Azerbaijan"));
	s_mapCountry.insert(pair<int, CString>(44, 		"commonwealth of the Bahamas"));
	s_mapCountry.insert(pair<int, CString>(48, 		"state of Bahrain"));
	s_mapCountry.insert(pair<int, CString>(50, 		"People,s Republic of Bangladesh"));
	s_mapCountry.insert(pair<int, CString>(52, 		"Barbados"));
	s_mapCountry.insert(pair<int, CString>(112,		"Republic of Belarus"));
	s_mapCountry.insert(pair<int, CString>(56, 		"Kingdom of Belgium"));
	s_mapCountry.insert(pair<int, CString>(84, 		"Belize"));
	s_mapCountry.insert(pair<int, CString>(204,		"Republic of Benin"));
	s_mapCountry.insert(pair<int, CString>(60,		"Bermuda"));
	s_mapCountry.insert(pair<int, CString>(64, 		"Kingdom of Bhutan"));
	s_mapCountry.insert(pair<int, CString>(68, 		"Republic of Bolivia"));
	s_mapCountry.insert(pair<int, CString>(70, 		"Bosnia and Herzegovina"));
	s_mapCountry.insert(pair<int, CString>(72, 		"Republic of Botswana"));
	s_mapCountry.insert(pair<int, CString>(74,		"Bouvet Island"));
	s_mapCountry.insert(pair<int, CString>(76,		"Federative Republic of Brazil"));
	s_mapCountry.insert(pair<int, CString>(86, 		"British Indian Ocean Territory"));
	s_mapCountry.insert(pair<int, CString>(90, 		"Solomon Islands"));
	s_mapCountry.insert(pair<int, CString>(92,		"British Virgin Islands"));
	s_mapCountry.insert(pair<int, CString>(96, 		"Brunei Darussalam"));
	s_mapCountry.insert(pair<int, CString>(535, 	"Bonaire, Sint Eustatius and Saba"));
	s_mapCountry.insert(pair<int, CString>(100, 	"Republic of Bulgaria"));
	s_mapCountry.insert(pair<int, CString>(854, 	"Burkina Faso"));
	s_mapCountry.insert(pair<int, CString>(108, 	"Republic of Burundi"));
	s_mapCountry.insert(pair<int, CString>(132, 	"Republic of Cabo Verde"));
	s_mapCountry.insert(pair<int, CString>(116, 	"Kingdom of Cambodia"));
	s_mapCountry.insert(pair<int, CString>(120, 	"Republic of Cameroon"));
	s_mapCountry.insert(pair<int, CString>(124, 	"Canada"));
	s_mapCountry.insert(pair<int, CString>(136, 	"Cayman Islands"));
	s_mapCountry.insert(pair<int, CString>(140, 	"Central African Republic"));
	s_mapCountry.insert(pair<int, CString>(148, 	"Republic of Chad"));
	s_mapCountry.insert(pair<int, CString>(152, 	"Republic of Chile"));
	s_mapCountry.insert(pair<int, CString>(156, 	"People's Republic of OurCountry"));
	s_mapCountry.insert(pair<int, CString>(162, 	"Christmas Island"));
	s_mapCountry.insert(pair<int, CString>(166, 	"Cocos (Keeling) Islands "));
	s_mapCountry.insert(pair<int, CString>(170, 	"Republic of Colombia"));
	s_mapCountry.insert(pair<int, CString>(174, 	"Islamic Federal Republic of the Comoros"));
	s_mapCountry.insert(pair<int, CString>(180, 	"Democratic Republic of Congo"));
	s_mapCountry.insert(pair<int, CString>(178, 	"Republic of Congo"));
	s_mapCountry.insert(pair<int, CString>(184, 	"Cook Islands"));
	s_mapCountry.insert(pair<int, CString>(188, 	"Republic of Costa Rica"));
	s_mapCountry.insert(pair<int, CString>(384, 	"Republic of Cote d'Ivoire"));
	s_mapCountry.insert(pair<int, CString>(191, 	"Republic of Croatia"));
	s_mapCountry.insert(pair<int, CString>(192, 	"Republic of Cuba"));
	s_mapCountry.insert(pair<int, CString>(531, 	"CuraCao"));
	s_mapCountry.insert(pair<int, CString>(196, 	"Republic of Cyprus"));
	s_mapCountry.insert(pair<int, CString>(203, 	"Czech Repblic"));
	s_mapCountry.insert(pair<int, CString>(208, 	"Kingdom od Denmark"));
	s_mapCountry.insert(pair<int, CString>(262, 	"Republic of Djibouti"));
	s_mapCountry.insert(pair<int, CString>(212, 	"Commonwealth of Dominica"));
	s_mapCountry.insert(pair<int, CString>(214, 	"Dominican Republic"));
	s_mapCountry.insert(pair<int, CString>(218, 	"Republic of Ecuador"));
	s_mapCountry.insert(pair<int, CString>(818, 	"Arab Republic of Egypt"));
	s_mapCountry.insert(pair<int, CString>(222, 	"Republic of El Salvador"));
	s_mapCountry.insert(pair<int, CString>(226, 	"Republic of Equatorial Guinea"));
	s_mapCountry.insert(pair<int, CString>(232, 	"state of Eritrea"));
	s_mapCountry.insert(pair<int, CString>(233, 	"Republic of Estonia"));
	s_mapCountry.insert(pair<int, CString>(748, 	"Eswatini"));
	s_mapCountry.insert(pair<int, CString>(231, 	"Federal Democratic Republic of Ethiopia"));
	s_mapCountry.insert(pair<int, CString>(238, 	"Malvinas"));
	s_mapCountry.insert(pair<int, CString>(234, 	"Faroe Islands"));
	s_mapCountry.insert(pair<int, CString>(242, 	"Republic of the Fiji Islands"));
	s_mapCountry.insert(pair<int, CString>(246, 	"Republic of Finland"));
	s_mapCountry.insert(pair<int, CString>(250, 	"France Republic"));
	s_mapCountry.insert(pair<int, CString>(254, 	"French Guiana"));
	s_mapCountry.insert(pair<int, CString>(258, 	"French Polynesia"));
	s_mapCountry.insert(pair<int, CString>(260, 	"French Southern Territories"));
	s_mapCountry.insert(pair<int, CString>(266, 	"Gabonese Republic"));
	s_mapCountry.insert(pair<int, CString>(270, 	"Republic of the Gambia"));
	s_mapCountry.insert(pair<int, CString>(268, 	"Georgia"));
	s_mapCountry.insert(pair<int, CString>(276, 	"Federal Republic of Germany"));
	s_mapCountry.insert(pair<int, CString>(288, 	"Republic of Ghana"));
	s_mapCountry.insert(pair<int, CString>(292, 	"Gibraltar"));
	s_mapCountry.insert(pair<int, CString>(300, 	"Hellenic Republic"));
	s_mapCountry.insert(pair<int, CString>(304, 	"Greenland"));
	s_mapCountry.insert(pair<int, CString>(308, 	"Grenada"));
	s_mapCountry.insert(pair<int, CString>(312, 	"Guadeloupe"));
	s_mapCountry.insert(pair<int, CString>(316, 	"Guam"));
	s_mapCountry.insert(pair<int, CString>(320, 	"Republic of Guatemala"));
	s_mapCountry.insert(pair<int, CString>(831, 	"Guernsey"));
	s_mapCountry.insert(pair<int, CString>(324, 	"Republic of Guinea"));
	s_mapCountry.insert(pair<int, CString>(624, 	"Republic of Guinea-Bissau"));
	s_mapCountry.insert(pair<int, CString>(328, 	"Cooperative Republic of Guyana"));
	s_mapCountry.insert(pair<int, CString>(332, 	"Republic of Haiti"));
	s_mapCountry.insert(pair<int, CString>(334, 	"Heard Island and McDonald Islands"));
	s_mapCountry.insert(pair<int, CString>(336, 	"Vatican City State"));
	s_mapCountry.insert(pair<int, CString>(340, 	"Republic of Honduras"));
	s_mapCountry.insert(pair<int, CString>(344, 	"Hong Kong Special Administrative Region of Ch"));
	s_mapCountry.insert(pair<int, CString>(348, 	"Republic of Hungary"));
	s_mapCountry.insert(pair<int, CString>(352, 	"Republic of Iceland"));
	s_mapCountry.insert(pair<int, CString>(356, 	"Republic India"));
	s_mapCountry.insert(pair<int, CString>(360, 	"Republic of Indonesia"));
	s_mapCountry.insert(pair<int, CString>(364, 	"Islamic Republic of Iran"));
	s_mapCountry.insert(pair<int, CString>(368, 	"Republic of Iraq"));
	s_mapCountry.insert(pair<int, CString>(372, 	"Ireland"));
	s_mapCountry.insert(pair<int, CString>(833, 	"Isle of Man"));
	s_mapCountry.insert(pair<int, CString>(376, 	"State of Israel"));
	s_mapCountry.insert(pair<int, CString>(380, 	"Italian Republic"));
	s_mapCountry.insert(pair<int, CString>(388, 	"Jamaica"));
	s_mapCountry.insert(pair<int, CString>(392, 	"Japan"));
	s_mapCountry.insert(pair<int, CString>(832, 	"Jersey"));
	s_mapCountry.insert(pair<int, CString>(400, 	"Hashemite Kingdom of Jordan"));
	s_mapCountry.insert(pair<int, CString>(398, 	"Republic of Kazakhstan"));
	s_mapCountry.insert(pair<int, CString>(404, 	"Republic of Kenya"));
	s_mapCountry.insert(pair<int, CString>(296, 	"Republic of Kiribati"));
	s_mapCountry.insert(pair<int, CString>(408, 	"Democratic People's Republic of Korea"));
	s_mapCountry.insert(pair<int, CString>(410, 	"Republic of Korea"));
	s_mapCountry.insert(pair<int, CString>(414, 	"State of Kuwait"));
	s_mapCountry.insert(pair<int, CString>(417, 	"Kyrgyzstan Republic"));
	s_mapCountry.insert(pair<int, CString>(418, 	"Lao People's Democratic Republic"));
	s_mapCountry.insert(pair<int, CString>(428, 	"Republic of Latvia"));
	s_mapCountry.insert(pair<int, CString>(422, 	"Lebanon Republic"));
	s_mapCountry.insert(pair<int, CString>(426, 	"Kingdom of Lesotho"));
	s_mapCountry.insert(pair<int, CString>(430, 	"Republic of Liberia"));
	s_mapCountry.insert(pair<int, CString>(434, 	"Great Socialist People's Libyan Arab Jamahiriya"));
	s_mapCountry.insert(pair<int, CString>(438, 	"Principality of Liechtenstein"));
	s_mapCountry.insert(pair<int, CString>(440, 	"Republic of Lithuania"));
	s_mapCountry.insert(pair<int, CString>(442, 	"Grand Duchy of Luxembourg"));
	s_mapCountry.insert(pair<int, CString>(446, 	"Macao Special Administrative Region of Ch"));
	s_mapCountry.insert(pair<int, CString>(450, 	"Republic of Madagascar"));
	s_mapCountry.insert(pair<int, CString>(454, 	"Republic of Malawi"));
	s_mapCountry.insert(pair<int, CString>(458, 	"Malaysia"));
	s_mapCountry.insert(pair<int, CString>(462, 	"Republic of Maldives"));
	s_mapCountry.insert(pair<int, CString>(466, 	"Republic of  Mali"));
	s_mapCountry.insert(pair<int, CString>(470, 	"Rrpublic of Malta"));
	s_mapCountry.insert(pair<int, CString>(584, 	"Republic of the Marshall Islands"));
	s_mapCountry.insert(pair<int, CString>(474, 	"Martinique"));
	s_mapCountry.insert(pair<int, CString>(478, 	"Islamic Republic of Mauritania"));
	s_mapCountry.insert(pair<int, CString>(480, 	"Republic of Mauritius"));
	s_mapCountry.insert(pair<int, CString>(175, 	"Mayotte"));
	s_mapCountry.insert(pair<int, CString>(484, 	"United States of Mexico"));
	s_mapCountry.insert(pair<int, CString>(583, 	"Federated States of Micronesia "));
	s_mapCountry.insert(pair<int, CString>(498, 	"Republic of Moldova"));
	s_mapCountry.insert(pair<int, CString>(492, 	"Principality of  Monaco"));
	s_mapCountry.insert(pair<int, CString>(496, 	"Mongolia"));
	s_mapCountry.insert(pair<int, CString>(499, 	"Montenegro"));
	s_mapCountry.insert(pair<int, CString>(500, 	"Montserrat"));
	s_mapCountry.insert(pair<int, CString>(504, 	"Kingdom of Morocco"));
	s_mapCountry.insert(pair<int, CString>(508, 	"Republic of  Mozambique"));
	s_mapCountry.insert(pair<int, CString>(104, 	"Union of Myanmar"));
	s_mapCountry.insert(pair<int, CString>(516, 	"Republic of Namibia"));
	s_mapCountry.insert(pair<int, CString>(520, 	"Republic of Nauru"));
	s_mapCountry.insert(pair<int, CString>(524, 	"Kingdom of  Nepal"));
	s_mapCountry.insert(pair<int, CString>(528, 	"Kingdom of the Netherlands"));
	s_mapCountry.insert(pair<int, CString>(540, 	"New Caledonia"));
	s_mapCountry.insert(pair<int, CString>(554, 	"New Zealand"));
	s_mapCountry.insert(pair<int, CString>(558, 	"Republic of Nicaragua"));
	s_mapCountry.insert(pair<int, CString>(562, 	"Republic of Niger"));
	s_mapCountry.insert(pair<int, CString>(566, 	"Federal Republic of Nigeria"));
	s_mapCountry.insert(pair<int, CString>(570, 	"Niue"));
	s_mapCountry.insert(pair<int, CString>(574, 	"Norfolk Island"));
	s_mapCountry.insert(pair<int, CString>(807, 	"The former Yugoslav Republic of Macedonia"));
	s_mapCountry.insert(pair<int, CString>(580, 	"Commonwealth of the Northern Mariana Islands"));
	s_mapCountry.insert(pair<int, CString>(578, 	"Kingdom of Norway"));
	s_mapCountry.insert(pair<int, CString>(512, 	"Sultanate of Oman"));
	s_mapCountry.insert(pair<int, CString>(586, 	"Islamic Rrpublic of Pakistan"));
	s_mapCountry.insert(pair<int, CString>(585, 	"Republic of Palau"));
	s_mapCountry.insert(pair<int, CString>(275, 	"State of Palestine"));
	s_mapCountry.insert(pair<int, CString>(591, 	"Republic of Panama"));
	s_mapCountry.insert(pair<int, CString>(598, 	"Independent State of Papua New Guinea"));
	s_mapCountry.insert(pair<int, CString>(600, 	"Republic of Paraguay"));
	s_mapCountry.insert(pair<int, CString>(604, 	"Republic of Peru"));
	s_mapCountry.insert(pair<int, CString>(608, 	"Republic of the Philippines"));
	s_mapCountry.insert(pair<int, CString>(612, 	"Pitcairn"));
	s_mapCountry.insert(pair<int, CString>(616, 	"Republic of Poland"));
	s_mapCountry.insert(pair<int, CString>(620, 	"Portuguese Republic"));
	s_mapCountry.insert(pair<int, CString>(630, 	"Puerto Rico"));
	s_mapCountry.insert(pair<int, CString>(634, 	"State of  Qatar"));
	s_mapCountry.insert(pair<int, CString>(638, 	"Reunion"));
	s_mapCountry.insert(pair<int, CString>(642, 	"Romania"));
	s_mapCountry.insert(pair<int, CString>(643, 	"Russian Federation"));
	s_mapCountry.insert(pair<int, CString>(646, 	"Republic of Rwanda"));
	s_mapCountry.insert(pair<int, CString>(652, 	"Saint Barthelemy"));
	s_mapCountry.insert(pair<int, CString>(654, 	"Saint Helena"));
	s_mapCountry.insert(pair<int, CString>(659, 	"Federation of Saint Kitts and Nevis"));
	s_mapCountry.insert(pair<int, CString>(662, 	"Saint Lucia"));
	s_mapCountry.insert(pair<int, CString>(663, 	"Saint Martin"));
	s_mapCountry.insert(pair<int, CString>(666, 	"Saint Pierre and Miquelo"));
	s_mapCountry.insert(pair<int, CString>(670, 	"Saint Vincent and the Grenadines"));
	s_mapCountry.insert(pair<int, CString>(882, 	"Independent State of Samoa"));
	s_mapCountry.insert(pair<int, CString>(674, 	"Republic of San Marino"));
	s_mapCountry.insert(pair<int, CString>(678, 	"Democratic Republic of Sao Tome and Principe"));
	s_mapCountry.insert(pair<int, CString>(682, 	"Kingdom of Saudi Arabia"));
	s_mapCountry.insert(pair<int, CString>(686, 	"Republic of Senegal"));
	s_mapCountry.insert(pair<int, CString>(688, 	"Serbia"));
	s_mapCountry.insert(pair<int, CString>(690, 	"Republic of Seychelles"));
	s_mapCountry.insert(pair<int, CString>(694, 	"Republic of Sierra Leone"));
	s_mapCountry.insert(pair<int, CString>(702, 	"Republic of Singapore"));
	s_mapCountry.insert(pair<int, CString>(534, 	"Sint Maarten"));
	s_mapCountry.insert(pair<int, CString>(703, 	"Slovakia Republic"));
	s_mapCountry.insert(pair<int, CString>(705, 	"Republic of Slovenia"));
	s_mapCountry.insert(pair<int, CString>(706, 	"Somalia Republic"));
	s_mapCountry.insert(pair<int, CString>(710, 	"Republic of South Africa"));
	s_mapCountry.insert(pair<int, CString>(239, 	"South Georgia and the South Sandwich Islands"));
	s_mapCountry.insert(pair<int, CString>(728, 	"South Sudan"));
	s_mapCountry.insert(pair<int, CString>(724, 	"Kingdom of Spain"));
	s_mapCountry.insert(pair<int, CString>(144, 	"Democratic Socialist Republic of Sri Lanka"));
	s_mapCountry.insert(pair<int, CString>(729, 	"Republic of Sudan"));
	s_mapCountry.insert(pair<int, CString>(740, 	"Republic of Suriname"));
	s_mapCountry.insert(pair<int, CString>(744, 	"Svalbard and Jan Mayen"));
	s_mapCountry.insert(pair<int, CString>(752, 	"Kingdom of Sweden"));
	s_mapCountry.insert(pair<int, CString>(756, 	"Swiss Confederation"));
	s_mapCountry.insert(pair<int, CString>(760, 	"Syrian Arab Republic"));
	s_mapCountry.insert(pair<int, CString>(158, 	"Taiwan ,Province of Ch"));
	s_mapCountry.insert(pair<int, CString>(762, 	"Republic of Tajikistan"));
	s_mapCountry.insert(pair<int, CString>(834, 	"United Republic of Tanzania"));
	s_mapCountry.insert(pair<int, CString>(764, 	"Kingdom of Thailand"));
	s_mapCountry.insert(pair<int, CString>(626, 	"East Timor"));
	s_mapCountry.insert(pair<int, CString>(768, 	"Republic of  Togo"));
	s_mapCountry.insert(pair<int, CString>(772, 	"Tokelau"));
	s_mapCountry.insert(pair<int, CString>(776, 	"Kingdom of Tonga"));
	s_mapCountry.insert(pair<int, CString>(780, 	"Republic of Trinidad and Tobago"));
	s_mapCountry.insert(pair<int, CString>(788, 	"Republic of  Tunisia"));
	s_mapCountry.insert(pair<int, CString>(792, 	"Republic of Turkey"));
	s_mapCountry.insert(pair<int, CString>(795, 	"Turkmenistan"));
	s_mapCountry.insert(pair<int, CString>(796, 	"Turks and Caicos Islands"));
	s_mapCountry.insert(pair<int, CString>(798, 	"Tuvalu"));
	s_mapCountry.insert(pair<int, CString>(800, 	"Republic of  Uganda"));
	s_mapCountry.insert(pair<int, CString>(804, 	"Ukraine"));
	s_mapCountry.insert(pair<int, CString>(784, 	"United Arab Emirates"));
	s_mapCountry.insert(pair<int, CString>(826, 	"United Kingdom of Great Britain and Northern Ireland"));
	s_mapCountry.insert(pair<int, CString>(581, 	"United States Minor Outlying Islands"));
	s_mapCountry.insert(pair<int, CString>(840, 	"United States of America"));
	s_mapCountry.insert(pair<int, CString>(858, 	"Oriental Republic of  Uruguay"));
	s_mapCountry.insert(pair<int, CString>(860, 	"Republic of  Uzbekistan"));
	s_mapCountry.insert(pair<int, CString>(548, 	"Republic of  Vanuatu"));
	s_mapCountry.insert(pair<int, CString>(862, 	"Republic of  Venezuela"));
	s_mapCountry.insert(pair<int, CString>(704, 	"Socialist Republic of  Viet Nam"));
	s_mapCountry.insert(pair<int, CString>(850, 	"Virgin Islands of the United States"));
	s_mapCountry.insert(pair<int, CString>(876, 	"Wallis and Futuna"));
	s_mapCountry.insert(pair<int, CString>(732, 	"Western Sahara"));
	s_mapCountry.insert(pair<int, CString>(887, 	"Republic of  Yemen"));
	s_mapCountry.insert(pair<int, CString>(894, 	"Republic of  Zambia"));
	s_mapCountry.insert(pair<int, CString>(716, 	"Republic of  Zimbabwe"));
}