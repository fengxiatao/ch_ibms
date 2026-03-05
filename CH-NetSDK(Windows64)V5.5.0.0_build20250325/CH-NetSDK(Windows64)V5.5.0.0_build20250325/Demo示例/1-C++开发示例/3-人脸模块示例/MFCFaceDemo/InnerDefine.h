
#ifndef _INNER_DEFINE_H_
#define _INNER_DEFINE_H_

#include <map>
#include <list>
#include <vector>
using namespace std;

struct ProvienceStr
{
	CString cstrProvience;	//Provinces, municipalities and special administrative regions
	int iSel;
	map<int, CString> mapCitys;	//city
	ProvienceStr()
	{
		iSel = 0;
	}
};

const CString CONST_CSTR_MODE[] = {"All", "Modeling Success", "Modeling Failed", "Not Modeled"};
const CString CONST_CSTR_SEX[] = {"Unknown", "Male", "Female"};
const CString CONST_CSTR_CARD[] = {"Unknown", "ID Card", "Certificate of officers", "PassPort", "StaffId"};

#ifdef ENGLISH_VERSION 
const CString CONST_CSTR_NATION[] = {"Unknown"};
#else
const CString CONST_CSTR_NATION[] = {"未知",
	"汉族", "蒙古族", "回族", "藏族", "维吾尔族", "苗族", "彝族", "壮族", "布依族", "朝鲜族", 
	"满族", "侗族", "瑶族", "白族", "土家族", "哈尼族", "哈萨克族", "傣族", "黎族", "僳僳族", 
	"佤族", "畲族", "高山族", "拉祜族", "水族", "东乡族", "纳西族", "景颇族", "柯尔克孜族", "土族", 
	"达斡尔族", "仫佬族", "羌族", "布朗族", "撒拉族", "毛难族", "仡佬族", "锡伯族", "阿昌族", "普米族", 
	"塔吉克族", "怒族", "乌孜别克族", "俄罗斯族", "鄂温克族", "崩龙族", "保安族", "裕固族", "京族", "塔塔尔族",
	"独龙族", "鄂伦春族", "赫哲族", "门巴族", "珞巴族", "基诺族", "其他", "外国血统"};
#endif

struct DLFacePicInfo
{
	RECT tRcSnap;
	RECT tRcNeg;
	RECT tRcSimi;
};

#define VCA_SUSPEND_STATUS_PAUSE		0
#define VCA_SUSPEND_STATUS_RESUME		1

#define VCA_SUSPEND_RESULT_SUCCESS		1 
#define VCA_SUSPEND_RESULT_CONFIGING	2

#define RET_FAILED						-1
#define RET_SUCCESS						0

#endif