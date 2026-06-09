import { defineStore } from 'pinia'
import axios from 'axios'

export const useAuthStore = defineStore('auth', {
    state: () => ({
        user: JSON.parse(localStorage.getItem('user')) || null,
        token: localStorage.getItem('token') || null,
    }),
    getters: {
        isAuthenticated: (state) => !!state.token,
        isAdmin: (state) => state.user?.role === 'ADMIN',
        isLandlord: (state) => state.user?.role === 'LANDLORD',
    },
    actions: {
        async login(username, password) {
            try {
                const res = await axios.post('/api/auth/login', { username, password })
                if (res.data.code === 200) {
                    this.token = res.data.data.token
                    this.user = res.data.data.user
                    localStorage.setItem('token', this.token)
                    localStorage.setItem('user', JSON.stringify(this.user))
                    return true
                }
                throw new Error(res.data.message)
            } catch (err) {
                throw err
            }
        },
        logout() {
            this.token = null
            this.user = null
            localStorage.removeItem('token')
            localStorage.removeItem('user')
        }
    }
})
