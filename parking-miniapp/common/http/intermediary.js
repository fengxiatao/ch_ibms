import store from "@/store/index.js"

let {apiIssue,apiUser} = store.state.apiHeader;

export default {
	// 查询所有园区信息和招商信息
	selectIntentionFromPark:apiIssue + "/middleman/selectIntentionFromPark",
	// 查询预约进展
	findMiddleAppoint:apiIssue+"/middleman/findMiddleAppoint",
	// 新增客户
	addMiddleFirm:apiIssue+"/middleman/addMiddleFirm",
	// 查询所有客户,下拉选择
	findMiddleFirmById:apiIssue+"/middleman/findMiddleFirmById",
	// 查询我的所有客户详情和搜索
	findMiddleFirm:apiIssue+"/middleman/findMiddleFirm",
	// 新增预约
	addMiddleAppoint:apiIssue+"/middleman/addMiddleAppoint",
	// 客户详情（客户信息）
	findMiddleFirmInfo:apiIssue+"/middleman/findMiddleFirmInfo",
	// 客户详情（跟进情况）
	findMiddleCourse:apiIssue+"/middleman/findMiddleCourse",
	// 客户详情（意向房源）
	findMiddleHouse:apiIssue+"/middleman/findMiddleHouse",
	// 客户详情(固定)
	findMiddleFirmUser:apiIssue+"/middleman/findMiddleFirmUser",
	// 新增客户联系人
	addFirmUser:apiIssue+"/middleman/addFirmUser",
	// 修改客户基本信息和联系人
	updateFirmUser:apiIssue+"/middleman/updateFirmUser",
	// 关联招商房源
	addMiddleHouse:apiIssue+"/middleman/addMiddleHouse",
	// 层级查找房间信息
	selectHousePcTier:apiUser+"/tbHouse/selectHousePcTier",
	// 删除客户联系人
	deleteFirmUser:apiIssue+"/middleman/deleteFirmUser",
	// 查询所有联系人,下拉选择
	findMiddleUserById:apiIssue+"/middleman/findMiddleUserById",
	// 查询我的所有客户联系人详情和搜索（已联系人为主）
	findMiddleUsers:apiIssue+"/middleman/findMiddleUsers"
}
