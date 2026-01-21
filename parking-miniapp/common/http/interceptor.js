import store from "../../store/index.js";
import errorRecovery from "../js/errorRecovery.js";
import securityService from "../js/securityService.js";

const install = (Vue, vm) => {

	let config = {
		baseUrl: store.state.baseUrl,
		// 设置为json，返回后会对数据进行一次JSON.parse()
		dataType: 'json',
		showLoading: true, // 是否显示请求中的loading
		loadingText: '请求中...', // 请求loading中的文字提示
		loadingTime: 8000, // 在此时间内，请求还没回来的话，就显示加载中动画，单位ms
		originalData: true, // 是否在拦截器中返回服务端的原始数据
		loadingMask: true, // 展示loading的时候，是否给一个透明的蒙层，防止触摸穿透
		// 配置请求头信息
		header: {
			'clientId': 'app',
			'clientSecret': 'app',
			'tenant-id': '1'  // 租户标识，多租户系统必需
		},
	}

	Vue.prototype.$u.http.setConfig(config);

	// 请求拦截部分，如配置，每次请求前都会执行
	Vue.prototype.$u.http.interceptor.request = (config) => {
		// Log request for debugging (Requirement 7.4) - redact sensitive data
		securityService.safeLog('[HTTP Request]', {
			url: config.url,
			method: config.method,
			params: config.params,
			data: config.data,
			timestamp: new Date().toISOString()
		});
		
		let userInfo = uni.getStorageSync('userInfo');
		let routes = getCurrentPages();
		let curRoute = routes[routes.length - 1].route;
		const token = uni.getStorageSync('token');

		// 设置请求头，保留 clientId 和 clientSecret (Requirement 3.2)
		config.header = {
			'clientId': 'app',
			'clientSecret': 'app',
			'tenant-id': '1'  // 租户标识，多租户系统必需
		};

		// 如果有 token，添加 Authorization 头 (Requirement 3.1)
		// Never log the actual token value
		if (token && token != '') {
			// 检查 token 是否已经包含 Bearer 前缀
			const authToken = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
			config.header['Authorization'] = authToken;
		}
		
		return config;
	}

	// 响应拦截，如配置，每次请求结束都会执行本方法
	Vue.prototype.$u.http.interceptor.response = (res) => {
		// Log response for debugging (Requirement 7.4) - redact sensitive data
		securityService.safeLog('[HTTP Response]', {
			url: res.config?.url,
			statusCode: res.statusCode,
			data: res.data,
			timestamp: new Date().toISOString()
		});

		const {
			data,
			statusCode
		} = res;
		
		// Validate response structure (Requirement 7.5)
		if (!data || typeof data !== 'object') {
			securityService.safeError('[HTTP Error] Invalid response structure:', res);
			vm.$u.toast('响应数据格式错误');
			return false;
		}
		
		if (statusCode == 200) {
			// Check for business logic errors in response
			if (data.code === 401) {
				// Handle 401 error in response body (Requirement 5.1)
				securityService.safeError('[HTTP Error] 401 Authentication failed:', data);
				handleAuthenticationError(data, vm);
				return data; // Return data so calling code can handle it
			} else if (data.code === 999) {
				// Handle 999 duplicate recharge error (Requirement 5.2)
				securityService.safeError('[HTTP Error] 999 Duplicate recharge:', data);
				handleDuplicateRechargeError(data, vm);
				return data; // Return data so calling code can handle it
			}
			
			// Return successful response
			return data;
		} else if (statusCode == 401) {
			// Handle 401 HTTP status code (Requirement 5.1)
			securityService.safeError('[HTTP Error] 401 HTTP status:', res);
			handleAuthenticationError(data, vm);
			return false;
		} else if (statusCode == 500) {
			// Handle 500 server error
			securityService.safeError('[HTTP Error] 500 Server error:', res);
			vm.$u.toast('服务端错误，请联系管理员');
			return false;
		} else if (statusCode >= 400 && statusCode < 500) {
			// Handle other 4xx client errors
			securityService.safeError('[HTTP Error] Client error:', res);
			vm.$u.toast(data.msg || '请求失败，请重试');
			return false;
		} else if (statusCode >= 500) {
			// Handle other 5xx server errors
			securityService.safeError('[HTTP Error] Server error:', res);
			vm.$u.toast('服务器错误，请稍后重试');
			return false;
		} else {
			// Handle other status codes
			securityService.safeError('[HTTP Error] Unknown status code:', res);
			vm.$u.toast('请求失败，请重试');
			return false;
		}
	}
	
	// Handle network errors (Requirement 5.5)
	Vue.prototype.$u.http.interceptor.fail = (err) => {
		securityService.safeError('[HTTP Network Error]', {
			error: err,
			timestamp: new Date().toISOString()
		});
		
		// Check if it's a network error
		if (err.errMsg && (
			err.errMsg.includes('network') || 
			err.errMsg.includes('timeout') ||
			err.errMsg.includes('fail')
		)) {
			handleNetworkError(err, vm);
		} else {
			vm.$u.toast('请求失败，请重试');
		}
		
		return Promise.reject(err);
	}
}

/**
 * Handle 401 authentication error (Requirement 5.1)
 * @param {Object} data - Response data
 * @param {Object} vm - Vue instance
 */
function handleAuthenticationError(data, vm) {
	console.log('[Error Handler] Handling 401 authentication error');
	
	// Show user-friendly message
	vm.$u.toast('认证失败，请重新登录');
	
	setTimeout(() => {
		// Clear storage except payment intent
		const paymentIntent = errorRecovery.getPaymentIntent();
		uni.clearStorageSync();
		
		// Restore payment intent if exists
		if (paymentIntent) {
			uni.setStorageSync(errorRecovery.STORAGE_KEYS.PAYMENT_INTENT, JSON.stringify(paymentIntent));
		}
		
		// Redirect to login page
		uni.reLaunch({
			url: '/pages/login/login?redirect=payment'
		});
	}, 1500);
}

/**
 * Handle 999 duplicate recharge error (Requirement 5.2)
 * @param {Object} data - Response data
 * @param {Object} vm - Vue instance
 */
function handleDuplicateRechargeError(data, vm) {
	console.log('[Error Handler] Handling 999 duplicate recharge error');
	
	// Display backend error message
	const message = data.msg || '您已充值过了，无需再次充值';
	vm.$u.toast(message);
}

/**
 * Handle network error (Requirement 5.5)
 * @param {Object} error - Error object
 * @param {Object} vm - Vue instance
 */
function handleNetworkError(error, vm) {
	console.log('[Error Handler] Handling network error:', error);
	
	// Show network error message
	vm.$u.toast('网络错误，请检查网络连接');
}

/**
 * Handle WeChat API errors (Requirement 5.3, 5.4)
 * This is exported for use in other modules
 */
export function handleWechatApiError(error, apiName) {
	securityService.safeError(`[WeChat API Error] ${apiName} failed:`, {
		error: error,
		timestamp: new Date().toISOString()
	});
	
	let message = '';
	
	if (apiName === 'wx.login') {
		// Handle wx.login failure (Requirement 5.3)
		message = '微信登录失败，请重试';
	} else if (apiName === 'wx.requestPayment') {
		// Handle wx.requestPayment failure (Requirement 5.4)
		if (error.errMsg && error.errMsg.includes('cancel')) {
			message = '支付已取消';
		} else {
			message = error.errMsg || '支付失败，请重试';
		}
	} else {
		message = '微信接口调用失败，请重试';
	}
	
	return {
		success: false,
		message: message,
		error: error
	};
}

export default {
	install
}
