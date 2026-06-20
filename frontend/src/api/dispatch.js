import request from '@/utils/request'

export const getDispatchPage = (params) => {
  return request.get('/dispatches/page', { params })
}

export const getDispatchById = (id) => {
  return request.get(`/dispatches/${id}`)
}

export const getDispatchByNo = (dispatchNo) => {
  return request.get(`/dispatches/no/${dispatchNo}`)
}

export const addDispatch = (data) => {
  return request.post('/dispatches', data)
}

export const updateDispatch = (data) => {
  return request.put('/dispatches', data)
}

export const deleteDispatch = (id) => {
  return request.delete(`/dispatches/${id}`)
}

export const startDispatch = (id) => {
  return request.post(`/dispatches/${id}/start`)
}

export const completeDispatch = (id) => {
  return request.post(`/dispatches/${id}/complete`)
}

export const cancelDispatch = (id) => {
  return request.post(`/dispatches/${id}/cancel`)
}

export const getTodayDispatches = () => {
  return request.get('/dispatches/today')
}

export const getPendingDispatches = () => {
  return request.get('/dispatches/pending')
}

export const getDispatchList = (params) => {
  return request.get('/dispatches/page', { params: { ...params, pageNum: 1, pageSize: 1000 } }).then(res => res.records || [])
}
