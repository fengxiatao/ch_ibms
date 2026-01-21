import request from '@/config/axios'

// 停车场API

// ========== 车场管理 ==========
export const ParkingLotApi = {
  // 获得车场分页
  getParkingLotPage: async (params: any) => {
    return await request.get({ url: '/iot/parking/lot/page', params })
  },
  // 获得车场
  getParkingLot: async (id: number) => {
    return await request.get({ url: '/iot/parking/lot/get', params: { id } })
  },
  // 创建车场
  createParkingLot: async (data: any) => {
    return await request.post({ url: '/iot/parking/lot/create', data })
  },
  // 更新车场
  updateParkingLot: async (data: any) => {
    return await request.put({ url: '/iot/parking/lot/update', data })
  },
  // 删除车场
  deleteParkingLot: async (id: number) => {
    return await request.delete({ url: '/iot/parking/lot/delete', params: { id } })
  },
  // 获得车场精简列表
  getSimpleList: async () => {
    return await request.get({ url: '/iot/parking/lot/simple-list' })
  }
}

// ========== 道闸设备管理 ==========
export const ParkingGateApi = {
  getParkingGatePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/gate/page', params })
  },
  getParkingGate: async (id: number) => {
    return await request.get({ url: '/iot/parking/gate/get', params: { id } })
  },
  createParkingGate: async (data: any) => {
    return await request.post({ url: '/iot/parking/gate/create', data })
  },
  updateParkingGate: async (data: any) => {
    return await request.put({ url: '/iot/parking/gate/update', data })
  },
  deleteParkingGate: async (id: number) => {
    return await request.delete({ url: '/iot/parking/gate/delete', params: { id } })
  },
  getListByLotId: async (lotId: number) => {
    return await request.get({ url: '/iot/parking/gate/list-by-lot', params: { lotId } })
  }
}

// ========== 车道管理 ==========
export const ParkingLaneApi = {
  getParkingLanePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/lane/page', params })
  },
  getParkingLane: async (id: number) => {
    return await request.get({ url: '/iot/parking/lane/get', params: { id } })
  },
  createParkingLane: async (data: any) => {
    return await request.post({ url: '/iot/parking/lane/create', data })
  },
  updateParkingLane: async (data: any) => {
    return await request.put({ url: '/iot/parking/lane/update', data })
  },
  deleteParkingLane: async (id: number) => {
    return await request.delete({ url: '/iot/parking/lane/delete', params: { id } })
  },
  getListByLotId: async (lotId: number) => {
    return await request.get({ url: '/iot/parking/lane/list-by-lot', params: { lotId } })
  }
}

// ========== 免费车管理 ==========
export const ParkingFreeVehicleApi = {
  getFreeVehiclePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/vehicle/free/page', params })
  },
  getFreeVehicle: async (id: number) => {
    return await request.get({ url: '/iot/parking/vehicle/free/get', params: { id } })
  },
  createFreeVehicle: async (data: any) => {
    return await request.post({ url: '/iot/parking/vehicle/free/create', data })
  },
  updateFreeVehicle: async (data: any) => {
    return await request.put({ url: '/iot/parking/vehicle/free/update', data })
  },
  deleteFreeVehicle: async (id: number) => {
    return await request.delete({ url: '/iot/parking/vehicle/free/delete', params: { id } })
  }
}

// ========== 月租车管理 ==========
export const ParkingMonthlyVehicleApi = {
  getMonthlyVehiclePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/vehicle/monthly/page', params })
  },
  getMonthlyVehicle: async (id: number) => {
    return await request.get({ url: '/iot/parking/vehicle/monthly/get', params: { id } })
  },
  createMonthlyVehicle: async (data: any) => {
    return await request.post({ url: '/iot/parking/vehicle/monthly/create', data })
  },
  updateMonthlyVehicle: async (data: any) => {
    return await request.put({ url: '/iot/parking/vehicle/monthly/update', data })
  },
  deleteMonthlyVehicle: async (id: number) => {
    return await request.delete({ url: '/iot/parking/vehicle/monthly/delete', params: { id } })
  },
  rechargeMonthlyVehicle: async (id: number, months: number, paidAmount: number, paymentMethod: string) => {
    return await request.post({ 
      url: '/iot/parking/vehicle/monthly/recharge', 
      params: { id, months, paidAmount, paymentMethod } 
    })
  }
}

// ========== 收费规则管理 ==========
export const ParkingChargeRuleApi = {
  getChargeRulePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/charge-rule/page', params })
  },
  getChargeRule: async (id: number) => {
    return await request.get({ url: '/iot/parking/charge-rule/get', params: { id } })
  },
  createChargeRule: async (data: any) => {
    return await request.post({ url: '/iot/parking/charge-rule/create', data })
  },
  updateChargeRule: async (data: any) => {
    return await request.put({ url: '/iot/parking/charge-rule/update', data })
  },
  deleteChargeRule: async (id: number) => {
    return await request.delete({ url: '/iot/parking/charge-rule/delete', params: { id } })
  },
  getSimpleList: async () => {
    return await request.get({ url: '/iot/parking/charge-rule/simple-list' })
  }
}

// ========== 放行规则管理 ==========
export const ParkingPassRuleApi = {
  getPassRulePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/pass-rule/page', params })
  },
  getPassRule: async (id: number) => {
    return await request.get({ url: '/iot/parking/pass-rule/get', params: { id } })
  },
  createPassRule: async (data: any) => {
    return await request.post({ url: '/iot/parking/pass-rule/create', data })
  },
  updatePassRule: async (data: any) => {
    return await request.put({ url: '/iot/parking/pass-rule/update', data })
  },
  deletePassRule: async (id: number) => {
    return await request.delete({ url: '/iot/parking/pass-rule/delete', params: { id } })
  }
}

// ========== 收费规则应用 ==========
export const ParkingChargeRuleApplyApi = {
  getChargeRuleApplyPage: async (params: any) => {
    return await request.get({ url: '/iot/parking/charge-rule-apply/page', params })
  },
  getChargeRuleApply: async (id: number) => {
    return await request.get({ url: '/iot/parking/charge-rule-apply/get', params: { id } })
  },
  createChargeRuleApply: async (data: any) => {
    return await request.post({ url: '/iot/parking/charge-rule-apply/create', data })
  },
  updateChargeRuleApply: async (data: any) => {
    return await request.put({ url: '/iot/parking/charge-rule-apply/update', data })
  },
  deleteChargeRuleApply: async (id: number) => {
    return await request.delete({ url: '/iot/parking/charge-rule-apply/delete', params: { id } })
  }
}

// ========== 停车记录管理 ==========
export const ParkingRecordApi = {
  // 在场车辆
  getPresentVehiclePage: async (params: any) => {
    return await request.get({ url: '/iot/parking/record/present/page', params })
  },
  getPresentVehicle: async (id: number) => {
    return await request.get({ url: '/iot/parking/record/present/get', params: { id } })
  },
  forceExit: async (id: number, remark?: string) => {
    return await request.post({ url: '/iot/parking/record/present/force-exit', params: { id, remark } })
  },
  // 进出记录
  getRecordPage: async (params: any) => {
    return await request.get({ url: '/iot/parking/record/page', params })
  },
  getRecord: async (id: number) => {
    return await request.get({ url: '/iot/parking/record/get', params: { id } })
  },
  // 计算费用
  calculateFee: async (plateNumber: string, lotId: number) => {
    return await request.get({ url: '/iot/parking/record/calculate-fee', params: { plateNumber, lotId } })
  },
  // 入场出场
  vehicleEntry: async (data: any) => {
    return await request.post({ url: '/iot/parking/record/entry', params: data })
  },
  vehicleExit: async (data: any) => {
    return await request.post({ url: '/iot/parking/record/exit', params: data })
  }
}

// ========== 月卡充值记录 ==========
export const ParkingRechargeRecordApi = {
  getRechargeRecordPage: async (params: any) => {
    return await request.get({ url: '/iot/parking/recharge-record/page', params })
  },
  getRechargeStatistics: async () => {
    return await request.get({ url: '/iot/parking/recharge-record/statistics' })
  }
}

// ========== 停车退款管理 ==========
export const ParkingRefundApi = {
  // 获得退款记录分页
  getRefundRecordPage: async (params: any) => {
    return await request.get({ url: '/iot/parking/refund/page', params })
  },
  // 获得退款记录详情
  getRefundRecord: async (id: number) => {
    return await request.get({ url: '/iot/parking/refund/get', params: { id } })
  },
  // 申请退款
  applyRefund: async (data: any) => {
    return await request.post({ url: '/iot/parking/refund/apply', data })
  },
  // 执行退款
  executeRefund: async (id: number) => {
    return await request.post({ url: '/iot/parking/refund/execute', params: { id } })
  },
  // 关闭退款
  closeRefund: async (id: number, reason: string) => {
    return await request.post({ url: '/iot/parking/refund/close', params: { id, reason } })
  },
  // 同步退款状态
  syncRefundStatus: async (id: number) => {
    return await request.post({ url: '/iot/parking/refund/sync', params: { id } })
  }
}

// 退款状态选项
export const RefundStatusOptions = [
  { label: '申请中', value: 0 },
  { label: '退款成功', value: 1 },
  { label: '退款失败', value: 2 },
  { label: '退款关闭', value: 3 }
]

// 常量定义
export const LotTypeOptions = [
  { label: '收费', value: 1 },
  { label: '免费', value: 2 }
]

export const DirectionOptions = [
  { label: '入口', value: 1 },
  { label: '出口', value: 2 },
  { label: '出入口', value: 3 }
]

export const GateTypeOptions = [
  { label: '车牌识别一体机', value: 1 },
  { label: '普通道闸', value: 2 }
]

export const VehicleTypeOptions = [
  { label: '小型车', value: 1 },
  { label: '中型车', value: 2 },
  { label: '新能源车', value: 3 },
  { label: '大型车', value: 4 },
  { label: '超大型车', value: 5 }
]

export const ChargeModeOptions = [
  { label: '按次收费', value: 1 },
  { label: '按时收费', value: 2 }
]

export const PaymentMethodOptions = [
  { label: '现金', value: 'cash' },
  { label: '微信', value: 'wechat' },
  { label: '支付宝', value: 'alipay' },
  { label: '刷卡', value: 'card' }
]

export const VehicleCategoryOptions = [
  { label: '免费车', value: 'free' },
  { label: '月租车', value: 'monthly' },
  { label: '临时车', value: 'temporary' }
]
