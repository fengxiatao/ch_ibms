export const state = {

	// 当前选择关联房屋的客户
	currentCustomer:{},
	
	

};

export const getters = {};

export const mutations = {
	// 更新当前选择关联房屋的客户
	SET_CURRENTCUSTOMER(state,info){
		state.currentCustomer = info
	}
};

export const actions = {}
