import request from '@/utils/request'

// ==================== 费用定义 ====================

export function listFeeDefinition(query) {
  return request({
    url: '/resi/feeconfig/definition',
    method: 'get',
    params: query
  })
}

export function getFeeDefinition(id) {
  return request({
    url: '/resi/feeconfig/definition/' + id,
    method: 'get'
  })
}

export function addFeeDefinition(data) {
  return request({
    url: '/resi/feeconfig/definition',
    method: 'post',
    data
  })
}

export function updateFeeDefinition(data) {
  return request({
    url: '/resi/feeconfig/definition/' + data.id,
    method: 'put',
    data
  })
}

export function delFeeDefinition(id) {
  return request({
    url: '/resi/feeconfig/definition/' + id,
    method: 'delete'
  })
}

export function selectFeeDefinition(projectId) {
  return request({
    url: '/resi/feeconfig/definition/select',
    method: 'get',
    params: { projectId }
  })
}

export function previewFormula(data) {
  return request({
    url: '/resi/feeconfig/definition/preview-formula',
    method: 'post',
    data
  })
}

// ==================== 费用分配 ====================

export function listFeeAllocation(query) {
  return request({
    url: '/resi/feeconfig/allocation',
    method: 'get',
    params: query
  })
}

export function getFeeAllocation(id) {
  return request({
    url: '/resi/feeconfig/allocation/' + id,
    method: 'get'
  })
}

export function addFeeAllocation(data) {
  return request({
    url: '/resi/feeconfig/allocation',
    method: 'post',
    data
  })
}

export function updateFeeAllocation(data) {
  return request({
    url: '/resi/feeconfig/allocation/' + data.id,
    method: 'put',
    data
  })
}

export function delFeeAllocation(id) {
  return request({
    url: '/resi/feeconfig/allocation/' + id,
    method: 'delete'
  })
}

export function batchAllocate(data) {
  return request({
    url: '/resi/feeconfig/allocation/batch',
    method: 'post',
    data
  })
}

export function previewBatchAllocate(params) {
  return request({
    url: '/resi/feeconfig/allocation/preview',
    method: 'get',
    params
  })
}

// ==================== 票据配置 ====================

export function listTicketConfig(query) {
  return request({
    url: '/resi/feeconfig/ticket',
    method: 'get',
    params: query
  })
}

export function getTicketConfig(id) {
  return request({
    url: '/resi/feeconfig/ticket/' + id,
    method: 'get'
  })
}

export function addTicketConfig(data) {
  return request({
    url: '/resi/feeconfig/ticket',
    method: 'post',
    data
  })
}

export function updateTicketConfig(data) {
  return request({
    url: '/resi/feeconfig/ticket/' + data.id,
    method: 'put',
    data
  })
}

export function delTicketConfig(id) {
  return request({
    url: '/resi/feeconfig/ticket/' + id,
    method: 'delete'
  })
}

export function getTicketDefaultFields() {
  return request({
    url: '/resi/feeconfig/ticket/default-fields',
    method: 'get'
  })
}

// ==================== 折扣配置 ====================

export function listDiscount(query) {
  return request({
    url: '/resi/feeconfig/discount',
    method: 'get',
    params: query
  })
}

export function getDiscount(id) {
  return request({
    url: '/resi/feeconfig/discount/' + id,
    method: 'get'
  })
}

export function addDiscount(data) {
  return request({
    url: '/resi/feeconfig/discount',
    method: 'post',
    data
  })
}

export function updateDiscount(data) {
  return request({
    url: '/resi/feeconfig/discount/' + data.id,
    method: 'put',
    data
  })
}

export function delDiscount(id) {
  return request({
    url: '/resi/feeconfig/discount/' + id,
    method: 'delete'
  })
}
