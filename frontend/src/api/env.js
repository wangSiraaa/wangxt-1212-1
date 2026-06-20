import request from '@/utils/request'

export const getEnvCheckPage = (params) => {
  return request.get('/env-checks/page', { params })
}

export const getEnvCheckById = (id) => {
  return request.get(`/env-checks/${id}`)
}

export const getEnvCheckByNo = (checkNo) => {
  return request.get(`/env-checks/no/${checkNo}`)
}

export const addEnvCheck = (data) => {
  return request.post('/env-checks', data)
}

export const updateEnvCheck = (data) => {
  return request.put('/env-checks', data)
}

export const deleteEnvCheck = (id) => {
  return request.delete(`/env-checks/${id}`)
}

export const checkEnvironmentalQualification = (id) => {
  return request.get(`/env-checks/${id}/qualification`)
}

export const performEnvCheck = (id, concentration, phValue, codValue, bodValue) => {
  return request.post(`/env-checks/${id}/perform`, null, { params: { concentration, phValue, codValue, bodValue } })
}

export const closeEnvCheck = (id) => {
  return request.post(`/env-checks/${id}/close`)
}

export const recheckEnvCheck = (id) => {
  return request.post(`/env-checks/${id}/recheck`)
}

export const getTodayEnvChecks = () => {
  return request.get('/env-checks/today')
}

export const getOpenEnvChecks = () => {
  return request.get('/env-checks/open')
}

export const getEnvPage = (params) => getEnvCheckPage(params)
export const getEnvById = (id) => getEnvCheckById(id)
export const getEnvByNo = (no) => getEnvCheckByNo(no)
export const closeCheck = (id) => closeEnvCheck(id)
export const getEnvList = (params) => {
  return request.get('/env-checks/page', { params: { ...params, pageNum: 1, pageSize: 1000 } }).then(res => res.records || [])
}
