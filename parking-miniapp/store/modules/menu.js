export const state = {

	// 首页底部导航栏
	dominNavBar: [{
			iconPath: "home",
			selectedIconPath: "home-fill",
			text: '首页',
			customIcon: false,
			pagePath: '/pages/index'
		},
		{
			iconPath: "file-text",
			selectedIconPath: "file-text-fill",
			text: '服务',
			customIcon: false,
			pagePath: '/pages/service/index'
		},
		{
			iconPath: "account",
			selectedIconPath: "account-fill",
			text: '我的',
			customIcon: false,
			pagePath: "/pages/main/main"
		}
	],
	// 中介招商系统导航栏
	interNavBar: [{
			iconPath: "home",
			text: '园区',
			path:"/package/IntermediarySystem/ListOfParks/ListOfParks"
		},
		{
			iconPath: "edit",
			text: '进展',
			path:"/package/IntermediarySystem/progress/progress"
		},
		{
			iconPath: "friend",
			text: '客户',
			path:"/package/IntermediarySystem/customer/customer"
		},
		{
			iconPath: "my",
			text: '我的',
			path:"/pages/main/main"
		},
		
	],
	// 中介系统导航栏当前下标；
	interNavIndex:0,

};

export const getters = {};

export const mutations = {
	SET_INTERNAVINDEX(state,index){
		state.interNavIndex = index;
	}
};

export const actions = {}
