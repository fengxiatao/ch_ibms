import store from "@/store/index.js"
let {
	apiUser,
	apiParking
} = store.state.apiHeader;
module.exports = {
	// ========== 停车收费相关接口 ==========
	// 查询停车费用
	queryParkingFee: "/app-api/parking/queryFee",
	// 停车微信支付
	parkingWechatPay: "/app-api/parking/wechat/pay",
	// 通知道闸放行
	notifyGateOpen: "/app-api/parking/gate/open",
	// 停车订单列表
	parkingOrderList: "/app-api/parking/order/list",
	// 停车订单详情
	parkingOrderDetail: "/app-api/parking/order/detail",
	
	// ========== 用户相关接口 ==========
	// 微信小程序登录（自动注册）
	wechatLogin: "/app-api/user/wechatLogin",
	// 获取用户信息
	userInfo: "/app-api/user/info",
	// 微信支付
	wexinPay: "/app-api/wechat/pay/unified/request",
	// 绑定微信账号
	bindWechat: "/app-api/user/bindWechat",
	// 检查微信绑定状态
	checkWechatBinding: "/app-api/user/wechatBinding",
	// 修改用户名称
	updateNickname: "/app-api/user/updateNickname",
	// 退出登录
	logout: "/app-api/user/logout",
	
	// ========== 其他接口 ==========
	// 上传附件
	file: apiUser + '/upload/uploadFile',
	// 查园区信息
	selectXq: apiUser + "/tbPark/selectXq",
	// 扫码解析访客小程序码
	analysisQRCode: apiUser + "/qrcode/analysisQRCode",
	// 查询费用
	findTbCarOrder: apiParking + "/xcx/findTbCarOrder",
	findUserListByOpenid: apiUser + "/users/findUserListByOpenid",
	// 根据账号密码修改用户openid（微信绑定账号）
	uploadUserOpenidByPassword: apiUser + "/users/uploadUserOpenidByPassword",
	// 根据用户id修改用户openid（微信解除账号）
	uploadUserOpenidByid: apiUser + "/users/uploadUserOpenidByid"
}
