import request from '@/utils/request'

export const getVehiclePage = (params) => {
  return request.get('/vehicles/page', { params })
}

export const getVehicleById = (id) => {
  return request.get(`/vehicles/${id}`)
}

export const getVehicleByNo = (vehicleNo) => {
  return request.get(`/vehicles/no/${vehicleNo}`)
}

export const addVehicle = (data) => {
  return request.post('/vehicles', data)
}

export const updateVehicle = (data) => {
  return request.put('/vehicles', data)
}

export const deleteVehicle = (id) => {
  return request.delete(`/vehicles/${id}`)
}

export const getAvailableVehicles = () => {
  return request.get('/vehicles/available')
}

export const updateVehicleStatus = (id, status) => {
  return request.post(`/vehicles/${id}/status`, null, { params: { status } })
}

export const updateFluidVolume = (id, volume) => {
  return request.post(`/vehicles/${id}/fluid-volume`, null, { params: { volume } })
}

export const getVehicleList = (params) => {
  return request.get('/vehicles/page', { params: { ...params, pageNum: 1, pageSize: 1000 } }).then(res => res.records || [])
}
