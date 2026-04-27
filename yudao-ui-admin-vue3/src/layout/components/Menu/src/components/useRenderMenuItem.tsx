import { ElSubMenu, ElMenuItem } from 'element-plus'
import { nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { hasOneShowingChild } from '../helper'
import { isUrl } from '@/utils/is'
import { useRenderMenuTitle } from './useRenderMenuTitle'
import { pathResolve } from '@/utils/routerHelper'
import { useI18n } from '@/hooks/web/useI18n'
import { findPathByMenuName } from '@/utils/menuLookup'
import { resolvePathByMenuPermission } from '@/utils/menuResolver'

const { renderMenuTitle } = useRenderMenuTitle()

/**
 * 过渡期：部分老菜单尚未回填 meta.directoryClickable / meta.directoryLanding，
 * 先按中文菜单名做兼容，后续逐步迁移到后端 meta 配置后即可清空。
 * 新增业务目录点击行为请走后端 system_menu：
 *   - meta.directoryClickable = true          -> 点击目录标题时跳转
 *   - meta.directoryLanding   = '子菜单名'    -> 跳到指定子菜单
 *   - meta.directoryPermission = 'xxx:yyy'     -> 按权限反查菜单树得到落地页
 */
const legacyClickableDirectoryTitles = new Set<string>([
  '门禁管理',
  '智慧通行',
  '智慧建筑',
  '智慧楼宇',
  '建筑设备监控',
  '智慧能源',
  '环境监测'
])

/**
 * 过渡期目录标题落地页映射：
 * 后端尚未统一配置 meta.directoryLanding 时，按目录标题指定稳定落地路由。
 */
const legacyDirectoryLandingByTitle = new Map<string, string>([
  ['门禁管理', '/smart-access/door/visual-dashboard'],
  ['智慧通行', '/smart-access/door/visual-dashboard'],
  ['智慧建筑', '/building/visual-dashboard'],
  ['智慧楼宇', '/building/visual-dashboard'],
  ['智慧能源', '/energy/overview'],
  ['智慧安防', '/security/video-surveillance/visual-board']
])

export const useRenderMenuItem = (options?: {
  onDirectoryTitleActivate?: (index: string, event: MouseEvent) => void
}) =>
  // allRouters: AppRouteRecordRaw[] = [],
  {
    const { push, resolve } = useRouter()
    const { t } = useI18n()

    const resolveMetaTitleText = (meta: AppRouteRecordRaw['meta']) => {
      return meta?.title != null ? t(String(meta.title)) : ''
    }

    const shouldHideMenuItem = (
      meta: AppRouteRecordRaw['meta'],
      parentMeta: AppRouteRecordRaw['meta']
    ) => {
      const parentTitle = resolveMetaTitleText(parentMeta)
      const title = resolveMetaTitleText(meta)
      return parentTitle === '访客管理' && title === '首页'
    }

    const shouldNavigateDirectoryTitle = (meta: AppRouteRecordRaw['meta']) => {
      if (!meta) return false
      const metaAny = meta as any
      if (metaAny.directoryClickable === true) return true
      if (metaAny.directoryLanding || metaAny.directoryPermission) return true
      const titleText = resolveMetaTitleText(meta)
      return legacyClickableDirectoryTitles.has(titleText)
    }

    const keepDirectoryTitleFocus = async (event: MouseEvent) => {
      await nextTick()
      ;(event.currentTarget as HTMLElement | null)?.focus?.()
    }

    const canResolveAsRealRoute = (targetPath: string) => {
      if (!targetPath || isUrl(targetPath)) return true
      const resolved = resolve(targetPath)
      if (!resolved.matched.length) return false
      const last = resolved.matched[resolved.matched.length - 1]
      if (!last) return false
      const recordName = String(last.name || '')
      if (
        last.path === '/:pathMatch(.*)*' ||
        last.path === '/:path(.*)*' ||
        recordName === '404Page' ||
        recordName === 'NoFound'
      ) {
        return false
      }
      const routeComponent = (last.components as any)?.default || (last as any).component
      const componentText = typeof routeComponent === 'function' ? routeComponent.toString() : ''
      return !componentText.includes('Error/404.vue')
    }

    const navigate = async (
      event: MouseEvent,
      path: string,
      meta: AppRouteRecordRaw['meta'],
      route: AppRouteRecordRaw
    ) => {
      // 不再阻止事件冒泡，保证点击目录标题时，ElSubMenu 仍然可以正常展开子菜单
      const metaAny = meta as any
      // 优先级：meta.directoryPermission -> meta.directoryLanding / landingName（兼容老字段）
      //        -> resolveFirstNavigableIndex（首个可见叶子兜底）
      const permissionKey = metaAny.directoryPermission as string | undefined
      const fromPermission = permissionKey ? resolvePathByMenuPermission(permissionKey) : undefined

      const landingName =
        (metaAny.directoryLanding as string | undefined) ||
        (metaAny.landingName as string | undefined)
      const fromLandingPath =
        !fromPermission && landingName?.startsWith('/') ? landingName : undefined
      const fromLanding =
        !fromPermission && !fromLandingPath && landingName ? findPathByMenuName(landingName) : undefined
      const fromLegacyLanding = legacyDirectoryLandingByTitle.get(resolveMetaTitleText(meta))

      const rawTargetPath = fromPermission
        ? fromPermission
        : fromLandingPath
          ? fromLandingPath
        : fromLanding
          ? fromLanding.startsWith('/')
            ? fromLanding
            : pathResolve(path, fromLanding)
          : fromLegacyLanding
            ? fromLegacyLanding
          : resolveFirstNavigableIndex(route, path) || path

      // 目录标题映射的目标路由可能不在当前目录子树内（例如统一可视化页放在静态路由），
      // 因此按“全局可解析真实路由”校验，不可达再回退首个可见叶子。
      const targetPath = canResolveAsRealRoute(rawTargetPath)
        ? rawTargetPath
        : resolveFirstNavigableIndex(route, path) || rawTargetPath

      if (isUrl(targetPath)) {
        window.open(targetPath)
      } else {
        await push(targetPath)
        await keepDirectoryTitleFocus(event)
      }
    }

    const resolveFirstNavigableIndex = (route: AppRouteRecordRaw, routeFullPath: string) => {
      const resolveFromNode = (node: AppRouteRecordRaw, parentFullPath: string): string | undefined => {
        if (node?.meta?.hidden) return undefined

        const nodeFullPath = isUrl(node.path) ? node.path : pathResolve(parentFullPath, node.path)
        if (isUrl(nodeFullPath)) return nodeFullPath

        const { oneShowingChild, onlyOneChild } = hasOneShowingChild(node.children, node)
        const meta = node.meta ?? {}

        if (
          oneShowingChild &&
          (!onlyOneChild?.children || onlyOneChild?.noShowingChildren) &&
          !meta?.alwaysShow
        ) {
          return onlyOneChild ? pathResolve(nodeFullPath, onlyOneChild.path) : nodeFullPath
        }

        const children = (node.children ?? []).filter((v) => !v.meta?.hidden)
        if (!children.length) return nodeFullPath
        for (const child of children) {
          const hit = resolveFromNode(child, nodeFullPath)
          if (hit) return hit
        }
        return undefined
      }

      const children = (route.children ?? []).filter((v) => !v.meta?.hidden)
      if (!children.length) return undefined
      for (const child of children) {
        const hit = resolveFromNode(child, routeFullPath)
        if (hit) return hit
      }
      return undefined
    }

    const handleDirectoryTitleClick = (
      event: MouseEvent,
      fullPath: string,
      meta: AppRouteRecordRaw['meta'],
      route: AppRouteRecordRaw
    ) => {
      options?.onDirectoryTitleActivate?.(fullPath, event)
      if (shouldNavigateDirectoryTitle(meta)) {
        void navigate(event, fullPath, meta, route)
        return
      }
      const firstIndex = resolveFirstNavigableIndex(route, fullPath)
      if (!firstIndex) return
      if (isUrl(firstIndex)) {
        window.open(firstIndex)
      } else {
        void push(firstIndex).then(() => keepDirectoryTitleFocus(event))
      }
    }

    const renderMenuItem = (
      routers: AppRouteRecordRaw[],
      parentPath = '/',
      parentRoute?: AppRouteRecordRaw
    ) => {
      return routers
        .filter((v) => !v.meta?.hidden)
        .map((v) => {
          const meta = v.meta ?? {}
          const { oneShowingChild, onlyOneChild } = hasOneShowingChild(v.children, v)
          const fullPath = isUrl(v.path) ? v.path : pathResolve(parentPath, v.path) // getAllParentPath<AppRouteRecordRaw>(allRouters, v.path).join('/')

          if (
            oneShowingChild &&
            (!onlyOneChild?.children || onlyOneChild?.noShowingChildren) &&
            !meta?.alwaysShow
          ) {
            const menuItemMeta = (onlyOneChild ? onlyOneChild?.meta : meta) as AppRouteRecordRaw['meta']
            const menuItemClass = shouldHideMenuItem(menuItemMeta, parentRoute?.meta) ? 'v-menu__item--hidden' : ''
            return (
              <ElMenuItem
                index={onlyOneChild ? pathResolve(fullPath, onlyOneChild.path) : fullPath}
                class={menuItemClass}
              >
                {{
                  default: () => renderMenuTitle(onlyOneChild ? onlyOneChild?.meta : meta)
                }}
              </ElMenuItem>
            )
          } else {
            return (
              <ElSubMenu index={fullPath}>
                {{
                  title: () =>
                    v.children && v.children.length ? (
                      <span
                        tabIndex={0}
                        onClick={(event) =>
                          handleDirectoryTitleClick(event as MouseEvent, fullPath, meta, v)
                        }
                      >
                        {renderMenuTitle(meta)}
                      </span>
                    ) : (
                      renderMenuTitle(meta)
                    ),
                  default: () => renderMenuItem(v.children!, fullPath, v)
                }}
              </ElSubMenu>
            )
          }
        })
    }

    return {
      renderMenuItem
    }
  }
