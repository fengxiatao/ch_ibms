import { ElSubMenu, ElMenuItem } from 'element-plus'
import { useRouter } from 'vue-router'
import { hasOneShowingChild } from '../helper'
import { isUrl } from '@/utils/is'
import { useRenderMenuTitle } from './useRenderMenuTitle'
import { pathResolve } from '@/utils/routerHelper'

const { renderMenuTitle } = useRenderMenuTitle()

const clickableDirectoryTitles = new Set(['门禁管理', '智慧建筑', '智慧能源', '环境监测'])
const directoryTitleNavigateTarget = new Map<string, string>([
  ['门禁管理', 'visual-dashboard'],
  ['智慧建筑', '/building/visual-dashboard'],
  ['智慧能源', '/energy/overview'],
  ['环境监测', '/iot/building/env/overview']
])

export const useRenderMenuItem = () =>
  // allRouters: AppRouteRecordRaw[] = [],
  {
    const { push } = useRouter()

    const shouldHideMenuItem = (
      meta: AppRouteRecordRaw['meta'],
      parentMeta: AppRouteRecordRaw['meta']
    ) => {
      const parentTitle = parentMeta?.title != null ? String(parentMeta.title) : ''
      const title = meta?.title != null ? String(meta.title) : ''
      return parentTitle === '访客管理' && title === '首页'
    }

    const shouldNavigateDirectoryTitle = (meta: AppRouteRecordRaw['meta']) => {
      if (!meta) return false
      return (
        (meta as any).directoryClickable === true ||
        clickableDirectoryTitles.has(String(meta.title))
      )
    }

    const navigate = (event: MouseEvent, path: string, meta: AppRouteRecordRaw['meta']) => {
      // 不再阻止事件冒泡，保证点击目录标题时，ElSubMenu 仍然可以正常展开子菜单
      const target = meta?.title ? directoryTitleNavigateTarget.get(String(meta.title)) : undefined
      const targetPath = target
        ? target.startsWith('/')
          ? target
          : pathResolve(path, target)
        : path
      if (isUrl(targetPath)) {
        window.open(targetPath)
      } else {
        push(targetPath)
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
      if (shouldNavigateDirectoryTitle(meta)) {
        navigate(event, fullPath, meta)
        return
      }
      const firstIndex = resolveFirstNavigableIndex(route, fullPath)
      if (!firstIndex) return
      if (isUrl(firstIndex)) {
        window.open(firstIndex)
      } else {
        push(firstIndex)
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
