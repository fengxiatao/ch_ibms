import { pathResolve } from '@/utils/routerHelper'
import { isUrl } from '@/utils/is'
import { usePermissionStoreWithOut } from '@/store/modules/permission'

/**
 * 深度优先，返回该节点下第一个非 hidden 的可达页面完整路径
 */
function findFirstVisibleLeafPath(
  route: AppRouteRecordRaw,
  parentPath: string
): string | undefined {
  if (route.meta?.hidden) {
    return undefined
  }
  const nodeFull = isUrl(route.path) ? route.path : pathResolve(parentPath, route.path)
  const children = (route.children ?? []).filter((c) => !c.meta?.hidden)
  if (children.length) {
    for (const child of children) {
      const hit = findFirstVisibleLeafPath(child, nodeFull)
      if (hit) {
        return hit
      }
    }
  }
  return nodeFull
}

/**
 * 按 meta.permission 在后端动态路由树中反查可跳转路径（若命中目录则取首个可见叶子）
 */
export function findPathByPermission(
  permission: string,
  routes: AppRouteRecordRaw[] = usePermissionStoreWithOut().getRouters,
  parentPath = '/'
): string | undefined {
  if (!permission) {
    return undefined
  }
  for (const route of routes) {
    if (route.meta?.hidden) {
      continue
    }
    const fullPath = isUrl(route.path) ? route.path : pathResolve(parentPath, route.path)
    if ((route.meta as any)?.permission === permission) {
      const visibleChildren = (route.children ?? []).filter((c) => !c.meta?.hidden)
      if (visibleChildren.length) {
        return findFirstVisibleLeafPath(route, parentPath) ?? fullPath
      }
      return fullPath
    }
    if (route.children?.length) {
      const sub = findPathByPermission(permission, route.children, fullPath)
      if (sub) {
        return sub
      }
    }
  }
  return undefined
}

/**
 * 按菜单展示名（与后端 name / meta.title 一致）反查可跳转路径
 */
export function findPathByMenuName(
  menuName: string,
  routes: AppRouteRecordRaw[] = usePermissionStoreWithOut().getRouters,
  parentPath = '/'
): string | undefined {
  if (!menuName) {
    return undefined
  }
  for (const route of routes) {
    if (route.meta?.hidden) {
      continue
    }
    const fullPath = isUrl(route.path) ? route.path : pathResolve(parentPath, route.path)
    if (String((route.meta as any)?.title ?? '') === menuName) {
      const visibleChildren = (route.children ?? []).filter((c) => !c.meta?.hidden)
      if (visibleChildren.length) {
        return findFirstVisibleLeafPath(route, parentPath) ?? fullPath
      }
      return fullPath
    }
    if (route.children?.length) {
      const sub = findPathByMenuName(menuName, route.children, fullPath)
      if (sub) {
        return sub
      }
    }
  }
  return undefined
}

export function hasMenuByName(
  menuName: string,
  routes: AppRouteRecordRaw[] = usePermissionStoreWithOut().getRouters
): boolean {
  return !!findPathByMenuName(menuName, routes)
}
