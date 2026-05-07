import { Layout } from '@/utils/routerHelper'

const { t } = useI18n()
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

 icon: 'svg-name'          设置该路由的图标

 noCache: true             如果设置为true，则不会被 <keep-alive> 缓存(默认 false)

 breadcrumb: false         如果设置为false，则不会在breadcrumb面包屑中显示(默认 true)

 affix: true               如果设置为true，则会一直固定在tag项中(默认 false)

 noTagsView: true          如果设置为true，则不会出现在tag中(默认 false)

 activeMenu: '/dashboard'  显示高亮的路由路径

 followAuth: '/dashboard'  跟随哪个路由进行权限过滤

 canTo: true               设置为true即使hidden为true，也依然可以进行路由跳转(默认 false)
 }
 **/
const remainingRouter: AppRouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    name: 'Redirect',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/index',
    name: 'Home',
    meta: {},
    children: [
      {
        path: 'index',
        component: () => import('@/views/Home/Index.vue'),
        name: 'Index',
        meta: {
          title: '首页',
          icon: 'ep:home-filled',
          noCache: false,
          affix: true,
          noTagsView: true
        }
      }
    ]
  },
  {
    path: '/security/electronic-patrol/visualization-board',
    component: Layout,
    name: 'SecurityElectronicPatrolVisualizationBoardParent',
    meta: {
      hidden: true
    },
    children: [
      {
        path: '',
        component: () =>
          import('@/views/security/ElectronicPatrol/PatrolVisualizationBoard/index.vue'),
        name: 'SecurityElectronicPatrolVisualizationBoard',
        meta: {
          title: '离线巡更可视化看板',
          hidden: true,
          canTo: true,
          noTagsView: true,
          noPadding: true,
          icon: 'ep:monitor'
        }
      }
    ]
  },
  {
    path: '/iot/building/env/overview',
    component: Layout,
    name: 'IotBuildingEnvOverviewParent',
    meta: {
      hidden: true
    },
    children: [
      {
        path: '',
        component: () => import('@/views/iot/building/env/overview/index.vue'),
        name: 'IotBuildingEnvOverview',
        meta: {
          title: '室内环境监测数据总览',
          hidden: true,
          canTo: true,
          icon: 'ep:orange',
          activeMenu: '/iot/building/env/sensor'
        }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    name: 'UserInfo',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'profile',
        component: () => import('@/views/Profile/Index.vue'),
        name: 'Profile',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:user',
          title: t('common.profile')
        }
      },
      {
        path: 'notify-message',
        component: () => import('@/views/system/notify/my/index.vue'),
        name: 'MyNotifyMessage',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:message',
          title: '我的站内信'
        }
      }
    ]
  },
  {
    path: '/dict',
    component: Layout,
    name: 'dict',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'type/data/:dictType',
        component: () => import('@/views/system/dict/data/index.vue'),
        name: 'SystemDictData',
        meta: {
          title: '字典数据',
          noCache: true,
          hidden: true,
          canTo: true,
          icon: '',
          activeMenu: '/system/dict'
        }
      }
    ]
  },

  {
    path: '/codegen',
    component: Layout,
    name: 'CodegenEdit',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'edit',
        component: () => import('@/views/infra/codegen/EditTable.vue'),
        name: 'InfraCodegenEditTable',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '修改生成配置',
          activeMenu: 'infra/codegen/index'
        }
      }
    ]
  },
  {
    path: '/job',
    component: Layout,
    name: 'JobL',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'job-log',
        component: () => import('@/views/infra/job/logger/index.vue'),
        name: 'InfraJobLog',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '调度日志',
          activeMenu: 'infra/job/index'
        }
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/sso',
    component: () => import('@/views/Login/Login.vue'),
    name: 'SSOLogin',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/social-login',
    component: () => import('@/views/Login/SocialLogin.vue'),
    name: 'SocialLogin',
    meta: {
      hidden: true,
      title: t('router.socialLogin'),
      noTagsView: true
    }
  },
  {
    path: '/403',
    component: () => import('@/views/Error/403.vue'),
    name: 'NoAccess',
    meta: {
      hidden: true,
      title: '403',
      noTagsView: true
    }
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFound',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  },
  {
    path: '/500',
    component: () => import('@/views/Error/500.vue'),
    name: 'Error',
    meta: {
      hidden: true,
      title: '500',
      noTagsView: true
    }
  },
  {
    path: '/ai',
    component: Layout,
    name: 'Ai',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'image/square',
        component: () => import('@/views/ai/image/square/index.vue'),
        name: 'AiImageSquare',
        meta: {
          title: '绘图作品',
          icon: 'ep:home-filled',
          noCache: false
        }
      },
      {
        path: 'knowledge/document',
        component: () => import('@/views/ai/knowledge/document/index.vue'),
        name: 'AiKnowledgeDocument',
        meta: {
          title: '知识库文档',
          icon: 'ep:document',
          noCache: false,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/document/create',
        component: () => import('@/views/ai/knowledge/document/form/index.vue'),
        name: 'AiKnowledgeDocumentCreate',
        meta: {
          title: '创建文档',
          icon: 'ep:plus',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/document/update',
        component: () => import('@/views/ai/knowledge/document/form/index.vue'),
        name: 'AiKnowledgeDocumentUpdate',
        meta: {
          title: '修改文档',
          icon: 'ep:edit',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/retrieval',
        component: () => import('@/views/ai/knowledge/knowledge/retrieval/index.vue'),
        name: 'AiKnowledgeRetrieval',
        meta: {
          title: '文档召回测试',
          icon: 'ep:search',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'knowledge/segment',
        component: () => import('@/views/ai/knowledge/segment/index.vue'),
        name: 'AiKnowledgeSegment',
        meta: {
          title: '知识库分段',
          icon: 'ep:tickets',
          noCache: true,
          hidden: true,
          activeMenu: '/ai/knowledge'
        }
      },
      {
        path: 'console/workflow/create',
        component: () => import('@/views/ai/workflow/form/index.vue'),
        name: 'AiWorkflowCreate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计 AI 工作流',
          activeMenu: '/ai/console/workflow'
        }
      },
      {
        path: 'console/workflow/:type/:id',
        component: () => import('@/views/ai/workflow/form/index.vue'),
        name: 'AiWorkflowUpdate',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          title: '设计 AI 工作流',
          activeMenu: '/ai/console/workflow'
        }
      }
    ]
  },
  {
    path: '/iot',
    component: Layout,
    name: 'IOT',
    meta: {
      hidden: true
    },
    children: [
      // 设备发现：必须挂在与菜单一致的 /iot 静态父路由下，否则仅依赖动态路由时可能与首条 /iot 记录冲突导致无法匹配、页面长期白屏/加载
      {
        path: 'discovery',
        name: 'IbmsDeviceDiscovery',
        meta: {
          title: '设备发现',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/discovery'
        },
        component: () => import('@/views/ibms/discovery/index.vue')
      },
      {
        path: 'product/product/detail/:id',
        name: 'IoTProductDetail',
        redirect: (to) => ({
          path: '/ibms/ibms-product',
          query: { id: String(to.params.id) }
        }),
        meta: {
          title: '产品详情',
          noCache: true,
          hidden: true,
          activeMenu: '/ibms/ibms-product'
        }
      },
      {
        path: 'device/detail/:id',
        name: 'IoTDeviceDetail',
        redirect: (to) => ({
          path: '/ibms/ibms-device',
          query: { deviceId: String(to.params.id) }
        }),
        meta: {
          title: '设备详情',
          noCache: true,
          hidden: true,
          activeMenu: '/ibms/ibms-device'
        }
      },
      {
        path: 'ota/operation/firmware/detail/:id',
        name: 'IoTOtaFirmwareDetail',
        meta: {
          title: '固件详情',
          noCache: true,
          hidden: true,
          activeMenu: '/iot/operation/ota/firmware'
        },
        component: () => import('@/views/iot/ota/firmware/detail/index.vue')
      },
      {
        path: 'gis',
        name: 'IoTGisMap',
        meta: {
          title: 'GIS 地图',
          noCache: false,
          hidden: true,
          canTo: true,
          icon: 'ep:map-location',
          activeMenu: '/ibms/ibms-device'
        },
        component: () => import('@/views/iot/gis/index.vue')
      },
      {
        path: 'spatial/floorplan',
        name: 'FloorPlanViewer',
        meta: {
          title: '建筑平面图',
          noCache: false,
          hidden: false, // 显示在菜单中
          canTo: true,
          icon: 'ep:office-building',
          activeMenu: '/ibms/ibms-device'
        },
        component: () => import('@/views/iot/spatial/floorplan/index.vue')
      },
      {
        path: 'building/visual-dashboard',
        name: 'IoTBuildingVisualDashboard',
        meta: {
          title: '智慧建筑可视化',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/building-visual-dashboard/index.vue')
      },
      {
        path: 'building/newlight/overview',
        name: 'IoTBuildingNewLightOverview',
        meta: {
          title: '智能照明V2-数据总览',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/overview/index.vue')
      },
      {
        path: 'building/newlight/control',
        name: 'IoTBuildingNewLightControl',
        meta: {
          title: '智能照明V2-照明控制',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/control/index.vue')
      },
      {
        path: 'building/newlight/device',
        name: 'IoTBuildingNewLightDevice',
        meta: {
          title: '智能照明V2-设备管理',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/device/index.vue')
      },
      {
        path: 'building/newlight/task',
        name: 'IoTBuildingNewLightTask',
        meta: {
          title: '智能照明V2-任务管理',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/task/index.vue')
      },
      {
        path: 'building/newlight/log',
        name: 'IoTBuildingNewLightLog',
        meta: {
          title: '智能照明V2-日志管理',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/log/index.vue')
      },
      {
        path: 'building/newlight/alarm',
        name: 'IoTBuildingNewLightAlarm',
        meta: {
          title: '智能照明V2-告警信息',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/alarm/index.vue')
      },
      {
        path: 'building/newlight/circuit',
        name: 'IoTBuildingNewLightCircuit',
        meta: {
          title: '智能照明V2-回路配置',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/building'
        },
        component: () => import('@/views/iot/building/newlight/circuit/index.vue')
      }
    ]
  },
  // 绝对路径兜底：实时预览页面（避免动态路由/扁平化导致嵌套路由匹配异常时落到 404）
  {
    path: '/security/video-surveillance/real-time-preview',
    component: Layout,
    name: 'RealTimePreviewAbsParent',
    meta: { hidden: true },
    children: [
      {
        path: '',
        name: 'RealTimePreviewAbs',
        meta: {
          title: '实时预览(大华)',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/security/video-surveillance/real-time-preview'
        },
        component: () => import('@/views/security/VideoSurveillance/RealTimePreview/index.vue')
      }
    ]
  },
  {
    path: '/security/perimeter-intrusion/arming-plan',
    component: Layout,
    name: 'PerimeterIntrusionArmingPlanAbsParent',
    meta: { hidden: true },
    children: [
      {
        path: '',
        name: 'PerimeterIntrusionArmingPlanAbs',
        meta: {
          title: '布防计划',
          noCache: true,
          hidden: true,
          canTo: true,
          noPadding: true,
          activeMenu: '/security/perimeter-intrusion/arming-plan'
        },
        component: () => import('@/views/security/PerimeterIntrusion/ArmingPlan/index.vue')
      }
    ]
  },
  {
    path: '/building/visual-dashboard',
    component: Layout,
    name: 'BuildingVisualDashboard',
    meta: {
      hidden: true
    },
    children: [
      {
        path: '',
        name: 'BuildingVisualDashboardRoute',
        meta: {
          title: '智慧建筑可视化',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/building'
        },
        component: () => import('@/views/iot/building/building-visual-dashboard/index.vue')
      }
    ]
  },
  {
    path: '/smart-access/door/visual-dashboard',
    component: Layout,
    name: 'AccessVisualDashboard',
    meta: {
      hidden: true
    },
    children: [
      {
        path: '',
        name: 'AccessVisualDashboardRoute',
        meta: {
          title: '智慧通行可视化',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/iot/access/management'
        },
        component: () => import('@/views/iot/access/visual-dashboard/index.vue')
      }
    ]
  },
  // 兼容旧门禁可视化地址：/iot/access/visual-dashboard -> /smart-access/door/visual-dashboard
  {
    path: '/iot/access/visual-dashboard',
    redirect: '/smart-access/door/visual-dashboard',
    name: 'AccessVisualDashboardRedirect',
    meta: {
      hidden: true,
      noTagsView: true,
      breadcrumb: false
    }
  },
  {
    path: '/security',
    component: Layout,
    name: 'Security',
    meta: {
      hidden: true
    },
    children: [
      // 视频监控可视化看板（智慧安防入口）
      {
        path: 'video-surveillance/visual-board',
        name: 'VideoSurveillanceVisualBoard',
        meta: {
          title: '智慧安防',
          noCache: false,
          hidden: false,
          canTo: true,
          icon: 'ep:video-camera',
          activeMenu: '/security/video-surveillance/visual-board'
        },
        component: () => import('@/views/security/VideoSurveillance/VisualBoard/index.vue')
      },
      // 兼容旧路径：/security/video-surveillance/realtime-preview -> /security/video-surveillance/real-time-preview
      {
        path: 'video-surveillance/realtime-preview',
        redirect: '/security/video-surveillance/real-time-preview',
        name: 'RealTimePreviewRedirect',
        meta: {
          hidden: true,
          noTagsView: true,
          breadcrumb: false
        }
      },
      {
        path: 'video-surveillance/real-time-preview',
        name: 'RealTimePreviewNew',
        meta: {
          title: '实时预览(大华)',
          noCache: true,
          hidden: false,
          canTo: true
        },
        component: () => import('@/views/security/VideoSurveillance/RealTimePreview/index.vue')
      },
      {
        path: 'video-surveillance/video-alarm-record',
        name: 'VideoAlarmRecord',
        meta: {
          title: '告警管理',
          noCache: true,
          hidden: true,
          canTo: true,
          noTagsView: true,
          activeMenu: '/security/video-surveillance/visual-board'
        },
        component: () => import('@/views/security/VideoSurveillance/VideoAlarmRecord/index.vue')
      },
      {
        path: 'perimeter-intrusion/intrusionalarm',
        name: 'PerimeterIntrusionVisualBoard',
        meta: {
          title: '入侵报警看板',
          noCache: true,
          hidden: true,
          canTo: true,
          noTagsView: true,
          activeMenu: '/security/perimeter-intrusion/arming-plan'
        },
        component: () => import('@/views/security/PerimeterIntrusion/VisualBoard/index.vue')
      },
      {
        path: 'video-surveillance/patrol-config',
        name: 'PatrolConfig',
        meta: {
          title: '轮巡配置',
          noCache: true,
          hidden: false,
          canTo: true
        },
        component: () => import('@/views/security/VideoSurveillance/PatrolConfig/index.vue')
      },
      {
        path: 'video-surveillance/patrol-config/task-edit',
        name: 'PatrolTaskEdit',
        meta: {
          title: '任务编辑',
          noCache: true,
          hidden: true,
          canTo: true
        },
        component: () => import('@/views/security/VideoSurveillance/PatrolConfig/TaskEdit.vue')
      },
      {
        path: 'video-surveillance/video-playback',
        name: 'VideoPlayback',
        meta: {
          title: '录像回放',
          noCache: true,
          hidden: false,
          canTo: true
        },
        component: () => import('@/views/security/VideoSurveillance/VideoPlayback/index.vue')
      },
      {
        path: 'video-surveillance/patrol-schedule',
        name: 'PatrolSchedule',
        meta: {
          title: '定时轮巡',
          noCache: true,
          hidden: false,
          canTo: true
        },
        component: () => import('@/views/security/VideoSurveillance/PatrolSchedule/index.vue')
      },
      {
        path: 'video-patrol/live',
        name: 'VideoPatrolLive',
        meta: {
          title: '实时巡更',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/security/video-patrol/task-management'
        },
        component: () => import('@/views/security/VideoPatrol/PatrolTasks/index.vue')
      },
      {
        path: 'video-patrol/task',
        name: 'PatrolTask',
        meta: {
          title: '巡检任务',
          noCache: true,
          hidden: true,
          canTo: true,
          activeMenu: '/security/video-patrol/patrol-task'
        },
        component: () => import('@/views/security/VideoPatrol/PatrolTask/index.vue')
      }
    ]
  },
  {
    path: '/factory/digital-twin-studio',
    component: Layout,
    name: 'FactoryDigitalTwinStudioParent',
    meta: {
      hidden: true
    },
    children: [
      {
        path: '',
        name: 'FactoryDigitalTwinStudio',
        meta: {
          title: '数字孪生开发工作台',
          noCache: true,
          hidden: true,
          canTo: true,
          noPadding: true,
          noTagsView: true
        },
        component: () => import('@/views/factory/digital-twin-studio/index.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/Error/404.vue'),
    name: '404Page',
    meta: {
      title: '404',
      hidden: true,
      breadcrumb: false
    }
  }
]

export default remainingRouter
