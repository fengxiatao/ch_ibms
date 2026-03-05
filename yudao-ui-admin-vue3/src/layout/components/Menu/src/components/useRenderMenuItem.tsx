import { ElSubMenu, ElMenuItem } from 'element-plus'
import { useRouter } from 'vue-router'
import { hasOneShowingChild } from '../helper'
import { isUrl } from '@/utils/is'
import { useRenderMenuTitle } from './useRenderMenuTitle'
import { pathResolve } from '@/utils/routerHelper'

const { renderMenuTitle } = useRenderMenuTitle()

const clickableDirectoryTitles = new Set(['门禁管理', '智慧建筑', '智慧能源'])
const directoryTitleNavigateTarget = new Map<string, string>([
  ['门禁管理', 'visual-dashboard'],
  ['智慧建筑', '/building/visual-dashboard'],
  ['智慧能源', '/energy/overview']
])

export const useRenderMenuItem = () =>
  // allRouters: AppRouteRecordRaw[] = [],
  {
    const { push } = useRouter()

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

    const renderMenuItem = (routers: AppRouteRecordRaw[], parentPath = '/') => {
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
            return (
              <ElMenuItem
                index={onlyOneChild ? pathResolve(fullPath, onlyOneChild.path) : fullPath}
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
                    shouldNavigateDirectoryTitle(meta) ? (
                      <span onClick={(event) => navigate(event as MouseEvent, fullPath, meta)}>
                        {renderMenuTitle(meta)}
                      </span>
                    ) : (
                      renderMenuTitle(meta)
                    ),
                  default: () => renderMenuItem(v.children!, fullPath)
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
