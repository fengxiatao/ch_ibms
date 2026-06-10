import { defineStore } from 'pinia'
import { store } from '../index'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { LocaleDropdownType } from '@/types/localeDropdown'

const { wsCache } = useCache()
interface LocaleState {
  currentLocale: LocaleDropdownType
  localeMap: LocaleDropdownType[]
}

export const useLocaleStore = defineStore('locales', {
  state: (): LocaleState => {
    return {
      currentLocale: {
        lang: 'zh-CN',
        elLocale: zhCn
      },
      // 本项目只支持简体中文
      localeMap: [
        {
          lang: 'zh-CN',
          name: '简体中文'
        }
      ]
    }
  },
  getters: {
    getCurrentLocale(): LocaleDropdownType {
      return this.currentLocale
    },
    getLocaleMap(): LocaleDropdownType[] {
      return this.localeMap
    }
  },
  actions: {
    setCurrentLocale(_localeMap?: LocaleDropdownType) {
      // 本项目禁用英文界面，任何语言切换都回落到简体中文
      this.currentLocale.lang = 'zh-CN'
      this.currentLocale.elLocale = zhCn
      wsCache.set(CACHE_KEY.LANG, 'zh-CN')
    }
  }
})

export const useLocaleStoreWithOut = () => {
  return useLocaleStore(store)
}
