import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('../App.vue'),
    children: [
      {
        path: 'exams',
        name: 'Exams',
        component: () => import('../views/Exams.vue')
      },
      {
        path: 'exam/:id',
        name: 'ExamDetail',
        component: () => import('../views/ExamDetail.vue')
      },
      {
        path: 'exam/:id/answer',
        name: 'ExamAnswer',
        component: () => import('../views/ExamAnswer.vue')
      },
      {
        path: 'results',
        name: 'Results',
        component: () => import('../views/Results.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
