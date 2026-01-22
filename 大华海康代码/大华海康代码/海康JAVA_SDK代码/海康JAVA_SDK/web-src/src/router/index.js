import Vue from 'vue'
import VueRouter from 'vue-router'
import Main from '@/views/Main/Main.vue'

Vue.use(VueRouter)

const routes = [
    {
        path: '/',
        redirect: '/main',
        name: 'main',
        component: () => import('../views/Main/Main.vue')
    },
    {
        path: '/main',
        name: 'main',
        redirect: '/main/home',
        component: Main,
        children: [
			{
				path: 'ptzControl',
				name: 'ptzControl',
				meta: {title: '云台控制'},
				component: () => import('../views/PtzControl/PtzControl.vue')
			},
            {
                path: 'home',
                name: 'home',
                meta: {title: '预置位'},
                component: () => import('../views/Home/Home.vue')
            },
            {
                path: 'storage',
                name: 'storage',
                meta: {title: '云端录像'},
                component: () => import('../views/Storage/Storage.vue')
            },
            {
                path: 'HCNetptzControl',
                name: 'HCNetptzControl',
                meta: {title: '海康云台控制'},
                component: () => import('../views/HCNetPtzControl/HCNetPtzControl.vue')
            },
        ]
    }
]

const router = new VueRouter({
    routes
})

export default router
