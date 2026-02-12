import { defineStore } from 'pinia'
import { loginApi, registerApi } from '../api/auth'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('rd_token') || '',
    username: localStorage.getItem('rd_user') || ''
  }),
  getters: {
    isAuthed: (state) => !!state.token
  },
  actions: {
    async login(payload) {
      const result = await loginApi(payload)
      this.token = result.token
      this.username = payload.username
      localStorage.setItem('rd_token', this.token)
      localStorage.setItem('rd_user', this.username)
    },
    async register(payload) {
      return registerApi(payload)
    },
    logout() {
      this.token = ''
      this.username = ''
      localStorage.removeItem('rd_token')
      localStorage.removeItem('rd_user')
    }
  }
})
