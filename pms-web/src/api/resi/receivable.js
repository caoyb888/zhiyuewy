import request from '@/utils/request'

// ==================== 应收管理 ====================

export function listReceivable(query) {
  return request({
    url: '/resi/receivable',
    method: 'get',
    params: query
  })
}

export function getReceivable(id) {
  return request({
    url: '/resi/receivable/' + id,
    method: 'get'
  })
}

export function createTempReceivable(data) {
  return request({
    url: '/resi/receivable/create-temp',
    method: 'post',
    data
  })
}

export function delReceivable(id) {
  return request({
    url: '/resi/receivable/' + id,
    method: 'delete'
  })
}

// ==================== 批量生成 ====================

export function generateReceivable(data) {
  return request({
    url: '/resi/receivable/generate',
    method: 'post',
    data
  })
}

export function deleteGenerateBatch(genBatch) {
  return request({
    url: '/resi/receivable/generate/' + genBatch,
    method: 'delete'
  })
}

// ==================== 调账 ====================

export function adjustReceivable(id, data) {
  return request({
    url: '/resi/receivable/' + id + '/adjust',
    method: 'put',
    data
  })
}
