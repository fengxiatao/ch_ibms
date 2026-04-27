import type { RouteRecordRaw } from 'vue-router'
import { defineComponent } from 'vue'

/**
* redirect: noredirect        当设置 noredirect 的时候该路由在面包屑导航中不可被点击
* name:'router-name'          设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
* meta : {
    hidden: true              当设置 true 的时候该路由不会再侧边栏出现 如404，login等页面(默认 false)

    alwaysShow: true          当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式，
                              只有一个时，会将那个子路由当做根路由显示在侧边栏，
                              若你想不管路由下面的 children 声明的个数都显示你的根路由，
                              你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，
                              一直显示根路由(默认 false)

    title: 'title'            设置该路由在侧边栏和面包屑中展示的名字

    titleSuffix: '2'          当 path 和 title 重复时的后缀或备注

    icon: 'svg-name'          设置该路由的图标

    noCache: true             如果设置为true，则不会被 <keep-alive> 缓存(默认 false)

    breadcrumb: false         如果设置为false，则不会在breadcrumb面包屑中显示(默认 true)

    affix: true               如果设置为true，则会一直固定在tag项中(默认 false)

    noTagsView: true          如果设置为true，则不会出现在tag中(默认 false)

    activeMenu: '/home'  显示高亮的路由路径

    followAuth: '/home'  跟随哪个路由进行权限过滤

    canTo: true               设置为true即使hidden为true，也依然可以进行路由跳转(默认 false)
  }
**/
declare module 'vue-router' {
  interface RouteMeta extends Record<string | number | symbol, unknown> {
    hidden?: boolean
    alwaysShow?: boolean
    title?: string
    titleSuffix?: string
    icon?: string
    noCache?: boolean
    breadcrumb?: boolean
    affix?: boolean
    pathKey?: boolean
    activeMenu?: string
    noTagsView?: boolean
    followAuth?: string
    canTo?: boolean
    /** 与后端 system_menu.permission 一致，供首页/目录反查可访问路径 */
    permission?: string
    /** 为 true 时侧栏目录标题可点击，配合首个可达叶子或 landingName */
    directoryClickable?: boolean
    /** 显式跳转到与标题匹配的子菜单名（兼容旧字段） */
    landingName?: string
    /** 目录标题点击后的落地目标，可为菜单标题或绝对路由 */
    directoryLanding?: string
    /** 目录标题点击按权限反查落地页 */
    directoryPermission?: string
  }
}

type Component<T = any> =
  | ReturnType<typeof defineComponent>
  | (() => Promise<typeof import('*.vue')>)
  | (() => Promise<T>)

declare global {
  interface AppRouteRecordRaw extends Omit<RouteRecordRaw, 'meta'> {
    name: string
    meta: RouteMeta
    component?: Component | string
    children?: AppRouteRecordRaw[]
    props?: Recordable
    fullPath?: string
    keepAlive?: boolean
  }

  interface AppCustomRouteRecordRaw extends Omit<RouteRecordRaw, 'meta'> {
    icon: any
    name: string
    meta: RouteMeta
    component: string
    componentName?: string
    path: string
    redirect: string
    children?: AppCustomRouteRecordRaw[]
    keepAlive?: boolean
    visible?: boolean
    parentId?: number
    alwaysShow?: boolean
    /** 后端 system_menu.permission，会写入 route.meta.permission */
    permission?: string
    /** 目录标题是否可点击 */
    directoryClickable?: boolean
    /** 目录标题落地目标（菜单名或绝对路由） */
    directoryLanding?: string
    /** 目录标题按权限反查落地页 */
    directoryPermission?: string
  }
}
