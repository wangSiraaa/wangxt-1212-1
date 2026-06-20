import request from '@/utils/request'

export const getDashboardData = () => {
  return request.get('/dashboard/data')
}

export const getDashboardStats = () => {
  return request.get('/dashboard/stats')
}

export const getRealtimeStatus = () => {
  return request.get('/dashboard/realtime')
}

export const refreshDashboard = () => {
  return request.post('/dashboard/refresh')
}
