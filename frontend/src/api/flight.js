import request from '@/utils/request'

export const getFlightPage = (params) => {
  return request.get('/flights/page', { params })
}

export const getFlightById = (id) => {
  return request.get(`/flights/${id}`)
}

export const getFlightByNo = (flightNo) => {
  return request.get(`/flights/no/${flightNo}`)
}

export const addFlight = (data) => {
  return request.post('/flights', data)
}

export const updateFlight = (data) => {
  return request.put('/flights', data)
}

export const deleteFlight = (id) => {
  return request.delete(`/flights/${id}`)
}

export const getPendingDeicingFlights = () => {
  return request.get('/flights/pending-deicing')
}

export const getTodayFlights = () => {
  return request.get('/flights/today')
}

export const checkFlightDeicingStatus = (id) => {
  return request.get(`/flights/${id}/deicing-status`)
}

export const completeFlightDeicing = (id) => {
  return request.post(`/flights/${id}/complete-deicing`)
}

export const getFlightList = (params) => {
  return request.get('/flights/page', { params: { ...params, pageNum: 1, pageSize: 1000 } }).then(res => res.records || [])
}
