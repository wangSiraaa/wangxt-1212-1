import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'DashboardIndex',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '实时看板', icon: 'dashboard' }
      }
    ]
  },
  {
    path: '/flight',
    name: 'Flight',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'FlightIndex',
        component: () => import('@/views/flight/index.vue'),
        meta: { title: '航班管理', icon: 'flight' }
      }
    ]
  },
  {
    path: '/vehicle',
    name: 'Vehicle',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'VehicleIndex',
        component: () => import('@/views/vehicle/index.vue'),
        meta: { title: '车辆管理', icon: 'vehicle' }
      }
    ]
  },
  {
    path: '/batch',
    name: 'Batch',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'BatchIndex',
        component: () => import('@/views/batch/index.vue'),
        meta: { title: '除冰液批次', icon: 'batch' }
      }
    ]
  },
  {
    path: '/dispatch',
    name: 'Dispatch',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'DispatchIndex',
        component: () => import('@/views/dispatch/index.vue'),
        meta: { title: '调度管理', icon: 'dispatch' }
      }
    ]
  },
  {
    path: '/spray',
    name: 'Spray',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'SprayIndex',
        component: () => import('@/views/spray/index.vue'),
        meta: { title: '喷洒记录', icon: 'spray' }
      }
    ]
  },
  {
    path: '/waste',
    name: 'Waste',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'WasteIndex',
        component: () => import('@/views/waste/index.vue'),
        meta: { title: '废液回收', icon: 'waste' }
      }
    ]
  },
  {
    path: '/env',
    name: 'Env',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      {
        path: '',
        name: 'EnvIndex',
        component: () => import('@/views/env/index.vue'),
        meta: { title: '环保检查', icon: 'env' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `${to.meta.title} - 机场冬季除冰液管理系统`
  }
  next()
})

export default router
