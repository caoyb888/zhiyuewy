import request from '@/utils/request'

// ==================== 收款流水 ====================

export function listPayLog(query) {
  return request({
    url: '/resi/finance/pay-log/list',
    method: 'get',
    params: query
  })
}

export function getPayLog(id) {
  return request({
    url: '/resi/finance/pay-log/' + id,
    method: 'get'
  })
}

export function verifyPayLog(id) {
  return request({
    url: '/resi/finance/pay-log/' + id + '/verify',
    method: 'post'
  })
}

// ==================== 预收款 ====================

export function listPreAccount(query) {
  return request({
    url: '/resi/finance/pre-account',
    method: 'get',
    params: query
  })
}

export function listPrePay(query) {
  return request({
    url: '/resi/finance/pre-pay',
    method: 'get',
    params: query
  })
}

// ==================== 押金 ====================

export function listDeposit(query) {
  return request({
    url: '/resi/finance/deposit',
    method: 'get',
    params: query
  })
}

export function refundDeposit(id, data) {
  return request({
    url: '/resi/finance/deposit/' + id + '/refund',
    method: 'put',
    data
  })
}

// ==================== 发票 ====================

export function listInvoice(query) {
  return request({
    url: '/resi/finance/invoice',
    method: 'get',
    params: query
  })
}

export function exchangeInvoice(data) {
  return request({
    url: '/resi/finance/invoice/exchange',
    method: 'post',
    data
  })
}

// ==================== 调账记录 ====================

export function listAdjustLog(query) {
  return request({
    url: '/resi/finance/adjust-log',
    method: 'get',
    params: query
  })
}

// ==================== 打印 ====================

export function getReceiptData(payLogId) {
  return request({
    url: '/resi/print/receipt/' + payLogId,
    method: 'get'
  })
}

export function getNoticeData(receivableId) {
  return request({
    url: '/resi/print/notice/' + receivableId,
    method: 'get'
  })
}

export function batchNoticeData(data) {
  return request({
    url: '/resi/print/notice/batch',
    method: 'post',
    data
  })
}
