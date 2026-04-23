import { ElSubMenu, ElMenuItem } from 'element-plus'
import { nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { hasOneShowingChild } from '../helper'
import { isUrl } from '@/utils/is'
import { useRenderMenuTitle } from './useRenderMenuTitle'
import { pathResolve } from '@/utils/routerHelper'
import { useI18n } from '@/hooks/web/useI18n'

const { renderMenuTitle } = useRenderMenuTitle()

const clickableDirectoryTitles = new Set([
  '门禁管理',
  '智慧通行',
  '智慧建筑',
  '智慧楼宇',
  '建筑设备监控',
  '智慧能源',
  '环境监测'
])
const directoryTitleNavigateTarget = new Map<string, string>([
  ['门禁管理', '/smart-access/door/management'],
  ['智慧通行', '/smart-access/door/management'],
  ['智慧建筑', '/iot/building/visual-dashboard'],
  ['智慧楼宇', '/iot/building/visual-dashboard'],
  ['建筑设备监控', '/iot/building/visual-dashboard'],
  ['智慧能源', '/building/newlight/overview'],
  ['环境监测', '/iot/building/env/overview']
])

export const useRenderMenuItem = (options?: {
  onDirectoryTitleActivate?: (index: string, event: MouseEvent) => void
}) =>
  // allRouters: AppRouteRecordRaw[] = [],
  {
    const { push } = useRouter()
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
      const titleText = resolveMetaTitleText(meta)
      return (
        (meta as any).directoryClickable === true ||
        clickableDirectoryTitles.has(titleText)
      )
    }

    const keepDirectoryTitleFocus = async (event: MouseEvent) => {
      await nextTick()
      ;(event.currentTarget as HTMLElement | null)?.focus?.()
    }

    const hasRouteInCurrentTree = (
      targetPath: string,
      route: AppRouteRecordRaw,
      parentFullPath: string
    ) => {
      const normalizedTargetPath = targetPath.replace(/\/+$/, '') || '/'
      const walk = (node: AppRouteRecordRaw, nodeParentPath: string): boolean => {
        const nodeFullPath = isUrl(node.path) ? node.path : pathResolve(nodeParentPath, node.path)
        const normalizedNodePath = nodeFullPath.replace(/\/+$/, '') || '/'
        if (normalizedNodePath === normalizedTargetPath) return true
        const children = node.children ?? []
        return children.some((child) => walk(child, nodeFullPath))
      }
      return walk(route, parentFullPath)
    }

    const navigate = async (
      event: MouseEvent,
      path: string,
      meta: AppRouteRecordRaw['meta'],
      route: AppRouteRecordRaw
    ) => {
      // 不再阻止事件冒泡，保证点击目录标题时，ElSubMenu 仍然可以正常展开子菜单
      const titleText = resolveMetaTitleText(meta)
      const target = titleText ? directoryTitleNavigateTarget.get(titleText) : undefined
      const rawTargetPath = target
        ? target.startsWith('/')
          ? target
          : pathResolve(path, target)
        : path

      // 目录标题映射的目标路由可能不在当前租户权限内，优先校验可访问性，不可访问则回退到首个可访问子菜单
      const targetPath = hasRouteInCurrentTree(rawTargetPath, route, path)
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
