import request from '@/utils/request'

export const getSprayPage = (params) => {
  return request.get('/sprays/page', { params })
}

export const getSprayById = (id) => {
  return request.get(`/sprays/${id}`)
}

export const getSprayByNo = (sprayNo) => {
  return request.get(`/sprays/no/${sprayNo}`)
}

export const addSpray = (data) => {
  return request.post('/sprays', data)
}

export const updateSpray = (data) => {
  return request.put('/sprays', data)
}

export const deleteSpray = (id) => {
  return request.delete(`/sprays/${id}`)
}

export const startSpray = (id) => {
  return request.post(`/sprays/${id}/start`)
}

export const completeSpray = (id, sprayVolume, deicingEffect) => {
  return request.post(`/sprays/${id}/complete`, null, { params: { sprayVolume, deicingEffect } })
}

export const cancelSpray = (id) => {
  return request.post(`/sprays/${id}/cancel`)
}

export const getTodaySprays = () => {
  return request.get('/sprays/today')
}

export const getInProgressSprays = () => {
  return request.get('/sprays/in-progress')
}
