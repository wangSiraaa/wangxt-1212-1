import request from '@/utils/request'

export const getWasteRecoveryPage = (params) => {
  return request.get('/waste-recovery/page', { params })
}

export const getWasteRecoveryById = (id) => {
  return request.get(`/waste-recovery/${id}`)
}

export const getWasteRecoveryByNo = (recoveryNo) => {
  return request.get(`/waste-recovery/no/${recoveryNo}`)
}

export const addWasteRecovery = (data) => {
  return request.post('/waste-recovery', data)
}

export const updateWasteRecovery = (data) => {
  return request.put('/waste-recovery', data)
}

export const deleteWasteRecovery = (id) => {
  return request.delete(`/waste-recovery/${id}`)
}

export const testWasteRecovery = (id, concentration, phValue, contaminationLevel) => {
  return request.post(`/waste-recovery/${id}/test`, null, { params: { concentration, phValue, contaminationLevel } })
}

export const qualifyWasteRecovery = (id, disposalMethod) => {
  return request.post(`/waste-recovery/${id}/qualify`, null, { params: { disposalMethod } })
}

export const unqualifyWasteRecovery = (id) => {
  return request.post(`/waste-recovery/${id}/unqualify`)
}

export const getTodayWasteRecoveries = () => {
  return request.get('/waste-recovery/today')
}

export const getPendingWasteRecoveries = () => {
  return request.get('/waste-recovery/pending')
}

export const getWastePage = (params) => getWasteRecoveryPage(params)
export const getWasteById = (id) => getWasteRecoveryById(id)
export const getWasteByNo = (no) => getWasteRecoveryByNo(no)
export const addWaste = (data) => addWasteRecovery(data)
export const updateWaste = (data) => updateWasteRecovery(data)
export const deleteWaste = (id) => deleteWasteRecovery(id)
export const getWasteList = (params) => {
  return request.get('/waste-recovery/page', { params: { ...params, pageNum: 1, pageSize: 1000 } }).then(res => res.records || [])
}
