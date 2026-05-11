/**
 * 住宅物业收费模块公共工具函数
 * @author zhaoxinms
 */

/**
 * 金额格式化：千分位 + 保留2位小数
 * @param {number|string} val 金额值
 * @returns {string} 格式化后的金额字符串
 */
export function formatMoney(val) {
  if (val === null || val === undefined || val === '') return '—'
  return Number(val).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

/**
 * 缴费状态 → 中文标签
 * @param {string} payState 缴费状态码
 * @returns {string}
 */
export function payStateLabel(payState) {
  const map = {
    '0': '未收款',
    '1': '部分收款',
    '2': '已收清',
    '3': '已减免'
  }
  return map[payState] || payState
}

/**
 * 缴费状态 → Element UI tag 类型
 * @param {string} payState 缴费状态码
 * @returns {string} success / warning / danger / info
 */
export function payStateTagType(payState) {
  const map = {
    '0': 'danger',
    '1': 'warning',
    '2': 'success',
    '3': 'info'
  }
  return map[payState] || ''
}

/**
 * 费用类型 → 中文标签
 * @param {string} feeType PERIOD/TEMP/DEPOSIT/PRE
 * @returns {string}
 */
export function feeTypeLabel(feeType) {
  const map = {
    'PERIOD': '周期费',
    'TEMP': '临时费',
    'DEPOSIT': '押金',
    'PRE': '预收款'
  }
  return map[feeType] || feeType
}

/**
 * 费用类型 → Element UI tag 类型
 * @param {string} feeType PERIOD/TEMP/DEPOSIT/PRE
 * @returns {string}
 */
export function feeTypeTagType(feeType) {
  const map = {
    'PERIOD': 'primary',
    'TEMP': 'warning',
    'DEPOSIT': 'danger',
    'PRE': 'success'
  }
  return map[feeType] || ''
}

/**
 * 计费方式 → 中文标签
 * @param {string} calcType FIXED/AREA/USAGE/FORMULA
 * @returns {string}
 */
export function calcTypeLabel(calcType) {
  const map = {
    'FIXED': '固定金额',
    'AREA': '按面积',
    'USAGE': '按用量',
    'FORMULA': '自定义公式'
  }
  return map[calcType] || calcType
}

/**
 * 计费方式 → Element UI tag 类型
 * @param {string} calcType FIXED/AREA/USAGE/FORMULA
 * @returns {string}
 */
export function calcTypeTagType(calcType) {
  const map = {
    'FIXED': '',
    'AREA': 'success',
    'USAGE': 'warning',
    'FORMULA': 'danger'
  }
  return map[calcType] || ''
}

/**
 * 抄表状态 → 中文标签
 * @param {string} status INPUT/BILLED/VERIFIED
 * @returns {string}
 */
export function meterStatusLabel(status) {
  const map = {
    'INPUT': '已录入',
    'BILLED': '已入账',
    'VERIFIED': '已复核'
  }
  return map[status] || status
}

/**
 * 抄表状态 → Element UI tag 类型
 * @param {string} status
 * @returns {string}
 */
export function meterStatusTagType(status) {
  const map = {
    'INPUT': 'info',
    'BILLED': 'success',
    'VERIFIED': ''
  }
  return map[status] || ''
}

/**
 * 支付方式 → 中文标签
 * @param {string} payMethod CASH/WECHAT/TRANSFER/BANK/OTHER
 * @returns {string}
 */
export function payMethodLabel(payMethod) {
  const map = {
    'CASH': '现金',
    'WECHAT': '微信支付',
    'TRANSFER': '银行转账',
    'BANK': '银行代收',
    'OTHER': '其他'
  }
  return map[payMethod] || payMethod
}

/**
 * 房间类型 → 中文标签
 * @param {number} roomType 1/2/3/4
 * @returns {string}
 */
export function roomTypeLabel(roomType) {
  const map = {
    1: '住宅',
    2: '商铺',
    3: '车库',
    4: '储藏室'
  }
  return map[roomType] || roomType
}

/**
 * 客户类型 → 中文标签
 * @param {number} customerType 1/2/3
 * @returns {string}
 */
export function customerTypeLabel(customerType) {
  const map = {
    1: '业主',
    2: '租户',
    3: '临时'
  }
  return map[customerType] || customerType
}

/**
 * 房间使用状态 → 中文标签
 * @param {string} state NORMAL/VACANT/DECORATING/TRANSFERRED
 * @returns {string}
 */
export function roomStateLabel(state) {
  const map = {
    'NORMAL': '正常入住',
    'VACANT': '空置',
    'DECORATING': '装修中',
    'TRANSFERRED': '已过户'
  }
  return map[state] || state
}

/**
 * 仪表类型 → 中文标签
 * @param {number} meterType 1/2/3/4
 * @returns {string}
 */
export function meterTypeLabel(meterType) {
  const map = {
    1: '水表',
    2: '电表',
    3: '燃气表',
    4: '暖气表'
  }
  return map[meterType] || meterType
}

/**
 * 账单月格式化：yyyy-MM
 * @param {Date|string} date
 * @returns {string}
 */
export function formatBillPeriod(date) {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = (d.getMonth() + 1).toString().padStart(2, '0')
  return `${year}-${month}`
}

/**
 * 脱敏手机号：138****8888
 * @param {string} phone
 * @returns {string}
 */
export function maskPhone(phone) {
  if (!phone || phone.length < 7) return phone
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

/**
 * 脱敏身份证号：110***********1234
 * @param {string} idCard
 * @returns {string}
 */
export function maskIdCard(idCard) {
  if (!idCard || idCard.length < 10) return idCard
  return idCard.replace(/(\d{6})\d{8}(\d{4})/, '$1********$2')
}
