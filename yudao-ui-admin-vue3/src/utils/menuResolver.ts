import { usePermissionStoreWithOut } from '@/store/modules/permission'
import { findPathByMenuName, findPathByPermission } from '@/utils/menuLookup'

/**
 * 租户套餐 × 菜单权限反查工具
 *
 * 真源：后端 @PreAuthorize('xx') -> system_menu.permission -> 前端菜单树（permissionStore.getRouters）
 *
 * - resolvePathByMenuPermission：按 permission 反查当前用户可跳转的页面路径
 * - resolvePathByMenuName：按菜单名反查（仅当无法以 permission 定位时的备用手段）
 * - hasMenuByPermission：当前用户菜单树中是否存在该 permission
 * - resolveFirstAvailablePath：从多个候选 permission / fallback 路径里挑第一个可用的
 */

function collectStaticFallbackPaths(routes: any[]): Map<string, string> {
  const map = new Map<string, string>()
  const visit = (list: any[], parent = '') => {
    for (const r of list || []) {
      if (!r) continue
      const p = r.path?.startsWith('/') ? r.path : parent ? `${parent.replace(/\/$/, '')}/${r.path}` : r.path
      const perm = r?.meta?.permission
      if (typeof perm === 'string' && perm && p && !map.has(perm)) {
        map.set(perm, p)
      }
      if (r.children?.length) visit(r.children, p)
    }
  }
  try {
    visit(routes)
  } catch {
    /* ignore */
  }
  return map
}

function staticFallback(permission: string): string | undefined {
  try {
    const store = usePermissionStoreWithOut()
    const map = collectStaticFallbackPaths(store.getRouters as any[])
    return map.get(permission)
  } catch {
    return undefined
  }
}

export function resolvePathByMenuPermission(permission: string): string | undefined {
  if (!permission) return undefined
  return findPathByPermission(permission) ?? staticFallback(permission)
}

export function resolvePathByMenuName(name: string): string | undefined {
  if (!name) return undefined
  return findPathByMenuName(name)
}

export function hasMenuByPermission(permission: string): boolean {
  return !!resolvePathByMenuPermission(permission)
}

export interface ResolveOptions {
  /** 声明的菜单 permission，按顺序依次尝试反查 */
  permissions?: string[]
  /** 兜底静态路径，若 permissions 都未命中，按顺序回退到这些路径（需与 remaining.ts 中已注册路由匹配才会被使用） */
  fallbackPaths?: string[]
}

/**
 * 按优先级在当前用户可用菜单树中找一个可跳转路径：
 *  1) permissions 列表里任一能命中菜单树 -> 返回该路径
 *  2) fallbackPaths 列表里任一静态路径在 permissionStore.routers 中存在 -> 返回该路径
 *  3) 否则返回 undefined（调用方应据此禁用入口/提示无权限）
 */
export function resolveFirstAvailablePath(options: ResolveOptions): string | undefined {
  const { permissions = [], fallbackPaths = [] } = options
  for (const perm of permissions) {
    const hit = resolvePathByMenuPermission(perm)
    if (hit) return hit
  }
  for (const fb of fallbackPaths) {
    if (isRouteRegistered(fb)) return fb
  }
  return undefined
}

/** 检查静态 remaining.ts + 动态菜单树中是否存在指定 fullPath */
export function isRouteRegistered(targetPath: string): boolean {
  if (!targetPath) return false
  const normalize = (p: string) => (p || '').replace(/\/+$/, '') || '/'
  const target = normalize(targetPath)
  try {
    const routes = usePermissionStoreWithOut().getRouters as any[]
    const walk = (list: any[], parent = ''): boolean => {
      for (const r of list || []) {
        if (!r) continue
        const p = r.path?.startsWith('/') ? r.path : parent ? `${parent.replace(/\/$/, '')}/${r.path}` : r.path
        if (normalize(p) === target) return true
        if (r.children?.length && walk(r.children, p)) return true
      }
      return false
    }
    return walk(routes)
  } catch {
    return false
  }
}
