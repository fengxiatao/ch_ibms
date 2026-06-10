/**
 * 首页可点击区域：不以硬编码路径为唯一依据，由 Index.vue 结合 {@link @/utils/menuLookup} 动态解析
 */
export interface HomeEntry {
  key: string
  /** 优先按权限反查（须与 @PreAuthorize / system_menu 一致） */
  permission?: string
  /** 无 permission 时按与后端菜单 name 一致的中文名反查 */
  menuName?: string
  /** 解析失败时兜底，仍按顺序尝试 router 匹配 */
  fallbackPaths?: string[]
  /** 与 subModuleRoutes 的 reload 行为一致，用于微前端/动态 chunk 等场景 */
  reloadIfNotMatched?: boolean
  /** 仅当以上均无法匹配时的路由 name 兜底 */
  routeName?: string
}

export const MODULE_ENTRIES: Record<'security' | 'access' | 'energy' | 'building', HomeEntry> = {
  security: {
    key: 'security',
    fallbackPaths: ['/security/video-surveillance/visual-board', '/security/security-overview']
  },
  access: {
    key: 'access',
    fallbackPaths: ['/smart-access/door/visual-dashboard', '/iot/access/visual-dashboard']
  },
  energy: {
    key: 'energy',
    fallbackPaths: ['/iot/building/newlight/overview', '/energy/overview', '/iot/building/newlight/control']
  },
  building: {
    key: 'building',
    fallbackPaths: ['/building/visual-dashboard', '/iot/building/visual-dashboard']
  }
}

export const SUB_ENTRIES: Record<string, HomeEntry> = {
  alarmType: {
    key: 'alarmType',
    permission: 'security:perimeter',
    menuName: '入侵报警',
    fallbackPaths: ['/security/perimeter-intrusion/intrusionalarm', '/security/new-intrusion-alarm']
  },
  ePatrol: {
    key: 'ePatrol',
    fallbackPaths: ['/security/electronic-patrol/visualization-board']
  },
  door: {
    key: 'door',
    menuName: '门禁管理',
    fallbackPaths: ['/smart-access/door/visual-dashboard', '/smart-access/door/management', '/iot/access/management']
  },
  visitor: {
    key: 'visitor',
    menuName: '访客管理',
    reloadIfNotMatched: true,
    fallbackPaths: ['/smart-access/visitor-management/home']
  },
  energy: {
    key: 'sub-energy',
    fallbackPaths: ['/energy/overview', '/building/newlight/overview', '/building/newlight/control']
  }
}
