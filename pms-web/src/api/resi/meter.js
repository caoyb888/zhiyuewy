import request from '@/utils/request'

// ==================== 抄表记录 ====================

export function listMeterReading(query) {
  return request({
    url: '/resi/meter/reading',
    method: 'get',
    params: query
  })
}

export function getMeterReading(id) {
  return request({
    url: '/resi/meter/reading/' + id,
    method: 'get'
  })
}

export function addMeterReading(data) {
  return request({
    url: '/resi/meter/reading',
    method: 'post',
    data
  })
}

export function updateMeterReading(data) {
  return request({
    url: '/resi/meter/reading/' + data.id,
    method: 'put',
    data
  })
}

export function delMeterReading(id) {
  return request({
    url: '/resi/meter/reading/' + id,
    method: 'delete'
  })
}

// ==================== Excel 导入 ====================

export function downloadImportTemplate() {
  return request({
    url: '/resi/meter/reading/import/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function uploadMeterImport(data) {
  return request({
    url: '/resi/meter/reading/import/upload',
    method: 'post',
    data
  })
}

export function confirmMeterImport(data) {
  return request({
    url: '/resi/meter/reading/import/confirm',
    method: 'post',
    data
  })
}

// ==================== 公摊计算 ====================

export function calcShare(data) {
  return request({
    url: '/resi/meter/reading/calc-share',
    method: 'post',
    data
  })
}

export function sharePreview(params) {
  return request({
    url: '/resi/meter/reading/share-preview',
    method: 'get',
    params
  })
}

// ==================== 入账 ====================

export function billMeterReading(id) {
  return request({
    url: '/resi/meter/reading/bill/' + id,
    method: 'post'
  })
}

export function batchBillMeterReading(data) {
  return request({
    url: '/resi/meter/reading/bill/batch',
    method: 'post',
    data
  })
}
