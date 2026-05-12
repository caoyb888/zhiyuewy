import request from '@/utils/request'

// ==================== 收银台查询 ====================

export function searchRoom(keyword, projectId) {
  return request({
    url: '/resi/cashier/room/search',
    method: 'get',
    params: { keyword, projectId }
  })
}

export function getRoomReceivables(roomId, params) {
  return request({
    url: '/resi/cashier/room/' + roomId + '/receivables',
    method: 'get',
    params
  })
}

export function getRoomSummary(roomId) {
  return request({
    url: '/resi/cashier/room/' + roomId + '/summary',
    method: 'get'
  })
}

export function getRoomPayLogs(roomId, params) {
  return request({
    url: '/resi/cashier/room/' + roomId + '/pay-logs',
    method: 'get',
    params
  })
}

export function getRoomPreAccounts(roomId) {
  return request({
    url: '/resi/cashier/room/' + roomId + '/pre-accounts',
    method: 'get'
  })
}

export function getRoomDeposits(roomId) {
  return request({
    url: '/resi/cashier/room/' + roomId + '/deposits',
    method: 'get'
  })
}

// ==================== 收款核心 ====================

export function calcCollect(data) {
  return request({
    url: '/resi/cashier/calc',
    method: 'post',
    data
  })
}

export function collectPayment(data) {
  return request({
    url: '/resi/cashier/collect',
    method: 'post',
    data
  })
}

export function getReceiptPrintData(payLogId) {
  return request({
    url: '/resi/print/receipt/' + payLogId,
    method: 'get'
  })
}

export function refundPayment(data) {
  return request({
    url: '/resi/cashier/refund',
    method: 'post',
    data
  })
}

export function writeOffPayment(data) {
  return request({
    url: '/resi/cashier/write-off',
    method: 'post',
    data
  })
}

// ==================== 辅助操作 ====================

export function waiveOverdue(data) {
  return request({
    url: '/resi/cashier/waive-overdue',
    method: 'post',
    data
  })
}

export function earmarkOffset(data) {
  return request({
    url: '/resi/cashier/earmark-offset',
    method: 'post',
    data
  })
}

export function addPrePay(data) {
  return request({
    url: '/resi/cashier/pre-pay/add',
    method: 'post',
    data
  })
}

export function offsetPrePay(data) {
  return request({
    url: '/resi/cashier/pre-pay/offset',
    method: 'post',
    data
  })
}

export function batchOffsetPrePay(data) {
  return request({
    url: '/resi/finance/pre-pay/batch-offset',
    method: 'post',
    data
  })
}
