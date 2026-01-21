// 路由
import {
	RouterMount,
	createRouter
} from 'uni-simple-router'
import store from "@/store/index.js"

const router = createRouter({
	platform: process.env.VUE_APP_PLATFORM,
	applet: {
		animationDuration: 0, //默认 300ms
		// 小程序特殊配置：完全使用原生导航
		mode: 'none'
	},
	h5: {
		vueRouterDev: false
	},
	encodeURI: false,
	// 通配符，非定义页面，跳转首页
	routes: [
		{
			path: '*',
			redirect: (to) => {
				// 避免首页本身触发重定向导致死循环
				if (to.path === '/pages/index/index') {
					return false; // 阻止重定向
				}
				return { path: '/pages/index/index' };
			}
		}
	],
	routerErrorEach: ({
		type,
		msg
	}) => {
		console.log('Router error:', type, msg);
		switch (type) {
			case 3: // APP退出应用
				// #ifdef APP-PLUS
				router.$lockStatus = false;
				uni.showModal({
					title: '提示',
					content: '您确定要退出应用吗？',
					success: function(res) {
						if (res.confirm) {
							plus.runtime.quit();
						}
					}
				});
				// #endif
				break;
			case 2:
			case 0:
				router.$lockStatus = false;
				break;
			default:
				break;
		}
	}
});

//全局路由前置守卫
router.beforeEach((to, from, next) => {
	// 白名单：不需要登录的页面
	const whiteList = [
		'/pages/login/login',
		'/pages/register/register',
		'/pages/retrievePassword/retrievePassword',
		'/pages/login/agreement/agreement'
	];

	// 获取用户信息
	const userInfo = uni.getStorageSync('userInfo');
	
	// 如果在白名单中，直接放行
	if (whiteList.includes(to.path)) {
		next();
		return;
	}
	
	// 如果未登录且不在白名单，跳转登录页
	if (!userInfo) {
		// 避免循环重定向：如果已经在跳转到登录页的过程中，不再重复跳转
		if (to.path === '/pages/login/login') {
			next();
		} else {
			next({
				path: '/pages/login/login',
				query: {
					redirect: to.fullPath
				}
			});
		}
	} else {
		// 已登录，放行
		next();
	}
});

export {
	router,
	RouterMount
}
