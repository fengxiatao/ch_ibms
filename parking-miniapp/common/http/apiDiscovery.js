import store from "@/store/index.js"

let {apiDiscovery} = store.state.apiHeader;

export default {
	// 分页查询园区活动
	selectParkActivity:apiDiscovery+"/parkActivity/selectParkActivity",
	// 新增园区活动人员
	parkActivityUserSave:apiDiscovery+"/parkActivityUser/save",
	// 小程序--根据id查询园区活动信息
	selectParkActivityById:apiDiscovery+"/parkActivity/selectParkActivityById",
	
	// 新增第三方服务商
	saveThirdParty:apiDiscovery+"/thirdParty/saveThirdParty",
	// 运营端根据服务商id/小程序端根据园区id：分页查询文章和服务信息
	selectThirdPartyArticle:apiDiscovery+"/thirdParty/selectThirdPartyArticle",
	// 新增文章和服务点击次数
	addClickNumber:apiDiscovery+"/thirdParty/addClickNumber"
}
