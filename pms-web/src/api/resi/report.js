import request from '@/utils/request'

// ==================== 核心报表（5张） ====================

export function transactionSummary(query) {
  return request({
    url: '/resi/report/transaction-summary',
    method: 'get',
    params: query
  })
}

export function transactionDetail(query) {
  return request({
    url: '/resi/report/transaction-detail',
    method: 'get',
    params: query
  })
}

export function collectionRate(query) {
  return request({
    url: '/resi/report/collection-rate',
    method: 'get',
    params: query
  })
}

export function arrearsDetail(query) {
  return request({
    url: '/resi/report/arrears-detail',
    method: 'get',
    params: query
  })
}

export function receivableMgmt(query) {
  return request({
    url: '/resi/report/receivable-mgmt',
    method: 'get',
    params: query
  })
}

// ==================== 其他报表 ====================

export function comprehensive(query) {
  return request({
    url: '/resi/report/comprehensive',
    method: 'get',
    params: query
  })
}

export function transactionLog(query) {
  return request({
    url: '/resi/report/transaction-log',
    method: 'get',
    params: query
  })
}

export function chargeDetail(query) {
  return request({
    url: '/resi/report/charge-detail',
    method: 'get',
    params: query
  })
}

export function discountDetail(query) {
  return request({
    url: '/resi/report/discount-detail',
    method: 'get',
    params: query
  })
}

export function prepayDetail(query) {
  return request({
    url: '/resi/report/prepay-detail',
    method: 'get',
    params: query
  })
}

export function clearanceSummary(query) {
  return request({
    url: '/resi/report/clearance-summary',
    method: 'get',
    params: query
  })
}

export function clearanceRate(query) {
  return request({
    url: '/resi/report/clearance-rate',
    method: 'get',
    params: query
  })
}

export function transferQuery(query) {
  return request({
    url: '/resi/report/transfer-query',
    method: 'get',
    params: query
  })
}

export function remindDetail(query) {
  return request({
    url: '/resi/report/remind-detail',
    method: 'get',
    params: query
  })
}

export function feeStatusMatrix(query) {
  return request({
    url: '/resi/report/fee-status-matrix',
    method: 'get',
    params: query
  })
}

export function invoiceStat(query) {
  return request({
    url: '/resi/report/invoice-stat',
    method: 'get',
    params: query
  })
}

export function depositRecord(query) {
  return request({
    url: '/resi/report/deposit-record',
    method: 'get',
    params: query
  })
}

export function dailySettle(query) {
  return request({
    url: '/resi/report/daily-settle',
    method: 'get',
    params: query
  })
}

export function adjustLogReport(query) {
  return request({
    url: '/resi/report/adjust-log',
    method: 'get',
    params: query
  })
}

export function bankTrust(query) {
  return request({
    url: '/resi/report/bank-trust',
    method: 'get',
    params: query
  })
}

// ==================== 导出通用方法 ====================

export function exportReport(url, query, fileName) {
  return request({
    url: url,
    method: 'get',
    params: { ...query, export: true },
    responseType: 'blob'
  }).then(response => {
    const blob = new Blob([response.data])
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = fileName + '.xlsx'
    link.click()
    URL.revokeObjectURL(link.href)
  })
}
