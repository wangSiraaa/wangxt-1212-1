import request from '@/utils/request'

export const getBatchPage = (params) => {
  return request.get('/batches/page', { params })
}

export const getBatchById = (id) => {
  return request.get(`/batches/${id}`)
}

export const getBatchByNo = (batchNo) => {
  return request.get(`/batches/no/${batchNo}`)
}

export const addBatch = (data) => {
  return request.post('/batches', data)
}

export const updateBatch = (data) => {
  return request.put('/batches', data)
}

export const deleteBatch = (id) => {
  return request.delete(`/batches/${id}`)
}

export const getAvailableBatches = () => {
  return request.get('/batches/available')
}

export const getAvailableBatchesByType = (fluidType) => {
  return request.get(`/batches/available/type/${fluidType}`)
}

export const checkBatchAvailability = (id, ambientTemperature) => {
  return request.get(`/batches/${id}/availability`, { params: { ambientTemperature } })
}

export const validateTemperatureRange = (id, ambientTemperature) => {
  return request.post(`/batches/${id}/validate-temperature`, null, { params: { ambientTemperature } })
}

export const consumeBatchVolume = (id, volume) => {
  return request.post(`/batches/${id}/consume`, null, { params: { volume } })
}

export const returnBatchVolume = (id, volume) => {
  return request.post(`/batches/${id}/return`, null, { params: { volume } })
}

export const getBatchList = (params) => {
  return request.get('/batches/page', { params: { ...params, pageNum: 1, pageSize: 1000 } }).then(res => res.records || [])
}
