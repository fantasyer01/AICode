import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/Layout.vue'

const routes = [
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: 'Dashboard' }
      },
      {
        path: 'spring/propagation',
        name: 'SpringPropagation',
        component: () => import('@/views/spring/PropagationDemo.vue'),
        meta: { title: 'Transaction Propagation' }
      },
      {
        path: 'spring/isolation',
        name: 'SpringIsolation',
        component: () => import('@/views/spring/IsolationDemo.vue'),
        meta: { title: 'Isolation Levels' }
      },
      {
        path: 'spring/rollback',
        name: 'SpringRollback',
        component: () => import('@/views/spring/RollbackDemo.vue'),
        meta: { title: 'Rollback Rules' }
      },
      {
        path: 'spring/programmatic',
        name: 'SpringProgrammatic',
        component: () => import('@/views/spring/ProgrammaticDemo.vue'),
        meta: { title: 'Programmatic Transaction' }
      },
      {
        path: 'spring/batch',
        name: 'SpringBatch',
        component: () => import('@/views/spring/BatchTransactionDemo.vue'),
        meta: { title: 'Batch Transaction' }
      },
      {
        path: 'distributed/seata',
        name: 'DistributedSeata',
        component: () => import('@/views/distributed/SeataDemo.vue'),
        meta: { title: 'Seata Distributed Transaction' }
      },
      {
        path: 'internals/mvcc',
        name: 'InternalsMVCC',
        component: () => import('@/views/internals/MVCCDemo.vue'),
        meta: { title: 'MVCC Visualization' }
      },
      {
        path: 'internals/logs',
        name: 'InternalsLogs',
        component: () => import('@/views/internals/LogsDemo.vue'),
        meta: { title: 'Redo/Undo Logs' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
