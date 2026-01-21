/**
* v-hasPermi 操作权限处理
* Copyright (c) 2019 ruoyi
*/

import store from '@/store/index.js'

export default {
  inserted(el, binding, vnode) {
    const { value } = binding
    console.log(value);
    const all_permission = "*:*:*";
    const permissions = store.getters && store.getters.permissions

    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some(res => {

        return all_permission === res.permission || permissionFlag.includes(res.permission)
      })
      console.log(hasPermissions);
      if (!hasPermissions) {
        console.log(123);
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(`请设置操作权限标签值`)
    }
  }
}
