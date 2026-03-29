import { defineStore } from 'pinia'

import { getCurrentUser, login as loginApi, logout as logoutApi, updateUserProfile as updateUserProfileApi } from '../api/auth'
import { clearToken, getToken, setToken } from '../utils/token'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    initialized: false,
  }),
  actions: {
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      this.initialized = true
    },
    clearUserInfo() {
      this.userInfo = null
      this.initialized = true
    },
    async login(credentials) {
      const response = await loginApi(credentials)
      const token = response?.data?.token

      if (!token) {
        throw new Error('登录成功但未返回 token')
      }

      setToken(token)
      await this.fetchCurrentUser()
      return response
    },
    async fetchCurrentUser(force = false) {
      if (!getToken()) {
        this.clearAuth()
        return null
      }

      if (!force && this.userInfo) {
        this.initialized = true
        return this.userInfo
      }

      try {
        const response = await getCurrentUser()
        this.setUserInfo(response?.data ?? null)
        return this.userInfo
      } catch (error) {
        this.clearAuth()
        throw error
      }
    },
    async updateProfile(profile) {
      const response = await updateUserProfileApi(profile)
      const nextUserInfo = {
        ...(this.userInfo ?? {}),
        ...(response?.data ?? profile),
      }

      this.setUserInfo(nextUserInfo)
      return nextUserInfo
    },
    async logout() {
      try {
        if (getToken()) {
          await logoutApi()
        }
      } catch {
        // Stateless JWT logout can fall back to local cleanup.
      } finally {
        this.clearAuth()
      }
    },
    clearAuth() {
      clearToken()
      this.clearUserInfo()
    },
  },
})
