/**
 * 微信小程序特定 API 封装
 * Requirements 9.3: 处理微信特定 API，包括位置和支付
 */

/**
 * 扫描二维码
 * @param {Object} options - 扫码配置
 * @param {Boolean} options.onlyFromCamera - 是否只允许从相机扫码，默认 true
 * @param {Array} options.scanType - 扫码类型，默认 ['qrCode', 'barCode']
 * @returns {Promise} 扫码结果
 */
export function scanCode(options = {}) {
  return new Promise((resolve, reject) => {
    uni.scanCode({
      onlyFromCamera: options.onlyFromCamera !== false,
      scanType: options.scanType || ['qrCode', 'barCode'],
      success: (res) => {
        console.log('扫码成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('扫码失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 微信支付
 * @param {Object} paymentData - 支付参数
 * @param {String} paymentData.timeStamp - 时间戳
 * @param {String} paymentData.nonceStr - 随机字符串
 * @param {String} paymentData.package - 统一下单接口返回的 prepay_id
 * @param {String} paymentData.signType - 签名算法
 * @param {String} paymentData.paySign - 签名
 * @returns {Promise} 支付结果
 */
export function requestPayment(paymentData) {
  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
    wx.requestPayment({
      timeStamp: paymentData.timeStamp,
      nonceStr: paymentData.nonceStr,
      package: paymentData.package,
      signType: paymentData.signType || 'MD5',
      paySign: paymentData.paySign,
      success: (res) => {
        console.log('支付成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('支付失败:', err)
        reject(err)
      }
    })
    // #endif
    
    // #ifndef MP-WEIXIN
    reject(new Error('当前环境不支持微信支付'))
    // #endif
  })
}

/**
 * 获取用户位置
 * @param {Object} options - 位置配置
 * @param {String} options.type - 坐标类型，默认 'gcj02'
 * @returns {Promise} 位置信息
 */
export function getLocation(options = {}) {
  return new Promise((resolve, reject) => {
    uni.getLocation({
      type: options.type || 'gcj02',
      success: (res) => {
        console.log('获取位置成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('获取位置失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 选择位置
 * @returns {Promise} 选择的位置信息
 */
export function chooseLocation() {
  return new Promise((resolve, reject) => {
    uni.chooseLocation({
      success: (res) => {
        console.log('选择位置成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('选择位置失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 打开地图导航
 * @param {Object} options - 导航配置
 * @param {Number} options.latitude - 纬度
 * @param {Number} options.longitude - 经度
 * @param {String} options.name - 位置名称
 * @param {String} options.address - 详细地址
 * @returns {Promise} 导航结果
 */
export function openLocation(options) {
  return new Promise((resolve, reject) => {
    uni.openLocation({
      latitude: options.latitude,
      longitude: options.longitude,
      name: options.name || '',
      address: options.address || '',
      scale: options.scale || 18,
      success: (res) => {
        console.log('打开地图成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('打开地图失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 获取用户信息（需要用户授权）
 * @returns {Promise} 用户信息
 */
export function getUserProfile() {
  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: (res) => {
        console.log('获取用户信息成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('获取用户信息失败:', err)
        reject(err)
      }
    })
    // #endif
    
    // #ifndef MP-WEIXIN
    uni.getUserInfo({
      success: (res) => {
        resolve(res)
      },
      fail: (err) => {
        reject(err)
      }
    })
    // #endif
  })
}

/**
 * 微信登录获取 code
 * @returns {Promise} 登录凭证
 */
export function wxLogin() {
  return new Promise((resolve, reject) => {
    uni.login({
      provider: 'weixin',
      success: (res) => {
        console.log('微信登录成功:', res)
        resolve(res)
      },
      fail: (err) => {
        console.log('微信登录失败:', err)
        reject(err)
      }
    })
  })
}

/**
 * 检查会话是否过期
 * @returns {Promise} 会话状态
 */
export function checkSession() {
  return new Promise((resolve, reject) => {
    // #ifdef MP-WEIXIN
    wx.checkSession({
      success: () => {
        resolve(true)
      },
      fail: () => {
        resolve(false)
      }
    })
    // #endif
    
    // #ifndef MP-WEIXIN
    resolve(true)
    // #endif
  })
}

/**
 * 显示分享菜单
 * @param {Object} options - 分享配置
 */
export function showShareMenu(options = {}) {
  // #ifdef MP-WEIXIN
  wx.showShareMenu({
    withShareTicket: options.withShareTicket || false,
    menus: options.menus || ['shareAppMessage', 'shareTimeline']
  })
  // #endif
}

/**
 * 拨打电话
 * @param {String} phoneNumber - 电话号码
 * @returns {Promise}
 */
export function makePhoneCall(phoneNumber) {
  return new Promise((resolve, reject) => {
    uni.makePhoneCall({
      phoneNumber: phoneNumber,
      success: (res) => {
        resolve(res)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

/**
 * 复制文本到剪贴板
 * @param {String} data - 要复制的文本
 * @returns {Promise}
 */
export function setClipboardData(data) {
  return new Promise((resolve, reject) => {
    uni.setClipboardData({
      data: data,
      success: (res) => {
        resolve(res)
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

export default {
  scanCode,
  requestPayment,
  getLocation,
  chooseLocation,
  openLocation,
  getUserProfile,
  wxLogin,
  checkSession,
  showShareMenu,
  makePhoneCall,
  setClipboardData
}
