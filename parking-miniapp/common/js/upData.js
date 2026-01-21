// 刷新touch监听
const updataEvent = {
	// // 刷新touch监听
	isRefresh(that,fun,params) {
		that.fun(params)
		setTimeout(() => {
			uni.showToast({
				icon: 'success',
				title: '刷新成功,数据恢复初始值'
			})
	
			// 刷新结束调用
			this.$refs.sibList.endAfter()
		}, 1000)
	},
	scrolltolowerFn(count,maxCount,that,fun,params) {
		return new Promise((resolve,reject)=>{
			count += 1 ;
			if(count > maxCount){
				count = maxCount ;
			}else{
				that.fun()
			}
			success: resolve;
			fail: reject
		})
		
		
		
	},
}

export default updataEvent
