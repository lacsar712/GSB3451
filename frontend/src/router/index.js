import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'home',
            component: HomeView
        },
        {
            path: '/login',
            name: 'login',
            component: () => import('../views/LoginView.vue')
        },
        {
            path: '/register',
            name: 'register',
            component: () => import('../views/RegisterView.vue')
        },
        {
            path: '/forgot-password',
            name: 'forgot-password',
            component: () => import('../views/ForgotPasswordView.vue')
        },
        {
            path: '/reset-password',
            name: 'reset-password',
            component: () => import('../views/ResetPasswordView.vue')
        },
        {
            path: '/house/:id',
            name: 'house-detail',
            component: () => import('../views/HouseDetailView.vue')
        },
        {
            path: '/dashboard',
            component: () => import('../layouts/DashboardLayout.vue'),
            children: [
                {
                    path: 'user',
                    name: 'user-dashboard',
                    component: () => import('../views/dashboard/UserDashboard.vue')
                },
                {
                    path: 'user/contracts',
                    name: 'user-contracts',
                    component: () => import('../views/dashboard/ContractsView.vue')
                },
                {
                    path: 'user/favorites',
                    name: 'user-favorites',
                    component: () => import('../views/dashboard/FavoritesView.vue')
                },
                {
                    path: 'landlord',
                    name: 'landlord-dashboard',
                    component: () => import('../views/dashboard/LandlordDashboard.vue')
                },
                {
                    path: 'landlord/contracts',
                    name: 'landlord-contracts',
                    component: () => import('../views/dashboard/ContractsView.vue')
                },
                {
                    path: 'landlord/appointments',
                    name: 'landlord-appointments',
                    component: () => import('../views/dashboard/AppointmentsView.vue')
                },
                {
                    path: 'admin',
                    name: 'admin-overview',
                    component: () => import('../views/dashboard/AdminOverview.vue')
                },
                {
                    path: 'admin/users',
                    name: 'admin-users',
                    component: () => import('../views/dashboard/AdminUsers.vue')
                },
                {
                    path: 'admin/houses',
                    name: 'admin-houses',
                    component: () => import('../views/dashboard/AdminDashboard.vue')
                },
                {
                    path: 'admin/house/:id',
                    name: 'admin-house-detail',
                    component: () => import('../views/HouseDetailView.vue')
                },
                {
                    path: 'admin/announcements',
                    name: 'admin-announcements',
                    component: () => import('../views/dashboard/AdminAnnouncements.vue')
                },
                {
                    path: 'payments',
                    name: 'payment-management',
                    component: () => import('../views/dashboard/PaymentsView.vue')
                }
            ]
        }
    ]
})

router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('token')
    if (to.path.startsWith('/dashboard') && !token) {
        next('/login')
    } else {
        next()
    }
})

export default router
