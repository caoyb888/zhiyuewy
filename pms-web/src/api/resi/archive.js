import request from '@/utils/request'

// ==================== 项目管理 ====================

export function listProject(query) {
  return request({
    url: '/resi/archive/project',
    method: 'get',
    params: query
  })
}

export function getProject(id) {
  return request({
    url: '/resi/archive/project/' + id,
    method: 'get'
  })
}

export function addProject(data) {
  return request({
    url: '/resi/archive/project',
    method: 'post',
    data
  })
}

export function updateProject(data) {
  return request({
    url: '/resi/archive/project/' + data.id,
    method: 'put',
    data
  })
}

export function delProject(id) {
  return request({
    url: '/resi/archive/project/' + id,
    method: 'delete'
  })
}

// ==================== 楼栋管理 ====================

export function listBuilding(query) {
  return request({
    url: '/resi/archive/building',
    method: 'get',
    params: query
  })
}

export function getBuilding(id) {
  return request({
    url: '/resi/archive/building/' + id,
    method: 'get'
  })
}

export function addBuilding(data) {
  return request({
    url: '/resi/archive/building',
    method: 'post',
    data
  })
}

export function updateBuilding(data) {
  return request({
    url: '/resi/archive/building/' + data.id,
    method: 'put',
    data
  })
}

export function delBuilding(id) {
  return request({
    url: '/resi/archive/building/' + id,
    method: 'delete'
  })
}

export function treeselectBuilding(projectId) {
  return request({
    url: '/resi/archive/building/treeselect',
    method: 'get',
    params: { projectId }
  })
}

// ==================== 房间管理 ====================

export function listRoom(query) {
  return request({
    url: '/resi/archive/room',
    method: 'get',
    params: query
  })
}

export function getRoom(id) {
  return request({
    url: '/resi/archive/room/' + id,
    method: 'get'
  })
}

export function addRoom(data) {
  return request({
    url: '/resi/archive/room',
    method: 'post',
    data
  })
}

export function updateRoom(data) {
  return request({
    url: '/resi/archive/room/' + data.id,
    method: 'put',
    data
  })
}

export function delRoom(id) {
  return request({
    url: '/resi/archive/room/' + id,
    method: 'delete'
  })
}

export function searchRoom(keyword, projectId) {
  return request({
    url: '/resi/archive/room/search',
    method: 'get',
    params: { keyword, projectId }
  })
}

export function importRoomTemplate() {
  return request({
    url: '/resi/archive/room/import/template',
    method: 'get',
    responseType: 'blob'
  })
}

export function uploadRoomImport(data) {
  return request({
    url: '/resi/archive/room/import/upload',
    method: 'post',
    data
  })
}

export function confirmRoomImport(data) {
  return request({
    url: '/resi/archive/room/import/confirm',
    method: 'post',
    data
  })
}

// ==================== 客户管理 ====================

export function listCustomer(query) {
  return request({
    url: '/resi/archive/customer',
    method: 'get',
    params: query
  })
}

export function getCustomer(id) {
  return request({
    url: '/resi/archive/customer/' + id,
    method: 'get'
  })
}

export function addCustomer(data) {
  return request({
    url: '/resi/archive/customer',
    method: 'post',
    data
  })
}

export function updateCustomer(data) {
  return request({
    url: '/resi/archive/customer/' + data.id,
    method: 'put',
    data
  })
}

export function delCustomer(id) {
  return request({
    url: '/resi/archive/customer/' + id,
    method: 'delete'
  })
}

export function bindCustomerAsset(data) {
  return request({
    url: '/resi/archive/customer/bind-asset',
    method: 'post',
    data
  })
}

export function unbindCustomerAsset(id) {
  return request({
    url: '/resi/archive/customer/bind-asset/' + id,
    method: 'delete'
  })
}

export function getCustomerAssets(customerId) {
  return request({
    url: '/resi/archive/customer/' + customerId + '/assets',
    method: 'get'
  })
}

// ==================== 仪表档案 ====================

export function listMeterDevice(query) {
  return request({
    url: '/resi/archive/meter-device',
    method: 'get',
    params: query
  })
}

export function getMeterDevice(id) {
  return request({
    url: '/resi/archive/meter-device/' + id,
    method: 'get'
  })
}

export function addMeterDevice(data) {
  return request({
    url: '/resi/archive/meter-device',
    method: 'post',
    data
  })
}

export function updateMeterDevice(data) {
  return request({
    url: '/resi/archive/meter-device/' + data.id,
    method: 'put',
    data
  })
}

export function delMeterDevice(id) {
  return request({
    url: '/resi/archive/meter-device/' + id,
    method: 'delete'
  })
}

// ==================== 车场/车位管理 ====================

export function listParkingArea(query) {
  return request({
    url: '/resi/archive/parking-area',
    method: 'get',
    params: query
  })
}

export function listParkingSpace(query) {
  return request({
    url: '/resi/archive/parking-space',
    method: 'get',
    params: query
  })
}

// ==================== 房屋过户 ====================

export function transferRoom(data) {
  return request({
    url: '/resi/archive/room/transfer',
    method: 'post',
    data
  })
}

export function queryTransferList(params) {
  return request({
    url: '/resi/archive/room/transfer/query',
    method: 'get',
    params
  })
}

export function getTransferHistory(roomId) {
  return request({
    url: '/resi/archive/room/transfer/history/' + roomId,
    method: 'get'
  })
}
