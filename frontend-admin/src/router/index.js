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
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('../views/user/Users.vue')
      },
      {
        path: 'questions',
        name: 'Questions',
        component: () => import('../views/question/Questions.vue')
      },
      {
        path: 'subjects',
        name: 'Subjects',
        component: () => import('../views/question/Subjects.vue')
      },
      {
        path: 'exam-papers',
        name: 'ExamPapers',
        component: () => import('../views/exam/ExamPapers.vue')
      },
      {
        path: 'exams',
        name: 'Exams',
        component: () => import('../views/exam/Exams.vue')
      },
      {
        path: 'grading',
        name: 'Grading',
        component: () => import('../views/grading/Grading.vue')
      },
      {
        path: 'manual-grading',
        name: 'ManualGrading',
        component: () => import('../views/grading/ManualGrading.vue')
      },
      {
        path: 'score-statistics',
        name: 'ScoreStatistics',
        component: () => import('../views/statistics/ScoreStatistics.vue')
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
