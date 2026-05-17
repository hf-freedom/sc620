import { createRouter, createWebHistory } from 'vue-router'
import Home from '../views/Home.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/sales-orders',
    name: 'SalesOrders',
    component: () => import('../views/SalesOrders.vue')
  },
  {
    path: '/work-orders',
    name: 'WorkOrders',
    component: () => import('../views/WorkOrders.vue')
  },
  {
    path: '/materials',
    name: 'Materials',
    component: () => import('../views/Materials.vue')
  },
  {
    path: '/quality-inspections',
    name: 'QualityInspections',
    component: () => import('../views/QualityInspections.vue')
  },
  {
    path: '/statistics',
    name: 'Statistics',
    component: () => import('../views/Statistics.vue')
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router
